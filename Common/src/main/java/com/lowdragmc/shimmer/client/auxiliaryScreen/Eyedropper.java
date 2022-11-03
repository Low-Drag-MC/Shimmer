package com.lowdragmc.shimmer.client.auxiliaryScreen;


import com.lowdragmc.shimmer.ShimmerConstants;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.lowdragmc.shimmer.client.shader.ShaderSSBO;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public enum Eyedropper {

	ShaderStorageBufferObject {
		public static ShaderSSBO ssbo;
		public static ShaderInstance colorPickShader;
		public static RenderTarget flipTarget;
		private static float[] colorContainer = new float[3];
		private static int bindingIndex = 5;

		@Override
		public String modeName() {
			return "Shader Storage Buffer Object";
		}

		@Override
		public void updateCurrentColor() {
			var window = Minecraft.getInstance().getWindow();

			RenderSystem.assertOnRenderThread();

			updateFlipTarget();

			RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();

			RenderUtils.warpGLDebugLabel("blit_move", () -> {
				RenderUtils.fastBlit(mainRenderTarget, flipTarget);
			});

			RenderUtils.warpGLDebugLabel("blit_back", () -> {
				ssbo.bindBuffer();
				GlStateManager._colorMask(true, true, true, true);
				GlStateManager._disableDepthTest();
				GlStateManager._depthMask(false);

				mainRenderTarget.bindWrite(true);

				colorPickShader.setSampler("DiffuseSampler", flipTarget.getColorTextureId());

				colorPickShader.apply();

				colorPickShader.SCREEN_SIZE.set((float)window.getWidth(),(float)window.getHeight());

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

				BufferUploader.end(bufferbuilder);
				colorPickShader.clear();

				GlStateManager._depthMask(true);
				GlStateManager._colorMask(true, true, true, true);
				GlStateManager._enableDepthTest();
				ssbo.unBindBuffer();
			});

			RenderUtils.warpGLDebugLabel("get_data", () -> {
				ssbo.getSubData(0, colorContainer);
			});

			Eyedropper.colors = Arrays.copyOf(colorContainer, 3);

		}

		@Override
		public void setShader(ShaderInstance shader) {
			colorPickShader = shader;
		}

		public void updateFlipTarget() {
			var window = Minecraft.getInstance().getWindow();
			if (flipTarget.width != window.getWidth() || flipTarget.height != window.getHeight()) {
				flipTarget.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
			}
		}

		@Override
		public Eyedropper init() {
			ssbo = new ShaderSSBO();
			ssbo.createBufferData(32, GL15.GL_DYNAMIC_READ);

			Objects.requireNonNull(colorPickShader, "colorPickShader should never be null");
			ssbo.bindToShader(colorPickShader.getId(), 0, bindingIndex);
			ssbo.bindIndex(bindingIndex);

			flipTarget = new RenderTarget(false) {
			};
			updateFlipTarget();
			return this;
		}

		@Override
		public void destroy() {
			flipTarget.destroyBuffers();
			flipTarget = null;
			ssbo.close();
			ssbo = null;
		}
	},
	DOWNLOAD {
		@Override
		public String modeName() {
			return "glGetTexImage";
		}

		@Override
		public void updateCurrentColor() {

		}

		@Override
		public void setShader(ShaderInstance shader) {

		}

		@Override
		public Eyedropper init() {
			return this;
		}

		@Override
		public void destroy() {
		}
	};


	protected static int eyedropperredColor;
	protected static boolean dataAvailable = false;

	protected static int currentColor;
	protected static float[] colors = new float[3];


	public static Eyedropper mode = ShaderSSBO.support() ? ShaderStorageBufferObject : DOWNLOAD;

	private static boolean enable = false;

	protected abstract void updateCurrentColor();

	public final void update(PoseStack matrixStack) {
		if (enable) {
			Font font = Minecraft.getInstance().font;
			var window = Minecraft.getInstance().getWindow();
			var scale = window.getGuiScale();
			updateCurrentColor();

//			renderIndicator();

			var message = MessageFormat.format("r:{0},g{1},b{2}", colors[0] * 255, colors[1] * 255, colors[2] * 255);

			RenderUtils.warpGLDebugLabel("render font", () -> {
				font.draw(matrixStack, message, (float) (window.getWidth() / 2f / scale), (float) (window.getHeight() / 2f / scale), DyeColor.BLACK.getTextColor());
			});


			RenderUtils.warpGLDebugLabel("draw_color_block",()->{

				Matrix4f pose = matrixStack.last().pose();

				float x = 50f;
				float y = 20f;
				float width = 50f;
				float height = 50f;
				float z = 0.01f;
				int color = FastColor.ARGB32.color(255, (int) (colors[0] * 255), (int) (colors[1] * 255), (int) (colors[2] * 255));

				RenderSystem.enableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.setShader(GameRenderer::getPositionColorShader);

				Tesselator tessellator = Tesselator.getInstance();
				BufferBuilder buffer = tessellator.getBuilder();
				buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				buffer.vertex(pose, x + width,    y, z).color(color).endVertex();
				buffer.vertex(pose,  x,    y, z).color(color).endVertex();
				buffer.vertex(pose,  x, y+height, z).color(color).endVertex();
				buffer.vertex(pose, x + width, y + height, z).color(color).endVertex();
				tessellator.end();

				RenderSystem.disableBlend();
				RenderSystem.enableTexture();
			});
		}
	}

	public static void renderIndicator() {
		if (dataAvailable) {

		}

	}

	public abstract void setShader(ShaderInstance shader);

	public abstract String modeName();

	protected abstract Eyedropper init();

	protected abstract void destroy();

	/**
	 * for fabric only
	 */
	public static Pair<ShaderInstance, Consumer<ShaderInstance>> registerShaders(ResourceManager resourceManager) {
		try {
			return Pair.of(new ShaderInstance(resourceManager, new ResourceLocation(ShimmerConstants.MOD_ID, "pick_color").toString(), DefaultVertexFormat.POSITION),
					shaderInstance -> mode.setShader(shaderInstance));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void switchState() {
		RenderSystem.recordRenderCall(() -> {
			if (enable) {
				mode.destroy();
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


}
