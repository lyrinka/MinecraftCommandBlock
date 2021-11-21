package weeaxe.plugins.commandblock.minecraft;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;

public class CommandBlockView implements Listener {

	public CommandBlockView(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.useInteractedBlock() == Result.DENY) return; 
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		Player player = event.getPlayer(); 
		if(player.isSneaking()) return; 
		if(!CommandBlockLogic.isUser(player)) return; 
		Block block = event.getClickedBlock(); 
		if(!CommandBlockLogic.isCommandBlock(block.getType())) return; 
		
		NbtCompound data = CommandBlockLogic.view(player, (CommandBlock)block.getState());
		if(data == null) return;
		
		data.put("id", block.getType().getKey().toString()); 
		data.put("x", block.getLocation().getBlockX()); 
		data.put("y", block.getLocation().getBlockY()); 
		data.put("z", block.getLocation().getBlockZ());
		
		PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);
		packet.getBlockPositionModifier().write(0, new BlockPosition(block.getLocation().toVector())); 
		packet.getIntegers().write(0, 2); 
		packet.getNbtModifier().write(0, data); 
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
}
