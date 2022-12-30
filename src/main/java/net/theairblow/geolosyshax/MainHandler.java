package net.theairblow.geolosyshax;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.theairblow.capatible.annotations.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EventHandler
public class MainHandler {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        if (!enabled) return;
        executor.execute(() -> {
            final Minecraft minecraft = Minecraft.getMinecraft();
            final Chunk chunk = event.getChunk();
            final List<String> names = new ArrayList<>();
            for (int x = 0; x < 16; x++)
            for (int y = 0; y < 256; y++)
            for (int z = 0; z < 16; z++) {
                final IBlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                final Block block = state.getBlock();
                try {
                    if (!Objects.requireNonNull(block.getRegistryName())
                            .getNamespace().equals("geolosys")) continue;
                } catch (NullPointerException e) {
                    GeolosysHax.LOGGER.error("Unable to get namespace for " + block.getLocalizedName());
                    e.printStackTrace();
                }

                // All of these are null because BlockOre/BlockOreVanilla getPickBlock uses only state argument
                final ItemStack item = block.getPickBlock(state, null, null, null, null);
                final String name = I18n.translateToLocal(item.getItem().getTranslationKey(item));
                if (!names.contains(name)) names.add(name);
            }

            if (!names.isEmpty()) {
                final String joined = String.join(", ", names);
                final String x = Integer.toString(chunk.getPos().x * 16 + 8);
                final String z = Integer.toString(chunk.getPos().z * 16 + 8);
                minecraft.addScheduledTask(() -> Minecraft.getMinecraft().ingameGUI.getChatGUI()
                        .printChatMessage(new TextComponentString("Found " + joined + " at " + x + " " + z)));
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onKey(InputEvent.KeyInputEvent event) {
        if (!GeolosysHax.BINDING.isPressed()) return;
        final EntityPlayerSP player = Minecraft.getMinecraft().player;
        final GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        enabled = !enabled; // Switch the enabled value

        if (enabled) chat.printChatMessage(new TextComponentString("GeolosysHax is ready to serve you!"));
        else chat.printChatMessage(new TextComponentString("Oh... No more hax :("));
    }
}
