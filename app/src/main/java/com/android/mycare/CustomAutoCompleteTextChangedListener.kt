package com.android.mycare

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.content_modify_user.*
import kotlinx.android.synthetic.main.test.*


class CustomAutoCompleteTextChangedListener(var context: Context) : TextWatcher {
    override fun afterTextChanged(s: Editable) { // TODO Auto-generated method stub
    }

    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int,
        after: Int
    ) { // TODO Auto-generated method stub
    }

    @SuppressLint("LongLogTag")
    override fun onTextChanged(
        userInput: CharSequence,
        start: Int,
        before: Int,
        count: Int
    ) {
        try { // if you want to see in the logcat what the user types
            Log.d(TAG, "MU:::User input: $userInput")
            val acticity: ModifyUserDetails = context as ModifyUserDetails
            // update the adapater
            acticity.myAdapter!!.notifyDataSetChanged()
            // get suggestions from the database
            val myObjs: Array<MyObject>? = null
            Log.d(TAG, "MU:::myObjs size: ${myObjs?.size}")
            // update the adapter
            acticity.myAdapter = AutocompleteCustomArrayAdapter(acticity,  myObjs)
            acticity.autoCompleteTextView_Sites.setAdapter(acticity.myAdapter)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val TAG = "CustomAutoCompleteTextChangedListener.java"
    }

}