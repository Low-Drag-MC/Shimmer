package com.lowdragmc.shimmer.client.auxiliaryScreen;

import com.google.common.collect.ImmutableMap;
import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_POSITION;

/**
 * the color picker using HSB color space<p>
 * the vertices must be feed by counterclockwise, or they will be clipped
 */
public class HsbColorWidget extends AbstractWidget {

	/**
	 * shader used by this widget, interpolated by vsh->fsh, with HSB2RGB by fsh
	 */
	public static ShaderInstance hsbShader;

	/**
	 * the vertex format for HSB color, three four of float
	 */
	private static final VertexFormatElement HSB_Alpha = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.COLOR, 4);

	public static final VertexFormat HSB_VERTEX_FORMAT = new VertexFormat(
			ImmutableMap.<String, VertexFormatElement>builder()
					.put("Position", ELEMENT_POSITION)
					.put("HSB_ALPHA", HSB_Alpha)
					.build());

	/**
	 * all supported pick mode
	 */
	private enum HSB_MODE {
		H("hue"), S("saturation"), B("brightness");
		private final String name;

		HSB_MODE(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * the length between the main and slide
	 */
	private int gap;
	/**
	 * the slide width
	 */
	private int barWidth;
	/**
	 * hue component, must range from 0f to 360f
	 */
	private float h = 204;
	/**
	 * saturation component, must range from 0f to 1f
	 */
	private float s = 0.72f;
	/**
	 * the brightness component, must range from 0f to 1f
	 */
	private float b = 0.94f;
	/**
	 * thr alpha used for draw main and slide
	 */
	private int alpha = 1;
	/**
	 * the rgb transformed from hsb color space
	 * [0x00rrggbb]
	 */
	private int rgb;

	private HSB_MODE mode = HSB_MODE.H;

	private List<Runnable> listeners = new ArrayList<>();

	public HsbColorWidget(int x, int y, int width, int height, int gap, int barWidth, Component arg) {
		super(x, y, width, height, arg);
		this.gap = gap;
		this.barWidth = barWidth;

		refreshRGB(true);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		Matrix4f pose = poseStack.last().pose();

		BufferBuilder builder = Tesselator.getInstance().getBuilder();
		drawHsbContext(pose, builder);

		renderInfo(poseStack, builder);
	}

	/**
	 * have context for render hsb content
	 */
	private void drawHsbContext(Matrix4f pose, BufferBuilder builder) {
		RenderSystem.setShader(() -> hsbShader);
		builder.begin(VertexFormat.Mode.QUADS, HSB_VERTEX_FORMAT);

		renderMain(pose, builder);
		renderSlide(pose, builder);
		renderColor(pose, builder);

		BufferUploader.drawWithShader(builder.end());
	}

	/**
	 * render the main color, must be called in {@link #drawHsbContext(Matrix4f, BufferBuilder)}
	 */
	private void renderMain(Matrix4f pose, BufferBuilder builder) {
		float _h = 0, _s = 0, _b = 0f;

		{
			//left-up corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 0f;
					_b = 1f;
				}
				case S -> {
					_h = 0f;
					_s = s;
					_b = 1f;
				}
				case B -> {
					_h = 0f;
					_s = 1f;
					_b = b;
				}
			}
			builder.vertex(pose, getX(), getY(), 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();
		}

		{
			//left-down corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 0f;
					_b = 0f;
				}
				case S -> {
					_h = 0f;
					_s = s;
					_b = 0f;
				}
				case B -> {
					_h = 0f;
					_s = 0;
					_b = b;
				}
			}
			builder.vertex(pose, getX(), getY() + height, 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();
		}

		{
			//right-down corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 1f;
					_b = 0f;
				}
				case S -> {
					_h = 360f;
					_s = s;
					_b = 0f;
				}
				case B -> {
					_h = 360f;
					_s = 0f;
					_b = b;
				}
			}
			builder.vertex(pose, getX() + width, getY() + height, 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();
		}

		{
			//right-up corner
			switch (mode) {
				case H -> {
					_h = h;
					_s = 1f;
					_b = 1f;
				}
				case S -> {
					_h = 360f;
					_s = s;
					_b = 1f;
				}
				case B -> {
					_h = 360f;
					_s = 1f;
					_b = b;
				}
			}

			builder.vertex(pose, getX() + width, getY(), 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();
		}
	}

	/**
	 * render the slide, must be called in {@link #drawHsbContext(Matrix4f, BufferBuilder)}
	 */
	private void renderSlide(Matrix4f pose, BufferBuilder builder) {
		float _h = 0f, _s = 0f, _b = 0f;
		var barX = getX() + width + gap;

		{
			//down two corners
			switch (mode) {
				case H -> {
					_h = 0f;
					_s = 1f;
					_b = 1f;
				}
				case S -> {
					_h = h;
					_s = 0f;
					_b = b;
				}
				case B -> {
					_h = h;
					_s = s;
					_b = 0f;
				}
			}
			builder.vertex(pose, barX, getY() + height, 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();

			builder.vertex(pose, barX + barWidth, getY() + height, 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();
		}

		{
			//up two corners
			switch (mode) {
				case H -> {
					_h = 360f;
					_s = 1f;
					_b = 1f;
				}
				case S -> {
					_h = h;
					_s = 1f;
					_b = b;
				}
				case B -> {
					_h = h;
					_s = s;
					_b = 1f;
				}
			}
			builder.vertex(pose, barX + barWidth, getY(), 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();

			builder.vertex(pose, barX, getY(), 0.0f);
			putColor(builder, _h, _s, _b, alpha).nextElement();
			builder.endVertex();
		}


	}

	/**
	 * render hsb/rgb/mode info
	 */
	private void renderInfo(PoseStack poseStack, BufferBuilder builder) {
		Font font = Minecraft.getInstance().font;
		var strX = getX() + width + gap + barWidth + 10;
		var strGapY = (int) Math.max(0, (height - 6f * font.lineHeight) / 5f) + font.lineHeight;
		drawString(poseStack, font, "h:" + (int) h + "°", strX, getY(), 0xffffffff);
		drawString(poseStack, font, "s:" + (int) (s * 100) + "%", strX, getY() + strGapY, 0xffffffff);
		drawString(poseStack, font, "b:" + (int) (b * 100) + "%", strX, getY() + strGapY * 2, 0xffffffff);
		drawString(poseStack, font, "r:" + ((rgb >> 16) & 0xff), strX, getY() + strGapY * 3, 0xffffffff);
		drawString(poseStack, font, "g:" + ((rgb >> 8) & 0xff), strX, getY() + strGapY * 4, 0xffffffff);
		drawString(poseStack, font, "b:" + (rgb & 0xff), strX, getY() + strGapY * 5, 0xffffffff);
		drawString(poseStack, font, "mode:" + mode, strX, getY() + strGapY * 6, 0xffffffff);
	}

	/**
	 * render the indicator color, must be called in {@link #drawHsbContext(Matrix4f, BufferBuilder)}
	 */
	private void renderColor(Matrix4f pose, BufferBuilder builder) {
		var colorX = getX() + width + gap + barWidth + 10 + 30;
		var colorSideLength = 20;
		builder.vertex(pose, colorX, getY(), 0.0f);
		putColor(builder, h, s, b, alpha).nextElement();
		builder.endVertex();

		builder.vertex(pose, colorX, getY() + colorSideLength, 0.0f);
		putColor(builder, h, s, b, alpha).nextElement();
		builder.endVertex();

		builder.vertex(pose, colorX + colorSideLength, getY() + colorSideLength, 0.0f);
		putColor(builder, h, s, b, alpha).nextElement();
		builder.endVertex();

		builder.vertex(pose, colorX + colorSideLength, getY(), 0.0f);
		putColor(builder, h, s, b, alpha).nextElement();
		builder.endVertex();

	}

	/**
	 * put hsb color into BufferBuilder
	 */
	private BufferBuilder putColor(BufferBuilder builder, float h, float s, float b, float alpha) {
		builder.putFloat(0, h);
		builder.putFloat(4, s);
		builder.putFloat(8, b);
		builder.putFloat(12, alpha);
		return builder;
	}

	/**
	 * take the slide into account
	 */
	@Override
	protected boolean clicked(double mouseX, double mouseY) {
		return this.active && this.visible &&
				mouseY >= (double) this.getY() &&
				mouseY < (double) (this.getY() + this.height) && (
				(mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width)) ||
						((mouseX >= (double) this.getX() + this.width + this.gap) &&
								mouseX <= (double) (this.getX() + this.width + this.gap + this.barWidth))
		);
	}

	/**
	 * change mode when right click
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			mode = switch (mode) {
				case H -> HSB_MODE.S;
				case S -> HSB_MODE.B;
				case B -> HSB_MODE.H;
			};
			return true;
		} else return super.mouseClicked(mouseX, mouseY, button);
	}

	/**
	 * calculate rgb color when hsb changed
	 */
	private void refreshRGB(boolean trigListener) {
		rgb = Utils.HSBtoRGB(h / 360f, s, b);
		if (trigListener){
			listeners.forEach(Runnable::run);
		}
	}

	/**
	 * helper method for normalized calculation, a period function with cycle of 2
	 * <p>
	 * y=x when x range from 0 to 1
	 * <p>
	 * y=2-x when x range from 1 to 2
	 *
	 * @param mouse the mouseX/Y
	 * @param pos   x/y position for widget
	 * @param size  width/height position for widget
	 * @return the normalized user friend value
	 */
	private static float normalizeMouse(double mouse, int pos, int size) {
		double x = mouse - pos;
		double y = x % size / size;
		if (y < 0) {
			x = -x;
			y = -y;
		}
		x /= size;
		return (float) (x % 2 > 1 ? 1 - y : y);
	}

	/**
	 * modify when keep pressing the mouse
	 */
	@Override
	public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		float normalizedX = normalizeMouse(mouseX, getX(), width);
		var isBar = mouseX - getX() > width;
		float normalizedY = normalizeMouse(mouseY, getY(), height);
		switch (mode) {
			case H -> {
				if (isBar) {
					h = (1.0f - normalizedY) * 360f;
				} else {
					s = normalizedX;
					b = 1.0f - normalizedY;
				}
			}
			case S -> {
				if (isBar) {
					s = 1.0f - normalizedY;
				} else {
					h = normalizedX * 360f;
					b = 1.0f - normalizedY;
				}
			}
			case B -> {
				if (isBar) {
					b = 1.0f - normalizedY;
				} else {
					h = normalizedX * 360f;
					s = normalizedY;
				}
			}
		}
		refreshRGB(true);
	}

	public float[] getHSB() {
		return new float[]{h, s, b};
	}

	public float[] getRGB() {
		return new float[]{FastColor.ARGB32.red(rgb) / 255f, FastColor.ARGB32.green(rgb) / 255f, FastColor.ARGB32.blue(rgb) / 255f};
	}

	public void setHSB(float[] hsb) {
		if (hsb[0] != this.h || hsb[1] != this.s || hsb[2] != this.b){
			this.h = hsb[0];
			this.s = hsb[1];
			this.b = hsb[2];
			refreshRGB(true);
		}
	}

	public void setRGB(float[] rgb) {
		var hsb = new float[3];
		Utils.RGBtoHSB(rgb, hsb);
		hsb[0] *= 360f;
		setHSB(hsb);
	}

	public int rgb() {
		return rgb;
	}

	public void registerListener(Runnable listener){
		listeners.add(listener);
	}

	/**
	 * for fabric only
	 */
	public static Pair<ShaderInstance, Consumer<ShaderInstance>> registerShaders(ResourceManager resourceManager) {
		try {
			return Pair.of(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "hsb_block").toString(), HSB_VERTEX_FORMAT),
					shaderInstance -> hsbShader = shaderInstance);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
