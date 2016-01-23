import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * @author Tobin Yehle
 */

fun puzzleString(board: Board<Int?>): String =
        board.map { if(it == null) "_" else it.toString() }.toString()

fun main(args: Array<String>) {
    val server = HttpServer.create(InetSocketAddress(6965), 0)
    server.createContext("/3x3-board.txt", ::handler3x3)
    server.executor = null
    server.start()
}

fun handler3x3(exchange: HttpExchange) {
    println("Building a 3x3 board")
    val board = "3 3\n" + puzzleString(randomPuzzle(3, 3))
    println("\nserving:\n$board")
    val response = board.toByteArray()
    exchange.sendResponseHeaders(200, response.size.toLong())
    val stream = exchange.responseBody
    stream.write(response)
    stream.close()
}
