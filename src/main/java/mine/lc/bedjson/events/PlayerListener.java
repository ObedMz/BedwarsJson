package mine.lc.bedjson.events;

import mine.lc.bedjson.Bedjson;
import mine.lc.bedjson.controllers.InventoryManager;
import mine.lc.bedjson.controllers.JSONServer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    @EventHandler
    public void EntityInteractEvent(PlayerInteractEntityEvent event){
       String mode = event.getRightClicked().getName();
       event.getPlayer().playSound(event.getPlayer().getLocation(),
               Sound.NOTE_BASS, 5f,5f);
       InventoryManager.openGameInventory(event.getPlayer(), mode);
    }

    @EventHandler
    public void ClickInventoryMapMenu(InventoryClickEvent event){
        String title = ChatColor.stripColor(event.getView().getTitle());
        String cf_title = Bedjson.getInstance().getConfigController().getString("inventory.maps.title").replaceAll("%mode%", "");
        if(!title.startsWith(cf_title))
            return;
        event.setCancelled(true);
        event.getWhoClicked().closeInventory();
        String mode = title.split(":")[1].replaceAll(" ", "");
        ItemStack item = event.getCursor();
        if(!item.hasItemMeta()) return;
        if(!item.getItemMeta().getDisplayName().startsWith("Mapa")) return;
        String map = item.getItemMeta().getDisplayName().replaceAll("Mapa: ", "");
        JSONServer server = JSONServer.getBestServerByMapMode(mode,map);
        if(server == null){
            event.getWhoClicked().sendMessage(ChatColor.RED + "No hay arenas disponibles");
            return;
        }
        server.sendPlayerToServer((Player) event.getWhoClicked());

    }

    @EventHandler
    public void PlayerJoinPortal(PlayerPortalEvent e){
        JSONServer js = JSONServer.getRandom_server();
        if(js == null){
            e.getPlayer().sendMessage(ChatColor.RED + "No hay servidores disponibles.");
            return;
        }
        js.sendPlayerToServer(e.getPlayer());

    }

    @EventHandler
    public void ClickInventoryMode(InventoryClickEvent event){
        String title = ChatColor.stripColor(event.getView().getTitle());
        String cf_title = Bedjson.getInstance().getConfigController().getString("inventory.games.title").replaceAll("%mode%", "");
        if(!title.startsWith(cf_title))
            return;
        event.setCancelled(true);
        String mode = title.split(":")[1].replaceAll(" ", "");
        ConfigurationSection cfg = Bedjson.getInstance().getConfigController().getConfigurationSection("inventoru.games.items");
        for(String str : cfg.getKeys(false)){
            ConfigurationSection csg = cfg.getConfigurationSection(str);
            if(csg.getInt("slot") != event.getSlot())
                return;

            switch (csg.getString("action")){
                case "TELEPORT":
                    JSONServer.getBestServerByMode(mode).sendPlayerToServer((Player) event.getWhoClicked());
                    break;
                case "MAPS":
                    event.getWhoClicked().closeInventory();
                    InventoryManager.openMapMenu((Player)event.getWhoClicked(), mode);
                    break;
                case "CLOSE":
                    event.getWhoClicked().closeInventory();
                    break;
            }
            break;
        }

    }

}
