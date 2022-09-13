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

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.format.DateFormat
import android.view.View
import androidx.core.view.isVisible
import com.bluelinelabs.conductor.RouterTransaction
import com.google.android.material.snackbar.Snackbar
import com.android.QKSMS.BuildConfig
import com.android.QKSMS.R
import com.android.QKSMS.common.MenuItem
import com.android.QKSMS.common.QkChangeHandler
import com.android.QKSMS.common.QkDialog
import com.android.QKSMS.common.base.QkController
import com.android.QKSMS.common.util.Colors
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.animateLayoutChanges
import com.android.QKSMS.common.util.extensions.setBackgroundTint
import com.android.QKSMS.common.util.extensions.setVisible
import com.android.QKSMS.common.widget.FieldDialog
import com.android.QKSMS.common.widget.PreferenceView
import com.android.QKSMS.common.widget.QkSwitch
import com.android.QKSMS.databinding.SettingsControllerBinding
import com.android.QKSMS.feature.settings.about.AboutController
import com.android.QKSMS.feature.settings.swipe.SwipeActionsController
import com.android.QKSMS.feature.themepicker.ThemePickerController
import com.android.QKSMS.injection.appComponent
import com.android.QKSMS.repository.SyncRepository
import com.android.QKSMS.util.Preferences
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class SettingsController : QkController<SettingsView, SettingsState, SettingsPresenter, SettingsControllerBinding>(
        SettingsControllerBinding::inflate
), SettingsView {

    @Inject lateinit var context: Context
    @Inject lateinit var colors: Colors
    @Inject lateinit var nightModeDialog: QkDialog
    @Inject lateinit var textSizeDialog: QkDialog
    @Inject lateinit var sendDelayDialog: QkDialog
    @Inject lateinit var mmsSizeDialog: QkDialog

    @Inject override lateinit var presenter: SettingsPresenter

    private val signatureDialog: FieldDialog by lazy {
        FieldDialog(activity!!, context.getString(R.string.settings_signature_title), signatureSubject::onNext)
    }

    private val viewQksmsPlusSubject: Subject<Unit> = PublishSubject.create()
    private val startTimeSelectedSubject: Subject<Pair<Int, Int>> = PublishSubject.create()
    private val endTimeSelectedSubject: Subject<Pair<Int, Int>> = PublishSubject.create()
    private val signatureSubject: Subject<String> = PublishSubject.create()

    private val progressAnimator by lazy { ObjectAnimator.ofInt(binding.syncingProgress, "progress", 0, 0) }

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH

        colors.themeObservable()
                .autoDisposable(scope())
                .subscribe { activity?.recreate() }
    }

    override fun onViewCreated() {
        binding.preferences.postDelayed({ binding.preferences.animateLayoutChanges = true }, 100)

        when (Build.VERSION.SDK_INT >= 29) {
            true -> nightModeDialog.adapter.setData(R.array.night_modes)
            false -> nightModeDialog.adapter.data = context.resources.getStringArray(R.array.night_modes)
                    .mapIndexed { index, title -> MenuItem(title, index) }
                    .drop(1)
        }
        textSizeDialog.adapter.setData(R.array.text_sizes)
        sendDelayDialog.adapter.setData(R.array.delayed_sending_labels)
        mmsSizeDialog.adapter.setData(R.array.mms_sizes, R.array.mms_sizes_ids)

//        binding.about.summary = context.getString(R.string.settings_version, BuildConfig.VERSION_NAME)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.title_settings)
        showBackButton(true)
    }

    override fun preferenceClicks(): Observable<PreferenceView> = (0 until binding.preferences.childCount)
            .map { index -> binding.preferences.getChildAt(index) }
            .mapNotNull { view -> view as? PreferenceView }
            .map { preference -> preference.clicks().map { preference } }
            .let { preferences -> Observable.merge(preferences) }

