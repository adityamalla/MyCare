package com.android.mycare

import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView


class CustomAutoCompleteView : AutoCompleteTextView {
    constructor(context: Context?) : super(context) { // TODO Auto-generated constructor stub
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) { // TODO Auto-generated constructor stub
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) { // TODO Auto-generated constructor stub
    }

    // this is how to disable AutoCompleteTextView filter
    override fun performFiltering(text: CharSequence, keyCode: Int) {
        val filterText = ""
        super.performFiltering(filterText, keyCode)
    }
}