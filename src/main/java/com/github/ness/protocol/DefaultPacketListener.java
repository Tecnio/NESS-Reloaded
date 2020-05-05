package com.github.ness.protocol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.github.ness.check.BadPackets;
import com.github.ness.check.PingSpoof;
import com.github.ness.events.PacketInEvent;

public class DefaultPacketListener {

	public static boolean Executor(Player sender, PacketContainer packet, PacketType packetType) {
		if (sender == null || packet == null) {
			return true;
		}
		// i controlli prima di tutto
		// KillauraBotCheck.Check1(packet,sender);
		PacketInEvent event = new PacketInEvent(sender,packet,packetType);
		Bukkit.getPluginManager().callEvent(event);
		if (packetType == PacketType.Play.Client.FLYING) {
			new PingSpoof().Check(sender, packet);
		}else if(packetType == PacketType.Play.Client.POSITION) {
			new BadPackets().Check(sender,packet);
		}
		return true;
	}

}