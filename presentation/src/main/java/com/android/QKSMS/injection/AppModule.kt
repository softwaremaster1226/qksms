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
package com.android.QKSMS.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.android.QKSMS.blocking.BlockingClient
import com.android.QKSMS.blocking.BlockingManager
import com.android.QKSMS.common.ViewModelFactory
import com.android.QKSMS.common.util.NotificationManagerImpl
import com.android.QKSMS.common.util.ShortcutManagerImpl
import com.android.QKSMS.feature.conversationinfo.injection.ConversationInfoComponent
import com.android.QKSMS.feature.themepicker.injection.ThemePickerComponent
import com.android.QKSMS.listener.ContactAddedListener
import com.android.QKSMS.listener.ContactAddedListenerImpl
import com.android.QKSMS.manager.ActiveConversationManager
import com.android.QKSMS.manager.ActiveConversationManagerImpl
import com.android.QKSMS.manager.AlarmManager
import com.android.QKSMS.manager.AlarmManagerImpl
import com.android.QKSMS.manager.AnalyticsManager
import com.android.QKSMS.manager.AnalyticsManagerImpl
import com.android.QKSMS.manager.ChangelogManager
import com.android.QKSMS.manager.ChangelogManagerImpl
import com.android.QKSMS.manager.KeyManager
import com.android.QKSMS.manager.KeyManagerImpl
import com.android.QKSMS.manager.NotificationManager
import com.android.QKSMS.manager.PermissionManager
import com.android.QKSMS.manager.PermissionManagerImpl
import com.android.QKSMS.manager.RatingManager
import com.android.QKSMS.manager.ReferralManager
import com.android.QKSMS.manager.ReferralManagerImpl
import com.android.QKSMS.manager.ShortcutManager
import com.android.QKSMS.manager.WidgetManager
import com.android.QKSMS.manager.WidgetManagerImpl
import com.android.QKSMS.mapper.CursorToContact
import com.android.QKSMS.mapper.CursorToContactGroup
import com.android.QKSMS.mapper.CursorToContactGroupImpl
import com.android.QKSMS.mapper.CursorToContactGroupMember
import com.android.QKSMS.mapper.CursorToContactGroupMemberImpl
import com.android.QKSMS.mapper.CursorToContactImpl
import com.android.QKSMS.mapper.CursorToConversation
import com.android.QKSMS.mapper.CursorToConversationImpl
import com.android.QKSMS.mapper.CursorToMessage
import com.android.QKSMS.mapper.CursorToMessageImpl
import com.android.QKSMS.mapper.CursorToPart
import com.android.QKSMS.mapper.CursorToPartImpl
import com.android.QKSMS.mapper.CursorToRecipient
import com.android.QKSMS.mapper.CursorToRecipientImpl
import com.android.QKSMS.mapper.RatingManagerImpl
import com.android.QKSMS.repository.BackupRepository
import com.android.QKSMS.repository.BackupRepositoryImpl
import com.android.QKSMS.repository.BlockingRepository
import com.android.QKSMS.repository.BlockingRepositoryImpl
import com.android.QKSMS.repository.ContactRepository
import com.android.QKSMS.repository.ContactRepositoryImpl
import com.android.QKSMS.repository.ConversationRepository
import com.android.QKSMS.repository.ConversationRepositoryImpl
import com.android.QKSMS.repository.MessageRepository
import com.android.QKSMS.repository.MessageRepositoryImpl
import com.android.QKSMS.repository.ScheduledMessageRepository
import com.android.QKSMS.repository.ScheduledMessageRepositoryImpl
import com.android.QKSMS.repository.SyncRepository
import com.android.QKSMS.repository.SyncRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [
    ConversationInfoComponent::class,
    ThemePickerComponent::class])
class AppModule(private var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRxPreferences(preferences: SharedPreferences): RxSharedPreferences {
        return RxSharedPreferences.create(preferences)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
    }

    @Provides
    fun provideViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory = factory

    // Listener

    @Provides
    fun provideContactAddedListener(listener: ContactAddedListenerImpl): ContactAddedListener = listener

    // Manager

    @Provides
    fun provideActiveConversationManager(manager: ActiveConversationManagerImpl): ActiveConversationManager = manager

    @Provides
    fun provideAlarmManager(manager: AlarmManagerImpl): AlarmManager = manager

    @Provides
    fun provideAnalyticsManager(manager: AnalyticsManagerImpl): AnalyticsManager = manager

    @Provides
    fun blockingClient(manager: BlockingManager): BlockingClient = manager

    @Provides
    fun changelogManager(manager: ChangelogManagerImpl): ChangelogManager = manager

    @Provides
    fun provideKeyManager(manager: KeyManagerImpl): KeyManager = manager

    @Provides
    fun provideNotificationsManager(manager: NotificationManagerImpl): NotificationManager = manager

    @Provides
    fun providePermissionsManager(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    fun provideRatingManager(manager: RatingManagerImpl): RatingManager = manager

    @Provides
    fun provideShortcutManager(manager: ShortcutManagerImpl): ShortcutManager = manager

    @Provides
    fun provideReferralManager(manager: ReferralManagerImpl): ReferralManager = manager

    @Provides
    fun provideWidgetManager(manager: WidgetManagerImpl): WidgetManager = manager

    // Mapper

    @Provides
    fun provideCursorToContact(mapper: CursorToContactImpl): CursorToContact = mapper

    @Provides
    fun provideCursorToContactGroup(mapper: CursorToContactGroupImpl): CursorToContactGroup = mapper

    @Provides
    fun provideCursorToContactGroupMember(mapper: CursorToContactGroupMemberImpl): CursorToContactGroupMember = mapper

    @Provides
    fun provideCursorToConversation(mapper: CursorToConversationImpl): CursorToConversation = mapper

    @Provides
    fun provideCursorToMessage(mapper: CursorToMessageImpl): CursorToMessage = mapper

    @Provides
    fun provideCursorToPart(mapper: CursorToPartImpl): CursorToPart = mapper

    @Provides
    fun provideCursorToRecipient(mapper: CursorToRecipientImpl): CursorToRecipient = mapper

    // Repository

    @Provides
    fun provideBackupRepository(repository: BackupRepositoryImpl): BackupRepository = repository

    @Provides
    fun provideBlockingRepository(repository: BlockingRepositoryImpl): BlockingRepository = repository

    @Provides
    fun provideContactRepository(repository: ContactRepositoryImpl): ContactRepository = repository

    @Provides
    fun provideConversationRepository(repository: ConversationRepositoryImpl): ConversationRepository = repository

    @Provides
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository = repository

    @Provides
    fun provideScheduledMessagesRepository(repository: ScheduledMessageRepositoryImpl): ScheduledMessageRepository = repository

    @Provides
    fun provideSyncRepository(repository: SyncRepositoryImpl): SyncRepository = repository

}