package me.ultimate.ST;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerTour extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("Server Tour - Create by Ultimate. http://dev.bukkit.org/profiles/ultimate_n00b/");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("st")) {
            final Player p = ((Player) sender);
            if (p.hasPermission("st.admin")) {
                if (args.length < 1) {
                    p.sendMessage(t("Try /st set, /st del, or /st done"));
                } else {
                    if (args[0].equalsIgnoreCase("set")) {
                        if (args.length > 2) {
                            final StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }

                            String allArgs = sb.toString().trim();
                            if (allArgs.equalsIgnoreCase("None"))
                                allArgs = null;
                            getConfig().set(args[1].toString() + ".Message", allArgs);
                            getConfig().set(args[1].toString() + ".world", p.getLocation().getWorld().getName());
                            getConfig().set(args[1].toString() + ".x", p.getLocation().getX());
                            getConfig().set(args[1].toString() + ".y", p.getLocation().getY());
                            getConfig().set(args[1].toString() + ".z", p.getLocation().getZ());
                            getConfig().set(args[1].toString() + ".pitch", p.getLocation().getPitch());
                            getConfig().set(args[1].toString() + ".yaw", p.getLocation().getYaw());
                            saveConfig();
                            p.sendMessage(t("Set position " + args[1] + " to your current position with the message:"));
                            p.sendMessage(t(allArgs));
                        } else {
                            p.sendMessage(t("Not enough arguments!"));
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        if (!p.hasPlayedBefore()) {
            addGuest(p);
            if (p.getInventory().contains(Material.STICK))
                p.getInventory().remove(Material.STICK);
            p.getInventory().addItem(stick());
        }
    }

    @EventHandler
    public void onItemHeldEvent(final PlayerItemHeldEvent event) {
        final Player p = event.getPlayer();
        if (isGuest(p)) {
            event.getPlayer().getInventory().setHeldItemSlot(0);
        }
    }

    @EventHandler
    public void onInteractEvent(final PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (isGuest(p)) {
                final int step = getConfig().getInt(p.getName()) + 1;
                getLogger().info(step + "");
                if (getConfig().get(step + "") != null) {
                    getConfig().set(p.getName(), step);
                    saveConfig();
                    p.teleport(new Location(Bukkit.getWorld(getConfig().getString(step + ".world")), getConfig()
                            .getInt(step + ".x"), getConfig().getInt(step + ".y"), getConfig().getInt(step + ".z"),
                            getConfig().getInt(step + ".pitch"), getConfig().getInt(step + ".yaw")));
                    if (getConfig().getString(step + ".Message") != null)
                        p.sendMessage(t(getConfig().getString(step + ".Message")));
                    saveConfig();
                } else {
                    removeGuest(p);
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (isGuest(p)) {
                final int step = getConfig().getInt(p.getName()) - 1;

                if (getConfig().get(step + "") != null) {
                    getConfig().set(p.getName(), step);
                    saveConfig();
                    p.teleport(new Location(Bukkit.getWorld(getConfig().getString(step + ".world")), getConfig()
                            .getInt(step + ".x"), getConfig().getInt(step + ".y"), getConfig().getInt(step + ".z"),
                            getConfig().getInt(step + ".pitch"), getConfig().getInt(step + ".yaw")));
                    if (getConfig().getString(step + ".Message") != null)
                        p.sendMessage(t(getConfig().getString(step + ".Message")));
                    saveConfig();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        if (isGuest(p)) {
            if (getConfig().getInt(p.getName()) > 0) {
                int step = getConfig().getInt(p.getName());
                Location l = new Location(Bukkit.getWorld(getConfig().getString(step + ".world")), getConfig().getInt(
                        step + ".x"), getConfig().getInt(step + ".y"), getConfig().getInt(step + ".z")).getBlock()
                        .getLocation();
                if (!p.getLocation().getBlock().getLocation().equals(l)) {
                    l.setPitch(p.getLocation().getPitch());
                    l.setYaw(p.getLocation().getYaw());
                    p.teleport(l);
                }
            }
        }
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent event) {
        if (isGuest((Player) event.getWhoClicked()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (isGuest(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().getInventory().setHeldItemSlot(0);
        }
    }

    Boolean isGuest(final Player p) {
        if (getConfig().get(p.getName()) != null)
            return true;
        return false;
    }

    String t(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', "&7" + msg);
    }

    ItemStack stick() {
        final ItemStack stick = new ItemStack(Material.STICK, 1);
        final ItemMeta sMeta = stick.getItemMeta();
        sMeta.setDisplayName(t("&6&lTutorial Stick"));
        final List<String> lore = Arrays.asList(t("&c--------------------- "), t("&4 Left Click to Move on"),
                t("&c-------------- "), t("&4 Right Click to Move Back"), t("&c--------------------- "));
        sMeta.setLore(lore);
        stick.setItemMeta(sMeta);
        return stick;
    }

    void addGuest(final Player p) {
        getConfig().set(p.getName(), 0);
        saveConfig();
    }

    void removeGuest(final Player p) {
        getConfig().set(p.getName(), null);
        saveConfig();
    }
}
