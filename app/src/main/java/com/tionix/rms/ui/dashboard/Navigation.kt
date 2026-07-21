package com.tionix.rms.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tionix.rms.ui.navigation.RmsRoutes
import com.tionix.rms.ui.theme.Dimens

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

/** Only Home + Profile — Scanner FAB lives in the centre slot */
val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, RmsRoutes.HOME),
    BottomNavItem("Profile", Icons.Default.Person, RmsRoutes.PROFILE)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RmsTopAppBar(
    title: String,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * Scanner FAB that floats above the centre of the bottom nav bar.
 * Elevated, circular, with a gradient-tinted background.
 */
@Composable
fun ScannerFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier.size(60.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scanner",
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun RmsBottomNavigation(
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        // Left side — Home
        val homeItem = bottomNavItems[0]
        NavigationBarItem(
            selected = currentRoute == homeItem.route,
            onClick = { onItemSelected(homeItem.route) },
            icon = {
                Icon(imageVector = homeItem.icon, contentDescription = homeItem.label)
            },
            label = {
                Text(text = homeItem.label, style = MaterialTheme.typography.labelSmall)
            },
            modifier = Modifier.defaultMinSize(minHeight = Dimens.touchTargetMin),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        )

        // Centre slot — empty space for the FAB cutout
        NavigationBarItem(
            selected = false,
            onClick = {},
            enabled = false,
            icon = { Box(modifier = Modifier.size(60.dp)) }, // invisible spacer
            label = { Text("") },
            modifier = Modifier.defaultMinSize(minHeight = Dimens.touchTargetMin)
        )

        // Right side — Profile
        val profileItem = bottomNavItems[1]
        NavigationBarItem(
            selected = currentRoute == profileItem.route,
            onClick = { onItemSelected(profileItem.route) },
            icon = {
                Icon(imageVector = profileItem.icon, contentDescription = profileItem.label)
            },
            label = {
                Text(text = profileItem.label, style = MaterialTheme.typography.labelSmall)
            },
            modifier = Modifier.defaultMinSize(minHeight = Dimens.touchTargetMin),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        )
    }
}
