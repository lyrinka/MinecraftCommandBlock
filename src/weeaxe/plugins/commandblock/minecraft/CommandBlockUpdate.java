package weeaxe.plugins.commandblock.minecraft;

import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

public class CommandBlockUpdate {
	
	public CommandBlockUpdate(JavaPlugin plugin) {
		PacketAdapter adapter = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.SET_COMMAND_BLOCK) {
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer(); 
				if(CommandBlockLogic.isOp(player)) return; 
				event.setCancelled(true); 
				if(!CommandBlockLogic.isUser(player)) return; 

				PacketContainer packet = event.getPacket(); 
				plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

					@Override
					public void run() {
						BlockPosition position = packet.getBlockPositionModifier().read(0); 
						Block block = player.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()); 
						if(!CommandBlockLogic.isCommandBlock(block.getType())) return; 
						
						CommandBlock blockState = (CommandBlock)block.getState(); 
						boolean result = CommandBlockLogic.update(player, blockState, packet); 
						if(result) blockState.update(); 
					}
					
				}); 
				
			}
			
		}; 
		ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
	}
	
}
