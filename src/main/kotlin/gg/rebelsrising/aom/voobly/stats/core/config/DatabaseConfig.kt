package gg.rebelsrising.aom.voobly.stats.core.config

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val user: String,
    val pass: String,
    val url: String,
    val port: Short,
    val database: String
)
