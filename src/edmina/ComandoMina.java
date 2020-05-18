package edmina;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class ComandoMina implements CommandExecutor {
    private EDMina pl = EDMina.getPlugin(EDMina.class);
    private int teste = 5;
    private int task = 0;


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player){
            Player p = (Player) sender;
            if(p.hasPermission("edmina.admin")){
                if(args.length == 0){
                    for(String m : pl.getConfig().getStringList("argumentos-mina")){
                        p.sendMessage(m.replaceAll("&", "§"));
                    }
                } else {
                    switch (args[0]){
                        case "teste":
                            if(task == 0){
                                task = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                                    @Override
                                    public void run() {
                                        if(teste > 0){
                                            teste--;
                                            ActionBar actionBar = new ActionBar("§cLimpando o chao em " + teste + " segundos");
                                            actionBar.sendToPlayer(p);
                                            p.playSound(p.getLocation(), Sound.LAVA_POP, 50, 50);
                                        } else {
                                            teste = 5;
                                            ActionBar actionBar = new ActionBar("§cChao limpo com sucesso");
                                            actionBar.sendToPlayer(p);
                                            p.playSound(p.getLocation(), Sound.LEVEL_UP, 50, 50);
                                            for(World world : Bukkit.getWorlds()){
                                                for(Entity entity : world.getEntities()){
                                                    if(!(entity instanceof Player)){
                                                        entity.remove();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }, 20, 20);
                            } else {
                                task = 0;
                                Bukkit.getScheduler().cancelTask(task);
                                ActionBar actionBar = new ActionBar("§cTask cancelada");
                                actionBar.sendToPlayer(p);
                            }

                            break;
                        case "deletar":
                            if(args.length == 2){
                                if(!pl.getConfig().contains("minas."+args[1])){
                                    p.sendMessage(pl.getString("mina-nao-existe"));
                                    return false;
                                }
                                pl.getConfig().set("minas."+args[1], null);
                                pl.saveConfig();
                                pl.reloadMinas();
                                p.sendMessage(pl.getString("mina-deletada").replace("%nome%", args[1]));
                            } else {
                                p.sendMessage(pl.getString("uso-correto-deletar"));
                            }
                            break;
                        case "setholograma":
                            if(args.length == 2){
                                if(!pl.getConfig().contains("minas."+args[1])){
                                    p.sendMessage(pl.getString("mina-nao-existe"));
                                    return false;
                                }
                                Location loc = p.getLocation();
                                String locString = loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getWorld().getName();
                                pl.getConfig().set("minas."+args[1]+".holograma", locString);
                                pl.saveConfig();
                            } else {
                                p.sendMessage(pl.getString("uso-correto-deletar"));
                            }
                            break;
                        case "criar":
                            if(args.length == 2){
                                if(pl.pos1.containsKey(p) && pl.pos2.containsKey(p)){
                                    if(pl.getConfig().contains("minas."+args[1])){
                                        p.sendMessage(pl.getString("mina-ja-existe"));
                                        return false;
                                    }
                                    pl.mina(args[1], pl.pos1.get(p), pl.pos2.get(p));
                                    p.sendMessage(pl.getString("mina-definida").replace("%nome%", args[1]));
                                } else {
                                    p.sendMessage(pl.getString("marque-uma-regiao"));
                                }
                            } else {
                                p.sendMessage(pl.getString("uso-correto-criar"));
                            }
                            break;
                        case "marcar":
                            ItemStack item = new ItemStack(Material.DIAMOND_AXE);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName("§bEDMina");
                            item.setItemMeta(meta);
                            p.getInventory().addItem(item);
                            p.sendMessage(pl.getString("marcar-regiao"));
                            break;
                        case "restaurar":
                            if(args.length == 2){
                                pl.minaRestaurar(args[1]);
                            } else {
                                p.sendMessage(pl.getString("uso-correto-restaurar"));
                            }
                            break;
                    }
                }
            } else {
                p.sendMessage(pl.getString("sem-permissao"));
            }
        }
        return false;
    }
}
