package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.ShimmerConstants;

import java.lang.reflect.InvocationTargetException;

public interface MixinPluginShared {

	static boolean isClassFound(String className) {
		try {
			Class.forName(className, false, Thread.currentThread().getContextClassLoader());
			ShimmerConstants.LOGGER.error("find class {}", className);
			return true;
		} catch (ClassNotFoundException e) {
			ShimmerConstants.LOGGER.error("can't find class {}", className);
			return false;
		}
	}

	private static boolean checkOptifine() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader.getName().equals("TRANSFORMER")) {
			//under forge's TransformingClassLoader
			try {
				//try not to load the class
				var fmlLoaderClass = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
				var getGameLayerMethod = fmlLoaderClass.getMethod("getGameLayer");
				var gameLayer = getGameLayerMethod.invoke(null);
				var configurationMethod = gameLayer.getClass().getMethod("configuration");
				//fully-qualified class name, Configuration is a common name
				var configuration = (java.lang.module.Configuration)configurationMethod.invoke(gameLayer);
                return configuration.toString().contains("optifine");
			} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				ShimmerConstants.LOGGER.catching(e);
			}
			//fall back, this will cause class loading and may prevent subsequent transforming operations
			return isClassFound("net.optifine.shaders.ShadersRender");
        }
		return false;
	}

	private static boolean doUnderOptifine(boolean underOptifine) {
		ShimmerConstants.LOGGER.error("detect shimmer is running under optifine, all the functions are disabled, consider just remove shimmer");
		return underOptifine;
	}

	boolean IS_OPT_LOAD = doUnderOptifine(isClassFound("optifine.OptiFineTranformationService") || checkOptifine());
	boolean IS_DASH_LOADER = isClassFound("dev.quantumfusion.dashloader.mixin.MixinPlugin");

	boolean IS_SODIUM_LOAD = isClassFound("me.jellysquid.mods.sodium.mixin.SodiumMixinPlugin");
	boolean IS_RUBIDIUM_LOAD = IS_SODIUM_LOAD;

}
