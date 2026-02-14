package com.yt.car.union.viewmodel

import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.net.TrainingRepository

open class TrainingBaseViewModel: BaseViewModel() {
    protected val vehicleRepository: TrainingRepository by lazy {
        TrainingRepository(RetrofitClient.getTrainingApiService())
    }
}