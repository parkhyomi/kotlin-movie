package domain.model.screeningschedule.policy

import domain.model.screeningschedule.Screening

class DefaultScreeningCreationPolicy : ScreeningCreationPolicy {
    override fun validate(
        candidate: Screening,
        existing: List<Screening>,
        screenPeriod: ScreenPeriod,
    ) {
        require(
            !candidate.screeningDate.isBefore(screenPeriod.screeningPeriodStart) && !candidate.screeningDate.isAfter(
                screenPeriod.screeningPeriodEnd
            )
        ) {
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
