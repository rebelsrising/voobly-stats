package gg.rebelsrising.aom.voobly.stats.core.model

enum class Civ(val id: Int) {

    UNKNOWN(-1),

    // As defined by Voobly.
    ZEUS(0),
    POSEIDON(1),
    HADES(2),
    ISIS(3),
    RA(4),
    SET(5),
    ODIN(6),
    THOR(7),
    LOKI(8),
    KRONOS(9),
    ORANOS(10),
    GAIA(11);

    companion object {
        private val map = values().associateBy(Civ::id)
        fun byId(id: Int): Civ = map.getOrDefault(id, UNKNOWN)
    }

}
