package com.lowdragmc.shimmer.client.auxiliaryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.http.util.Asserts;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * radius slider widget, with min/max/step support<p>
 * {@link super#value} stores a value range in [0,1]<p>
 * {@link AutoCloseable} is calculated from it according to min/max/step
 */
public class SliderWidget extends AbstractSliderButton {

	protected final double minValue;
	protected final double maxValue;
	protected final double stepSize;
	protected double actualValue;
	protected final MutableComponent prefixMessage;
	protected final String formatStr;
	protected final List<SliderListener> listeners = new ArrayList<>();

	public SliderWidget(int x, int y, int width, int height, double minValue, double maxValue, double stepSize, MutableComponent prefixMessage, String formatStr, double initialValue) {
		super(x, y, width, height, prefixMessage, initialValue);
		Asserts.check(stepSize != 0, "step for slider widget can't be zero");
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.stepSize = stepSize;
		this.prefixMessage = prefixMessage;
		this.formatStr = formatStr;
		this.value = initialValue / (maxValue - minValue);
		this.actualValue = initialValue;
		applyValue();
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		if (formatStr.contains("%d")) {
			this.setMessage(prefixMessage.copy().append(Component.literal(":" + String.format(formatStr, (int) actualValue))));
		} else {
			this.setMessage(prefixMessage.copy().append(Component.literal(":" + String.format(formatStr, actualValue))));
		}
	}

	@Override
	protected void applyValue() {
		applyValueInner(true);
	}

	private void applyValueInner(boolean trigListener) {
		var newValue = this.value * (maxValue - minValue) + minValue;
		var candidateValue = ((int) (newValue / stepSize)) * stepSize;
		var candidateValue2 = candidateValue + stepSize;
		var oldActualValue = actualValue;
		if (Math.abs(newValue - candidateValue2) > Math.abs(newValue - candidateValue)) {
			this.actualValue = candidateValue;
		} else {
			this.actualValue = candidateValue2;
		}
		if (trigListener) {
			listeners.forEach(listener -> listener.accept(oldActualValue, this.actualValue));
		}
		updateMessage();
	}

	public void trySetValue(double value, boolean trigListener) {
		var percent = (value - minValue) / (maxValue - minValue);
		var newValue = percent >= 0 ? (percent <= 1 ? percent : 1) : 0;
		if (newValue != this.value) {
			this.value = newValue;
			applyValueInner(trigListener);
		}
	}

	public double getActualValue() {
		return actualValue;
	}

	public void addListener(SliderListener listener) {
		listeners.add(listener);
	}

	@FunctionalInterface
	public interface SliderListener {
		void accept(double oldValue, double newValue);
	}

	private void trySetActualValue(double newActualValue) {
		if (newActualValue > maxValue || newActualValue < minValue) return;
		this.value = (newActualValue - minValue) / (maxValue - minValue);
		applyValueInner(true);
	}

	private double calculateShift(int modifiers) {
		var isShiftDown = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
		var isAltDown = (modifiers & GLFW.GLFW_MOD_ALT) != 0;
		var isCtrlDown = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
		double shift = 0;
		if (isShiftDown) shift += 5;
		if (isAltDown) shift += 10;
		if (isCtrlDown) shift += 20;
		if (shift == 0) shift = 1;
		shift *= stepSize;
		return shift;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			trySetActualValue(actualValue - calculateShift(modifiers));
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
			trySetActualValue(actualValue + calculateShift(modifiers));
			return true;
		}else return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		var moveFactor = 10 * (isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) ? 0.5 : 1);
		trySetActualValue(actualValue + delta * stepSize * moveFactor);
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	private static boolean isKeyPressed(int key) {
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
	}

}
