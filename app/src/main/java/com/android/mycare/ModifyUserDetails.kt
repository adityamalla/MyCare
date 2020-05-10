package com.android.mycare

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.content_flats.*
import kotlinx.android.synthetic.main.content_modify_user.*


class ModifyUserDetails : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    var listItems: ArrayList<String>? = null
    var profilePicURI: String? = ""
    var username: String? = ""
    var role:Long = 0
    lateinit var imageProfileView: ImageView
    lateinit var welcomeText: TextView
    private val PERMISSION_REQUEST_CODE = 200
    private var mDatabase: DatabaseReference? = null
    // adapter for auto-complete
    var myAdapter: ArrayAdapter<MyObject>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modify_user)
        if (checkPermission()) {
            requestPermissionAndContinue()
        }
        toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout2)
        navView = findViewById(R.id.nav_view2)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        val intent = intent
        role = intent.getLongExtra("role",0)
        val headerView = navView.getHeaderView(0) as View
        imageProfileView= headerView.findViewById<ImageView>(R.id.imageProfileView)
        welcomeText= headerView.findViewById<TextView>(R.id.welcomeText)
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        val userListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val message = dataSnapshot.getValue(UserModel::class.java)
                    profilePicURI = message?.profileimageuri.toString()
                    username = message?.username.toString()
                    setProfileData(profilePicURI!!,username!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        ref!!.addValueEventListener(userListener)
        getSiteSuggestions("Ganesh")
       /* autoCompleteTextView_Sites.addTextChangedListener( CustomAutoCompleteTextChangedListener(this@ModifyUserDetails))
            // ObjectItemData has no value at first
        val ObjectItemData: Array<MyObject>? = null
            // set the custom ArrayAdapter
        myAdapter = AutocompleteCustomArrayAdapter(this@ModifyUserDetails, R.layout.autocomplete_row, ObjectItemData)
        autoCompleteTextView_Sites.setAdapter(myAdapter)*/
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.modify_user -> {
                val intent = Intent(this, ModifyUserDetails::class.java)
                intent.putExtra("role",role)
                startActivity(intent)
            }
            R.id.nav_site -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("role",role)
                startActivity(intent)
            }
            R.id.nav_flats -> {
                val intent = Intent(this, FlatsActivity::class.java)
                intent.putExtra("role",role)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val intent = Intent(this, Login_Activity::class.java)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    fun setProfileData(profilePicUri: String,username: String){
        Log.d("FlatsActivity:::",profilePicUri+"***")
        Glide.with(this).
            load(profilePicUri).
            override(500,200)
            .into(imageProfileView)
        welcomeText.setText("Welcome "+username)
    }
    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setCancelable(true)
                alertBuilder.setTitle("permission_necessary")
                alertBuilder.setMessage("storage_permission_is_necessary_to_wrote_event")
                alertBuilder.setPositiveButton(
                    android.R.string.yes
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                            , Manifest.permission.READ_EXTERNAL_STORAGE
                        ), PERMISSION_REQUEST_CODE
                    )
                }
                val alert = alertBuilder.create()
                alert.show()
                //////log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    private fun getSiteSuggestions(searchTerm:String){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("sites");
        val query = mDatabase!!.orderByChild("site_name").startAt("Ganes")
            .endAt("\uf8ff").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        val children = p0!!.children
                        // This returns the correct child count...
                        Log.d("ModifyUserDetails: ",p0.children.count().toString())
                        children.forEach {
                            Log.d("ModifyUserDetails*****",it.toString())
                        }
                    }
                })
    }
}