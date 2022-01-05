package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipapi.contract.*;
import com.odigeo.interview.coding.battleshipapi.event.GameCreatedEvent;
import com.odigeo.interview.coding.battleshipapi.event.GameFireEvent;
import com.odigeo.interview.coding.battleshipservice.exception.*;
import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.repository.GameRepository;
import com.odigeo.interview.coding.battleshipservice.util.ShipDeploymentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

    @Inject
    private CoordinateService coordinateService;

    @Inject
    private FieldService fieldService;

    @Inject
    private KafkaProducerService kafkaProducerService;

    @Inject
    private GameRepository repository;

    @Inject
    private ShipDeploymentValidator shipDeploymentValidator;

    /**
     * Create a new game
     * @param command
     * @return
     */
    public Game newGame(GameStartCommand command) {
        Game game = new Game();
        game.setId(UUID.randomUUID().toString());
        game.setPlayerOneId(command.getPlayerId());
        game.setVsComputer(command.isVsComputer());
        // Se comunica via kafka con el servicio battleship-computer-service que actúa como player 2 cuando seleccionamos
        // jugar contra computer
        if (command.isVsComputer()) {
            kafkaProducerService.publish(new GameCreatedEvent(game.getId()));
        }
        game.setCreatedAt(Instant.now());
        game.setPlayerTurn(1);
        repository.saveOrUpdateGame(game);
        logger.info("New game created {}", game.getId());
        return game;
    }

    /**
     * A new player request to join the game
     * @param gameId
     * @param command
     */
    public void joinGame(String gameId, GameJoinCommand command) {
        Game game = repository.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.getPlayerTwoId() != null) {
            throw new GameJoinException("Another player is already playing this game");
        }

        game.setPlayerTwoId(command.getPlayerId());
        repository.saveOrUpdateGame(game);
    }

    /**
     * Deploys the player ships for a player in the game
     * @param gameId
     * @param command
     */
    public void deployShips(String gameId, DeployShipsCommand command) {
        Game game = repository.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.playerReady(command.getPlayerId())) { // Validate player is set as a player in the game
            throw new ShipsAlreadyDeployedException(command.getPlayerId());
        }
        List<Ship> shipsDeployment = mapShipsDeployment(command.getShipsDeploy());
        shipDeploymentValidator.validate(shipsDeployment); // validate deployed ships
        Cell[][] playerField = fieldService.buildField(shipsDeployment);
        game.setPlayerField(command.getPlayerId(), playerField);

        if (game.playersReady()) {
            game.setStartedAt(Instant.now());
        }

        repository.saveOrUpdateGame(game);
    }

    /**
     * Utility mapper method from ShipDeployment to Ship
     * @param shipDeployments
     * @return
     */
    private List<Ship> mapShipsDeployment(List<DeployShipsCommand.ShipDeployment> shipDeployments) {
        List<Ship> ships = new ArrayList<>();
        for (DeployShipsCommand.ShipDeployment shipDeployment : shipDeployments) {
            try {
                Ship ship = ShipType.getByTypeName(shipDeployment.getShipType()).newInstance();

                // Map from string to coordinate
                ship.setCoordinates(shipDeployment.getCoordinates().stream()
                        .map(coordinate -> coordinateService.decodeCoordinate(coordinate))// service to map from String to Coordinate
                        .collect(Collectors.toList()));

                ships.add(ship);
            } catch (Exception e) {
                throw new ShipDeploymentException(shipDeployment.getShipType(), shipDeployment.getCoordinates(), e);
            }
        }
        return ships;
    }


    /**
     * Manage fire action
     * @param gameId
     * @param command
     * @return
     */
    public GameFireResponse fire(String gameId, GameFireCommand command) {
        Game game = repository.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.isFinished()) { // Check if the game is not finished
            throw new GameFinishedException(game.getWinner());
        }

        if (!game.playersReady()) { // Check if is one of the player of the game
            throw new GameStartException("Players not ready");
        }

        if (!game.isPlayerTurn(command.getPlayerId())) { // Check if it is the player turn
            if (game.isVsComputer() && game.isPlayerTurn(1)) {
                // Ping the computer to avoid rare deadlocks
                kafkaProducerService.publish(new GameFireEvent(game.getId()));
            }
            throw new NotYourTurnException(command.getPlayerId());
        }

        Cell[][] field = game.getOpponentField(command.getPlayerId());
        Coordinate coordinate = coordinateService.decodeCoordinate(command.getCoordinate());
        Cell cell = field[coordinate.getRow()][coordinate.getColumn()];
        GameFireResponse response;
        if (cell.isWater()) {
            cell.hit();
            response = new GameFireResponse(GameFireResponse.FireOutcome.MISS);
        } else {

            cell.hit();
            Ship ship = cell.getShip();

            if (fieldService.isShipSunk(field, ship)) {
                response = new GameFireResponse(GameFireResponse.FireOutcome.SUNK);
                if (fieldService.allShipsSunk(field)) {
                    response.setGameWon(true);
                    game.setWinner(command.getPlayerId());
                    game.setFinishedAt(Instant.now());
                }
            } else {
                response = new GameFireResponse(GameFireResponse.FireOutcome.HIT);
            }
        }

        if (game.isVsComputer() && game.isPlayerTurn(1)) {
            kafkaProducerService.publish(new GameFireEvent(game.getId()));
        }

        game.setNextPlayerTurn();
        repository.saveOrUpdateGame(game);
        return response;
    }
}
