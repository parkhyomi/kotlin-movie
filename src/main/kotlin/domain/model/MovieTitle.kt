package domain.model

data class MovieTitle(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "영화 제목은 비어 있을 수 없습니다." }
    }

    override fun toString(): String = value

    companion object {
        fun from(raw: String): MovieTitle = MovieTitle(raw.trim())
    }
}
