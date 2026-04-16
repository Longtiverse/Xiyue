package com.xiyue.app.features.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xiyue.app.features.home.LibraryUiItem
import com.xiyue.app.ui.components.EmptyState
import com.xiyue.app.ui.components.LibraryItemRow
import com.xiyue.app.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<LibraryUiItem>,
    onToggleFavorite: (String) -> Unit,
    onSelectItem: (String) -> Unit,
    onBrowseLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("收藏") },
            )
        },
    ) { padding ->
        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Favorite,
                title = "暂无收藏",
                subtitle = "在练习界面点击收藏按钮来添加",
                actionLabel = "去浏览曲库",
                onAction = onBrowseLibrary,
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DesignTokens.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            ) {
                Text(
                    text = "${favorites.size} 项收藏",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                LazyColumn(
                    contentPadding = PaddingValues(bottom = DesignTokens.Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                ) {
                    items(
                        items = favorites,
                        key = { it.id },
                    ) { item ->
                        LibraryItemRow(
                            item = item,
                            isFavorite = true,
                            onToggleFavorite = { onToggleFavorite(item.id) },
                            onClick = { onSelectItem(item.id) },
                        )
                    }
                }
            }
        }
    }
}
