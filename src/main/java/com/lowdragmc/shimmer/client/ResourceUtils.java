package com.lowdragmc.shimmer.client;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/5/5
 * @implNote ResourceUtils
 */
public class ResourceUtils {
    private static final Map<ResourceLocation, Boolean> cachedTexture = Maps.newHashMap();
    private static final Map<ResourceLocation, Boolean> cachedResources = Maps.newHashMap();

    public static boolean isTextureExist(ResourceLocation rs) {
        if (!cachedTexture.containsKey(rs)) {
            InputStream inputstream = ResourceUtils.class.getResourceAsStream(String.format("/assets/%s/textures/%s.png", rs.getNamespace(), rs.getPath()));
            if (inputstream == null) {
                cachedTexture.put(rs, false);
            } else {
                IOUtils.closeQuietly(inputstream);
                cachedTexture.put(rs, true);
            }
        }

        return cachedTexture.get(rs);
    }

    public static boolean isResourceExist(ResourceLocation rs) {
        if (!cachedResources.containsKey(rs)) {
            InputStream inputstream = ResourceUtils.class.getResourceAsStream(String.format("/assets/%s/%s", rs.getNamespace(), rs.getPath()));
            if (inputstream == null) {
                cachedResources.put(rs, false);
            } else {
                IOUtils.closeQuietly(inputstream);
                cachedResources.put(rs, true);
            }
        }

        return cachedResources.get(rs);
    }
}
