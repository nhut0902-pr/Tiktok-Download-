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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.update.ApkUpdateDownloadProgress
import com.example.data.update.GitHubAutoUpdater
import com.example.data.update.GitHubReleaseInfo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldBadge
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioElevatedSurface
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PreviewModalTarget
import com.example.util.FormatUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioSettingsBottomSheet(
    settings: StudioSettings,
    currentVersionName: String,
    releaseInfo: GitHubReleaseInfo?,
    isCheckingUpdate: Boolean,
    apkDownloadProgress: ApkUpdateDownloadProgress?,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onSelectDefaultQuality: (VideoQualityTier) -> Unit,
    onToggleForceNoWatermark: (Boolean) -> Unit,
    onToggleAutoDetectClipboard: (Boolean) -> Unit,
    onToggleSaveToGallery: (Boolean) -> Unit,
    onToggleIncludeMp3WithCarousel: (Boolean) -> Unit,
    onSelectNamingRule: (FileNamingRule) -> Unit,
    onUpdateRepoSlug: (String) -> Unit,
    onToggleAutoCheckUpdate: (Boolean) -> Unit,
    onCheckLatestRelease: (String) -> Unit,
    onStartAutoUpdate: () -> Unit,
    onInstallReadyApk: () -> Unit,
    onCopyWorkflowYaml: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var repoInput by remember(settings.githubRepoSlug) { mutableStateOf(settings.githubRepoSlug) }
    var showYamlCode by remember { mutableStateOf(false) }
    var yamlCopied by remember { mutableStateOf(false) }

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
            // Header
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
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Cài đặt & Cập nhật ứng dụng",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Clean 2-Tab Segmented Switcher inside Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioBackground)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    0 to "Chất lượng tải",
                    1 to "Cập nhật & GitHub"
                )
                tabs.forEach { (idx, title) ->
                    val active = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (active) StudioElevatedSurface else Color.Transparent)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 9.dp)
                            .testTag("settings_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 13.sp,
                                color = if (active) ElectricCyan else TextSecondary
                            )
                            if (idx == 1 && releaseInfo?.isNewerThanCurrent == true) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldBadge)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // TAB 0: Quality & Download Preferences
                Text(
                    text = "Chất lượng Video mặc định",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    color = TextPrimary
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
                            .background(if (selected) ElectricCyan.copy(alpha = 0.12f) else StudioElevatedSurface)
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
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
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

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = StudioCardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                SettingSwitchItem(
                    title = "Luôn xoá Logo (No Watermark)",
                    subtitle = "Chỉ hiển thị các luồng video/ảnh sạch không dính ID",
                    checked = settings.forceNoWatermark,
                    onCheckedChange = onToggleForceNoWatermark
                )

                SettingSwitchItem(
                    title = "Tự động dán link từ Clipboard",
                    subtitle = "Tự động phát hiện link TikTok ngay khi mở ứng dụng",
                    checked = settings.autoDetectClipboard,
                    onCheckedChange = onToggleAutoDetectClipboard
                )

                SettingSwitchItem(
                    title = "Lưu trực tiếp vào Thư viện (Gallery)",
                    subtitle = "Tự động xuất vào thư mục Movies/TikStudioHD & Pictures",
                    checked = settings.saveToPublicGallery,
                    onCheckedChange = onToggleSaveToGallery
                )

                SettingSwitchItem(
                    title = "Tải kèm nhạc MP3 cùng Bộ Ảnh",
                    subtitle = "Lưu thêm âm thanh gốc khi tải ảnh Slideshow",
                    checked = settings.includeMp3WithCarousel,
                    onCheckedChange = onToggleIncludeMp3WithCarousel
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = StudioCardBorder)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quy tắc đặt tên tệp",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FileNamingRule.entries.forEach { rule ->
                    val selected = settings.fileNamingRule == rule
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) ElectricCyan.copy(alpha = 0.12f) else StudioElevatedSurface)
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
                                style = MaterialTheme.typography.bodyMedium,
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
            } else {
                // TAB 1: App Update & GitHub CI/CD (Version 1.0.2 & Gradle 9.3.1)
                val targetVersion = releaseInfo?.versionTag ?: GitHubAutoUpdater.TARGET_UPDATE_VERSION
                val isUpToDate = releaseInfo?.isNewerThanCurrent == false || currentVersionName == targetVersion

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StudioElevatedSurface)
                        .border(
                            1.dp,
                            if (isUpToDate) EmeraldBadge.copy(alpha = 0.6f) else ElectricCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                        .testTag("github_workflow_dialog")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUpToDate) Icons.Filled.CheckCircle else Icons.Filled.SystemUpdate,
                                contentDescription = null,
                                tint = EmeraldBadge,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isUpToDate) {
                                    "Đã cập nhật bản mới nhất ($targetVersion)"
                                } else {
                                    "Bản cập nhật TikStudio HD $targetVersion"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isUpToDate) {
                                    "Phiên bản hiện tại: $currentVersionName • Đã cập nhật hoàn tất"
                                } else {
                                    "Hiện tại: $currentVersionName • Bản cập nhật: $targetVersion"
                                },
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = EmeraldBadge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (releaseInfo != null) {
                        Text(
                            text = releaseInfo.releaseNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (apkDownloadProgress != null && !apkDownloadProgress.isReadyToInstall) {
                        val pct = (apkDownloadProgress.progress * 100).toInt().coerceIn(1, 100)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioBackground)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = apkDownloadProgress.statusText,
                                    fontSize = 12.sp,
                                    color = ElectricCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$pct%",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBadge
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { apkDownloadProgress.progress.coerceIn(0.02f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmeraldBadge,
                                trackColor = StudioSurface
                            )
                        }
                    } else if (isUpToDate) {
                        // Already updated state — never nag the user to update again!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldBadgeBg)
                                .padding(10.dp)
                                .testTag("update_completed_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldBadge,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Bạn đang dùng bản $targetVersion mới nhất. Đã hoàn tất cập nhật!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFA7F3D0)
                                )
                            }

                            if (apkDownloadProgress?.isReadyToInstall == true) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    onClick = onInstallReadyApk,
                                    shape = RoundedCornerShape(8.dp),
                                    color = StudioSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBadge.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.InstallMobile,
                                            contentDescription = null,
                                            tint = EmeraldBadge,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Mở trình cài đặt APK ($targetVersion)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldBadge
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = onStartAutoUpdate,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldBadge,
                                contentColor = Color(0xFF001F24)
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auto_update_apk_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cập nhật & Tải APK tự động ($targetVersion)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Repository Configuration
                Text(
                    text = "Kho lưu trữ GitHub (owner/repo)",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = {
                            repoInput = it
                            onUpdateRepoSlug(it)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("github_repo_input"),
                        placeholder = {
                            Text("VD: user/Tiktok-Download-", fontSize = 12.sp)
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            color = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = StudioElevatedSurface,
                            unfocusedContainerColor = StudioElevatedSurface,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = StudioCardBorder
                        )
                    )

                    Surface(
                        onClick = { onCheckLatestRelease(repoInput) },
                        shape = RoundedCornerShape(10.dp),
                        color = StudioElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("check_github_release_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Kiểm tra",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isCheckingUpdate) "..." else "Kiểm tra",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SettingSwitchItem(
                    title = "Tự động báo khi có bản Release mới",
                    subtitle = "Kiểm tra GitHub Releases mỗi khi khởi động ứng dụng",
                    checked = settings.autoCheckGitHubUpdate,
                    onCheckedChange = onToggleAutoCheckUpdate
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Expandable Workflow YAML Guide (Fixed for Gradle 9.3.1 & JDK 21)
                Surface(
                    onClick = { showYamlCode = !showYamlCode },
                    shape = RoundedCornerShape(12.dp),
                    color = StudioElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
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
                                    imageVector = Icons.Filled.Code,
                                    contentDescription = null,
                                    tint = GoldBadge,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Workflow GitHub Actions (Chuẩn Gradle 9.3.1)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = ".github/workflows/android-release.yml • Nhấn để xem/copy",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    onCopyWorkflowYaml(GitHubAutoUpdater.WORKFLOW_YAML_TEMPLATE)
                                    yamlCopied = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (yamlCopied) EmeraldBadgeBg else StudioSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (yamlCopied) EmeraldBadge else ElectricCyan
                                ),
                                modifier = Modifier.testTag("copy_workflow_yaml_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = null,
                                        tint = if (yamlCopied) EmeraldBadge else ElectricCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (yamlCopied) "Đã copy" else "Copy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (yamlCopied) EmeraldBadge else ElectricCyan
                                    )
                                }
                            }
                        }

                        if (showYamlCode) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StudioBackground)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = GitHubAutoUpdater.WORKFLOW_YAML_TEMPLATE,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = Color(0xFFA7F3D0)
                                )
                            }
                        }
                    }
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
                fontSize = 14.sp,
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
                                        "Ảnh gốc Không Logo (${currentPhotoIdx + 1}/${media.photos.size})"
                                    } else {
                                        "Luồng Full HD Không Logo"
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
