package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.TextBlock
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.core_models.ext.overlap
import com.anytypeio.anytype.core_models.misc.Overlap
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine.*
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState.Companion.init
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState.Toolbar
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingMode
import com.anytypeio.anytype.presentation.editor.editor.styling.getStyleConfig
import com.anytypeio.anytype.presentation.editor.editor.styling.getSupportedMarkupTypes
import com.anytypeio.anytype.presentation.extension.isInRange
import com.anytypeio.anytype.presentation.extension.style
import com.anytypeio.anytype.presentation.mapper.marks
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
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
            val target: Block,
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

        data class OnBlockActionToolbarStyleClicked(
            val target: Block,
            val focused: Boolean,
            val selection: IntRange?
        ) : Event()

        object OnMultiSelectStyleClicked : Event()

        sealed class SearchToolbar : Event() {
            object OnEnterSearchMode : SearchToolbar()
            object OnExitSearchMode : SearchToolbar()
        }

        sealed class MarkupToolbar : Event() {
            object OnMarkupColorToggleClicked : MarkupToolbar()
            object OnMarkupHighlightToggleClicked : MarkupToolbar()
            object OnMarkupToolbarUrlClicked: MarkupToolbar()
            object OnMarkupUrlSet: MarkupToolbar()
            object OnBlockerClicked: MarkupToolbar()
        }

        /**
         * Styling-toolbar-related events
         */

        sealed class StylingToolbar : Event() {
            object OnExtraClicked : StylingToolbar()
            object OnColorClicked : StylingToolbar()
            object OnExtraClosed : StylingToolbar()
            object OnColorClosed : StylingToolbar()
            data class OnClose(val focused: Boolean) : StylingToolbar()
            object OnCloseMulti : StylingToolbar()
            object OnExit : StylingToolbar()
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
            data class OnQuickStart(val countOnStart: Int) : SAM()
        }

        /**
         * Mention-related events.
         */
        sealed class Mentions : Event() {
            data class OnStart(val cursorCoordinate: Int, val mentionFrom: Int) : Mentions()
            data class OnQuery(val text: String) : Mentions()
            data class OnResult(val mentions: List<DefaultObjectView>) : Mentions()
            object OnMentionClicked : Mentions()
            object OnStop : Mentions()
        }

        sealed class Slash : Event() {
            data class OnStart(
                val cursorCoordinate: Int,
                val slashFrom: Int
            ) : Slash()

            data class OnFilterChange(
                val widgetState: SlashWidgetState
            ) : Slash()

            object OnStop : Slash()
            object OnStopAndClearFocus: Slash()
        }

        sealed class OnRefresh : Event() {
            data class StyleToolbar(val target: Block?, val selection: IntRange?) : OnRefresh()
            data class Markup(val target: Block?, val selection: IntRange?) : OnRefresh()
        }

        object OnDocumentMenuClicked : Event()
        object OnDocumentIconClicked : Event()
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
                    state.mainToolbar.isVisible -> {
                        if (event.selection.isEmpty() || event.selection.first == event.selection.last) {
                            state.copy(
                                markupMainToolbar = Toolbar.MarkupMainToolbar.reset()
                            )
                        } else {
                            state.copy(
                                mainToolbar = state.mainToolbar.copy(
                                    isVisible = false
                                ),
                                markupMainToolbar = state.markupMainToolbar.copy(
                                    isVisible = true,
                                    style= event.target.style(event.selection),
                                    supportedTypes = event.target.content.let { content ->
                                        if (content is TextBlock) {
                                            content.getSupportedMarkupTypes()
                                        } else {
                                            emptyList()
                                        }
                                    }
                                )
                            )
                        }
                    }
                    state.markupMainToolbar.isVisible -> {
                        if (event.selection.isEmpty() || event.selection.first == event.selection.last) {
                            state.copy(
                                mainToolbar = state.mainToolbar.copy(
                                    isVisible = true
                                ),
                                markupMainToolbar = Toolbar.MarkupMainToolbar.reset(),
                                markupColorToolbar = state.markupColorToolbar.copy(
                                    isVisible = false
                                )
                            )
                        } else {
                            state.copy(
                                markupMainToolbar = state.markupMainToolbar.copy(
                                    style = event.target.style(event.selection)
                                )
                            )
                        }
                    }
                    state.stylingToolbar.isVisible -> {
                        handleOnSelectionChangedForStylingToolbar(event.selection, event, state)
                    }
                    state.mentionToolbar.isVisible -> {
                        val newMentionToolbarState = handleOnSelectionChangedForMentionState(
                            state = state.mentionToolbar,
                            start = event.selection.first
                        )
                        state.copy(
                            mentionToolbar = newMentionToolbarState,
                            mainToolbar = state.mainToolbar.copy(
                                isVisible = !newMentionToolbarState.isVisible
                            )
                        )
                    }
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
            is Event.MarkupToolbar.OnMarkupColorToggleClicked -> {
                val isVisible = if (state.markupColorToolbar.isVisible) {
                    state.markupMainToolbar.isBackgroundColorSelected
                } else {
                    true
                }
                state.copy(
                    markupColorToolbar = state.markupColorToolbar.copy(
                        isVisible = isVisible
                    ),
                    markupMainToolbar = state.markupMainToolbar.copy(
                        isBackgroundColorSelected = false,
                        isTextColorSelected = isVisible
                    )
                )
            }
            is Event.MarkupToolbar.OnMarkupHighlightToggleClicked -> {
                val isVisible = if (state.markupColorToolbar.isVisible) {
                    state.markupMainToolbar.isTextColorSelected
                } else {
                    true
                }
                state.copy(
                    markupColorToolbar = state.markupColorToolbar.copy(
                        isVisible = isVisible
                    ),
                    markupMainToolbar = state.markupMainToolbar.copy(
                        isTextColorSelected = false,
                        isBackgroundColorSelected = isVisible
                    )
                )
            }
            is Event.MarkupToolbar.OnMarkupToolbarUrlClicked -> {
                state.copy(
                    markupColorToolbar = state.markupColorToolbar.copy(
                        isVisible = false
                    ),
                    markupMainToolbar = state.markupMainToolbar.copy(),
                    markupUrlToolbar = state.markupUrlToolbar.copy(
                        isVisible = true
                    )
                )
            }
            is Event.MarkupToolbar.OnMarkupUrlSet -> {
                state.copy(
                    markupUrlToolbar = state.markupUrlToolbar.copy(
                        isVisible = false
                    )
                )
            }
            is Event.MarkupToolbar.OnBlockerClicked -> {
                state.copy(
                    markupUrlToolbar = state.markupUrlToolbar.copy(
                        isVisible = false
                    )
                )
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
                        ),
                        slashWidget = Toolbar.SlashWidget.reset()
                    )
                } else {
                    state.copy(
                        slashWidget = Toolbar.SlashWidget.reset()
                    )
                }
            }
            is Event.OnBlockActionToolbarStyleClicked -> {
                val target = target(event.target)
                val style = event.target.let {
                    val content = it.content
                    check(content is Block.Content.Text)
                    content.style
                }
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = getModeForSelection(event.selection),
                        target = target,
                        style = style,
                        config = event.target.getStyleConfig(event.focused, event.selection),
                        props = getPropsForSelection(target, event.selection)
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    )
                )
            }
            // TODO move it somewhere in appropriate group
            is Event.OnMultiSelectStyleClicked -> {
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = null,
                        target = null,
                        style = null,
                        config = null,
                        props = null
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    )
                )
            }
            is Event.OnRefresh.StyleToolbar -> {
                handleRefreshForMarkupLevelStyling(state, event)
            }
            is Event.OnRefresh.Markup -> {
                if (event.target != null && event.selection != null) {
                    state.copy(
                        markupMainToolbar = state.markupMainToolbar.copy(
                            style = event.target.style(event.selection)
                        )
                    )
                } else {
                    state.copy()
                }
            }
            is Event.MultiSelect -> {
                handleMultiSelectEvent(event, state)
            }
            is Event.SearchToolbar.OnEnterSearchMode -> state.copy(
                searchToolbar = Toolbar.SearchToolbar(isVisible = true),
                mainToolbar = Toolbar.Main(isVisible = false),
                multiSelect = Toolbar.MultiSelect(
                    isVisible = false,
                    isScrollAndMoveEnabled = false,
                    count = 0
                ),
                stylingToolbar = Toolbar.Styling.reset(),
                navigationToolbar = Toolbar.Navigation(isVisible = false)
            )
            is Event.SearchToolbar.OnExitSearchMode -> state.copy(
                searchToolbar = state.searchToolbar.copy(isVisible = false),
                navigationToolbar = state.navigationToolbar.copy(isVisible = true)
            )
            is Event.SAM -> {
                handleScrollAndMoveEvent(event, state)
            }
            is Event.ReadMode -> {
                handleReadModeEvent(event, state)
            }
            is Event.Mentions -> {
                handleMentionEvent(event, state)
            }
            is Event.Slash -> {
                handleSlashEvent(event, state)
            }
            is Event.OnFocusChanged -> {
                when {
                    state.multiSelect.isVisible -> state.copy(
                        mentionToolbar = Toolbar.MentionToolbar.reset(),
                        slashWidget = Toolbar.SlashWidget.reset()
                    )
                    !state.mainToolbar.isVisible -> state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true
                        ),
                        stylingToolbar = Toolbar.Styling.reset(),
                        mentionToolbar = Toolbar.MentionToolbar.reset(),
                        slashWidget = Toolbar.SlashWidget.reset(),
                        markupMainToolbar = Toolbar.MarkupMainToolbar.reset(),
                        navigationToolbar = Toolbar.Navigation(
                            isVisible = false
                        )
                    )
                    else -> {
                        state.copy(
                            stylingToolbar = state.stylingToolbar.copy(
                                isVisible = false
                            ),
                            mentionToolbar = Toolbar.MentionToolbar.reset(),
                            slashWidget = Toolbar.SlashWidget.reset()
                        )
                    }
                }
            }
            Event.OnDocumentMenuClicked -> {
                state.copy(
                    slashWidget = Toolbar.SlashWidget.reset()
                )
            }
            Event.OnDocumentIconClicked -> {
                state.copy(
                    slashWidget = Toolbar.SlashWidget.reset()
                )
            }
        }

        private fun handleRefreshForMarkupLevelStyling(
            state: ControlPanelState,
            event: Event.OnRefresh.StyleToolbar
        ): ControlPanelState {
            val target = event.target?.let { target(it) }
            val style = event.target?.let {
                val content = it.content
                check(content is Block.Content.Text)
                content.style
            } ?: TextStyle.P
            return state.copy(
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
                    },
                    style = style
                )
            )
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
            is Event.StylingToolbar.OnCloseMulti -> {
                state.copy(
                    stylingToolbar = Toolbar.Styling.reset()
                )
            }
            is Event.StylingToolbar.OnExit -> {
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = true
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = Toolbar.Styling.reset(),
                    styleColorToolbar = Toolbar.Styling.Color(),
                    styleExtraToolbar = Toolbar.Styling.Other()
                )
            }
            is Event.StylingToolbar.OnExtraClicked -> {
                state.copy(
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = false
                    ),
                    styleExtraToolbar = Toolbar.Styling.Other(true)
                )
            }
            is Event.StylingToolbar.OnColorClicked -> {
                state.copy(
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = false
                    ),
                    styleColorToolbar = Toolbar.Styling.Color(true)
                )
            }
            is Event.StylingToolbar.OnExtraClosed -> {
                state.copy(
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true
                    ),
                    styleExtraToolbar = Toolbar.Styling.Other(false)
                )
            }
            is Event.StylingToolbar.OnColorClosed -> {
                state.copy(
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true
                    ),
                    styleColorToolbar = Toolbar.Styling.Color(false)
                )
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
                ),
                mainToolbar = Toolbar.Main(isVisible = false)
            )
            is Event.Mentions.OnStop -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    updateList = false,
                    mentionFrom = null,
                    mentionFilter = null,
                    mentions = emptyList()
                ),
                mainToolbar = Toolbar.Main(isVisible = true)
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

        private fun handleSlashEvent(
            event: Event.Slash,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.Slash.OnStart -> {
                state.copy(
                    slashWidget = state.slashWidget.copy(
                        isVisible = true,
                        from = event.slashFrom,
                        filter = "",
                        widgetState = null,
                        cursorCoordinate = event.cursorCoordinate,
                        updateList = false,
                        items = emptyList()
                    ),
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    )
                )
            }
            Event.Slash.OnStop -> {
                state.copy(slashWidget = Toolbar.SlashWidget.reset())
            }
            is Event.Slash.OnStopAndClearFocus -> {
                state.copy(
                    slashWidget = Toolbar.SlashWidget.reset(),
                    mainToolbar = state.mainToolbar.copy(isVisible = false),
                    navigationToolbar = state.navigationToolbar.copy(isVisible = true)
                )
            }
            is Event.Slash.OnFilterChange -> {
                state.copy(
                    slashWidget = state.slashWidget.copy(
                        widgetState = event.widgetState
                    )
                )
            }
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
                    isQuickScrollAndMoveMode = false,
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
                    mentionToolbar = Toolbar.MentionToolbar.reset(),
                    slashWidget = Toolbar.SlashWidget.reset()
                )
            }
            Event.ReadMode.OnExit -> state.copy()
        }

        private fun handleScrollAndMoveEvent(
            event: Event.SAM,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.SAM.OnExit -> {
                if (state.multiSelect.isQuickScrollAndMoveMode) {
                    state.copy(
                        multiSelect = state.multiSelect.copy(
                            isVisible = false,
                            isScrollAndMoveEnabled = false,
                            isQuickScrollAndMoveMode = false,
                            count = NO_BLOCK_SELECTED
                        ),
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = false
                        ),
                        navigationToolbar = state.navigationToolbar.copy(
                            isVisible = true
                        )
                    )
                } else {
                    state.copy(
                        multiSelect = state.multiSelect.copy(
                            isScrollAndMoveEnabled = false,
                            isQuickScrollAndMoveMode = false
                        )
                    )
                }
            }
            is Event.SAM.OnEnter -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isScrollAndMoveEnabled = true
                )
            )
            is Event.SAM.OnQuickStart -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isVisible = true,
                    isScrollAndMoveEnabled = true,
                    isQuickScrollAndMoveMode = true,
                    count = event.countOnStart
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                navigationToolbar = state.navigationToolbar.copy(
                    isVisible = false
                ),
                slashWidget = Toolbar.SlashWidget.reset()
            )
            is Event.SAM.OnApply -> {
                if (state.multiSelect.isQuickScrollAndMoveMode) {
                    state.copy(
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
                } else {
                    state.copy(
                        multiSelect = state.multiSelect.copy(
                            count = NO_BLOCK_SELECTED,
                            isScrollAndMoveEnabled = false
                        )
                    )
                }
            }
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

        //todo Need refactoring
        private fun target(block: Block): Toolbar.Styling.Target =
            when (val content = block.content) {
                is Block.Content.RelationBlock -> {
                    Toolbar.Styling.Target(
                        id = block.id,
                        text = "",
                        color = null,
                        background = content.background,
                        alignment = null,
                        marks = listOf()
                    )
                }
                is Block.Content.Text -> {
                    Toolbar.Styling.Target(
                        id = block.id,
                        text = content.text,
                        color = content.color,
                        background = content.backgroundColor,
                        alignment = content.align?.let { alignment ->
                            when (alignment) {
                                Block.Align.AlignCenter -> Alignment.CENTER
                                Block.Align.AlignLeft -> Alignment.START
                                Block.Align.AlignRight -> Alignment.END
                            }
                        },
                        marks = content.marks()
                    )
                }
                else -> {
                    throw IllegalArgumentException("Unexpected content type for style toolbar: ${block.content::class.java.simpleName}")
                }
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