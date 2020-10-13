package com.anytypeio.anytype.presentation.page

import com.anytypeio.anytype.core_ui.common.Alignment
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.features.page.styling.StylingMode
import com.anytypeio.anytype.core_ui.state.ControlPanelState
import com.anytypeio.anytype.core_ui.state.ControlPanelState.Companion.init
import com.anytypeio.anytype.core_ui.state.ControlPanelState.Toolbar
import com.anytypeio.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.domain.ext.overlap
import com.anytypeio.anytype.domain.misc.Overlap
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.extension.isInRange
import com.anytypeio.anytype.presentation.mapper.marks
import com.anytypeio.anytype.presentation.page.ControlPanelMachine.*
import com.anytypeio.anytype.presentation.page.editor.getStyleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * State machine for control panels consisting of [Interactor], [ControlPanelState], [Event] and [Reducer]
 * [Interactor] reduces [Event] to the immutable [ControlPanelState] by applying [Reducer] fuction.
 * This [ControlPanelState] then will be rendered.
 */
sealed class ControlPanelMachine {

    companion object {
        const val NO_BLOCK_SELECTED = 0
    }

    /**
     * @property scope coroutine scope (state machine runs inside this scope)
     */
    class Interactor(
        private val scope: CoroutineScope
    ) : ControlPanelMachine() {

        private val reducer: Reducer = Reducer()
        val channel: Channel<Event> = Channel()
        private val events: Flow<Event> = channel.consumeAsFlow()

        fun onEvent(event: Event) =
            scope.launch { channel.send(event) }
                //.also { Timber.d("Event: $event") }

        /**
         * @return a stream of immutable states, as processed by [Reducer].
         */
        fun state(): Flow<ControlPanelState> = events.scan(init(), reducer.function)
    }

    /**
     * Represents events related to this state machine and to control panel logics.
     */
    sealed class Event {

        /**
         * Represents text selection changes events
         * @property selection text selection (end index and start index are inclusive)
         */
        data class OnSelectionChanged(
            val target: String,
            val selection: IntRange
        ) : Event()


        object OnAddBlockToolbarOptionSelected : Event()

        /**
         * Represents an event when user selected a markup text color on [Toolbar.Styling] toolbar.
         */
        object OnMarkupTextColorSelected : Event()

        /**
         * Represents an event when user selected a background color on [Toolbar.Styling] toolbar.
         */
        object OnMarkupBackgroundColorSelected : Event()

        /**
         * Represents an event when user selected a block text color on [Toolbar.Styling] toolbar.
         */
        object OnBlockTextColorSelected : Event()


        data class OnEditorContextMenuStyleClicked(
            val selection: IntRange,
            val target: Block
        ) : Event()


        object OnBlockStyleSelected : Event()

        /**
         * Represents an event when user selected block background color on [Toolbar.Styling] toolbar.
         */
        object OnBlockBackgroundColorSelected : Event()


        /**
         * Represents an event when user cleares the current focus by closing keyboard.
         */
        object OnClearFocusClicked : Event()

        /**
         * Represents an event when user clicked on text input widget.
         * This event is expected to trigger keyboard openining.
         */
        object OnTextInputClicked : Event()

        /**
         * Represents an event when focus changes.
         * @property id id of the focused block
         */
        data class OnFocusChanged(
            val id: String,
            val style: Block.Content.Text.Style
        ) : Event()

        data class OnBlockActionToolbarStyleClicked(val target: Block,
                                                    val focused: Boolean,
                                                    val selection: IntRange?) : Event()

        /**
         * Styling-toolbar-related events
         */

        sealed class StylingToolbar : Event() {

            data class OnClose(val focused: Boolean) : StylingToolbar()
        }

        /**
         * Multi-select-related events
         */
        sealed class MultiSelect : Event() {
            object OnEnter : MultiSelect()
            object OnExit : MultiSelect()
            object OnDelete : MultiSelect()
            object OnTurnInto : MultiSelect()
            data class OnBlockClick(val count: Int) : MultiSelect()
        }

        /**
         * Read mode events
         */
        sealed class ReadMode: Event() {
            object OnEnter : ReadMode()
            object OnExit : ReadMode()
        }

        /**
         * Scroll-and-move-related events.
         */
        sealed class SAM : Event() {
            object OnApply : SAM()
            object OnExit : SAM()
            object OnEnter : SAM()
        }

