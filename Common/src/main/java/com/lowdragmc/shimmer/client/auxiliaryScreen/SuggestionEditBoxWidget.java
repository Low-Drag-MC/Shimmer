package com.lowdragmc.shimmer.client.auxiliaryScreen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * the text input widget with suggestion and completion support
 */
public class SuggestionEditBoxWidget extends EditBox {

	public SuggestionEditBoxWidget(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
		tryInitCache();
		allSuggestion = cache.get(KeyType.DEFAULT);
		setMaxLength(50);
	}

	/**
	 * the number of suggestions to show
	 */
	private final int showNumbers = 10;
	/**
	 * collection for all candidates form {@link #allSuggestion}
	 */
	public List<String> suggestions;
	/**
	 * cache for current mode
	 */
	private Set<ResourceLocation> allSuggestion;
	/**
	 * all cache for all modes
	 */
	private static Map<KeyType, Set<ResourceLocation>> cache;
	String lastContent;
	String candidate;
	String lastCandidate;
	boolean isComplete = false;
	List<BiConsumer<KeyType, ResourceLocation>> completeListeners = new ArrayList<>();
	List<BiConsumer<KeyType, ResourceLocation>> candidateListeners = new ArrayList<>();
	KeyType type = KeyType.DEFAULT;

	/**
	 * @param consumer callbacks called when complete rigged
	 */
	public void addCompleteListener(BiConsumer<KeyType, ResourceLocation> consumer) {
		completeListeners.add(consumer);
	}

	/**
	 * @param consumer callbacks called when candidate change
	 */
	public void addCandidateListener(BiConsumer<KeyType, ResourceLocation> consumer) {
		candidateListeners.add(consumer);
	}

	/**
	 * update is complete and call callbacks
	 */
	@Override
	public void tick() {
		super.tick();
		var last = isComplete;
		//check complete change
		if (ResourceLocation.isValidResourceLocation(this.getValue())) {
			ResourceLocation resourceLocation = new ResourceLocation(this.getValue());
			isComplete = allSuggestion.contains(resourceLocation);
			if (!last && isComplete) {
				completeListeners.forEach(item -> item.accept(type, resourceLocation));
			}
		} else {
			isComplete = false;
		}
		//check candidate change
		if (!Objects.equals(candidate, lastCandidate)) {
			lastCandidate = candidate;
			if (lastCandidate != null && ResourceLocation.isValidResourceLocation(lastCandidate)) {
				ResourceLocation resourceLocation = new ResourceLocation(lastCandidate);
				if (allSuggestion.contains(resourceLocation)) {
					candidateListeners.forEach(item -> item.accept(type, resourceLocation));
				}
			}
		}

	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		//render current by super
		super.render(poseStack, mouseX, mouseY, partialTick);

		//check if suggestion need render
		if (suggestions != null && suggestions.contains(this.getValue())) return;

		if (isFocused() && !isComplete) {
			//update suggestion with check
			var currentContent = this.getValue();
			if (!Objects.equals(currentContent, lastContent)) {
				lastContent = currentContent;
				if (!currentContent.contains(":")) {
					suggestions = allSuggestion.stream().parallel().map(ResourceLocation::getNamespace).collect(Collectors.toSet()).stream().toList();
				} else {
					lastContent = currentContent;
					suggestions = allSuggestion.stream().parallel().map(ResourceLocation::toString).filter(res -> res.startsWith(currentContent)).toList();
				}
			}

			//no suggestion, skip
			if (suggestions.isEmpty()) return;

			//calculate render suggestion range
			var begin = suggestions.contains(lastCandidate) ? Math.max(0, suggestions.indexOf(lastCandidate) - 3) : 0;
			var max = Math.min(begin + showNumbers, suggestions.size());

			//render suggestion background
			int gap = Minecraft.getInstance().font.lineHeight + 8;
			fill(poseStack, height, gap * (max - begin + 1), 0x7F000000);

			//select candidate
			if (candidate == null || (!suggestions.contains(lastContent)) && !suggestions.contains(candidate))
				candidate = suggestions.get(0);

			var font = Minecraft.getInstance().font;
			int currentHeight = 0;

			//render suggestions
			for (int i = begin; i < max; i++) {
				String str = suggestions.get(i);
				//candidate color differ
				int color = Objects.equals(str, candidate) ? 0xffcecf67 : 0xffffffff;
				drawString(poseStack, font, font.plainSubstrByWidth(str,200), x + 5 , y + height + currentHeight, color);
				currentHeight += gap;
			}

		}
	}

	/**
	 * implementation Up/Down select and tab for completion
	 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
		switch (keyCode) {
			case GLFW.GLFW_KEY_UP -> candidate = suggestions.get(Math.max(0, suggestions.indexOf(candidate) - 1));
			case GLFW.GLFW_KEY_DOWN ->
					candidate = suggestions.get(Math.min(suggestions.indexOf(candidate) + 1, suggestions.size() - 1));
			case GLFW.GLFW_KEY_TAB -> this.setValue((getValue().isEmpty() || !getValue().contains(":") ? candidate + ":" : candidate));
			default -> {
				return false;
			}
		}
		return true;
	}

	/**
	 * helper method for drawing suggestion background
	 */
	private void fill(PoseStack poseStack, int minY, int height, int color) {
		GuiComponent.fill(poseStack, x, minY, x + width, minY + height, color);
	}

	/**
	 * callback when mode changed, called from other widget
	 */
	public void onModeChange(KeyType type) {
		this.setValue("");
		allSuggestion = cache.get(type);
		this.type = type;
	}

	private void tryInitCache() {
		if (cache == null) {
			var blocks = Registry.BLOCK.keySet();
			cache = Map.of(KeyType.LIGHT_ITEM, Set.copyOf(Registry.ITEM.keySet()), KeyType.BLOOM_PARTICLE, Set.copyOf(Registry.PARTICLE_TYPE.keySet()), KeyType.BLOOM_FLUID, Set.copyOf(Registry.FLUID.keySet()), KeyType.COLORED_BLOCK, Set.copyOf(blocks), KeyType.BLOOM_BLOCK, Set.copyOf(blocks));
		}
	}

}
