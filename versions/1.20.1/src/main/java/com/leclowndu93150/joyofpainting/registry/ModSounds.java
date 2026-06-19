package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JoyOfPainting.MODID);

    public static final RegistryObject<SoundEvent> STROKE_LOOP = register("stroke_loop");
    public static final RegistryObject<SoundEvent> MIX = register("mix");
    public static final RegistryObject<SoundEvent> COLOR_PICKER = register("color_picker");
    public static final RegistryObject<SoundEvent> COLOR_PICKER_SUCK = register("color_picker_suck");
    public static final RegistryObject<SoundEvent> WATER = register("water");
    public static final RegistryObject<SoundEvent> WATER_DROP = register("water_drop");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(JoyOfPainting.id(name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
