package com.lowdragmc.shimmer.config;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.util.FastColor;
import org.apache.http.util.Asserts;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColorReferences extends KeyShared implements Check {
	Object2IntMap<String> namedIntColorMap;
	Map<String, Consumer<Light>> colors;

	private static final String colorReferencesNameRegex = "\\w+";


	public void init() {
		this.namedIntColorMap = this.namedIntColorMap == null ? Object2IntMaps.emptyMap() : Object2IntMaps.unmodifiable(this.namedIntColorMap);

		this.colors = namedIntColorMap.object2IntEntrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, stringEntry -> {

			int intColor = stringEntry.getIntValue();
			int r = FastColor.ARGB32.red(intColor);
			int g = FastColor.ARGB32.green(intColor);
			int b = FastColor.ARGB32.blue(intColor);
			int a = FastColor.ARGB32.alpha(intColor);

			return light -> {
				light.setR(r);
				light.setG(g);
				light.setB(b);
				light.setAlpha(a);
			};
		}));
	}

	public void analyzeReferenceColor(Light light) {
		String color = light.getColorReference();
		if (color == null) return;
		Consumer<Light> setLightColorFunction = colors.get(color);
		setLightColorFunction.accept(light);
	}


	@Override
	public void check() {

	}

	public void checkLight(Light light) {
		String colorReference = light.getColorReference();
		if (colorReference != null) {
			Objects.requireNonNull(colors.get(colorReference), MessageFormat.format("can't find color reference called {0}", colorReference));
		}
	}


	public static void assertHexColorReferenceFormat(String color) {
		Objects.requireNonNull(color, "color reference for hex can't be null");
		Asserts.check(color.length() == 9, "color reference for hex must be formatted in hex #RRGGBBAA");
		Asserts.check(Objects.equals(color.charAt(0), '#'), "the first char of color references must be #");
		Asserts.check(color.substring(1).chars().allMatch(c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')),
				"color reference can only contain 0-9 a-f A-F");
	}

	public static void assertColorName(String colorName) {
		Asserts.check(colorName.matches(colorReferencesNameRegex), "color reference " + colorName + " not match " + colorReferencesNameRegex);
	}

}
