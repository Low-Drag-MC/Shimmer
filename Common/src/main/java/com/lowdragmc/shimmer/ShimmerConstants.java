package com.lowdragmc.shimmer;

import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShimmerConstants {

	public static final String MOD_ID = "shimmer";
	public static final String MOD_NAME = "Shimmer";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

	public static final KeyMapping recordScreenColor = new KeyMapping("shimmer.key.pickColor", InputConstants.KEY_V, "key.categories.misc");

	public static final boolean useOpenGlDebugLabel = Services.PLATFORM.isDevelopmentEnvironment();

}
