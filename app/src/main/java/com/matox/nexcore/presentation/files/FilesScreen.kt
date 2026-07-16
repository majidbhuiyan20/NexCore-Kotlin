package com.matox.nexcore.presentation.files

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matox.nexcore.core.util.AppContainer
import com.matox.nexcore.domain.model.BottomNavIcon
import com.matox.nexcore.domain.model.BottomNavItem
import com.matox.nexcore.domain.model.MetricAccent
import com.matox.nexcore.presentation.dashboard.components.DashboardBottomBar
import com.matox.nexcore.presentation.files.components.FilesTopBar
import com.matox.nexcore.presentation.files.viewmodel.FilesViewModel
import com.matox.nexcore.presentation.storageanalyzer.state.StorageAnalyzerUiState
import com.matox.nexcore.ui.theme.BackgroundGradientBottom
import com.matox.nexcore.ui.theme.BackgroundGradientTop
import com.matox.nexcore.ui.theme.CardStroke
import com.matox.nexcore.ui.theme.MetricBlue
import com.matox.nexcore.ui.theme.NexCoreGreen
import com.matox.nexcore.ui.theme.Surface
import com.matox.nexcore.ui.theme.TextPrimary
import com.matox.nexcore.ui.theme.TextSecondary

/** Bottom padding under the scrollable content so it doesn't hide behind the floating dock. */
private val BottomContentPadding: Dp = 110.dp

/** Static bottom-nav used while on the Files screen. */
private val FilesBottomNav = listOf(
    BottomNavItem("nav_home", "Home", BottomNavIcon.HOME, isCenter = false, isActive = false),
    BottomNavItem("nav_files", "Files", BottomNavIcon.FILES, isCenter = false, isActive = true),
    BottomNavItem("nav_apps", "Apps", BottomNavIcon.APPS, isCenter = true, isActive = false),
    BottomNavItem("nav_settings", "Settings", BottomNavIcon.SETTINGS, isCenter = false, isActive = false),
)

@Composable
fun FilesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
) {
    // Reuse the storage-analyzer repository so the Files screen
    // shares the same MediaStore / Downloads numbers without
    // duplicating the provider code.
    val viewModel: FilesViewModel = viewModel(
        factory = FilesViewModel.Factory(AppContainer.storageAnalyzerRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }

    FilesContent(
        state = state,
        modifier = modifier,
        onBack = onBack,
        onBottomNavClick = onBottomNavClick,
    )
}

@Composable
internal fun FilesContent(
    state: StorageAnalyzerUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onBottomNavClick: (BottomNavItem) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundGradientTop, BackgroundGradientBottom),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilesTopBar(onBackClick = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = contentPadding.calculateTopPadding())
                    .padding(bottom = BottomContentPadding),
            ) {
                when (state) {
                    StorageAnalyzerUiState.Loading -> CenteredMessage("Reading folders…")
                    is StorageAnalyzerUiState.Error -> CenteredMessage(state.message)
                    is StorageAnalyzerUiState.Success -> FilesReadyState(state)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            DashboardBottomBar(
                items = FilesBottomNav,
                onItemClick = onBottomNavClick,
            )
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

@Composable
private fun FilesReadyState(state: StorageAnalyzerUiState.Success) {
    val data = state.snapshot
    // Pick the categories we want surfaced as top-level "folders".
    // Order matches how users think about their phone storage.
    val folderIds = listOf(
        "cat_images" to "Images",
        "cat_videos" to "Videos",
        "cat_audio" to "Audio",
        "cat_downloads" to "Downloads",
        "cat_documents" to "Documents",
        "cat_apps" to "Apps",
    )
    val folders = folderIds.mapNotNull { (id, label) ->
        data.categories.firstOrNull { it.id == id }?.let { it to label }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Hero header
        FilesHeroCard(
            usedGb = data.internalUsedGb,
            totalGb = data.internalTotalGb,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Folders",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = TextPrimary,
        )

        folders.forEach { (cat, label) ->
            FolderRow(
                label = label,
                sizeGb = cat.usedGb,
                accent = cat.accent,
                icon = iconFor(cat.id),
            )
        }
    }
}

@Composable
private fun FilesHeroCard(usedGb: Float, totalGb: Float) {
    val pct = if (totalGb <= 0f) 0 else ((usedGb / totalGb) * 100f).toInt().coerceIn(0, 100)
    val usedDisplay = formatGb(usedGb)
    val totalDisplay = formatGb(totalGb)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MetricBlue.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = MetricBlue,
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "Internal storage",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                )
                Text(
                    text = "$usedDisplay of $totalDisplay used",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1F2A44)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NexCoreGreen, MetricBlue),
                        ),
                    ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$pct% used",
                style = MaterialTheme.typography.labelSmall,
                color = NexCoreGreen,
            )
            Text(
                text = "${100 - pct}% free",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun FolderRow(
    label: String,
    sizeGb: Float,
    accent: MetricAccent,
    icon: ImageVector,
) {
    val color = colorForAccent(accent)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, CardStroke, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = TextPrimary,
            )
            Text(
                text = "Tap to browse",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
        Text(
            text = formatGb(sizeGb),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = color,
        )
    }
}

private fun iconFor(categoryId: String): ImageVector = when (categoryId) {
    "cat_images" -> Icons.Outlined.Image
    "cat_videos" -> Icons.Outlined.Movie
    "cat_audio" -> Icons.Outlined.AudioFile
    "cat_downloads" -> Icons.Outlined.Download
    "cat_documents" -> Icons.Outlined.Description
    else -> Icons.Outlined.Folder
}

private fun colorForAccent(accent: MetricAccent): Color = when (accent) {
    MetricAccent.PINK -> Color(0xFFEC4899)
    MetricAccent.PURPLE -> Color(0xFFA855F7)
    MetricAccent.BLUE -> Color(0xFF3B82F6)
    MetricAccent.VIOLET -> Color(0xFF7C3AED)
    MetricAccent.GREEN -> Color(0xFF22C55E)
    MetricAccent.ORANGE -> Color(0xFFF97316)
    MetricAccent.CYAN -> Color(0xFF06B6D4)
    MetricAccent.TEAL -> Color(0xFF14B8A6)
    MetricAccent.RED -> Color(0xFFEF4444)
}

private fun formatGb(v: Float): String {
    if (v <= 0f) return "0 GB"
    val rounded = (v * 10f).toInt() / 10f
    return "$rounded GB"
}