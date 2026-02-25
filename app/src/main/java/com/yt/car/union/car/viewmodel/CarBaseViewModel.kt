package com.yt.car.union.car.viewmodel

import com.yt.car.union.net.CarRepository
import com.yt.car.union.net.RetrofitClient
import com.yt.car.union.viewmodel.BaseViewModel

open class CarBaseViewModel: BaseViewModel() {
    protected val vehicleRepository: CarRepository by lazy {
        CarRepository(RetrofitClient.getCarApiService())
    }
}