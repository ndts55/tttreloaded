@file:OptIn(ExperimentalMaterial3Api::class)

package org.ndts.tttreloaded

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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

@Composable
fun MaterialTheme.leftColor() = Color.Red

@Composable
fun MaterialTheme.rightColor() = Color.Blue

@Composable
fun MaterialTheme.noneColor() = this.colorScheme.background

@Composable
fun MaterialTheme.drawColor() = this.colorScheme.onBackground

@Composable
fun Game() {
    var state by remember { mutableStateOf(GameState()) }

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
        }
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

            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    when (state.outerBoardState.result) {
                        OuterBoardResult.Left, OuterBoardResult.Right, OuterBoardResult.Draw -> state =
                            GameState()

                        else -> showDialog = true
                    }
                },
            ) {
                Text(text = "Reset")
            }
            if (showDialog) AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Really?") },
                text = { Text(text = "Reset the game state?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        state = GameState()
                    }) { Text(text = "Yes") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                    }) { Text(text = "No") }
                }
            )
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
) = AnimatedContent(targetState = state.result, label = "InnerBoardContent",
    transitionSpec =
    {
        (fadeIn(animationSpec = tween(220)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(220)
        )).togetherWith(fadeOut(animationSpec = tween(90)))
    }
) {
    Box(Modifier.padding(2.dp)) {
        when (it) {
            InnerBoardResult.Left -> LeftTile(
                modifier = modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            InnerBoardResult.Right -> RightTile(
                modifier = modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            InnerBoardResult.Draw -> DrawTile(
                modifier = modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            InnerBoardResult.None -> Board(
                modifier = modifier,
                tileStates = state.tiles,
                enabled = state.enabled,
                onTileClick = onTileClick
            )
        }
    }
}

@Composable
fun Board(
    modifier: Modifier = Modifier,
    tileStates: Array<TileState>,
    enabled: Boolean,
    onTileClick: (Int) -> Unit
) {
    val overlayColor by animateColorAsState(
        targetValue = if (enabled) Color.Transparent else Color.Black.copy(
            alpha = 0.25f
        ),
        label = "overlayColor",
        animationSpec = tween(durationMillis = 75, easing = FastOutLinearInEasing, delayMillis = 90)
    )
    Box {
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
                        Tile(state = tileStates[tileId],
                            modifier = Modifier
                                .weight(1.0F)
                                .clickable(enabled = enabled) { onTileClick(tileId) })
                    }
                }
            }
        }
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(overlayColor)
        }
    }
}

@Composable
fun Tile(state: TileState, modifier: Modifier = Modifier) =
    AnimatedContent(targetState = state, label = "TileContent",
        transitionSpec =
        {
            (fadeIn(animationSpec = tween(220)) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(220)
            )).togetherWith(fadeOut(animationSpec = tween(90)))
        }

    ) {
        when (it) {
            TileState.Left -> LeftTile(modifier = modifier)
            TileState.Right -> RightTile(modifier = modifier)
            TileState.None -> NoneTile(modifier = modifier)
        }
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