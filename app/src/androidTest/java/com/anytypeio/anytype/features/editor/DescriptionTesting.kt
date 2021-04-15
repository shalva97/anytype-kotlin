package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.anytypeio.anytype.utils.checkHasText
import com.anytypeio.anytype.utils.onItemView
import com.anytypeio.anytype.utils.rVMatcher
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DescriptionTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(PageFragment.ID_KEY to root)

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "Description in Editor.",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldRenderDescriptionAfterTitle() {

        // SETUP

        val description = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = "A lighthouse is a tower, building, or another type of structure designed to emit light from a system of lamps and lenses and to serve as a navigational aid for maritime pilots at sea or on inland waterways.",
                marks = emptyList(),
                style = Block.Content.Text.Style.DESCRIPTION
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id, description.id)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id)
        )

        val document = listOf(page, header, title, description)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)

        launchFragment(args)

        // TESTING

        R.id.recycler.rVMatcher().apply {
            onItemView(0, R.id.title).checkHasText(
                title.content<Block.Content.Text>().text
            )
            onItemView(1, R.id.tvBlockDescription).checkHasText(
                description.content<Block.Content.Text>().text
            )
        }
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestPageFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}