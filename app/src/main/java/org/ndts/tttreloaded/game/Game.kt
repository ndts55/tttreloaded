package org.ndts.tttreloaded.game

import org.ndts.tttreloaded.game.InnerBoardResult.Draw
import org.ndts.tttreloaded.game.InnerBoardResult.None

const val DIM = 3
const val DD = DIM * DIM

// region Utils
private fun <T, R> getResult(
    arr: Array<T>, player: T, none: T, drawResult: R, noneResult: R, lineResult: R
): R = when {
    checkLines(arr, player) -> lineResult
    checkDraw(arr, none) -> drawResult
    else -> noneResult
}

private fun <T> checkLines(arr: Array<T>, desired: T): Boolean = (0 until DIM).any { i ->
    (0 until DIM).all { j -> arr[i * DIM + j] == desired } || (0 until DIM).all { j -> arr[j * DIM + i] == desired }
} || (0 until DIM).all { arr[it * DIM + it] == desired } || (0 until DIM).all { arr[(DIM - it - 1) * DIM + it] == desired }

private fun <T> checkDraw(arr: Array<T>, none: T): Boolean = arr.none { it == none }
//endregion

// region State
enum class TileState {
    Left, Right, None
}

enum class InnerBoardResult {
    Left, Right, None, Draw;
}

enum class OuterBoardResult {
    Left, Right, None, Draw;
}

data class InnerBoardState(
    val result: InnerBoardResult,
    val tiles: Array<TileState>,
    val enabled: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InnerBoardState

        if (result != other.result) return false
        if (!tiles.contentEquals(other.tiles)) return false
        if (enabled != other.enabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = result.hashCode()
        result1 = 31 * result1 + tiles.contentHashCode()
        result1 = 31 * result1 + enabled.hashCode()
        return result1
    }
}

data class OuterBoardState(
    val result: OuterBoardResult, val innerBoards: Array<InnerBoardState>
) {
    fun apply(event: PlayEvent): OuterBoardState {
        // Is it even valid to have clicked on the specified tile?
        if (result != OuterBoardResult.None
            || !innerBoards[event.boardId].enabled
            || innerBoards[event.boardId].result != None
            || innerBoards[event.boardId].tiles[event.tileId] != TileState.None
        ) return this
        val nextTiles = innerBoards[event.boardId].tiles.clone()
        nextTiles[event.tileId] = event.player.ts()
        val nextInnerBoardResult = getResult(
            arr = nextTiles,
            player = event.player.ts(),
            none = TileState.None,
            drawResult = Draw,
            noneResult = None,
            lineResult = event.player.ibr()
        )
        val constructIsEnabled = {
            val relevantBoardResult =
                if (event.boardId == event.tileId) nextInnerBoardResult else innerBoards[event.tileId].result
            val nextEnabledBoardId = if (relevantBoardResult == None) event.tileId else null
            { bid: Int -> nextEnabledBoardId == null || bid == nextEnabledBoardId }
        }
        val isEnabled = constructIsEnabled()
        val changedInnerBoard =
            InnerBoardState(nextInnerBoardResult, nextTiles, isEnabled(event.boardId))
        val nextInnerBoards =
            innerBoards.mapIndexed { boardId, innerBoardState ->
                if (boardId == event.boardId) changedInnerBoard else innerBoardState.copy(
                    enabled = isEnabled(
                        boardId
                    )
                )
            }.toTypedArray()
        val nextOuterBoardResult = getResult(
            arr = nextInnerBoards.map { it.result }.toTypedArray(),
            player = event.player.ibr(),
            none = None,
            drawResult = OuterBoardResult.Draw,
            noneResult = OuterBoardResult.None,
            lineResult = event.player.obr()
        )
        return this.copy(result = nextOuterBoardResult, innerBoards = nextInnerBoards)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OuterBoardState

        if (result != other.result) return false
        if (!innerBoards.contentEquals(other.innerBoards)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = result.hashCode()
        result1 = 31 * result1 + innerBoards.contentHashCode()
        return result1
    }
}

data class GameState(
    val player: Player = Player.Left, val outerBoardState: OuterBoardState = OuterBoardState(
        OuterBoardResult.None, (0 until DD).map {
            InnerBoardState(
                None, (0 until DD).map { TileState.None }.toTypedArray(), true
            )
        }.toTypedArray()
    )
) {
    fun apply(event: PlayEvent): GameState {
        if (event.player != player) return this
        val nextOuterBoardState = outerBoardState.apply(event)
        return if (nextOuterBoardState === outerBoardState) this else this.copy(
            player = when (nextOuterBoardState.result) {
                OuterBoardResult.None -> player.next()
                else -> player
            },
            outerBoardState = nextOuterBoardState
        )
    }
}
// endregion

// region Events
enum class Player {
    Left, Right;

    fun next(): Player = when (this) {
        Left -> Right
        Right -> Left
    }

    fun ts(): TileState = when (this) {
        Left -> TileState.Left
        Right -> TileState.Right
    }

    fun ibr(): InnerBoardResult = when (this) {
        Left -> InnerBoardResult.Left
        Right -> InnerBoardResult.Right
    }

    fun obr(): OuterBoardResult = when (this) {
        Left -> OuterBoardResult.Left
        Right -> OuterBoardResult.Right
    }

    override fun toString(): String = when (this) {
        Left -> "Cross"
        Right -> "Circles"
    }
}

data class PlayEvent(val player: Player, val boardId: Int, val tileId: Int)
// endregion