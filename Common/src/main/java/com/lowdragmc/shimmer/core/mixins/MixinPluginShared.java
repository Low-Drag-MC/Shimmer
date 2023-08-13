package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.ShimmerConstants;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public interface MixinPluginShared {

	static boolean isClassFound(String className) {
		try {
			Class.forName(className, false, Thread.currentThread().getContextClassLoader());
			ShimmerConstants.LOGGER.debug("find class {}", className);
			return true;
		} catch (ClassNotFoundException e) {
			ShimmerConstants.LOGGER.debug("can't find class {}", className);
			return false;
		}
	}

	private static boolean checkOptifine() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			//knot class loader's name is null
			if (Objects.equals(classLoader.getName(),"TRANSFORMER")) {
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
				return isClassFound("optifine.Installer");
			}
		} catch (Exception e){
			ShimmerConstants.LOGGER.catching(e);
		}
		return false;
	}

	private static boolean doUnderOptifine(boolean underOptifine) {
		if (underOptifine) {
			ShimmerConstants.LOGGER.error("detect shimmer is running under optifine, all the functions are disabled, consider just remove shimmer");
		}
		return underOptifine;
	}

	boolean IS_OPT_LOAD = doUnderOptifine(isClassFound("optifine.OptiFineTranformationService") || checkOptifine());
	boolean IS_DASH_LOADER = isClassFound("dev.quantumfusion.dashloader.mixin.MixinPlugin");

	boolean IS_SODIUM_LOAD = isClassFound("me.jellysquid.mods.sodium.mixin.SodiumMixinPlugin");
	boolean IS_RUBIDIUM_LOAD = IS_SODIUM_LOAD;

	boolean IS_IRIS_LOAD = isClassFound("net.coderbot.iris.compat.sodium.mixin.IrisSodiumCompatMixinPlugin");
	boolean IS_OCULUS_LOAD = IS_IRIS_LOAD;

}
