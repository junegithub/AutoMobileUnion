package com.yt.car.union.viewmodel

import com.yt.car.union.net.SearchCarTypeData
import com.yt.car.union.net.SearchHistoryRequest
import com.yt.car.union.net.TreeNode
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 搜索相关ViewModel（搜索历史、车辆搜索等）
 */
class SearchViewModel : CarBaseViewModel() {

    // 添加搜索历史
    fun addSearchHistory(request: SearchHistoryRequest,
                         stateFlow: MutableStateFlow<ApiState<Int>>) {
        launchRequest(
            block = { vehicleRepository.addSearchHistory(request) },
            stateFlow
        )
    }



    // 树形结构搜索
    fun getTree(ancestors: String?, pos: Boolean?, tree: Boolean?,
                stateFlow: MutableStateFlow<ApiState<List<TreeNode>>>) {
        launchRequest(
            block = { vehicleRepository.getTree(ancestors, pos, tree) },
            stateFlow
        )
    }

    // 模糊树形搜索
    fun getTreeBlurry(blurry: String, pos: Boolean?, tree: Boolean?,
                      stateFlow: MutableStateFlow<ApiState<List<TreeNode>>>) {
        launchRequest(
            block = { vehicleRepository.getTreeBlurry(blurry, pos, tree) },
            stateFlow
        )
    }

    // 按类型搜索车辆
    fun searchCarByType(search: String, tree: Boolean?, type: String,
                        pageSize: String, pageNum: String,
                        stateFlow: MutableStateFlow<ApiState<SearchCarTypeData>>) {
        launchRequest(
            block = { vehicleRepository.searchCarByType(search, tree, type, pageSize, pageNum) },
            stateFlow
        )
    }
}