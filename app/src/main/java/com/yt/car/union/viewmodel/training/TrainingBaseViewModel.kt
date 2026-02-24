package com.yt.car.union.viewmodel.training

import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.net.TrainingRepository
import com.yt.car.union.viewmodel.BaseViewModel

open class TrainingBaseViewModel: BaseViewModel() {
    protected val vehicleRepository: TrainingRepository by lazy {
        TrainingRepository(RetrofitClient.getTrainingApiService())
    }
}