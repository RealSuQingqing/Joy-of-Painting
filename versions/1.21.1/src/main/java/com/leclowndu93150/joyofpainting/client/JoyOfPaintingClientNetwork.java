package com.leclowndu93150.joyofpainting.client;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.network.packets.CloseGuiPacket;
import com.leclowndu93150.joyofpainting.network.packets.ExportPaintingPacket;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingPacket;
import com.leclowndu93150.joyofpainting.network.packets.OpenGuiPacket;
import com.leclowndu93150.joyofpainting.network.packets.PictureSendPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = JoyOfPainting.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class JoyOfPaintingClientNetwork {
    private JoyOfPaintingClientNetwork() {}

    @SubscribeEvent
    public static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");

        r.playToClient(CloseGuiPacket.TYPE, CloseGuiPacket.STREAM_CODEC, ClientPayloadHandler::handleCloseGui);
        r.playToClient(ExportPaintingPacket.TYPE, ExportPaintingPacket.STREAM_CODEC, ClientPayloadHandler::handleExport);
        r.playToClient(ImportPaintingPacket.TYPE, ImportPaintingPacket.STREAM_CODEC, ClientPayloadHandler::handleImport);
        r.playToClient(OpenGuiPacket.TYPE, OpenGuiPacket.STREAM_CODEC, ClientPayloadHandler::handleOpenGui);
        r.playToClient(PictureSendPacket.TYPE, PictureSendPacket.STREAM_CODEC, PictureSendPacket::handle);
    }
}
