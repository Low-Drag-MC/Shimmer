package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import com.lowdragmc.shimmer.ShimmerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShimmerConfig {

	@SerializedName("Enable")
	public AtomicBoolean enable;
	@SerializedName("ConfigSource")
	public transient String configSource;

	@SerializedName("LightBlock")
	public List<BlockLight> blockLights;
	@SerializedName("LightItem")
	public List<ItemLight> itemLights;
	@SerializedName("Bloom")
	public List<Bloom> blooms;
	@SerializedName("ColorReference")
	public ColorReferences colorReferences;
	@SerializedName("BuildIn")
	public AtomicBoolean buildIn;

	/**
	 * init containers if necessary
	 */
	public void init() {
		if (blockLights == null) blockLights = new ArrayList<>();
		if (itemLights == null) itemLights = new ArrayList<>();
		if (blooms == null) blooms = new ArrayList<>();
		if (colorReferences == null) colorReferences = new ColorReferences();

		colorReferences.init();

		if (enable == null) enable = new AtomicBoolean(true);

		blockLights.forEach(i -> i.shimmerConfig = this);
		itemLights.forEach(i -> i.shimmerConfig = this);
		blooms.forEach(i -> i.shimmerConfig = this);
		colorReferences.shimmerConfig = this;

		if (configSource == null) configSource = "Unknown";
	}

	public boolean check(String message) {

		List<String> errors = new ArrayList<>();

		this.configSource = message;
		init();
		if (blockLights.isEmpty() && itemLights.isEmpty() && blooms.isEmpty()) {
			ShimmerConstants.LOGGER.error("find empty config " + message);
			return false;
		} else {

			blockLights.forEach(i -> checkItem(i, errors));
			itemLights.forEach(i -> checkItem(i, errors));
			blooms.forEach(i -> checkItem(i, errors));

			blockLights.forEach(colorReferences::checkLight);
			itemLights.forEach(colorReferences::checkLight);

		}

		apply();


		if (errors.isEmpty()) {
			return true;
		} else {
			ShimmerConstants.LOGGER.error("detect error from " + message);
			errors.forEach(ShimmerConstants.LOGGER::error);
			return false;
		}
	}

	private void apply(){
		blockLights.forEach(colorReferences::analyzeReferenceColor);
		itemLights.forEach(colorReferences::analyzeReferenceColor);
	}

	private static <T extends Check> void checkItem(T item, List<String> errors) {
		try {
			item.check();
		} catch (Exception e) {
			errors.add(e.getMessage());
		}
	}

}
