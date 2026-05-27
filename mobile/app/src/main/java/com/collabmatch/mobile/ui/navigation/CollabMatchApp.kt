package com.collabmatch.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.collabmatch.mobile.core.data.CollabMatchRepository
import com.collabmatch.mobile.feature.auth.data.AuthPayload
import com.collabmatch.mobile.feature.auth.ui.LoginScreen
import com.collabmatch.mobile.feature.auth.ui.RegisterScreen
import com.collabmatch.mobile.feature.profile.ui.ProfileScreen
import com.collabmatch.mobile.feature.projects.ui.CreateProjectScreen
import com.collabmatch.mobile.feature.projects.ui.DashboardScreen
import com.collabmatch.mobile.feature.projects.ui.ProjectDetailScreen

@Composable
fun CollabMatchApp(repository: CollabMatchRepository, navController: NavHostController = rememberNavController()) {
    val startDestination = if (repository.getAccessToken().isNullOrBlank()) AppRoute.Login.route else AppRoute.Dashboard.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                repository = repository,
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
                onLoginSuccess = { payload ->
                    onAuthenticated(repository, payload)
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Register.route) {
            RegisterScreen(
                repository = repository,
                onLoginClick = { navController.navigate(AppRoute.Login.route) },
                onRegisterSuccess = { payload ->
                    onAuthenticated(repository, payload)
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Dashboard.route) {
            DashboardScreen(
                repository = repository,
                onOpenProfile = { navController.navigate(AppRoute.Profile.route) },
                onCreateProject = { navController.navigate(AppRoute.CreateProject.route) },
                onOpenProject = { projectId ->
                    navController.navigate(AppRoute.ProjectDetail.create(projectId))
                },
                onLoggedOut = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.CreateProject.route) {
            CreateProjectScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onProjectCreated = { projectId ->
                    navController.navigate(AppRoute.ProjectDetail.create(projectId)) {
                        popUpTo(AppRoute.CreateProject.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppRoute.ProjectDetail.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            ProjectDetailScreen(
                repository = repository,
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Dashboard.route) { inclusive = true }
                    }
                },
                onDeleted = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Dashboard.route)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

private fun onAuthenticated(repository: CollabMatchRepository, payload: AuthPayload) {
    repository.saveTokens(payload.accessToken, payload.refreshToken)
}
