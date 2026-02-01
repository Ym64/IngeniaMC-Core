package me.m64diamondstar.ingeniamccore.utils.entities

import gg.flyte.twilight.extension.hidePlayer
import gg.flyte.twilight.extension.showPlayer
import gg.flyte.twilight.scheduler.sync
import io.papermc.paper.entity.TeleportFlag
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerEntity
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec3
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Chicken
import org.bukkit.entity.Player

class CameraPacketEntity(private val world: World, loc: Location, private val player: Player, private val originalLocation: Location) : Display.ItemDisplay(
    EntityType.ITEM_DISPLAY, (world as CraftWorld).handle) {

    private val craftPlayer = player as CraftPlayer
    private var defaultGamemode = player.gameMode
    private var isAlive = false
    private val chicken: Chicken

    init{
        this.entityData.set(Display.DATA_POS_ROT_INTERPOLATION_DURATION_ID, 20) // DO NOT REMOVE Display. OR IT WILL THROW ERRORS
        this.setPos(loc.x, loc.y, loc.z)
        this.moveOrInterpolateTo(Vec3(loc.x, loc.y, loc.z), loc.yaw, loc.pitch)
        chicken = world.spawnEntity(loc.clone().add(0.0, -2.0, 0.0), org.bukkit.entity.EntityType.CHICKEN) as Chicken
        EntityRegistry.addBukkitEntity(chicken)
        chicken.isInvisible = true
        chicken.isInvulnerable = true
        chicken.isSilent = true
        chicken.isPersistent = false
        chicken.setAI(false)
        chicken.eggLayTime = Int.MAX_VALUE
    }

    fun spawn(){
        val entityData = this.getEntityData().nonDefaultValues

        val serverEntity = ServerEntity((world as CraftWorld).handle.level, this, 0, false, null, emptySet())
        craftPlayer.handle.connection.send(ClientboundAddEntityPacket(this, serverEntity))
        if (entityData != null) {
            if(entityData.isNotEmpty())
                craftPlayer.handle.connection.send(ClientboundSetEntityDataPacket(this.id /*ID*/, entityData /*Data Watcher*/))
        }
    }

    fun watch(){
        defaultGamemode = player.gameMode
        craftPlayer.handle.connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, 3.0f))
        craftPlayer.handle.connection.send(ClientboundSetCameraPacket(this))
        isAlive = true
        player.eject()
        player.leaveVehicle()
        chicken.addPassenger(player)

        player.hidePlayer()
    }

    fun setLocation(location: Location){
        setLocation(location.x, location.y, location.z, location.yaw, location.pitch)
    }

    private fun setLocation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float){
        this.moveOrInterpolateTo(Vec3(x, y, z), yaw, pitch)
        craftPlayer.handle.connection.send(ClientboundTeleportEntityPacket(this, PositionMoveRotation()))
        sync {
            chicken.teleport(Location(world, x, y - 2, z, yaw, pitch))
        }
    }

    fun despawn(){
        craftPlayer.handle.connection.send(ClientboundRemoveEntitiesPacket(this.bukkitEntity.entityId))
        craftPlayer.handle.connection.send(ClientboundSetCameraPacket(craftPlayer.handle))
        craftPlayer.handle.connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE,
            when(defaultGamemode){
                GameMode.SURVIVAL -> 0.0f
                GameMode.CREATIVE -> 1.0f
                GameMode.ADVENTURE -> 2.0f
                GameMode.SPECTATOR -> 3.0f
            }
        ))
        isAlive = false

        sync {
            chicken.teleportAsync(originalLocation).thenAccept {
                chicken.remove()
                player.teleport(originalLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS)
                player.showPlayer()
            }
        }
    }

}