package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.update.ApkUpdateDownloadProgress
import com.example.data.update.GitHubAutoUpdater
import com.example.data.update.GitHubReleaseInfo
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils

/**
 * Prominent in-app banner notifying the user when a new APK release has been built on GitHub
 * and allowing 1-tap automatic download + installation.
 */
@Composable
fun GitHubUpdateNotificationBanner(
    release: GitHubReleaseInfo,
    currentVersionName: String,
    apkDownloadProgress: ApkUpdateDownloadProgress?,
    onStartAutoUpdate: () -> Unit,
    onInstallReadyApk: () -> Unit,
    onOpenWorkflowDialog: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF10252A), Color(0xFF1A1829))
                )
            )
            .border(
                width = 1.4.dp,
                brush = Brush.horizontalGradient(listOf(EmeraldBadge, ElectricCyan)),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("github_update_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(EmeraldBadgeBg)
                        .border(1.dp, EmeraldBadge, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = EmeraldBadge,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "CÓ BẢN CẬP NHẬT MỚI TỪ GITHUB",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldBadge
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(GoldBadgeBg)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = release.versionTag,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldBadge
                            )
                        }
                    }

                    Text(
                        text = "${release.releaseTitle} • Hiện tại: v$currentVersionName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDismissBanner,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Ẩn thông báo cập nhật",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = release.releaseNotes,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = TextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (apkDownloadProgress != null) {
            val pct = (apkDownloadProgress.progress * 100).toInt().coerceIn(1, 100)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioBackground)
                    .padding(10.dp)
                    .testTag("apk_download_progress_box")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = apkDownloadProgress.statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
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
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldBadge,
                    trackColor = StudioElevatedSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${FormatUtils.formatFileSize(apkDownloadProgress.downloadedBytes)} / ${FormatUtils.formatFileSize(apkDownloadProgress.totalBytes)}",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )

                    if (apkDownloadProgress.isReadyToInstall) {
                        Button(
                            onClick = onInstallReadyApk,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldBadge,
                                contentColor = Color(0xFF001F24)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("install_apk_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.InstallMobile,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mở Trình Cài Đặt APK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartAutoUpdate,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldBadge,
                        contentColor = Color(0xFF001F24)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("auto_update_apk_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cập nhật & Tải APK (${FormatUtils.formatFileSize(release.apkSizeBytes)})",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenWorkflowDialog,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("open_workflow_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Workflow CI/CD",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}

/**
 * Full modal dialog for configuring GitHub repository, viewing the 3-step GitHub Actions workflow,
 * copying `.github/workflows/android-release.yml`, and testing automatic OTA APK updates.
 */
@Composable
fun GitHubWorkflowAndUpdateDialog(
    githubRepoSlug: String,
    autoCheckEnabled: Boolean,
    currentVersionName: String,
    releaseInfo: GitHubReleaseInfo?,
    isCheckingUpdate: Boolean,
    apkDownloadProgress: ApkUpdateDownloadProgress?,
    onUpdateRepoSlug: (String) -> Unit,
    onToggleAutoCheck: (Boolean) -> Unit,
    onCheckLatestRelease: (String) -> Unit,
    onStartAutoUpdate: () -> Unit,
    onInstallReadyApk: () -> Unit,
    onCopyWorkflowYaml: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    var repoInput by remember(githubRepoSlug) { mutableStateOf(githubRepoSlug) }
    var yamlCopied by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(16.dp)
                .testTag("github_workflow_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(StudioSurface)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldBadgeBg)
                                .border(1.dp, EmeraldBadge, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = null,
                                tint = EmeraldBadge,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Tự Động Build APK & Cập Nhật Từ GitHub",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Đã tạo sẵn: .github/workflows/android-release.yml",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = EmeraldBadge
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Đóng",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Repository Configuration & Check Now
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(StudioElevatedSurface)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CẤU HÌNH KHO LƯU TRỮ GITHUB (OWNER/REPO)",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = {
                            repoInput = it
                            onUpdateRepoSlug(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_repo_input"),
                        placeholder = {
                            Text("VD: lamminhnhut/TikStudio-HD", fontSize = 13.sp)
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            color = TextPrimary
                        ),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tự động kiểm tra bản cập nhật khi mở App",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Phiên bản đang cài: v$currentVersionName",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoCheckEnabled,
                            onCheckedChange = onToggleAutoCheck,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF001F24),
                                checkedTrackColor = EmeraldBadge
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCheckLatestRelease(repoInput) },
                            enabled = !isCheckingUpdate,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color(0xFF001F24)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("check_github_release_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCheckingUpdate) "Đang kiểm tra..." else "Kiểm tra Release",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (releaseInfo != null) {
                            Button(
                                onClick = {
                                    if (apkDownloadProgress?.isReadyToInstall == true) {
                                        onInstallReadyApk()
                                    } else {
                                        onStartAutoUpdate()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldBadge,
                                    contentColor = Color(0xFF001F24)
                                ),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .testTag("dialog_download_apk_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (apkDownloadProgress?.isReadyToInstall == true) {
                                        "Cài đặt ${releaseInfo.versionTag}"
                                    } else {
                                        "Tải APK (${releaseInfo.versionTag})"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Step-by-Step Guide (1 -> 2 -> 3)
                Text(
                    text = "QUY TRÌNH 3 BƯỚC TỰ ĐỘNG BUILD & CẬP NHẬT APK",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldBadge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                WorkflowStepCard(
                    stepNumber = "BƯỚC 1",
                    title = "Đẩy code lên GitHub -> Tự động Build APK",
                    description = "Mỗi khi bạn đẩy code (Push) lên nhánh main/master từ AI Studio hoặc Git, GitHub Actions sẽ tự động chạy file .github/workflows/android-release.yml để biên dịch ra file APK."
                )
                Spacer(modifier = Modifier.height(8.dp))
                WorkflowStepCard(
                    stepNumber = "BƯỚC 2",
                    title = "Tự động tạo bản Release & Đính kèm APK",
                    description = "Sau khi lệnh gradle :app:assembleDebug hoàn tất, workflow tự động gắn mã phiên bản v1.0.<run_number> và đăng tệp TikStudio-HD-v1.0.x.apk lên mục GitHub Releases."
                )
                Spacer(modifier = Modifier.height(8.dp))
                WorkflowStepCard(
                    stepNumber = "BƯỚC 3",
                    title = "Mở App -> Báo có bản mới & Tự động tải cài đặt",
                    description = "Khi bạn mở ứng dụng trên điện thoại, GitHubAutoUpdater kiểm tra API Releases mới nhất, hiển thị thông báo cập nhật và tự động tải file APK về mở trình cài đặt Android."
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Workflow YAML Preview & Copy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "NỘI DUNG FILE .github/workflows/android-release.yml",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        onClick = {
                            onCopyWorkflowYaml(GitHubAutoUpdater.WORKFLOW_YAML_TEMPLATE)
                            yamlCopied = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (yamlCopied) EmeraldBadgeBg else StudioElevatedSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (yamlCopied) EmeraldBadge else ElectricCyan
                        ),
                        modifier = Modifier.testTag("copy_workflow_yaml_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (yamlCopied) Icons.Filled.CheckCircle else Icons.Filled.ContentCopy,
                                contentDescription = null,
                                tint = if (yamlCopied) EmeraldBadge else ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (yamlCopied) "Đã sao chép YAML" else "Copy YAML",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (yamlCopied) EmeraldBadge else ElectricCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioBackground)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = GitHubAutoUpdater.WORKFLOW_YAML_TEMPLATE,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkflowStepCard(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioElevatedSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(GoldBadgeBg)
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = stepNumber,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldBadge
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
