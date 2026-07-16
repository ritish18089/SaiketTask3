package com.example.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Keypad : Screen("keypad")
    object Settings : Screen("settings")
    object BlockedNumbers : Screen("blocked_numbers")
    object ThemeAppearance : Screen("theme_appearance")
    object CallForwarding : Screen("call_forwarding")
    object SpamDetection : Screen("spam_detection")
    object HelpFeedback : Screen("help_feedback")
    object About : Screen("about")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsConditions : Screen("terms_conditions")
}
