package gg.rebelsrising.aom.voobly.stats.core.model

class Player(
    val playerId: Int,
    val name: String,
    val teamTag: String,
    val teamUrl: String,
    val civ: Civ,
    val team: Byte,
    val newRating: Short,
    val delta: Byte,
)
