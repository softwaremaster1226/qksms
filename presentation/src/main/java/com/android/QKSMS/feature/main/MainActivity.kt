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
package com.android.QKSMS.feature.main

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import com.android.QKSMS.R
import com.android.QKSMS.common.Navigator
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.*
import com.android.QKSMS.databinding.MainActivityBinding
import com.android.QKSMS.feature.blocking.BlockingDialog
import com.android.QKSMS.feature.changelog.ChangelogDialog
import com.android.QKSMS.feature.compose.ComposeActivity
import com.android.QKSMS.feature.conversations.ConversationItemTouchCallback
import com.android.QKSMS.feature.conversations.ConversationsAdapter
import com.android.QKSMS.interactor.MarkUnLocked
import com.android.QKSMS.manager.ChangelogManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.vanniktech.emoji.EmojiManager.install
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

class MainActivity : QkThemedActivity(), MainView {

    @Inject lateinit var blockingDialog: BlockingDialog
    @Inject lateinit var disposables: CompositeDisposable
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var conversationsAdapter: ConversationsAdapter
    @Inject lateinit var drawerBadgesExperiment: DrawerBadgesExperiment
    @Inject lateinit var searchAdapter: SearchAdapter
    @Inject lateinit var itemTouchCallback: ConversationItemTouchCallback
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var markUnLocked: MarkUnLocked

    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent by lazy { binding.toolbarSearch.textChanges() }
    override val composeIntent by lazy { binding.compose.clicks() }
    private lateinit var gesture1: ScaleGestureDetector
    private lateinit var gesture2: ScaleGestureDetector
    val listener: MyOnScaleGestureListener = MyOnScaleGestureListener()
//    override val drawerOpenIntent: Observable<Boolean> by lazy {
//        binding.drawerLayout
//                .drawerOpen(Gravity.START)
//                .doOnNext { dismissKeyboard() }
//    }
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val navigationIntent: Observable<NavItem> by lazy {
        Observable.merge(listOf(
                backPressedSubject
//                binding.drawer.inbox.clicks().map { NavItem.INBOX },
//                binding.drawer.archived.clicks().map { NavItem.ARCHIVED },
////                binding.drawer.backup.clicks().map { NavItem.BACKUP },
//                binding.drawer.scheduled.clicks().map { NavItem.SCHEDULED },
//                binding.drawer.blocking.clicks().map { NavItem.BLOCKING },
//                binding.drawer.settings.clicks().map { NavItem.SETTINGS }
////                binding.drawer.plus.clicks().map { NavItem.PLUS },
////                binding.drawer.help.clicks().map { NavItem.HELP },
////                binding.drawer.invite.clicks().map { NavItem.INVITE }
        ))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
//    override val plusBannerIntent by lazy { binding.drawer.plusBanner.clicks() }
//    override val dismissRatingIntent by lazy { binding.drawer.rateDismiss.clicks() }
//    override val rateIntent by lazy { binding.drawer.rateOkay.clicks() }
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val swipeConversationIntent by lazy { itemTouchCallback.swipes }
    override val changelogMoreIntent by lazy { changelogDialog.moreClicks }
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent by lazy { binding.snackbar.button.clicks() }

    private val binding by viewBinding(MainActivityBinding::inflate)
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java] }
//    private val toggle by lazy { ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.main_drawer_open_cd, 0) }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchCallback) }
    private val changelogDialog by lazy { ChangelogDialog(this) }
    private val backPressedSubject: Subject<NavItem> = PublishSubject.create()

    private lateinit var conversations: List<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.bindView(this)
        onNewIntentIntent.onNext(intent)

        install(GoogleEmojiProvider())

//        toggle.syncState()

//        val params = binding.collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
//        params.scrollFlags = 0
//        binding.collapsingToolbar.layoutParams = params

        binding.appBarLayout.setExpanded(false, false)
        binding.appBarLayout.isActivated = false
        binding.toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }

        binding.toolbar.showOverflowMenu()

        itemTouchCallback.adapter = conversationsAdapter
        conversationsAdapter.autoScrollToStart(binding.recyclerView)

        // Don't allow clicks to pass through the drawer layout
