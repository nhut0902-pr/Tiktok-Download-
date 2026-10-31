package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 MB"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1.0) {
            String.format(Locale.US, "%.1f MB", mb)
        } else {
            val kb = bytes / 1024.0
            String.format(Locale.US, "%.0f KB", kb)
        }
    }

    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatSpeed(speedKbps: Int): String {
        return if (speedKbps >= 1024) {
            String.format(Locale.US, "%.1f MB/s", speedKbps / 1024.0)
        } else {
            "$speedKbps KB/s"
        }
    }

    fun formatRelativeTime(timestampMs: Long): String {
        val diff = System.currentTimeMillis() - timestampMs
        val minutes = diff / 60_000L
        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            minutes < 1440 -> "${minutes / 60} giờ trước"
            else -> SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestampMs))
        }
    }
}
