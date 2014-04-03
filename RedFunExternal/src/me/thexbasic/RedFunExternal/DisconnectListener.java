
package me.thexbasic.RedFunExternal;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author lobnews
 */
public class DisconnectListener implements Listener
{
    private final Main plugin;
    
    public DisconnectListener(Main plugin){
        this.plugin = plugin;
    }
    
    
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event){
        Player p = event.getPlayer();
        if(plugin.tasks.containsKey(p.getName() + "_spam")){
            BukkitTask task = plugin.tasks.get(p.getName() + "_spam");
            task.cancel();
            plugin.tasks.remove(p.getName() + "_spam");
        }
        if(plugin.tasks.containsKey(p.getName() + "_tp")){
            BukkitTask task = plugin.tasks.get(p.getName() + "_tp");
            task.cancel();
            plugin.tasks.remove(p.getName() + "_tp");
        }
        if(plugin.onInteract.containsKey(p.getName())){
            plugin.onInteract.remove(p.getName());
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player p = event.getPlayer();
        if(plugin.onInteract.containsKey(p.getName())){
            plugin.tpPlayer(p);
        }
    }
}