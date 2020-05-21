package com.android.mycare

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
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
import kotlinx.android.synthetic.main.activity_registration.*
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
    var site_id_selected:String=""
    var user_id_selected:String=""
    var role_id_selected:String=""
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
        toolbar = this.findViewById(R.id.toolbar2)
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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("role_master")
        val query = mDatabase!!.orderByChild("role_name")
        val roleSpinnerListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val rolesList = java.util.ArrayList<Spinner_Model>()
                    rolesList.add(Spinner_Model("please select a role","-1"))
                    for(dataSnapshot1 in dataSnapshot.children){
                        val role_name = dataSnapshot1.child("role_name").getValue()
                        val role_id:String = dataSnapshot1.child("role_id").getValue().toString()
                        rolesList.add(Spinner_Model(role_name as String,role_id as String))
                    }
                    val arrayAdapter = MyCustomSpinnerAdapter(this@ModifyUserDetails,rolesList)
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_roles.setAdapter(arrayAdapter)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        query.addValueEventListener(roleSpinnerListener)
        val adapter = PoiAdapter(this, android.R.layout.simple_list_item_1, getSiteSuggestions())
        autoCompleteTextView_Sites.setAdapter(adapter)
        autoCompleteTextView_Sites.threshold = 3
        autoCompleteTextView_Sites.setOnItemClickListener() { parent, _, position, id ->
            val selectedPoi = parent.adapter.getItem(position) as MyObject?
            autoCompleteTextView_Sites.setText(selectedPoi?.objectName)
            site_id_selected = selectedPoi?.objectId as String
            autoCompleteTextView_Site_Users.setText("")
            val adapter1 = PoiAdapter(this, android.R.layout.simple_list_item_1, getUserBasedonSiteSuggestions(site_id_selected))
            autoCompleteTextView_Site_Users.setAdapter(adapter1)
            autoCompleteTextView_Site_Users.threshold = 3
        }
        autoCompleteTextView_Site_Users.setOnItemClickListener() { parent, _, position, id ->
            val selectedPoiUser = parent.adapter.getItem(position) as MyObject?
            autoCompleteTextView_Site_Users.setText(selectedPoiUser?.objectName)
            user_id_selected = selectedPoiUser?.objectId as String
        }
        button_modify_role.setOnClickListener {
            val obj:Spinner_Model = spinner_roles.selectedItem as Spinner_Model
            role_id_selected = obj.id.toString()
            val hashMap:HashMap<String, Any> = HashMap<String,Any>()
            hashMap.put("role", role_id_selected);
            FirebaseDatabase.getInstance().getReference().child("users").child(user_id_selected)
                .updateChildren(hashMap)

                        val builder = AlertDialog.Builder(this@ModifyUserDetails)
                        //set title for alert dialog
                        builder.setTitle("")
                        //set message for alert dialog
                        builder.setMessage("User Role Modified Successfully")
                        builder.setIcon(android.R.drawable.ic_dialog_alert)
                        //performing positive action
                        builder.setPositiveButton("Ok"){dialogInterface, which ->
                            val intent = Intent(this@ModifyUserDetails, ModifyUserDetails::class.java)
                            intent.putExtra("role",role)
                            startActivity(intent)
                        }
                        // Create the AlertDialog
                        val alertDialog: AlertDialog = builder.create()
                        // Set other dialog properties
                        alertDialog.setCancelable(false)
                        alertDialog.show()
        }
        mDatabase = null
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
        Log.d("ModifyUserDetails:::",profilePicUri+"***")
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
    public fun getSiteSuggestions():List<MyObject>{
        var myList: MutableList<MyObject> = mutableListOf<MyObject>()
        var count:Int = 0
            mDatabase = FirebaseDatabase.getInstance().getReference().child("sites");
            val query = mDatabase!!.orderByChild("site_name");
            val siteSuggestionsListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (dataSnapshot1 in dataSnapshot.children) {
                            val site_name = dataSnapshot1.child("site_name").getValue()
                            val uid = dataSnapshot1.child("uid").getValue()
                            myList?.add(count,MyObject(site_name as String,uid as String))
                            count++
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Failed to read value
                }
            }
            query!!.addValueEventListener(siteSuggestionsListener)
        return myList
    }
    public fun getUserBasedonSiteSuggestions(site_id:String):List<MyObject>{
        var myList: MutableList<MyObject> = mutableListOf<MyObject>()
        var count:Int = 0
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        val query = mDatabase!!.orderByChild("site_id").equalTo(site_id);
        val siteSuggestionsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (dataSnapshot1 in dataSnapshot.children) {
                        val firstname = dataSnapshot1.child("firstname").getValue()
                        val lastname = dataSnapshot1.child("lastname").getValue()
                        val name = "$firstname $lastname"
                        val uid = dataSnapshot1.child("uid").getValue()
                        myList?.add(count,MyObject(name as String,uid as String))
                        count++
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Failed to read value
            }
        }
        query!!.addValueEventListener(siteSuggestionsListener)
        return myList
    }
    inner class PoiAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val allPois: List<MyObject>):
        ArrayAdapter<MyObject>(context, layoutResource, allPois),
        Filterable {
        private var mPois: List<MyObject> = allPois

        override fun getCount(): Int {
            return mPois.size
        }

        override fun getItem(p0: Int): MyObject? {
            return mPois.get(p0)
        }

        override fun getItemId(p0: Int): Long {
            // Or just return p0
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
            if(mPois.size>0)
            view.text = "${mPois[position].objectName}"
            else
            view.text = "No result found"
            return view
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(charSequence: CharSequence?, filterResults: Filter.FilterResults) {
                    mPois = filterResults.values as List<MyObject>
                    notifyDataSetChanged()
                }

                override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                    val queryString = charSequence?.toString()?.toLowerCase()

                    val filterResults = Filter.FilterResults()
                    filterResults.values = if (queryString==null || queryString.isEmpty())
                        allPois
                    else
                        allPois.filter {
                            it.objectName.toLowerCase().contains(queryString)
                        }
                    return filterResults
                }
            }
        }
    }
}