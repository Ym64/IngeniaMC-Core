package me.m64diamondstar.ingeniamccore.general.commands

import com.ticxo.modelengine.api.ModelEngineAPI
import fr.mrmicky.fastboard.adventure.FastBoard
import gg.flyte.twilight.scheduler.delay
import io.papermc.paper.entity.TeleportFlag
import me.m64diamondstar.ingeniamccore.IngeniaMC
import me.m64diamondstar.ingeniamccore.games.wandclash.gui.TeamChooseGui
import me.m64diamondstar.ingeniamccore.games.wandclash.gui.VoteGameModeGui
import me.m64diamondstar.ingeniamccore.general.player.IngeniaPlayer
import me.m64diamondstar.ingeniamccore.npc.utils.DialogueUtils
import me.m64diamondstar.ingeniamccore.utils.entities.CameraPacketEntity
import me.m64diamondstar.ingeniamccore.utils.entities.NpcPlayerEntity
import me.m64diamondstar.ingeniamccore.utils.items.Items
import me.m64diamondstar.ingeniamccore.utils.messages.Colors
import me.m64diamondstar.ingeniamccore.utils.messages.MessageType
import me.m64diamondstar.ingeniamccore.utils.messages.Messages
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.SkullBlockEntity
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Rotatable
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.io.File
import java.io.IOException
import java.sql.DriverManager
import java.sql.SQLException


class AdminCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if(sender !is Player){
            sender.sendMessage(Messages.noPlayer())
            return false
        }

        if(args.isEmpty()){
            sender.sendMessage(Colors.format(MessageType.ERROR + "Please enter a valid sub-command."))
            return false
        }

        if(args[0].equals("speed", ignoreCase = true) && args.size == 2){
            try {
                if (sender.isFlying)
                    sender.flySpeed = args[1].toFloat() / 10
                else
                    sender.walkSpeed = args[1].toFloat() / 5

                sender.sendMessage(Colors.format(MessageType.SUCCESS + "Speed set to ${args[1]}."))
            }catch (_: NumberFormatException){
                sender.sendMessage(Messages.invalidNumber())
            }catch (_: IllegalArgumentException){
                sender.sendMessage(Colors.format(MessageType.ERROR + "This amount of speed doesn't exist."))
            }
        }

        if(args[0].equals("givehead", ignoreCase = true) && args.size == 2){
            val head = Items.getPlayerHead(args[1])
            sender.inventory.addItem(head)
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "You've been given the player-head!"))
        }

        if(args[0].equals("heal", ignoreCase = true) && args.size == 1){
            sender.health = 20.0
            sender.foodLevel = 20
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Fully healed."))
        }

        if(args[0].equals("fly", ignoreCase = true) && args.size == 1){
            sender.allowFlight = !sender.allowFlight
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Toggled flight."))
        }

        if (args[0].equals("undress", ignoreCase = true)) {
            if (sender.inventory.helmet != null) sender.inventory
                .addItem(sender.inventory.helmet!!)
            if (sender.inventory.chestplate != null) sender.inventory
                .addItem(sender.inventory.chestplate!!)
            if (sender.inventory.leggings != null) sender.inventory
                .addItem(sender.inventory.leggings!!)
            if (sender.inventory.boots != null) sender.inventory
                .addItem(sender.inventory.boots!!)
            sender.equipment.helmet = ItemStack(Material.AIR)
            sender.equipment.chestplate = ItemStack(Material.AIR)
            sender.equipment.leggings = ItemStack(Material.AIR)
            sender.equipment.boots = ItemStack(Material.AIR)
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Undressed!"))
        }

        if (args[0].equals("hat", ignoreCase = true) && args.size == 1) {
            val item: ItemStack = sender.inventory.itemInMainHand
            sender.equipment.helmet = item
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Changed hat!"))
        }

        if (args[0].equals("weatherclear", ignoreCase = true) && args.size == 1) {
            sender.world.weatherDuration = 0
            sender.world.setStorm(false)
            sender.world.isThundering = false
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Weather has been cleared!"))
        }

        if(args[0].equals("nightvision", ignoreCase = true)){
            val effect: PotionEffect? = sender.getPotionEffect(PotionEffectType.NIGHT_VISION)
            if (effect != null) {
                sender.removePotionEffect(PotionEffectType.NIGHT_VISION)
                sender.sendMessage(Colors.format(MessageType.SUCCESS + "SUPER EYES DISABLED."))
            } else {
                sender.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 99999999, 1, true))
                sender.sendMessage(Colors.format(MessageType.SUCCESS + "SUPER EYES ENABLED."))
            }
        }

        if(args[0].equals("back", ignoreCase = true)){
            val player = IngeniaPlayer(sender)
            if(player.previousLocation == null){
                sender.sendMessage(Colors.format(MessageType.ERROR + "You haven't even teleported yet..."))
                return false
            }
            sender.teleport(player.previousLocation!!)
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "* poof *"))
        }

        if(args[0].equals("day", ignoreCase = true)){
            sender.world.time = 6000
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "AAHHH MY EYESSSS, oh wait i don't have any..."))
        }

        if(args[0].equals("night", ignoreCase = true)){
            sender.world.time = 18000
            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Dark mode enabled!"))
        }

        if(args[0].equals("jort", ignoreCase = true)){
            sender.sendMessage(Colors.format(MessageType.ERROR + "CMON YOU CAN DO IT, DO NOT GIVE UP!"))
        }

        if(args[0].equals("spawnRandomPresent", ignoreCase = true)){
            val block = sender.location.block
            block.type = Material.PLAYER_HEAD

            val faces = BlockFace.values().toMutableList()
            faces.remove(BlockFace.DOWN)
            faces.remove(BlockFace.UP)
            faces.remove(BlockFace.SELF)

            val randomRotation = faces.shuffled().first()
            val blockData = (block.blockData as Rotatable)
            blockData.rotation = randomRotation

            block.blockData = blockData

            block.state.update()
        }

        if(args[0].equals("database", ignoreCase = true)){
            if(args.size == 2 && args[1].equals("connection-test", ignoreCase = true)){
                val url = IngeniaMC.plugin.config.getString("Database-Url")

                Bukkit.getScheduler().runTaskAsynchronously(IngeniaMC.plugin, Runnable {
                    try{
                        val connection = DriverManager.getConnection(url)
                        if(connection != null && !connection.isClosed){
                            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Successfully connected!"))
                            connection.close()
                        }else{
                            sender.sendMessage(Colors.format(MessageType.ERROR + "Couldn't connect to the database."))
                        }
                    }catch (_: SQLException){
                        sender.sendMessage(Colors.format(MessageType.ERROR + "Couldn't connect to the database."))
                    }
                })
            }
        }

        if(args[0].equals("invsee", ignoreCase = true)){
            if(args.size == 2){
                val player = Bukkit.getPlayerExact(args[1]) ?: return false
                sender.openInventory(player.inventory)
            }
        }

        if(args[0].equals("gravity", ignoreCase = true)){
            when (args.size) {
                2 -> {
                    val player = Bukkit.getPlayerExact(args[1]) ?: return false
                    sender.sendMessage(Colors.format(MessageType.SUCCESS + "Gravity of " + player.name + ": " + player.hasGravity()))
                }
                3 -> {
                    val value = args[2].lowercase().toBooleanStrictOrNull() ?: return false
                    val player = Bukkit.getPlayerExact(args[1]) ?: return false
                    player.setGravity(value)
                    sender.sendMessage(Colors.format(MessageType.SUCCESS + "Set gravity of " + player.name + " to " + value))
                }
                else -> {
                    sender.sendMessage(Messages.commandUsage("adm gravity <player> <true/false>"))
                }
            }
        }

        if(args[0].equals("playtime", ignoreCase = true)){
            if(args.size == 2){
                val player = Bukkit.getPlayerExact(args[1]) ?: return false
                sender.sendMessage(Colors.format(MessageType.SUCCESS + "Playtime of " + player.name + " in ticks: " + player.getStatistic(
                    Statistic.PLAY_ONE_MINUTE)))

                val ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE) // Name is misleading, it's actually playtime in ticks
                val totalSeconds: Int = ticks / 20
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                val playTimeFormatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

                sender.sendMessage(Colors.format(MessageType.SUCCESS + "Playtime of " + player.name + ": " + playTimeFormatted))
            }else{
                sender.sendMessage(Messages.commandUsage("adm playtime <player>"))
            }
        }

        if(args[0].equals("testmessage", ignoreCase = true)){
            if(args.size < 2){
                sender.sendMessage(Messages.commandUsage("adm testmessage <message/font/negative/bunny>"))
                return false
            }
            if(args[1].equals("font", ignoreCase = true)){
                (sender as Audience).sendMessage(Component.text("some text font").font(Key.key("ingeniamc:dialogue_line_1")))
            }else if(args[1].equals("negative", ignoreCase = true)){
                (sender as Audience).sendMessage(Component.text("some wierd \uF808 text"))
            }else if(args[1].equals("bunny", ignoreCase = true)){
                val textComponent2 = Component.text()
                    .content("You're a ")
                    .color(TextColor.color(0x443344))
                    .append(Component.text().content("Bunny").color(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text("! Press "))
                    .append(
                        Component.keybind().keybind("key.jump")
                            .color(NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.BOLD, true)
                            .build()
                    )
                    .append(Component.text(" to jump!"))
                    .build()
                (sender as Audience).sendMessage(textComponent2)
            }else{
                val stringBuilder = StringBuilder()
                for (element in args) {
                    stringBuilder.append(element).append(" ")
                }
                (sender as Audience).sendMessage(MiniMessage.miniMessage().deserialize(stringBuilder.toString()))
            }
        }

        if(args[0].equals("testboard", ignoreCase = true)){
            val board = FastBoard(sender)
            board.updateTitle(Messages.ingeniaMCComponent())
            board.updateLine(0, Component.empty())
            board.updateLine(1, Component.text("Very cool text yeah"))
        }

        if(args[0].equals("testnpc", ignoreCase = true)){
            val pig = sender.location.world.spawn(sender.location, Pig::class.java)
            pig.isSilent = true
            pig.setAI(false)
            pig.isCollidable = false
            pig.isPersistent = false
            pig.setGravity(false)
            pig.isInvisible = true

            val modeledEntity = ModelEngineAPI.createModeledEntity(pig)
            val activeModel = ModelEngineAPI.createActiveModel("female_wizard_with_hat")
            modeledEntity.addModel(activeModel, true)

        }

        if(args[0].equals("disableresourcepack", ignoreCase = true)){
            sender.clearResourcePacks()
        }

        if(args[0].equals("testdialogue", ignoreCase = true)){
            (sender as Audience).sendActionBar(
                DialogueUtils.getDialogueFormat(
                    "Hello there! Welcome to IngeniaMC",
                    "We're a custom theme park with a ton",
                    "of cool rides!",
                    "",
                    "We hope you enjoy your stay!",
                    null,
                    null,
                    "\uE001"
                )
            )
        }

        if(args[0].equals("testprogressivedialogue", ignoreCase = true)){
            DialogueUtils.sendProgressiveDialogue(
                sender,
                "Hello there! Welcome to",
                "IngeniaMC! We're a custom",
                "theme park with a ton of",
                "cool rides! We hope you",
                "enjoy your stay!",
                null,
                null,
                "\uE001"
            )
        }

        if(args[0].equals("testcinematic", ignoreCase = true)){
            val cameraPacketEntity = CameraPacketEntity(sender.world, sender.location, sender, sender.location)
            cameraPacketEntity.spawn()
            cameraPacketEntity.watch()
            object: BukkitRunnable(){
                var c = 0
                val location = sender.location
                override fun run() {
                    if(c == 100) {
                        cameraPacketEntity.despawn()
                        cancel()
                        return
                    }

                    location.add(0.0, 0.05, 0.0)
                    location.yaw += 1f
                    location.pitch += 1
                    cameraPacketEntity.setLocation(location)

                    c++
                }
            }.runTaskTimer(IngeniaMC.plugin, 0L, 1L)
        }

        if(args[0].equals("checkfontwidth", ignoreCase = true)){
            if(args.size != 2){
                sender.sendMessage(Messages.commandUsage("adm checkfontwidth <string>"))
                return false
            }
            try {
                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, File(IngeniaMC.plugin.dataFolder, "fonts/Minecraft.otf")).deriveFont(10F))
            }catch (e: IOException){
                e.printStackTrace()
            }

            val font = Font.createFont(Font.TRUETYPE_FONT, File(IngeniaMC.plugin.dataFolder, "fonts/Minecraft.otf")).deriveFont(10F)
            val frc = FontRenderContext(AffineTransform(), true, true)

            (sender as Audience).sendMessage(Component.text("${args[1]}: " + font.getStringBounds(args[1], frc).width))
        }

        if(args[0].equals("testbossbar", ignoreCase = true)){
            val bossBar = BossBar.bossBar(Component.text("|"), 0.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
            (sender as Audience).showBossBar(bossBar)
            delay(10, gg.flyte.twilight.time.TimeUnit.SECONDS) {
                (sender as Audience).hideBossBar(bossBar)
            }
        }

        if(args[0].equals("testnpcentity", ignoreCase = true)){
            val npcEntity = NpcPlayerEntity(sender.world, sender.location, sender)
            npcEntity.spawn(true)
        }

        if(args[0].equals("teststriptags", ignoreCase = true)){
            (sender as Audience).sendMessage(Component.text(MiniMessage.miniMessage().stripTags("<red>Hello <blue>World!")))
        }

        if(args[0].equals("testshield", ignoreCase = true)){
            val location = sender.location
            location.yaw = 0f
            location.pitch = 0f

            val itemStack = ItemStack(Material.LEATHER_HORSE_ARMOR)
            val meta = itemStack.itemMeta!! as LeatherArmorMeta
            meta.setColor(Color.fromRGB(120, 196, 255))
            meta.setCustomModelData(11)
            itemStack.itemMeta = meta

            for(i in -3..3){
                location.yaw = i.toFloat() * 14
                location.pitch = 0f

                for(i2 in -3..3) {
                    location.pitch = i2.toFloat() * 14
                    if(i * 14 % 28 == 0)
                        location.pitch += 7

                    val itemDisplay = sender.world.spawn(location, ItemDisplay::class.java)
                    itemDisplay.setItemStack(itemStack)
                    val transformation = itemDisplay.transformation
                    transformation.translation.add(0f, 0f, 1.5f)
                    transformation.scale.set(0.5f, 0.5f, 0.5f)
                    itemDisplay.transformation = transformation
                    sender.addPassenger(itemDisplay)
                }
            }
        }

        if(args[0].equals("testwcvote", ignoreCase = true)){
            val voteGameModeGui = VoteGameModeGui(sender)
            voteGameModeGui.open()
        }

        if(args[0].equals("testwcteam", ignoreCase = true)){
            val teamChooseGui = TeamChooseGui(sender)
            teamChooseGui.open()
        }

        if(args[0].equals("settitle", ignoreCase = true)){
            val ingeniaPlayer = IngeniaPlayer(sender)
            ingeniaPlayer.playerConfig.setPlayerTitle(args.drop(1).joinToString(" "))

            sender.sendMessage(Colors.format(MessageType.SUCCESS + "Title set!"))
        }

        return false
    }
}