//        binding.drawer.root.clicks().autoDisposable(scope()).subscribe()

        // Set the theme color tint to the recyclerView, progressbar, and FAB
        theme
                .autoDisposable(scope())
                .subscribe { theme ->
                    // Set the color for the drawer icons
//                    val states = arrayOf(
//                            intArrayOf(android.R.attr.state_activated),
//                            intArrayOf(-android.R.attr.state_activated))

//                    resolveThemeColor(android.R.attr.textColorSecondary)
//                            .let { textSecondary -> ColorStateList(states, intArrayOf(theme.theme, textSecondary)) }
//                            .let { tintList ->
//                                binding.drawer.inboxIcon.imageTintList = tintList
//                                binding.drawer.archivedIcon.imageTintList = tintList
//                            }

                    // Miscellaneous views binding.drawer.plusBadge1,
//                    listOf(binding.drawer.plusBadge2).forEach { badge ->
//                        badge.setBackgroundTint(theme.theme)
//                        badge.setTextColor(theme.textPrimary)
//                    }
//                    binding.drawer.plusIcon.setTint(theme.theme)
//                    binding.drawer.rateIcon.setTint(theme.theme)
                    binding.compose.setBackgroundTint(theme.theme)

                    // Set the FAB compose icon color
//                    binding.compose.setTint(theme.textPrimary)
                }

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            binding.toolbarSearch.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }

        gesture1 = ScaleGestureDetector(this, listener)
        gesture2 = ScaleGestureDetector(this, listener)
        binding.root.setOnTouchListener { _, event ->
            gesture1.onTouchEvent(event)
        }
//        if(!hasPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), REQUEST_STORAGE);
//        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gesture1.onTouchEvent(event)
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run(onNewIntentIntent::onNext)
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            finish()
            return
        }

        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        val markLocked = when (state.page) {
            is Inbox -> state.page.locked
            is Archived -> state.page.locked
            else -> false
        }

        binding.toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
//        binding.toolbarTitle.setVisible(binding.toolbarSearch.visibility != View.VISIBLE)

        binding.toolbar.menu.findItem(R.id.archive)?.isVisible = state.page is Inbox && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unarchive)?.isVisible = state.page is Archived && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.lock)?.isVisible = !markLocked && selectedConversations != 0
        binding.toolbar.menu.findItem(R.id.unlock)?.isVisible = markLocked && selectedConversations != 0

        val flag = binding.toolbarSearch.isVisible
        binding.toolbar.menu.findItem(R.id.inbox)?.isVisible = flag
        binding.toolbar.menu.findItem(R.id.scheduled)?.isVisible = flag
        binding.toolbar.menu.findItem(R.id.archived)?.isVisible = flag
        binding.toolbar.menu.findItem(R.id.settings)?.isVisible = flag
        binding.toolbar.menu.findItem(R.id.blocking)?.isVisible = flag

//        binding.drawer.plusBadge1,
//        listOf(binding.drawer.plusBadge2).forEach { badge ->
//            badge.isVisible = drawerBadgesExperiment.variant && !state.upgraded
//        }
//        binding.drawer.plus.isVisible = state.upgraded
//        binding.drawer.plusBanner.isVisible = !state.upgraded
//        binding.drawer.rateLayout.setVisible(state.showRating)

        binding.compose.setVisible(state.page is Inbox || state.page is Archived)
        conversationsAdapter.emptyView = binding.empty.takeIf { state.page is Inbox || state.page is Archived }
        searchAdapter.emptyView = binding.empty.takeIf { state.page is Searching }

        when (state.page) {
            is Inbox -> {
                showBackButton(state.page.selected > 0)
                title = if (state.page.selected == 0) {
                    getString(R.string.app_name)
                } else {
                    getString(R.string.main_title_selected, state.page.selected)
                }
                if (binding.recyclerView.adapter !== conversationsAdapter) binding.recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                binding.empty.setText(R.string.inbox_empty_text)
            }

            is Searching -> {
                showBackButton(true)
                if (binding.recyclerView.adapter !== searchAdapter) binding.recyclerView.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                itemTouchHelper.attachToRecyclerView(null)
                binding.empty.setText(R.string.inbox_search_empty_text)
            }

            is Archived -> {
                showBackButton(true)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (binding.recyclerView.adapter !== conversationsAdapter) binding.recyclerView.adapter = conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                itemTouchHelper.attachToRecyclerView(null)
                binding.empty.setText(R.string.archived_empty_text)
            }
        }

