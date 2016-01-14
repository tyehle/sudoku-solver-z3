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
                (0..rowNum-1).map { colNum ->
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
}

fun main(args: Array<String>) {
    val m = 3
    val n = 3

    val board = """_ _ _ _ 4 8 3 _ _
    _ _ _ 9 2 _ 5 _ _
    2 4 1 _ _ _ 9 _ 7
    1 _ _ 2 _ _ _ _ _
    _ _ 7 8 _ 6 _ 4 _
    3 _ 8 _ _ _ 6 5 9
    8 7 _ 3 _ _ _ _ 5
    _ _ 2 _ 9 _ 8 7 1
    9 _ 5 _ _ _ 2 6 _"""

    val test = Board(listOf(listOf(1, 2), listOf(3 , 4)))
    println(test.rows)
    println(test.cols)

    val context = Context()
    val s = context.mkSolver()

    s.add(makeCells(context, 3, 3, parseInitialValues(board)))

    s.check()

    println(s.model)
}

fun makeCell(context: Context, row:Int, col:Int) = context.mkConst("Cell($row, $col)", context.intSort) as IntExpr

fun makeCells(context: Context, blockRows: Int, blockCols: Int, initialValues: Board<Int?>):BoolExpr {
    val variables = Board({row, col -> makeCell(context, row, col)}, initialValues.numRows, initialValues.numCols)

    val rangeConstraints = variables.elements.map { v -> context.mkAnd(context.mkGt(v, context.mkInt(0)),
                                                                       context.mkLe(v, context.mkInt(9))) }

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

    return context.mkAnd(*((rangeConstraints + rowConstraints + colConstraints + blockConstraints).toTypedArray()))
}
