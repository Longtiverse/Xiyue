package com.xiyue.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentStrong
import com.xiyue.app.ui.theme.XiyueBackgroundDeep
import com.xiyue.app.ui.theme.XiyueOutline
import com.xiyue.app.ui.theme.XiyueSurface
import com.xiyue.app.ui.theme.XiyueSurfaceVariant

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
) {
    PRACTICE("练习", Icons.Outlined.PlayCircleOutline),
    COMBO("组合", Icons.Outlined.Piano),
    FAVORITES("收藏", Icons.Outlined.FavoriteBorder),
    SETTINGS("设置", Icons.Outlined.Settings),
}

@Composable
fun XiyueBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(XiyueBackgroundDeep)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(XiyueSurfaceVariant.copy(alpha = 0.98f), XiyueSurface.copy(alpha = 0.98f))),
                    RoundedCornerShape(16.dp),
                )
                .border(1.dp, XiyueOutline.copy(alpha = 0.38f), RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp),
        ) {
            BottomNavItem.entries.forEach { item ->
                MockupNavTab(
                    item = item,
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MockupNavTab(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 8.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (selected) XiyueAccentStrong else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) XiyueAccentStrong else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
        if (selected) {
            NavTabIndicator()
        } else {
            Box(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
private fun NavTabIndicator() {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(3.dp)
            .background(XiyueAccent, RoundedCornerShape(999.dp)),
    )
}
