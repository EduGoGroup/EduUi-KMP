package com.edugo.kmp.dynamicui.contract

class ScreenContractRegistry(
    contracts: List<ScreenContract> = emptyList()
) {
    private val map: Map<String, ScreenContract> = contracts.associateBy { it.screenKey }

    fun find(screenKey: String): ScreenContract? = map[screenKey]

    fun has(screenKey: String): Boolean = map.containsKey(screenKey)

    fun allKeys(): Set<String> = map.keys
}
