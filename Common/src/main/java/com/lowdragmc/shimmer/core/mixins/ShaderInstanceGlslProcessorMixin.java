package com.lowdragmc.shimmer.core.mixins;

import com.lowdragmc.shimmer.core.IGlslProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(targets = {"net.minecraft.client.renderer.ShaderInstance$1"})
public class ShaderInstanceGlslProcessorMixin implements IGlslProcessor {
    @Final
    @Shadow
    private Set<String> importedPaths;

    public void shimmer$clearImportedPathRecord() {
        importedPaths.clear();
    }

}
