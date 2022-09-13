/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.android.QKSMS.injection.android

import com.android.QKSMS.feature.backup.BackupActivity
import com.android.QKSMS.feature.blocking.BlockingActivity
import com.android.QKSMS.feature.calendarevent.CalendarEventActivity
import com.android.QKSMS.feature.calendarevent.CalendarEventActivityModule
import com.android.QKSMS.feature.compose.ComposeActivity
import com.android.QKSMS.feature.compose.ComposeActivityModule
import com.android.QKSMS.feature.contacts.ContactsActivity
import com.android.QKSMS.feature.contacts.ContactsActivityModule
import com.android.QKSMS.feature.contactsplus.ContactsPlusActivity
import com.android.QKSMS.feature.contactsplus.ContactsPlusActivityModule
import com.android.QKSMS.feature.contactsplus.fragment.ContactPlusFragment
import com.android.QKSMS.feature.contactsplus.fragment.ContactsPlusFragmentModule
import com.android.QKSMS.feature.contactsplus.fragment.ContactsPlusFragmentViewModel
import com.android.QKSMS.feature.conversationinfo.ConversationInfoActivity
import com.android.QKSMS.feature.gallery.GalleryActivity
import com.android.QKSMS.feature.gallery.GalleryActivityModule
import com.android.QKSMS.feature.main.MainActivity
import com.android.QKSMS.feature.main.MainActivityModule
import com.android.QKSMS.feature.notificationprefs.NotificationPrefsActivity
import com.android.QKSMS.feature.notificationprefs.NotificationPrefsActivityModule
import com.android.QKSMS.feature.plus.PlusActivity
import com.android.QKSMS.feature.plus.PlusActivityModule
import com.android.QKSMS.feature.qkreply.QkReplyActivity
import com.android.QKSMS.feature.qkreply.QkReplyActivityModule
import com.android.QKSMS.feature.quick.QuickMessageActivity
import com.android.QKSMS.feature.quick.QuickMessageActivityModule
import com.android.QKSMS.feature.scheduled.ScheduledActivity
import com.android.QKSMS.feature.scheduled.ScheduledActivityModule
import com.android.QKSMS.feature.settings.SettingsActivity
import com.android.QKSMS.feature.settings.ringtone.RingtoneActivity
import com.android.QKSMS.feature.settings.ringtone.RingtoneActivityModule
import com.android.QKSMS.injection.scope.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsActivityModule::class])
    abstract fun bindContactsActivity(): ContactsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBlockingActivity(): BlockingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QuickMessageActivityModule::class])
    abstract fun bindQuickMessageActivity(): QuickMessageActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsPlusActivityModule::class])
    abstract fun bindContactsPlusActivity(): ContactsPlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsPlusFragmentModule::class])
    abstract fun bindContactsPlusFragment(): ContactPlusFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [CalendarEventActivityModule::class])
    abstract fun bindCalendarEventActivity(): CalendarEventActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [RingtoneActivityModule::class])
    abstract fun bindRingtoneActivity(): RingtoneActivity
}