//    override fun aboutLongClicks(): Observable<*> = binding.about.longClicks()
    override fun statistic(): Observable<*> = binding.statistic.clicks()
    override fun setShowAvatar(): Observable<*> = binding.isAvatar.clicks()

    override fun statisticDialog(content: String): androidx.appcompat.app.AlertDialog? {
        val dialog = activity?.let {
            androidx.appcompat.app.AlertDialog.Builder(it)
                .setTitle("Statistic")
                .setMessage(content)
                .setNegativeButton(context.resources.getText(R.string.imagepicker_multiselect_done)) { dialogInterface: DialogInterface, i: Int ->
                    return@setNegativeButton
                }
                .create()
        }
        Utils.adjustAlertDialog(dialog, context.getDrawable(R.drawable.chip_background))
        return dialog
    }

    override fun viewQksmsPlusClicks(): Observable<*> = viewQksmsPlusSubject

    override fun nightModeSelected(): Observable<Int> = nightModeDialog.adapter.menuItemClicks

    override fun nightStartSelected(): Observable<Pair<Int, Int>> = startTimeSelectedSubject

    override fun nightEndSelected(): Observable<Pair<Int, Int>> = endTimeSelectedSubject

    override fun textSizeSelected(): Observable<Int> = textSizeDialog.adapter.menuItemClicks

    override fun sendDelaySelected(): Observable<Int> = sendDelayDialog.adapter.menuItemClicks

    override fun signatureSet(): Observable<String> = signatureSubject

    override fun mmsSizeSelected(): Observable<Int> = mmsSizeDialog.adapter.menuItemClicks

    override fun render(state: SettingsState) {
        binding.theme.widget<View>().setBackgroundTint(state.theme)
        binding.night.summary = state.nightModeSummary
        nightModeDialog.adapter.selectedItem = state.nightModeId
        binding.nightStart.setVisible(state.nightModeId == Preferences.NIGHT_MODE_AUTO)
        binding.nightStart.summary = state.nightStart
        binding.nightEnd.setVisible(state.nightModeId == Preferences.NIGHT_MODE_AUTO)
        binding.nightEnd.summary = state.nightEnd

        binding.black.setVisible(state.nightModeId != Preferences.NIGHT_MODE_OFF)
        binding.black.widget<QkSwitch>().isChecked = state.black

        binding.autoEmoji.widget<QkSwitch>().isChecked = state.autoEmojiEnabled

        binding.delayed.summary = state.sendDelaySummary
        sendDelayDialog.adapter.selectedItem = state.sendDelayId

        binding.delivery.widget<QkSwitch>().isChecked = state.deliveryEnabled

        binding.signature.summary = state.signature.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.settings_signature_summary)

        binding.textSize.summary = state.textSizeSummary
        textSizeDialog.adapter.selectedItem = state.textSizeId

        binding.autoColor.widget<QkSwitch>().isChecked = state.autoColor

        binding.systemFont.widget<QkSwitch>().isChecked = state.systemFontEnabled

//        binding.unicode.widget<QkSwitch>().isChecked = state.stripUnicodeEnabled
        binding.mobileOnly.widget<QkSwitch>().isChecked = state.mobileOnly
        binding.longAsMms.widget<QkSwitch>().isChecked = state.longAsMms

        binding.isAvatar.widget<QkSwitch>().isChecked = state.showAvatar

        binding.mmsSize.summary = state.maxMmsSizeSummary
        mmsSizeDialog.adapter.selectedItem = state.maxMmsSizeId

        when (state.syncProgress) {
            is SyncRepository.SyncProgress.Idle -> binding.syncingProgress.isVisible = false

            is SyncRepository.SyncProgress.Running -> {
                binding.syncingProgress.isVisible = true
                binding.syncingProgress.max = state.syncProgress.max
                progressAnimator.apply { setIntValues(binding.syncingProgress.progress, state.syncProgress.progress) }.start()
                binding.syncingProgress.isIndeterminate = state.syncProgress.indeterminate
            }
        }
    }

    override fun showQksmsPlusSnackbar() {
        view?.run {
            Snackbar.make(binding.root, R.string.toast_qksms_plus, Snackbar.LENGTH_LONG).run {
                setAction(R.string.button_more) { viewQksmsPlusSubject.onNext(Unit) }
                setActionTextColor(colors.theme().theme)
                show()
            }
        }
    }

    // TODO change this to a PopupWindow
    override fun showNightModeDialog() = nightModeDialog.show(activity!!)

    override fun showStartTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { _, newHour, newMinute ->
            startTimeSelectedSubject.onNext(Pair(newHour, newMinute))
        }, hour, minute, DateFormat.is24HourFormat(activity)).show()
    }

    override fun showEndTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { _, newHour, newMinute ->
            endTimeSelectedSubject.onNext(Pair(newHour, newMinute))
        }, hour, minute, DateFormat.is24HourFormat(activity)).show()
    }

    override fun showTextSizePicker() = textSizeDialog.show(activity!!)

    override fun showDelayDurationDialog() = sendDelayDialog.show(activity!!)

    override fun showSignatureDialog(signature: String) = signatureDialog.setText(signature).show()

    override fun showMmsSizePicker() = mmsSizeDialog.show(activity!!)

    override fun showSwipeActions() {
        router.pushController(RouterTransaction.with(SwipeActionsController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun showThemePicker() {
        router.pushController(RouterTransaction.with(ThemePickerController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

    override fun showAbout() {
        router.pushController(RouterTransaction.with(AboutController())
                .pushChangeHandler(QkChangeHandler())
                .popChangeHandler(QkChangeHandler()))
    }

}