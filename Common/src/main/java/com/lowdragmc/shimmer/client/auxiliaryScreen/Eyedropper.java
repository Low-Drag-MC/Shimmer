package com.lowdragmc.shimmer.client.auxiliaryScreen;


import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.Utils;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderSSBO;
import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL15;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public enum Eyedropper {

	ShaderStorageBufferObject {
		public static ShaderSSBO ssbo;
		public static ShaderInstance colorPickShader;
		public static RenderTarget flipTarget;
		private static final int bindingIndex = 5;

		@Override
		public void updateCurrentColor() {
			var window = Minecraft.getInstance().getWindow();

			RenderSystem.assertOnRenderThread();

			updateFlipTarget();

			RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();

			RenderUtils.warpGLDebugLabel("blit_move", () -> RenderUtils.fastBlit(mainRenderTarget, flipTarget));

			RenderUtils.warpGLDebugLabel("blit_back", () -> {
				ssbo.bindBuffer();
				GlStateManager._colorMask(true, true, true, true);
				GlStateManager._disableDepthTest();
				GlStateManager._depthMask(false);

				mainRenderTarget.bindWrite(true);

				colorPickShader.setSampler("DiffuseSampler", flipTarget.getColorTextureId());

				colorPickShader.apply();

				colorPickShader.SCREEN_SIZE.set((float) window.getWidth(), (float) window.getHeight());

				GlStateManager._enableBlend();
				RenderSystem.defaultBlendFunc();

				Tesselator tesselator = RenderSystem.renderThreadTesselator();
				BufferBuilder bufferbuilder = tesselator.getBuilder();

				bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
				bufferbuilder.vertex(-1, 1, 0).endVertex();
				bufferbuilder.vertex(-1, -1, 0).endVertex();
				bufferbuilder.vertex(1, -1, 0).endVertex();
				bufferbuilder.vertex(1, 1, 0).endVertex();

				bufferbuilder.end();

				BufferUploader._endInternal(bufferbuilder);
				colorPickShader.clear();

				GlStateManager._depthMask(true);
				GlStateManager._colorMask(true, true, true, true);
				GlStateManager._enableDepthTest();
				ssbo.unBindBuffer();
			});

			ssbo.getSubData(0, Eyedropper.currentColor);

		}

		@Override
		public void setShader(ShaderInstance shader) {
			colorPickShader = shader;
		}

		private void updateFlipTarget() {
			var window = Minecraft.getInstance().getWindow();
			if (flipTarget.width != window.getWidth() || flipTarget.height != window.getHeight()) {
				flipTarget.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
			}
		}

		@Override
		public void init() {
			ssbo = new ShaderSSBO();
			ssbo.createBufferData(32, GL15.GL_DYNAMIC_READ);

			Objects.requireNonNull(colorPickShader, "colorPickShader should never be null");
			ssbo.bindToShader(colorPickShader.getId(), 0, bindingIndex);
			ssbo.bindIndex(bindingIndex);

			flipTarget = new RenderTarget(false) {
			};
			updateFlipTarget();
		}

		@Override
		public void destroy() {
			if (flipTarget != null) {
				flipTarget.destroyBuffers();
				flipTarget = null;
			}
			if (ssbo != null) {
				ssbo.close();
				ssbo = null;
			}
		}

		@Override
		public String modeName() {
			return "ShaderStorageBufferObject";
		}
	},
	DOWNLOAD {

		private static NativeImage nativeImage;
		private static int lastWidth;
		private static int lastHeight;
		private static int openCount = 0;
		private static int closeCount = 0;

		@Override
		public void updateCurrentColor() {
			RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();

			if (lastWidth != renderTarget.width || lastHeight != renderTarget.height) {
				if (nativeImage != null) {
					nativeImage.close();
					closeCount++;
				}
				nativeImage = new NativeImage(renderTarget.width, renderTarget.height, false);
				openCount++;
				lastWidth = renderTarget.width;
				lastHeight = renderTarget.height;
			}

			RenderSystem.bindTexture(renderTarget.getColorTextureId());
			nativeImage.downloadTexture(0, true);
			nativeImage.flipY();

			Window window = Minecraft.getInstance().getWindow();

			int rgba = nativeImage.getPixelRGBA(window.getWidth() / 2, window.getHeight() / 2);
			currentColor[0] = NativeImage.getR(rgba) / 255f;
			currentColor[1] = NativeImage.getG(rgba) / 255f;
			currentColor[2] = NativeImage.getB(rgba) / 255f;

			if (Math.abs(closeCount - openCount) >= 5) {
				throw new RuntimeException();
			}
		}

		@Override
		public void setShader(ShaderInstance shader) {

		}

		@Override
		public void init() {
		}

		@Override
		public void destroy() {
			if (nativeImage != null) {
				nativeImage.close();
				nativeImage = null;
				closeCount++;
			}
			lastWidth = -1;
			lastHeight = -1;
		}

		@Override
		public String modeName() {
			return "glGetTexImage";
		}
	};


	private static boolean dataAvailable = false;
	private static final float[] eyedropperColor = new float[3];

	private static float[] currentColor = new float[3];


	public static Eyedropper mode = ShaderSSBO.support() ? ShaderStorageBufferObject : DOWNLOAD;

	private static boolean enable = false;
	private static boolean readyForRecord = true;

	private static final String colorPreviewChar = Util.make(() -> {
		var holder = "⬛";
		if (Services.PLATFORM.isModLoaded("modernui")) {
			holder = "\u200c" + holder + "\u200c";
		}
		return holder;
	});

	private static Component makeColorPreview(float[] color) {
		return new TextComponent(colorPreviewChar).withStyle((style) -> style.withColor(Utils.pack(color)));
	}

	protected abstract void updateCurrentColor();

	public static void update(PoseStack matrixStack) {
		if (enable) {
			mode.updateCurrentColor();
			mode.renderIndicator(matrixStack);

			if (ShimmerConstants.recordScreenColor.isDown() && readyForRecord) {
				eyedropperColor[0] = currentColor[0];
				eyedropperColor[1] = currentColor[1];
				eyedropperColor[2] = currentColor[2];
				dataAvailable = true;
				readyForRecord = false;
				Minecraft.getInstance().player.sendMessage(new TextComponent("set color " + formatRGB(eyedropperColor))
						.append(makeColorPreview(eyedropperColor)), Util.NIL_UUID);
			} else if (!ShimmerConstants.recordScreenColor.isDown()) {
				readyForRecord = true;
			}

		}
	}

	private static String formatRGB(float[] color) {
		var r = (int) (color[0] * 255);
		var g = (int) (color[1] * 255);
		var b = (int) (color[2] * 255);
		var str = new StringBuilder();
		str.append("r:").append(r);
		do {
			str.append(' ');
		} while (str.length() != 6);
		str.append("g:").append(g);
		do {
			str.append(' ');
		} while (str.length() != 12);
		str.append("b:").append(b);
		do {
			str.append(' ');
		} while (str.length() != 18);
		return str.toString();
	}

	private void renderIndicator(PoseStack poseStack) {

		var window = Minecraft.getInstance().getWindow();
		var scale = window.getGuiScale();

		var centerX = (int) (window.getWidth() / 2f / scale);
		var centerY = (int) (window.getHeight() / 2f / scale);

		var backWidth = 1;

		RenderUtils.warpGLDebugLabel("draw_back", () ->
				GuiComponent.fill(poseStack, centerX + 10 - backWidth, centerY + 10 - backWidth, centerX + 30 + backWidth, centerY + 10 + 20 + backWidth, 0x7F_FF_FF_FF));

		RenderUtils.warpGLDebugLabel("draw_current_color_block", () ->
				GuiComponent.fill(poseStack, centerX + 10, centerY + 10, centerX + 30, centerY + 10 + 20, Utils.pack(currentColor)));

		if (dataAvailable) {
			RenderUtils.warpGLDebugLabel("draw_selected_color_block", () ->
					GuiComponent.fill(poseStack, centerX + 10 + 10, centerY + 10, centerX + 30, centerY + 10 + 20, Utils.pack(eyedropperColor)));
		}

	}

	public abstract void setShader(ShaderInstance shader);

	public abstract String modeName();

	protected abstract void init();

	protected abstract void destroy();

	/**
	 * for fabric only
	 */
	public static Pair<ShaderInstance, Consumer<ShaderInstance>> registerShaders(ResourceManager resourceManager) {
		try {
			return Pair.of(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "pick_color").toString(), DefaultVertexFormat.POSITION),
					Eyedropper.mode::setShader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void switchState() {
		//called from command execute, but may not on render thread
		RenderSystem.recordRenderCall(() -> {
			if (enable) {
				mode.destroy();
				dataAvailable = false;
				enable = false;
			} else {
				mode.init();
				enable = true;
			}
		});
	}

	public static boolean getState() {
		return enable;
	}


	public static void switchMode(Eyedropper newMode) {
		//called from command execute, but may not on render thread
		RenderSystem.recordRenderCall(() -> {
			if (newMode == mode) {
				Minecraft.getInstance().player.sendMessage(new TextComponent("already in " + mode.modeName()), Util.NIL_UUID);
				return;
			}
			var isEnable = enable;
			if (isEnable) {
				switchState();//close
			}
			switch (newMode) {
				case ShaderStorageBufferObject -> DOWNLOAD.destroy();
				case DOWNLOAD -> ShaderStorageBufferObject.destroy();
			}
			mode = newMode;
			if (isEnable) {
				switchState();//open
			}
			Minecraft.getInstance().player.sendMessage(new TextComponent("switch to " + mode.modeName()), Util.NIL_UUID);
		});
	}

	public static boolean isDataAvailable() {
		return dataAvailable;
	}

	public static float[] getCurrentColor() {
		if (enable) {
			return Arrays.copyOf(currentColor, 3);
		} else {
			throw new RuntimeException("can't get current when eyedropper mode is disabled");
		}
	}

	public static float[] getEyedropperColor() {
		if (dataAvailable) {
			return Arrays.copyOf(eyedropperColor, 3);
		} else {
			throw new RuntimeException("can't get eyedropper color while data is unavailable");
		}
	}

}
