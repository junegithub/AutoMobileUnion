package com.fx.zfcar.car.viewmodel

import com.fx.zfcar.net.CarRepository
import com.fx.zfcar.net.RetrofitClient
import com.fx.zfcar.viewmodel.BaseViewModel

open class CarBaseViewModel: BaseViewModel() {
    protected val vehicleRepository: CarRepository by lazy {
        CarRepository(RetrofitClient.getCarApiService())
    }
}