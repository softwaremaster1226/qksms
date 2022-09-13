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
package com.android.QKSMS.feature.compose

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.KeyguardManager
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.QKSMS.R
import com.android.QKSMS.common.Navigator
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.DateFormatter
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.*
import com.android.QKSMS.databinding.ComposeActivityBinding
import com.android.QKSMS.feature.calendarevent.CalendarEventActivity
import com.android.QKSMS.feature.compose.editing.ChipsAdapter
import com.android.QKSMS.feature.contacts.ContactsActivity
import com.android.QKSMS.feature.contactsplus.ContactsPlusActivity
import com.android.QKSMS.feature.fragment.*
import com.android.QKSMS.feature.quick.QuickMessageActivity
import com.android.QKSMS.model.Attachment
import com.android.QKSMS.model.Recipient
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.mms.ContentType
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.vanniktech.emoji.EmojiPopup
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class ComposeActivity : QkThemedActivity(), ComposeView, CameraFragment.PhotoFromCamera, RecordClickListener {

    companion object {
        const val SelectContactRequestCode = 0
        const val TakePhotoRequestCode = 1
        const val AttachPhotoRequestCode = 2
        const val AttachContactRequestCode = 3
        const val AttachAudioRequestCode = 4
        const val AttachQuickMessageRequestCode = 5
        const val CalendarEventRequestCode = 6
        const val CheckSecurityRequestCode = 7
        const val CheckSecurityForDeleteRequestCode = 8

        const val CameraDestinationKey = "camera_destination"
    }

    private var checked: Boolean = false
    @Inject lateinit var attachmentAdapter: AttachmentAdapter
    @Inject lateinit var chipsAdapter: ChipsAdapter
    @Inject lateinit var dateFormatter: DateFormatter
    @Inject lateinit var messageAdapter: MessagesAdapter
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val chipsSelectedIntent: Subject<HashMap<String, String?>> = PublishSubject.create()
    override val chipDeletedIntent: Subject<Recipient> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val sendAsGroupIntent by lazy { binding.sendAsGroupBackground.clicks() }
    override val messageClickIntent: Subject<Long> by lazy { messageAdapter.clicks }
    override val messagePartClickIntent: Subject<Long> by lazy { messageAdapter.partClicks }
    override val messagesSelectedIntent by lazy { messageAdapter.selectionChanges }
    override val cancelSendingIntent: Subject<Long> by lazy { messageAdapter.cancelSending }
    override val attachmentDeletedIntent: Subject<Attachment> by lazy { attachmentAdapter.attachmentDeleted }
    override val textChangedIntent by lazy { binding.message.textChanges() }
    override val attachIntent by lazy { binding.attach.clicks() } //{ Observable.merge(binding.attach.clicks(), binding.attachingBackground.clicks()) }
//    override val cameraIntent by lazy { Observable.merge(binding.camera.clicks(), binding.cameraLabel.clicks()) }
//    override val galleryIntent by lazy { Observable.merge(binding.gallery.clicks(), binding.galleryLabel.clicks()) }
//    override val scheduleIntent by lazy { Observable.merge(binding.schedule.clicks(), binding.scheduleLabel.clicks()) }
//    override val attachContactIntent by lazy { Observable.merge(binding.contact.clicks(), binding.contactLabel.clicks()) }
    override val attachmentSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val contactSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val inputContentIntent by lazy { binding.message.inputContentSelected }
    override val scheduleSelectedIntent: Subject<Long> = PublishSubject.create()
    override val changeSimIntent by lazy { binding.sim.clicks() }
    override val scheduleCancelIntent by lazy { binding.scheduledCancel.clicks() }
    override val sendIntent by lazy { binding.send.clicks() }
    override val viewQksmsPlusIntent: Subject<Unit> = PublishSubject.create()
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()

    override val emoji by lazy { binding.emoji.clicks() }
    override val emojiPopup by lazy { EmojiPopup.Builder.fromRootView(binding.contentView).build(binding.message) }
    override val audioContentIntent: Subject<Uri> = PublishSubject.create()
    override val messageIntent by lazy { binding.message.clicks() }

    private val binding by viewBinding(ComposeActivityBinding::inflate)
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[ComposeViewModel::class.java] }

    private var cameraDestination: Uri? = null

    private var initial: Int = 0
    private var iheight: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showBackButton(true)
        viewModel.bindView(this)

