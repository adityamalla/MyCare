package com.android.mycare

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class MyListAdapter(private val context: Activity, private val block: ArrayList<String>)
    : ArrayAdapter<String>(context, R.layout.list_item, block) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_item, null, true)
        val blockText = rowView.findViewById(R.id.blocks_or_flats_list_items) as TextView
        blockText.text = block[position]
        val buttonClick = rowView.findViewById(R.id.listView_clear) as ImageView
        buttonClick.setOnClickListener {
            block.removeAt(position)
            notifyDataSetChanged()
        }
        return rowView
    }

}