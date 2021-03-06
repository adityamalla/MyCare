package com.android.mycare

import android.Manifest.permission
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_main_super_admin.*
import kotlinx.android.synthetic.main.tiles_content.*

class HomeActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {
    var adapter: ArrayAdapter<String>? = null
    var listItems: ArrayList<String>? = null
    var profilePicURI: String? = ""
    var role:Long = 0
    var username: String? = ""
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var imageProfileView: ImageView
    lateinit var welcomeText: TextView
    lateinit var nav_Menu: Menu
    private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tiles)
        if (checkPermission()) {
            requestPermissionAndContinue()
        }
        toolbar = findViewById(R.id.toolbar_tiles)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout_tiles)
        navView = findViewById(R.id.nav_view_tiles)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
        val intent = intent
        role = intent.getLongExtra("role",0)
        if(role == 1L){
            home_page_text.setText("Home Page(Super Admin)")
            add_appartment_tile.visibility = View.VISIBLE
            textView_sites_label.visibility = View.VISIBLE
            add_flats_tile.visibility = View.VISIBLE
            textView_flats_label.visibility = View.VISIBLE
            edit_user_role_tile.visibility = View.VISIBLE
            textView_modify_user_role.visibility = View.VISIBLE
        }else if(role == 2L){
            home_page_text.setText("Home Page(Admin)")
            add_appartment_tile.visibility = View.GONE
            textView_sites_label.visibility = View.GONE
            add_flats_tile.visibility = View.GONE
            textView_flats_label.visibility = View.GONE
            edit_user_role_tile.visibility = View.GONE
            textView_modify_user_role.visibility = View.GONE
        }else if(role == 3L){
            home_page_text.setText("Home Page(Security)")
            add_appartment_tile.visibility = View.GONE
            textView_sites_label.visibility = View.GONE
            add_flats_tile.visibility = View.GONE
            textView_flats_label.visibility = View.GONE
            edit_user_role_tile.visibility = View.GONE
            textView_modify_user_role.visibility = View.GONE
        }else if(role == 4L){
            home_page_text.setText("Home Page(General)")
            add_appartment_tile.visibility = View.GONE
            textView_sites_label.visibility = View.GONE
            add_flats_tile.visibility = View.GONE
            textView_flats_label.visibility = View.GONE
            edit_user_role_tile.visibility = View.GONE
            textView_modify_user_role.visibility = View.GONE
        }
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
        add_appartment_tile.setOnClickListener {
            val intent = Intent(this, SitesActivity::class.java)
            intent.putExtra("role",role)
            startActivity(intent)
        }
        add_flats_tile.setOnClickListener {
            val intent = Intent(this, FlatsActivity::class.java)
            intent.putExtra("role",role)
            startActivity(intent)
        }
        edit_user_role_tile.setOnClickListener {
            val intent = Intent(this, ModifyUserDetails::class.java)
            intent.putExtra("role",role)
            startActivity(intent)
        }
    }
    fun setProfileData(profilePicUri: String,username: String){
       Log.d("HomeActivity:::",profilePicUri+"***")
       Glide.with(this).
           load(profilePicUri).
            override(500,200)
           .into(imageProfileView)
        welcomeText.setText("Welcome "+username)
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_home -> {
                val intent = Intent(this, HomeActivity::class.java)
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
    private val PERMISSION_REQUEST_CODE = 200

    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(
                this,
                permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission.WRITE_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(this, permission.READ_EXTERNAL_STORAGE)
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
                            permission.WRITE_EXTERNAL_STORAGE
                            , permission.READ_EXTERNAL_STORAGE
                        ), PERMISSION_REQUEST_CODE
                    )
                }
                val alert = alertBuilder.create()
                alert.show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        permission.WRITE_EXTERNAL_STORAGE,
                        permission.READ_EXTERNAL_STORAGE
                    ), PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}

