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
    println("Query: ${exchange.requestURI.query}")

    val qs = exchange.requestURI.query.split('&').toMap { kv -> val parts = kv.split('='); Pair(parts[0], parts[1]) }

    fun getInt(key: String): Int = try { qs[key]?.toInt() } catch(e: NumberFormatException) { null } ?: 3
    val m = getInt("m")
    val n = getInt("n")

    println("Building a ${m}x$n board")
    val board = "$m $n\n" + puzzleString(randomPuzzle(m, n))
    println("\nserving:\n$board")
    val response = board.toByteArray()
    exchange.sendResponseHeaders(200, response.size.toLong())
    val stream = exchange.responseBody
    stream.write(response)
    stream.close()
}
