package com.mineinabyss.geary.minecraft.systems

import com.mineinabyss.geary.ecs.api.entities.with
import com.mineinabyss.geary.ecs.api.systems.TickingSystem
import com.mineinabyss.geary.ecs.components.CooldownManager
import com.mineinabyss.geary.ecs.engine.iteration.QueryResult
import com.mineinabyss.geary.ecs.entities.parent
import com.mineinabyss.looty.ecs.components.inventory.SlotType
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

private const val INTERVAL = 1L

@OptIn(ExperimentalTime::class)
object CooldownDisplaySystem : TickingSystem(interval = INTERVAL) {
    init {
        has<SlotType.Held>()
    }

    private val QueryResult.cooldownManager by get<CooldownManager>()

    private const val displayLength = 10
    private const val displayChar = '■'

    override fun QueryResult.tick() {
        entity.parent?.with { player: Player ->
            player.sendActionBar(cooldownManager.incompleteCooldowns.entries.joinToString("\n") { (key, cooldown) ->
                val length = Duration.milliseconds(cooldown.length)
                val timeLeft = Duration.milliseconds((cooldown.endTime - System.currentTimeMillis()))
                val squaresLeft =
                    if (timeLeft.toDouble(DurationUnit.SECONDS) * 20 < INTERVAL) 0 else (timeLeft / length * displayLength).roundToInt()

                buildString {
                    append("$key ")
                    append(ChatColor.GREEN)
                    repeat(displayLength - squaresLeft) {
                        append(displayChar)
                    }
                    append(ChatColor.RED)
                    repeat(squaresLeft) {
                        append(displayChar)
                    }
                    if (timeLeft.toDouble(DurationUnit.MILLISECONDS) < INTERVAL * 1000 / 20) append(
                        ChatColor.GREEN,
                        " [✔]"
                    ) else append(ChatColor.GRAY, " [$timeLeft]")
                }
            })
        }
    }
}
