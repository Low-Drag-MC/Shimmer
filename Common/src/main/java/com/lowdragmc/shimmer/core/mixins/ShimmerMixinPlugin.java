package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.platform.Services;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * @author HypherionSA
 * @date 2022/06/07
 * Added to stop Rubidium & Sodium mixins from trying to load if the mod is not installed. Prevents log spam of Mixin Errors
 */
public class ShimmerMixinPlugin implements IMixinConfigPlugin {
    public static final boolean IS_OPT_LOAD = Services.PLATFORM.isClassFound("net.optifine.reflect.ReflectorClass");

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !IS_OPT_LOAD;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

}
