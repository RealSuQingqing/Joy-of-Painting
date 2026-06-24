package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, JoyOfPainting.MODID);

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, JoyOfPainting.id(name));
    }

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCanvas>> CANVAS = ENTITY_TYPES.register("canvas",
            () -> EntityType.Builder.<EntityCanvas>of(EntityCanvas::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(key("canvas")));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEasel>> EASEL = ENTITY_TYPES.register("easel",
            () -> EntityType.Builder.of(EntityEasel::new, MobCategory.MISC)
                    .sized(0.8f, 1.975f)
                    .build(key("easel")));

    private ModEntities() {}

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
