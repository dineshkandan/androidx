/*
 * Copyright 2019 The Android Open Source Project
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

package androidx.ui.core

import android.content.Context
import androidx.ui.engine.geometry.Rect
import androidx.ui.painting.Canvas

/**
 * A density of the screen. Used for convert [Dp] to pixels.
 */
// TODO(Andrey): Mark it as inline when it will work stable for us.
/*inline*/ data class Density(val value: Float)

/**
 * Creates a [Density] from this [Context]
 *
 */
// TODO(Andrey): Move to android specific module
fun Density(context: Context): Density =
    Density(context.resources.displayMetrics.density)

/**
 * If you have a [Density] object and you want to perform some conversions use this.
 *
 * Usage examples:
 *
 *     withDensity(density) {
 *        drawWithHeight(canvas, DpHeight.toPx())
 *     }
 *
 * or
 *
 *     val pxHeight = withDensity(density) { DpHeight.toPx() }
 *
 */
// can't make this inline as tests are failing with "DensityKt.$jacocoInit()' is inaccessible"
/*inline*/ fun <R> withDensity(density: Density, block: DensityReceiver.() -> R) =
    DensityReceiverImpl(density).block()

/**
 * Used to add density resolution logic within a receiver scope.
 *
 * @see [withDensity] for a simple usage
 */
interface DensityReceiver {

    /**
     * A [Density] object. Useful if you need to pass it as a param.
     */
    val density: Density

    /**
     * Convert [Dp] to [Px]. Pixels are used to paint to [Canvas].
     */
    fun Dp.toPx(): Px = Px(value * density.value)

    /**
     * Convert [Dp] to [IntPx] by rounding
     */
    fun Dp.toIntPx(): IntPx = toPx().round()

    /**
     * Convert [Px] to [Dp].
     */
    fun Px.toDp(): Dp = (value / density.value).dp

    /** Convert a [Float] pixel value to a Dp */
    fun Float.toDp(): Dp = (this / density.value).dp

    /** Convert a [Float] pixel value to a Dp */
    fun Int.toDp(): Dp = toFloat().toDp()

    /**
     * Convert a [Size] to a [PxSize].
     */
    fun Size.toPx(): PxSize =
        PxSize(width.toPx(), height.toPx())

    /**
     * Convert a [Bounds] to a [Rect].
     */
    fun Bounds.toRect(): Rect {
        return Rect(
            left.toPx().value,
            top.toPx().value,
            right.toPx().value,
            bottom.toPx().value
        )
    }
}

/**
 * A simple implementation for [DensityReceiver].
 */
class DensityReceiverImpl(override val density: Density) : DensityReceiver
