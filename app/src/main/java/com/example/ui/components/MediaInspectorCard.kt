package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ActiveDownloadProgress
import com.example.data.model.CustomDownloadOptions
import com.example.data.model.PhotoSlideItem
import com.example.data.model.QualityStreamOption
import com.example.data.model.TikTokMediaInfo
import com.example.data.model.VideoQualityTier
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

@Composable
fun MediaInspectorCard(
    media: TikTokMediaInfo,
    customOptions: CustomDownloadOptions,
    forceNoWatermark: Boolean,
    isCustomOptionsExpanded: Boolean,
    activeDownload: ActiveDownloadProgress?,
    onSelectTier: (VideoQualityTier) -> Unit,
    onTogglePhotoIndex: (Int) -> Unit,
    onSelectAllPhotos: () -> Unit,
    onDeselectAllPhotos: () -> Unit,
    onToggleIncludeAudioWithPhotos: () -> Unit,
    onToggleCustomOptionsExpanded: () -> Unit,
    onUpdateCustomPrefix: (String) -> Unit,
    onCopyDirectUrl: (String) -> Unit,
    onOpenLivePreview: (Int) -> Unit,
    onStartDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleStreams = if (forceNoWatermark) {
        media.streams.filter { it.tier.isNoWatermark }
    } else {
        media.streams
    }

    val activeStream: QualityStreamOption? = visibleStreams.find {
        it.tier == customOptions.selectedTier
    } ?: visibleStreams.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
            .testTag("media_inspector_card")
    ) {
        // 1. Clean Author Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = media.authorAvatarUrl,
                    contentDescription = media.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(StudioElevatedSurface)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = media.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = media.authorHandle,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldBadgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (media.isPhotoSlideshow) "BỘ ẢNH • KHÔNG LOGO" else "KHÔNG LOGO",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldBadge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Refined Cinema Cover Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(StudioBackground)
                .clickable { onOpenLivePreview(0) }
                .testTag("preview_media_button")
        ) {
            AsyncImage(
                model = media.coverUrl,
                contentDescription = media.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )

            // Duration Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = FormatUtils.formatDuration(media.durationSeconds),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    color = TextPrimary
                )
            }

            // Center Play Icon
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.5.dp, ElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (media.isPhotoSlideshow) Icons.Filled.ZoomIn else Icons.Filled.PlayArrow,
                    contentDescription = "Xem trước",
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Caption at bottom
            Text(
                text = media.title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Clean Quality Selector OR Photo Carousel Grid
        if (media.isPhotoSlideshow) {
            PhotoSlideshowSelectorSection(
                photos = media.photos,
                selectedIndices = customOptions.selectedPhotoIndices,
                includeAudioMp3 = customOptions.includeAudioWithPhotos,
                onTogglePhoto = onTogglePhotoIndex,
                onSelectAll = onSelectAllPhotos,
                onDeselectAll = onDeselectAllPhotos,
                onToggleIncludeAudio = onToggleIncludeAudioWithPhotos,
                onPreviewPhoto = onOpenLivePreview
            )
        } else {
            VideoQualityTierSelectorSection(
                streams = visibleStreams,
                selectedTier = customOptions.selectedTier,
                onSelectTier = onSelectTier
            )
        }

        // Expandable Custom File Name Prefix (Only shown when user taps the Edit icon)
        AnimatedVisibility(visible = isCustomOptionsExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedTextField(
                    value = customOptions.customFilePrefix,
                    onValueChange = onUpdateCustomPrefix,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_prefix_input"),
                    placeholder = {
                        Text(
                            text = "Nhập tiền tố tên tệp (VD: TikTok_1080p)...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioElevatedSurface,
                        unfocusedContainerColor = StudioElevatedSurface,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeStream?.codecLabel ?: "JPEG Lossless • Không Logo",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = {
                            val targetUrl = activeStream?.streamUrl
                                ?: media.photos.firstOrNull()?.imageUrl
                                ?: media.originalUrl
                            onCopyDirectUrl(targetUrl)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy link gốc",
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Primary Download Button + Inline Customize Filename Button
        if (activeDownload != null) {
            ActiveDownloadTelemetryBox(progress = activeDownload)
        } else {
            val buttonLabel = if (media.isPhotoSlideshow) {
                val count = customOptions.selectedPhotoIndices.size
                val audioSuffix = if (customOptions.includeAudioWithPhotos) " + MP3" else ""
                "Tải $count Ảnh HD$audioSuffix"
            } else {
                val tier = activeStream?.tier ?: VideoQualityTier.FULL_HD_1080P
                val sizeStr = FormatUtils.formatFileSize(activeStream?.fileSizeBytes ?: 16_000_000L)
                "Tải ${tier.shortTitle} • $sizeStr"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartDownload,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF001F24)
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_primary_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buttonLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001F24)
                    )
                }

                Surface(
                    onClick = onToggleCustomOptionsExpanded,
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCustomOptionsExpanded) ElectricCyan.copy(alpha = 0.16f) else StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCustomOptionsExpanded) ElectricCyan else StudioCardBorder
                    ),
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("toggle_custom_options_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Đặt tên tệp tuỳ chỉnh",
                            tint = if (isCustomOptionsExpanded) ElectricCyan else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoQualityTierSelectorSection(
    streams: List<QualityStreamOption>,
    selectedTier: VideoQualityTier,
    onSelectTier: (VideoQualityTier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Chọn chất lượng tải về",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )

        streams.forEach { option ->
            val isSelected = option.tier == selectedTier
            val isFullHd = option.tier == VideoQualityTier.FULL_HD_1080P
            val isAudio = option.tier == VideoQualityTier.AUDIO_MP3

            val accentColor = when {
                isFullHd -> GoldBadge
                isAudio -> NeonCoral
                else -> ElectricCyan
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.1f)
                        else StudioElevatedSurface
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) accentColor else StudioCardBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectTier(option.tier) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("quality_option_${option.tier.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isSelected) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = if (isSelected) accentColor else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = option.tier.shortTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (option.tier.isNoWatermark) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (isFullHd) "60FPS" else if (isAudio) "320K" else "HD",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                        Text(
                            text = option.tier.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = FormatUtils.formatFileSize(option.fileSizeBytes),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun PhotoSlideshowSelectorSection(
    photos: List<PhotoSlideItem>,
    selectedIndices: Set<Int>,
    includeAudioMp3: Boolean,
    onTogglePhoto: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onToggleIncludeAudio: () -> Unit,
    onPreviewPhoto: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Đã chọn ${selectedIndices.size}/${photos.size} ảnh HD",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            val allSelected = selectedIndices.size == photos.size
            Surface(
                onClick = { if (allSelected) onDeselectAll() else onSelectAll() },
                shape = RoundedCornerShape(8.dp),
                color = StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                modifier = Modifier.testTag("select_all_photos_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelectAll,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (allSelected) "Bỏ chọn" else "Chọn tất cả",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(photos, key = { it.index }) { photo ->
                val isSelected = selectedIndices.contains(photo.index)
                Box(
                    modifier = Modifier
                        .width(108.dp)
                        .height(142.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioElevatedSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ElectricCyan else StudioCardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTogglePhoto(photo.index) }
                        .testTag("photo_slide_${photo.index}")
                ) {
                    AsyncImage(
                        model = photo.imageUrl,
                        contentDescription = "Ảnh số ${photo.index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "#${photo.index + 1}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                color = TextPrimary
                            )
                        }

                        Icon(
                            imageVector = if (isSelected) {
                                Icons.Filled.CheckCircle
                            } else {
                                Icons.Filled.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = if (isSelected) ElectricCyan else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { onPreviewPhoto(photo.index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Phóng to",
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioElevatedSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Audiotrack,
                    contentDescription = null,
                    tint = NeonCoral,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Tải kèm nhạc nền MP3 (320kbps)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }

            Switch(
                checked = includeAudioMp3,
                onCheckedChange = { onToggleIncludeAudio() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF001F24),
                    checkedTrackColor = ElectricCyan
                )
            )
        }
    }
}

@Composable
private fun ActiveDownloadTelemetryBox(
    progress: ActiveDownloadProgress
) {
    val pct = (progress.progress * 100).toInt().coerceIn(1, 100)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioElevatedSurface)
            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("active_download_box")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progress.statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ElectricCyan
            )
            Text(
                text = "$pct% • ${FormatUtils.formatSpeed(progress.speedKbps)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBadge
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress.progress.coerceIn(0.02f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ElectricCyan,
            trackColor = StudioBackground
        )
    }
}
