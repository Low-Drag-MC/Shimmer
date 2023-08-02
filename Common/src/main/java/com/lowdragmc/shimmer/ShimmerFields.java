package com.lowdragmc.shimmer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/**
 * put fields that related to mc stuffs to avoid class loading to early
 */
public class ShimmerFields {
    public static final KeyMapping recordScreenColor = new KeyMapping("shimmer.key.pickColor", InputConstants.KEY_V, "key.categories.misc");
}
