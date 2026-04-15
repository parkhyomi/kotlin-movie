package domain.model.payment

data class Point(
    val value: Int,
) {
    init {
        require(value >= 0) { "포인트는 0 이상이어야 합니다." }
    }

    companion object {
        fun from(value: Int): Point = Point(value)
    }
}
