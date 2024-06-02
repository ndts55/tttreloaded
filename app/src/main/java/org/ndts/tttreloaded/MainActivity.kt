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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun ColorScheme.leftColor() = secondary

@Composable
fun ColorScheme.leftContainer() = secondaryContainer

@Composable
fun ColorScheme.onLeftContainer() = onSecondaryContainer

@Composable
fun ColorScheme.rightColor() = tertiary

@Composable
fun ColorScheme.rightContainer() = tertiaryContainer

@Composable
fun ColorScheme.onRightContainer() = onTertiaryContainer

@Composable
fun ColorScheme.noneColor() = outlineVariant

@Composable
fun ColorScheme.drawColor() = onBackground

@Composable
fun ColorScheme.borderColor() = outline

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
        },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = when (state.player) {
                    Player.Left -> MaterialTheme.colorScheme.leftContainer()
                    Player.Right -> MaterialTheme.colorScheme.rightContainer()
                },
                titleContentColor = when (state.player) {
                    Player.Left -> MaterialTheme.colorScheme.onLeftContainer()
                    Player.Right -> MaterialTheme.colorScheme.onRightContainer()
                }
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

            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    when (state.outerBoardState.result) {
                        OuterBoardResult.Left, OuterBoardResult.Right, OuterBoardResult.Draw -> state =
                            GameState()

                        else -> showDialog = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (state.player) {
                        Player.Left -> MaterialTheme.colorScheme.leftContainer()
                        Player.Right -> MaterialTheme.colorScheme.rightContainer()
                    },
                    contentColor = when (state.player) {
                        Player.Left -> MaterialTheme.colorScheme.onLeftContainer()
                        Player.Right -> MaterialTheme.colorScheme.onRightContainer()
                    }
                )
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
                    color = MaterialTheme.colorScheme.borderColor()
                )
            )

            InnerBoardResult.Right -> RightTile(
                modifier = modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.borderColor()
                )
            )

            InnerBoardResult.Draw -> DrawTile(
                modifier = modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.borderColor()
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
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.borderColor()),
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
    Square(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.leftContainer())
    ) { LeftIcon(modifier = Modifier.fillMaxSize()) }

@Composable
fun RightTile(modifier: Modifier = Modifier) =
    Square(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.rightContainer())
    ) { RightIcon(modifier = Modifier.fillMaxSize()) }

@Composable
fun DrawTile(modifier: Modifier = Modifier) =
    Square(modifier = modifier) {
        Icon(
            painterResource(id = R.drawable.dash),
            contentDescription = "Draw",
            tint = MaterialTheme.colorScheme.drawColor(),
            modifier = Modifier.fillMaxSize()
        )
    }

@Composable
fun NoneTile(modifier: Modifier = Modifier) =
    Square(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.noneColor())
    ) {}

@Composable
fun Square(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable (ColumnScope.() -> Unit)
) = Card(
    modifier = modifier.aspectRatio(1.0f),
    shape = RoundedCornerShape(0.dp),
    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.borderColor()),
    colors = colors,
    content = content,
)

@Composable
fun LeftIcon(modifier: Modifier = Modifier) = Icon(
    painterResource(id = R.drawable.close),
    contentDescription = "Left",
    tint = MaterialTheme.colorScheme.leftColor(),
    modifier = modifier
)

@Composable
fun RightIcon(modifier: Modifier = Modifier) = Icon(
    painterResource(id = R.drawable.circle),
    contentDescription = "Right",
    tint = MaterialTheme.colorScheme.rightColor(),
    modifier = modifier
)

@Preview(showBackground = true)
@Composable
fun Preview() {
    TTTReloadedTheme {
        Game()
    }
}