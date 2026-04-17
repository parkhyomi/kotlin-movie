package domain.model.movie

data class MovieTitle(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "영화 제목은 비어 있을 수 없습니다." }
    }
}
