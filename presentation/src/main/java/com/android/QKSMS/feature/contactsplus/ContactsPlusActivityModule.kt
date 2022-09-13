package com.android.QKSMS.feature.contactsplus

import androidx.lifecycle.ViewModel
import com.android.QKSMS.feature.contacts.ContactsActivity
import com.android.QKSMS.feature.contactsplus.fragment.ContactsPlusFragmentViewModel
import com.android.QKSMS.feature.quick.QuickMessageViewModel
import com.android.QKSMS.injection.ViewModelKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ContactsPlusActivityModule {
    @Provides
    fun provideChips(activity: ContactsPlusActivity): HashMap<String, String?> {
        return activity.intent.extras?.getSerializable(ContactsActivity.ChipsKey)
                ?.let { serializable -> serializable as? HashMap<String, String?> }
                ?: hashMapOf()
    }

    @Provides
    @IntoMap
    @ViewModelKey(ContactsPlusViewModel::class)
    fun provideContactsPlusViewModel(viewModel: ContactsPlusViewModel): ViewModel = viewModel
}