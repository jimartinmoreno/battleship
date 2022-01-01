package com.odigeo.interview.coding.battleshipapi.contract;

import java.util.Arrays;
import java.util.List;

/**
 * Despliega la flota de barcos
 */

public class DeployShipsCommand {

    // Contiene el jugador
    private String playerId;
    // Contiene la lista de barcos y sus coordenadas
    private List<ShipDeployment> shipsDeploy;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public List<ShipDeployment> getShipsDeploy() {
        return shipsDeploy;
    }

    public void setShipsDeploy(List<ShipDeployment> shipsDeploy) {
        this.shipsDeploy = shipsDeploy;
    }

    /**
     * Clase que contiene las coordenadas de cada barco en el el tablero
     */
    public static class ShipDeployment {

        private String shipType;
        private List<String> coordinates;

        public ShipDeployment() {
        }

        public ShipDeployment(String shipType, String... coordinates) {
            this.shipType = shipType;
            this.coordinates = Arrays.asList(coordinates);
        }

        public String getShipType() {
            return shipType;
        }

        public void setShipType(String shipType) {
            this.shipType = shipType;
        }

        public List<String> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<String> coordinates) {
            this.coordinates = coordinates;
        }
    }
}
