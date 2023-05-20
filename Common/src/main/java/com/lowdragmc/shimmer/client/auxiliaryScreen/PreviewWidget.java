package com.lowdragmc.shimmer.client.auxiliaryScreen;

import com.lowdragmc.shimmer.platform.Services;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * the preview widget for block/fluid/particle
 */
@ParametersAreNonnullByDefault
public class PreviewWidget extends AbstractWidget {

	private KeyType type;
	private ResourceLocation resourceLocation;
	/**
	 * scale for show
	 */
	private final float scale = 5f;

	public PreviewWidget(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		if (type != null && resourceLocation != null) {
			switch (type) {
				case COLORED_BLOCK, BLOOM_BLOCK -> {
					//must call renderAndDecorateItem with x and y 0
					//translate position ourselves and scale, or vertex position will fly
					//same for LIGHT_ITEM
					ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
					Block block = BuiltInRegistries.BLOCK.get(resourceLocation);

					PoseStack modelViewStack = RenderSystem.getModelViewStack();
					modelViewStack.pushPose();
					modelViewStack.translate(getX(), getY(),0);
					modelViewStack.scale(scale,scale,1f);

					itemRenderer.renderAndDecorateItem(modelViewStack, new ItemStack(block), 0, 0);

					modelViewStack.popPose();
					RenderSystem.applyModelViewMatrix();
				}
				case LIGHT_ITEM -> {
					ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
					Item item = BuiltInRegistries.ITEM.get(resourceLocation);

					PoseStack modelViewStack = RenderSystem.getModelViewStack();

					modelViewStack.pushPose();
					modelViewStack.translate(getX(), getY(),0);
					modelViewStack.scale(scale,scale,1f);

					itemRenderer.renderAndDecorateItem(modelViewStack, new ItemStack(item), 0, 0);

					modelViewStack.popPose();
					RenderSystem.applyModelViewMatrix();
				}
				case BLOOM_PARTICLE -> {
					TextureAtlas textureAtlas = Minecraft.getInstance().particleEngine.textureAtlas;
					TextureAtlasSprite sprite = textureAtlas.getSprite(new ResourceLocation(resourceLocation.getNamespace(), "particle/" + resourceLocation.getPath()));
					if (Objects.equals(sprite.atlasLocation(), MissingTextureAtlasSprite.getLocation())) return;

					Matrix4f pose = poseStack.last().pose();
					RenderSystem._setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					BufferBuilder builder = Tesselator.getInstance().getBuilder();
					builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

					builder.vertex(pose, getX(), getY(), 0.0f).uv(sprite.getU0(), sprite.getV1()).endVertex();
					builder.vertex(pose, getX(), getY() + height, 0.0f).uv(sprite.getU0(), sprite.getV0()).endVertex();
					builder.vertex(pose, getX() + width, getY() + height, 0.0f).uv(sprite.getU1(), sprite.getV0()).endVertex();
					builder.vertex(pose, getX() + width, getY(), 0.0f).uv(sprite.getU1(), sprite.getV1()).endVertex();

					BufferUploader.draw(builder.end());
				}
				case BLOOM_FLUID -> {
					Matrix4f pose = poseStack.last().pose();
					RenderSystem._setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
					RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
					BufferBuilder builder = Tesselator.getInstance().getBuilder();
					builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

					var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
					Fluid fluid = BuiltInRegistries.FLUID.get(resourceLocation);

					TextureAtlasSprite sprite = atlas.apply(Services.PLATFORM.getFluidTextureLocation(fluid, true));
					int fluidColor = Services.PLATFORM.getFluidColor(fluid);

					builder.vertex(pose, getX(), getY(), 0.0f).uv(sprite.getU0(), sprite.getV1()).color(fluidColor).endVertex();
					builder.vertex(pose, getX(), getY() + height, 0.0f).uv(sprite.getU0(), sprite.getV0()).color(fluidColor).endVertex();
					builder.vertex(pose, getX() + width, getY() + height, 0.0f).uv(sprite.getU1(), sprite.getV0()).color(fluidColor).endVertex();
					builder.vertex(pose, getX() + width, getY(), 0.0f).uv(sprite.getU1(), sprite.getV1()).color(fluidColor).endVertex();

					BufferUploader.draw(builder.end());
				}
			}
		}
	}

	public void onModeChange(KeyType type) {
		this.type = type;
		this.resourceLocation = null;
	}

	public void onContentNeedChange(KeyType type, ResourceLocation content) {
		this.type = type;
		this.resourceLocation = content;
	}

	public void onCandicateChange(KeyType type,ResourceLocation candicate) {
		this.type = type;
		this.resourceLocation = candicate;
	}

}
