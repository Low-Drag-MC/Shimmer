package com.lowdragmc.shimmer.fabric;

import com.lowdragmc.shimmer.ShimmerConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Config(name = ShimmerConstants.MOD_ID)
public class FabricShimmerConfig implements ConfigData {
    public final AtomicInteger UBO_OFFSET = new AtomicInteger(1);
    public final AtomicInteger BLOOM_COLOR_ATTACHMENT_NUMBER = new AtomicInteger(1);
    public final AtomicBoolean BLOCK_BLOOM = new AtomicBoolean(true);
    public final AtomicBoolean USE_LIGHT_MAP = new AtomicBoolean(true);
    public final AtomicBoolean ENABLED_COLORED_LIGHT = new AtomicBoolean(true);
    public final AtomicBoolean ENABLE_BLOOM_EFFECT = new AtomicBoolean(true);
    public final AtomicBoolean ADDITIVE_EFFECT = new AtomicBoolean(false);
    public final AtomicBoolean INSERT_SHADER_INFO = new AtomicBoolean(false);

    static {
        AutoConfig.register(FabricShimmerConfig.class, GsonConfigSerializer::new);
    }

    public static final FabricShimmerConfig CONFIG
            = AutoConfig.getConfigHolder(FabricShimmerConfig.class).getConfig();

}
