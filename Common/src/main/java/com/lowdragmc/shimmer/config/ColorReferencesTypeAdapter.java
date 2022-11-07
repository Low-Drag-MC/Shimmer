package com.lowdragmc.shimmer.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.util.FastColor;
import org.apache.http.util.Asserts;

import java.io.IOException;

public class ColorReferencesTypeAdapter extends TypeAdapter<ColorReferences> {
	@Override
	public void write(JsonWriter out, ColorReferences colorReferences) throws IOException {

	}

	@Override
	public ColorReferences read(JsonReader in) throws IOException {
		var colorReference = new ColorReferences();

		colorReference.namedIntColorMap = new Object2IntArrayMap<>();
		in.beginObject();

		while (in.hasNext()) {
			String colorName = in.nextName();
			ColorReferences.assertColorName(colorName);
			switch (in.peek()) {
				case BEGIN_OBJECT -> {
					in.beginObject();
					int r = -1;
					int g = -1;
					int b = -1;
					int a = -1;
					while (in.peek() != JsonToken.END_OBJECT) {
						String colorTypeName = in.nextName();
						switch (colorTypeName) {
							case "r", "red" -> {
								if (r == -1) {
									r = in.nextInt();
									Asserts.check(r >= 0 && r <= 255, "r must range in [0-255]");
								} else {
									throw new RuntimeException("can't set r for multi time");
								}
							}
							case "g", "green" -> {
								if (g == -1) {
									g = in.nextInt();
									Asserts.check(g >= 0 && g <= 255, "g must range in [0-255]");
								} else {
									throw new RuntimeException("can't set rg for multi time");
								}
							}
							case "b", "blue" -> {
								if (b == -1) {
									b = in.nextInt();
									Asserts.check(b >= 0 && b <= 255, "b must range in [0-255]");
								} else {
									throw new RuntimeException("can't set r for multi time");
								}
							}
							case "a", "alpha" -> {
								if (a == -1) {
									a = in.nextInt();
									Asserts.check(a >= 0 && a <= 255, "a must range in [0-255]");
								} else {
									throw new RuntimeException("can't set a for multi time");
								}
							}
							default -> throw new RuntimeException("unknown colorTypeName " + colorTypeName);
						}
					}
					in.endObject();
					colorReference.namedIntColorMap.put(colorName, FastColor.ARGB32.color(a, r, g, b));
				}
				case STRING -> {
					String color = in.nextString();
					ColorReferences.assertHexColorReferenceFormat(color);
					var intColor = Integer.parseInt(color.substring(1, 9), 16);
					var r = (intColor >> (8 * 3)) & 0xFF;
					var g = (intColor >> (8 * 2)) & 0xFF;
					var b = (intColor >> (8)) & 0xFF;
					var a = intColor & 0xFF;

					colorReference.namedIntColorMap.put(colorName, FastColor.ARGB32.color(a, r, g, b));
				}
				default -> throw new RuntimeException("unexpected json token " + in.peek());
			}
		}

		in.endObject();

		return colorReference;
	}


}
