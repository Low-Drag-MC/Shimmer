package com.lowdragmc.shimmer;

import com.lowdragmc.shimmer.platform.Services;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * can't call {@link Services}'s method to init field here<br>
 * {@link Services}'s constructor need this class and at that time<br>
 * the method call {@link Services#PLATFORM} will return null and cause a {@link NullPointerException}
 */
public class ShimmerConstants {

	public static final String MOD_ID = "shimmer";
	public static final String MOD_NAME = "Shimmer";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static final boolean useOpenGlDebugLabel = true;

	public static final boolean IRIS_COMPACT_ENABLE = true;
	public static final String SHIMMER_IDENTIFIER_MACRO = "shimmer_installed";
	public static final String SHIMMER_SHADERS_PROPERTIES_PREFIX = "shimmer.config.";
	public static final String SHIMMER_SHADERS_PROPERTIES_LIGHTS_IDENTIFIER = "lights";
	public static final String SHIMMER_SHADERS_PROPERTIES_ENV_IDENTIFIER = "env";
}
