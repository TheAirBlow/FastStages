package net.theairblow.geolosyshax;

import com.oitsjustjose.geolosys.common.api.GeolosysAPI;
import com.oitsjustjose.geolosys.common.api.world.IOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.theairblow.capatible.annotations.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EventHandler
public class MainHandler {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final List<ChunkPos> pos = new ArrayList<>();
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
        if (!enabled) return;
        executor.execute(() -> {
            final Chunk chunk = event.getChunk();
            final List<IBlockState> blocks = new ArrayList<>();
            for (int x = 0; x < 16; x++)
            for (int y = 0; y < 256; y++)
            for (int z = 0; z < 16; z++) {
                final IBlockState state = chunk.getBlockState(new BlockPos(x, y, z));
                if (!blocks.contains(state))
                    blocks.add(state);
            }

            if (!blocks.isEmpty()) {
                final Optional<IOre> deposit = GeolosysUtil.getDeposit(blocks);
                if (deposit.isPresent()) {
                    final BlockPos start = GeolosysUtil.findVeinStart(chunk, deposit.get());
                    if (start == null) {
                        GeolosysHax.LOGGER.error("Vein start is null for some reason...?");
                        return;
                    }
                    final String name = deposit.get().getFriendlyName();
                    final int cx = chunk.getPos().x;
                    final int cz = chunk.getPos().z;
                    synchronized(pos) {
                        // Check for duplicates or if chunk is a part a multi-chunk vein (reduces chat spam)
                        if (pos.contains(new ChunkPos(cx, cz))
                                || pos.contains(new ChunkPos(cx, cz - 1))
                                || pos.contains(new ChunkPos(cx, cz + 1))
                                || pos.contains(new ChunkPos(cx - 1, cz))
                                || pos.contains(new ChunkPos(cx + 1, cz))
                                || pos.contains(new ChunkPos(cx + 1, cz + 1))
                                || pos.contains(new ChunkPos(cx - 1, cz + 1))
                                || pos.contains(new ChunkPos(cx + 1, cz - 1))
                                || pos.contains(new ChunkPos(cx - 1, cz + 1)))
                            return;
                        GeolosysUtil.sendToChat("§2Found §3" + name + "§2 at §a"
                                + start.getX() + " " + start.getY() + " " + start.getZ());
                        pos.add(chunk.getPos());
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onChat(ClientChatEvent event) {
        final Minecraft minecraft = Minecraft.getMinecraft();
        final String message = event.getOriginalMessage().toLowerCase();
        if (!message.startsWith("!hax")) return;
        final String[] args = message.split(" ");
        if (args.length == 1)
            GeolosysUtil.sendToChat("" + // This is here just so IntelliJ places the "str:" here
                    "§2!hax [enable/disable] - §3Automatically finds veins in chunks\n" +
                    "§2!hax info <ID> - §3Prints all info about a specific ore vein\n" +
                    "§2!hax list - §3Lists all vein names and their IDs\n" +
                    "§2!hax clear - §3Clears internal ore vein history", true);
        else switch (args[1]) {
            case "enable":
                GeolosysUtil.sendToChat("§2Ready to search for veins and spam your chat!");
                enabled = true;
                break;
            case "disable":
                GeolosysUtil.sendToChat("§4Stopped annoying you with too many messages in chat.");
                enabled = false;
                break;
            case "clear":
                GeolosysUtil.sendToChat("§2Successfully cleared ore vein history!");
                synchronized(pos) {
                    pos.clear();
                }
                break;
            case "info":
                if (args.length < 3)
                    GeolosysUtil.sendToChat("§6Usage: §c !hax info <ID>");
                else {
                    try {
                        final int id = Integer.parseInt(args[2]);
                        final IOre vein = (IOre)GeolosysAPI.oreBlocks.toArray()[id];
                        final Optional<List<String>> biomes = GeolosysUtil.listBiomes(vein);
                        GeolosysUtil.sendToChat("====== §2Info for \"" + vein.getFriendlyName() + "\"§r ======", true);
                        if (biomes.isPresent()) {
                            GeolosysUtil.sendToChat("§2Biomes list:", true);
                            for (String str : biomes.get()) {
                                GeolosysUtil.sendToChat("§3-> §a " + str, true);
                            }
                        }
                        GeolosysUtil.sendToChat("§2Y from " + vein.getYMin() + " to " + vein.getYMax(), true);
                        GeolosysUtil.sendToChat("§2Density: " + vein.getDensity(), true);
                        GeolosysUtil.sendToChat("§2Chance: " + vein.getChance(), true);
                        GeolosysUtil.sendToChat("§2Size: " + vein.getSize(), true);
                    } catch (Exception e) {
                        GeolosysUtil.sendToChat("§4Invalid ID!");
                    }
                }
                break;
            case "list":
                GeolosysUtil.sendToChat("====== §2List of all ore veins:§r ======", true);
                for (int i = 0; i < GeolosysAPI.oreBlocks.size(); i++) {
                    final IOre vein = GeolosysAPI.oreBlocks.get(i);
                    GeolosysUtil.sendToChat("§3-> §a " + vein.getFriendlyName()
                            + "§c (ID: " + i + ")", true);
                }
                break;
            default:
                GeolosysUtil.sendToChat("§4Unknown command!");
                break;
        }

        event.setCanceled(true); // Cancel and add message to history
        minecraft.ingameGUI.getChatGUI().addToSentMessages(message);
    }
}
