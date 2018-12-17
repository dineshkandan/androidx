/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.ui.port.engine.text.platform

import android.app.Instrumentation
import android.content.Context
import android.graphics.Typeface
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.ui.engine.text.FontStyle
import androidx.ui.engine.text.FontWeight
import androidx.ui.engine.text.font.Font
import androidx.ui.engine.text.font.FontFamily
import androidx.ui.engine.text.font.FontMatcher
import androidx.ui.engine.text.font.asFontFamily
import androidx.ui.engine.text.platform.TypefaceAdapter
import androidx.ui.port.bitmap
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_100_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_100_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_200_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_200_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_300_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_300_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_400_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_400_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_500_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_500_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_600_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_600_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_700_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_700_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_800_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_800_REGULAR
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_900_ITALIC
import androidx.ui.port.engine.text.FontTestData.Companion.FONT_900_REGULAR
import androidx.ui.port.matchers.equalToBitmap
import androidx.ui.port.matchers.isTypefaceOf
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.not
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@SmallTest
class TypefaceAdapterTest {
    // TODO(Migration/siyamed): These native calls should be removed after the
    // counterparts are implemented in crane.
    private lateinit var instrumentation: Instrumentation
    private lateinit var context: Context

    @Before
    fun setup() {
        instrumentation = InstrumentationRegistry.getInstrumentation()
        context = instrumentation.context
    }

    @Test
    fun createDefaultTypeface() {
        val typeface = TypefaceAdapter().create()

        assertThat(typeface).isNotNull()
        assertThat(typeface.isBold).isFalse()
        assertThat(typeface.isItalic).isFalse()
    }

    @Test
    fun fontWeightItalicCreatesItalicFont() {
        val typeface = TypefaceAdapter().create(fontStyle = FontStyle.italic)

        assertThat(typeface).isNotNull()
        assertThat(typeface.isBold).isFalse()
        assertThat(typeface.isItalic).isTrue()
    }

    @Test
    fun fontWeightBoldCreatesBoldFont() {
        val typeface = TypefaceAdapter().create(fontWeight = FontWeight.bold)

        assertThat(typeface).isNotNull()
        assertThat(typeface.isBold).isTrue()
        assertThat(typeface.isItalic).isFalse()
    }

    @Test
    fun fontWeightBoldFontStyleItalicCreatesBoldItalicFont() {
        val typeface = TypefaceAdapter().create(
            fontStyle = FontStyle.italic,
            fontWeight = FontWeight.bold
        )

        assertThat(typeface).isNotNull()
        assertThat(typeface.isBold).isTrue()
        assertThat(typeface.isItalic).isTrue()
    }

    @Test
    fun serifAndSansSerifPaintsDifferent() {
        val typefaceSans = TypefaceAdapter().create(genericFontFamily = "sans-serif")
        val typefaceSerif = TypefaceAdapter().create(genericFontFamily = "serif")

        assertThat(typefaceSans).isNotNull()
        assertThat(typefaceSans).isNotNull()
        Assert.assertThat(typefaceSans.bitmap(), not(equalToBitmap(typefaceSerif.bitmap())))
    }

    @Test
    fun getTypefaceStyleSnapToNormalFor100to500() {
        val fontWeights = arrayOf(
            FontWeight.w100,
            FontWeight.w200,
            FontWeight.w300,
            FontWeight.w400,
            FontWeight.w500
        )

        for (fontWeight in fontWeights) {
            for (fontStyle in FontStyle.values()) {
                val typefaceStyle = TypefaceAdapter().getTypefaceStyle(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                )

                if (fontStyle == FontStyle.normal) {
                    assertThat(typefaceStyle).isEqualTo(Typeface.NORMAL)
                } else {
                    assertThat(typefaceStyle).isEqualTo(Typeface.ITALIC)
                }
            }
        }
    }

    @Test
    fun getTypefaceStyleSnapToBoldFor600to900() {
        val fontWeights = arrayOf(
            FontWeight.w600,
            FontWeight.w700,
            FontWeight.w800,
            FontWeight.w900
        )

        for (fontWeight in fontWeights) {
            for (fontStyle in FontStyle.values()) {
                val typefaceStyle = TypefaceAdapter().getTypefaceStyle(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                )

                if (fontStyle == FontStyle.normal) {
                    assertThat(typefaceStyle).isEqualTo(Typeface.BOLD)
                } else {
                    assertThat(typefaceStyle).isEqualTo(Typeface.BOLD_ITALIC)
                }
            }
        }
    }

