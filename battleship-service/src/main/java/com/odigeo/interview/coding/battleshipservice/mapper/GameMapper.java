package com.odigeo.interview.coding.battleshipservice.mapper;

import com.google.gson.*;
import com.odigeo.interview.coding.battleshipservice.model.Cell;
import com.odigeo.interview.coding.battleshipservice.model.Coordinate;
import com.odigeo.interview.coding.battleshipservice.model.Game;
import com.odigeo.interview.coding.battleshipservice.model.ship.Ship;
import com.odigeo.interview.coding.battleshipservice.model.ship.ShipType;
import com.odigeo.interview.coding.battleshipservice.repository.entity.GameEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Mapper between Game model class and GameEntity entity class
 */
@Mapper
public interface GameMapper {

    // Returns an instance of the given mapper type. By convention, a single instance of each mapper is retrieved from
    // the factory and exposed on the mapper interface
    GameMapper INSTANCE = Mappers.getMapper(GameMapper.class);

    GameEntity map(Game source);

    Game map(GameEntity source);

    /**
     * Mapper method to serialize the board cells field to a String to be saved in the repository (GameEntity)
     * @param source
     * @return
     */
    default String serializeField(Cell[][] source) {
        return new Gson().toJson(source);
    }

    /**
     * Mapper method to deserialize the board cells field from a JSON String representation obtained  from the repository to a bidimensional Cell [][] (Game)
     * @param source
     * @return
     */
    default Cell[][] deserializeField(String source) {
        return new GsonBuilder()
                .registerTypeAdapter(Ship.class, new ShipJsonDeserializer())
                .create().fromJson(source, Cell[][].class);
    }

    /**
     * Class representing a custom deserializer for Json for the Ship Class.
     */
    class ShipJsonDeserializer implements JsonDeserializer<Ship> {

        @Override
        public Ship deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            final String shipType = jsonObject.get("shipType").getAsString();
            Ship ship = ShipType.valueOf(shipType).newInstance();
            //Iterator<JsonElement> coordinatesIt = jsonElement.getAsJsonObject().get("coordinates").getAsJsonArray().iterator();
            Iterator<JsonElement> coordinatesIt = jsonObject.get("coordinates").getAsJsonArray().iterator();
            while (coordinatesIt.hasNext()) {
                JsonObject coordinate = (JsonObject) coordinatesIt.next();
                ship.getCoordinates().add(new Coordinate(coordinate.get("value").getAsString(), coordinate.get("column").getAsInt(), coordinate.get("row").getAsInt()));
            }
            return ship;
        }
    }
}
