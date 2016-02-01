import java.net.URL

/**
 * @author Tobin Yehle
 */

fun <T> List<T>.group(n: Int): List<List<T>> = if(this.isEmpty()) listOf<List<T>>() else listOf(this.take(n)) + this.drop(n).group(n)

fun toNumberList(raw: String): List<Int?> = raw.split("\\s+".toRegex()).filter { it.isNotEmpty() }.map { when(it) {
    "_" -> null
    else -> it.toInt()
} }

fun makeBoard(numbers: List<Int?>):Board<Int?> {
    val numRows = Math.sqrt(numbers.size.toDouble()).toInt()

    return Board(numbers.group(numRows))
}

fun main(args: Array<String>) {
//    val url = "http://tobin.yehle.io/sudoku-board.txt"
//    val url = "http://cs.utah.edu/~tyehle/sudoku/board.txt"
//    val url = "http://www.cs.utah.edu/~acherk/tooPro.txt"
    val url = if(args.size > 0) args[0] else "http://www.cs.utah.edu/~acherk/tooPro.txt"

    println("Downloading board from $url")

    val numbers = toNumberList(URL(url).readText())

    val m = numbers[0] ?: throw RuntimeException("Board size must be defined")
    val n = numbers[1] ?: throw RuntimeException("Board size must be defined")
    val board = makeBoard(numbers.drop(2))

    println("Solving\n-------")
    val list = allSolutions(board, m, n)
    println("${list.size} solutions found\n")
    println(list.first())
}
