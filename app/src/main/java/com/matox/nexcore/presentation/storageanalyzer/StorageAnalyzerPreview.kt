package com.matox.nexcore.presentation.storageanalyzer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.matox.nexcore.domain.model.LargeFileEntry
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.domain.model.StorageBreakdown
import com.matox.nexcore.domain.model.StorageCategory
import com.matox.nexcore.domain.model.StorageInsights
import com.matox.nexcore.presentation.storageanalyzer.state.StorageAnalyzerUiState
import com.matox.nexcore.ui.theme.NexCoreTheme

private val PreviewSnapshot = StorageBreakdown(
    internalUsedGb = 128f,
    internalTotalGb = 256f,
    categories = listOf(
        StorageCategory("cat_images", "Images", 45f, 17, MetricAccent.PINK),
        StorageCategory("cat_videos", "Videos", 30f, 12, MetricAccent.PURPLE),
        StorageCategory("cat_apps", "Apps", 20f, 8, MetricAccent.BLUE),
        StorageCategory("cat_documents", "Documents", 8f, 3, MetricAccent.ORANGE),
        StorageCategory("cat_audio", "Audio", 5f, 2, MetricAccent.CYAN),
        StorageCategory("cat_downloads", "Downloads", 6f, 2, MetricAccent.TEAL),
        StorageCategory("cat_others", "Others", 14f, 5, MetricAccent.GREEN),
    ),
    largeFiles = listOf(
        LargeFileEntry("lf_1", "Nature Documentary.mp4", 4.8f, MetricAccent.PURPLE),
        LargeFileEntry("lf_2", "Game_Pro_v2.1.apk", 2.3f, MetricAccent.GREEN, isApk = true),
        LargeFileEntry("lf_3", "Backup_2026_06_01.zip", 1.7f, MetricAccent.ORANGE),
        LargeFileEntry("lf_4", "City Tour 4K.mp4", 1.2f, MetricAccent.PINK),
    ),
    insights = StorageInsights(
        largeFilesCount = 12,
        largeFilesGb = 23.8f,
        duplicateCount = 45,
        duplicateGb = 6.7f,
        emptyFolders = 18,
        oldFilesCount = 67,
        oldFilesGb = 12.3f,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0B1220, widthDp = 412, heightDp = 920)
@Composable
fun StorageAnalyzerPreview() {
    NexCoreTheme {
        StorageAnalyzerContent(
            state = StorageAnalyzerUiState.Success(PreviewSnapshot),
            onBack = {},
            onCategoryClick = {},
            onInsightClick = {},
            onLargeFileClick = {},
            onSmartCleanClick = {},
            onBottomNavClick = {},
        )
    }
}