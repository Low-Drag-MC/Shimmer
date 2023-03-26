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

    public void addConfiguration(String modId,String configuration){
        additionConfigurations.put(modId,configuration);
    }
    public void addConfiguration(ResourceLocation rl) {
		Configuration.configurationOfRl(rl).ifPresent(configString -> additionConfigurations.put(rl.getNamespace(), configString));
    }

	/**
	 * @return the immutable map
	 */
	public Map<String, String> getConfiguration(){
		return Map.copyOf(additionConfigurations);
	}

}
