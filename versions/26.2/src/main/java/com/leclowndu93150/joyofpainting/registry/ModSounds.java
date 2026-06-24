package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, JoyOfPainting.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> STROKE_LOOP = register("stroke_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIX = register("mix");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOR_PICKER = register("color_picker");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLOR_PICKER_SUCK = register("color_picker_suck");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER = register("water");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATER_DROP = register("water_drop");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(JoyOfPainting.id(name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
