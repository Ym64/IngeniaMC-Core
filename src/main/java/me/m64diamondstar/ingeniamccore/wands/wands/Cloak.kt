package me.m64diamondstar.ingeniamccore.wands.wands

import io.papermc.paper.entity.TeleportFlag
import me.m64diamondstar.ingeniamccore.IngeniaMC
import me.m64diamondstar.ingeniamccore.general.player.IngeniaPlayer
import me.m64diamondstar.ingeniamccore.utils.messages.Colors
import me.m64diamondstar.ingeniamccore.wands.utils.Cooldowns
import me.m64diamondstar.ingeniamccore.wands.utils.Wand
import java.util.HashMap
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.cos
import kotlin.math.sin

class Cloak: Wand {
    private var c = 0
    private val armorInv: MutableMap<Player, Array<ItemStack?>> = HashMap()

    override fun getDisplayName(): String{
        return Colors.format("#d400ff&lC#ba05f9&ll#9f0af4&lo#850fee&la#6a14e8&lk #5019e2&lW#351edd&la#1b23d7&ln#0028d1&ld")
    }

    override fun getCustomModelData(): Int {
        return 10
    }

    override fun hasPermission(player: Player): Boolean {
        return player.hasPermission("ingeniawands.cloak")
    }

    override fun run(player: Player) {
        val loc = player.eyeLocation
        val nLoc = player.location
        val particles = 10
        val radius = 0.7f

        player.walkSpeed = 0.0f
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 20, 200))
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20, 1))
        loc.add(0.0, -1.33, 0.0)
        player.teleport(nLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS)
        armorInv[player] = player.inventory.armorContents

        val s = Bukkit.getScheduler().scheduleSyncRepeatingTask(
            IngeniaMC.plugin, {
                val x: Double
                val z: Double
                val angle: Double = 2 * Math.PI * c / particles
                x = cos(angle) * radius
                z = sin(angle) * radius
                loc.add(x, 0.1, z)
                player.world.spawnParticle(Particle.GLOW, loc, 10, 0.01, 0.01, 0.01, 0.0)
                loc.subtract(x, 0.0, z)
                player.teleport(nLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS)
                c++
            }, 0L, 1L
        )

        Bukkit.getScheduler().scheduleSyncDelayedTask(
            IngeniaMC.plugin, {
                Bukkit.getScheduler().cancelTask(s)
                player.world.spawnParticle(
                    Particle.LARGE_SMOKE,
                    player.location.add(0.0, 1.0, 0.0),
                    50,
                    0.3,
                    1.0,
                    0.3,
                    0.0
                )
                player.walkSpeed = 0.6f
                player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(120, 1))
                player.inventory.helmet = null
                player.inventory.chestplate = null
                player.inventory.leggings = null
                player.inventory.boots = null
            }, 20L
        )

        val s2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(
            IngeniaMC.plugin, { player.inventory.heldItemSlot = 8 }, 0L, 1L
        )

        object : BukkitRunnable() {

            var c = 0

            override fun run() {
                if(IngeniaPlayer(player).isInGame || c == 140){
                    player.world.spawnParticle(
                        Particle.LARGE_SMOKE,
                        player.location.add(0.0, 1.0, 0.0),
                        50,
                        0.3,
                        1.0,
                        0.3,
                        0.0
                    )
                    player.inventory.heldItemSlot = 5
                    player.inventory.armorContents = armorInv[player] ?: emptyArray<ItemStack>()
                    player.walkSpeed = 0.2f
                    Bukkit.getScheduler().cancelTask(s2)
                    this.cancel()
                    return
                }



                c++
            }
        }.runTaskTimer(IngeniaMC.plugin, 0L, 1L)

        Cooldowns.addPlayer(player, 6000L, 7000L, 9000L, 12000L)
    }
}