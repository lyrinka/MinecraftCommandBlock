package weeaxe.plugins.commandblock.minecraft;

import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandBlockBreak implements Listener {
	
	
	public CommandBlockBreak(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled()) return; 
		Player player = event.getPlayer(); 
		if(!CommandBlockLogic.isUser(player)) return; 
		Block block = event.getBlock(); 
		if(!CommandBlockLogic.isCommandBlock(block.getType())) return; 
		
		boolean result = CommandBlockLogic.destroy(player, (CommandBlock)block.getState()); 
		if(result) block.breakNaturally(); 
	}
	
}
