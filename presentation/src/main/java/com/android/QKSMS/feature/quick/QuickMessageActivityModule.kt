package com.android.QKSMS.feature.quick

import androidx.lifecycle.ViewModel
import com.android.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class QuickMessageActivityModule {
    @Provides
    @IntoMap
    @ViewModelKey(QuickMessageViewModel::class)
    fun provideQuickMessageViewModel(viewModel: QuickMessageViewModel): ViewModel = viewModel
}