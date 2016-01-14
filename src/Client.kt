/**
 * @author Tobin Yehle
 */

fun <T> List<T>.group(n: Int): List<List<T>> = if(this.isEmpty()) listOf<List<T>>() else listOf(this.take(n)) + this.drop(n).group(n)

fun parseInitialValues(raw: String):Board<Int?> {
    val numbersMaybe = raw.split("\\s+".toRegex()).map { when(it) {
        "_" -> null
        else -> it.toInt()
    } }

    val numRows = Math.sqrt(numbersMaybe.size.toDouble()).toInt()

    return Board(numbersMaybe.group(numRows))
}

fun main(args: Array<String>) {
    val board = parseInitialValues("""_ _ _ _ 4 8 3 _ _
    _ _ _ 9 2 _ 5 _ _
    2 4 1 _ _ _ 9 _ 7
    1 _ _ 2 _ _ _ _ _
    _ _ 7 8 _ 6 _ 4 _
    3 _ 8 _ _ _ 6 5 9
    8 7 _ 3 _ _ _ _ 5
    _ _ 2 _ 9 _ 8 7 1
    9 _ 5 _ _ _ 2 6 _""")

    println(board.rows)
}
