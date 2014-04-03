package me.thexbasic.RedFunExternal;
//java utils
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//bukkit utils
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/*********************
 * @author THExBASIC *
 *********************/

public class Main extends JavaPlugin
{
    protected String[] infos = { "Site 1 - Text 1", "Site 1 - Text 2", "Site 1 - Text 3", "Site 1 - Text 4", "Site 1 - Text 5", "Site 1 - Text 6", "Site 1 - Text 7", "Site 1 - Text 8", "Site 1 - Text 9" };
    
    protected Map<String,BukkitTask> tasks = new HashMap<>();
    protected Map<String,String> messages = new HashMap<>();
    protected Map<String,Boolean> onInteract = new HashMap<>();
    private final Listener dclistener = new DisconnectListener(this);
    //Config & Nachrichten laden
    protected String noPermission = "§7Dir fehlt die Berechtigung: §6<perm> §7für diesen Command!";
    protected String reloadP = "§7Die §8Confi §7von §8RedFun §7wurde neu geladen.";
    protected String reloadC = "-> RedFun: Confi reloaded";
    
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(dclistener, this);
        System.out.println("---------------------------------------\n RedFunExternal (version " + this.getDescription().getVersion() +  ") enabled.\n---------------------------------------");
    }
    
    @Override
    public void onDisable()
    {
        System.out.println("----------------------------------------\n RedFunExternal (version " + this.getDescription().getVersion() +  ") disabled.\n---------------------------------------");
    }
    
    // USED TO CHECK THE PLAYERS PERMISSIONS FOR THE SPECIFIC COMMAND
    public boolean hasPermission(Player p, String cmd, int type)
    {
        boolean allowed = false;
        String perm = "redfun." + cmd;
        if(type >= 0)
        {
            perm += "." + type;
        }
        allowed = p.hasPermission(perm);
        if(!allowed)
        {
            p.sendMessage(noPermission.replace("<perm>", perm));
        }
        return allowed;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLable, final String[] args)
    {
    // INITIATE PLAYER
        Player p = null;
        if(sender instanceof Player) { p = (Player) sender; }
        
    // INFO
        if(cmd.getName().equalsIgnoreCase("redfun"))
        {
            int site = 0;
            try
            {    
                if(args.length == 1)
                {
                    site = Integer.parseInt(args[0]) * 9;
                }
            }
            catch (NumberFormatException ex) { return true; }
            sender.sendMessage("--- RedFunExternal Version " + this.getDescription().getVersion() +  " ---");
            for(int i = site; i < site + 9; i++)
            {
                sender.sendMessage(infos[i]);
            }
        }
        
    // SPAM
        if(cmd.getName().equalsIgnoreCase("spam"))
        {
            if(!hasPermission(p, cmd.getName(), -1))
            {
                return true;
            }
            if(args.length <= 2)
            {
                return true;
            }
            Player opfer = getServer().getPlayer(args[0]);
            if(opfer == null || !opfer.isOnline())
            {
                return true;
            }
            int intervall;
            try
            {
                intervall = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex)
            {
                return true;
            }
            String msg = getMsg(args);
            if(!(sender instanceof Player))
            {
                msg = msg.replaceAll("&", "§");
            }
            messages.put(opfer.getName(), msg);
            addPlayerSpam(opfer.getName(), intervall);
            sender.sendMessage(args[0] + " wird jetzt zugespammt.");
// REMOTESAY
        }
        else if(cmd.getName().equalsIgnoreCase("remotesay"))
        {
            if(sender instanceof Player)
            {
                if(!hasPermission(p, cmd.getName(), -1))
                {
                    return true;
                }
            }
            if(args.length > 0)
            {
                if(getServer().getPlayer(args[0]).isOnline())
                {
                    final Player target = (Player) getServer().getPlayer(args[0]);
                    Player[] online = getServer().getOnlinePlayers();
                    final Set<Player> players = new HashSet<>();
                    players.addAll(Arrays.asList(online));
                    getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, target, getMsg(args), players);
                            getServer().getPluginManager().callEvent(event);
                            if (!(event.isCancelled()))
                            {
                                getServer().broadcastMessage(String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage()));
                            }
                            else
                            {
                                return;
                            }
                        }
                    });
                    return true;
                }
                else
                {
                    sender.sendMessage("Spieler offline oder nicht gefunden.");
                }
            }
            else
            {
                sender.sendMessage("/remotesay <player> <text>");
            }
