package com.lowdragmc.shimmer.comp.iris;

import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public enum ShaderpackInjection {
    TERRAIN;

    private final List<Tuple<Predicate<String>, Function<String, String>>> VSH_INJECTIONS = new ArrayList<>();
    private final List<Tuple<Predicate<String>, Function<String, String>>> FSH_INJECTIONS = new ArrayList<>();

    public void registerTerrainVshInjection(Predicate<String> predicate, Function<String, String> injection) {
        VSH_INJECTIONS.add(new Tuple<>(predicate, injection));
    }

    public void registerTerrainFshInjection(Predicate<String> predicate, Function<String, String> injection) {
        FSH_INJECTIONS.add(new Tuple<>(predicate, injection));
    }

    public String injectTerrainVsh(String vsh) {
        for (Tuple<Predicate<String>, Function<String, String>> injection : VSH_INJECTIONS) {
            if (injection.getA().test(vsh)) {
                return injection.getB().apply(vsh);
            }
        }
        return vsh;
    }

    public String injectTerrainFsh(String fsh) {
        for (Tuple<Predicate<String>, Function<String, String>> injection : FSH_INJECTIONS) {
            if (injection.getA().test(fsh)) {
                return injection.getB().apply(fsh);
            }
        }
        return fsh;
    }
}