//        binding.contentView.layoutTransition = LayoutTransition().apply {
//            disableTransitionType(LayoutTransition.CHANGING)
//        }
        initial = 200.dpToPx(applicationContext)
        iheight = windowManager.defaultDisplay.height

        chipsAdapter.view = binding.chips

        binding.chips.itemAnimator = null
        binding.chips.layoutManager = FlexboxLayoutManager(this)

        messageAdapter.autoScrollToStart(binding.messageList)
        messageAdapter.emptyView = binding.messagesEmpty

        binding.messageList.setHasFixedSize(true)
        binding.messageList.adapter = messageAdapter

        binding.attachments.adapter = attachmentAdapter

        binding.message.supportsInputContent = true

        theme
                .doOnNext { binding.loading.setTint(it.theme) }
                .doOnNext { binding.attach.setBackgroundTint(it.theme) }
                .doOnNext { binding.attach.setTint(it.textPrimary) }
                .doOnNext { messageAdapter.theme = it }
                .autoDisposable(scope())
                .subscribe()

        window.callback = ComposeWindowCallback(window.callback, this)

        // These theme attributes don't apply themselves on API 21
        if (Build.VERSION.SDK_INT <= 22) {
            binding.messageBackground.setBackgroundTint(resolveThemeColor(R.attr.bubbleColor))
        }
        val pager = PagerAdapter(supportFragmentManager)
        binding.pager.adapter = pager
        binding.tabLayout.setupWithViewPager(binding.pager)
        for (i in 0 until binding.tabLayout.tabCount) {
            binding.tabLayout.getTabAt(i)?.icon = when(i) {
                0 -> resources.getDrawable(R.drawable.ic_camera)
                1 -> resources.getDrawable(R.drawable.ic_gallery)
                2 -> resources.getDrawable(R.drawable.ic_record)
                3 -> resources.getDrawable(R.drawable.ic_more_vert_black_24dp)
                else -> resources.getDrawable(R.drawable.ic_more_vert_black_24dp)
            }
        }
        var oY = 0f
        binding.pager.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oY = event.y
                }
                MotionEvent.ACTION_MOVE -> v.updateLayoutParams {
                    this.height += (oY - event.y).toInt() + 1
                }
                MotionEvent.ACTION_UP -> {
                    v.updateLayoutParams {
                        if (this.height > initial + 100) {
                            this.height = iheight
                        } else {
                            this.height = initial
                        }
                    }
                }
            }
            super.onTouchEvent(event)
        }
    }

    override fun onStart() {
        super.onStart()
        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    override fun render(state: ComposeState) {
        if (state.hasError) {
            finish()
            return
        }

        if (intent.extras?.getBoolean("locked")!! && !checked) Thread.sleep(200)

        threadId.onNext(state.threadId)

        title = when {
            state.selectedMessages > 0 -> getString(R.string.compose_title_selected, state.selectedMessages)
            state.query.isNotEmpty() -> state.query
            else -> state.conversationtitle
        }

        binding.toolbarSubtitle.setVisible(state.query.isNotEmpty())
        binding.toolbarSubtitle.text = getString(R.string.compose_subtitle_results, state.searchSelectionPosition,
                state.searchResults)

        binding.toolbarTitle.setVisible(!state.editingMode)
        binding.chips.setVisible(state.editingMode)
        binding.composeBar.setVisible(!state.loading)

        // Don't set the adapters unless needed
        if (state.editingMode && binding.chips.adapter == null) binding.chips.adapter = chipsAdapter

        binding.toolbar.menu.findItem(R.id.add)?.isVisible = state.editingMode
        binding.toolbar.menu.findItem(R.id.call)?.isVisible = !state.editingMode && state.selectedMessages == 0
                && state.query.isEmpty()
        binding.toolbar.menu.findItem(R.id.info)?.isVisible = !state.editingMode && state.selectedMessages == 0
                && state.query.isEmpty()
        binding.toolbar.menu.findItem(R.id.copy)?.isVisible = !state.editingMode && state.selectedMessages > 0
        binding.toolbar.menu.findItem(R.id.details)?.isVisible = !state.editingMode && state.selectedMessages == 1
        binding.toolbar.menu.findItem(R.id.delete)?.isVisible = !state.editingMode && state.selectedMessages > 0
        binding.toolbar.menu.findItem(R.id.forward)?.isVisible = !state.editingMode && state.selectedMessages == 1
        binding.toolbar.menu.findItem(R.id.previous)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()
        binding.toolbar.menu.findItem(R.id.next)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()
        binding.toolbar.menu.findItem(R.id.clear)?.isVisible = state.selectedMessages == 0 && state.query.isNotEmpty()

        chipsAdapter.data = state.selectedChips

        binding.loading.setVisible(state.loading)

        binding.sendAsGroup.setVisible(state.editingMode && state.selectedChips.size >= 2)
        binding.sendAsGroupSwitch.isChecked = state.sendAsGroup

        binding.messageList.setVisible(!state.editingMode || state.sendAsGroup || state.selectedChips.size == 1)
        messageAdapter.data = state.messages
        messageAdapter.highlight = state.searchSelectionId

        binding.scheduledGroup.isVisible = state.scheduled != 0L
        binding.scheduledTime.text = dateFormatter.getScheduledTimestamp(state.scheduled)

        binding.attachments.setVisible(state.attachments.isNotEmpty())
        attachmentAdapter.data = state.attachments

        binding.attach.animate().rotation(if (state.attaching) 135f else 0f).start()
//        binding.attaching.isVisible = state.attaching
        if(state.attaching) {
            binding.pager.updateLayoutParams {
                this.height = initial
            }
            binding.pager.visibility = View.VISIBLE
            binding.message.hideKeyboard()
        } else {
            binding.pager.visibility = View.GONE
        }

        binding.counter.text = state.remaining
        binding.counter.setVisible(binding.counter.text.isNotBlank())

        binding.sim.setVisible(state.subscription != null)
        binding.sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        binding.simIndex.text = state.subscription?.simSlotIndex?.plus(1)?.toString()

        binding.send.isEnabled = state.canSend
        binding.send.imageAlpha = if (state.canSend) 255 else 128

    }

    override fun clearSelection() = messageAdapter.clearSelection()

    override fun showDetails(details: String) {
        AlertDialog.Builder(this)
                .setTitle(R.string.compose_details_title)
                .setMessage(details)
                .setCancelable(true)
                .show()
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    override fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
    }

    override fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS), 0)
    }

    private fun requestCalendarPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR), 0)
    }

    override fun requestDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                scheduleSelectedIntent.onNext(calendar.timeInMillis)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(this))
                    .show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun requestContact() {
        val intent = Intent(Intent.ACTION_PICK)
                .setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

        startActivityForResult(Intent.createChooser(intent, null), AttachContactRequestCode)
    }

    override fun showContacts(sharing: Boolean, chips: List<Recipient>) {
        binding.message.hideKeyboard()
        val serialized = HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        val intent = Intent(this, ContactsActivity::class.java)
                .putExtra(ContactsActivity.SharingKey, sharing)
                .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    override fun showContactsPlus(sharing: Boolean, chips: List<Recipient>) {
        binding.message.hideKeyboard()
        val serialized = HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        val intent = Intent(this, ContactsPlusActivity::class.java)
                .putExtra(ContactsActivity.SharingKey, sharing)
                .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    override fun themeChanged() {
        binding.messageList.scrapViews()
    }

    override fun showKeyboard() {
        binding.message.postDelayed({
            binding.message.showKeyboard()
        }, 200)
    }

    override fun requestCamera() {
        cameraDestination = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                .let { timestamp -> ContentValues().apply { put(MediaStore.Images.Media.TITLE, timestamp) } }
                .let { cv -> contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, cameraDestination)
        startActivityForResult(Intent.createChooser(intent, null), TakePhotoRequestCode)
    }

    override fun requestGallery() {
        val intent = Intent(Intent.ACTION_PICK)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, false)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setType("image/*")
        startActivityForResult(Intent.createChooser(intent, null), AttachPhotoRequestCode)
    }

    override fun setDraft(draft: String) = binding.message.setText(draft)

    override fun scrollToMessage(id: Long) {
        messageAdapter.data?.second
                ?.indexOfLast { message -> message.id == id }
                ?.takeIf { position -> position != -1 }
                ?.let(binding.messageList::scrollToPosition)
    }

    override fun showQksmsPlusSnackbar(message: Int) {
        Snackbar.make(binding.contentView, message, Snackbar.LENGTH_LONG).run {
            setAction(R.string.button_more) { viewQksmsPlusIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun getColoredMenuItems(): List<Int> {
        return super.getColoredMenuItems() + R.id.call
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == SelectContactRequestCode -> {
                chipsSelectedIntent.onNext(data?.getSerializableExtra(ContactsActivity.ChipsKey)
                        ?.let { serializable -> serializable as? HashMap<String, String?> }
                        ?: hashMapOf())
            }
            requestCode == TakePhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                cameraDestination?.let(attachmentSelectedIntent::onNext)
            }
            requestCode == AttachPhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.clipData?.itemCount
                        ?.let { count -> 0 until count }
                        ?.mapNotNull { i -> data.clipData?.getItemAt(i)?.uri }
                        ?.forEach(attachmentSelectedIntent::onNext)
                        ?: data?.data?.let(attachmentSelectedIntent::onNext)
            }
            requestCode == AttachContactRequestCode && resultCode == Activity.RESULT_OK -> {
                val v = layoutInflater.inflate(R.layout.compose_contact_type_chooser_dialog, null)
                val dialog = AlertDialog.Builder(this)
                        .setTitle(R.string.main_menu_add_contact)
                        .setView(v)
                        .setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                            return@setNegativeButton
                        }
                        .create()
                (v.findViewById(R.id.text) as Button).setOnClickListener {
                    insertMessage(getNameNumber(Uri.parse(data?.data.toString())))
                    dialog.dismiss()
                }
                (v.findViewById(R.id.vcard) as Button).setOnClickListener {
                    data?.data?.let(contactSelectedIntent::onNext)
                    dialog.dismiss()
                }
                Utils.adjustAlertDialog(dialog, resources.getDrawable(R.drawable.chip_background))
                dialog.show()
            }
            requestCode == AttachAudioRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.data?.let(audioContentIntent::onNext)
            }
            requestCode == AttachQuickMessageRequestCode && resultCode == Activity.RESULT_OK -> {
                val qm = data?.getStringExtra("data")
                if(qm != null) {
                    insertMessage(qm)
                }
            }
            requestCode == CalendarEventRequestCode && resultCode == Activity.RESULT_OK -> {
                val calendarEventDetails = data?.getSerializableExtra("data") as ArrayList<*>
                val tailTemp = calendarEventDetails.toString()
                val tail = tailTemp.substring(1, tailTemp.length - 1)
                insertMessage(tail)
            }
            requestCode == CheckSecurityRequestCode -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    finish()
                } else {
                    checked = true
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun insertMessage(messagePart: String) {
        val originText = binding.message.text.toString()
        val additionalText = if (originText.isNotEmpty()) {
            "\n"
        } else {
            ""
        }
        binding.message.setText(originText + additionalText + messagePart)
    }

    private fun getNameNumber(uri: Uri): String {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER)
        val names = contentResolver.query(uri, projection, null, null, null)
        val indexName = names!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val indexNumber = names.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        names.moveToFirst()
        val name = names.getString(indexName)
        val number = names.getString(indexNumber)

        return "Name:$name\nNumber:$number"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CameraDestinationKey, cameraDestination)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        cameraDestination = savedInstanceState?.getParcelable(CameraDestinationKey)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() = backPressedIntent.onNext(Unit)

    override fun setPhotoFromCamera(uri: Uri) {
        uri.let(attachmentSelectedIntent::onNext)
    }

    override fun setRecord(uri: Uri) {
        uri.let(audioContentIntent::onNext)
    }

    override fun onResume() {
        super.onResume()
        if (intent.extras?.getBoolean("locked")!! && !checked) {
            checkSecurity()
        }
    }

    fun otherClick(position: Int) {
        binding.attach.performClick()
        when(position) {
            0 -> {
                requestContact()
            }
            1 -> {
                val intent = Intent(this, QuickMessageActivity::class.java)
                startActivityForResult(intent, AttachQuickMessageRequestCode)
            }
            2 -> {
                requestDatePicker()
            }
            3 -> {
                val intent: Intent
                val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                chooseFile.type = ContentType.AUDIO_UNSPECIFIED
                intent = Intent.createChooser(chooseFile, "Choose a file")
                startActivityForResult(intent, AttachAudioRequestCode)
            }
            4 -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, CalendarEventActivity::class.java)
                    startActivityForResult(intent, CalendarEventRequestCode)
                } else {
                    requestCalendarPermission()
                }
            }
        }
    }

    private fun checkSecurity() {
        val keyguardManager: KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardSecure) {
            val authIntent: Intent = keyguardManager.createConfirmDeviceCredentialIntent("Enter password", "Enter password to see this conversation")
            startActivityForResult(authIntent, CheckSecurityRequestCode)
        }
    }

    class PagerAdapter(fm: androidx.fragment.app.FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(i: Int): Fragment {
            val fragment: Fragment = CameraFragment()
            val args = Bundle()
            fragment.arguments = args
            when(i) {
                0 -> return CameraFragment()
                1 -> return GalleryFragment()
                2 -> return RecordFragment()
                3 -> return OtherFragment()
            }
            return fragment
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence {
            return ""
        }
    }
}
