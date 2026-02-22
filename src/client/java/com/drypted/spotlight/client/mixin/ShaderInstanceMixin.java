package com.drypted.spotlight.client.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ShaderInstance.class)
public class ShaderInstanceMixin
{
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)V",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private ResourceLocation modifyResourceLocation(ResourceLocation original, ResourceProvider resourceProvider, String string, VertexFormat vertexFormat)
    {
        if (string.equals("mosaic_background"))
            return ResourceLocation.fromNamespaceAndPath("spotlight", original.getPath());
        else return original;
    }
}