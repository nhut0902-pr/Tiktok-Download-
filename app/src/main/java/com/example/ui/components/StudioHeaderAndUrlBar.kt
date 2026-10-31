package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.SampleTikTokLink
import com.example.data.model.ValidatedThumbnailPreview
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
fun StudioTopHeader(
    defaultQuality: VideoQualityTier,
    hasUpdateAvailable: Boolean,
    onOpenGitHubUpdater: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Dual-tone Neon Studio Emblem
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF172233), Color(0xFF281326))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(ElectricCyan, NeonCoral)),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HighQuality,
                    contentDescription = "Biểu tượng TikStudio HD",
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "TikStudio HD",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldBadgeBg)
                            .border(1.dp, EmeraldBadge.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NO LOGO",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldBadge
                        )
                    }
                }
                Text(
                    text = "Tải Video & Ảnh TikTok Không Logo • HD / Full HD",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // GitHub Auto-Update & CI/CD Pill Button
            Surface(
                onClick = onOpenGitHubUpdater,
                shape = RoundedCornerShape(12.dp),
                color = if (hasUpdateAvailable) EmeraldBadgeBg else StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (hasUpdateAvailable) EmeraldBadge else StudioCardBorder
                ),
                modifier = Modifier.testTag("github_update_header_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudSync,
                        contentDescription = "Cập nhật APK từ GitHub",
                        tint = if (hasUpdateAvailable) EmeraldBadge else ElectricCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = if (hasUpdateAvailable) "UPDATE" else "GITHUB",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (hasUpdateAvailable) EmeraldBadge else TextPrimary
                    )
                }
            }

            // Custom Quality & Studio Settings Pill Button
            Surface(
                onClick = onOpenSettings,
                shape = RoundedCornerShape(12.dp),
                color = StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                modifier = Modifier.testTag("settings_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Tuỳ chỉnh chất lượng tải",
                        tint = ElectricCyan,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = if (defaultQuality == VideoQualityTier.FULL_HD_1080P) "1080P" else "720P",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = GoldBadge
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
    isInspecting: Boolean,
    sampleLinks: List<SampleTikTokLink>,
    statusMessage: String?,
    isStatusError: Boolean,
    onUrlChange: (String) -> Unit,
    onClearUrl: () -> Unit,
    onPasteClick: () -> Unit,
    onAnalyzeClick: () -> Unit,
    onSelectSample: (SampleTikTokLink) -> Unit,
    onThumbnailPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(StudioSurface)
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ElectricCyan.copy(alpha = 0.65f),
                        StudioCardBorder,
                        NeonCoral.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "LIÊN KẾT TIKTOK (VIDEO HOẶC BỘ ẢNH)",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isUrlValid) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldBadgeBg)
                        .border(1.dp, EmeraldBadge.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("url_valid_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldBadgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "HỖ TRỢ 1080P 60FPS",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBadge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_input_field"),
            placeholder = {
                Text(
                    text = "Dán link TikTok (vt.tiktok.com / tiktok.com/@user/video/...)",
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
                unfocusedBorderColor = if (isUrlValid) EmeraldBadge.copy(alpha = 0.6f) else StudioCardBorder,
                cursorColor = ElectricCyan
            )
        )

        // Immediate Coil Thumbnail Preview shown right after URL validation
        if (isUrlValid && validatedThumbnailPreview != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ValidatedUrlThumbnailPreviewBox(
                preview = validatedThumbnailPreview,
                onPreviewClick = onThumbnailPreviewClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Row: Paste from Clipboard + Inspect URL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onPasteClick,
                shape = RoundedCornerShape(12.dp),
                color = StudioElevatedSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("paste_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = "Dán từ bộ nhớ tạm",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                    .height(48.dp)
                    .testTag("analyze_button")
            ) {
                if (isInspecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.2.dp,
                        color = Color(0xFF001F24)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang tách...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Phân Tích HD",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Showcase Sample Chips so user can test Video 1080p vs Photo Carousel immediately
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = GoldBadge,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Thử nhanh mẫu Không Logo:",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleLinks.forEachIndexed { index, sample ->
                val isSelected = urlInput == sample.url
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) ElectricCyan.copy(alpha = 0.16f)
                            else StudioElevatedSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) ElectricCyan else StudioCardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectSample(sample) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("sample_chip_$index")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (sample.isPhotoCarousel) {
                                Icons.Filled.PhotoLibrary
                            } else {
                                Icons.Filled.VideoFile
                            },
                            contentDescription = null,
                            tint = if (sample.isPhotoCarousel) NeonCoral else ElectricCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = sample.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) ElectricCyan else TextPrimary
                        )
                    }
                }
            }
        }

        // Status / Telemetry feedback banner
        AnimatedVisibility(
            visible = !statusMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isStatusError) Color(0xFF36151E) else EmeraldBadgeBg
                        )
                        .border(
                            width = 1.dp,
                            color = if (isStatusError) NeonCoral else EmeraldBadge.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
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
}

/**
 * Displays the TikTok video's cover image immediately after the URL is validated using Coil.
 */
@Composable
fun ValidatedUrlThumbnailPreviewBox(
    preview: ValidatedThumbnailPreview,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioElevatedSurface)
            .border(1.dp, EmeraldBadge.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .clickable { onPreviewClick() }
            .padding(10.dp)
            .testTag("url_thumbnail_preview"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Coil Cover Image Thumbnail with crossfade and loading/error handling
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 96.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StudioBackground)
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(preview.coverImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Ảnh bìa xem trước TikTok: ${preview.title}",
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ElectricCyan
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("validated_thumbnail_image")
            )

            // Subtle dark overlay + play indicator
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )

            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(26.dp)
            )

            // Duration pill at bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = FormatUtils.formatDuration(preview.durationSeconds),
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 9.sp,
                    color = TextPrimary
                )
            }
        }

        // Validated Video / Photo Metadata
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(EmeraldBadgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ẢNH BÌA XEM TRƯỚC",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldBadge
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(GoldBadgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = preview.resolutionBadge,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldBadge
                    )
                }
            }

            Text(
                text = preview.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${preview.authorName} (${preview.authorHandle}) • ID #${preview.videoId.takeLast(6)}",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                color = ElectricCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
