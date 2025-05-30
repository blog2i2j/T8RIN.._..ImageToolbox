/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.filters.presentation.model

import androidx.compose.ui.graphics.Color
import com.t8rin.imagetoolbox.core.domain.model.ColorModel
import com.t8rin.imagetoolbox.core.filters.domain.model.Filter
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.toModel


class UiVignetteFilter(
    override val value: Triple<Float, Float, ColorModel> = Triple(
        first = 0.3f,
        second = 0.75f,
        third = Color.Black.toModel()
    ),
) : UiFilter<Triple<Float, Float, ColorModel>>(
    title = R.string.vignette,
    value = value,
    paramsInfo = listOf(
        R.string.start paramTo -2f..2f,
        R.string.end paramTo -2f..2f,
        R.string.color paramTo 0f..0f
    )
), Filter.Vignette