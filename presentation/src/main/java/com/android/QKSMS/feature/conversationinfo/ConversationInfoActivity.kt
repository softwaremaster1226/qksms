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
package com.android.QKSMS.feature.conversationinfo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.extensions.viewBinding
import com.android.QKSMS.databinding.ContainerActivityBinding
import com.android.QKSMS.model.Contact
import com.android.QKSMS.model.Conversation
import dagger.android.AndroidInjection
import io.realm.Realm

class ConversationInfoActivity : QkThemedActivity() {
    companion object {
        val PICKER_RESULT = 1000
    }

    private val binding by viewBinding(ContainerActivityBinding::inflate)
    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        router = Conductor.attachRouter(this, binding.container, savedInstanceState)
        if (!router.hasRootController()) {
            val threadId = intent.extras?.getLong("threadId") ?: 0L
            router.setRoot(RouterTransaction.with(ConversationInfoController(threadId)))
        }
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICKER_RESULT && resultCode == Activity.RESULT_OK) {
            val id = data?.getLongExtra("id", -1)
            val uri = data?.data
            Log.d("-------------", id.toString())
            Realm.getDefaultInstance().use {
                it.where(Conversation::class.java).equalTo("id", id).findFirst()?.run {
                    this.recipients
                }
            }
        }
    }
}