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

package com.t8rin.imagetoolbox.feature.erase_background.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.t8rin.imagetoolbox.feature.erase_background.domain.AutoBackgroundRemover
import com.t8rin.imagetoolbox.feature.erase_background.domain.AutoBackgroundRemoverBackend
import com.t8rin.logger.makeLog
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

internal class AndroidAutoBackgroundRemover @Inject constructor(
    private val backend: AutoBackgroundRemoverBackend<Bitmap>
) : AutoBackgroundRemover<Bitmap> {

    override suspend fun trimEmptyParts(
        image: Bitmap
    ): Bitmap = coroutineScope {
        async {
            var firstX = 0
            var firstY = 0
            var lastX = image.width
            var lastY = image.height
            val pixels = IntArray(image.width * image.height)
            image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
            loop@ for (x in 0 until image.width) {
                for (y in 0 until image.height) {
                    if (pixels[x + y * image.width] != Color.Transparent.toArgb()) {
                        firstX = x
                        break@loop
                    }
                }
            }
            loop@ for (y in 0 until image.height) {
                for (x in firstX until image.width) {
                    if (pixels[x + y * image.width] != Color.Transparent.toArgb()) {
                        firstY = y
                        break@loop
                    }
                }
            }
            loop@ for (x in image.width - 1 downTo firstX) {
                for (y in image.height - 1 downTo firstY) {
                    if (pixels[x + y * image.width] != Color.Transparent.toArgb()) {
                        lastX = x
                        break@loop
                    }
                }
            }
            loop@ for (y in image.height - 1 downTo firstY) {
                for (x in image.width - 1 downTo firstX) {
                    if (pixels[x + y * image.width] != Color.Transparent.toArgb()) {
                        lastY = y
                        break@loop
                    }
                }
            }
            return@async Bitmap.createBitmap(image, firstX, firstY, lastX - firstX, lastY - firstY)
        }.await()
    }

    override fun removeBackgroundFromImage(
        image: Bitmap,
        onSuccess: (Bitmap) -> Unit,
        onFailure: (Throwable) -> Unit
    ) = backend.performBackgroundRemove(
        image = image,
        onFinish = { result ->
            result
                .onSuccess(onSuccess)
                .onFailure {
                    it.makeLog()
                    onFailure(it)
                }
        }
    )

}