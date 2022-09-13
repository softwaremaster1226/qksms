package com.android.QKSMS.feature.contactsplus.fragment

import androidx.lifecycle.ViewModel
import com.android.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ContactsPlusFragmentModule {

    @Provides
    @IntoMap
    @ViewModelKey(ContactsPlusFragmentViewModel::class)
    fun provideContactsPlusViewModel(viewModel: ContactsPlusFragmentViewModel): ViewModel = viewModel
}