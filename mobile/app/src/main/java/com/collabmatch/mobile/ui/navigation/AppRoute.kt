package com.collabmatch.mobile.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Dashboard : AppRoute("dashboard")
    data object Profile : AppRoute("profile")
    data object CreateProject : AppRoute("create-project")
    data object ProjectDetail : AppRoute("project/{projectId}") {
        fun create(projectId: Long) = "project/$projectId"
    }
}
