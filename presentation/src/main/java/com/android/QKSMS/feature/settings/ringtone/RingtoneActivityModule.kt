package com.android.QKSMS.feature.settings.ringtone

import androidx.lifecycle.ViewModel
import com.android.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class RingtoneActivityModule {
    @Provides
    @IntoMap
    @ViewModelKey(RingtoneViewModel::class)
    fun provideRingtoneViewModel(viewModel: RingtoneViewModel): ViewModel = viewModel
}
