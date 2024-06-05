@file:OptIn(ExperimentalMaterial3Api::class)

package org.ndts.tttreloaded

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import org.ndts.tttreloaded.ui.theme.TTTReloadedTheme
import org.ndts.tttrlib.DIM
import org.ndts.tttrlib.GameState
import org.ndts.tttrlib.InnerBoardResult
import org.ndts.tttrlib.InnerBoardState
import org.ndts.tttrlib.OuterBoardResult
import org.ndts.tttrlib.OuterBoardState
import org.ndts.tttrlib.PlayEvent
import org.ndts.tttrlib.Player
import org.ndts.tttrlib.TileState

class MainActivity : ComponentActivity() {
    private val tag = "TTTReloaded"
    private val stateKey = "GameState"
    private lateinit var _state: GameState
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadState()
        setContent {
            TTTReloadedTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    var state by rememberSaveable { mutableStateOf(_state) }
                    GameView(state,
                        onPlayEvent = { state = state.apply(it) },
                        onReset = { state = GameState() })
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveState()
    }

    private fun saveState() =
        getSharedPreferences(tag, MODE_PRIVATE).edit().also {
            it.putString(stateKey, Gson().toJson(_state))
            it.apply()
        }

    private fun loadState() =
        getSharedPreferences(tag, MODE_PRIVATE).getString(stateKey, null).also {
            _state = if (it != null) Gson().fromJson(it, GameState::class.java) else GameState()
        }
}

@Composable
fun ColorScheme.cross() = secondary

@Composable
fun ColorScheme.crossContainer() = secondaryContainer

@Composable
fun ColorScheme.onCrossContainer() = onSecondaryContainer

@Composable
fun ColorScheme.circle() = tertiary

@Composable
fun ColorScheme.circleContainer() = tertiaryContainer

@Composable
fun ColorScheme.onCircleContainer() = onTertiaryContainer

@Composable
fun ColorScheme.noneColor() = outlineVariant

@Composable
fun ColorScheme.drawColor() = onBackground

@Composable
fun ColorScheme.borderColor() = outline

@Composable
fun ColorScheme.borderHighlightColor() = onBackground

@Composable
fun GameView(state: GameState, onPlayEvent: (PlayEvent) -> Unit, onReset: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                text = when (state.outerBoardState.result) {
                    OuterBoardResult.Cross -> stringResource(id = R.string.cross_wins_title)
                    OuterBoardResult.Circle -> stringResource(id = R.string.circle_wins_title)
                    OuterBoardResult.None -> stringResource(id = R.string.app_name)
                    OuterBoardResult.Draw -> stringResource(id = R.string.draw_title)
                }
            )
        }, navigationIcon = {
            when (state.player) {
                Player.Cross -> CrossIcon()
                Player.Circle -> CircleIcon()
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = when (state.player) {
                Player.Cross -> MaterialTheme.colorScheme.crossContainer()
                Player.Circle -> MaterialTheme.colorScheme.circleContainer()
            }, titleContentColor = when (state.player) {
                Player.Cross -> MaterialTheme.colorScheme.onCrossContainer()
                Player.Circle -> MaterialTheme.colorScheme.onCircleContainer()
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
                onPlayEvent(PlayEvent(state.player, boardId, tileId))
            }
            Spacer(modifier = Modifier.height(20.dp))

            var showDialog by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    when (state.outerBoardState.result) {
                        OuterBoardResult.Cross, OuterBoardResult.Circle, OuterBoardResult.Draw -> onReset()
                        else -> showDialog = true
                    }
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = when (state.player) {
                        Player.Cross -> MaterialTheme.colorScheme.crossContainer()
                        Player.Circle -> MaterialTheme.colorScheme.circleContainer()
                    }, contentColor = when (state.player) {
                        Player.Cross -> MaterialTheme.colorScheme.onCrossContainer()
                        Player.Circle -> MaterialTheme.colorScheme.onCircleContainer()
                    }
                )
            ) {
                Text(text = stringResource(id = R.string.reset_button_text))
            }
            if (showDialog) AlertDialog(onDismissRequest = { showDialog = false },
                title = { Text(text = stringResource(id = R.string.confirmation_question)) },
                text = { Text(text = stringResource(id = R.string.reset_game_question)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        onReset()
                    }) { Text(text = stringResource(id = R.string.confirmation_button)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                    }) { Text(text = stringResource(id = R.string.rejection_button)) }
                })
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
) = AnimatedContent(targetState = state.result, label = "InnerBoardContent", transitionSpec = {
    (fadeIn(animationSpec = tween(220)) + scaleIn(
        initialScale = 0.92f, animationSpec = tween(220)
    )).togetherWith(fadeOut(animationSpec = tween(150)))
}) {
    Box(Modifier.padding(2.dp)) {
        when (it) {
            InnerBoardResult.Cross -> CrossTile(
                modifier = modifier.border(
                    width = 1.5.dp, color = MaterialTheme.colorScheme.borderColor()
                )
            )

            InnerBoardResult.Circle -> CircleTile(
                modifier = modifier.border(
                    width = 1.5.dp, color = MaterialTheme.colorScheme.borderColor()
                )
            )

            InnerBoardResult.Draw -> DrawTile(
                modifier = modifier.border(
                    width = 1.5.dp, color = MaterialTheme.colorScheme.borderColor()
                )
            )

            InnerBoardResult.None -> InnerBoard(
                tileStates = state.tiles, enabled = state.enabled, onTileClick = onTileClick
            )
        }
    }
}

