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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuxiliaryScreen extends Screen {

	private HsbColorWidget colorPicker;
	private SuggestionEditBoxWidget inputText;
	private CycleButton<KeyType> mode;
	private SliderWidget radiusSlide;
	private PreviewWidget previewWidget;
	private Button applyButton;
	private Button exportButton;
	private Button addButton;
	private Button clearButton;
	private Button importColorButton;

	/**
	 * [0-360]
	 */
	private SliderWidget colorHue;
	/**
	 * [0-100]
	 */
	private SliderWidget colorSaturation;
	/**
	 * [0-100]
	 */
	private SliderWidget colorBrightness;
	/**
	 * [0-255]
	 */
	private SliderWidget colorR;
	/**
	 * [0-255]
	 */
	private SliderWidget colorG;
	/**
	 * [0-255]
	 */
	private SliderWidget colorB;

	private static final CycleButton.Builder<KeyType> cycleButtonBuilder =
			CycleButton.<KeyType>builder(key -> new TextComponent(key.toString().toLowerCase()))
					.withInitialValue(KeyType.DEFAULT)
					.withValues(KeyType.VALUES);


	public AuxiliaryScreen() {
		super(new TextComponent("AuxiliaryScreen"));
	}

	@Override
	protected void init() {
		Objects.requireNonNull(this.minecraft).keyboardHandler.setSendRepeatsToGui(true);

		colorPicker = new HsbColorWidget(20, 20, 100, 80, 20, 10, new TextComponent("1"));
		inputText = new SuggestionEditBoxWidget(this.minecraft.font, 220, 20, 200, 20, new TextComponent("s"));

		radiusSlide = new SliderWidget(20, 110, 150, 20, 1, 15, 0.2, new TextComponent("Radius"), "%.2f", 10);

		mode = cycleButtonBuilder.create(20, 130, 150, 20, new TextComponent("select mode"),
				(button, value) -> {
					inputText.onModeChange(value);
					previewWidget.onModeChange(value);
				});
		previewWidget = new PreviewWidget(20, 160, 90, 90, new TextComponent("preview"));

		inputText.addCompleteListener(previewWidget::onContentNeedChange);
		inputText.addCandidateListener(previewWidget::onCandicateChange);

		addRenderableWidget(colorPicker);
		addRenderableWidget(inputText);
		addRenderableWidget(mode);
		addRenderableWidget(previewWidget);
		addRenderableWidget(radiusSlide);

		applyButton = new Button(220, 50, 45, 20, new TextComponent("apply"),
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
		addButton = new Button(270, 50, 45, 20, new TextComponent("add"),
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
		exportButton = new Button(320, 50, 45, 20, new TextComponent("export"),
				button -> {
					if (Configuration.auxiliaryConfig != null) {
						var json = Configuration.gson.toJson(Configuration.auxiliaryConfig);
						TextFieldHelper.setClipboardContents(minecraft, json);
					} else {
						TextFieldHelper.setClipboardContents(minecraft, " ");
					}
				});
		clearButton = new Button(370, 50, 45, 20, new TextComponent("clear"),
				button -> Configuration.auxiliaryConfig = null
		);

		addRenderableWidget(applyButton);
		addRenderableWidget(addButton);
		addRenderableWidget(exportButton);
		addRenderableWidget(clearButton);

		importColorButton = new Button(220, 80, 95, 20, new TextComponent("import color"), button -> {
			if (Eyedropper.isDataAvailable()) {
				colorPicker.setRGB(Eyedropper.getEyedropperColor());
				minecraft.player.sendMessage(new TextComponent("set record color"), Util.NIL_UUID);
			} else if (Eyedropper.getState()) {
				colorPicker.setRGB(Eyedropper.getCurrentColor());
				minecraft.player.sendMessage(new TextComponent("no record, use current"), Util.NIL_UUID);
			} else {
				minecraft.player.sendMessage(new TextComponent("not under eyedropper mode, can't import color"), Util.NIL_UUID);
			}
		});

		addRenderableWidget(importColorButton);

		colorHue = new SliderWidget(220, 110, 200, 20, 0, 360, 1,
				new TextComponent("Hue"), "%d", 204);
		colorSaturation = new SliderWidget(220, 130, 200, 20, 0, 100, 1,
				new TextComponent("Saturation"), "%d%%", 72);
		colorBrightness = new SliderWidget(220, 150, 200, 20, 0, 100, 1,
				new TextComponent("Brightness"), "%d%%", 94);
		colorR = new SliderWidget(220, 170, 200, 20, 0, 255, 1,
				new TextComponent("R"), "%d", 67);
		colorG = new SliderWidget(220, 190, 200, 20, 0, 255, 1,
				new TextComponent("G"), "%d", 170);
		colorB = new SliderWidget(220, 210, 200, 20, 0, 255, 1,
				new TextComponent("B"), "%d", 239);

		addRenderableWidget(colorHue);
		addRenderableWidget(colorSaturation);
		addRenderableWidget(colorBrightness);
		addRenderableWidget(colorR);
		addRenderableWidget(colorG);
		addRenderableWidget(colorB);

		colorHue.addListener(((oldValue, newValue) -> {
			var color = colorPicker.getHSB();
			color[0] = (float) newValue;
			colorPicker.setHSB(color);
		}));
		colorSaturation.addListener(((oldValue, newValue) -> {
			var color = colorPicker.getHSB();
			color[1] = (float) (newValue / 100f);
			colorPicker.setHSB(color);
		}));
		colorBrightness.addListener(((oldValue, newValue) -> {
			var color = colorPicker.getHSB();
			color[2] = (float) (newValue / 100f);
			colorPicker.setHSB(color);
		}));
		colorR.addListener(((oldValue, newValue) -> {
			var color = colorPicker.getRGB();
			color[0] = (float) (newValue / 255f);
			colorPicker.setRGB(color);
		}));
		colorG.addListener(((oldValue, newValue) -> {
			var color = colorPicker.getRGB();
			color[1] = (float) (newValue / 255f);
			colorPicker.setRGB(color);
		}));
		colorB.addListener(((oldValue, newValue) -> {
			var color = colorPicker.getRGB();
			color[2] = (float) (newValue / 255f);
			colorPicker.setRGB(color);
		}));

		colorPicker.registerListener(() -> {
			float[] hsb = colorPicker.getHSB();
			colorHue.trySetValue(hsb[0], false);
			colorSaturation.trySetValue(hsb[1] * 100f, false);
			colorBrightness.trySetValue(hsb[2] * 100f, false);
			float[] rgb = colorPicker.getRGB();
			colorR.trySetValue(rgb[0] * 255f, false);
			colorG.trySetValue(rgb[1] * 255f, false);
			colorB.trySetValue(rgb[2] * 255f,false);
		});
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
			if (this.getFocused() == null) {
				if (colorHue.isHoveredOrFocused()) {
					return colorHue.keyPressed(keyCode, scanCode, modifiers);
				} else if (colorSaturation.isHoveredOrFocused()) {
					return colorSaturation.keyPressed(keyCode, scanCode, modifiers);
				} else if (colorBrightness.isHoveredOrFocused()) {
					return colorBrightness.keyPressed(keyCode, scanCode, modifiers);
				} else if (colorR.isHoveredOrFocused()) {
					return colorR.keyPressed(keyCode, scanCode, modifiers);
				} else if (colorG.isHoveredOrFocused()) {
					return colorG.keyPressed(keyCode, scanCode, modifiers);
				} else if (radiusSlide.isHoveredOrFocused()) {
					return radiusSlide.keyPressed(keyCode, scanCode, modifiers);
				}
			}
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public void tick() {
		inputText.tick();
		var buttonVisible = (inputText.isComplete || StringUtils.isBlank(inputText.getValue())) && !inputText.isFocused();
		addButton.visible = buttonVisible;
		exportButton.visible = buttonVisible;
		clearButton.visible = buttonVisible;
		applyButton.visible = buttonVisible;
		importColorButton.visible = buttonVisible;

		colorHue.visible = buttonVisible;
		colorSaturation.visible = buttonVisible;
		colorBrightness.visible = buttonVisible;
		colorR.visible = buttonVisible;
		colorG.visible = buttonVisible;
		colorB.visible = buttonVisible;
	}
}
