package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

public class ItemLight extends Light implements Check {
	@SerializedName("item_id")
	public String itemName;
	@SerializedName("item_tag")
	public String itemTag;

	@Override
	public void check() {
		super.check();
		Asserts.check(StringUtils.isNotEmpty(itemName) || StringUtils.isNotEmpty(itemTag),
				"one of Field item_id ot item_tag must be specified for ItemLight");
	}
}