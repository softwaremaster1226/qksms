package com.android.QKSMS.feature.quick

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.R
import com.android.QKSMS.common.base.QkThemedActivity
import com.android.QKSMS.common.util.Utils
import com.android.QKSMS.common.util.extensions.viewBinding
import com.android.QKSMS.databinding.QuickActivityBinding
import com.android.QKSMS.model.QuickMessage
import com.jakewharton.rxbinding2.view.clicks
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.realm.Realm
import javax.inject.Inject

class QuickMessageActivity : QkThemedActivity(), QuickMessageView, QuickMessageClick {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val QUICK_PREFS = "QUICK_PREFS"

    override val addIntent: Observable<*> by lazy { binding.add.clicks() }
    override lateinit var adapter: RecyclerView.Adapter<*>

    private val binding by viewBinding(QuickActivityBinding::inflate)
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory)[QuickMessageViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewModel.bindView(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

//        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        prefs.getString(QUICK_PREFS, "")
        val data = ArrayList<String>()
        Realm.getDefaultInstance().use{
            for(item in it.where(QuickMessage::class.java).findAll()) {
                data.add(item.messageQ)
            }
        }
        adapter = QuickMessageAdapter(data)
        binding.recyclerView.adapter = adapter
    }

    override fun addQM() {
        val v = layoutInflater.inflate(R.layout.quick_message_add_dialog, null)
        val editDialog = AlertDialog.Builder(this)
                .setTitle("Add Quick Message.")
                .setView(v)
                .setPositiveButton(R.string.button_save) { dialogInterface: DialogInterface, i: Int ->
                    val et = v.findViewById(R.id.message) as EditText
                    et.requestFocus()
                    val qmForInsert = et.text.toString()
                    if (qmForInsert.isNotEmpty()) {
                        Realm.getDefaultInstance().use {
                            val maxId = it.where(QuickMessage::class.java)
                                    .max("id")?.toLong() ?: -1
                            it.beginTransaction()
                            it.insert(QuickMessage(maxId + 1, qmForInsert))
                            it.commitTransaction()
                            (adapter as QuickMessageAdapter).getData().add(qmForInsert)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                .setNegativeButton(R.string.button_cancel, DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                    return@OnClickListener
                })
                .create()
        Utils.adjustAlertDialog(editDialog, this.resources.getDrawable(R.drawable.chip_background))
        editDialog.show()
    }

    override fun render(state: QuickMessageState) {

    }

    override fun onClick(data: String) {
        val intent = Intent()
        intent.putExtra("data", data)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}