package com.collabmatch.mobile.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Dashboard : AppRoute("dashboard")
}
