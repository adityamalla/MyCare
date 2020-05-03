package com.android.mycare

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.content_flats.*
import kotlinx.android.synthetic.main.content_flats.add_flats_button
import kotlinx.android.synthetic.main.content_flats.editText_flats_add
import kotlinx.android.synthetic.main.content_flats.flats_list
import kotlinx.android.synthetic.main.content_flats.imageView3
import kotlinx.android.synthetic.main.content_flats.imageView_add_flats
import kotlinx.android.synthetic.main.content_flats.scrollTopFlatsPage
import kotlinx.android.synthetic.main.content_flats.spinner_blocks
import kotlinx.android.synthetic.main.content_flats.spinner_sites


class FlatsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    var listItems: ArrayList<String>? = null
    var profilePicURI: String? = ""
    var username: String? = ""
    lateinit var imageProfileView: ImageView
    lateinit var welcomeText: TextView
    private val PERMISSION_REQUEST_CODE = 200
    private var mDatabase: DatabaseReference? = null
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flats)
        if (checkPermission()) {
            requestPermissionAndContinue()
        }
        toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar)
        flats_progressBar.visibility = View.GONE
        drawerLayout = findViewById(R.id.drawer_layout1)
        navView = findViewById(R.id.nav_view1)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("sites");
        val query = mDatabase!!.orderByChild("site_name");
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
                val arrayAdapter = MyCustomSpinnerAdapter(this@FlatsActivity,sitetitleList)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_sites.setAdapter(arrayAdapter)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        query.addValueEventListener(siteSpinnerListener)
        spinner_sites.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                val itemSelected = parent.getItemAtPosition(position) as Spinner_Model
                val query = mDatabase!!.orderByChild("uid").equalTo(itemSelected.id.toString())
                val specificsiteSelected = object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val blocksList =  ArrayList<String>()
                            blocksList.add("please select a block")
                            for(dataSnapshot1 in dataSnapshot.children) {
                                val blocks: String? = dataSnapshot1.child("blocks").getValue().toString()
                                Log.d("FlatsActivity:::",blocks+"***")
                                if (blocks!!.contains(",")){
                                    val blocksArray = blocks?.split(",")
                                    if (blocksArray != null) {
                                        if (blocksArray.size > 0) {
                                            for (i in 0..blocksArray.size - 1)
                                                blocksList.add(blocksArray[i])
                                        }
                                    }
                                }else{
                                    if(blocks.trim().length>0)
                                    blocksList.add(blocks)
                                }
                            }
                            val arrayAdapter = ArrayAdapter(this@FlatsActivity,android.R.layout.simple_spinner_item,blocksList)
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner_blocks.setAdapter(arrayAdapter)
                            if(blocksList.size > 1){
                                spinner_blocks.visibility = View.VISIBLE
                                imageView3.visibility = View.VISIBLE
                                val params = editText_flats_add.layoutParams as ConstraintLayout.LayoutParams
                                params.startToStart = spinner_blocks.id
                                params.topToBottom = spinner_blocks.id
                                params.endToEnd = spinner_blocks.id
                                editText_flats_add.requestLayout()
                            }else{
                                spinner_blocks.visibility = View.GONE
                                imageView3.visibility = View.GONE
                                val params = editText_flats_add.layoutParams as ConstraintLayout.LayoutParams
                                params.startToStart = spinner_sites.id
                                params.topToBottom = spinner_sites.id
                                params.endToEnd = spinner_sites.id
                                editText_flats_add.requestLayout()
                            }

                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // Failed to read value
                    }
                }
                query.addValueEventListener(specificsiteSelected)
            }
            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }
        listItems = ArrayList()
        val myListAdapter = MyListAdapter(this, listItems!!)
        imageView_add_flats.setOnClickListener {
            listItems!!.add(editText_flats_add.text.toString())
            flats_list.adapter = myListAdapter
            setListViewHeightBasedOnChildren(flats_list)
            editText_flats_add.setText("")
        }
        scrollView_flats.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            if (i2 > 500) {
                scrollTopFlatsPage.setVisibility(View.VISIBLE);
            }else {
                scrollTopFlatsPage.setVisibility(View.INVISIBLE);
            }
        }
        scrollTopFlatsPage.setOnClickListener {
            scrollView_flats.scrollTo(0,0)
        }
        add_flats_button.setOnClickListener {
            flats_progressBar.visibility = View.VISIBLE
            val site_selected_obj = spinner_sites.selectedItem as Spinner_Model
            val site_selected_id = site_selected_obj.id as String
            if(site_selected_id.equals("-1")){
                flats_progressBar.visibility = View.GONE
                Toast.makeText(this,"Please select site!!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if(flats_list.count == 0){
            flats_progressBar.visibility = View.GONE
            Toast.makeText(this,"Please enter flat numbers!!",Toast.LENGTH_SHORT).show()
            return@setOnClickListener
            }
            saveSitesFlatsIntegratedDataToFireBaseDatabase(site_selected_id)
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_home -> {
                val intent = Intent(this, FlatsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_flats -> {
                val intent = Intent(this, FlatsActivity::class.java)
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
        scrollView_flats.scrollToDescendant(scrollTopFlatsPage)
    }
    private fun saveSitesFlatsIntegratedDataToFireBaseDatabase(site_selected:String){
        val itemsCount = flats_list.childCount
        Log.d("FlatsActivity:::","itemsCount---"+itemsCount)
        var flatsData: String = ""
        if(itemsCount > 1) {
            for (i in 0..itemsCount - 1) {
                val view = flats_list.getChildAt(i)
                flatsData =
                    flatsData + (view.findViewById<TextView>(R.id.blocks_or_flats_list_items)).getText().toString() + ","
            }
            flatsData = flatsData.substring(0, flatsData.length - 1)
        }else if(itemsCount == 1) {
            val view = flats_list.getChildAt(0)
            flatsData = (view.findViewById<TextView>(R.id.blocks_or_flats_list_items)).getText().toString()
        }
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("sites_flats_map")
        val key = ref.push().key
        Log.d("FlatsActivity:::","key--"+key)
        val ref1 = FirebaseDatabase.getInstance().getReference("/sites_flats_map/${key}")
        var blockSelected = ""
        if(spinner_blocks.visibility == View.VISIBLE) {
            blockSelected  = spinner_blocks.selectedItem.toString()
        }
        val flatsSiteMap = key?.let { FlatsSiteMap(it,site_selected,blockSelected,flatsData) }
        ref1.setValue(flatsSiteMap).addOnSuccessListener {
            Log.d("FlatsActivity:::","Succesfully saved site data to firebase database")
            flats_progressBar.visibility = View.GONE
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("")
            //set message for alert dialog
            builder.setMessage("Flats Added Successfully")
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            //performing positive action
            builder.setPositiveButton("Ok"){dialogInterface, which ->
                val intent = Intent(this, FlatsActivity::class.java)
                startActivity(intent)
            }
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()
        }.addOnFailureListener {
            Log.d("FlatsActivity:::","Failed to save flats data:${it.message}")
            flats_progressBar.visibility = View.GONE
            Toast.makeText(this,"Failed to flats user:${it.message}",Toast.LENGTH_SHORT).show()
        }
    }
}
class FlatsSiteMap(val uid: String,val site_id: String, val block: String,val flats: String)
