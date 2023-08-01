package com.lowdragmc.shimmer.comp.iris;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.shader.ShaderSSBO;
import com.lowdragmc.shimmer.core.mixins.MixinPluginShared;
import com.lowdragmc.shimmer.platform.Services;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public interface IrisHandle {
    @Nullable Pair<ShaderSSBO, ShaderSSBO> getBuffer();

    /**
     * @param buffers should be ShaderStorageBuffer[]
     */
    void updateInfo(Object buffers);

    void onSSBODestroyed();

    void setLightsIndex(int index);

    void setEnvIndex(int index);

    static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    IrisHandle INSTANCE = make(() -> {
        if (!MixinPluginShared.IS_IRIS_LOAD) return null;
        var platformName = Services.PLATFORM.getPlatformName();
        var classLocation = switch (platformName) {
            case "Fabric" -> "com.lowdragmc.shimmer.fabric.compact.iris.FabricIrisHandle";
            case "Forge" -> "com.lowdragmc.shimmer.fabric.platform.FabricPlatformHelper";
            default -> throw new RuntimeException("unknown loader " + platformName);
        };
        IrisHandle handle = null;
        try {
            handle = (IrisHandle) Class.forName(classLocation).getConstructor().newInstance();
        } catch (Exception e) {
            ShimmerConstants.LOGGER.catching(e);
        }
        return handle;
    });

    boolean underShaderPack();

    static void analyzeShaderProperties(String shaderProperties) {
        shaderProperties.lines().forEach(str -> {
            var s = str.replaceAll(" ", "");
            if (s.indexOf(ShimmerConstants.SHIMMER_SHADERS_PROPERTIES_PREFIX) == 0) {
                var subsequent = s.replace(ShimmerConstants.SHIMMER_SHADERS_PROPERTIES_PREFIX,"");
                var analyze = subsequent.split("=");
                if (analyze.length == 2) {
                    try {
                        final int index = Integer.parseInt(analyze[1]);
                        switch (analyze[0]) {
                            case ShimmerConstants.SHIMMER_SHADERS_PROPERTIES_LIGHTS_IDENTIFIER ->
                                    INSTANCE.setLightsIndex(index);
                            case ShimmerConstants.SHIMMER_SHADERS_PROPERTIES_ENV_IDENTIFIER ->
                                    INSTANCE.setEnvIndex(index);
                        }
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
        });
    }

}
