package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.platform.Services;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2022/07/01
 * Added to stop Rubidium & Sodium mixins from trying to load if the mod is not installed. Prevents log spam of Mixin Errors
 */
public class ShimmerFabricMixinPlugin implements IMixinConfigPlugin {
    public static final boolean IS_OPT_LOAD = Services.PLATFORM.isClassFound("net.optifine.reflect.ReflectorClass");

    public static final boolean IS_SODIUM_LOAD = Services.PLATFORM.isClassFound("me.jellysquid.mods.sodium.mixin.SodiumMixinPlugin");

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (IS_OPT_LOAD) return false;
        if (mixinClassName.contains("com.lowdragmc.shimmer.core.mixins.sodium")) {
            return IS_SODIUM_LOAD;
        }
        return true;
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
