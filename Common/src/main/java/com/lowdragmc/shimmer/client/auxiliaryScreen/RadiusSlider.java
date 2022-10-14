package com.lowdragmc.shimmer.client.auxiliaryScreen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * radius slider widget, with min/max/step support<p>
 * {@link super#value} stores a value range in [0,1]<p>
 * {@link AutoCloseable} is calculated from it according to min/max/step
 */
public class RadiusSlider extends AbstractSliderButton {

	protected double minValue = 1;
	protected double maxValue = 15;
	protected double stepSize = 0.2;
	protected double actualValue;

	public RadiusSlider(int x, int y, int width, int height, MutableComponent message, double initialValue) {
		super(x, y, width, height, message, initialValue);
		this.value = initialValue / (maxValue - minValue);
		this.actualValue = initialValue;
		applyValue();
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(Component.literal("Radius:" + String.format("%.2f",actualValue)));
	}

	@Override
	protected void applyValue() {
		actualValue = this.value * (maxValue - minValue) + minValue;
		var candicateValue = ((int) (actualValue / stepSize)) * stepSize;
		var candicateValue2 = candicateValue + stepSize;
		if (Math.abs(actualValue - candicateValue2) > Math.abs(actualValue - candicateValue)) {
			actualValue = candicateValue;
		} else {
			actualValue = candicateValue2;
		}
	}

	public double getActualValue() {
		return actualValue;
	}
}
