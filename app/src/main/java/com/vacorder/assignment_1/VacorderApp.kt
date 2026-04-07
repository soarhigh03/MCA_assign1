package com.vacorder.assignment_1

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vacorder.assignment_1.ui.home.HomeScreen
import com.vacorder.assignment_1.ui.imu.ImuScreen
import com.vacorder.assignment_1.ui.camera.CameraScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Imu : Screen("imu")
    data object Camera : Screen("camera")
}

@Composable
fun VacorderApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToImu = { navController.navigate(Screen.Imu.route) },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) }
            )
        }
        composable(Screen.Imu.route) {
            ImuScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Camera.route) {
            CameraScreen(onBack = { navController.popBackStack() })
        }
    }
}
