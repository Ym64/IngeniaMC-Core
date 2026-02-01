package me.m64diamondstar.ingeniamccore.games.splashbattle

import io.papermc.paper.entity.TeleportFlag
import me.m64diamondstar.ingeniamccore.IngeniaMC
import me.m64diamondstar.ingeniamccore.games.PhysicalGameType
import me.m64diamondstar.ingeniamccore.general.player.IngeniaPlayer
import me.m64diamondstar.ingeniamccore.utils.messages.Colors
import me.m64diamondstar.ingeniamccore.utils.messages.MessageType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File

object SplashBattleUtils {

    var players: ArrayList<Player> = ArrayList()
    var dead: ArrayList<Player> = ArrayList()
    var games: HashMap<Player, String> = HashMap()

    private var playerInventory: HashMap<Player, Array<ItemStack?>> = HashMap()
    private var playerGamemode: HashMap<Player, GameMode> = HashMap()
    private var playerLocation: HashMap<Player, Location> = HashMap()
    private var playerRunnables: HashMap<Player, Int> = HashMap()

    var playerCombat: HashMap<Player, Long> = HashMap()

    fun getSplashBattles(): ArrayList<File> {
        val file = File(IngeniaMC.plugin.dataFolder, "games/splashbattle")

        val files = ArrayList<File>()
        file.listFiles()?.forEach {
            if(!it.name.contains(".DS_Store"))
                files.add(it)
        }

        return files
    }

    private fun existsSplashBattle(name: File): Boolean{
        return File(IngeniaMC.plugin.dataFolder, "games/splashbattle/").listFiles()!!.contains(name)
    }

    fun existsSplashBattle(name: String): Boolean{
        return existsSplashBattle(
            File(IngeniaMC.plugin.dataFolder,
                if(name.contains(".yml")) "games/splashbattle/$name" else "games/splashbattle/$name.yml")
        )
    }

    fun getAllSplashBattles(): List<SplashBattle>{
        val list = ArrayList<SplashBattle>()
        File(IngeniaMC.plugin.dataFolder, "games/splashbattle/").listFiles()?.forEach {
            if(!it.name.contains(".DS_Store"))
                list.add(SplashBattle(it.name))
        }
        return list
    }

    fun join(player: Player, splashBattle: SplashBattle) {

        if(players.contains(player))
            return

        playerLocation[player] = player.location
        player.teleport(splashBattle.getRandomSpawnPoint(), TeleportFlag.EntityState.RETAIN_PASSENGERS)

        players.add(player)
        games[player] = splashBattle.name
        playerInventory[player] = player.inventory.contents
        playerGamemode[player] = player.gameMode

        IngeniaPlayer(player).game = PhysicalGameType.SPLASH_BATTLE
        IngeniaPlayer(player).allowDamage = true

        player.sendMessage(Colors.format(MessageType.PLAYER_UPDATE + "You joined a SplashBattles game!"))
        player.sendMessage(Colors.format(MessageType.PLAYER_UPDATE + "Soak your enemies with water!"))
        player.sendMessage(Colors.format(MessageType.PLAYER_UPDATE + "Use '/leave' to leave this game."))
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE
        player.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = 10.0
        giveItems(player)

        val runnable = Bukkit.getScheduler().scheduleSyncRepeatingTask(IngeniaMC.plugin, Runnable {

            if(player.inventory.getItem(6) != null && player.inventory.getItem(7) != null && player.inventory.getItem(8) != null &&
                player.inventory.getItem(6)!!.type == Material.BUCKET && player.inventory.getItem(7)!!.type == Material.BUCKET &&
                player.inventory.getItem(8)!!.type == Material.BUCKET){

                (player as Audience).sendActionBar(MiniMessage.miniMessage().deserialize("<#44c2c2>Out of water! Shift while looking at water to fill your buckets"))

            }

            if (player.inventory.getItem(0) == null || player.inventory.getItem(1) == null) {
                giveWaterBalloon(player)
                return@Runnable
            }
            val balloons = player.inventory.getItem(0)!!.amount + player.inventory.getItem(1)!!.amount
            if (balloons < 32) {
                giveWaterBalloon(player)
            }
        }, 40L, 40L)

        playerRunnables[player] = runnable

    }

    fun leave(player: Player){

        // Reset Inventory
        if(playerInventory.containsKey(player))
            player.inventory.contents = playerInventory[player]!!

        // Reset Gamemode
        if(playerGamemode.containsKey(player))
            player.gameMode = playerGamemode[player]!!

        // Reset Health
        player.getAttribute(Attribute.MAX_HEALTH)!!.baseValue = 20.0
        player.health = 20.0

        // Remove player from lists and maps
        players.remove(player)
        playerInventory.remove(player)
        playerGamemode.remove(player)
        IngeniaPlayer(player).game = null
        IngeniaPlayer(player).allowDamage = false

        Bukkit.getScheduler().cancelTask(playerRunnables[player]!!)

        player.teleport(playerLocation[player]!!, TeleportFlag.EntityState.RETAIN_PASSENGERS)

        player.sendMessage(Colors.format(MessageType.PLAYER_UPDATE + "You left the game."))

    }

