package rat.poison.scripts

import com.badlogic.gdx.graphics.Color
import org.jire.arrowhead.keyPressed
import rat.poison.curSettings
import rat.poison.game.entity.dead
import rat.poison.game.entity.weapon
import rat.poison.game.me
import rat.poison.game.worldToScreen
import rat.poison.overlay.App
import rat.poison.settings.MENUTOG
import rat.poison.utils.Vector
import rat.poison.utils.generalUtil.strToBool
import rat.poison.utils.notInGame

fun getRangeRecords(entID: Int, minIDX: Int = 0, maxIDX: Int = 13): Array<Int> {
    var youngestSimtime = Float.MAX_VALUE
    var oldestSimtime = 0F
    val minMaxIDX = arrayOf(Int.MAX_VALUE, -1)

    for (i in minIDX until maxIDX) {
        val record = btRecords[entID][i]

        if (isValidTick(timeToTicks(record.simtime))) {
            if (record.simtime > oldestSimtime) {
                oldestSimtime = record.simtime
                minMaxIDX[1] = i
            }

            if (record.simtime < youngestSimtime) {
                youngestSimtime = record.simtime
                minMaxIDX[0] = i
            }
        }
    }

    return minMaxIDX
}

fun getValidRecords(entID: Int): List<Int> {
    val recordsList = mutableListOf<Int>()

    for (i in 0 until 13) {
        if (isValidTick(timeToTicks(btRecords[entID][i].simtime))) {
            recordsList.add(i)
        }
    }

    return recordsList
}

fun drawBacktrack() = App {
    if (MENUTOG) return@App
    if (me.dead()) return@App
    if (notInGame || !curSettings["BACKTRACK_VISUALIZE"].strToBool() || !curSettings["ENABLE_ESP"].strToBool() || !curSettings["ENABLE_BACKTRACK"].strToBool()) return@App

    val backtrackOnKey = curSettings["ENABLE_BACKTRACK_ON_KEY"].strToBool()
    val backtrackKeyPressed = keyPressed(curSettings["BACKTRACK_KEY"].toInt())

    if (backtrackOnKey && !backtrackKeyPressed) return@App

    val meWep = me.weapon()

    if (!meWep.gun) return@App

    for (i in 0 until 63) {
        val minMaxIDX = getRangeRecords(i)

        if (minMaxIDX[0] == Int.MAX_VALUE || minMaxIDX[1] == -1) continue

        val minRecord = btRecords[i][minMaxIDX[0]]
        val maxRecord = btRecords[i][minMaxIDX[1]]

        val minHeadPos = Vector()
        val maxHeadPos = Vector()
        val minAbsPos = Vector()
        val maxAbsPos = Vector()

        if (worldToScreen(minRecord.headPos, minHeadPos) && worldToScreen(minRecord.absPos, minAbsPos) && worldToScreen(maxRecord.headPos, maxHeadPos) && worldToScreen(maxRecord.absPos, maxAbsPos)) {
            val w = (minAbsPos.y - minHeadPos.y) / 4F
            val minMidX = (minAbsPos.x + minHeadPos.x) / 2F
            val maxMidX = (maxAbsPos.x + maxAbsPos.x) / 2F

            var sign = -1

            if (minMidX > maxMidX) {
                sign = 1
            }

            val topLeft = Vector(minHeadPos.x - (w / 1.5F) * sign, minHeadPos.y, minHeadPos.z)
            val topRight = Vector(maxHeadPos.x + (w / 1.5F) * sign, maxHeadPos.y, maxHeadPos.z)

            val bottomLeft = Vector(minMidX - w * sign, minAbsPos.y, minAbsPos.z)
            val bottomRight = Vector(maxMidX + w * sign, maxAbsPos.y, maxAbsPos.z)

            shapeRenderer.apply {
                if (shapeRenderer.isDrawing) {
                    end()
                }

                begin()

                color = Color(1F, 1F, 1F, 1F)

                line(topLeft.x, topLeft.y, topRight.x, topRight.y)
                line(topRight.x, topRight.y, bottomRight.x, bottomRight.y)
                line(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y)
                line(bottomLeft.x, bottomLeft.y, topLeft.x, topLeft.y)

                color = Color.WHITE

                end()
            }

            for (j in btRecords[i]) {
                j.alpha -= .5F
            }
        }
    }
}