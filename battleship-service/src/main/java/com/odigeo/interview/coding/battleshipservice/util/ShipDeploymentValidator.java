package com.odigeo.interview.coding.battleshipservice.util;

import com.odigeo.interview.coding.battleshipservice.exception.ShipDeploymentException;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;

import javax.inject.Singleton;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Utility class for game validation
 */
@Singleton
public class ShipDeploymentValidator {

    public void validate(List<Ship> ships) {
        numberOfDeployedShips(ships);
        duplicatedDeployment(ships);
        shipsOverlap(ships);
        ships.forEach(ship -> {
            shipLengthIsWrong(ship);
            shipIsOutOfGrid(ship);
            shipIsNotContiguous(ship);
        });
    }

    private void duplicatedDeployment(List<Ship> ships) {
        List<String> deployedTypes = new ArrayList<>();
        for (Ship ship : ships) {
            String shipType = ship.getShipType().getShipTypeName();
            if (deployedTypes.contains(shipType)) {
                throw new ShipDeploymentException(shipType, ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
            }
            deployedTypes.add(shipType);
        }
        // Alternativas

        // Alternativa 1
        //        ships.stream()
        //                .filter(ship -> Collections.frequency(ships, ship) > 1)
        //                .forEach(ship -> {
        //                    throw new ShipDeploymentException(ship.getShipType().getShipTypeName(),
        //                            ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        //                });

        // Alternativa 2
        //        Set<ShipType> deployedShipTypes = new HashSet<>();
        //        ships.stream()
        //                .filter(ship -> !deployedShipTypes.add(ship.getShipType()))
        //                .forEach(ship -> {
        //                    throw new ShipDeploymentException(ship.getShipType().getShipTypeName(),
        //                            ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        //                });

    }

    private void numberOfDeployedShips(List<Ship> shipDeployments) {
        int requiredNumberOfDeployments = ShipType.values().length;
        int providedDeploymentsNumber = shipDeployments.size();
        if (providedDeploymentsNumber != requiredNumberOfDeployments) {
            throw new ShipDeploymentException(String.format("Wrong number of deployed ships: expected %d, got %d", requiredNumberOfDeployments, providedDeploymentsNumber));
        }
    }

    private void shipLengthIsWrong(Ship ship) {
        // Use a set to account for overlapping coordinates
        boolean isLengthWrong = new HashSet<>(ship.getCoordinates()).size() != ship.getShipType().getShipLength();
        if (isLengthWrong) {
            throw new ShipDeploymentException(ship.getShipType().getShipTypeName(), ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        }
    }

    private void shipIsOutOfGrid(Ship ship) {
        boolean isShipOutOfGrid = ship.getCoordinates().stream().anyMatch(ShipDeploymentValidator::coordinateIsOutOfGrid);
        if (isShipOutOfGrid) {
            throw new ShipDeploymentException(ship.getShipType().getShipTypeName(), ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        }
    }

    private static boolean coordinateIsOutOfGrid(Coordinate coordinate) {
        return coordinate.getRow() < 0 ||
                coordinate.getColumn() < 0 ||
                coordinate.getRow() >= GameConfiguration.FIELD_HEIGHT ||
                coordinate.getColumn() >= GameConfiguration.FIELD_WIDTH;
    }

    /**
     * Validate the ship location is either horizontal or vertical
     */
    private void shipIsNotContiguous(Ship ship) {
        boolean shipIsHorizontalContiguous = isHorizontal(ship) && isContiguous(ship, (Coordinate c1, Coordinate c2) -> c1.getColumn() + 1 != c2.getColumn());
        boolean shipIsVerticalContiguous = isVertical(ship) && isContiguous(ship, (Coordinate c1, Coordinate c2) -> c1.getRow() + 1 != c2.getRow());
        if (!shipIsHorizontalContiguous && !shipIsVerticalContiguous) {
            throw new ShipDeploymentException(ship.getShipType().getShipTypeName(), ship.getCoordinates().stream().map(Coordinate::getValue).collect(toList()));
        }
    }

    private boolean isContiguous(Ship ship, BiPredicate<Coordinate, Coordinate> function) {
        List<Coordinate> coordinates = ship.getCoordinates();
        return !IntStream.range(1, coordinates.size())
                .anyMatch(i -> function.test(coordinates.get(i - 1), coordinates.get(i)));
    }

    private boolean isHorizontal(Ship ship) {
        List<Coordinate> coordinates = ship.getCoordinates();
        int firstRow = coordinates.get(0).getRow();
        return coordinates.stream().allMatch(c -> c.getRow() == firstRow);
    }

    private boolean isVertical(Ship ship) {
        List<Coordinate> coordinates = ship.getCoordinates();
        int firstColumn = coordinates.get(0).getColumn();
        return coordinates.stream().allMatch(c -> c.getColumn() == firstColumn);
    }

//    private boolean isHorizontalContiguous(Ship ship) {
//        List<Coordinate> coordinates = ship.getCoordinates();
//        boolean isContiguous = true;
//        for (int i = 1; i < coordinates.size(); i++) {
//            if (coordinates.get(i - 1).getColumn() + 1 != coordinates.get(i).getColumn()) {
//                isContiguous = false;
//                break;
//            }
//        }
//        return isContiguous;
//    }

//    private boolean isVerticalContiguous(Ship ship) {
//        List<Coordinate> coordinates = ship.getCoordinates();
//        boolean isContiguous = true;
//        for (int i = 1; i < coordinates.size(); i++) {
//            if (coordinates.get(i - 1).getRow() + 1 != coordinates.get(i).getRow()) {
//                isContiguous = false;
//                break;
//            }
//        }
//        return isContiguous;
//    }

    private void shipsOverlap(Collection<Ship> deployedShips) {
        List<Coordinate> allCoordinates = deployedShips.stream()
                .flatMap(ship -> ship.getCoordinates().stream())
                .collect(toList());
        Set<Coordinate> reducedCoordinates = new HashSet<>(allCoordinates);

        boolean isOverlapping = allCoordinates.size() != reducedCoordinates.size();
        if (isOverlapping) {
            throw new ShipDeploymentException("Deployed ships overlap - they cannot overlap!");
        }
    }
}