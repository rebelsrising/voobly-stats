package gg.rebelsrising.aom.voobly.stats.core.model

enum class Ladder(val mainUrl: String, val idUrl: String) {

    UNKNOWN("", ""),
    AOT_1X("Age-of-Mythology-The-Titans/1v1-Supremacy", "326"),
    AOT_TG("Age-of-Mythology-The-Titans/TG-Supremacy", "327"),
    AOT_DM("Age-of-Mythology-The-Titans/AoT-Deathmatch", "328");

    companion object {
        private val map = values().associateBy(Ladder::mainUrl)
        fun byUrl(url: String): Ladder = map.getOrDefault(url, UNKNOWN)
    }

}
