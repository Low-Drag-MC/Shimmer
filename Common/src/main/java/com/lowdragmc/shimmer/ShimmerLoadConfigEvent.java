package com.lowdragmc.shimmer;

import java.util.HashMap;
import java.util.Map;

/**
 * called when shimmer reload configuration files<br>
 * for forge: subscriber ForgeShimmerLoadConfigEvent<br>
 * for fabric: use FabricShimmerLoadConfigCallback.EVENT#register
 */
public class ShimmerLoadConfigEvent implements ShimmerEvent{
    final Map<String,String> additionConfigurations = new HashMap<>();

    public void addConfiguration(String modId,String configuration){
        additionConfigurations.put(modId,configuration);
    }

}
