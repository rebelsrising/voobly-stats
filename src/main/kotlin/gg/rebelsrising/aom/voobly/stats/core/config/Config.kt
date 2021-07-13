package gg.rebelsrising.aom.voobly.stats.core.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Config(val voobly: VooblyConfig, val database: DatabaseConfig) {

    companion object {
        const val DEFAULT_CONFIG_FILE = "config.json"

        fun load(config: String): Config {
            val jsonString = File(config).readText()

            return Json.decodeFromString(serializer(), jsonString)
        }
    }

}
