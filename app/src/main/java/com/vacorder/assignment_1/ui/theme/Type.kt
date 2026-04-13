package com.vacorder.assignment_1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.vacorder.assignment_1.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val interFont = GoogleFont("Inter")

val InterFamily = FontFamily(
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

private fun style(weight: FontWeight, size: Int, lineHeight: Int) = TextStyle(
    fontFamily = InterFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp
)

val Typography = Typography(
    displayLarge = style(FontWeight.Bold, 48, 56),
    displayMedium = style(FontWeight.Bold, 40, 48),
    displaySmall = style(FontWeight.SemiBold, 32, 40),
    headlineLarge = style(FontWeight.Bold, 28, 36),
    headlineMedium = style(FontWeight.SemiBold, 24, 32),
    headlineSmall = style(FontWeight.SemiBold, 20, 28),
    titleLarge = style(FontWeight.SemiBold, 20, 28),
    titleMedium = style(FontWeight.Medium, 16, 24),
    titleSmall = style(FontWeight.Medium, 14, 20),
    bodyLarge = style(FontWeight.Normal, 16, 24),
    bodyMedium = style(FontWeight.Normal, 14, 20),
    bodySmall = style(FontWeight.Normal, 12, 16),
    labelLarge = style(FontWeight.Medium, 14, 20),
    labelMedium = style(FontWeight.Medium, 12, 16),
    labelSmall = style(FontWeight.Medium, 11, 16)
)
