package weeaxe.plugins.commandblock;

import org.bukkit.plugin.java.JavaPlugin;

import weeaxe.plugins.commandblock.minecraft.CommandBlockLogic;

public class Run extends JavaPlugin {
	
	@Override
	public void onEnable() {
		new CommandBlockLogic(this); 
	}
	

	
}
