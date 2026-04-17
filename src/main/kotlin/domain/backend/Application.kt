package domain.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["domain.backend", "global", "domain"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
