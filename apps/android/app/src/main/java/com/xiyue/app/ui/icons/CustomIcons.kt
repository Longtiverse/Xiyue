package com.xiyue.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CustomHeadphones: ImageVector by lazy {
    ImageVector.Builder(
        name = "CustomHeadphones",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(12f, 1f)
            curveTo(7.03f, 1f, 3f, 5.03f, 3f, 10f)
            verticalLineTo(17f)
            curveTo(3f, 19.76f, 5.24f, 22f, 8f, 22f)
            horizontalLineTo(9f)
            verticalLineTo(16f)
            horizontalLineTo(7f)
            verticalLineTo(10f)
            curveTo(7f, 7.24f, 9.24f, 5f, 12f, 5f)
            curveTo(14.76f, 5f, 17f, 7.24f, 17f, 10f)
            verticalLineTo(16f)
            horizontalLineTo(15f)
            verticalLineTo(22f)
            horizontalLineTo(16f)
            curveTo(18.76f, 22f, 21f, 19.76f, 21f, 17f)
            verticalLineTo(10f)
            curveTo(21f, 5.03f, 16.97f, 1f, 12f, 1f)
            close()
        }
    }.build()
}

val CustomTouchApp: ImageVector by lazy {
    ImageVector.Builder(
        name = "CustomTouchApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(9f, 11.24f)
            verticalLineTo(7.5f)
            curveTo(9f, 6.12f, 10.12f, 5f, 11.5f, 5f)
            reflectiveCurveTo(14f, 6.12f, 14f, 7.5f)
            verticalLineToRelative(3.74f)
            curveToRelative(1.21f, 0.81f, 2f, 2.18f, 2f, 3.74f)
            verticalLineToRelative(0.5f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-0.5f)
            curveTo(14f, 13.57f, 13.33f, 12.5f, 12f, 12.5f)
            reflectiveCurveToRelative(-2f, 1.07f, -2f, 2.48f)
            verticalLineToRelative(0.5f)
            horizontalLineTo(8f)
            verticalLineToRelative(-0.5f)
            curveTo(8f, 13.42f, 8.79f, 12.05f, 9f, 11.24f)
            close()
            moveTo(11.5f, 9f)
            curveTo(11.78f, 9f, 12f, 8.78f, 12f, 8.5f)
            verticalLineTo(7.5f)
            curveTo(12f, 7.22f, 11.78f, 7f, 11.5f, 7f)
            reflectiveCurveTo(11f, 7.22f, 11f, 7.5f)
            verticalLineToRelative(0.5f)
            horizontalLineToRelative(1f)
            verticalLineToRelative(1f)
            horizontalLineTo(11f)
            verticalLineToRelative(1f)
            horizontalLineToRelative(1f)
            verticalLineToRelative(-1f)
            horizontalLineToRelative(-0.5f)
            verticalLineToRelative(-0.5f)
            close()
            moveTo(5f, 14.5f)
            curveTo(5f, 14.22f, 5.22f, 14f, 5.5f, 14f)
            horizontalLineTo(6.5f)
            curveTo(6.78f, 14f, 7f, 14.22f, 7f, 14.5f)
            verticalLineTo(19.5f)
            curveTo(7f, 19.78f, 6.78f, 20f, 6.5f, 20f)
            horizontalLineTo(5.5f)
            curveTo(5.22f, 20f, 5f, 19.78f, 5f, 19.5f)
            verticalLineTo(14.5f)
            close()
        }
    }.build()
}

val CustomSwipeUp: ImageVector by lazy {
    ImageVector.Builder(
        name = "CustomSwipeUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(7.41f, 15.41f)
            lineTo(12f, 10.83f)
            lineTo(16.59f, 15.41f)
            lineTo(18f, 14f)
            lineTo(12f, 8f)
            lineTo(6f, 14f)
            lineTo(7.41f, 15.41f)
            close()
        }
    }.build()
}
