package com.leclowndu93150.joyofpainting.registry;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.entity.EntityCanvas;
import com.leclowndu93150.joyofpainting.entity.EntityEasel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JoyOfPainting.MODID);

    public static final RegistryObject<EntityType<EntityCanvas>> CANVAS = ENTITY_TYPES.register("canvas",
            () -> EntityType.Builder.<EntityCanvas>of(EntityCanvas::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .setCustomClientFactory((spawn, level) -> new EntityCanvas(ModEntities.CANVAS.get(), level))
                    .build("canvas"));

    public static final RegistryObject<EntityType<EntityEasel>> EASEL = ENTITY_TYPES.register("easel",
            () -> EntityType.Builder.<EntityEasel>of(EntityEasel::new, MobCategory.MISC)
                    .sized(0.8f, 1.975f)
                    .setCustomClientFactory((spawn, level) -> new EntityEasel(ModEntities.EASEL.get(), level))
                    .build("easel"));

    private ModEntities() {}

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
