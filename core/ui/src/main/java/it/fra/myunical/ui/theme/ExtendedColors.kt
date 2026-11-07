package it.fra.myunical.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** A color with its content color and container pair, like the roles of the Material scheme. */
@Immutable
data class ColorRole(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color
)

/**
 * Semantic colors the Material scheme does not have: outcomes (passed, paid) and states that need
 * attention (deadlines, bookings not open yet). Tones generated like the scheme (40/100/90/10 in
 * light, 80/20/30/90 in dark).
 */
@Immutable
data class ExtendedColors(
    val success: ColorRole,
    val warning: ColorRole,
    val info: ColorRole
)

val LightExtendedColors = ExtendedColors(
    success = ColorRole(Color(0xFF3C6939), Color(0xFFFFFFFF), Color(0xFFBCF0B4), Color(0xFF002204)),
    warning = ColorRole(Color(0xFF9C4400), Color(0xFFFFFFFF), Color(0xFFFFDBCA), Color(0xFF331200)),
    info = ColorRole(Color(0xFF594CC4), Color(0xFFFFFFFF), Color(0xFFE4DFFF), Color(0xFF160066))
)

val DarkExtendedColors = ExtendedColors(
    success = ColorRole(Color(0xFFA1D39A), Color(0xFF0A390F), Color(0xFF245024), Color(0xFFBCF0B4)),
    warning = ColorRole(Color(0xFFFFB68F), Color(0xFF542100), Color(0xFF773200), Color(0xFFFFDBCA)),
    info = ColorRole(Color(0xFFC6BFFF), Color(0xFF290F95), Color(0xFF4131AB), Color(0xFFE4DFFF))
)

/** Provided by the app theme; previews without it get the light colors. */
val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

/** The Material roles as [ColorRole]s, to pick a family per item (quick links, list icons). */
object ColorRoles {
    val primary: ColorRole
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.let { ColorRole(it.primary, it.onPrimary, it.primaryContainer, it.onPrimaryContainer) }

    val secondary: ColorRole
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.let { ColorRole(it.secondary, it.onSecondary, it.secondaryContainer, it.onSecondaryContainer) }

    val tertiary: ColorRole
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.let { ColorRole(it.tertiary, it.onTertiary, it.tertiaryContainer, it.onTertiaryContainer) }

    val error: ColorRole
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.let { ColorRole(it.error, it.onError, it.errorContainer, it.onErrorContainer) }

    /** Neutral family for items without a meaning of their own. */
    val neutral: ColorRole
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.let {
            ColorRole(it.onSurfaceVariant, it.surface, it.surfaceContainerHighest, it.onSurfaceVariant)
        }
}

/** Families of the app, named by meaning so screens do not pick raw roles. */
enum class Accent { PRIMARY, SECONDARY, TERTIARY, SUCCESS, WARNING, INFO, ERROR, NEUTRAL }

val Accent.role: ColorRole
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        Accent.PRIMARY -> ColorRoles.primary
        Accent.SECONDARY -> ColorRoles.secondary
        Accent.TERTIARY -> ColorRoles.tertiary
        Accent.SUCCESS -> MaterialTheme.extendedColors.success
        Accent.WARNING -> MaterialTheme.extendedColors.warning
        Accent.INFO -> MaterialTheme.extendedColors.info
        Accent.ERROR -> ColorRoles.error
        Accent.NEUTRAL -> ColorRoles.neutral
    }
