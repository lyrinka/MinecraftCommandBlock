package weeaxe.plugins.commandblock.minecraft;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public class CommandBlockLogic {
	
	// When trying to place a command block, after all tests and checks this method will be called. 
	// If returned with a false, the procedure will be cancelled. 
	public static boolean check(Player player, Location location) {
		player.sendMessage("Approved your request of creating a command block");
		return true; 
	}
	
	// When placing a command block, this method will be called. 
	// Appropriate operations can be performed on the block. 
	// There's no way to cancel the event here, unless we set the block to air again. 
	public static void place(Player player, CommandBlock block) { // Create a command block at Block, if applicable. 
		block.setCommand("say some command executed: <empty>");
		block.setName("Delegate CommandBlock");
		player.sendMessage("Command block placed");
	}
	
	// When client queries for a command block, this method will be called. 
	// This method will return the actual NBT tag to be sent to the client. 
	// If returned null, nothing will be sent to the client. 
	public static NbtCompound view(Player player, CommandBlock block) { // Return a command block NBT compound, if applicable.
		String command = block.getCommand(); 
		if(!command.startsWith("say some command executed: ")) return null; 
		command = command.substring(27); 
		
		NbtCompound nbt = NbtFactory.ofCompound(""); 
		nbt.put("Command", command); 
		player.sendMessage("Command block data sent back");
		return nbt; 
	}
	
	// When client sets the command for a command block, this method will be called. 
	// The actual block object will also be included so appropriate operation could be made. 
	// If returned with a false, no update is performed. 
	public static boolean update(Player player, CommandBlock block, PacketContainer packet) { // Retrieve info from the packet then update the block, if applicable. 
		String command = packet.getStrings().read(0); 
		block.setCommand("say some command executed: " + command);
		player.sendMessage("Received your request of changing the command to: " + command); 
		return true; 
	}
	
	// When trying to destroy a command block, after all tests and checks this method will be called. 
	// If returned with a false, the procedure will be cancelled. 
	public static boolean destroy(Player player, CommandBlock commandBlock) { // Destroy the block, if applicable. 
		player.sendMessage("Approved your request of destroying a command block");
		return true; 
	}
	
	// Operators will be bypassed. Everything is vanilla behavior and the plugin does nothing. 
	public static boolean isOp(Player player) {
		if(player.getGameMode() != GameMode.CREATIVE) return false; 
//		if(player.hasPermission("commandblock.use.force")) return false; 
		return player.isOp() || player.hasPermission("minecraft.commandblock"); 
	}
	
	// Users are normal players that will normally be rejected by the client but the plugin applies a workaround to them. 
	// Non-users will be approved/rejected as per vanilla behavior mostly. 
	public static boolean isUser(Player player) {
		if(player.getGameMode() != GameMode.CREATIVE) return false;
		if(CommandBlockLogic.isOp(player)) return false; 
		if(player.hasPermission("commandblock.use")) return true; 
		//return false; 
		return true; 
	}
	
	// Command blocks are all blocks that can be casted to org.bukkit.block.CommandBlock. Do not add anything else here.
	public static boolean isCommandBlock(Material material) {
		return material == Material.COMMAND_BLOCK || material == Material.CHAIN_COMMAND_BLOCK || material == Material.REPEATING_COMMAND_BLOCK; 
	}
	
	// Game master blocks are the blocks that previously a client cannot interact, now can interact, and should be interacted instead of being placed on. 
	// For users, when trying to place on these blocks, unless the player is sneaking the place will be cancelled. 
	public static boolean isGameMasterBlock(Material material) {
		return material == Material.COMMAND_BLOCK || material == Material.CHAIN_COMMAND_BLOCK || material == Material.REPEATING_COMMAND_BLOCK || material == Material.STRUCTURE_BLOCK; 
	}
	
	public CommandBlockLogic(JavaPlugin plugin) {
		new CommandBlockPermInject(plugin); 
		new CommandBlockPlace(plugin); 
		new CommandBlockBreak(plugin); 
		new CommandBlockView(plugin); 
		new CommandBlockUpdate(plugin); 
	}

}
