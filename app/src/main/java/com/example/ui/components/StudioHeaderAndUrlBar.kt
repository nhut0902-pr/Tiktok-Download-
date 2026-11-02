package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.BatchQueueProgress
import com.example.data.model.SampleTikTokLink
import com.example.data.model.ValidatedThumbnailPreview
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
fun StudioTopHeader(
    hasUpdateAvailable: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricCyan.copy(alpha = 0.2f), NeonCoral.copy(alpha = 0.2f))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(ElectricCyan, NeonCoral)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HighQuality,
                    contentDescription = "TikStudio HD",
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TikStudio HD",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldBadgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "No Logo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldBadge
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("header_version_v103_badge")
                    ) {
                        Text(
                            text = "V1.0.4",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }
                }
                Text(
                    text = "Tải Video, Quay Màn Hình Nền & Hàng Loạt Full HD",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Single Unified Settings & Update Hub Button
        Surface(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            color = StudioSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier
                .size(44.dp)
                .testTag("settings_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Cài đặt & Cập nhật ứng dụng",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                if (hasUpdateAvailable) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-9).dp, y = 9.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldBadge)
                    )
                }
            }
        }
    }
}

@Composable
fun UrlCommandInputCard(
    urlInput: String,
    isUrlValid: Boolean,
    validatedThumbnailPreview: ValidatedThumbnailPreview?,
    isBatchModeExpanded: Boolean,
    batchInputText: String,
    batchQueueProgress: BatchQueueProgress?,
    isInspecting: Boolean,
    sampleLinks: List<SampleTikTokLink> = emptyList(),
    statusMessage: String?,
    isStatusError: Boolean,
    onUrlChange: (String) -> Unit,
    onClearUrl: () -> Unit,
    onPasteClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onSelectSample: (SampleTikTokLink) -> Unit = {},
    onThumbnailPreviewClick: () -> Unit,
    onDownloadCoverClick: () -> Unit,
    onToggleBatchMode: () -> Unit,
    onBatchInputChange: (String) -> Unit,
    onStartBatchDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        // Mode Switcher Bar: Single Link vs Batch Multi-Link Queue + URL Valid Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                onClick = onToggleBatchMode,
                shape = RoundedCornerShape(8.dp),
                color = if (isBatchModeExpanded) ElectricCyan.copy(alpha = 0.16f) else StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isBatchModeExpanded) ElectricCyan else StudioCardBorder
                ),
                modifier = Modifier.testTag("batch_mode_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DynamicFeed,
                        contentDescription = null,
                        tint = if (isBatchModeExpanded) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isBatchModeExpanded) "Đang bật Tải hàng loạt" else "Tải hàng loạt (Batch)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBatchModeExpanded) ElectricCyan else TextSecondary
                    )
                }
            }

            if (isUrlValid) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldBadgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("url_valid_badge")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldBadge,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "URL HỢP LỆ",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldBadge
                    )
                }
            }
        }

        OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_input_field"),
            placeholder = {
                Text(
                    text = "Dán link video hoặc bộ ảnh TikTok...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                color = TextPrimary
            ),
            trailingIcon = {
                if (urlInput.isNotEmpty()) {
                    IconButton(
                        onClick = onClearUrl,
                        modifier = Modifier.testTag("clear_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Xoá đường dẫn",
                            tint = TextSecondary
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onAnalyzeClick() }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = StudioElevatedSurface,
                unfocusedContainerColor = StudioElevatedSurface,
                focusedBorderColor = if (isUrlValid) EmeraldBadge else ElectricCyan,
                unfocusedBorderColor = if (isUrlValid) EmeraldBadge.copy(alpha = 0.5f) else StudioCardBorder,
                cursorColor = ElectricCyan
            )
        )

        // Immediate Coil Thumbnail Preview shown right after URL validation + 1-Tap Save HD Cover
        if (isUrlValid && validatedThumbnailPreview != null) {
            Spacer(modifier = Modifier.height(10.dp))
            ValidatedUrlThumbnailPreviewBox(
                preview = validatedThumbnailPreview,
                onPreviewClick = onThumbnailPreviewClick,
                onDownloadCoverClick = onDownloadCoverClick
            )
        }

        // Expandable Batch Multi-Link Queue Downloader
        AnimatedVisibility(visible = isBatchModeExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(StudioElevatedSurface)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
                    .testTag("batch_queue_panel")
            ) {
                Text(
                    text = "Hàng đợi tải nhiều link cùng lúc (Mỗi dòng 1 link TikTok)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = batchInputText,
                    onValueChange = onBatchInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .testTag("batch_input_field"),
                    placeholder = {
                        Text(
                            text = "https://vt.tiktok.com/...\nhttps://www.tiktok.com/@user/video/...",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioBackground,
                        unfocusedContainerColor = StudioBackground,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (batchQueueProgress != null) {
                    val frac = (batchQueueProgress.currentIndex.toFloat() / batchQueueProgress.totalLinks.coerceAtLeast(1))
                        .coerceIn(0.05f, 1f)
                    Text(
                        text = batchQueueProgress.statusText,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = GoldBadge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { frac },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldBadge,
                        trackColor = StudioBackground
                    )
                } else {
                    Button(
                        onClick = onStartBatchDownload,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldBadge,
                            contentColor = Color(0xFF001F24)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_batch_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlaylistAddCheck,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tải toàn bộ danh sách (Full HD Không Logo)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clean 2-Button Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onPasteClick,
                shape = RoundedCornerShape(12.dp),
                color = StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("paste_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = "Dán link",
                        tint = ElectricCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dán Link",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }

            Button(
                onClick = onAnalyzeClick,
                enabled = !isInspecting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF001F24)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .weight(1.25f)
                    .height(46.dp)
                    .testTag("analyze_button")
            ) {
                if (isInspecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF001F24)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang xử lý...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Phân Tích",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }


        // Subtle Status Feedback
        AnimatedVisibility(
            visible = !statusMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isStatusError) Color(0xFF2D141C) else EmeraldBadgeBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isStatusError) NeonCoral else EmeraldBadge)
                    )
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        color = if (isStatusError) Color(0xFFFFB2C1) else Color(0xFFA7F3D0)
                    )
                }
            }
        }
    }
}

/**
 * Compact, refined Coil thumbnail preview displayed immediately after URL validation,
 * including a 1-tap HD Cover Saver button.
 */
@Composable
fun ValidatedUrlThumbnailPreviewBox(
    preview: ValidatedThumbnailPreview,
    onPreviewClick: () -> Unit,
    onDownloadCoverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioElevatedSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
            .clickable { onPreviewClick() }
            .padding(8.dp)
            .testTag("url_thumbnail_preview"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StudioBackground),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(preview.coverImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = preview.title,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.8.dp,
                            color = ElectricCyan
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("validated_thumbnail_image")
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = preview.authorName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldBadgeBg)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = preview.resolutionBadge,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBadge
                    )
                }
            }

            Text(
                text = preview.title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${preview.authorHandle} • ${FormatUtils.formatDuration(preview.durationSeconds)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = ElectricCyan
            )
        }

        // 1-Tap Save HD Cover Image Button
        Surface(
            onClick = onDownloadCoverClick,
            shape = RoundedCornerShape(8.dp),
            color = StudioSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
            modifier = Modifier.testTag("quick_save_cover_button")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "Lưu ảnh bìa HD",
                    tint = ElectricCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Lưu bìa",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }
    }
}
