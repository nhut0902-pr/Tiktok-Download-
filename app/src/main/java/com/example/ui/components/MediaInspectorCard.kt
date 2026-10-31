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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
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
import com.example.ui.theme.GoldBadgeBg
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
        // 1. Author & Watermark Status Strip
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(StudioElevatedSurface)
                        .border(1.5.dp, ElectricCyan, CircleShape)
                ) {
                    AsyncImage(
                        model = media.authorAvatarUrl,
                        contentDescription = "Ảnh đại diện ${media.authorName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = media.authorName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "${media.authorHandle} • ID: ${media.id.takeLast(8)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // No-Watermark Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldBadgeBg)
                    .border(1.dp, EmeraldBadge.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (media.isPhotoSlideshow) "SLIDESHOW • NO LOGO" else "KHÔNG LOGO • HD",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldBadge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Hero Media Frame (Video Cover or Slideshow Preview)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(195.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(StudioBackground)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .clickable { onOpenLivePreview(0) }
                .testTag("preview_media_button")
        ) {
            AsyncImage(
                model = media.coverUrl,
                contentDescription = "Ảnh bìa ${media.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Studio Darkroom Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top-left & Top-right floating badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .border(1.dp, GoldBadge.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (media.isPhotoSlideshow) {
                            "BỘ ${media.photos.size} ẢNH GỐC (1080x1440)"
                        } else {
                            activeStream?.tier?.badgeText ?: "FULL HD 1080P"
                        },
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBadge
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = FormatUtils.formatDuration(media.durationSeconds),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Center Play / Inspect Button
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.68f))
                    .border(
                        width = 1.8.dp,
                        brush = Brush.linearGradient(listOf(ElectricCyan, NeonCoral)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (media.isPhotoSlideshow) Icons.Filled.ZoomIn else Icons.Filled.PlayArrow,
                    contentDescription = "Xem trước",
                    tint = ElectricCyan,
                    modifier = Modifier.size(30.dp)
                )
            }

            // Bottom Caption & Stats Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = FormatUtils.formatCount(media.playCount),
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = NeonCoral,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = FormatUtils.formatCount(media.likeCount),
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = GoldBadge,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = media.musicTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Mode-Specific Selector: Video Quality Tier Selector OR Photo Carousel Multi-Selector
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

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Expandable Custom File Prefix & Stream Technical Specs
        Surface(
            onClick = onToggleCustomOptionsExpanded,
            shape = RoundedCornerShape(12.dp),
            color = StudioElevatedSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("toggle_custom_options_button")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Tuỳ chỉnh tên tệp & Thông số luồng HD",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = activeStream?.codecLabel ?: "JPEG 100% Lossless • Không Logo",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isCustomOptionsExpanded) {
                            Icons.Filled.ExpandLess
                        } else {
                            Icons.Filled.ExpandMore
                        },
                        contentDescription = "Mở rộng tuỳ chỉnh",
                        tint = TextSecondary
                    )
                }

                AnimatedVisibility(visible = isCustomOptionsExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = customOptions.customFilePrefix,
                            onValueChange = onUpdateCustomPrefix,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_prefix_input"),
                            label = {
                                Text(
                                    text = "Tiền tố tên tệp tuỳ chỉnh (Tuỳ chọn)",
                                    fontSize = 12.sp
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "VD: TikTok_Review_1080p",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = StudioSurface,
                                unfocusedContainerColor = StudioSurface,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = StudioCardBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Engine: ${media.sourceEngine}",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    color = EmeraldBadge
                                )
                                activeStream?.let { s ->
                                    Text(
                                        text = "Bitrate: ${s.bitrateKbps} kbps • Độ phân giải: ${s.tier.resolution}",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy Link Gốc",
                                    fontSize = 12.sp,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Active Download Telemetry Bar OR Primary Download Button
        if (activeDownload != null) {
            ActiveDownloadTelemetryBox(progress = activeDownload)
        } else {
            val buttonLabel = if (media.isPhotoSlideshow) {
                val count = customOptions.selectedPhotoIndices.size
                val audioSuffix = if (customOptions.includeAudioWithPhotos) " + Nhạc MP3" else ""
                "Tải $count Ảnh HD Không Logo$audioSuffix"
            } else {
                val tier = activeStream?.tier ?: VideoQualityTier.FULL_HD_1080P
                val sizeStr = FormatUtils.formatFileSize(activeStream?.fileSizeBytes ?: 16_000_000L)
                val logoSuffix = if (tier.isNoWatermark) "Không Logo" else "Có Logo"
                "Tải ${tier.shortTitle} ($logoSuffix) • $sizeStr"
            }

            Button(
                onClick = onStartDownload,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF001F24)
                ),
                contentPadding = PaddingValues(vertical = 15.dp, horizontal = 18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("download_primary_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001F24)
                )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TUỲ CHỈNH CHẤT LƯỢNG TẢI (NO WATERMARK)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Chuẩn H.264 / MP3",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = ElectricCyan
            )
        }

        streams.forEach { option ->
            val isSelected = option.tier == selectedTier
            val isFullHd = option.tier == VideoQualityTier.FULL_HD_1080P
            val isAudio = option.tier == VideoQualityTier.AUDIO_MP3

            val accentColor = when {
                isFullHd -> GoldBadge
                isAudio -> NeonCoral
                else -> ElectricCyan
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.12f)
                        else StudioElevatedSurface
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) accentColor else StudioCardBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectTier(option.tier) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("quality_option_${option.tier.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            modifier = Modifier.size(22.dp)
                        )

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = option.tier.shortTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = option.tier.badgeText,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = option.tier.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isFullHd -> Icons.Filled.HighQuality
                                    isAudio -> Icons.Filled.Audiotrack
                                    else -> Icons.Filled.Hd
                                },
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = FormatUtils.formatFileSize(option.fileSizeBytes),
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = if (option.fps > 0) "${option.tier.resolution} • ${option.fps}fps" else "320 kbps",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
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
            Column {
                Text(
                    text = "CHỌN ẢNH TRONG BỘ SLIDESHOW (${selectedIndices.size}/${photos.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Chất lượng gốc 1080x1440 • Đã xoá watermark TikTok",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            val allSelected = selectedIndices.size == photos.size
            Surface(
                onClick = { if (allSelected) onDeselectAll() else onSelectAll() },
                shape = RoundedCornerShape(8.dp),
                color = StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("select_all_photos_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelectAll,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (allSelected) "Bỏ chọn" else "Chọn tất cả",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(photos, key = { it.index }) { photo ->
                val isSelected = selectedIndices.contains(photo.index)
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(156.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StudioElevatedSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ElectricCyan else StudioCardBorder,
                            shape = RoundedCornerShape(14.dp)
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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )

                    // Top row: slide number & checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${photo.index + 1}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
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
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Bottom row: zoom preview & resolution
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = FormatUtils.formatFileSize(photo.estimatedSizeBytes),
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = TextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { onPreviewPhoto(photo.index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ZoomIn,
                                contentDescription = "Phóng to ảnh",
                                tint = ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Toggle to also download original background MP3 with the photo slideshow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioElevatedSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Audiotrack,
                    contentDescription = null,
                    tint = NeonCoral,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Tải kèm nhạc nền gốc MP3 (320kbps)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Lưu thêm tệp âm thanh gốc của bộ ảnh",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
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
            .border(1.dp, ElectricCyan.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .padding(14.dp)
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
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                text = "$pct% • ${FormatUtils.formatSpeed(progress.speedKbps)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBadge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress.progress.coerceIn(0.02f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = ElectricCyan,
            trackColor = StudioBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progress.taskTitle,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${FormatUtils.formatFileSize(progress.downloadedBytes)} / ${FormatUtils.formatFileSize(progress.totalBytes)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = TextPrimary
            )
        }
    }
}