@Composable
fun InnerBoard(
    modifier: Modifier = Modifier,
    tileStates: Array<TileState>,
    enabled: Boolean,
    onTileClick: (Int) -> Unit
) {
    val overlayColor by animateColorAsState(
        targetValue = if (enabled) Color.Transparent else Color.Black.copy(
            alpha = 0.25f
        ), label = "overlayColor", animationSpec = tween(durationMillis = 250, delayMillis = 150)
    )
    val outerBorderColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.borderHighlightColor() else MaterialTheme.colorScheme.borderColor(),
        label = "outerBorderColor",
        animationSpec = tween(durationMillis = 250, delayMillis = 150)
    )
    val outerBorderWidth by animateDpAsState(
        targetValue = if (enabled) 2.5.dp else 1.5.dp,
        label = "outerBorderWidth",
        animationSpec = tween(durationMillis = 250, delayMillis = 150)
    )
    Box {
        Card(
            modifier = modifier
                .aspectRatio(1.0f)
                .fillMaxSize(),
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(outerBorderWidth, outerBorderColor),
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
    AnimatedContent(targetState = state, label = "TileContent", transitionSpec = {
        (fadeIn(animationSpec = tween(220)) + scaleIn(
            initialScale = 0.92f, animationSpec = tween(220)
        )).togetherWith(fadeOut(animationSpec = tween(100)))
    }

    ) {
        when (it) {
            TileState.Cross -> CrossTile(modifier = modifier)
            TileState.Circle -> CircleTile(modifier = modifier)
            TileState.None -> NoneTile(modifier = modifier)
        }
    }

@Composable
fun CrossTile(modifier: Modifier = Modifier) = Square(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.crossContainer())
) { CrossIcon(modifier = Modifier.fillMaxSize()) }

@Composable
fun CircleTile(modifier: Modifier = Modifier) = Square(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.circleContainer())
) { CircleIcon(modifier = Modifier.fillMaxSize()) }

@Composable
fun DrawTile(modifier: Modifier = Modifier) = Square(modifier = modifier) {
    Icon(
        painterResource(id = R.drawable.dash),
        contentDescription = stringResource(id = R.string.draw_content_description),
        tint = MaterialTheme.colorScheme.drawColor(),
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun NoneTile(modifier: Modifier = Modifier) = Square(
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
fun CrossIcon(modifier: Modifier = Modifier) = Icon(
    painterResource(id = R.drawable.close),
    contentDescription = stringResource(id = R.string.cross_content_description),
    tint = MaterialTheme.colorScheme.cross(),
    modifier = modifier
)

@Composable
fun CircleIcon(modifier: Modifier = Modifier) = Icon(
    painterResource(id = R.drawable.circle),
    contentDescription = stringResource(id = R.string.circle_content_description),
    tint = MaterialTheme.colorScheme.circle(),
    modifier = modifier
)
