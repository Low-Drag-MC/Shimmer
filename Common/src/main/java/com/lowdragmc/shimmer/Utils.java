package com.lowdragmc.shimmer;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

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

}
