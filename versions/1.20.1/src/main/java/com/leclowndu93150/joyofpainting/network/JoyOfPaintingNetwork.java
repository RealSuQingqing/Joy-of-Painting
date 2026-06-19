package com.leclowndu93150.joyofpainting.network;

import com.leclowndu93150.joyofpainting.JoyOfPainting;
import com.leclowndu93150.joyofpainting.network.packets.CanvasMiniUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.CanvasUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.CloseGuiPacket;
import com.leclowndu93150.joyofpainting.network.packets.EaselLeftPacket;
import com.leclowndu93150.joyofpainting.network.packets.ExportPaintingPacket;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingPacket;
import com.leclowndu93150.joyofpainting.network.packets.ImportPaintingSendPacket;
import com.leclowndu93150.joyofpainting.network.packets.OpenGuiPacket;
import com.leclowndu93150.joyofpainting.network.packets.PaletteUpdatePacket;
import com.leclowndu93150.joyofpainting.network.packets.PictureRequestPacket;
import com.leclowndu93150.joyofpainting.network.packets.PictureSendPacket;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class JoyOfPaintingNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(JoyOfPainting.id("main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int id = 0;

    private JoyOfPaintingNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(CanvasUpdatePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CanvasUpdatePacket::encode).decoder(CanvasUpdatePacket::new)
                .consumerMainThread(CanvasUpdatePacket::handle).add();
        CHANNEL.messageBuilder(CanvasMiniUpdatePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CanvasMiniUpdatePacket::encode).decoder(CanvasMiniUpdatePacket::new)
                .consumerMainThread(CanvasMiniUpdatePacket::handle).add();
        CHANNEL.messageBuilder(EaselLeftPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EaselLeftPacket::encode).decoder(EaselLeftPacket::new)
                .consumerMainThread(EaselLeftPacket::handle).add();
        CHANNEL.messageBuilder(ImportPaintingSendPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ImportPaintingSendPacket::encode).decoder(ImportPaintingSendPacket::new)
                .consumerMainThread(ImportPaintingSendPacket::handle).add();
        CHANNEL.messageBuilder(PaletteUpdatePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PaletteUpdatePacket::encode).decoder(PaletteUpdatePacket::new)
                .consumerMainThread(PaletteUpdatePacket::handle).add();
        CHANNEL.messageBuilder(PictureRequestPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PictureRequestPacket::encode).decoder(PictureRequestPacket::new)
                .consumerMainThread(PictureRequestPacket::handle).add();

        CHANNEL.messageBuilder(CloseGuiPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CloseGuiPacket::encode).decoder(CloseGuiPacket::new)
                .consumerMainThread(CloseGuiPacket::handle).add();
        CHANNEL.messageBuilder(ExportPaintingPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ExportPaintingPacket::encode).decoder(ExportPaintingPacket::new)
                .consumerMainThread(ExportPaintingPacket::handle).add();
        CHANNEL.messageBuilder(ImportPaintingPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ImportPaintingPacket::encode).decoder(ImportPaintingPacket::new)
                .consumerMainThread(ImportPaintingPacket::handle).add();
        CHANNEL.messageBuilder(OpenGuiPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenGuiPacket::encode).decoder(OpenGuiPacket::new)
                .consumerMainThread(OpenGuiPacket::handle).add();
        CHANNEL.messageBuilder(PictureSendPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PictureSendPacket::encode).decoder(PictureSendPacket::new)
                .consumerMainThread(PictureSendPacket::handle).add();
    }
}
