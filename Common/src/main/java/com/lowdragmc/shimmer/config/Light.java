package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.FastColor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import java.util.Objects;

class Light extends KeyShared implements Check {
	@SerializedName(value = "r", alternate = {"red"})
	private int r;
	@SerializedName(value = "g", alternate = {"green"})
	private int g;
	@SerializedName(value = "b", alternate = {"blue"})
	private int b;
	@SerializedName(value = "a", alternate = {"alpha"})
	public int alpha = 255;
	public float radius;

	private String color;


	public int color() {
		return FastColor.ARGB32.color(alpha, r, g, b);
	}

	@Override
	public void check() {

		if (color != null) {
			Asserts.check(StringUtils.isNoneBlank(color), "color reference can't be blank or null");
			Asserts.check(color.length() >= 2, "color reference must formatted in #referenceName");
			Asserts.check(Objects.equals(color.charAt(0), '#'), "color reference usage must begin with #");
			this.color = color.substring(1);
			ColorReferences.assertColorName(color);
		}else {
			Asserts.check(r <= 255 && r >= 0, "red must range in [0,255] for Light");
			Asserts.check(g <= 255 && g >= 0, "greed must range in [0,255] for Light");
			Asserts.check(b <= 255 && b >= 0, "blue must range in [0,255] for Light");
			Asserts.check(alpha <= 255 && alpha >= 0, "alpha must range in [0,255] for Light");
			Asserts.check(radius >= 0 && radius <= 15, "radius must range in [1,15] for Light");
		}
	}

	public void setRGB(int rgb) {
		this.r = FastColor.ARGB32.red(rgb);
		this.g = FastColor.ARGB32.green(rgb);
		this.b = FastColor.ARGB32.blue(rgb);
	}

	public String getColorReference() {
		return color;
	}

	public void setR(int r) {
		this.r = r;
	}

	public void setG(int g) {
		this.g = g;
	}

	public void setB(int b) {
		this.b = b;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
}