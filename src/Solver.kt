import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.IntExpr

/**
 * @author Tobin Yehle
 */

val <T> List<List<T>>.transpose: List<List<T>>
    get() = this.first().indices.map { i -> this.map{ it[i] } }

data class Pos(val row: Int, val col: Int)

class Board<T>(val rows: List<List<T>>) {
    constructor(builder: (Int, Int) -> T, numRows: Int, numCols: Int): this (
            (0..numRows-1).map { rowNum ->
                (0..numCols-1).map { colNum ->
                    builder(rowNum, colNum)
                }
            }
    )

    val numRows = rows.size
    val numCols = rows.first().size

    val indices = rows.indices.flatMap { rowNum ->
        rows[rowNum].indices.map { colNum -> Pos(rowNum, colNum) }
    }

    val cols = rows.transpose

    operator fun get(row: Int, col: Int):T = rows[row][col]

    val elements = rows.flatten()

    fun <S> map(f: (T) -> S): Board<S> = Board(rows.map{row -> row.map{f(it)}})

    override fun toString(): String = rows.map { row -> row.joinToString(" ") }.joinToString("\n")
}

fun main(args: Array<String>) {
    val m = 3
    val n = 3

    val board = makeBoard("""_ _ _ _ 4 8 3 _ _
    _ _ _ 9 2 _ 5 _ _
    2 4 1 _ _ _ 9 _ 7
    1 _ _ 2 _ _ _ _ _
    _ _ 7 8 _ 6 _ 4 _
    3 _ 8 _ _ _ 6 5 9
    8 7 _ 3 _ _ _ _ 5
    _ _ 2 _ 9 _ 8 7 1
    9 _ 5 _ _ _ 2 6 _""")

    val context = Context()
    val s = context.mkSolver()

    val cells = makeVars(context, m*n, m*n)

    s.add(genConstraints(context, cells, 3, 3, board))

    s.check()

    val result = cells.map { cell -> s.model.eval(cell, false) }

    println(board)

    println("---")

    println(result)
}

fun makeVar(context: Context, row:Int, col:Int) = context.mkConst("Cell($row, $col)", context.intSort) as IntExpr

fun makeVars(context: Context, numRows: Int, numCols: Int) =
        Board({row, col -> makeVar(context, row, col)}, numRows, numCols)

fun genConstraints(context: Context, variables: Board<IntExpr>, blockRows: Int, blockCols: Int, initialValues: Board<Int?>):BoolExpr {
    val rangeConstraints = variables.elements.map { v -> context.mkAnd(context.mkGt(v, context.mkInt(0)),
                                                                       context.mkLe(v, context.mkInt(variables.numRows))) }

    val initialValueConstraints = initialValues.indices.filter {
        pos -> initialValues[pos.row, pos.col] != null
    }.map {
        pos -> context.mkEq(variables[pos.row, pos.col], context.mkInt(initialValues[pos.row, pos.col]!!.toInt()))
    }

    val rowConstraints = variables.rows.map { row -> context.mkDistinct(*(row.toTypedArray())) }

    val colConstraints = variables.cols.map { col -> context.mkDistinct(*(col.toTypedArray())) }

    val numBlockRows = initialValues.numRows / blockRows
    val numBlockCols = initialValues.numCols / blockCols

    val blockConstraints = (0..numBlockRows-1).flatMap { blockRowNum -> (0..numBlockCols-1).map { blockColNum ->
        val blockIndices = variables.indices.filter { pos -> pos.row >= blockRowNum*blockRows &&
                                                             pos.row < (blockRowNum+1) * blockRows &&
                                                             pos.col >= blockColNum*blockCols &&
                                                             pos.col < (blockColNum+1)*blockCols
        }

        context.mkDistinct(*(blockIndices.map{pos -> variables[pos.row, pos.col]}.toTypedArray()))
    } }

    return context.mkAnd(*((rangeConstraints + initialValueConstraints + rowConstraints + colConstraints + blockConstraints).toTypedArray()))
}
