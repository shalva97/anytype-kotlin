package com.anytypeio.anytype.core_ui.common

import android.os.Build
import androidx.core.text.getSpans
import com.anytypeio.anytype.core_utils.ext.hasSpan
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SpanTest {

    @Mock
    lateinit var markup: Markup

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should find url span with red color`() {

        val source = "Everything was in confusion in the Oblonskys’ house"
        val link = "www.anytype.io"
        val textColor = 2131

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = link,
            type = Markup.Type.LINK
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable(textColor = textColor)

        assertTrue(hasSpan(editable, Span.Url::class.java))

        val spans = editable.getSpans<Span.Url>(start = 0, end = 5)

        assertEquals(1, spans.size)

        assertEquals(expected = link, actual = spans[0].url)
        assertEquals(expected = textColor, actual = spans[0].color)
    }


    private fun stubMarkup(
        source: String,
        mark: Markup.Mark
    ) {
        markup.stub {
            on { body } doReturn source
            on { marks } doReturn listOf(mark)
        }
    }
}