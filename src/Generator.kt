import java.util.Random

/**
 * @author Tobin Yehle
 */

fun <T> List<T>.shuffle(): List<T> {
    val rng = Random()

    fun randomList(partial: List<Int> = listOf()): List<Int> =
        if(partial.size >= size) partial
        else {
            val candidate = rng.nextInt()
            if(partial.contains(candidate)) randomList(partial) else randomList(partial + candidate)
        }

    return this.zip(randomList()).sortedBy { pair -> pair.second }.map { pair -> pair.first }
}

fun Board<Int?>.withGuess(): Board<Int?> {
    val rng = Random()
    val guess = rng.nextInt(numRows) + 1
    val validPositions = indices.filter { this[it.row, it.col] == null }
    val indexChange = validPositions[rng.nextInt(validPositions.size)]

    return Board(rows.zip(rows.indices).map { pair ->
        val row = pair.first
        val rowIndex = pair.second
        if(rowIndex == indexChange.row) row.zip(row.indices).map { colPair ->
            val value = colPair.first
            val colIndex = colPair.second
            if(colIndex == indexChange.col) guess
            else value
        }
        else row
    })
}

fun <T> Board<T?>.without(pos: Pos): Board<T?> =
        Board({row, col -> if(row == pos.row && col == pos.col) null else this[row, col]},
              this.numRows,
              this.numCols)

fun randomBoard(m: Int, n: Int): Board<Int> {
    val emptyBoard = Board<Int?>({i, j -> null}, m*n, m*n)
//    val threshold = (m*m*n*n) / 3
    val threshold = Math.pow((m*n).±toDouble(), 1.5)

    tailrec fun buildBoard(board: Board<Int?> = emptyBoard, tries: Int = 0):Board<Int> =
        if(board.count { it != null } >= threshold) {
            println("broke the threshold after $tries tries")
            val all = allSolutions(board, n, m)
            println("${all.size} solutions found")
            if(all.isEmpty()) buildBoard()
            else all[Random().nextInt(all.size)]
        }
        else {
            val withNewEntry = board.withGuess()
            if (isValid(withNewEntry, m)) buildBoard(withNewEntry, tries)
            else buildBoard(tries = tries + 1)
        }

    return buildBoard()
}

fun randomPuzzle(m: Int, n: Int): Board<Int?> {
    val randomSolved:Board<Int?> = randomBoard(m, n).map { it } // apparently this shit is required

    fun removeIfPossible(board: Board<Int?>, pos: Pos): Board<Int?> {
        val without = board.without(pos)
        return when(unique(without, n, m)) {
            null  -> throw RuntimeException("Error, not solvable!\n$board")
            false -> board
            true  -> without
        }
    }

    return randomSolved.indices.shuffle().fold(randomSolved, ::removeIfPossible)
}

fun isValid(board: Board<Int?>, m: Int): Boolean = checkRows(board) && checkCols(board) && checkBlocks(board, m)

fun checkRows(board: Board<Int?>) = board.rows.all(::distinct)

fun checkCols(board: Board<Int?>) = board.cols.all(::distinct)

fun checkBlocks(board: Board<Int?>, m: Int) = board.blocks(m).all(::distinct)

fun distinct(entries: List<Int?>): Boolean {
    val notNull = entries.filter { it != null }
    return notNull.size == notNull.toSet().size
}
