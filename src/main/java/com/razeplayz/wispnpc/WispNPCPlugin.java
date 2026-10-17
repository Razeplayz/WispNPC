package com.razeplayz.wispnpc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;

import java.util.Locale;
import java.util.Objects;

public final class WispNPCPlugin extends JavaPlugin implements Listener {
    private NamespacedKey npcKey;
    private NamespacedKey typeKey;
    private NamespacedKey healthKey;

    @Override public void onEnable() {
        saveDefaultConfig();
        npcKey = new NamespacedKey(this, "npc");
        typeKey = new NamespacedKey(this, "type");
        healthKey = new NamespacedKey(this, "dummy_health");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("WispNPC enabled for Paper 1.21.11.");
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wispnpc.admin")) { sender.sendMessage(color("&cYou don't have permission.")); return true; }
        if (args.length == 0) { sendHelp(sender); return true; }
        if (args[0].equalsIgnoreCase("reload")) { reloadConfig(); sender.sendMessage(color("&aWispNPC config reloaded.")); return true; }
        if (!(sender instanceof Player player)) { sender.sendMessage("This command must be run in-game."); return true; }
        if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length < 2) { sender.sendMessage(color("&cUsage: /npc spawn <fighter|netherite>")); return true; }
            String type = args[1].toLowerCase(Locale.ROOT);
            if (type.equals("fighter")) spawnFighter(player.getLocation());
            else if (type.equals("netherite")) spawnDummy(player.getLocation());
            else { sender.sendMessage(color("&cUnknown type. Use fighter or netherite.")); return true; }
            sender.sendMessage(color("&aSpawned &f" + type + " &aNPC.")); return true;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            Entity target = getTargetNpc(player, 6.0);
            if (target == null) { player.sendMessage(color("&cLook directly at a WispNPC within 6 blocks.")); return true; }
            target.remove(); player.sendMessage(color("&aRemoved the NPC.")); return true;
        }
        sendHelp(sender); return true;
    }

    private void spawnFighter(Location source) {
        Zombie zombie = (Zombie) source.getWorld().spawnEntity(source.clone(), EntityType.ZOMBIE);
        mark(zombie, "fighter");
        zombie.setCustomName(color(getConfig().getString("fighter.name", "&c&lWisp Fighter")));
        zombie.setCustomNameVisible(true);
        zombie.setAdult();
        zombie.setCanPickupItems(false);
        zombie.setRemoveWhenFarAway(false);
        zombie.setPersistent(true);
        zombie.setShouldBurnInDay(false);
        zombie.setAware(true);
        setMaxHealth(zombie, getConfig().getDouble("fighter.max-health", 40.0));
        zombie.setHealth(Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).getValue());
        EntityEquipment eq = zombie.getEquipment();
        if (eq != null) {
            Material helmet = Material.matchMaterial(getConfig().getString("fighter.armor", "DIAMOND") + "_HELMET");
            eq.setHelmet(new ItemStack(helmet == null ? Material.DIAMOND_HELMET : helmet));
            eq.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            eq.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
            Material weapon = Material.matchMaterial(getConfig().getString("fighter.weapon", "DIAMOND_SWORD"));
            eq.setItemInMainHand(new ItemStack(weapon == null ? Material.DIAMOND_SWORD : weapon));
            eq.setHelmetDropChance(0f); eq.setChestplateDropChance(0f); eq.setLeggingsDropChance(0f); eq.setBootsDropChance(0f); eq.setItemInMainHandDropChance(0f);
        }
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
    }

    private void spawnDummy(Location source) {
        Zombie zombie = (Zombie) source.getWorld().spawnEntity(source.clone(), EntityType.ZOMBIE);
        mark(zombie, "netherite");
        zombie.setCustomName(color(getConfig().getString("netherite-dummy.name", "&5&lNetherite Dummy")));
        zombie.setCustomNameVisible(true);
        zombie.setAdult();
        zombie.setAI(false);
        zombie.setAware(false);
        zombie.setCanPickupItems(false);
        zombie.setRemoveWhenFarAway(false);
        zombie.setPersistent(true);
        zombie.setShouldBurnInDay(false);
        zombie.setInvulnerable(getConfig().getBoolean("netherite-dummy.invulnerable", false));
        setMaxHealth(zombie, getConfig().getDouble("netherite-dummy.max-health", 1024.0));
        zombie.setHealth(Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).getValue());
        EntityEquipment eq = zombie.getEquipment();
        if (eq != null) {
            eq.setHelmet(maxed(Material.NETHERITE_HELMET, new EnchantSpec("protection", 4), new EnchantSpec("unbreaking", 3), new EnchantSpec("mending", 1), new EnchantSpec("thorns", 3), new EnchantSpec("respiration", 3), new EnchantSpec("aqua_affinity", 1)));
            eq.setChestplate(maxed(Material.NETHERITE_CHESTPLATE, new EnchantSpec("protection", 4), new EnchantSpec("unbreaking", 3), new EnchantSpec("mending", 1), new EnchantSpec("thorns", 3)));
            eq.setLeggings(maxed(Material.NETHERITE_LEGGINGS, new EnchantSpec("protection", 4), new EnchantSpec("unbreaking", 3), new EnchantSpec("mending", 1), new EnchantSpec("thorns", 3), new EnchantSpec("swift_sneak", 3)));
            eq.setBoots(maxed(Material.NETHERITE_BOOTS, new EnchantSpec("protection", 4), new EnchantSpec("unbreaking", 3), new EnchantSpec("mending", 1), new EnchantSpec("thorns", 3), new EnchantSpec("feather_falling", 4), new EnchantSpec("depth_strider", 3), new EnchantSpec("soul_speed", 3)));
            eq.setItemInMainHand(maxed(Material.NETHERITE_SWORD, new EnchantSpec("sharpness", 5), new EnchantSpec("unbreaking", 3), new EnchantSpec("mending", 1), new EnchantSpec("fire_aspect", 2), new EnchantSpec("looting", 3), new EnchantSpec("sweeping_edge", 3), new EnchantSpec("knockback", 2)));
            eq.setItemInOffHand(maxed(Material.SHIELD, new EnchantSpec("unbreaking", 3), new EnchantSpec("mending", 1)));
            eq.setHelmetDropChance(0f); eq.setChestplateDropChance(0f); eq.setLeggingsDropChance(0f); eq.setBootsDropChance(0f); eq.setItemInMainHandDropChance(0f); eq.setItemInOffHandDropChance(0f);
        }
        zombie.getPersistentDataContainer().set(healthKey, PersistentDataType.DOUBLE, zombie.getHealth());
    }

    private void mark(Entity entity, String type) {
        entity.getPersistentDataContainer().set(npcKey, PersistentDataType.BYTE, (byte) 1);
        entity.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type);
    }

    private void setMaxHealth(Zombie zombie, double value) {
        double health = Math.max(1.0, Math.min(value, 1024.0));
        Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(health);
    }

    private ItemStack maxed(Material material, EnchantSpec... enchants) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (EnchantSpec spec : enchants) {
                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(spec.key));
                if (enchantment != null) meta.addEnchant(enchantment, spec.level, true);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || !isNpc(zombie)) return;
        String type = zombie.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (!"fighter".equals(type)) { event.setCancelled(true); return; }
        if (event.getTarget() != null && !(event.getTarget() instanceof Player)) event.setCancelled(true);
    }

    @EventHandler public void onDummyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || !isNpc(zombie)) return;
        String type = zombie.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (!"netherite".equals(type) || zombie.isInvulnerable()) return;
        if (!getConfig().getBoolean("netherite-dummy.auto-heal", true)) return;
        double max = Objects.requireNonNull(zombie.getAttribute(Attribute.MAX_HEALTH)).getValue();
        double damage = event.getFinalDamage();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!zombie.isValid() || zombie.isDead()) return;
            zombie.setHealth(Math.min(max, zombie.getHealth() + damage));
        }, Math.max(1L, getConfig().getLong("netherite-dummy.auto-heal-delay-ticks", 30)));
    }

    @EventHandler public void onDeath(EntityDeathEvent event) {
        if (isNpc(event.getEntity())) event.getDrops().clear();
    }

    private boolean isNpc(Entity entity) {
        return entity.getPersistentDataContainer().has(npcKey, PersistentDataType.BYTE);
    }

    private Entity getTargetNpc(Player player, double maxDistance) {
        var ray = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), maxDistance,
                e -> e instanceof Zombie && isNpc(e));
        return ray == null ? null : ray.getHitEntity();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&6WispNPC commands:"));
        sender.sendMessage(color("&e/npc spawn fighter &7- Spawn an aggressive fighter"));
        sender.sendMessage(color("&e/npc spawn netherite &7- Spawn a stationary knockback dummy"));
        sender.sendMessage(color("&e/npc remove &7- Remove the NPC you're looking at"));
        sender.sendMessage(color("&e/npc reload &7- Reload config.yml"));
    }

    private String color(String input) { return ChatColor.translateAlternateColorCodes('&', input); }
    private record EnchantSpec(String key, int level) { }
}