    @Test
    @SdkSuppress(maxSdkVersion = 27)
    fun fontWeights100To500SnapToNormalBeforeApi28() {
        val fontWeights = arrayOf(
            FontWeight.w100,
            FontWeight.w200,
            FontWeight.w300,
            FontWeight.w400,
            FontWeight.w500
        )

        for (fontWeight in fontWeights) {
            for (fontStyle in FontStyle.values()) {
                val typeface = TypefaceAdapter().create(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                )

                assertThat(typeface).isNotNull()
                assertThat(typeface.isBold).isFalse()
                assertThat(typeface.isItalic).isEqualTo(fontStyle == FontStyle.italic)
            }
        }
    }

    @Test
    @SdkSuppress(maxSdkVersion = 27)
    fun fontWeights600To900SnapToBoldBeforeApi28() {
        val fontWeights = arrayOf(
            FontWeight.w600,
            FontWeight.w700,
            FontWeight.w800,
            FontWeight.w900
        )

        for (fontWeight in fontWeights) {
            for (fontStyle in FontStyle.values()) {
                val typeface = TypefaceAdapter().create(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                )

                assertThat(typeface).isNotNull()
                assertThat(typeface.isBold).isTrue()
                assertThat(typeface.isItalic).isEqualTo(fontStyle == FontStyle.italic)
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 28)
    fun typefaceCreatedWithCorrectFontWeightAndFontStyle() {
        for (fontWeight in FontWeight.values) {
            for (fontStyle in FontStyle.values()) {
                val typeface = TypefaceAdapter().create(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                )

                assertThat(typeface).isNotNull()
                assertThat(typeface.weight).isEqualTo(fontWeight.weight)
                assertThat(typeface.isItalic).isEqualTo(fontStyle == FontStyle.italic)
            }
        }
    }

    @Test
    fun customSingleFont() {
        val context = instrumentation.context
        val resources = context.resources
        val defaultTypeface = TypefaceAdapter().create()

        val typeface = TypefaceAdapter().create(
            fontFamily = Font(name = FONT_100_REGULAR.name).asFontFamily(),
            context = context
        )

        assertThat(typeface).isNotNull()
        Assert.assertThat(typeface.bitmap(), not(equalToBitmap(defaultTypeface.bitmap())))

        assertThat(typeface.isItalic).isFalse()
        assertThat(typeface.isBold).isFalse()
    }

    @Test
    fun customSingleFontBoldItalic() {
        val defaultTypeface = TypefaceAdapter().create()

        val typeface = TypefaceAdapter().create(
            fontFamily = Font(name = FONT_100_REGULAR.name).asFontFamily(),
            context = context,
            fontStyle = FontStyle.italic,
            fontWeight = FontWeight.bold
        )

        assertThat(typeface).isNotNull()
        Assert.assertThat(typeface.bitmap(), not(equalToBitmap(defaultTypeface.bitmap())))
        assertThat(typeface.isItalic).isTrue()
        assertThat(typeface.isBold).isTrue()
    }

    @Test
    fun customSingleFontFamilyExactMatch() {
        val fontFamily = FontFamily(
            FONT_100_REGULAR,
            FONT_100_ITALIC,
            FONT_200_REGULAR,
            FONT_200_ITALIC,
            FONT_300_REGULAR,
            FONT_300_ITALIC,
            FONT_400_REGULAR,
            FONT_400_ITALIC,
            FONT_500_REGULAR,
            FONT_500_ITALIC,
            FONT_600_REGULAR,
            FONT_600_ITALIC,
            FONT_700_REGULAR,
            FONT_700_ITALIC,
            FONT_800_REGULAR,
            FONT_800_ITALIC,
            FONT_900_REGULAR,
            FONT_900_ITALIC
        )

        for (fontWeight in FontWeight.values) {
            for (fontStyle in FontStyle.values()) {
                val typeface = TypefaceAdapter().create(
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    fontFamily = fontFamily,
                    context = context
                )

                assertThat(typeface).isNotNull()
                Assert.assertThat(
                    typeface,
                    isTypefaceOf(fontWeight = fontWeight, fontStyle = fontStyle)
                )
            }
        }
    }

    @Test
    fun fontMatcherCalledForCustomFont() {
        // customSingleFontFamilyExactMatch tests all the possible outcomes that FontMatcher
        // might return. Therefore for the best effort matching we just make sure that FontMatcher
        // is called.
        val fontWeight = FontWeight.w300
        val fontStyle = FontStyle.italic
        val fontFamily = FontFamily(FONT_200_ITALIC)

        val fontMatcher = mock<FontMatcher>()
        whenever(fontMatcher.matchFont(any(), any(), any()))
            .thenReturn(FONT_200_ITALIC)
        TypefaceAdapter(fontMatcher).create(
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontFamily = fontFamily,
            context = context
        )

        verify(fontMatcher, times(1)).matchFont(
            eq(fontFamily),
            eq(fontWeight),
            eq(fontStyle)
        )
    }
}