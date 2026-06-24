package com.leclowndu93150.joyofpainting;

import com.leclowndu93150.joyofpainting.command.CommandExport;
import com.leclowndu93150.joyofpainting.command.CommandImport;
import com.leclowndu93150.joyofpainting.registry.ModCreativeTabs;
import com.leclowndu93150.joyofpainting.registry.ModDataComponents;
import com.leclowndu93150.joyofpainting.registry.ModEntities;
import com.leclowndu93150.joyofpainting.registry.ModItems;
import com.leclowndu93150.joyofpainting.registry.ModRecipeSerializers;
import com.leclowndu93150.joyofpainting.registry.ModSounds;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(JoyOfPainting.MODID)
public class JoyOfPainting {
    public static final String MODID = "joyofpainting";

    public JoyOfPainting(IEventBus modBus) {
        ModDataComponents.register(modBus);
        ModItems.register(modBus);
        ModSounds.register(modBus);
        ModEntities.register(modBus);
        ModRecipeSerializers.register(modBus);
        ModCreativeTabs.register(modBus);
        NeoForge.EVENT_BUS.register(this);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandExport.register(event.getDispatcher());
        CommandImport.register(event.getDispatcher());
    }
}
