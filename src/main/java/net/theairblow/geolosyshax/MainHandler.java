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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@EventHandler
public class MainHandler {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final HashMap<IOre, List<BlockPos>> veins = new HashMap<>();
    private static final List<ChunkPos> pos = new ArrayList<>();

    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event) {
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
                        pos.add(chunk.getPos());
                    }

                    synchronized(veins) {
                        if (!veins.containsKey(deposit.get()))
                            veins.put(deposit.get(), new ArrayList<>());
                        final List<BlockPos> list = veins.get(deposit.get());
                        if (list.contains(start)) return;
                        list.add(start);
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
            GeolosysUtil.sendToChat("§6Veins in the ignore list are still added to history!\n" +
                    "§2!hax found <ID> <page> - §3Lists all coords of veins with that ID\n" +
                    "§2!hax info <ID> - §3Prints all info about a specific ore vein\n" +
                    "§2!hax found list - §3Tells you what veins were found\n" +
                    "§2!hax list - §3Lists all vein names and their IDs\n" +
                    "§2!hax clear - §3Clears found ore veins list", true);
        else switch (args[1]) {
            case "clear":
                GeolosysUtil.sendToChat("§2Successfully cleared found veins list!");
                synchronized(pos) {
                    pos.clear();
                }
                synchronized(veins) {
                    veins.clear();
                }
                break;
            case "found":
                if (args.length < 3)
                    GeolosysUtil.sendToChat("§6Usage: §c!hax found <ID> <page> §6/ §c!hax found list");
                else if (args[2].equals("list")) {
                    GeolosysUtil.sendToChat("§c====== §2List of all found veins§c ======", true);
                    ArrayList<IOre> oreBlocks = GeolosysAPI.oreBlocks;
                    for (int i = 0; i < oreBlocks.size(); i++) {
                        IOre vein = oreBlocks.get(i);
                        if (veins.containsKey(vein))
                            GeolosysUtil.sendToChat("§3-> §a" + vein.getFriendlyName()
                                    + "§5 (ID: " + i + ", " + veins.get(vein).size() + "veins)", true);
                        else GeolosysUtil.sendToChat("§3-> §4" + vein.getFriendlyName()
                                + "§5 (ID: " + i + ")", true);
                    }
                } else try {
                    int page;
                    final int id = Integer.parseInt(args[2]);
                    if (args.length < 4) page = 1;
                    else page = Integer.parseInt(args[3]);
                    final IOre vein = (IOre)GeolosysAPI.oreBlocks.toArray()[id];
                    final int pages = (int)Math.ceil(veins.get(vein).size() / 10D);
                    if (veins.containsKey(vein)) {
                        if (page > pages) GeolosysUtil.sendToChat("§4This page doesn't exist (there's only " + pages + ")");
                        else if (page < 1) GeolosysUtil.sendToChat("§4Pages start at one and can't be negative!");
                        else {
                            GeolosysUtil.sendToChat("§c====== §2" + vein.getFriendlyName()
                                    + " §a(Page " + page + "/" + pages + ")§c ======", true);
                            for (BlockPos pos : veins.get(vein).stream().skip((page - 1) * 10L)
                                    .limit(10).collect(Collectors.toList()))
                                GeolosysUtil.sendToChat("§3-> §a" + pos.getX() + " "
                                        + pos.getY() + " " + pos.getZ(), true);
                        }
                    } else GeolosysUtil.sendToChat("§4No veins were found yet!");
                } catch (Exception e) {
                    GeolosysUtil.sendToChat("§4Invalid ID or page number!");
                }
                break;
            case "info":
                if (args.length < 3)
                    GeolosysUtil.sendToChat("§6Usage: §c!hax info <ID>");
                else {
                    try {
                        final int id = Integer.parseInt(args[2]);
                        final IOre vein = (IOre)GeolosysAPI.oreBlocks.toArray()[id];
                        final Optional<List<String>> biomes = GeolosysUtil.listBiomes(vein);
                        GeolosysUtil.sendToChat("§c====== §2Info for §a\"" + vein.getFriendlyName() + "\"§c ======", true);
                        if (biomes.isPresent()) {
                            GeolosysUtil.sendToChat("§2Biomes list:", true);
                            for (String str : biomes.get()) {
                                GeolosysUtil.sendToChat("§3-> §a" + str, true);
                            }
                        }
                        GeolosysUtil.sendToChat("§2Y from §a" + vein.getYMin() + "§2 to §a" + vein.getYMax(), true);
                        GeolosysUtil.sendToChat("§2Density: §a" + vein.getDensity(), true);
                        GeolosysUtil.sendToChat("§2Chance: §a" + vein.getChance(), true);
                        GeolosysUtil.sendToChat("§2Size: §a" + vein.getSize(), true);
                    } catch (Exception e) {
                        GeolosysUtil.sendToChat("§4Invalid ID!");
                    }
                }
                break;
            case "list":
                GeolosysUtil.sendToChat("§c====== §2List of all ore veins:§c ======", true);
                for (int i = 0; i < GeolosysAPI.oreBlocks.size(); i++) {
                    final IOre vein = GeolosysAPI.oreBlocks.get(i);
                    GeolosysUtil.sendToChat("§3-> §a" + vein.getFriendlyName()
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
