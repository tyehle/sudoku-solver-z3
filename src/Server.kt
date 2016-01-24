import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * @author Tobin Yehle
 */

fun puzzleString(board: Board<Int?>): String =
        board.map { if(it == null) "_" else it.toString() }.toString()

fun main(args: Array<String>) {
    val server = HttpServer.create(InetSocketAddress(8080), 0)
    server.createContext("/board.txt", ::boardHandler)
    server.executor = null
    server.start()
}

fun boardHandler(exchange: HttpExchange) {
    val query = exchange.requestURI?.query ?: ""
    println("Query: $query")

    val qs = query.split('&').filter { it.length > 0 }.toMap { kv -> val parts = kv.split('='); Pair(parts[0], parts[1]) }

    // default to m = n = 2
    fun getInt(key: String): Int = try { qs[key]?.toInt() } catch(e: NumberFormatException) { null } ?: 2
    val m = getInt("m")
    val n = getInt("n")

    println("Building a ${m}x$n board")
    val puzzle = randomPuzzle(m, n)
    val solution = solution(puzzle, n, m)
    val board = "$m $n\n" + puzzleString(puzzle)
    println("\nserving:\n$board")
    println("with solution\n$solution")
    val response = board.toByteArray()
    exchange.sendResponseHeaders(200, response.size.toLong())
    val stream = exchange.responseBody
    stream.write(response)
    stream.close()
}
