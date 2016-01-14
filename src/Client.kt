import java.net.URL

/**
 * @author Tobin Yehle
 */

fun <T> List<T>.group(n: Int): List<List<T>> = if(this.isEmpty()) listOf<List<T>>() else listOf(this.take(n)) + this.drop(n).group(n)

fun toNumberList(raw: String): List<Int?> = raw.split("\\s+".toRegex()).filter { it.isNotEmpty() }.map { when(it) {
    "_" -> null
    else -> it.toInt()
} }

fun makeBoard(raw: String):Board<Int?> {
    val numbersMaybe = toNumberList(raw)

    val m = numbersMaybe[0]
    val n = numbersMaybe[1]
    val boardNumbers = numbersMaybe.drop(2)

    val numRows = Math.sqrt(boardNumbers.size.toDouble()).toInt()

    return Board(boardNumbers.group(numRows))
}

fun main(args: Array<String>) {
    val url = "http://tobin.yehle.io/sudoku-board.txt"

    val board = makeBoard(URL(url).readText())

    println(solution(board, 3, 3))
}
