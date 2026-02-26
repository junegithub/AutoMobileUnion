package com.fx.zfcar.car.viewmodel

import com.fx.zfcar.net.SearchCarTypeData
import com.fx.zfcar.net.TreeNode
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 搜索相关ViewModel（搜索历史、车辆搜索等）
 */
class SearchViewModel : CarBaseViewModel() {

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