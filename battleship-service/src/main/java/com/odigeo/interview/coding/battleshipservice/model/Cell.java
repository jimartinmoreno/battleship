package com.odigeo.interview.coding.battleshipservice.model;

import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;

/**
 * Contiene cada celda junto con el barco que contiene y si
 */

public class Cell {

    private Ship ship;
    private boolean hit;

    public Cell() {
    }

    public Cell(Ship ship) {
        this.ship = ship;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isWater() {
        return ship == null;
    }

    public void hit() {
        setHit(true);
    }

    @Override
    public String toString() {
        return "Cell{" +
                "ship=" + ship +
                ", hit=" + hit +
                '}';
    }
}
