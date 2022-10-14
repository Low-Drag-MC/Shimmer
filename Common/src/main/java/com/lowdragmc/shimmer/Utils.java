package com.lowdragmc.shimmer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

	@NotNull
	public static List<BlockState> getAvailableStates(Map<String,String> state, Block block) {
		var stateDefinition = block.getStateDefinition();
		return stateDefinition.getPossibleStates().stream().filter(blockState ->{
			for (var entry : state.entrySet()){
				Object value = blockState.getValue(stateDefinition.getProperty(entry.getKey()));
				if (value != null && !Objects.equals(value.toString(),entry.getValue())){
					return false;
				}
			}
			return true;
		}).toList();
	}

	public static boolean checkBlockProperties(String configSource, Map<String,String> state, ResourceLocation blockLocation) {
		var block = Registry.BLOCK.get(blockLocation);
		List<String> properties = block.getStateDefinition().getProperties().stream().map(Property::getName).toList();
		var lack = state.keySet().stream().filter(key->!properties.contains(key)).toList();
		if (!lack.isEmpty()){
			ShimmerConstants.LOGGER.error("can't find one or more property for block " + blockLocation + " from" + configSource);
			lack.forEach(item -> ShimmerConstants.LOGGER.error("missing property " + item));
			properties.forEach(item-> ShimmerConstants.LOGGER.error("owning property " + item + "for block" + blockLocation));
			return true;
		}
		return false;
	}

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 1 -> {
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 2 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                }
                case 3 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 4 -> {
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                }
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b);
    }

}
