@file:OptIn(ExperimentalMaterial3Api::class)

package org.ndts.tttreloaded

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ndts.tttreloaded.game.DIM
import org.ndts.tttreloaded.game.GameState
import org.ndts.tttreloaded.game.InnerBoardResult
import org.ndts.tttreloaded.game.InnerBoardState
import org.ndts.tttreloaded.game.OuterBoardResult
import org.ndts.tttreloaded.game.OuterBoardState
import org.ndts.tttreloaded.game.PlayEvent
import org.ndts.tttreloaded.game.Player
import org.ndts.tttreloaded.game.TileState
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

private val colorAnimationSpec: AnimationSpec<Color> =
    tween(durationMillis = 75, easing = FastOutLinearInEasing)

@Composable
fun MaterialTheme.leftColor() = this.colorScheme.primary

@Composable
fun MaterialTheme.rightColor() = this.colorScheme.secondary

@Composable
fun MaterialTheme.noneColor() = this.colorScheme.background

@Composable
fun MaterialTheme.drawColor() = this.colorScheme.onBackground

@Composable
fun Game() {
    var state by remember { mutableStateOf(GameState()) }
    val playerIndicatorColor by animateColorAsState(
        targetValue = when (state.player) {
            Player.Left -> MaterialTheme.leftColor()
            Player.Right -> MaterialTheme.rightColor()
        }, label = "playerColor", animationSpec = colorAnimationSpec
    )

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = when (state.outerBoardState.result) {
                    OuterBoardResult.Left, OuterBoardResult.Right -> "${state.player} wins"
                    OuterBoardResult.None -> "TTT Reloaded"
                    OuterBoardResult.Draw -> "Draw"
                }
            )
        }, navigationIcon = {
            when (state.player) {
                Player.Left -> LeftIcon()
                Player.Right -> RightIcon()
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            navigationIconContentColor = playerIndicatorColor,
        )
        )
    }) {
        Column(
            modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OuterBoard(
                modifier = Modifier.padding(it), state = state.outerBoardState
            ) { boardId, tileId ->
                state = state.apply(PlayEvent(state.player, boardId, tileId))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { state = GameState() },
                enabled = state.outerBoardState.result != OuterBoardResult.None
            ) {
                Text(text = "Reset")
            }
        }
    }
}

@Composable
fun OuterBoard(
    modifier: Modifier = Modifier, state: OuterBoardState, onTileClick: (Int, Int) -> Unit
) = Column(modifier = modifier.aspectRatio(1.0f)) {
    repeat(DIM) { i ->
        Row(modifier = Modifier.weight(1.0F)) {
            repeat(DIM) { j ->
                val boardId = i * DIM + j
                InnerBoard(
                    state.innerBoards[boardId],
                    modifier = Modifier.weight(1.0F),
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
    val overlayColor by animateColorAsState(
        targetValue = if (state.enabled) Color.Transparent else Color.Black.copy(
            alpha = 0.25f
        ), label = "overlayColor", animationSpec = colorAnimationSpec
    )

    Box(Modifier.padding(2.dp)) {
        when (state.result) {
            InnerBoardResult.Left -> LeftTile(
                modifier = Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            InnerBoardResult.Right -> RightTile(
                modifier = Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            InnerBoardResult.Draw -> DrawTile(
                modifier = Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            InnerBoardResult.None -> {
                Card(
                    modifier = modifier
                        .aspectRatio(1.0f)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onBackground),
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
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRect(overlayColor)
                }
            }
        }
    }
}

@Composable
fun Tile(state: TileState, modifier: Modifier = Modifier) = when (state) {
    TileState.Left -> LeftTile(modifier = modifier)
    TileState.Right -> RightTile(modifier = modifier)
    TileState.None -> NoneTile(modifier = modifier)
}

@Composable
fun LeftTile(modifier: Modifier = Modifier) =
    Square(modifier = modifier) { LeftIcon(modifier = Modifier.fillMaxSize()) }

@Composable
fun RightTile(modifier: Modifier = Modifier) =
    Square(modifier = modifier) { RightIcon(modifier = Modifier.fillMaxSize()) }

@Composable
fun DrawTile(modifier: Modifier = Modifier) =
    Square(modifier = modifier) {
        Icon(
            painterResource(id = R.drawable.dash),
            contentDescription = "Draw",
            tint = MaterialTheme.drawColor(),
            modifier = Modifier.fillMaxSize()
        )
    }

@Composable
fun NoneTile(modifier: Modifier = Modifier) =
    Square(modifier = modifier.background(MaterialTheme.noneColor())) {}

@Composable
fun Square(modifier: Modifier = Modifier, content: @Composable (ColumnScope.() -> Unit)) = Card(
    modifier = modifier.aspectRatio(1.0f),
    shape = RoundedCornerShape(0.dp),
    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground),
    content = content
)

@Composable
fun LeftIcon(modifier: Modifier = Modifier) = Icon(
    painterResource(id = R.drawable.close),
    contentDescription = "Left",
    tint = MaterialTheme.leftColor(),
    modifier = modifier
)

@Composable
fun RightIcon(modifier: Modifier = Modifier) = Icon(
    painterResource(id = R.drawable.circle),
    contentDescription = "Right",
    tint = MaterialTheme.rightColor(),
    modifier = modifier
)

@Preview(showBackground = true)
@Composable
fun Preview() {
    TTTReloadedTheme {
        Game()
    }
}