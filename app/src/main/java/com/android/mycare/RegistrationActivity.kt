package com.android.mycare

import android.app.Activity
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.content_flats.*
import java.net.URI
import java.util.*

class RegistrationActivity : AppCompatActivity() {
    private var mDatabase: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        supportActionBar?.hide()
        progressBar.visibility = View.GONE
        registration_success_textview.visibility = View.GONE
        mDatabase = FirebaseDatabase.getInstance().getReference().child("sites")
        val query = mDatabase!!.orderByChild("site_name")
        val siteSpinnerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val sitetitleList =  ArrayList<Spinner_Model>()
                    sitetitleList.add(Spinner_Model("please select a site","-1"))
                    for(dataSnapshot1 in dataSnapshot.children){
                        val site_name = dataSnapshot1.child("site_name").getValue()
                        val uid = dataSnapshot1.child("uid").getValue()
                        sitetitleList.add(Spinner_Model(site_name as String,uid as String))
                    }
                    val arrayAdapter = MyCustomSpinnerAdapter(this@RegistrationActivity,sitetitleList)
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_register_site.setAdapter(arrayAdapter)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        query.addValueEventListener(siteSpinnerListener)
        select_photo_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
        button_register.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            hideKeyboard(it)
            performRegister()
        }
        textView_account_exits.setOnClickListener {
            val intent = Intent(this,Login_Activity::class.java)
            startActivity(intent)
        }
        registration_success_textview.setOnClickListener {
         if(registration_success_textview.visibility == View.VISIBLE) {
             val intent = Intent(this, Login_Activity::class.java)
             startActivity(intent)
         }
        }
    }
    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectimageview_register.setImageBitmap(bitmap)
            select_photo_button_register.alpha = 0f
        }
    }
    private fun performRegister(){
        val email = email_edittext_reg.text.toString()
        val password = password_edittext_reg.text.toString()
        val firstname = firstname_edittext_reg.text.toString()
        val lastname = lastname_edittext_reg.text.toString()
        val referral_code = enter_referral_code.text.toString()
        val site_selected = spinner_register_site.selectedItem as Spinner_Model
        if(email.isEmpty() || password.isEmpty()||firstname.isEmpty()||lastname.isEmpty()){
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.GONE
            Toast.makeText(this,"Please enter valid data in the fields!!",Toast.LENGTH_SHORT).show()
            return
        }else if(site_selected.id.equals("-1")){
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.GONE
            Toast.makeText(this,"Please select a site",Toast.LENGTH_SHORT).show()
            return
        }else if(referral_code.isEmpty()) {
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.GONE
            Toast.makeText(this,"Please enter referral code given for the selected site",Toast.LENGTH_SHORT).show()
            return
        }
        else if(selectedPhotoUri == null) {
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.GONE
            Toast.makeText(this,"Please select your profile pic!!",Toast.LENGTH_SHORT).show()
            return
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("sites")
        val query = mDatabase!!.orderByChild("uid").equalTo(site_selected.id.toString().trim())
        val specificsiteSelected = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var code: String = ""
                    for(dataSnapshot1 in dataSnapshot.children) {
                        code = dataSnapshot1.child("referral_code").getValue().toString()
                        if(code.trim().equals(enter_referral_code.text.toString().trim())){
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                                .addOnCompleteListener{
                                    if(!it.isSuccessful)
                                    {
                                        return@addOnCompleteListener
                                    }
                                    else
                                    {
                                        Log.d("RegisterActivity:::","succesfully created user")
                                        uploadImageToFireBaseStorage(site_selected.id.toString().trim())
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d("RegisterActivity:::","Failed to create user:${it.message}")
                                    progressBar.visibility = View.GONE
                                    registration_success_textview.visibility = View.GONE
                                    Toast.makeText(this@RegistrationActivity,"Failed to create user:${it.message}",Toast.LENGTH_SHORT).show()
                                }
                        }else{
                            progressBar.visibility = View.GONE
                            registration_success_textview.visibility = View.GONE
                            val builder = AlertDialog.Builder(this@RegistrationActivity)
                            //set title for alert dialog
                            builder.setTitle("")
                            //set message for alert dialog
                            builder.setMessage("Referral code entered is not valid!!")
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
                        progressBar.visibility = View.GONE
                        registration_success_textview.visibility = View.GONE
                        val builder = AlertDialog.Builder(this@RegistrationActivity)
                        //set title for alert dialog
                        builder.setTitle("")
                        //set message for alert dialog
                        builder.setMessage("Referral code don't exists for this site!! Please contact administrator")
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
        query.addValueEventListener(specificsiteSelected)
    }
    private fun uploadImageToFireBaseStorage(site_id:String){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref  = FirebaseStorage.getInstance().getReference("/images/${filename}")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("RegisterActivity:::","Successfully uploaded image: ${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {
                saveUserToFireBaseDatabase(it.toString(),site_id)
            }
        }.addOnFailureListener {
            Log.d("RegisterActivity:::","Failed to upload image:${it.message}")
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.GONE
            Toast.makeText(this,"Failed to  upload image:${it.message}",Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveUserToFireBaseDatabase(profileimageuri: String,site_id:String){
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        val user = User(uid,username_edittext_reg.text.toString().trim(),email_edittext_reg.text.toString().trim(),
            profileimageuri,"active","active",4,site_id,firstname_edittext_reg.text.toString(),lastname_edittext_reg.text.toString())
        ref.setValue(user).addOnSuccessListener {
            Log.d("RegisterActivity:::","Succesfully saved user firebase database")
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.VISIBLE
            username_edittext_reg.setText("")
            email_edittext_reg.setText("")
            password_edittext_reg.setText("")
            spinner_register_site.setSelection(0)
            enter_referral_code.setText("")
            firstname_edittext_reg.setText("")
            lastname_edittext_reg.setText("")
        }.addOnFailureListener {
            Log.d("RegisterActivity:::","Failed to save user:${it.message}")
            progressBar.visibility = View.GONE
            registration_success_textview.visibility = View.GONE
            Toast.makeText(this,"Failed to save user:${it.message}",Toast.LENGTH_SHORT).show()
        }
    }
    private fun hideKeyboard(view: View) {
        view?.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
class User(val uid: String,val username: String,val email: String, val profileimageuri: String,val user_status:String,val site_status: String,val role:Int,val site_id:String
           ,val firstname:String,val lastname:String)
