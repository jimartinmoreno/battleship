package com.odigeo.interview.coding.battleshipservice.model.ship;

import com.odigeo.interview.coding.battleshipservice.model.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase abstracta de la que heredan todos los tipos de barco
 */
public abstract class Ship {

    private final UUID id; // Id
    private final ShipType shipType; // tipo de barco
    private List<Coordinate> coordinates; //Lista de coordenadas en el tablero del barco

    public Ship(ShipType shipType) {
        this.id = UUID.randomUUID();
        this.shipType = shipType;
    }

    public UUID getId() {
        return id;
    }

    public ShipType getShipType() {
        return shipType;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Coordinate> getCoordinates() {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        }
        return coordinates;
    }
}
