package com.fx.zfcar.training.viewmodel

import com.fx.zfcar.net.RetrofitClient
import com.fx.zfcar.net.TrainingRepository
import com.fx.zfcar.viewmodel.BaseViewModel

open class TrainingBaseViewModel: BaseViewModel() {
    protected val vehicleRepository: TrainingRepository by lazy {
        TrainingRepository(RetrofitClient.getTrainingApiService())
    }
}