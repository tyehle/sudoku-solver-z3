import com.microsoft.z3.*

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

    fun <S> zip(other: Board<S>) = Board(rows.zip(other.rows).map { pair -> pair.first.zip(pair.second) })

    override fun toString(): String = rows.map { row -> row.joinToString(" ") }.joinToString("\n")
}

/** Gets a solution to a board if one exists. If there is no solution then the result is null */
fun solution(initialValues: Board<Int?>, blockRows: Int, blockCols: Int): Board<Int>? {
    val context = Context()
    val s = context.mkSolver()

    val cells = makeVars(context, blockRows*blockCols, blockRows*blockCols)

    s.add(genConstraints(context, cells, blockRows, blockCols, initialValues))

    return if(s.check() == Status.SATISFIABLE) cells.map { cell -> s.model.eval(cell, false).toString().toInt() }
           else null
}

/**
 * Returns 0, 1 or 2. 0 means there is no solution to the puzzle, 1 means there is exactly 1 solution, and 2 means
 * there is more than one solution.
 */
fun numSolutions(initialValues: Board<Int?>, blockRows: Int, blockCols: Int): Int {
    val context = Context()
    val s = context.mkSolver()

    val cells = makeVars(context, blockRows*blockCols, blockRows*blockCols)

    s.add(genConstraints(context, cells, blockRows, blockCols, initialValues))

    if(s.check() == Status.SATISFIABLE) {
        s.add(genDifferentConstraint(context, cells, cells.map { cell -> s.model.eval(cell, false) }))
        return if(s.check() == Status.SATISFIABLE) 2 else 1
    }
    else return 0
}

fun genDifferentConstraint(context: Context, variables: Board<IntExpr>, solution: Board<Expr>): BoolExpr =
        context.mkNot(context.mkAnd(*(variables.zip(solution).elements.map { pair -> context.mkEq(pair.first, pair.second) }.toTypedArray())))

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
