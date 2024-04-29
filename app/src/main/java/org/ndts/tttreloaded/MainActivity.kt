package org.ndts.tttreloaded

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ndts.tttreloaded.game.GameState
import org.ndts.tttreloaded.game.InnerBoardResult
import org.ndts.tttreloaded.game.InnerBoardState
import org.ndts.tttreloaded.game.OuterBoardState
import org.ndts.tttreloaded.game.PlayEvent
import org.ndts.tttreloaded.game.TileState
import org.ndts.tttreloaded.ui.theme.DrawColor
import org.ndts.tttreloaded.ui.theme.LeftColor
import org.ndts.tttreloaded.ui.theme.NoneColor
import org.ndts.tttreloaded.ui.theme.RightColor
import org.ndts.tttreloaded.ui.theme.TTTReloadedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TTTReloadedTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Game()
                }
            }
        }
    }
}

@Composable
fun Game() {
    var state by remember { mutableStateOf(GameState()) }
    OuterBoard(
        state = state.outerBoardState
    ) { boardId, tileId ->
        state = state.apply(PlayEvent(state.player, boardId, tileId))
    }
}

const val DIM = 3

@Composable
fun OuterBoard(
    state: OuterBoardState, modifier: Modifier = Modifier, onTileClick: (Int, Int) -> Unit
) = Column(modifier = modifier.aspectRatio(1.0f)) {
    repeat(DIM) { i ->
        Row(modifier = Modifier.weight(1.0F)) {
            repeat(DIM) { j ->
                val boardId = i * DIM + j
                InnerBoard(
                    state.innerBoards[boardId],
                    modifier = Modifier
                        .weight(1.0F),
                ) { tileId ->
                    onTileClick(boardId, tileId)
                }
            }
        }
    }
}

@Composable
fun InnerBoard(
    state: InnerBoardState, modifier: Modifier = Modifier, onTileClick: (Int) -> Unit
) {
    Box(Modifier.padding(2.dp)) {
        when (state.result) {
            InnerBoardResult.Left -> ColoredSquare(
                color = LeftColor
            )

            InnerBoardResult.Right -> ColoredSquare(
                color = RightColor
            )

            InnerBoardResult.Draw -> ColoredSquare(
                color = DrawColor
            )

            InnerBoardResult.None -> {
                Card(
                    modifier = modifier
                        .aspectRatio(1.0f)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(0.dp),
                ) {
                    repeat(DIM) { i ->
                        Row(
                            modifier = Modifier
                                .weight(1.0F)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            repeat(DIM) { j ->
                                val tileId = i * DIM + j
                                Tile(state = state.tiles[tileId],
                                    modifier = Modifier
                                        .weight(1.0F)
                                        .clickable(enabled = state.enabled) { onTileClick(tileId) })
                            }
                        }
                    }
                }

                if (!state.enabled)
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRect(Color.Black.copy(alpha = 0.25f))
                    }
            }
        }
    }
}

@Composable
fun Tile(state: TileState, modifier: Modifier = Modifier) = ColoredSquare(
    modifier = modifier,
    color = when (state) {
        TileState.Left -> LeftColor
        TileState.Right -> RightColor
        TileState.None -> NoneColor
    }
)

@Composable
fun ColoredSquare(
    modifier: Modifier = Modifier,
    color: Color,
) = Card(
    modifier = modifier
        .aspectRatio(1.0f),
    shape = RoundedCornerShape(0.dp),
    border = BorderStroke(0.5.dp, Color.Gray),
    colors = CardDefaults.cardColors(containerColor = color)
) {}

@Preview(showBackground = true)
@Composable
fun Preview() {
    TTTReloadedTheme {
        Game()
    }
}