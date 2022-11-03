package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.FastColor;
import org.apache.http.util.Asserts;

class Light extends KeyShared implements Check {
	private int r;
	private int g;
	private int b;
	@SerializedName(value = "a", alternate = {"alpha"})
	public int alpha = 255;
	public float radius;

	public int color() {
		return FastColor.ARGB32.color(alpha, r, g, b);
	}

	@Override
	public void check() {
		Asserts.check(r <= 255 && r >= 0, "red must range in [0,255] for Light");
		Asserts.check(g <= 255 && g >= 0, "greed must range in [0,255] for Light");
		Asserts.check(b <= 255 && b >= 0, "blue must range in [0,255] for Light");
		Asserts.check(alpha <= 255 && alpha >= 0, "alpha must range in [0,255] for Light");
		Asserts.check(radius >= 0 && radius <= 15, "radius must range in [1,15] for Light");
	}

	public void setRGB(int rgb) {
		this.r = FastColor.ARGB32.red(rgb);
		this.g = FastColor.ARGB32.green(rgb);
		this.b = FastColor.ARGB32.blue(rgb);
	}
}