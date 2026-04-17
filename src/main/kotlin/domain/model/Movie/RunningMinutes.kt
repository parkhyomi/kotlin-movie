package domain.model.movie

data class RunningMinutes(
    val value: Long,
) {
    init {
        require(value > 0) { "영화 러닝타임은 0보다 커야 합니다." }
    }
}
