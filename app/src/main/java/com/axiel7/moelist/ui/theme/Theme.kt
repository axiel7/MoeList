package com.axiel7.moelist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoeListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    useBlackColors: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.Expressive,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(dynamicColor, darkTheme, useBlackColors, paletteStyle) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val colors = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

                dynamicColorScheme(
                    primary = colors.primary,
                    isDark = darkTheme,
                    isAmoled = useBlackColors,
                    style = paletteStyle,
                    specVersion = ColorSpec.SpecVersion.SPEC_2025,
                )
            }

            else -> dynamicColorScheme(
                seedColor = seed,
                isDark = darkTheme,
                isAmoled = useBlackColors,
                style = paletteStyle,
                specVersion = ColorSpec.SpecVersion.SPEC_2025
            )
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}