/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.axiel7.moelist.uicompose.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.axiel7.moelist.R

/**
 * Temporary implementation of theme object for Glance-appwidgets.
 *
 * Important: It will change!
 */
object GlanceTheme {
    val colors: ColorProviders
        @Composable
        @ReadOnlyComposable
        get() = LocalColorProviders.current
}

internal val LocalColorProviders = staticCompositionLocalOf { dynamicThemeColorProviders() }

/**
 * Temporary implementation of Material3 theme for Glance.
 *
 * Note: This still requires manually setting the colors for all Glance components.
 */
@Composable
fun WidgetTheme(colors: ColorProviders = GlanceTheme.colors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalColorProviders provides colors) {
        content()
    }
}

/**
 * Holds a set of Glance-specific [ColorProvider] following Material naming conventions.
 */
data class ColorProviders(
    val primary: ColorProvider,
    val onPrimary: ColorProvider,
    val primaryContainer: ColorProvider,
    val onPrimaryContainer: ColorProvider,
    val secondary: ColorProvider,
    val onSecondary: ColorProvider,
    val secondaryContainer: ColorProvider,
    val onSecondaryContainer: ColorProvider,
    val tertiary: ColorProvider,
    val onTertiary: ColorProvider,
    val tertiaryContainer: ColorProvider,
    val onTertiaryContainer: ColorProvider,
    val error: ColorProvider,
    val errorContainer: ColorProvider,
    val onError: ColorProvider,
    val onErrorContainer: ColorProvider,
    val background: ColorProvider,
    val onBackground: ColorProvider,
    val surface: ColorProvider,
    val onSurface: ColorProvider,
    val surfaceVariant: ColorProvider,
    val onSurfaceVariant: ColorProvider,
    val outline: ColorProvider,
    val inverseOnSurface: ColorProvider,
    val inverseSurface: ColorProvider,
    val inversePrimary: ColorProvider,
)

/**
 * Creates a set of color providers that represents a Material3 style dynamic color theme. On
 * devices that support it, the theme is derived from the user specific platform colors, on other
 * devices this falls back to the Material3 baseline theme.
 */
fun dynamicThemeColorProviders(): ColorProviders {
    return ColorProviders(
        primary = ColorProvider(R.color.md_theme_primary),
        onPrimary = ColorProvider(R.color.md_theme_onPrimary),
        primaryContainer = ColorProvider(R.color.md_theme_primaryContainer),
        onPrimaryContainer = ColorProvider(R.color.md_theme_onPrimaryContainer),
        secondary = ColorProvider(R.color.md_theme_secondary),
        onSecondary = ColorProvider(R.color.md_theme_onSecondary),
        secondaryContainer = ColorProvider(R.color.md_theme_secondaryContainer),
        onSecondaryContainer = ColorProvider(R.color.md_theme_onSecondaryContainer),
        tertiary = ColorProvider(R.color.md_theme_tertiary),
        onTertiary = ColorProvider(R.color.md_theme_onTertiary),
        tertiaryContainer = ColorProvider(R.color.md_theme_tertiaryContainer),
        onTertiaryContainer = ColorProvider(R.color.md_theme_onTertiaryContainer),
        error = ColorProvider(R.color.md_theme_error),
        errorContainer = ColorProvider(R.color.md_theme_errorContainer),
        onError = ColorProvider(R.color.md_theme_onError),
        onErrorContainer = ColorProvider(R.color.md_theme_onErrorContainer),
        background = ColorProvider(R.color.md_theme_background),
        onBackground = ColorProvider(R.color.md_theme_onBackground),
        surface = ColorProvider(R.color.md_theme_surface),
        onSurface = ColorProvider(R.color.md_theme_onSurface),
        surfaceVariant = ColorProvider(R.color.md_theme_surfaceVariant),
        onSurfaceVariant = ColorProvider(R.color.md_theme_onSurfaceVariant),
        outline = ColorProvider(R.color.md_theme_outline),
        inverseOnSurface = ColorProvider(R.color.md_theme_inverseOnSurface),
        inverseSurface = ColorProvider(R.color.md_theme_inverseSurface),
        inversePrimary = ColorProvider(R.color.md_theme_inversePrimary),
    )
}


/**
 * Provide a Box composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetBox(
    modifier: GlanceModifier = GlanceModifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    Box(
        modifier = appWidgetBackgroundModifier().then(modifier),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Provide a Column composable using the system parameters for app widgets background with rounded
 * corners and background color.
 */
@Composable
fun AppWidgetColumn(
    modifier: GlanceModifier = GlanceModifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = appWidgetBackgroundModifier().then(modifier),
        verticalAlignment = verticalAlignment,
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

@Composable
fun appWidgetBackgroundModifier() = GlanceModifier
    .fillMaxSize()
    .padding(16.dp)
    .appWidgetBackground()
    .background(GlanceTheme.colors.background)
    .cornerRadius(24.dp)

@Composable
fun stringResource(@StringRes id: Int, vararg args: Any): String {
    return LocalContext.current.getString(id, args)
}