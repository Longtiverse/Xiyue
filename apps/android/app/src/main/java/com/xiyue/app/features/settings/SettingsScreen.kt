package com.xiyue.app.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xiyue.app.ui.theme.DesignTokens

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

private fun getThemeModeLabel(mode: ThemeMode): String =
    when (mode) {
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
        ThemeMode.SYSTEM -> "跟随系统"
    }
