package me.m64diamondstar.ingeniamccore.protect.listeners

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent

class BoatListener: Listener {

    @EventHandler
    fun onBoatCollide(event: EntityChangeBlockEvent){
        if(!event.entityType.name.contains("BOAT")) return
        if(event.block.type != Material.LILY_PAD) return

        event.isCancelled = true
    }

}