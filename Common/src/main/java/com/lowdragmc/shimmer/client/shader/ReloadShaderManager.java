package com.lowdragmc.shimmer.client.shader;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReloadShaderManager {

    private static Map<ResourceLocation, Resource> reloadResources = new HashMap<>();
    public static boolean isReloading = false;
    private static boolean foreReloadAll = false;
    private static final ResourceProvider reloadShaderResource = reloadResources::get;

    private static void recordResource(ResourceLocation resourceLocation, Resource resource) {
        reloadResources.put(resourceLocation, resource);
    }

    public static void cleanResource() {
        message(new TextComponent("clear all resource for backup usage"));
        reloadResources.clear();
    }

    private static void recordCopyResource(ResourceLocation resourceLocation, Resource resource) {
        try {
            final byte[] data = resource.getInputStream().readAllBytes();

            Resource copyResource = new Resource() {
                boolean hasMcmeta = resource.hasMetadata();
                final ResourceLocation resourceLocation = resource.getLocation();
                final String resourceName = resource.getSourceName();

                @Override
                public void close() {
                }

                @Override
                public ResourceLocation getLocation() {
                    return resourceLocation;
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(data);
                }

                @Override
                public boolean hasMetadata() {
                    return hasMcmeta;
                }

                @Nullable
                @Override
                public <T> T getMetadata(MetadataSectionSerializer<T> var1) {
                    return null;
                }

                @Override
                public String getSourceName() {
                    return "copied_" + resourceName;
                }
            };
            recordResource(resourceLocation, copyResource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadShader() {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();
        message(new TextComponent("start reloading shader"));
        long time = System.currentTimeMillis();
        Map<ResourceLocation, Resource> backupResource = reloadResources;
        reloadResources = new HashMap<>();
        isReloading = true;
        foreReloadAll = false;
        try {
            minecraft.gameRenderer.onResourceManagerReload(resourceManager);
            minecraft.levelRenderer.onResourceManagerReload(resourceManager);
            message(new TextComponent("reload success"));
            message(new TextComponent(MessageFormat.format("cache resource:{0}", reloadResources.size())));
            message(new TextComponent(MessageFormat.format("total time cost:{0}s", (System.currentTimeMillis() - time) / 1000f)));
            backupResource.clear();
        } catch (Exception tryException) {
            foreReloadAll = true;
            reloadResources.clear();
            reloadResources = backupResource;
            message(new TextComponent("exception occur will reloading , trying to backup").withStyle(ChatFormatting.RED));
            message(new TextComponent(MessageFormat.format("error:{0}", tryException.getMessage())).withStyle(ChatFormatting.RED));
            try {
                minecraft.gameRenderer.onResourceManagerReload(resourceManager);
                minecraft.levelRenderer.onResourceManagerReload(resourceManager);
                message(new TextComponent("load backup resource successful"));
            } catch (Exception backupException) {
                message(new TextComponent("exception occur while trying backup").withStyle(ChatFormatting.RED));
                message(new TextComponent(MessageFormat.format("error:{0}", backupException.getMessage())).withStyle(ChatFormatting.RED));
                backupException.addSuppressed(tryException);
                throw backupException;
            }
        } finally {
            isReloading = false;
            foreReloadAll = false;
        }
    }

    private static void message(Component component) {
        Minecraft.getInstance().player.sendMessage(component, Util.NIL_UUID);
    }

    @Nonnull
    public static ShaderInstance backupNewShaderInstance(ResourceProvider resourceProvider, String shaderName, VertexFormat vertexFormat) throws IOException {
        if (foreReloadAll) {
            return new ShaderInstance(reloadShaderResource, shaderName, vertexFormat);
        }
        ShaderInstance shaderInstance = new ShaderInstance(resourceProvider, shaderName, vertexFormat);
        ResourceLocation shaderResourceLocation = new ResourceLocation(shaderName);
        recordProgramResource(resourceProvider, shaderResourceLocation.getNamespace(), shaderResourceLocation.getPath());
        return shaderInstance;
    }

    public static ShaderInstance backupNewShaderInstance(ResourceProvider resourceProvider, ResourceLocation shaderLocation, VertexFormat vertexFormat) throws IOException {
        return backupNewShaderInstance(resourceProvider, shaderLocation.toString(), vertexFormat);
    }

    @Nonnull
    private static void recordProgramResource(ResourceProvider resourceProvider, String nameSpace, String shaderName) throws IOException {
        ResourceLocation programResourceLocation = new ResourceLocation(nameSpace, "shaders/core/" + shaderName + ".json");
        Resource programResource = resourceProvider.getResource(programResourceLocation);
        ReloadShaderManager.recordCopyResource(programResourceLocation, programResource);
        JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(resourceProvider.getResource(programResourceLocation).getInputStream(), StandardCharsets.UTF_8));
        ResourceLocation vertex = new ResourceLocation(GsonHelper.getAsString(jsonObject, "vertex"));
        ResourceLocation vertexResourceLocation = new ResourceLocation(vertex.getNamespace(), "shaders/core/" + vertex.getPath() + ".vsh");
        ReloadShaderManager.recordCopyResource(vertexResourceLocation, resourceProvider.getResource(vertexResourceLocation));
        ResourceLocation fragment = new ResourceLocation(GsonHelper.getAsString(jsonObject, "fragment"));
        ResourceLocation fragmentResourceLocation = new ResourceLocation(fragment.getNamespace(), "shaders/core/" + fragment.getPath() + ".fsh");
        ReloadShaderManager.recordCopyResource(fragmentResourceLocation, resourceProvider.getResource(fragmentResourceLocation));
    }

    public static ResourceManager reloadResourceManager = new ResourceManager() {
        @Override
        public Set<String> getNamespaces() {
            return null;
        }

        @Override
        public boolean hasResource(ResourceLocation resourceLocation) {
            return reloadResources.containsKey(resourceLocation);
        }

        @Override
        public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
            return List.of(getResource(resourceLocation));
        }

        @Override
        public Collection<ResourceLocation> listResources(String var1, Predicate<String> var2) {
            return null;
        }

        @Override
        public Stream<PackResources> listPacks() {
            return null;
        }

        @Override
        public Resource getResource(ResourceLocation resourceLocation) throws IOException {
            return reloadShaderResource.getResource(resourceLocation);
        }
    };

    public static PostChain backupNewPostChain(TextureManager textureManager, ResourceManager resourceManager, RenderTarget renderTarget, ResourceLocation resourceLocation) throws IOException {
        if (foreReloadAll) {
            return new PostChain(textureManager, reloadResourceManager, renderTarget, resourceLocation);
        }
        PostChain postChain = new PostChain(textureManager, resourceManager, renderTarget, resourceLocation);
        recordPostChainResource(resourceManager, resourceLocation);
        return postChain;
    }

    private static void recordPostChainResource(ResourceManager resourceManager, ResourceLocation resourceLocation) throws IOException {
        Resource postChainResource = resourceManager.getResource(resourceLocation);
        recordCopyResource(resourceLocation, postChainResource);
    }

    public static EffectInstance backupNewEffectInstance(ResourceManager resourceProvider, String shaderName) throws IOException {
        if (foreReloadAll) {
            return new EffectInstance(reloadResourceManager, shaderName);
        }
        EffectInstance effectInstance = new EffectInstance(resourceProvider, shaderName);
        recordEffectInstanceResource(resourceProvider, shaderName);
        return effectInstance;
    }

    private static void recordEffectInstanceResource(ResourceManager resourceProvider, String shaderName) throws IOException {
        ResourceLocation resourceLocation = make(ResourceLocation.tryParse(shaderName), (rl) ->
                new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + ".json"));
        Resource effectResource = resourceProvider.getResource(resourceLocation);
        recordCopyResource(resourceLocation, effectResource);
        JsonObject effectJsonObject = GsonHelper.parse(new InputStreamReader(resourceProvider.getResource(resourceLocation).getInputStream(), StandardCharsets.UTF_8));
        String vertex = GsonHelper.getAsString(effectJsonObject, "vertex");
        ResourceLocation vertexResourceLocation = make(ResourceLocation.tryParse(vertex), rl ->
                new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + ".vsh"));
        recordCopyResource(vertexResourceLocation, resourceProvider.getResource(vertexResourceLocation));
        String fragment = GsonHelper.getAsString(effectJsonObject, "fragment");
        ResourceLocation fragmentResourceLocation = make(ResourceLocation.tryParse(fragment), rl ->
                new ResourceLocation(rl.getNamespace(), "shaders/program/" + rl.getPath() + ".fsh"));
        recordCopyResource(fragmentResourceLocation, resourceProvider.getResource(fragmentResourceLocation));
    }

    private static <T, R> R make(T origin, Function<T, R> transformer) {
        return transformer.apply(origin);
    }
}