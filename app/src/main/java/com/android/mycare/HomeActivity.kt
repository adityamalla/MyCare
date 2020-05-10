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
import kotlinx.android.synthetic.main.content_main_admin.*
import kotlinx.android.synthetic.main.content_main_general_user.*
import kotlinx.android.synthetic.main.content_main_super_admin.*

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
        setContentView(R.layout.activity_home)
        if (checkPermission()) {
            requestPermissionAndContinue()
        }
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
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
        val intent = intent
        role = intent.getLongExtra("role",0)
        if(role == 4L){
            layout_stub.setLayoutResource(R.layout.content_main_general_user)
            layout_stub.inflate()
            toolbar = findViewById(R.id.toolbar_general_user)
            setSupportActionBar(toolbar)
            val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            nav_Menu = navView.menu
            nav_Menu.findItem(R.id.nav_site).setVisible(false)
            nav_Menu.findItem(R.id.nav_flats).setVisible(false)
            nav_Menu.findItem(R.id.modify_user).setVisible(false)
        }else if(role == 2L){
            layout_stub.setLayoutResource(R.layout.content_main_admin)
            layout_stub.inflate()
            toolbar = findViewById(R.id.toolbar_admin)
            setSupportActionBar(toolbar)
            val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            nav_Menu = navView.menu
            nav_Menu.findItem(R.id.nav_site).setVisible(false)
            nav_Menu.findItem(R.id.nav_flats).setVisible(false)
            nav_Menu.findItem(R.id.modify_user).setVisible(false)
        }else if(role == 1L){
            layout_stub.setLayoutResource(R.layout.content_main_super_admin)
            layout_stub.inflate()
            toolbar = findViewById(R.id.toolbar_super_admin)
            setSupportActionBar(toolbar)
            val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, 0, 0
            )
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
            sites_progressBar.visibility = View.GONE
            nav_Menu = navView.menu
            nav_Menu.findItem(R.id.nav_site).setVisible(true)
            nav_Menu.findItem(R.id.nav_flats).setVisible(true)
            nav_Menu.findItem(R.id.modify_user).setVisible(true)
            listItems = ArrayList()
            val myListAdapter = MyListAdapter(this, listItems!!)
            add_blocks_button.setOnClickListener {
                listItems!!.add(editText_block_names.text.toString())
                blocks_list.adapter = myListAdapter
                setListViewHeightBasedOnChildren(blocks_list)
                editText_block_names.setText("")
            }
            add_sites_button.setOnClickListener {
                hideKeyboard(it)
                val site_name = editText_site_name.text.toString()
                val location = editText_location.text.toString()
                val referral_code = editText_generate_referal_code.text.toString()
                if(site_name.isEmpty() || location.isEmpty() || referral_code.isEmpty()){
                    sites_progressBar.visibility = View.GONE
                    Toast.makeText(this,"Please enter valid data in the fields!!",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                sites_progressBar.visibility = View.VISIBLE
                saveSitesDataToFireBaseDatabase(site_name,location,referral_code)
            }
            scrollView_home.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                if (i2 > 600) {
                    scrollTop.setVisibility(View.VISIBLE);
                }else {
                    scrollTop.setVisibility(View.INVISIBLE);
                }
            }
            scrollTop.setOnClickListener {
                scrollView_home.scrollTo(0,0)
            }
            generate_code.setOnClickListener {
                val randomString = (1..10)
                    .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")
                editText_generate_referal_code.setText(randomString)
            }
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
    private fun saveSitesDataToFireBaseDatabase(site_name: String,location: String,referral_code: String){
        val itemsCount = blocks_list.childCount
        Log.d("HomeActivity:::","itemsCount---"+itemsCount)
        var blocksData: String = ""
        if(itemsCount > 1) {
            for (i in 0..itemsCount - 1) {
                val view = blocks_list.getChildAt(i)
                blocksData =
                    blocksData + (view.findViewById<TextView>(R.id.blocks_or_flats_list_items)).getText().toString() + ","
            }
            blocksData = blocksData.substring(0, blocksData.length - 1)
        }else if(itemsCount == 1) {
            val view = blocks_list.getChildAt(0)
            blocksData = (view.findViewById<TextView>(R.id.blocks_or_flats_list_items)).getText().toString()
        }
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("sites")
        val key = ref.push().key
        Log.d("HomeActivity:::","key--"+key)
        val ref1 = FirebaseDatabase.getInstance().getReference("/sites/${key}")
        val sites = key?.let { Sites(it,site_name,blocksData,location
            ,editText_City.text.toString(),editText_State.text.toString(),editText_Pin.text.toString(),referral_code) }
        ref1.setValue(sites).addOnSuccessListener {
            Log.d("HomeActivity:::","Succesfully saved site data to firebase database")
            sites_progressBar.visibility = View.GONE
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("")
            //set message for alert dialog
            builder.setMessage("Site Added Successfully")
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            //performing positive action
            builder.setPositiveButton("Ok"){dialogInterface, which ->
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("role",role)
                startActivity(intent)
            }
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()
        }.addOnFailureListener {
            Log.d("HomeActivity:::","Failed to save site data:${it.message}")
            sites_progressBar.visibility = View.GONE
            Toast.makeText(this,"Failed to save user:${it.message}",Toast.LENGTH_SHORT).show()
        }
    }
    private fun hideKeyboard(view: View) {
        view?.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setListViewHeightBasedOnChildren
                (listView: ListView) {
        val listAdapter = listView.getAdapter()
        if (listAdapter == null) return
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
            View.MeasureSpec.UNSPECIFIED)
        var totalHeight = 0
        for (i in 0..listAdapter.getCount() - 1) {
            val view = listAdapter.getView(i, null, listView)
            if (i == 0)
                view.setLayoutParams(ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT))
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += view.getMeasuredHeight()
        }
        val params = listView.getLayoutParams()
        params.height = totalHeight + (listView.getDividerHeight() *
                (listAdapter.getCount()-1))
        listView.setLayoutParams(params)
        scrollView_home.scrollToDescendant(scrollTop)
    }

}
class Sites(val uid: String,val site_name: String, val blocks: String,val location: String,val city: String,val state: String,val pinCode: String,val referral_code: String)

