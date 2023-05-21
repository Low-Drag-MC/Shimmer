package com.lowdragmc.shimmer.platform;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.platform.services.IPlatformHelper;
import net.minecraft.client.ClientBrandRetriever;

/**
 * @author HypherionSA
 * @date 2022/06/09
 */
public class Services {

    public static final IPlatformHelper PLATFORM = load();

    private static IPlatformHelper load() {
        String loaderName = ClientBrandRetriever.getClientModName().toLowerCase().trim();
        var classLocation = switch (loaderName) {
            case "forge" -> "com.lowdragmc.shimmer.forge.platform.ForgePlatformHelper";
            case "fabric" -> "com.lowdragmc.shimmer.fabric.platform.FabricPlatformHelper";
            case "quilt" -> {
                ShimmerConstants.LOGGER.warn("quilt detected, just work under fabric");
                ShimmerConstants.LOGGER.warn("behaviour may ne be correct");
                yield "com.lowdragmc.shimmer.fabric.platform.FabricPlatformHelper";
            }
            case "vanilla" -> throw new RuntimeException("run on vanilla?");
            default -> throw new RuntimeException("unknown loader " + loaderName);
        };
        ShimmerConstants.LOGGER.debug("detect loader " + loaderName);
        IPlatformHelper loadedService;
        try {
            loadedService = (IPlatformHelper) Class.forName(classLocation).getConstructor().newInstance();
        } catch (Exception e) {
            ShimmerConstants.LOGGER.error("failed to init PlatformHelper");
            throw new RuntimeException(e);
        }
        ShimmerConstants.LOGGER.debug("Loaded {} for service", loadedService);
        return loadedService;
    }
}
