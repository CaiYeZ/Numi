package com.herb.numi.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.herb.numi.data.CustomCategoryRepositoryInterface
import com.herb.numi.data.RecordRepositoryInterface

/**
 * RecordViewModel 工厂类
 * 通过构造函数注入 Application 和 Repository 接口，遵循依赖注入原则
 * 避免在 ViewModel 内部直接实例化依赖对象
 */
class RecordViewModelFactory(
    private val application: Application,
    private val repository: RecordRepositoryInterface,
    private val customCategoryRepository: CustomCategoryRepositoryInterface
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            return RecordViewModel(application, repository, customCategoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
