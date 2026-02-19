package com.yt.car.union.net

import java.util.concurrent.ConcurrentHashMap

data class DictItem(
    val searchValue: String? = null,
    val createBy: String? = null,
    val createTime: String? = null,
    val updateBy: String? = null,
    val updateTime: String? = null,
    val remark: String? = null,
    val params: Any,
    val dictCode: String? = null,
    val dictSort: String? = null,
    val dictLabel: String? = null,
    val dictValue: String? = null,
    val dictType: String? = null,
    val cssClass: String? = null,
    val listClass: String? = null,
    val isDefault: String? = null,
    val status: String? = null,
    val default: Boolean? = null
)

/**
 * 全局字典Map管理单例
 * 使用ConcurrentHashMap保证多线程环境下的线程安全
 * 自动过滤dictValue/dictLabel为空的无效项
 */
object DictMapManager {
    // 核心Map：key=dictValue，value=dictLabel，线程安全的可变Map
    private val _dictLabelMap = ConcurrentHashMap<String, String>()

    // 对外暴露不可变视图，防止外部直接修改Map（仅可通过提供的方法操作）
    val dictLabelMap: Map<String, String>
        get() = _dictLabelMap.toMap() // 返回不可变副本

    /**
     * 从DictItem列表初始化/更新全局Map
     * @param dictItems 字典项列表
     */
    fun initDictMap(dictItems: List<DictItem>) {
        // 清空原有数据（可选，根据需求决定是覆盖还是追加）
        _dictLabelMap.clear()

        // 遍历列表，过滤空值后存入Map
        try {
            dictItems.forEach { item ->
                val key = item.dictValue
                val value = item.dictLabel
                // 仅当key和value都不为null时才存入，避免空指针
                if (key != null && value != null) {
                    _dictLabelMap[key] = value
                }
            }
        } catch (e: Exception) {
            System.out.println(e)
        }
    }

    /**
     * 便捷方法：根据dictValue获取dictLabel，不存在则返回默认值
     */
    fun getDictLabelByValue(dictValue: String?, defaultValue: String = ""): String {
        return dictLabelMap[dictValue] ?: defaultValue
    }
}