// RANDOMTP
        } else if(cmd.getName().equalsIgnoreCase("randomtp")) {
            if(sender instanceof Player) {
                if(!sender.hasPermission("redfun.randomtp")) {
                    sender.sendMessage("§7Du hast §6keine Berechtigung §7dafür!");
                    return false;
                }
            }
            if(args.length == 2){
                if(args[0].equalsIgnoreCase("onaction")) {
                    onInteract.put(args[1], true);
                    sender.sendMessage(args[1] + "Wird nun zufällig Teleportiert. (A)");
                    return true;
                }
                return true;
            } else if(args.length == 3) {
                if(getServer().getPlayer(args[1]).isOnline()) {
                    if(args[0].equalsIgnoreCase("ontimer")) {
                        addPlayerTp(args[1], Integer.parseInt(args[2]));
                        sender.sendMessage(args[1] + "Wird nun zufällig Teleportiert. (T)");
                        return true;
                    }
                } else {
                    sender.sendMessage("Spieler offline oder nicht gefunden.");
                    return true;
                }
            } else {
                sender.sendMessage("/randomtp [mode] <player>");
                sender.sendMessage("Available mode's: onaction, ontimer");
            }
//FIXLOCATION
        } else if(cmd.getName().equalsIgnoreCase("fix")) {
            if(sender instanceof Player) {
                //if(!sender.hasPermission("redfun.fix")) {
                //    sender.sendMessage("§7Du hast §6keine Berechtigung §7dafür!");
                //    return false;
                //}
                Player pfix = (Player) sender;
                fixLocation(pfix);
                pfix.sendMessage(ChatColor.GOLD + "Woosh!");
                return true;
            } else {
                System.out.println("Dieser Command ist nur fuer Spieler.");
            }
 // NOFUN           
        } else if(cmd.getName().equalsIgnoreCase("nofun")) {
            if(sender instanceof Player) {
                if(!sender.hasPermission("redfun.nofun")) {
                    sender.sendMessage("§7Du hast §6keine Berechtigung §7dafür!");
                    return false;
                }
            }
            if(args.length == 1) {
                if(getServer().getPlayer(args[0]).isOnline()) {
                    if(tasks.containsKey(args[0] + "_spam")) {
                        messages.remove(args[0]);
                        tasks.get(args[0] + "_spam").cancel();
                        tasks.remove(args[0] + "_spam");
                        sender.sendMessage("Spam für den Spieler beendet.");
                    }
                    if(tasks.containsKey(args[0] + "_tp")) {
                        tasks.get(args[0] + "_tp").cancel();
                        tasks.remove(args[0] + "_tp");
                        sender.sendMessage("RandomTp für den Spieler beendet. (T)");
                    }
                    if(onInteract.containsKey(args[0])){
                        onInteract.remove(args[0]);
                        sender.sendMessage("RandomTp für den Spieler beendet. (A)");
                    }
                    //weitere Tasks zum beenden
                    return true;
                } else {
                    sender.sendMessage("Spieler offline oder nicht gefunden.");
                }
            } else {
                sender.sendMessage("/nofun <player>");
            }
        }
        return false;
    }


    protected void tpPlayer(Player p){
        Location newloc = p.getLocation().clone();
        int rndX = (int) (Math.random()*3);
        switch(rndX) {
            case 0: newloc.setX(newloc.getX() + (3 + Math.random() * 10)); break;
            case 1: newloc.setX(newloc.getX() - (3 + Math.random() * 10)); break;
        }
        int rndZ = (int) (Math.random()*3);
        switch(rndZ) {
            case 0: newloc.setZ(newloc.getZ() + (3 + Math.random() * 10)); break;
            case 1: newloc.setZ(newloc.getZ() - (3 + Math.random() * 10)); break;
        }        
        if (!((Entity) p).isOnGround()) {
            int rndY = (int) (Math.random()*3);
            switch(rndY) {
                case 0: newloc.setY(newloc.getY() + (3 + Math.random() * 10)); break;
                case 1: newloc.setY(newloc.getY() - (3 + Math.random() * 10)); break;
            }
        }
        p.teleport(newloc);
        fixLocation(p);
    }
    
    private void fixLocation(Player p){
        Location loc = p.getLocation().clone();
        Location newloc = loc;
        boolean tp = false;
        if(!(safelocation(p.getWorld(), loc))) {
            int max = 0;
            while(true) {
                for(int i = loc.getBlockY(); i < 256; i++) {
                    newloc.setY(i);
                    if(safelocation(p.getWorld(), newloc.clone())) { tp = true; break; }
                    else if(safelocation(p.getWorld(), setX(newloc.clone(), +1))) { tp = true; newloc.setX(newloc.getBlockX() +1); break; } 
                    else if(safelocation(p.getWorld(), setX(newloc.clone(), -1))) { tp = true; newloc.setX(newloc.getBlockX() -1); break; } 
                    else if(safelocation(p.getWorld(), setZ(newloc.clone(), +1))) { tp = true; newloc.setZ(newloc.getBlockZ() +1); break; } 
                    else if(safelocation(p.getWorld(), setZ(newloc.clone(), -1))) { tp = true; newloc.setZ(newloc.getBlockZ() -1); break; }
                }
                for(int i = loc.getBlockY(); i > 0; i--) {
                    newloc.setY(i);
                    if(safelocation(p.getWorld(), newloc.clone())) { tp = true; break; }
                    else if(safelocation(p.getWorld(), setX(newloc.clone(), +1))) { tp = true; newloc.setX(newloc.getBlockX() +1); break; } 
                    else if(safelocation(p.getWorld(), setX(newloc.clone(), -1))) { tp = true; newloc.setX(newloc.getBlockX() -1); break; } 
                    else if(safelocation(p.getWorld(), setZ(newloc.clone(), +1))) { tp = true; newloc.setZ(newloc.getBlockZ() +1); break; } 
                    else if(safelocation(p.getWorld(), setZ(newloc.clone(), -1))) { tp = true; newloc.setZ(newloc.getBlockZ() -1); break; }
                }
                if(tp) {
                    newloc.setX(newloc.getBlockX() + 0.5);
                    newloc.setZ(newloc.getBlockZ() + 0.5);
                    p.teleport(newloc);
                    break;
                }
                if(max < 20) {
                    newloc.setX(newloc.getBlockX() +1);
                    newloc.setZ(newloc.getBlockZ() +1);
                    max ++;
                } else {
                    break;
                }
            }
        }
    }
    
    public static Location setX(Location loc, int diff){ loc.setX(loc.getX() + diff); return loc; }
    public static Location setZ(Location loc, int diff){ loc.setZ(loc.getZ() + diff); return loc; }
    
    // TODO
    @SuppressWarnings("deprecation")
    private boolean safelocation(World wrld, Location loc) {
        if(loc.getBlockY() > 257 || loc.getBlockY() < 1) {
            return false;
        }
        int b2 = (int) (wrld.getBlockTypeIdAt(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ()));
        int b1 = (int) (wrld.getBlockTypeIdAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        int b0 = (int) (wrld.getBlockTypeIdAt(loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ()));
        if(notDangerous(b2) && notDangerous(b1) && isSolid(b0)) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isSolid(int blockid) {
        String ids = (String) (",1,2,3,4,5,7,12,13,14,15,16,17,18,19,20,21,22,23,24,25,29,33,35,41,42,43,44,45,46,47,48,49,52,53,54,56,57,58,60,61,62,67,73,74,79,80,82,84,86,87,88,87,91,92,97,98,99,100,101,102,103,108,109,110,111,112,114,116,117,118,120,121,123,124,125,126,128,129,130,133,134,135,136,137,138,144,145,146,152,153,154,155,158,159,170,172,173,");
        String bid = blockid + "";
        if(ids.contains("," + bid + ",")) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean notDangerous(int blockid) {
        String ids = (String) (",0,6,8,9,31,32,37,38,39,40,50,55,59,63,68,69,70,71,72,75,76,77,78,83,93,94,96,104,105,106,111,115,131,132,147,148,149,150,171,");
        String bid = blockid + "";
        if(ids.contains("," + bid + ",") ) {
            return true;
        } else {
            return false;
        }
    }
    
    private String getMsg(String[] args){
        String h = args[1];
        for(int i = 2; i < args.length; i++){
            h += " " + args[i];
        }
        return h;
    }
    //creates a new cb task and spams the player ontick.
    private void addPlayerSpam(final String player, int intervall){
        if(tasks.containsKey(player + "_spam")){
            tasks.get(player + "_spam").cancel();
        }
        BukkitTask task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

            @Override
            public void run() {
                Player p = getServer().getPlayer(player);
                if(p == null || !p.isOnline()){
                    messages.remove(player);
                    tasks.get(player + "_spam").cancel();
                    tasks.remove(player + "_spam");
                    
                } else{
                    p.sendMessage(messages.get(player));
                }
            }
        }, 0, intervall);
        tasks.put(player + "_spam", task);
    }
    
    //creates a new cb task and teleport the player in a random direction.
    private void addPlayerTp(final String player, int intervall){
        if(tasks.containsKey(player + "_tp")){
            tasks.get(player + "_tp").cancel();
        }
        BukkitTask task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

            @Override
            public void run() {
                Player p = getServer().getPlayer(player);
                if(p == null || !p.isOnline()){
                    tasks.get(player + "_tp").cancel();
                    tasks.remove(player + "_tp");
                    
                } else{
                    tpPlayer(p);
                }
            }
        }, 0, intervall);
        tasks.put(player + "_tp", task);
    }
}
