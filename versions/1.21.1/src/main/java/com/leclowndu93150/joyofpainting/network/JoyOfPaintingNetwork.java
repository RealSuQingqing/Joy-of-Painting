package com.leclowndu93150.joyofpainting.network;

import com.leclowndu93150.joyofpainting.network.packets.CanvasMiniUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.CanvasUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.EaselLeftPacket;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingSendPacket;
import com.leclowndu93150.joyofpainting.network.packets.PaletteUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.PictureRequestPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "joyofpainting", bus = EventBusSubscriber.Bus.MOD)
public final class JoyOfPaintingNetwork {
    private JoyOfPaintingNetwork() {}

    @SubscribeEvent
    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");

        r.playToServer(CanvasUpdatePacket.TYPE, CanvasUpdatePacket.STREAM_CODEC, CanvasUpdatePacket::handle);
        r.playToServer(CanvasMiniUpdatePacket.TYPE, CanvasMiniUpdatePacket.STREAM_CODEC, CanvasMiniUpdatePacket::handle);
        r.playToServer(EaselLeftPacket.TYPE, EaselLeftPacket.STREAM_CODEC, EaselLeftPacket::handle);
        r.playToServer(ImportPaintingSendPacket.TYPE, ImportPaintingSendPacket.STREAM_CODEC, ImportPaintingSendPacket::handle);
        r.playToServer(PaletteUpdatePacket.TYPE, PaletteUpdatePacket.STREAM_CODEC, PaletteUpdatePacket::handle);
        r.playToServer(PictureRequestPacket.TYPE, PictureRequestPacket.STREAM_CODEC, PictureRequestPacket::handle);
    }
}
