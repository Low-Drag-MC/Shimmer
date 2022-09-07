package com.lowdragmc.shimmer.forge;

import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.forge.client.ClientProxy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(ShimmerConstants.MOD_ID)
public class ShimmerMod {

    public ShimmerMod() {
        ForgeShimmerConfig.registerConfig();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    public static boolean isRubidiumLoaded() {
        return ModList.get().isLoaded("rubidium");
    }
}
