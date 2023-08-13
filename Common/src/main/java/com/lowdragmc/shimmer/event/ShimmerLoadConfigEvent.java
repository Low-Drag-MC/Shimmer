package com.lowdragmc.shimmer.event;

import com.lowdragmc.shimmer.Configuration;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * called when shimmer reload configuration files<br>
 * for forge: subscriber ForgeShimmerLoadConfigEvent<br>
 * for fabric: use FabricShimmerLoadConfigCallback.EVENT#register
 */
public class ShimmerLoadConfigEvent implements ShimmerEvent{
	/**
	 * key for modid
	 * value for configuration string
	 */
    final Map<String,String> additionConfigurations = new HashMap<>();

	/**
	 * @param modId the modID
	 * @param configuration the literal configuration string
	 */
    public void addConfiguration(String modId,String configuration){
        additionConfigurations.put(modId,configuration);
    }

	/**
	 * @param configurationPath the full resourceLocation of shimmer configuration name should be like modID$path
	 */
	public void addConfiguration(ResourceLocation configurationPath) {
		Configuration.readConfiguration(configurationPath)
				.ifPresent(configString -> additionConfigurations.put(configurationPath.getNamespace(), configString));
    }

	/**
	 * @return the immutable analyzeShaderProperties
	 */
	public Map<String, String> getConfiguration(){
		return Map.copyOf(additionConfigurations);
	}

}
