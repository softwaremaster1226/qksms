package com.android.QKSMS.feature.settings.ringtone

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.SongsModel
import com.android.QKSMS.common.util.extensions.viewBinding
import com.android.QKSMS.databinding.RingtoneActivityBinding
import com.android.QKSMS.feature.settings.ringtone.crop.RingdroidEditActivity
import com.android.QKSMS.feature.settings.ringtone.util.RingtoneAdapter
import com.android.QKSMS.feature.settings.ringtone.util.Ringtones
import dagger.android.AndroidInjection
import javax.inject.Inject


class RingtoneActivity : QkThemedActivity(), RingtoneView {

    @Inject lateinit var context: Context
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val binding by viewBinding(RingtoneActivityBinding::inflate)
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[RingtoneViewModel::class.java] }
    private var mediaPlayer = MediaPlayer()
    private val updateSongTime: Runnable = Runnable {
        mediaPlayer.currentPosition
    }
    private lateinit var adapter: RingtoneAdapter
    private var path: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.bindView(this)

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
//        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
//        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, null as Uri?)
//        this.startActivityForResult(intent, 5)

        binding.done.setOnClickListener {

        }
//        adapter = RingtoneAdapter(listOf(Ringtones("Songs", listOf(SongsModel("1", "name", "artist", "duration", "album", "path", "albumid", "type"), SongsModel("1", "name", "artist", "duration", "album", "path", "albumid", "type"), SongsModel("1", "name", "artist", "duration", "album", "path", "albumid", "type"))), Ringtones("Songs", listOf(SongsModel("1", "name", "artist", "duration", "album", "path", "albumid", "type"), SongsModel("1", "name", "artist", "duration", "album", "path", "albumid", "type"), SongsModel("1", "name", "artist", "duration", "album", "path", "albumid", "type")))))
        val layoutManager = LinearLayoutManager(this)

        val list = ArrayList<SongsModel>()
        val manager = RingtoneManager(this)
        manager.setType(RingtoneManager.TYPE_RINGTONE)
        val cursor: Cursor = manager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val ringtoneURI: Uri = manager.getRingtoneUri(cursor.position)
            list.add(SongsModel("id", title, "name", "artist", "duration", "album", "path" ,"albumid"))
        }

        adapter = RingtoneAdapter(listOf(Ringtones("Music", list)))
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        binding.cutter.setOnClickListener {
            val intent = Intent(this, RingdroidEditActivity::class.java).putExtra("FILE_PATH", "file:///storage/emulated/0/Music/abc.mp3")
            startActivityForResult(intent, 1)
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx: Int = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        adapter.onRestoreInstanceState(savedInstanceState)
    }

    override fun render(state: RingtoneState) {

    }
}