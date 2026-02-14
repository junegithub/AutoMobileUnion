package com.yt.car.union.viewmodel

import com.yt.car.union.net.CarRepository
import com.yt.car.union.net.RetrofitClient

open class CarBaseViewModel: BaseViewModel() {
    protected val vehicleRepository: CarRepository by lazy {
        CarRepository(RetrofitClient.getCarApiService())
    }
}