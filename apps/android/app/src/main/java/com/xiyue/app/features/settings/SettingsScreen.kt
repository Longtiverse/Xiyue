package com.xiyue.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.data.AnalyticsSummary
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    showHints: Boolean,
    onShowHintsChange: (Boolean) -> Unit,
    analyticsSummary: AnalyticsSummary,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
            )
        },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.md),
        ) {
            Text(
                text = "主题",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

            ThemeModeSelector(
                selectedMode = themeMode,
                onModeSelected = onThemeModeChange,
            )

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "显示操作提示词",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "如“长按可停止”等辅助文案",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showHints,
                    onCheckedChange = onShowHintsChange,
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

            Text(
                text = "使用统计",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
            AnalyticsCard(summary = analyticsSummary)

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

            Text(
                text = "关于习乐",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
            Text(
                text = "版本 0.1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ThemeMode.entries.forEach { mode ->
            ListItem(
                headlineContent = { Text(getThemeModeLabel(mode)) },
                leadingContent = {
                    RadioButton(
                        selected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsCard(
    summary: AnalyticsSummary,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                maxItemsInEachRow = 2,
            ) {
                StatBadge(label = "练习次数", value = summary.totalPracticeSessions.toString())
                StatBadge(label = "练习时长(分)", value = summary.totalPracticeMinutes.toString())
                StatBadge(label = "听力答题", value = summary.totalEarTrainingQuestions.toString())
                StatBadge(label = "听力正确", value = summary.totalEarTrainingCorrect.toString())
                StatBadge(label = "连续天数", value = summary.consecutiveDays.toString())
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
            )
            .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = XiyueAccent,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun getThemeModeLabel(mode: ThemeMode): String =
    when (mode) {
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
        ThemeMode.SYSTEM -> "跟随系统"
    }
