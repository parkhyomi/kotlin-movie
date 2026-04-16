package infra.db

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

object H2ConnectionFactory {
    // 요구사항의 파일 DB URL. 프로그램을 다시 실행해도 데이터가 유지된다.
    private const val LOCAL_FILE_URL =
        "jdbc:h2:file:./data/cinema;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE"
    private const val TEST_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    private const val USER = "sa"
    private const val PASSWORD = ""

    fun connection(isLocal: Boolean): Connection {
        if (isLocal) {
            Files.createDirectories(Path.of("data"))
            return DriverManager.getConnection(LOCAL_FILE_URL, USER, PASSWORD)
        }
        return DriverManager.getConnection(TEST_URL, USER, PASSWORD)
    }

    fun connection(url: String): Connection = DriverManager.getConnection(url, USER, PASSWORD)
}
