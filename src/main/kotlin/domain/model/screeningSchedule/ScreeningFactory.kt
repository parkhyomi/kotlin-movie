package domain.model.screeningschedule

import domain.model.Movie
import java.time.LocalDate

class ScreeningFactory {
    fun withSamples(
        screeningPeriodStart: LocalDate,
        screeningPeriodEnd: LocalDate,
        movies: List<Movie>,
        samples: List<ScreeningTemplate>,
    ): ScreeningSchedule {
        val screeningSchedule =
            ScreeningSchedule(
                movies = movies,
                screeningPeriodStart = screeningPeriodStart,
                screeningPeriodEnd = screeningPeriodEnd,
            )

        samples.forEach { sample ->
            screeningSchedule.createScreening(
                movieTitle = sample.movieTitle,
                screeningDate = sample.screeningDate,
                startTime = sample.startTime,
            )
        }
        return screeningSchedule
    }
}
