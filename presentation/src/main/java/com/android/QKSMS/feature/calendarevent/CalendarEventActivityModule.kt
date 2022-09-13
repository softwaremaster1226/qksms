package com.android.QKSMS.feature.calendarevent

import androidx.lifecycle.ViewModel
import com.android.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class CalendarEventActivityModule {
    @Provides
    @IntoMap
    @ViewModelKey(CalendarEventViewModel::class)
    fun provideCalendarEventViewModel(viewModel: CalendarEventViewModel): ViewModel = viewModel
}
