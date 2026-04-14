package view

import view.ScreeningTemplate
import java.time.LocalDate
import java.time.LocalTime

fun defaultScreeningSeeds(): List<ScreeningTemplate> =
    // 상영 제목 , 상영 날짜, 상영 시작 시간 리스트들
    listOf(
        ScreeningTemplate(
            movieTitle = "탑건: 매버릭",
            screeningDate = LocalDate.of(2026, 4, 6),
            startTime = LocalTime.of(10, 0),
        ),
        ScreeningTemplate(
            movieTitle = "마더",
            screeningDate = LocalDate.of(2026, 4, 6),
            startTime = LocalTime.of(13, 0),
        ),
        ScreeningTemplate(
            movieTitle = "아이언맨 3",
            screeningDate = LocalDate.of(2026, 4, 6),
            startTime = LocalTime.of(16, 0),
        ),
        ScreeningTemplate(
            movieTitle = "아이언맨 3",
            screeningDate = LocalDate.of(2026, 4, 6),
            startTime = LocalTime.of(19, 0),
        ),
        ScreeningTemplate(
            movieTitle = "탑건: 매버릭",
            screeningDate = LocalDate.of(2026, 4, 7),
            startTime = LocalTime.of(10, 0),
        ),
        ScreeningTemplate(
            movieTitle = "스파이더맨: 노 웨이 홈",
            screeningDate = LocalDate.of(2026, 4, 7),
            startTime = LocalTime.of(13, 30),
        ),
        ScreeningTemplate(
            movieTitle = "탑건: 매버릭",
            screeningDate = LocalDate.of(2026, 4, 8),
            startTime = LocalTime.of(10, 0),
        ),
        ScreeningTemplate(
            movieTitle = "남은 인생 10년",
            screeningDate = LocalDate.of(2026, 4, 8),
            startTime = LocalTime.of(14, 0),
        ),
        ScreeningTemplate(
            movieTitle = "오늘 밤 이세상에서 사랑이 사라진다 해도",
            screeningDate = LocalDate.of(2026, 4, 9),
            startTime = LocalTime.of(12, 20),
        ),
        ScreeningTemplate(
            movieTitle = "아이언맨 3",
            screeningDate = LocalDate.of(2026, 4, 10),
            startTime = LocalTime.of(9, 50),
        ),
        ScreeningTemplate(
            movieTitle = "체인소맨",
            screeningDate = LocalDate.of(2026, 4, 10),
            startTime = LocalTime.of(16, 0),
        ),
        ScreeningTemplate(
            movieTitle = "호퍼스",
            screeningDate = LocalDate.of(2026, 4, 11),
            startTime = LocalTime.of(20, 10),
        ),
        ScreeningTemplate(
            movieTitle = "스파이더맨: 노 웨이 홈",
            screeningDate = LocalDate.of(2026, 4, 12),
            startTime = LocalTime.of(15, 40),
        ),
        ScreeningTemplate(
            movieTitle = "마더",
            screeningDate = LocalDate.of(2026, 4, 13),
            startTime = LocalTime.of(11, 0),
        ),
    )
