package com.lowdragmc.shimmer.fabric.core.mixins.iris;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lowdragmc.shimmer.comp.iris.ShaderpackInjection;
import net.coderbot.iris.shaderpack.ProgramSet;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(ProgramSet.class)
public abstract class ProgramSetMixin {
    @ModifyExpressionValue(
            method = "readProgramSource(Lnet/coderbot/iris/shaderpack/include/AbsolutePackPath;Ljava/util/function/Function;Ljava/lang/String;Lnet/coderbot/iris/shaderpack/ProgramSet;Lnet/coderbot/iris/shaderpack/ShaderProperties;Lnet/coderbot/iris/gl/blending/BlendModeOverride;)Lnet/coderbot/iris/shaderpack/ProgramSource;",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0)
            , remap = false)
    private static Object injectShaderpackVsh(Object value, AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ProgramSet programSet, ShaderProperties properties){
        if (program.equals("gbuffers_terrain") && value instanceof String vsh) {
            return ShaderpackInjection.TERRAIN.injectTerrainVsh(vsh);
        }
        return value;
    }

    @ModifyExpressionValue(
            method = "readProgramSource(Lnet/coderbot/iris/shaderpack/include/AbsolutePackPath;Ljava/util/function/Function;Ljava/lang/String;Lnet/coderbot/iris/shaderpack/ProgramSet;Lnet/coderbot/iris/shaderpack/ShaderProperties;Lnet/coderbot/iris/gl/blending/BlendModeOverride;)Lnet/coderbot/iris/shaderpack/ProgramSource;",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 2)
            , remap = false)
    private static Object injectShaderpackFsh(Object value, AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ProgramSet programSet, ShaderProperties properties){
        if (program.equals("gbuffers_terrain") && value instanceof String fsh) {
            return ShaderpackInjection.TERRAIN.injectTerrainFsh(fsh);
        }
        return value;
    }

}
