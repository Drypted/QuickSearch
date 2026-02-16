package com.drypted.spotlight.client.mixin;

import com.drypted.spotlight.client.core.handlers.SearchHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin
{
    @Inject(method = "buildAllTabContents", at = @At("TAIL"))
    private static void rebuildCreativeModeItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CallbackInfo ci)
    {
        SearchHandler.rebuildGameItems();
    }
}
