package domain.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cinema.db")
data class CinemaDbProperties(
    val url: String? = null,
    val local: Boolean = true,
)
