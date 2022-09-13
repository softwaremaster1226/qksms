package com.android.QKSMS.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.QKSMS.feature.compose.editing.ComposeItemPlusAdapter
import java.util.*

class SideBarView(context: Context?, attrs: AttributeSet?) : View(context, attrs), OnLetterClickedListener {
    var arrLetters = arrayOf("#", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z")
    private var listener: OnLetterClickedListener? = this
    private var textView_dialog: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private var isChoosedPosition = -1
    private val alphabetic = ArrayList<Char>()
    fun setTextView(textView: TextView?) {
        textView_dialog = textView
    }

    fun setRecyclerView(mlv: RecyclerView?) {
        mRecyclerView = mlv
        val count = mRecyclerView!!.adapter!!.itemCount
        val datas = (mRecyclerView!!.adapter as ComposeItemPlusAdapter?)!!.data
        for (i in 0 until count - 1) {
            datas[i].getContacts().associate {contact ->
                val address = contact.getDefaultNumber()?.address ?: contact.numbers[0]!!.address
                alphabetic.add(address[0])
                address to contact.lookupKey
            }
            alphabetic.add(datas[i].getContacts()[0].name[0])
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 当前view的宽度
        val width = width
        // 当前view的高度
        val height = height
        // 当前view中每个字母所占的高度
        val singleTextHeight = (height - 344) / arrLetters.size
        val paint = Paint()
        paint.isAntiAlias = true
        paint.typeface = Typeface.DEFAULT
        for (i in arrLetters.indices) {
            paint.color = Color.GRAY
            paint.textSize = 40f
            if (i == isChoosedPosition) {
                paint.color = resources.getColor(android.R.color.holo_blue_light)
                paint.isFakeBoldText = true
            }
            val x = (width - paint.measureText(arrLetters[i])) / 2
            val y = singleTextHeight * (i + 1).toFloat()
            canvas.drawText(arrLetters[i], x, y, paint)
            paint.reset()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y
        val position = (y / (height - 400) * arrLetters.size).toInt()
        val lastChoosedPosition = isChoosedPosition
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (textView_dialog != null) {
                    textView_dialog!!.visibility = GONE
                }
                invalidate()
            }
            else ->
                if (lastChoosedPosition != position) {
                    if (position >= 0 && position < arrLetters.size) {
                        if (listener != null) {
                            listener!!.onLetterClicked(arrLetters[position])
                        }
                        if (textView_dialog != null) {
                            textView_dialog!!.visibility = VISIBLE
                            textView_dialog!!.text = arrLetters[position]
                        }
                        if (isChoosedPosition == -3) isChoosedPosition = position
                        invalidate()
                    }
                }
        }
        return true
    }

    override fun onLetterClicked(str: String?) {
        if (mRecyclerView != null) {
            val count = mRecyclerView!!.adapter!!.itemCount
            val datas = (mRecyclerView!!.adapter as ComposeItemPlusAdapter?)!!.data
            for (i in 0 until count - 1) {
                var firstChar = 'A'
                datas[i].getContacts().associate { contact ->
                    val address = contact.getDefaultNumber()?.address ?: contact.name
                    firstChar = address[0]
                    address to contact.lookupKey
                }
                if (Character.isDigit(firstChar) && str == "#") {
                    mRecyclerView!!.layoutManager!!.scrollToPosition(0)
                    isChoosedPosition = -3
                    return
                }
                if (str != null) {
                    if (firstChar.toString().toLowerCase() == str.toLowerCase()) {
                        mRecyclerView!!.layoutManager!!.scrollToPosition(i)
                        isChoosedPosition = -3
                        return
                    }
                }
            }
        }
    }

    fun setHighlightLetter(str: String) {
        findFirstLetter(str)
        invalidate()
    }

    private fun findFirstLetter(str: String) {
//        onLetterClicked(str);
        var pos = 0
        while (pos < arrLetters.size) {
            if (Character.isDigit(str[0])) {
                pos = 0
                break
            }
            if (arrLetters[pos].toLowerCase() == str) {
                break
            }
            pos++
        }
        //        if(isChoosedPosition == -3)
        isChoosedPosition = pos
    }

    fun setOnLetterClickedListener(listener: OnLetterClickedListener?) {
        this.listener = listener
    }
}

interface OnLetterClickedListener {
    fun onLetterClicked(str: String?)
}