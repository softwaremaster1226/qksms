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
package com.android.QKSMS.feature.settings

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.android.QKSMS.R
import com.android.QKSMS.common.Navigator
import com.android.QKSMS.common.base.QkPresenter
import com.android.QKSMS.common.util.BillingManager
import com.android.QKSMS.common.util.Colors
import com.android.QKSMS.common.util.DateFormatter
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.interactor.SyncMessages
import com.android.QKSMS.manager.AnalyticsManager
import com.android.QKSMS.model.Message
import com.android.QKSMS.repository.SyncRepository
import com.android.QKSMS.util.NightModeManager
import com.android.QKSMS.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.realm.Realm
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    colors: Colors,
    syncRepo: SyncRepository,
    private val analytics: AnalyticsManager,
    private val context: Context,
    private val billingManager: BillingManager,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator,
    private val nightModeManager: NightModeManager,
    private val prefs: Preferences,
    private val syncMessages: SyncMessages
) : QkPresenter<SettingsView, SettingsState>(SettingsState(
        nightModeId = prefs.nightMode.get()
)) {

    init {
        disposables += colors.themeObservable()
                .subscribe { theme -> newState { copy(theme = theme.theme) } }

        val nightModeLabels = context.resources.getStringArray(R.array.night_modes)
        disposables += prefs.nightMode.asObservable()
                .subscribe { nightMode ->
                    newState { copy(nightModeSummary = nightModeLabels[nightMode], nightModeId = nightMode) }
                }

        disposables += prefs.nightStart.asObservable()
                .map { time -> nightModeManager.parseTime(time) }
                .map { calendar -> calendar.timeInMillis }
                .map { millis -> dateFormatter.getTimestamp(millis) }
                .subscribe { nightStart -> newState { copy(nightStart = nightStart) } }

        disposables += prefs.nightEnd.asObservable()
                .map { time -> nightModeManager.parseTime(time) }
                .map { calendar -> calendar.timeInMillis }
                .map { millis -> dateFormatter.getTimestamp(millis) }
                .subscribe { nightEnd -> newState { copy(nightEnd = nightEnd) } }

        disposables += prefs.black.asObservable()
                .subscribe { black -> newState { copy(black = black) } }

        disposables += prefs.notifications().asObservable()
                .subscribe { enabled -> newState { copy(notificationsEnabled = enabled) } }

        disposables += prefs.autoEmoji.asObservable()
                .subscribe { enabled -> newState { copy(autoEmojiEnabled = enabled) } }

        val delayedSendingLabels = context.resources.getStringArray(R.array.delayed_sending_labels)
        disposables += prefs.sendDelay.asObservable()
                .subscribe { id -> newState { copy(sendDelaySummary = delayedSendingLabels[id], sendDelayId = id) } }

        disposables += prefs.delivery.asObservable()
                .subscribe { enabled -> newState { copy(deliveryEnabled = enabled) } }

        disposables += prefs.signature.asObservable()
                .subscribe { signature -> newState { copy(signature = signature) } }

        val textSizeLabels = context.resources.getStringArray(R.array.text_sizes)
        disposables += prefs.textSize.asObservable()
                .subscribe { textSize ->
                    newState { copy(textSizeSummary = textSizeLabels[textSize], textSizeId = textSize) }
                }

        disposables += prefs.autoColor.asObservable()
                .subscribe { autoColor -> newState { copy(autoColor = autoColor) } }

        disposables += prefs.systemFont.asObservable()
                .subscribe { enabled -> newState { copy(systemFontEnabled = enabled) } }

//        disposables += prefs.unicode.asObservable()
//                .subscribe { enabled -> newState { copy(stripUnicodeEnabled = enabled) } }

        disposables += prefs.mobileOnly.asObservable()
                .subscribe { enabled -> newState { copy(mobileOnly = enabled) } }

        disposables += prefs.longAsMms.asObservable()
                .subscribe { enabled -> newState { copy(longAsMms = enabled) } }

        disposables += prefs.showAvatar.asObservable()
                .subscribe { flag -> newState { copy(showAvatar = flag) }}

        val mmsSizeLabels = context.resources.getStringArray(R.array.mms_sizes)
        val mmsSizeIds = context.resources.getIntArray(R.array.mms_sizes_ids)
        disposables += prefs.mmsSize.asObservable()
                .subscribe { maxMmsSize ->
                    val index = mmsSizeIds.indexOf(maxMmsSize)
                    newState { copy(maxMmsSizeSummary = mmsSizeLabels[index], maxMmsSizeId = maxMmsSize) }
                }

        disposables += syncRepo.syncProgress
                .sample(16, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe { syncProgress -> newState { copy(syncProgress = syncProgress) } }

        disposables += syncMessages
    }

    override fun bindIntents(view: SettingsView) {
        super.bindIntents(view)

        view.preferenceClicks()
                .autoDisposable(view.scope())
                .subscribe {
                    Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

                    when (it.id) {
                        R.id.theme -> view.showThemePicker()

                        R.id.night -> view.showNightModeDialog()

                        R.id.nightStart -> {
                            val date = nightModeManager.parseTime(prefs.nightStart.get())
                            view.showStartTimePicker(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                        }

                        R.id.nightEnd -> {
                            val date = nightModeManager.parseTime(prefs.nightEnd.get())
                            view.showEndTimePicker(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                        }

                        R.id.black -> prefs.black.set(!prefs.black.get())

                        R.id.autoEmoji -> prefs.autoEmoji.set(!prefs.autoEmoji.get())

                        R.id.alarm -> navigator.showAlarmChooseSettings()

                        R.id.notifications -> navigator.showNotificationSettings()

                        R.id.swipeActions -> view.showSwipeActions()

                        R.id.delayed -> view.showDelayDurationDialog()

                        R.id.delivery -> prefs.delivery.set(!prefs.delivery.get())

                        R.id.signature -> view.showSignatureDialog(prefs.signature.get())

                        R.id.textSize -> view.showTextSizePicker()

                        R.id.autoColor -> {
                            analytics.setUserProperty("Preference: Auto Color", !prefs.autoColor.get())
                            prefs.autoColor.set(!prefs.autoColor.get())
                        }

                        R.id.systemFont -> prefs.systemFont.set(!prefs.systemFont.get())

//                        R.id.unicode -> prefs.unicode.set(!prefs.unicode.get())

                        R.id.mobileOnly -> prefs.mobileOnly.set(!prefs.mobileOnly.get())

                        R.id.longAsMms -> prefs.longAsMms.set(!prefs.longAsMms.get())

                        R.id.mmsSize -> view.showMmsSizePicker()

                        R.id.sync -> syncMessages.execute(Unit)

                        R.id.statistic -> view.statistic()

                        R.id.isAvatar -> view.setShowAvatar()
//                        R.id.about -> view.showAbout()
                    }
                }

//        view.aboutLongClicks()
//                .map { !prefs.logging.get() }
//                .doOnNext { enabled -> prefs.logging.set(enabled) }
//                .autoDisposable(view.scope())
//                .subscribe { enabled ->
//                    context.makeToast(when (enabled) {
//                        true -> R.string.settings_logging_enabled
//                        false -> R.string.settings_logging_disabled
//                    })
//                }

        view.setShowAvatar()
                .autoDisposable(view.scope())
                .subscribe {
                    prefs.showAvatar.set(!prefs.showAvatar.get())
                }

        view.statistic()
                .autoDisposable(view.scope())
                .subscribe {
                    val totalSmsCount = Realm.getDefaultInstance().where(Message::class.java).equalTo("type", "sms").count()
                    val readSmsCount = Realm.getDefaultInstance().where(Message::class.java).equalTo("type", "sms").equalTo("read", true).count()
                    val totalMmsCount = Realm.getDefaultInstance().where(Message::class.java).equalTo("type", "mms").count()
                    val readMmsCount = Realm.getDefaultInstance().where(Message::class.java).equalTo("type", "mms").equalTo("read", true).count()

                    view.statisticDialog(
                            "sms count: $totalSmsCount\nsms unread: $readSmsCount" +
                                    "\nmms count: $totalMmsCount\nmms unread: $readMmsCount"
                    )?.show()
                }

        view.nightModeSelected()
                .withLatestFrom(billingManager.upgradeStatus) { mode, upgraded ->
                    if (!upgraded && mode == Preferences.NIGHT_MODE_AUTO) {
                        view.showQksmsPlusSnackbar()
                    } else {
                        nightModeManager.updateNightMode(mode)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.viewQksmsPlusClicks()
                .autoDisposable(view.scope())
                .subscribe { navigator.showQksmsPlusActivity("settings_night") }

        view.nightStartSelected()
                .autoDisposable(view.scope())
                .subscribe { nightModeManager.setNightStart(it.first, it.second) }

        view.nightEndSelected()
                .autoDisposable(view.scope())
                .subscribe { nightModeManager.setNightEnd(it.first, it.second) }

        view.textSizeSelected()
                .autoDisposable(view.scope())
                .subscribe(prefs.textSize::set)

        view.sendDelaySelected()
                .withLatestFrom(billingManager.upgradeStatus) { duration, upgraded ->
                    if (!upgraded && duration != 0) {
                        view.showQksmsPlusSnackbar()
                    } else {
                        prefs.sendDelay.set(duration)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.signatureSet()
                .doOnNext(prefs.signature::set)
                .autoDisposable(view.scope())
                .subscribe()

        view.mmsSizeSelected()
                .autoDisposable(view.scope())
                .subscribe(prefs.mmsSize::set)
    }

}