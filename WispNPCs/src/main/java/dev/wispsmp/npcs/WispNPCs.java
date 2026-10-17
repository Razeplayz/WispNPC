package dev.wispsmp.npcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class WispNPCs extends JavaPlugin implements Listener {
    private NPCRegistry registry;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            getLogger().severe("Citizens is required. Install a Citizens build compatible with your Paper version, then restart.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        registry = CitizensAPI.getNPCRegistry();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("WispNPCs enabled with Citizens NPC support.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(sender);
            return true;
        }
        if (!sender.hasPermission("wispnpcs.admin")) {
            msg(sender, "&cYou don't have permission.");
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            if (!(sender instanceof Player player)) {
                msg(sender, "&cRun this in-game.");
                return true;
            }
            if (args.length < 2) {
                msg(sender, "&cUsage: /wnpc create <name>");
                return true;
            }
            String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            NPC npc = registry.createNPC(EntityType.PLAYER, ChatColor.translateAlternateColorCodes('&', name));
            npc.spawn(player.getLocation());
            msg(sender, "&aCreated player-like NPC &f" + npc.getName() + " &7(ID " + npc.getId() + ").");
            msg(sender, "&7Use &f/wnpc skin " + npc.getId() + " <playername> &7to set its skin.");
            return true;
        }
        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
                msg(sender, "&cUsage: /wnpc delete <id>");
                return true;
            }
            NPC npc = getNpc(args[1]);
            if (npc == null) {
                msg(sender, "&cNPC not found. Use its numeric ID.");
                return true;
            }
            npc.destroy();
            getConfig().set("npcs." + npc.getId(), null);
            saveConfig();
            msg(sender, "&aDeleted NPC.");
            return true;
        }
        if (args[0].equalsIgnoreCase("kit")) {
            if (args.length < 3) {
                msg(sender, "&cUsage: /wnpc kit <npc-id> <kit-name>");
                return true;
            }
            NPC npc = getNpc(args[1]);
            if (npc == null) {
                msg(sender, "&cNPC not found. Use its numeric ID.");
                return true;
            }
            String kit = args[2];
            getConfig().set("npcs." + npc.getId() + ".type", "player");
            getConfig().set("npcs." + npc.getId() + ".command", "kit " + kit);
            saveConfig();
            msg(sender, "&aNPC " + npc.getName() + " now runs &f/kit " + kit + " &afor the player who right-clicks it.");
            msg(sender, "&7If your WispKits claim command differs, use /wnpc action instead.");
            return true;
        }
        if (args[0].equalsIgnoreCase("action")) {
            if (args.length < 4) {
                msg(sender, "&cUsage: /wnpc action <npc-id> <player|console> <command...>");
                return true;
            }
            NPC npc = getNpc(args[1]);
            if (npc == null) {
                msg(sender, "&cNPC not found. Use its numeric ID.");
                return true;
            }
            String type = args[2].toLowerCase(Locale.ROOT);
            if (!type.equals("player") && !type.equals("console")) {
                msg(sender, "&cChoose player or console.");
                return true;
            }
            String cmd = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            getConfig().set("npcs." + npc.getId() + ".type", type);
            getConfig().set("npcs." + npc.getId() + ".command", cmd);
            saveConfig();
            msg(sender, "&aSaved right-click action for NPC " + npc.getName() + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("skin")) {
            if (args.length != 3) {
                msg(sender, "&cUsage: /wnpc skin <npc-id> <playername>");
                return true;
            }
            NPC npc = getNpc(args[1]);
            if (npc == null) {
                msg(sender, "&cNPC not found. Use its numeric ID.");
                return true;
            }
            npc.getOrAddTrait(SkinTrait.class).setSkinName(args[2]);
            msg(sender, "&aRequested skin &f" + args[2] + "&a. It may take a moment to update.");
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            boolean any = false;
            for (NPC npc : registry) {
                msg(sender, "&7- &f" + npc.getName() + " &8(ID " + npc.getId() + ")");
                any = true;
            }
            if (!any) {
                msg(sender, "&7No Citizens NPCs found.");
            }
            return true;
        }
        help(sender);
        return true;
    }

    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent event) {
        if (!CitizensAPI.getNPCRegistry().isNPC(event.getRightClicked())) {
            return;
        }
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());
        if (npc == null) {
            return;
        }
        String path = "npcs." + npc.getId();
        String cmd = getConfig().getString(path + ".command");
        String type = getConfig().getString(path + ".type", "player");
        if (cmd == null || cmd.isBlank()) {
            return;
        }
        event.setCancelled(true);
        Player clicker = event.getPlayer();
        cmd = cmd.replace("{player}", clicker.getName())
                .replace("{npc}", npc.getName())
                .replace("{npc_id}", String.valueOf(npc.getId()));
        if (type.equalsIgnoreCase("console")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.startsWith("/") ? cmd.substring(1) : cmd);
        } else {
            clicker.performCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
        }
    }

    private NPC getNpc(String id) {
        try {
            return registry.getById(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void help(CommandSender sender) {
        msg(sender, "&bWispNPCs commands:");
        msg(sender, "&f/wnpc create <name> &7- create player-like NPC at your location");
        msg(sender, "&f/wnpc list &7- list NPC IDs");
        msg(sender, "&f/wnpc skin <id> <playername> &7- set skin");
        msg(sender, "&f/wnpc kit <id> <kit> &7- right-click runs /kit <kit> as clicker");
        msg(sender, "&f/wnpc action <id> <player|console> <command> &7- custom click action");
        msg(sender, "&f/wnpc delete <id> &7- delete NPC");
    }

    private void msg(CommandSender sender, String text) {
        String prefix = getConfig().getString("messages.prefix", "&8[&bWispNPCs&8] &r");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + text));
    }
}