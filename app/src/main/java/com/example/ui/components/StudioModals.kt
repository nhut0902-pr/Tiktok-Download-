package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.VideoQualityTier
import com.example.data.preferences.FileNamingRule
import com.example.data.preferences.StudioSettings
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PreviewModalTarget
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioSettingsBottomSheet(
    settings: StudioSettings,
    onDismiss: () -> Unit,
    onSelectDefaultQuality: (VideoQualityTier) -> Unit,
    onToggleForceNoWatermark: (Boolean) -> Unit,
    onToggleAutoDetectClipboard: (Boolean) -> Unit,
    onToggleSaveToGallery: (Boolean) -> Unit,
    onToggleIncludeMp3WithCarousel: (Boolean) -> Unit,
    onSelectNamingRule: (FileNamingRule) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("settings_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.HighQuality,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Tuỳ chỉnh Tải Không Logo & HD",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Đóng cài đặt",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Default Video Quality Tier
            Text(
                text = "CHẤT LƯỢNG VIDEO MẶC ĐỊNH",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val qualityChoices = listOf(
                VideoQualityTier.FULL_HD_1080P,
                VideoQualityTier.HD_720P
            )
            qualityChoices.forEach { tier ->
                val selected = settings.defaultQualityTier == tier
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) ElectricCyan.copy(alpha = 0.14f) else StudioElevatedSurface)
                        .border(
                            1.dp,
                            if (selected) ElectricCyan else StudioCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectDefaultQuality(tier) }
                        .padding(12.dp)
                        .testTag("setting_quality_${tier.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${tier.shortTitle} (Không Logo)",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = tier.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (selected) ElectricCyan else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = StudioCardBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Watermark & Gallery Switches
            Text(
                text = "CHẾ ĐỘ XỬ LÝ WATERMARK & LƯU TRỮ",
                style = MaterialTheme.typography.labelMedium,
                color = GoldBadge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingSwitchItem(
                title = "Luôn bắt buộc Xoá Logo (No Watermark)",
                subtitle = "Ẩn các luồng chứa ID/Logo mờ của TikTok, chỉ giữ luồng sạch gốc",
                checked = settings.forceNoWatermark,
                onCheckedChange = onToggleForceNoWatermark
            )

            SettingSwitchItem(
                title = "Lưu trực tiếp vào Thư viện (Gallery)",
                subtitle = "Tự động xuất Video vào Movies/TikStudioHD và Ảnh vào Pictures/TikStudioHD",
                checked = settings.saveToPublicGallery,
                onCheckedChange = onToggleSaveToGallery
            )

            SettingSwitchItem(
                title = "Tải kèm nhạc MP3 khi lưu Bộ Ảnh",
                subtitle = "Tự động tải thêm bản nhạc nền 320kbps khi tải TikTok Photo Mode",
                checked = settings.includeMp3WithCarousel,
                onCheckedChange = onToggleIncludeMp3WithCarousel
            )

            SettingSwitchItem(
                title = "Tự động kiểm tra Bộ nhớ tạm (Clipboard)",
                subtitle = "Nhận diện ngay khi bạn vừa sao chép liên kết từ ứng dụng TikTok",
                checked = settings.autoDetectClipboard,
                onCheckedChange = onToggleAutoDetectClipboard
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = StudioCardBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // 3. File Naming Convention
            Text(
                text = "ĐỊNH DẠNG ĐẶT TÊN TỆP TẢI VỀ",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FileNamingRule.entries.forEach { rule ->
                val selected = settings.fileNamingRule == rule
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) ElectricCyan.copy(alpha = 0.14f) else StudioElevatedSurface)
                        .border(
                            1.dp,
                            if (selected) ElectricCyan else StudioCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectNamingRule(rule) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rule.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = rule.example,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = EmeraldBadge
                        )
                    }
                    Icon(
                        imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (selected) ElectricCyan else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF001F24),
                checkedTrackColor = ElectricCyan
            )
        )
    }
}

@Composable
fun StudioMediaPreviewDialog(
    target: PreviewModalTarget,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .padding(16.dp)
                .testTag("media_preview_dialog")
        ) {
            when (target) {
                is PreviewModalTarget.LiveMedia -> {
                    val media = target.media
                    var currentPhotoIdx by remember {
                        mutableIntStateOf(target.initialPhotoIndex.coerceIn(0, (media.photos.size - 1).coerceAtLeast(0)))
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = media.authorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (media.isPhotoSlideshow) {
                                        "Xem ảnh gốc Không Logo (${currentPhotoIdx + 1}/${media.photos.size})"
                                    } else {
                                        "Xem trước luồng Full HD Không Logo"
                                    },
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = ElectricCyan
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StudioElevatedSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng xem trước",
                                    tint = TextPrimary
                                )
                            }
                        }

                        // Center viewport
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(StudioBackground)
                                .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (media.isPhotoSlideshow && media.photos.isNotEmpty()) {
                                val slide = media.photos[currentPhotoIdx]
                                AsyncImage(
                                    model = slide.imageUrl,
                                    contentDescription = "Ảnh số ${slide.index + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Prev / Next navigation arrows
                                if (media.photos.size > 1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                currentPhotoIdx =
                                                    if (currentPhotoIdx > 0) currentPhotoIdx - 1 else media.photos.lastIndex
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.65f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Ảnh trước",
                                                tint = ElectricCyan
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                currentPhotoIdx =
                                                    if (currentPhotoIdx < media.photos.lastIndex) currentPhotoIdx + 1 else 0
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.65f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Ảnh kế tiếp",
                                                tint = ElectricCyan
                                            )
                                        }
                                    }
                                }
                            } else {
                                val streamUrl = media.streams.firstOrNull()?.streamUrl.orEmpty()
                                VideoStreamPlayerBox(
                                    videoUrl = streamUrl,
                                    posterUrl = media.coverUrl
                                )
                            }
                        }

                        // Bottom caption
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(StudioSurface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = media.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KHÔNG LOGO • ${media.sourceEngine}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = EmeraldBadge
                            )
                        }
                    }
                }

                is PreviewModalTarget.SavedDownload -> {
                    val item = target.item
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.qualityLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldBadge
                                )
                                Text(
                                    text = item.localFilePath.substringAfterLast('/'),
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(StudioElevatedSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = TextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(StudioBackground)
                                .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.mediaType == "PHOTO") {
                                val localFile = File(item.localFilePath)
                                AsyncImage(
                                    model = if (localFile.exists()) localFile else item.thumbnailUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                VideoStreamPlayerBox(
                                    videoUrl = item.remoteStreamUrl,
                                    posterUrl = item.thumbnailUrl
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(StudioSurface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Đã lưu tại: ${item.localFilePath}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = EmeraldBadge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoStreamPlayerBox(
    videoUrl: String,
    posterUrl: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (posterUrl.isNotBlank()) {
            AsyncImage(
                model = posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (videoUrl.isNotBlank()) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val controller = MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setVideoURI(Uri.parse(videoUrl))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            start()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
