package com.edugo.kmp.dynamicui.data

import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.foundation.result.Result

interface DataLoader {
    suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String> = emptyMap()
    ): Result<DataPage>
}
