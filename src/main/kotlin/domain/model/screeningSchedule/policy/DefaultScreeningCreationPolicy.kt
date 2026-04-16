package domain.model.screeningschedule.policy

import domain.model.screeningschedule.Screening
import java.time.LocalDate

class DefaultScreeningCreationPolicy : ScreeningCreationPolicy {
    override fun validate(
        candidate: Screening,
        existing: List<Screening>,
        periodStart: LocalDate,
        periodEnd: LocalDate,
    ) {
        require(!candidate.screeningDate.isBefore(periodStart) && !candidate.screeningDate.isAfter(periodEnd)) {
            "상영 기간 밖의 날짜입니다."
        }

        val hasOverlapForSameMovie =
            existing
                .filter { screening ->
                    screening.isForMovie(candidate.movie.findMovieTitle())
                }.any { screening ->
                    screening.overlapsWith(candidate)
                }

        require(!hasOverlapForSameMovie) { "같은 영화의 상영 시간이 겹칩니다." }
    }
}
