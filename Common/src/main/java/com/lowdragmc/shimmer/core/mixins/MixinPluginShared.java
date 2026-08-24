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

	private static boolean detectEmbeddium() {
		//Embeddium keeps Sodium's package names, so the plugin class alone can't tell the two apart - the
		//`org.embeddedt` API is what identifies the fork.
		boolean sodiumFamily = isClassFound("me.jellysquid.mods.sodium.mixin.SodiumMixinPlugin");
		boolean embeddium = isClassFound("org.embeddedt.embeddium.api.BlockRendererRegistry");
		if (sodiumFamily && !embeddium) {
			ShimmerConstants.LOGGER.warn("detect a Sodium fork that isn't Embeddium; shimmer's terrain integration " +
					"(chunk bloom and colored light on blocks/fluids) is written against Embeddium and stays off");
		}
		return sodiumFamily && embeddium;
	}

	private static boolean doUnderOptifine(boolean underOptifine) {
		if (underOptifine) {
			ShimmerConstants.LOGGER.error("detect shimmer is running under optifine, all the functions are disabled, consider just remove shimmer");
		}
		return underOptifine;
	}

	boolean IS_OPT_LOAD = doUnderOptifine(isClassFound("optifine.OptiFineTranformationService") || checkOptifine());
	boolean IS_DASH_LOADER = isClassFound("dev.quantumfusion.dashloader.mixin.MixinPlugin");

	/**
	 * Our terrain mixins are written against Embeddium's renderer, which has drifted far enough from
	 * upstream Sodium 0.5.x (different chunk vertex encoders, no {@code ModelQuadUtil#mergeBakedLight})
	 * that applying them to plain Sodium would fail. Covers Embeddium on both Forge and Fabric.
	 */
	boolean IS_EMBEDDIUM_LOAD = detectEmbeddium();

	/**
	 * Iris 1.7 / Oculus 1.7 moved everything from {@code net.coderbot.iris} to {@code net.irisshaders.iris}.
	 * Our iris/oculus mixins are written against the new package, so an older Iris must read as absent -
	 * otherwise they would be applied against classes that no longer exist and take the game down with them.
	 */
	boolean IS_IRIS_LOAD = isClassFound("net.irisshaders.iris.compat.sodium.mixin.IrisSodiumCompatMixinPlugin");
	boolean IS_OCULUS_LOAD = IS_IRIS_LOAD;

}
