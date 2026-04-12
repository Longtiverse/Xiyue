package com.xiyue.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
) {
    PRACTICE("练习", Icons.Filled.Home),
    COMBO("组合", Icons.Filled.LibraryMusic),
    FAVORITES("收藏", Icons.Filled.Favorite),
    SETTINGS("设置", Icons.Filled.Settings),
}

@Composable
fun XiyueBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selectedItem == item,
                onClick = { onItemSelected(item) },
            )
        }
    }
}
