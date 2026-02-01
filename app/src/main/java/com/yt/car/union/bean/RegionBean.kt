package com.yt.car.union.bean

// 省份实体
data class ProvinceBean(val provinceName: String, val cities: List<CityBean>)
// 城市实体
data class CityBean(val cityName: String, val districts: List<String>)