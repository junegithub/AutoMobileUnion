package com.yt.car.union.viewmodel

import com.yt.car.union.net.SearchHistoryRequest

/**
 * 搜索相关ViewModel（搜索历史、车辆搜索等）
 */
class SearchViewModel : CarBaseViewModel() {

    // 添加搜索历史
    fun addSearchHistory(request: SearchHistoryRequest) {
        launchRequest(
            block = { vehicleRepository.addSearchHistory(request) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code != null) {
                    // 业务成功逻辑
                }
            }
        )
    }



    // 树形结构搜索
    fun getTree(ancestors: String?, pos: Boolean?, tree: Boolean?) {
        launchRequest(
            block = { vehicleRepository.getTree(ancestors, pos, tree) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 模糊树形搜索
    fun getTreeBlurry(blurry: String, pos: Boolean?, tree: Boolean?) {
        launchRequest(
            block = { vehicleRepository.getTreeBlurry(blurry, pos, tree) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }

    // 按类型搜索车辆
    fun searchCarByType(search: String, tree: Boolean?, type: String, pageSize: String, pageNum: String) {
        launchRequest(
            block = { vehicleRepository.searchCarByType(search, tree, type, pageSize, pageNum) },
            onSuccess = { response ->
                if (response.isSuccessful && response.body()?.code == 1) {
                    // 业务成功逻辑
                }
            }
        )
    }
}