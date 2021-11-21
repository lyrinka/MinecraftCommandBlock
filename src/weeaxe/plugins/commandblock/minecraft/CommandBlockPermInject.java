package weeaxe.plugins.commandblock.minecraft;


import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class CommandBlockPermInject {

	public CommandBlockPermInject(JavaPlugin plugin) {
		PacketAdapter adapter = new PacketAdapter(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.ENTITY_STATUS) {
			
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket(); 
				int level = packet.getBytes().read(0); 
				if(level == 24 || level == 25) 
					packet.getBytes().write(0, (byte)26);
			}
			
		}; 
		ProtocolLibrary.getProtocolManager().addPacketListener(adapter); 
	}
	
}
