package edmina;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Eventos implements Listener {
    private EDMina pl = EDMina.getPlugin(EDMina.class);
    @EventHandler
    public void onPosMark(PlayerInteractEvent event){
        if(event.getClickedBlock() != null){
            try {
                if(event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("§bEDMina")){
                    event.setCancelled(true);
                    Player p = event.getPlayer();
                    Location loc = event.getClickedBlock().getLocation();
                    String pos = loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ() + ";" + loc.getWorld().getName();
                    Action action = event.getAction();
                    if(action == Action.LEFT_CLICK_BLOCK){
                        if(pl.pos1.containsKey(p)){
                            pl.pos1.replace(p, pos);
                        } else {
                            pl.pos1.put(p, pos);
                        }
                        p.sendMessage(pl.getString("pos1-marcada"));
                    } else {
                        if(action == Action.RIGHT_CLICK_BLOCK){
                            if(pl.pos2.containsKey(p)){
                                pl.pos2.replace(p, pos);
                            } else {
                                pl.pos2.put(p, pos);
                            }
                            p.sendMessage(pl.getString("pos2-marcada"));
                        }
                    }
                }
            } catch (Exception e){}
        }
    }
}
