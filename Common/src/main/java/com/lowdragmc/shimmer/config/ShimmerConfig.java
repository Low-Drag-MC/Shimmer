package com.lowdragmc.shimmer.config;

import com.google.gson.annotations.SerializedName;
import com.lowdragmc.shimmer.ShimmerConstants;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;

import javax.annotation.Nullable;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShimmerConfig {

	@SerializedName("LightBlock")
	public List<BlockLight> blockLights;
	@SerializedName("LightItem")
	public List<ItemLight> itemLights;
	@SerializedName("Bloom")
	public List<Bloom> blooms;

	public transient String configSource;

	public void init(){
		blockLights = new ArrayList<>();
		itemLights = new ArrayList<>();
		blooms = new ArrayList<>();
	}

	public boolean check(String message) {
		this.configSource = message;
		if (blockLights.isEmpty() && itemLights.isEmpty() && blooms.isEmpty()) {
			ShimmerConstants.LOGGER.error("find empty config " + message);
			return false;
		} else {
			List<String> errors = new ArrayList<>();
			blockLights.forEach(i -> checkItem(i, errors));
			itemLights.forEach(i -> checkItem(i, errors));
			blooms.forEach(i -> checkItem(i, errors));
			if (errors.isEmpty()) {
				return true;
			} else {
				ShimmerConstants.LOGGER.error("detect error from " + message);
				errors.forEach(ShimmerConstants.LOGGER::error);
				return false;
			}
		}
	}

	private static <T extends Check> void checkItem(T item, List<String> errors) {
		try {
			item.check();
		} catch (Exception e) {
			errors.add(e.getMessage());
		}
	}

	private interface Check {
		void check();
		String getConfigSource();
	}


	private interface BlockChecker extends Check{
		String getBlockName();
		default Pair<ResourceLocation, Block> block(){
			var blockName = getBlockName();
			Objects.requireNonNull(blockName);
			if (!ResourceLocation.isValidResourceLocation(blockName)){
				ShimmerConstants.LOGGER.error("invalid block name " + blockName + " form" + getConfigSource());
				return null;
			}
			var blockLocation = new ResourceLocation(blockName);
			if (!Registry.BLOCK.containsKey(blockLocation)){
				ShimmerConstants.LOGGER.error("can't find block " + blockLocation + " from" + getConfigSource());
				return Pair.of(blockLocation,null);
			}
			return Pair.of(blockLocation,Registry.BLOCK.get(blockLocation));
		}
	}

	private interface FluidChecker extends Check{

		String getFluidName();

		default Pair<ResourceLocation, Fluid> fluid(){
			var fluidName = getFluidName();
			Objects.requireNonNull(fluidName);
			if (!ResourceLocation.isValidResourceLocation(fluidName)){
				ShimmerConstants.LOGGER.error("invalid fluid name " + fluidName + " form" + getConfigSource());
				return null;
			}
			var fluidLocation  = new ResourceLocation(fluidName);
			if (!Registry.FLUID.containsKey(fluidLocation)){
				ShimmerConstants.LOGGER.error("can't find fluid " + fluidLocation + " from" + getConfigSource());
				return Pair.of(fluidLocation,null);
			}
			Fluid fluid = Registry.FLUID.get(fluidLocation);
			return Pair.of(fluidLocation,fluid);
		}
	}

	class Light implements Check {
		private int r;
		private int g;
		private int b;
		@SerializedName(value = "a", alternate = {"alpha"})
		public int alpha = 255;
		public float radius;

		public int color() {
			return FastColor.ARGB32.color(alpha, r, g, b);
		}

		@Override
		public void check() {
			Asserts.check(r <= 255 && r >= 0, "red must range in [0,255] for Light");
			Asserts.check(g <= 255 && g >= 0, "greed must range in [0,255] for Light");
			Asserts.check(b <= 255 && b >= 0, "blue must range in [0,255] for Light");
			Asserts.check(alpha <= 255 && alpha >= 0, "alpha must range in [0,255] for Light");
			Asserts.check(radius >= 0 && radius <= 15, "radius must range in [1,15] for Light");
		}

		public void setRGB(int rgb){
			this.r = FastColor.ARGB32.red(rgb);
			this.g = FastColor.ARGB32.green(rgb);
			this.b = FastColor.ARGB32.blue(rgb);
		}

		@Override
		public String getConfigSource() {
			return configSource;
		}
	}

	public class BlockLight extends Light implements BlockChecker,FluidChecker{
		@SerializedName("block")
		public String blockName;
		@SerializedName("fluid")
		public String fluidName;
		@SerializedName("state")
		@Nullable
		public Map<String, String> state;

		@Override
		public void check() {
			super.check();
			Asserts.check(StringUtils.isNotEmpty(blockName) || StringUtils.isNotEmpty(fluidName),
					"field block or fluid must be specified for BlockLight");
			Asserts.check(!(StringUtils.isNotEmpty(blockName) && StringUtils.isNotEmpty(fluidName)),
					"can't set block and fluid at same time");
		}

		@Override
		public String getConfigSource() {
			return configSource;
		}

		public boolean hasState() {
			return state != null && !state.isEmpty();
		}

		@Override
		public String getBlockName(){
			return blockName;
		}

		@Override
		public String getFluidName() {
			return fluidName;
		}
	}

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

		@Override
		public String getConfigSource() {
			return configSource;
		}
	}

	public class Bloom implements Check , BlockChecker, FluidChecker{
		@SerializedName("fluid")
		public String fluidName;
		@SerializedName("particle")
		public String particleName;
		@SerializedName("block")
		public String blockName;
		@SerializedName("state")
		public Map<String, String> state;


		@Override
		public void check() {
			int i = 0;
			if (StringUtils.isNotEmpty(fluidName)) i++;
			if (StringUtils.isNotEmpty(particleName)) i++;
			if (StringUtils.isNotEmpty(blockName)) i++;
			Asserts.check(i == 1, "only of Field fluid, particle or block can be specified for bloom");
		}

		@Override
		public String getConfigSource() {
			return configSource;
		}

		@Override
		public String getBlockName() {
			return blockName;
		}

		@Override
		public String getFluidName() {
			return fluidName;
		}

		public boolean hasState() {
			return state != null && !state.isEmpty();
		}

	}

}