//        binding.drawer.inbox.isActivated = state.page is Inbox
//        binding.drawer.archived.isActivated = state.page is Archived

//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START) && !state.drawerOpen) {
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//        } else if (!binding.drawerLayout.isDrawerVisible(GravityCompat.START) && state.drawerOpen) {
//            binding.drawerLayout.openDrawer(GravityCompat.START)
//        }

        when {
            !state.defaultSms -> {
                binding.snackbar.title.setText(R.string.main_default_sms_title)
                binding.snackbar.message.setText(R.string.main_default_sms_message)
                binding.snackbar.button.setText(R.string.main_default_sms_change)
            }

            !state.smsPermission -> {
                binding.snackbar.title.setText(R.string.main_permission_required)
                binding.snackbar.message.setText(R.string.main_permission_sms)
                binding.snackbar.button.setText(R.string.main_permission_allow)
            }

            !state.contactPermission -> {
                binding.snackbar.title.setText(R.string.main_permission_required)
                binding.snackbar.message.setText(R.string.main_permission_contacts)
                binding.snackbar.button.setText(R.string.main_permission_allow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun showBackButton(show: Boolean) {
//        toggle.onDrawerSlide(binding.drawer.root, if (show) 1f else 0f)
//        toggle.drawerArrowDrawable.color = when (show) {
//            true -> resolveThemeColor(android.R.attr.textColorSecondary)
//            false -> resolveThemeColor(android.R.attr.textColorPrimary)
//        }
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR), 0)
    }

    override fun clearSearch() {
        dismissKeyboard()
        binding.toolbarSearch.text = null
    }

    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun themeChanged() {
        binding.recyclerView.scrapViews()
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(this, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        var locked = 0
        conversations.map {
            if (conversationRepo.getConversation(it)?.locked!!) {
                locked++
            }
        }
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(resources.getQuantityString(R.plurals.dialog_delete_message, count, count))
                .setPositiveButton(R.string.button_delete) { _, _ ->
                    if (isSecure() && locked != 0) {
                        checkSecurityForDelete(conversations)
                        clearSelection()
                    } else {
                        confirmDeleteIntent.onNext(conversations)
                    }
                }
                .setNegativeButton(R.string.button_cancel, null)
                .create()
        Utils.adjustAlertDialog(dialog, resources.getDrawable(R.drawable.chip_background))
        dialog.show()
    }

    override fun showChangelog(changelog: ChangelogManager.Changelog) {
        changelogDialog.show(changelog)
    }

    override fun showArchivedSnackbar() {
//        Snackbar.make(binding.drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
//            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
//            setActionTextColor(colors.theme().theme)
//            show()
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onBackPressed() {
        backPressedSubject.onNext(NavItem.BACK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ComposeActivity.CheckSecurityRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                markUnLocked.execute(conversations)
            }
        } else if (requestCode == ComposeActivity.CheckSecurityForDeleteRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                confirmDeleteIntent.onNext(conversations)
            }
        }
    }

    override fun checkSecurity(conversations: List<Long>) {
        this.conversations = conversations
        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardSecure) {
            val authIntent: Intent = keyguardManager.createConfirmDeviceCredentialIntent("Enter password", "Enter password to see this conversation")
            startActivityForResult(authIntent, ComposeActivity.CheckSecurityRequestCode)
        }
    }

    override fun checkSecurityForDelete(conversations: List<Long>) {
        this.conversations = conversations
        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardSecure) {
            val authIntent: Intent = keyguardManager.createConfirmDeviceCredentialIntent("Enter password", "Enter password to see this conversation")
            startActivityForResult(authIntent, ComposeActivity.CheckSecurityForDeleteRequestCode)
        }
    }

    override fun isSecure(): Boolean {
        return (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardSecure
    }

    override fun toSecuritySetting() {
        Toast.makeText(this.applicationContext, "You must set security for lock!. After that lock again.", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        startActivity(intent)
    }

    inner class MyOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor > 1) {
                conversationsAdapter.updateData(conversationRepo.getConversations(archived = false, locked = true))
            } else {
                conversationsAdapter.updateData(conversationRepo.getConversations())
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {}
    }
}
