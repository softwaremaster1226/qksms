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
package com.android.QKSMS.feature.settings.about

import android.view.View
import com.android.QKSMS.BuildConfig
import com.android.QKSMS.R
import com.android.QKSMS.common.base.QkController
import com.android.QKSMS.common.widget.PreferenceView
import com.android.QKSMS.databinding.AboutControllerBinding
import com.android.QKSMS.injection.appComponent
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import javax.inject.Inject

class AboutController : QkController<AboutView, Unit, AboutPresenter, AboutControllerBinding>(
        AboutControllerBinding::inflate
), AboutView {

    @Inject override lateinit var presenter: AboutPresenter

    init {
        appComponent.inject(this)
    }

    override fun onViewCreated() {
        binding.version.summary = BuildConfig.VERSION_NAME
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.about_title)
        showBackButton(true)
    }

    override fun preferenceClicks(): Observable<PreferenceView> = (0 until binding.preferences.childCount)
            .map { index -> binding.preferences.getChildAt(index) }
            .mapNotNull { view -> view as? PreferenceView }
            .map { preference -> preference.clicks().map { preference } }
            .let { preferences -> Observable.merge(preferences) }

    override fun render(state: Unit) {
        // No special rendering required
    }

}