package fr.younishd.zombievirus;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.bukkit.enchantments.Enchantment.BINDING_CURSE;
import static org.bukkit.enchantments.Enchantment.VANISHING_CURSE;
import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.*;

public class ZombieVirusPlugin extends JavaPlugin implements Listener {

    private static final int MIN_FOOD_LEVEL = 8;
    private static final int MIN_SATURATION_LEVEL = 8;
    private static final float ZOMBIE_INFECTION_PROB = 0.2f;

    private List<Player> zombies;

    @Override
    public void onEnable() {
        this.getLogger().info("Hello.");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.zombies = new ArrayList<>();

        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            for (Player p : this.zombies) {
                this.refreshPotions(p);
                p.getWorld().spawnEntity(p.getLocation().add(
                        new Random().nextInt(20 + 20 + 1) - 20,
                        new Random().nextInt(10 - 5 + 1) + 5,
                        new Random().nextInt(20 + 20 + 1) - 20),
                        EntityType.ZOMBIE);
            }
        }, 0, 20 * 10);
    }

    @Override
    public void onDisable() {
        for (Player p : this.zombies) p.setHealth(0);

        this.getLogger().info("Bye.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("zombie")) {
            if (args.length != 1) {
                sender.sendMessage("Wrong number of arguments.");
                return false;
            }

            boolean found = false;
            Player player = null;
            for (Player p : this.getServer().getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    found = true;
                    player = p;
                    sender.sendMessage("Player " + p.getName() + " is now infected.");
                    break;
                }
            }
            if (!found) {
                sender.sendMessage("No such player online.");
                return false;
            }

            this.zombies.add(player);
            this.zombify(player);

            return true;
        }
        return false;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player &&
                e.getEntity() instanceof Player &&
                this.isZombie((Player) e.getDamager()) &&
                !this.isZombie((Player) e.getEntity())) {

            this.zombies.add((Player) e.getEntity());
            this.zombify((Player) e.getEntity());
        } else if (e.getEntity() instanceof Player &&
                e.getDamager().getType() == EntityType.ZOMBIE &&
                !this.isZombie((Player) e.getEntity())) {

            if (new Random().nextFloat() < ZOMBIE_INFECTION_PROB) {
                this.zombies.add((Player) e.getEntity());
                this.zombify((Player) e.getEntity());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (this.isZombie(e.getPlayer())) {
            this.zombify(e.getPlayer());
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        if (this.isZombie(player) && e.getFoodLevel() < MIN_FOOD_LEVEL) {
            player.setFoodLevel(MIN_FOOD_LEVEL);
            player.setSaturation(MIN_SATURATION_LEVEL);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player && (
                e.getReason() == CLOSEST_PLAYER ||
                        e.getReason() == TARGET_ATTACKED_ENTITY ||
                        e.getReason() == TARGET_ATTACKED_NEARBY_ENTITY)) {
            if (this.isZombie((Player) e.getTarget())) {
                e.setTarget(null);
            }
        }
    }

    private boolean isZombie(Player player) {
        boolean found = false;
        for (Player p : this.zombies) {
            if (player.getName().equals(p.getName())) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void zombify(Player p) {
        p.getInventory().clear();

        ItemStack[] armor = new ItemStack[4];

        armor[3] = new ItemStack(Material.ZOMBIE_HEAD, 1);
        armor[3].addEnchantment(BINDING_CURSE, 1);
        armor[3].addEnchantment(VANISHING_CURSE, 1);

        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        LeatherArmorMeta meta_chestplate = (LeatherArmorMeta) armor[2].getItemMeta();
        meta_chestplate.setColor(Color.AQUA);
        meta_chestplate.setUnbreakable(true);
        armor[2].setItemMeta(meta_chestplate);
        armor[2].addEnchantment(BINDING_CURSE, 1);
        armor[2].addEnchantment(VANISHING_CURSE, 1);

        armor[1] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        LeatherArmorMeta meta_leggings = (LeatherArmorMeta) armor[1].getItemMeta();
        meta_leggings.setColor(Color.BLUE);
        meta_leggings.setUnbreakable(true);
        armor[1].setItemMeta(meta_leggings);
        armor[1].addEnchantment(BINDING_CURSE, 1);
        armor[1].addEnchantment(VANISHING_CURSE, 1);

        armor[0] = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta meta_boots = (LeatherArmorMeta) armor[0].getItemMeta();
        meta_boots.setColor(Color.GRAY);
        meta_boots.setUnbreakable(true);
        armor[0].setItemMeta(meta_boots);
        armor[0].addEnchantment(BINDING_CURSE, 1);
        armor[0].addEnchantment(VANISHING_CURSE, 1);

        p.getInventory().setArmorContents(armor);
        p.getInventory().addItem(new ItemStack(Material.ROTTEN_FLESH, new Random().nextInt(11 - 3 + 1) + 3));
        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD, 1);
        sword.addEnchantment(VANISHING_CURSE, 1);
        p.getInventory().addItem(sword);

        p.setFoodLevel(MIN_FOOD_LEVEL);

        this.refreshPotions(p);

        for (Entity entity : p.getWorld().getNearbyEntities(p.getLocation(), 32, 32, 32)) {
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                if (mob.getTarget() != null && mob.getTarget().getName().equals(p.getName())) {
                    mob.setTarget(null);
                }
            }
        }
    }

    private void refreshPotions(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 30, new Random().nextInt(5 - 1 + 1) + 1), true);
        if (new Random().nextBoolean()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, new Random().nextInt(2 + 1)), true);
        } else {
            p.removePotionEffect(PotionEffectType.SLOW);
        }
    }

}
