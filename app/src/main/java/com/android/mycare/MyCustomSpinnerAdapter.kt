package com.android.mycare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.spinner_items.view.*

class MyCustomSpinnerAdapter(ctx: Context,
                             moods: List<Spinner_Model>) :
    ArrayAdapter<Spinner_Model>(ctx, 0, moods) {
    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val obj = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.spinner_items,
            parent,
            false
        )
        if (obj != null) {
            view.spinner_value.text = obj.name.toString()
        }
        return view
    }
}