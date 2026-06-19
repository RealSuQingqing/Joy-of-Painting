package com.leclowndu93150.joyofpainting;

import com.leclowndu93150.joyofpainting.command.CommandExport;
import com.leclowndu93150.joyofpainting.command.CommandImport;
import com.leclowndu93150.joyofpainting.network.JoyOfPaintingNetwork;
import com.leclowndu93150.joyofpainting.registry.ModCreativeTabs;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import com.leclowndu93150.joyofpainting.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(JoyOfPainting.MODID)
public class JoyOfPainting {
    public static final String MODID = "joyofpainting";

    public JoyOfPainting() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        ModSounds.register(modBus);
        ModEntities.register(modBus);
        ModRecipeSerializers.register(modBus);
        ModCreativeTabs.register(modBus);
        JoyOfPaintingNetwork.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandExport.register(event.getDispatcher());
        CommandImport.register(event.getDispatcher());
    }
}
