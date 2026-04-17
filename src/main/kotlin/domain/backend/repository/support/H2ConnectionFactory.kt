package domain.backend.repository.support

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

object H2ConnectionFactory {
    // 요구사항의 파일 DB URL. 프로그램을 다시 실행해도 데이터가 유지된다.
    private const val LOCAL_FILE_URL =
        "jdbc:h2:file:./data/cinema;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE"

    // H2 2.4.x는 위 옵션 조합을 지원하지 않아, 호환 가능한 URL을 순차 시도한다.
    private const val LOCAL_FILE_FALLBACK_URL = "jdbc:h2:file:./data/cinema;AUTO_SERVER=TRUE"
    private const val LOCAL_FILE_SAFE_URL = "jdbc:h2:file:./data/cinema"
    private const val TEST_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    private const val USER = "sa"
    private const val PASSWORD = ""

    fun connection(isLocal: Boolean): Connection {
        if (isLocal) {
            Files.createDirectories(Path.of("data"))
            val candidates = listOf(LOCAL_FILE_URL, LOCAL_FILE_FALLBACK_URL, LOCAL_FILE_SAFE_URL)
            var lastError: Exception? = null

            candidates.forEach { url ->
                try {
                    return DriverManager.getConnection(url, USER, PASSWORD)
                } catch (error: Exception) {
                    lastError = error
                }
            }

            throw lastError ?: IllegalStateException("로컬 H2 데이터베이스 연결에 실패했습니다.")
        }
        return DriverManager.getConnection(TEST_URL, USER, PASSWORD)
    }

    fun connection(url: String): Connection = DriverManager.getConnection(url, USER, PASSWORD)
}
