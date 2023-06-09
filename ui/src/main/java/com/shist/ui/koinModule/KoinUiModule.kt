package com.shist.ui.koinModule

import com.shist.ui.viewModels.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Koin module is needed for initializing all variables and objects in classes
val uiModule = module {

    viewModel { MapViewModel(application = get()) }

}