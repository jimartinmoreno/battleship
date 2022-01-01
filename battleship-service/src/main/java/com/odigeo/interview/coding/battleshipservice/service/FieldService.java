package com.odigeo.interview.coding.battleshipservice.service;

import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.util.GameConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
public class FieldService {

    @Inject
    private CoordinateService coordinateService;

    /**
     * Método harcoded, imagino que para la prueba del martes
     * @param field
     * @return
     */
    public boolean allShipsSunk(Cell[][] field) {
        
        int totalHits = Arrays.stream(ShipType.values()).map(ShipType::getShipLength).mapToInt(Integer::intValue).sum();
        //System.out.println("totalHits = " + totalHits);

        long hitCount = Arrays.stream(field).flatMap(cells -> Arrays.stream(cells))
                //.peek(System.out::println)
                .filter(Cell::isHit)
                .count();

        //System.out.println("hitCount = " + hitCount);

        return hitCount == totalHits;
    }

    /**
     * Método harcoded, imagino que para la prueba del martes
     *
     * @param field
     * @param ship
     * @return
     */
    public boolean isShipSunk(Cell[][] field, Ship ship) {
        // Check
        //        ship.getCoordinates().stream()
        //                .map(coordinate -> field[coordinate.getRow()][coordinate.getColumn()])
        //                .forEach(System.out::println);

        // Versión 1
        //        return ship.getCoordinates().stream()
        //                .allMatch(coordinate -> field[coordinate.getRow()][coordinate.getColumn()].isHit());

        // Versión 2
        return ship.getCoordinates().stream()
                .allMatch(coordinate -> {
                    Cell cell = field[coordinate.getRow()][coordinate.getColumn()];
                    return cell.isHit() && cell.getShip().getId() == ship.getId();
                });
    }

    public Cell[][] buildField(List<Ship> shipsDeployment) {
        Cell[][] field = buildWater();
        deployShips(field, shipsDeployment);
        return field;
    }

    private Cell[][] buildWater() {
        Cell[][] field = new Cell[GameConfiguration.FIELD_HEIGHT][GameConfiguration.FIELD_WIDTH];
        for (int row = 0; row < GameConfiguration.FIELD_HEIGHT; row++) {
            for (int col = 0; col < GameConfiguration.FIELD_WIDTH; col++) {
                field[row][col] = new Cell();
            }
        }
        return field;
    }

    private void deployShips(Cell[][] field, List<Ship> ships) {
        ships.forEach(ship ->
                ship.getCoordinates().forEach(coordinate ->
                        field[coordinate.getRow()][coordinate.getColumn()] = new Cell(ship)
                )
        );
    }
}
