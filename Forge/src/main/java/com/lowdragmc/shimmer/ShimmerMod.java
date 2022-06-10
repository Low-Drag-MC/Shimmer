package com.lowdragmc.shimmer;

import com.lowdragmc.shimmer.client.ClientProxy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.Logger;

@Mod(ShimmerConstants.MOD_ID)
public class ShimmerMod {
    public static final Logger LOGGER = ShimmerConstants.LOGGER;

    public ShimmerMod() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        ForgeShimmerConfig.registerConfig();
    }

    public static boolean isRubidiumLoaded() {
        return ModList.get().isLoaded("rubidium");
    }
}
