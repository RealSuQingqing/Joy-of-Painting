package com.leclowndu93150.joyofpainting.network.packets;

import com.leclowndu93150.joyofpainting.command.CommandExport;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExportPaintingPacket {
    public final String canvasId;

    public ExportPaintingPacket(String canvasId) {
        this.canvasId = canvasId;
    }

    public ExportPaintingPacket(FriendlyByteBuf buf) {
        this.canvasId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(canvasId);
    }

    public static void handle(ExportPaintingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg));
    }

    private static void handleClient(ExportPaintingPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (CommandExport.doExport(mc.player, msg.canvasId)) {
            mc.player.displayClientMessage(Component.translatable("joyofpainting.export.success", msg.canvasId).withStyle(ChatFormatting.GREEN), false);
        } else {
            mc.player.displayClientMessage(Component.translatable("joyofpainting.export.fail", msg.canvasId).withStyle(ChatFormatting.RED), false);
        }
    }
}
