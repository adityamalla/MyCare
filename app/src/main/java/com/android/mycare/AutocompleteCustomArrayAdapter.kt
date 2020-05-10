package com.android.mycare

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class AutocompleteCustomArrayAdapter(
    var mContext: Context,
    var layoutResourceId: Int,
    data: Array<MyObject>?
) :
    ArrayAdapter<MyObject>(mContext, layoutResourceId, data!!) {
    val TAG = "AutocompleteCustomArrayAdapter.java"
    var data: Array<MyObject>? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        try { /*
             * The convertView argument is essentially a "ScrapView" as described is Lucas post
             * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
             * It will have a non-null value when ListView is asking you recycle the row layout.
             * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
             */
            if (convertView == null) { // inflate the layout
                val inflater: LayoutInflater = (mContext as ModifyUserDetails).getLayoutInflater()
                convertView = inflater.inflate(R.layout.autocomplete_row, parent, false)
            }
            // object item based on the position
            val objectItem: MyObject = data!![position]
            // get the TextView and then set the text (item name) and tag (item ID) values
            val textViewItem = convertView!!.findViewById<View>(R.id.text_view_name) as TextView
            textViewItem.setText(objectItem.objectName.toString())
            // in case you want to add some style, you can do something like:
            textViewItem.setBackgroundColor(Color.CYAN)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }

    init {
        this.data = data
    }
}