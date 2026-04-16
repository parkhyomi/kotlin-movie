package domain.model.screeningschedule.policy

import java.time.LocalDate

data class ScreenPeriod(
    val screeningPeriodStart: LocalDate = LocalDate.of(2026, 4, 6),
    val screeningPeriodEnd: LocalDate = LocalDate.of(2026, 4, 13),
) {
    init {
        require(!screeningPeriodEnd.isBefore(screeningPeriodStart)) { "상영 기간 종료일은 시작일보다 빠를 수 없습니다." }
    }
}
