package com.lowdragmc.shimmer.client.auxiliaryScreen;

import com.lowdragmc.shimmer.Configuration;
import com.lowdragmc.shimmer.client.light.LightManager;
import com.lowdragmc.shimmer.client.model.ShimmerMetadataSection;
import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.config.BlockLight;
import com.lowdragmc.shimmer.config.Bloom;
import com.lowdragmc.shimmer.config.ItemLight;
import com.lowdragmc.shimmer.config.ShimmerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuxiliaryScreen extends Screen {

	private HsbColorWidget colorPicker;
	private SuggestionEditBoxWidget inputText;
	private CycleButton<KeyType> mode;
	private RadiusSlider radiusSlide;
	private PreviewWidget previewWidget;
	private Button applyButton;
	private Button exportButton;
	private Button addButton;
	private Button clearButton;
	private Button importColorButton;

	private static final CycleButton.Builder<KeyType> cycleButtonBuilder =
			CycleButton.<KeyType>builder(key -> Component.literal(key.toString().toLowerCase()))
					.withInitialValue(KeyType.DEFAULT)
					.withValues(KeyType.VALUES);


	public AuxiliaryScreen() {
		super( Component.literal("AuxiliaryScreen"));
	}

	@Override
	protected void init() {
		Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);

		colorPicker = new HsbColorWidget(20, 20, 100, 80, 20, 10, Component.literal("1"));
		inputText = new SuggestionEditBoxWidget(this.minecraft.font, 220, 20, 200, 20, Component.literal("s"));

		radiusSlide = new RadiusSlider(20, 110, 150, 20, Component.literal("Radius"), 10);

		mode = cycleButtonBuilder.create(20, 130, 150, 20, Component.literal("select mode:"),
				(button, value) -> {
					inputText.onModeChange(value);
					previewWidget.onModeChange(value);
				});
		previewWidget = new PreviewWidget(20, 160, 90, 90, Component.literal("preview"));

		inputText.addCompleteListener(previewWidget::onContentNeedChange);
		inputText.addCandidateListener(previewWidget::onCandicateChange);

		addRenderableWidget(colorPicker);
		addRenderableWidget(inputText);
		addRenderableWidget(mode);
		addRenderableWidget(previewWidget);
		addRenderableWidget(radiusSlide);

		applyButton = new Button(220, 50, 45, 20, Component.literal("apply"),
				button -> {
					Configuration.load();
					LightManager.INSTANCE.loadConfig();
					PostProcessing.loadConfig();
					ShimmerMetadataSection.onResourceManagerReload();
					LightManager.onResourceManagerReload();
					for (PostProcessing postProcessing : PostProcessing.values()) {
						postProcessing.onResourceManagerReload(minecraft.getResourceManager());
					}
					minecraft.tell(minecraft.levelRenderer::allChanged);
				});
		addButton = new Button(270, 50, 45, 20, Component.literal("add"),
				button -> {

					if (!inputText.isComplete) return;

					ShimmerConfig config;
					if (Configuration.auxiliaryConfig != null) {
						config = Configuration.auxiliaryConfig;
					} else {
						config = new ShimmerConfig();
						config.configSource = "AuxiliaryScreen";
						config.init();
						config.enable = new AtomicBoolean(true);
						Configuration.auxiliaryConfig = config;
					}

					int rgb = colorPicker.rgb();
					float radius = (float) radiusSlide.actualValue;
					String content = inputText.getValue();

					switch (mode.getValue()) {
						case COLORED_BLOCK -> {
							var light = new BlockLight();
							light.setRGB(rgb);
							light.radius = radius;
							light.blockName = content;
							config.blockLights.add(light);
						}
						case LIGHT_ITEM -> {
							var light = new ItemLight();
							light.setRGB(rgb);
							light.radius = radius;
							light.itemName = content;
							config.itemLights.add(light);
						}
						case BLOOM_PARTICLE -> {
							var bloom = new Bloom();
							bloom.particleName = content;
							config.blooms.add(bloom);
						}
						case BLOOM_FLUID -> {
							var bloom = new Bloom();
							bloom.fluidName = content;
							config.blooms.add(bloom);
						}
						case BLOOM_BLOCK -> {
							var bloom = new Bloom();
							bloom.blockName = content;
							config.blooms.add(bloom);
						}
					}
				});
		exportButton = new Button(320, 50, 45, 20, Component.literal("export"),
				button -> {
					if (Configuration.auxiliaryConfig != null) {
						var json = Configuration.gson.toJson(Configuration.auxiliaryConfig);
						TextFieldHelper.setClipboardContents(minecraft, json);
					} else {
						TextFieldHelper.setClipboardContents(minecraft, " ");
					}
				});
		clearButton = new Button(370, 50, 45, 20, Component.literal("clear"),
				button -> Configuration.auxiliaryConfig = null
		);

		addRenderableWidget(applyButton);
		addRenderableWidget(addButton);
		addRenderableWidget(exportButton);
		addRenderableWidget(clearButton);

//		importColorButton = new Button(, , );

//		addRenderableWidget(importColorButton);
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		this.minecraft = minecraft;
		this.itemRenderer = minecraft.getItemRenderer();
		this.font = minecraft.font;
		this.width = width;
		this.height = height;
	}

	@Override
	public void removed() {
		Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(false);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTick);
	}

	/**
	 * overwrite the tab logic
	 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		} else if (this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public void tick() {
		inputText.tick();
		addButton.visible = inputText.isComplete;
		exportButton.visible = inputText.isComplete;
		clearButton.visible = inputText.isComplete;
		applyButton.visible = inputText.isComplete;
	}
}
