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

package ru.tech.imageresizershrinker.feature.single_edit.presentation.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import com.smarttoolfactory.cropper.settings.CropProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.model.DomainAspectRatio
import ru.tech.imageresizershrinker.core.domain.utils.notNullAnd
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.CropSmall
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.image.AspectRatioSelector
import ru.tech.imageresizershrinker.core.ui.widget.other.EnhancedTopAppBar
import ru.tech.imageresizershrinker.core.ui.widget.other.EnhancedTopAppBarType
import ru.tech.imageresizershrinker.core.ui.widget.other.Loading
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.feature.crop.presentation.components.CropMaskSelection
import ru.tech.imageresizershrinker.feature.crop.presentation.components.Cropper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditOption(
    visible: Boolean,
    onDismiss: () -> Unit,
    useScaffold: Boolean,
    bitmap: Bitmap?,
    onGetBitmap: (Bitmap) -> Unit,
    cropProperties: CropProperties,
    selectedAspectRatio: DomainAspectRatio,
    setCropAspectRatio: (DomainAspectRatio, AspectRatio) -> Unit,
    setCropMask: (CropOutlineProperty) -> Unit,
    loadImage: suspend (Uri) -> Bitmap?
) {
    val scope = rememberCoroutineScope()
    bitmap?.let {
        var crop by remember(visible) { mutableStateOf(false) }
        var stateBitmap by remember(bitmap, visible) { mutableStateOf(bitmap) }
        FullscreenEditOption(
            canGoBack = stateBitmap == bitmap,
            visible = visible,
            onDismiss = onDismiss,
            useScaffold = useScaffold,
            controls = { scaffoldState ->
                val focus = LocalFocusManager.current
                LaunchedEffect(scaffoldState?.bottomSheetState?.currentValue, focus) {
                    val current = scaffoldState?.bottomSheetState?.currentValue
                    if (current.notNullAnd { it != SheetValue.Expanded }) {
                        focus.clearFocus()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                AspectRatioSelector(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    selectedAspectRatio = selectedAspectRatio,
                    onAspectRatioChange = setCropAspectRatio
                )
                Spacer(modifier = Modifier.height(8.dp))
                CropMaskSelection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onCropMaskChange = setCropMask,
                    selectedItem = cropProperties.cropOutlineProperty,
                    loadImage = {
                        loadImage(it)?.asImageBitmap()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            },
            fabButtons = {
                var job by remember { mutableStateOf<Job?>(null) }
                EnhancedFloatingActionButton(
                    onClick = {
                        job?.cancel()
                        job = scope.launch {
                            delay(500)
                            crop = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CropSmall,
                        contentDescription = stringResource(R.string.crop)
                    )
                }
            },
            actions = {},
            topAppBar = { closeButton ->
                EnhancedTopAppBar(
                    type = EnhancedTopAppBarType.Center,
                    navigationIcon = closeButton,
                    actions = {
                        AnimatedVisibility(visible = stateBitmap != bitmap) {
                            EnhancedIconButton(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                onClick = {
                                    onGetBitmap(stateBitmap)
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Done,
                                    contentDescription = "Done"
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.crop),
                            modifier = Modifier.marquee()
                        )
                    }
                )
            }
        ) {
            var loading by remember { mutableStateOf(false) }
            Box(contentAlignment = Alignment.Center) {
                Cropper(
                    bitmap = stateBitmap,
                    crop = crop,
                    onImageCropStarted = { loading = true },
                    onImageCropFinished = {
                        if (it != null) {
                            stateBitmap = it
                            scope.launch {
                                delay(500)
                                loading = false
                            }
                        } else {
                            loading = false
                        }
                        crop = false
                    },
                    cropProperties = cropProperties
                )
                AnimatedVisibility(
                    visible = loading,
                    modifier = Modifier.fillMaxSize(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(0.5f))
                    ) {
                        Loading()
                    }
                }
            }
        }
    }
}