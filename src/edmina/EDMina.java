package edmina;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

public class EDMina extends JavaPlugin {

    protected Map<Player, String> pos1 = new HashMap<Player, String>();
    protected Map<Player, String> pos2 = new HashMap<Player, String>();
    protected Map<String, HashMap<ItemStack, Double>> minas = new HashMap<>();
    protected Map<String, String> minasPos1 = new HashMap<>();
    protected Map<String, String> minasPos2 = new HashMap<>();
    private int tempoReset = 0;
    private int task = 0;
    private String titulo;
    private String subtitulo;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        getCommand("mina").setExecutor(new ComandoMina());
        getServer().getPluginManager().registerEvents(new Eventos(), this);
        tempoReset = getConfig().getInt("tempo-reset-mina");
        titulo = getString("mina-resetada-titulo");
        subtitulo = getString("mina-resetada-subtitulo");
        reloadMinas();
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("§6-----§f - §l" + this.getName() + " §f- §6-----");
        sender.sendMessage(" ");
        sender.sendMessage("§7Plugin §ahabilitado §7com sucesso");
        sender.sendMessage("§7Versao: §a" + this.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion());
        sender.sendMessage("§7Desenvolvedor: §aEduardoMGP");
        sender.sendMessage("§7Site: §ahttps://uaibits.com.br");
        sender.sendMessage(" ");
        sender.sendMessage("§6-----§f - §l" + this.getName() + " §f- §6-----");
    }

    public String getString(String path) {
        return getConfig().getString(path).replaceAll("&", "§");
    }

    public void reloadMinas() {
        Map<String, HashMap<ItemStack, Double>> minas = new HashMap<>();
        if (getConfig().contains("minas")) {
            for (String mina : getConfig().getConfigurationSection("minas").getKeys(false)) {
                HashMap<ItemStack, Double> m = new HashMap<>();
                ItemStack item;
                for (String i : getConfig().getStringList("minas." + mina + ".blocos")) {
                    String[] b = i.split(";");
                    item = new ItemStack(Integer.parseInt(b[0]), 1, (byte) Integer.parseInt(b[1]));
                    m.put(item, Double.parseDouble(b[2]));
                    minasPos1.put(mina, getConfig().getString("minas." + mina + ".pos1"));
                    minasPos2.put(mina, getConfig().getString("minas." + mina + ".pos2"));
                }
                minas.put(mina, m);
            }
            for (String mina : getConfig().getConfigurationSection("minas").getKeys(false)) {
                HashMap<ItemStack, Double> m = minas.get(mina)
                        .entrySet()
                        .stream()
                        .sorted(comparingByValue())
                        .collect(
                                toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                        LinkedHashMap::new));
                this.minas.put(mina, m);
            }
        }
        if(task != 0){
            Bukkit.getScheduler().cancelTask(task);
            task = 0;
        }
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (String m : minas.keySet()) {
                    minaRestaurar(m);
                }
            }
        }, 20 * tempoReset, 20 * tempoReset);
    }

    public void mina(String mina, String pos1, String pos2) {
        String[] pos = pos1.split(";");
        Location loc1 = new Location(Bukkit.getWorld(pos[3]), Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]));
        pos = pos2.split(";");
        Location loc2 = new Location(Bukkit.getWorld(pos[3]), Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]));
        int maxX, maxY, maxZ, minX, minY, minZ;
        if (loc1.getBlockX() > loc2.getBlockX()) {
            maxX = loc1.getBlockX();
            minX = loc2.getBlockX();
        } else {
            maxX = loc2.getBlockX();
            minX = loc1.getBlockX();
        }
        if (loc1.getBlockY() > loc2.getBlockY()) {
            maxY = loc1.getBlockY();
            minY = loc2.getBlockY();
        } else {
            maxY = loc2.getBlockY();
            minY = loc1.getBlockY();
        }
        if (loc1.getBlockZ() > loc2.getBlockZ()) {
            maxZ = loc1.getBlockZ();
            minZ = loc2.getBlockZ();
        } else {
            maxZ = loc2.getBlockZ();
            minZ = loc1.getBlockZ();
        }
        int count = 0;
        Map<String, Integer> blocos = new HashMap<String, Integer>();
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(Bukkit.getWorld(pos[3]), x, y, z);
                    count++;
                    Block b = loc.getBlock();
                    if (b.getType() != Material.AIR) {
                        String bloco = b.getType().getId() + ":" + b.getData();
                        if (blocos.containsKey(bloco)) {
                            blocos.replace(bloco, blocos.get(bloco) + 1);
                        } else {
                            blocos.put(bloco, 1);
                        }
                    }
                }
            }
        }
        List<String> blocosList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : blocos.entrySet()) {
            String b = entry.getKey();
            int quantidade = entry.getValue();
            DecimalFormat df = new DecimalFormat("#,###.00");
            double porcentagem = (double) (quantidade) / (double) (count) * 100;
            blocosList.add(b.replace(":", ";") + ";" + df.format(porcentagem));
        }

        getConfig().set("minas." + mina + ".pos1", pos1);
        getConfig().set("minas." + mina + ".pos2", pos2);
        getConfig().set("minas." + mina + ".blocos", blocosList);
        saveConfig();
        reloadMinas();
    }

    public double getMinaPorcentagem(String mina) {
        String[] pos = minasPos1.get(mina).split(";");
        Location loc1 = new Location(Bukkit.getWorld(pos[3]), Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]));
        pos = minasPos2.get(mina).split(";");
        Location loc2 = new Location(Bukkit.getWorld(pos[3]), Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]));
        int maxX, maxY, maxZ, minX, minY, minZ;
        if (loc1.getBlockX() > loc2.getBlockX()) {
            maxX = loc1.getBlockX();
            minX = loc2.getBlockX();
        } else {
            maxX = loc2.getBlockX();
            minX = loc1.getBlockX();
        }
        if (loc1.getBlockY() > loc2.getBlockY()) {
            maxY = loc1.getBlockY();
            minY = loc2.getBlockY();
        } else {
            maxY = loc2.getBlockY();
            minY = loc1.getBlockY();
        }
        if (loc1.getBlockZ() > loc2.getBlockZ()) {
            maxZ = loc1.getBlockZ();
            minZ = loc2.getBlockZ();
        } else {
            maxZ = loc2.getBlockZ();
            minZ = loc1.getBlockZ();
        }
        int countBlock = 0;
        int count = 0;
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(Bukkit.getWorld(pos[3]), x, y, z);
                    count++;
                    if (loc.getBlock().getType() != Material.AIR) {
                        countBlock++;
                    }
                }
            }
        }
        return countBlock / count * 100;
    }

    public void minaRestaurar(String mina) {
        String[] pos = minasPos1.get(mina).split(";");
        Location loc1 = new Location(Bukkit.getWorld(pos[3]), Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]));
        pos = minasPos2.get(mina).split(";");
        Location loc2 = new Location(Bukkit.getWorld(pos[3]), Double.valueOf(pos[0]), Double.valueOf(pos[1]), Double.valueOf(pos[2]));
        int maxX, maxY, maxZ, minX, minY, minZ;
        if (loc1.getBlockX() > loc2.getBlockX()) {
            maxX = loc1.getBlockX();
            minX = loc2.getBlockX();
        } else {
            maxX = loc2.getBlockX();
            minX = loc1.getBlockX();
        }
        if (loc1.getBlockY() > loc2.getBlockY()) {
            maxY = loc1.getBlockY();
            minY = loc2.getBlockY();
        } else {
            maxY = loc2.getBlockY();
            minY = loc1.getBlockY();
        }

        double min = 100;
        double max = 0;
        ItemStack item = null;
        if (loc1.getBlockZ() > loc2.getBlockZ()) {
            maxZ = loc1.getBlockZ();
            minZ = loc2.getBlockZ();
        } else {
            maxZ = loc2.getBlockZ();
            minZ = loc1.getBlockZ();
        }
        for (Map.Entry<ItemStack, Double> c : minas.get(mina).entrySet()) {
            if (c.getValue() < min) {
                min = c.getValue();
            }
            if (c.getValue() > max) {
                max = c.getValue();
            }
        }
        Random rand = new Random();
        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(Bukkit.getWorld(pos[3]), x, y, z);
                    if (loc.getBlock().getType() == Material.AIR) {
                        double r = rand.nextDouble() * (100 - 0) + 0;
                        if (r < min) {
                            r = min;
                        }
                        if (r > max) {
                            r = max;
                        }
                        for (Map.Entry<ItemStack, Double> c : minas.get(mina).entrySet()) {
                            if (r <= c.getValue()) {
                                item = c.getKey();
                                break;
                            }
                        }
                        loc.getBlock().setType(item.getType());
                        Location locParticula = loc;
                        locParticula.setY(locParticula.getY() + 1);
                        loc.getWorld().playEffect(locParticula, Effect.HEART, 0);
                        Entity[] e = loc.getChunk().getEntities();
                        for (Entity players : e) {
                            if (players.getLocation().distance(loc) < 2) {
                                loc = players.getLocation();
                                if(players instanceof Player){
                                    ((Player) players).sendTitle(titulo.replace("%nome%", mina), subtitulo.replace("%nome%", mina));
                                    ((Player) players).playSound(loc, Sound.LEVEL_UP, 50, 50);
                                }
                                if(loc.getBlockY() <= maxY){
                                    loc.setY(maxY + 1);
                                    players.teleport(loc);
                                }
                            }
                        }

                    }
                }
            }
        }

    }

}
