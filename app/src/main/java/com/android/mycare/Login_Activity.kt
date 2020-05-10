package com.android.mycare

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registration.*

class Login_Activity : AppCompatActivity() {
    private var mDatabase: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        login_button.setOnClickListener {
            login_progressBar.visibility = View.VISIBLE
            hideKeyboard(it)
            performLoginAuth()
        }
        textView_dont_have_account.setOnClickListener {
            val intent = Intent(this,RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
    private fun performLoginAuth(){
        val email = editText_email_login.text.toString()
        val password = editText_password_login.text.toString()
        if(email.isEmpty()||password.isEmpty()){
            login_progressBar.visibility = View.GONE
            Toast.makeText(this,"Please enter valid data in the fields!!",Toast.LENGTH_SHORT).show()
            return
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        val query = mDatabase!!.orderByChild("email").equalTo(email)
        val loginAuth = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var user_status: String = ""
                    var site_status: String = ""
                    var role_id: Long = 0;
                    for(dataSnapshot1 in dataSnapshot.children) {
                        user_status = dataSnapshot1.child("user_status").getValue().toString()
                        site_status = dataSnapshot1.child("site_status").getValue().toString()
                        role_id = dataSnapshot1.child("role").getValue() as Long
                        if(user_status.trim().equals("active")&&site_status.trim().equals("active")){
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).
                                addOnCompleteListener {
                                    if (it.isSuccessful){
                                        val intent = Intent(this@Login_Activity,HomeActivity::class.java)
                                        intent.putExtra("role",role_id)
                                        startActivity(intent)
                                    }else{
                                        return@addOnCompleteListener
                                    }
                                }.
                                addOnFailureListener {
                                    login_progressBar.visibility = View.GONE
                                    Log.d("LoginActivity:::","Failed to login:${it.message}")
                                    Toast.makeText(this@Login_Activity,"Failed to login:${it.message}",Toast.LENGTH_SHORT).show()
                                }
                        }else{
                            login_progressBar.visibility = View.GONE
                            val builder = AlertDialog.Builder(this@Login_Activity)
                            //set title for alert dialog
                            builder.setTitle("")
                            //set message for alert dialog
                            builder.setMessage("Your account is in inactive state!!")
                            builder.setIcon(android.R.drawable.ic_dialog_alert)
                            //performing positive action
                            builder.setPositiveButton("Ok"){dialogInterface, which ->
                                return@setPositiveButton
                            }
                            // Create the AlertDialog
                            val alertDialog: AlertDialog = builder.create()
                            // Set other dialog properties
                            alertDialog.setCancelable(false)
                            alertDialog.show()
                            return
                        }
                    }
                }else{
                    login_progressBar.visibility = View.GONE
                    val builder = AlertDialog.Builder(this@Login_Activity)
                    //set title for alert dialog
                    builder.setTitle("")
                    //set message for alert dialog
                    builder.setMessage("User not exists!!")
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                    //performing positive action
                    builder.setPositiveButton("Ok"){dialogInterface, which ->
                        return@setPositiveButton
                    }
                    // Create the AlertDialog
                    val alertDialog: AlertDialog = builder.create()
                    // Set other dialog properties
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                    return
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        query.addValueEventListener(loginAuth)
        mDatabase = null
    }
    private fun hideKeyboard(view: View) {
        view?.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