        /**
         * Mention-related events.
         */
        sealed class Mentions : Event() {
            data class OnStart(val cursorCoordinate: Int, val mentionFrom: Int) : Mentions()
            data class OnQuery(val text: String) : Mentions()
            data class OnResult(val mentions: List<Mention>) : Mentions()
            object OnMentionClicked : Mentions()
            object OnStop : Mentions()
        }

        sealed class OnRefresh : Event() {
            data class StyleToolbar(val target: Block?, val selection: IntRange?) : OnRefresh()
        }
    }

    /**
     * Concrete reducer implementation that holds all the logic related to control panels.
     */
    class Reducer : StateReducer<ControlPanelState, Event> {

        private val excl = listOf(Overlap.LEFT, Overlap.RIGHT, Overlap.OUTER)
        private val incl = listOf(
            Overlap.EQUAL,
            Overlap.INNER,
            Overlap.LEFT,
            Overlap.RIGHT,
            Overlap.INNER_RIGHT,
            Overlap.INNER_LEFT
        )

        override val function: suspend (ControlPanelState, Event) -> ControlPanelState
            get() = { state, event ->
                logEvent(event)
                logState(text = "BEFORE", state = state)
                val afterState = reduce(state, event)
                logState(text = "AFTER", state = afterState)
                afterState
            }