    private fun giveItems(player: Player){
        player.inventory.setItem(0, SplashBattleItems.getWaterBalloonStack())
        player.inventory.setItem(1, SplashBattleItems.getWaterBalloonStack())

        player.inventory.setItem(3, SplashBattleItems.getWaterPistol())
        player.inventory.setItem(4, SplashBattleItems.getWaterGun())

        player.inventory.setItem(6, SplashBattleItems.getWaterAmmoEmpty())
        player.inventory.setItem(7, SplashBattleItems.getWaterAmmoEmpty())
        player.inventory.setItem(8, SplashBattleItems.getWaterAmmoEmpty())
    }

    private fun giveWaterBalloon(player: Player){
        player.inventory.addItem(SplashBattleItems.getWaterBalloon())
    }

    fun damagePlayer(player: Player, damager: Player?, damage: Int) {
        player.damage(damage.toDouble(), damager)
        val event = EntityDamageEvent(player, EntityDamageEvent.DamageCause.PROJECTILE, DamageSource.builder(DamageType.ARROW).build(), 2.0)
        player.lastDamageCause = event
    }

    private fun hasAmmoBuckets(player: Player): Boolean{
        if(player.inventory.getItem(6) == null || !player.inventory.getItem(6)!!.type.toString().contains("BUCKET")
            || !player.inventory.getItem(6)!!.hasItemMeta()) return false
        if(player.inventory.getItem(7) == null || !player.inventory.getItem(7)!!.type.toString().contains("BUCKET")
            || !player.inventory.getItem(7)!!.hasItemMeta()) return false
        if(player.inventory.getItem(8) == null || !player.inventory.getItem(8)!!.type.toString().contains("BUCKET")
            || !player.inventory.getItem(8)!!.hasItemMeta()) return false

        val namespacedKey = NamespacedKey(IngeniaMC.plugin, "ammo")

        val bucket1 = player.inventory.getItem(6)!!
        val bucket2 = player.inventory.getItem(7)!!
        val bucket3 = player.inventory.getItem(8)!!

        if(!bucket1.itemMeta!!.persistentDataContainer.has(namespacedKey, PersistentDataType.INTEGER) ||
            !bucket2.itemMeta!!.persistentDataContainer.has(namespacedKey, PersistentDataType.INTEGER) ||
            !bucket3.itemMeta!!.persistentDataContainer.has(namespacedKey, PersistentDataType.INTEGER)) return false

        return true
    }

    fun getAmmo(player: Player): Int{
        if(!hasAmmoBuckets(player)) return 0

        val namespacedKey = NamespacedKey(IngeniaMC.plugin, "ammo")

        val bucket1 = player.inventory.getItem(6)!!
        val bucket2 = player.inventory.getItem(7)!!
        val bucket3 = player.inventory.getItem(8)!!

        return bucket1.itemMeta!!.persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER)!! +
                bucket2.itemMeta!!.persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER)!! +
                bucket3.itemMeta!!.persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER)!!
    }

    fun addAmmo(player: Player, amount: Int){
        if(!hasAmmoBuckets(player) || getAmmo(player) >= 30) return

        val namespacedKey = NamespacedKey(IngeniaMC.plugin, "ammo")

        var bullets = 0

        for(i in 6..8){
            val bucket = player.inventory.getItem(i)!!

            // If bucket isn't filled, add one ammo and return the method
            while(bucket.itemMeta!!.persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER)!! < 10){

                player.inventory.setItem(i, SplashBattleItems.getWaterAmmoFilled(bucket.itemMeta!!.persistentDataContainer
                    .get(namespacedKey, PersistentDataType.INTEGER)!! + 1))
                bullets++

                if(bullets == amount)
                    return

            }

        }
    }

    fun removeAmmo(player: Player, amount: Int){
        if(!hasAmmoBuckets(player) || getAmmo(player) <= 0) return

        val namespacedKey = NamespacedKey(IngeniaMC.plugin, "ammo")

        var bullets = 0

        for(i in 8 downTo 6){
            var bucket = player.inventory.getItem(i)!!

            // If bucket isn't filled, add one ammo and return the method
            while(bucket.itemMeta!!.persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER)!! > 0){
                player.inventory.setItem(i, SplashBattleItems.getWaterAmmoFilled(bucket.itemMeta!!.persistentDataContainer
                    .get(namespacedKey, PersistentDataType.INTEGER)!! - 1))
                bucket = player.inventory.getItem(i)!!
                bullets++

                if(bullets == amount)
                    return
            }

        }
    }
}