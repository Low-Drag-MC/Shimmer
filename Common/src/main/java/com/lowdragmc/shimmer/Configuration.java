package com.lowdragmc.shimmer;

import com.google.gson.*;
import com.lowdragmc.shimmer.config.ColorReferences;
import com.lowdragmc.shimmer.config.ColorReferencesTypeAdapter;
import com.lowdragmc.shimmer.config.ShimmerConfig;
import com.lowdragmc.shimmer.event.ShimmerLoadConfigEvent;
import com.lowdragmc.shimmer.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2022/05/05
 * @implNote Configs
 */
public class Configuration {

	private static final String configurationFileName = "shimmer.json";
	/**
	 * config location from mod jar and resource packs
	 */
	private static final ResourceLocation configLocation = new ResourceLocation(ShimmerConstants.MOD_ID, configurationFileName);

	/**
	 * the Gson object, with pretty print
	 */
	public static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(ColorReferences.class,new ColorReferencesTypeAdapter())
			.setPrettyPrinting()
			.create();
	/**
	 * the collection stores all the configs from file and mod
	 */
	public static final List<ShimmerConfig> configs = new ArrayList<>();
	/**
	 * the config object from auxiliary screen
	 */
	@Nullable public static ShimmerConfig auxiliaryConfig;

	/**
	 * load all configs
	 */
	public static void load() {
		configs.clear();
		String causedSource = "unknown";
		try {
			//from mod jar and resource pack
			List<Resource> resources = Minecraft.getInstance().getResourceManager().getResourceStack(configLocation);
			for (var resource : resources) {
				causedSource = " file managed my minecraft located in" + " [sourceName:" + resource.sourcePackId() + "," + "location:" + resource.sourcePackId() + "]";
				try (InputStreamReader reader = new InputStreamReader(resource.open())) {
					ShimmerConfig config = gson.fromJson(reader, ShimmerConfig.class);
					if (config.check(causedSource)) configs.add(config);
				}
			}
			//automatic mod compat discovery
			for (var modId : Services.PLATFORM.getLoadedMods()) {
				if (modId.equals(ShimmerConstants.MOD_ID) && !Services.PLATFORM.enableBuildinSetting()) continue; //special process for shimmer
				causedSource = " automatic configuration added by mod " + modId;
				ResourceLocation candidateConfigurationPath = new ResourceLocation(modId, configurationFileName);
				Optional<String> optionalConfiguration = readConfiguration(candidateConfigurationPath);
				if (optionalConfiguration.isPresent()) {
					ShimmerConfig config = gson.fromJson(optionalConfiguration.get(), ShimmerConfig.class);
					if (config.check(causedSource)) {
						configs.add(config);
						ShimmerConstants.LOGGER.info("automatic configuration added by mod:" + modId + " path:" + candidateConfigurationPath);
					}
				}
			}
			//added by mods through event
			for (var entry : Services.PLATFORM.postLoadConfigurationEvent(new ShimmerLoadConfigEvent()).getConfiguration().entrySet()) {
				causedSource = " configuration added by mod " + entry.getKey();
				ShimmerConfig config = gson.fromJson(entry.getValue(), ShimmerConfig.class);
				if (config.check(causedSource)) configs.add(config);
			}

			//from config file
			File shimmerConfigDir = Services.PLATFORM.getConfigDir().resolve("shimmer").toFile();
			if (!shimmerConfigDir.exists() || !shimmerConfigDir.isDirectory()) shimmerConfigDir.mkdir();
			var configFiles = Objects.requireNonNullElse(shimmerConfigDir.listFiles(),new File[0]);
			for (var configFile : configFiles) {
				if (!configFile.getName().endsWith(".json")) return;
				causedSource = " file in config folder:" + configFile.getAbsolutePath();
				if (configFile.isDirectory()) continue;
				try (var stream = new FileReader(configFile)) {
					ShimmerConfig config = gson.fromJson(stream, ShimmerConfig.class);
					if (config.check(causedSource)){
						if (config.enable.get()){
							configs.add(config);
						}else {
							ShimmerConstants.LOGGER.info("skip disabled config file from" + causedSource);
						}
					}
				}
			}

			//from auxiliary screen
			if (auxiliaryConfig != null) {
				configs.add(auxiliaryConfig);
			}

		} catch (IOException ioException) {
			ShimmerConstants.LOGGER.error("failed to get config resources, caused by " + causedSource);
			ShimmerConstants.LOGGER.error(ioException.getMessage());
		} catch (JsonSyntaxException jsonSyntaxException) {
			ShimmerConstants.LOGGER.error("json syntax error in " + causedSource);
			ShimmerConstants.LOGGER.error(jsonSyntaxException.getMessage());
		} catch (SecurityException e){
			ShimmerConstants.LOGGER.error("has no permission to create shimmer config directory");
			ShimmerConstants.LOGGER.error(e.getMessage());
		} catch (Exception e) {
			ShimmerConstants.LOGGER.error("an un-expected exception happen while reloading config files, caused by" + causedSource);
			ShimmerConstants.LOGGER.error(e.getMessage());
		} finally {
			ShimmerConstants.LOGGER.debug("reloading config files end");
		}
	}

	/**
	 * read shimmer configuration from path
	 */
	public static Optional<String> readConfiguration(ResourceLocation configurationPath) {
		return Minecraft.getInstance().getResourceManager().getResource(configurationPath).flatMap(resource -> {
			try (BufferedReader reader = resource.openAsReader()) {
				return Optional.of(reader.lines().collect(Collectors.joining()));
			} catch (IOException ioException) {
				ShimmerConstants.LOGGER.error("find shimmer configuration file:" + configurationPath + " but failed to read", ioException);
			}
			return Optional.empty();
		});
	}
}
