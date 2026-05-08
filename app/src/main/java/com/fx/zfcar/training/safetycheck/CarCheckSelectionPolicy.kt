package com.fx.zfcar.training.safetycheck

object CarCheckSelectionPolicy {
    fun resolveCarNum(carNum: String?, carnum: String?): String {
        return carNum.orEmpty().ifBlank { carnum.orEmpty() }
    }
}
