package com.collabmatch.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.ui.screen.DashboardScreen
import com.collabmatch.mobile.ui.screen.LoginScreen
import com.collabmatch.mobile.ui.screen.RegisterScreen

@Composable
fun CollabMatchApp(repository: AuthRepository, navController: NavHostController = rememberNavController()) {
    val startDestination = if (repository.getToken().isNullOrBlank()) AppRoute.Login.route else AppRoute.Dashboard.route

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
                onLoggedOut = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun onAuthenticated(repository: AuthRepository, payload: AuthPayload) {
    repository.saveToken(payload.token)
}