        override suspend fun reduce(state: ControlPanelState, event: Event) = when (event) {
            is Event.OnSelectionChanged -> {
                when {
                    state.stylingToolbar.isVisible -> {
                        handleOnSelectionChangedForStylingToolbar(event.selection, event, state)
                    }
                    state.mentionToolbar.isVisible -> state.copy(
                        mentionToolbar = handleOnSelectionChangedForMentionState(
                            state = state.mentionToolbar,
                            start = event.selection.first
                        )
                    )
                    else -> {
                        state.copy(
                            mainToolbar = state.mainToolbar.copy(
                                isVisible = true
                            ),
                            navigationToolbar = state.navigationToolbar.copy(
                                isVisible = false
                            )
                        )
                    }
                }
            }
            is Event.StylingToolbar -> {
                handleStylingToolbarEvent(event, state)
            }
            is Event.OnMarkupTextColorSelected -> state.copy()
            is Event.OnBlockTextColorSelected -> state.copy()
            is Event.OnBlockBackgroundColorSelected -> state.copy()
            is Event.OnBlockStyleSelected -> state.copy()
            is Event.OnAddBlockToolbarOptionSelected -> state.copy()
            is Event.OnMarkupBackgroundColorSelected -> state.copy()
            is Event.OnEditorContextMenuStyleClicked -> {
                val config = event.target.getStyleConfig(
                    focus = true,
                    selection = event.selection
                )
                val target = target(event.target)
                val props = getMarkupLevelStylingProps(target, event.selection)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        config = config,
                        mode = StylingMode.MARKUP,
                        target = target,
                        props = props
                    )
                )
            }
            is Event.OnClearFocusClicked -> init()
            is Event.OnTextInputClicked -> {
                if (state.stylingToolbar.isVisible) {
                    state.copy(
                        stylingToolbar = Toolbar.Styling.reset(),
                        mainToolbar = state.mainToolbar.copy(isVisible = true),
                        navigationToolbar = state.navigationToolbar.copy(
                            isVisible = false
                        )
                    )
                } else {
                    state.copy()
                }
            }
            is Event.OnBlockActionToolbarStyleClicked -> {
                val target = target(event.target)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = getModeForSelection(event.selection),
                        target = target,
                        config = event.target.getStyleConfig(event.focused, event.selection),
                        props = getPropsForSelection(target, event.selection)
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    )
                )
            }
            is Event.OnRefresh.StyleToolbar -> {
                handleRefreshForMarkupLevelStyling(state, event)
            }
            is Event.MultiSelect -> {
                handleMultiSelectEvent(event, state)
            }
            is Event.SAM -> {
                handleScrollAndMoveEvent(event, state)
            }
            is Event.ReadMode -> {
                handleReadModeEvent(event, state)
            }
            is Event.Mentions -> {
                handleMentionEvent(event, state)
            }
            is Event.OnFocusChanged -> {
                when {
                    state.multiSelect.isVisible -> state.copy(
                        mentionToolbar = Toolbar.MentionToolbar.reset()
                    )
                    !state.mainToolbar.isVisible -> state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true
                        ),
                        stylingToolbar = Toolbar.Styling.reset(),
                        mentionToolbar = Toolbar.MentionToolbar.reset(),
                        navigationToolbar = Toolbar.Navigation(
                            isVisible = false
                        )
                    )
                    else -> {
                        state.copy(
                            stylingToolbar = state.stylingToolbar.copy(
                                isVisible = false
                            ),
                            mentionToolbar = Toolbar.MentionToolbar.reset()
                        )
                    }
                }
            }
        }

        private fun handleRefreshForMarkupLevelStyling(
            state: ControlPanelState,
            event: Event.OnRefresh.StyleToolbar
        ): ControlPanelState {
            return if (state.stylingToolbar.mode == StylingMode.MARKUP) {
                if (event.target != null) {
                    val target = target(event.target)
                    event.selection?.let {
                        val props = getMarkupLevelStylingProps(target, it)
                        state.copy(
                            stylingToolbar = state.stylingToolbar.copy(
                                props = props,
                                target = target
                            )
                        )
                    } ?: state.copy()
                } else {
                    state.copy()
                }
            } else {
                val target = event.target?.let { target(it) }
                state.copy(
                    stylingToolbar = state.stylingToolbar.copy(
                        target = target,
                        props = target?.let {
                            Toolbar.Styling.Props(
                                isBold = it.isBold,
                                isItalic = it.isItalic,
                                isStrikethrough = it.isStrikethrough,
                                isCode = it.isCode,
                                isLinked = it.isLinked,
                                color = it.color,
                                background = it.background,
                                alignment = it.alignment
                            )
                        }
                    )
                )
            }
        }

        private fun getModeForSelection(selection: IntRange?): StylingMode {
            return if (selection != null && selection.first != selection.last) StylingMode.MARKUP
            else StylingMode.BLOCK
        }

        private fun getPropsForSelection(target: Toolbar.Styling.Target, selection: IntRange?)
                : Toolbar.Styling.Props {
            return if (selection != null && selection.first != selection.last) {
                getMarkupLevelStylingProps(target, selection)
            } else {
                Toolbar.Styling.Props(
                    isBold = target.isBold,
                    isItalic = target.isItalic,
                    isStrikethrough = target.isStrikethrough,
                    isCode = target.isCode,
                    isLinked = target.isLinked,
                    color = target.color,
                    background = target.background,
                    alignment = target.alignment
                )
            }
        }

        private fun getMarkupLevelStylingProps(
            target: Toolbar.Styling.Target,
            selection: IntRange
        ): Toolbar.Styling.Props {

            var color: String? = null
            var background: String? = null

            val colorOverlaps = mutableListOf<Overlap>()
            val backgroundOverlaps = mutableListOf<Overlap>()

            target.marks.forEach { mark ->
                if (mark.type == Markup.Type.TEXT_COLOR) {
                    val range = mark.from..mark.to
                    val overlap = selection.overlap(range)
                    if (incl.contains(overlap))
                        color = mark.param
                    else
                        colorOverlaps.add(overlap)
                } else if (mark.type == Markup.Type.BACKGROUND_COLOR) {
                    val range = mark.from..mark.to
                    val overlap = selection.overlap(range)
                    if (incl.contains(overlap))
                        background = mark.param
                    else
                        backgroundOverlaps.add(overlap)
                }
            }

            if (color == null) {
                if (colorOverlaps.isEmpty() || colorOverlaps.none { value -> excl.contains(value) })
                    color = target.color ?: ThemeColor.DEFAULT.title
            }

            if (background == null) {
                if (backgroundOverlaps.isEmpty() || backgroundOverlaps.none { value ->
                        excl.contains(
                            value
                        )
                    })
                    background = target.background ?: ThemeColor.DEFAULT.title
            }

            return Toolbar.Styling.Props(
                isBold = Markup.Type.BOLD.isInRange(target.marks, selection),
                isItalic = Markup.Type.ITALIC.isInRange(target.marks, selection),
                isStrikethrough = Markup.Type.STRIKETHROUGH.isInRange(target.marks, selection),
                isCode = Markup.Type.KEYBOARD.isInRange(target.marks, selection),
                isLinked = Markup.Type.LINK.isInRange(target.marks, selection),
                color = color,
                background = background,
                alignment = target.alignment
            )
        }

        private fun handleOnSelectionChangedForStylingToolbar(
            selection: IntRange?,
            event: Event.OnSelectionChanged,
            state: ControlPanelState
        ): ControlPanelState {
            return if (selection == null || selection.first >= selection.last) {
                state.copy(stylingToolbar = Toolbar.Styling.reset())
            } else {
                val target = state.stylingToolbar.target
                if (target != null && state.stylingToolbar.mode == StylingMode.MARKUP) {
                    state.copy(
                        stylingToolbar = state.stylingToolbar.copy(
                            props = getMarkupLevelStylingProps(target, event.selection)
                        )
                    )
                } else {
                    state.copy()
                }
            }
        }

        private fun handleStylingToolbarEvent(
            event: Event.StylingToolbar,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.StylingToolbar.OnClose -> {
                if (event.focused) {
                    state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true
                        ),
                        navigationToolbar = state.navigationToolbar.copy(
                            isVisible = false
                        ),
                        stylingToolbar = Toolbar.Styling.reset()
                    )
                } else {
                    init()
                }
            }
        }

        private fun handleMentionEvent(
            event: Event.Mentions,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.Mentions.OnStart -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = true,
                    cursorCoordinate = event.cursorCoordinate,
                    mentionFilter = "",
                    updateList = false,
                    mentionFrom = event.mentionFrom
                )
            )
            is Event.Mentions.OnStop -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    updateList = false,
                    mentionFrom = null,
                    mentionFilter = null,
                    mentions = emptyList()
                )
            )
            is Event.Mentions.OnResult -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    mentions = event.mentions,
                    updateList = true
                )
            )
            is Event.Mentions.OnMentionClicked -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    mentionFrom = null,
                    updateList = true,
                    mentionFilter = null,
                    mentions = emptyList()
                )
            )
            is Event.Mentions.OnQuery -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    mentionFilter = event.text,
                    updateList = false
                )
            )
        }

        private fun handleMultiSelectEvent(
            event: Event.MultiSelect,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.MultiSelect.OnEnter -> state.copy(
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                multiSelect = state.multiSelect.copy(
                    isVisible = true,
                    count = NO_BLOCK_SELECTED
                ),
                navigationToolbar = state.navigationToolbar.copy(
                    isVisible = false
                )
            )
            is Event.MultiSelect.OnExit -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isVisible = false,
                    isScrollAndMoveEnabled = false,
                    count = NO_BLOCK_SELECTED
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                navigationToolbar = state.navigationToolbar.copy(
                    isVisible = true
                )
            )
            is Event.MultiSelect.OnDelete -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = NO_BLOCK_SELECTED
                )
            )
            is Event.MultiSelect.OnTurnInto -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = NO_BLOCK_SELECTED
                )
            )
            is Event.MultiSelect.OnBlockClick -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = event.count
                )
            )
        }

        private fun handleReadModeEvent(
            event: Event.ReadMode,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            Event.ReadMode.OnEnter -> {
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    multiSelect = state.multiSelect.copy(
                        isVisible = false
                    ),
                    stylingToolbar = Toolbar.Styling.reset(),
                    mentionToolbar = Toolbar.MentionToolbar.reset()
                )
            }
            Event.ReadMode.OnExit -> state.copy()
        }

        private fun handleScrollAndMoveEvent(
            event: Event.SAM,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.SAM.OnExit -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isScrollAndMoveEnabled = false
                )
            )
            is Event.SAM.OnEnter -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isScrollAndMoveEnabled = true
                )
            )
            is Event.SAM.OnApply -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = NO_BLOCK_SELECTED,
                    isScrollAndMoveEnabled = false
                )
            )
        }

        private fun handleOnSelectionChangedForMentionState(
            state: Toolbar.MentionToolbar,
            start: Int
        ): Toolbar.MentionToolbar {
            val from = state.mentionFrom
            return if (state.isVisible && from != null && start < from) {
                state.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    mentionFrom = null,
                    updateList = false,
                    mentionFilter = null,
                    mentions = emptyList()
                )
            } else {
                state.copy()
            }
        }

        private fun target(block: Block): Toolbar.Styling.Target {
            val content = block.content<Block.Content.Text>()
            return Toolbar.Styling.Target(
                id = block.id,
                text = content.text,
                color = content.color,
                background = content.backgroundColor,
                alignment = content.align?.let { alignment ->
                    when (alignment) {
                        Block.Align.AlignLeft -> Alignment.START
                        Block.Align.AlignRight -> Alignment.END
                        Block.Align.AlignCenter -> Alignment.CENTER
                    }
                },
                marks = content.marks()
            )
        }

        private fun logState(text: String, state: ControlPanelState) {
            Timber.i(
                "REDUCER, $text STATE:${
                    state
                }"
            )
        }

        private fun logEvent(event: Event) {
            Timber.i(
                "REDUCER, EVENT:${
                    event::class.qualifiedName?.substringAfter("Event.")
                }"
            )
        }
    }
}