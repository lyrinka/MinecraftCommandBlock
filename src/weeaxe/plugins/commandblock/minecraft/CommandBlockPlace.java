package weeaxe.plugins.commandblock.minecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Observer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

class PlayerInfo {
	
	public boolean isOffHanded; 
	public Material material; 
	public int tick; 
	
	public PlayerInfo(Material material, boolean isOffHanded) {
		this.material = material; 
		this.isOffHanded = isOffHanded; 
	}
	
}

public class CommandBlockPlace implements Listener {
	
	private final JavaPlugin plugin;  
	private final Map<UUID, PlayerInfo> storage; 
	
	public CommandBlockPlace(JavaPlugin plugin) {
		this.plugin = plugin;
		this.storage = new HashMap<>(); 
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		PacketAdapter adapter = new PacketAdapter(this.plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.USE_ITEM) {
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer(); 
				if(!CommandBlockLogic.isUser(player)) return; 
				
				Material mainHand = player.getInventory().getItemInMainHand().getType();  
				Material offHand = player.getInventory().getItemInOffHand().getType(); 
				
				if(mainHand == Material.AIR && offHand == Material.AIR) return; 
				
				boolean isOffHanded = false; 
				if(mainHand == Material.AIR) {
					isOffHanded = true; 
					mainHand = offHand; 
				}
				
				if(!CommandBlockLogic.isCommandBlock(mainHand)) return; 
				
				PlayerInfo info = new PlayerInfo(mainHand, isOffHanded);  
				storage.put(player.getUniqueId(), info); 
				
				ItemStack item = new ItemStack(Material.OBSERVER); 
				if(!isOffHanded) 
					player.getInventory().setItemInMainHand(item);
				else 
					player.getInventory().setItemInOffHand(item);
			}
			
		}; 
		ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
		Runnable task = new Runnable() {

			@Override
			public void run() {
				if(storage.isEmpty()) return; 
				
				storage.forEach(new BiConsumer<UUID, PlayerInfo>() {

					@Override
					public void accept(UUID id, PlayerInfo info) {
						Player player = plugin.getServer().getPlayer(id); 
						ItemStack item = new ItemStack(info.material); 
						if(!info.isOffHanded) 
							player.getInventory().setItemInMainHand(item); 
						else 
							player.getInventory().setItemInOffHand(item); 
					}
					
				});
				
				storage.clear(); 
			}
			
		}; 
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, task, 1, 1); 
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer(); 
		
		if(!CommandBlockLogic.isOp(player) && !player.isSneaking() && CommandBlockLogic.isGameMasterBlock(event.getBlockAgainst().getType())) 
			event.setCancelled(true); 
		
		PlayerInfo info = this.storage.remove(player.getUniqueId()); 
		if(info == null) return; 
		
		ItemStack item = new ItemStack(info.material); 
		if(!info.isOffHanded) 
			player.getInventory().setItemInMainHand(item); 
		else 
			player.getInventory().setItemInOffHand(item); 
		
		if(event.isCancelled()) return; 
		
		event.setCancelled(true); 
		Block block = event.getBlock(); 
		
		boolean result = CommandBlockLogic.check(player, block.getLocation()); 
		if(!result) return; 
		
		CommandBlock blockData = (CommandBlock)plugin.getServer().createBlockData(info.material); 
		blockData.setFacing(((Observer)block.getBlockData()).getFacing().getOppositeFace());
		
		this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable() {

			@Override
			public void run() {
				block.setType(info.material); 
				block.setBlockData(blockData); 
				org.bukkit.block.CommandBlock blockState = (org.bukkit.block.CommandBlock)block.getState(); 
				CommandBlockLogic.place(player, blockState); 
				blockState.update(); 
			}
			
		}); 
	}
	
}
