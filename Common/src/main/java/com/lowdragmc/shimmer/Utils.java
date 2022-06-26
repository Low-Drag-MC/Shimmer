package com.lowdragmc.shimmer;

import com.google.gson.JsonObject;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2022/6/21
 * @implNote Utils
 */
public class Utils {

    public static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S state, Property<T> property, String value) {
        Optional<T> optionalT = property.getValue(value);
        if (optionalT.isPresent()) {
            return state.setValue(property, optionalT.get());
        } else {
            ShimmerConstants.LOGGER.warn("Unable to read property: {} with value: {} for state", new Object[]{property.getName(), value});
            return state;
        }
    }

    public static Set<BlockState> getAllPossibleStates(JsonObject jsonObj, Block bb) {
        Set<BlockState> available = new HashSet<>();
        JsonObject state = jsonObj.get("state").getAsJsonObject();
        StateDefinition<Block, BlockState> stateStateDefinition = bb.getStateDefinition();
        for (BlockState possibleState : stateStateDefinition.getPossibleStates()) {
            boolean found = true;
            for (String key : state.keySet()) {
                Property<?> property = stateStateDefinition.getProperty(key);
                if (property == null) {
                    found = false;
                    break;
                } else {
                    Comparable real = possibleState.getValue(property);
                    Optional<?> require = property.getValue(state.get(key).getAsString());
                    if (require.isEmpty() || real.compareTo(require.get()) != 0) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                available.add(possibleState);
            }
        }
        return available;
    }

}
