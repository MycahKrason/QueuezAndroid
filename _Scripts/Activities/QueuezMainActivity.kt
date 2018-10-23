package com.mycahkrason.queuez

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mycahkrason.queuez.Adapters.QueuezListAdapter
import com.mycahkrason.queuez.Model.QueueObject
import kotlinx.android.synthetic.main.activity_queuez_main.*
import java.util.HashMap
import android.support.v7.widget.DividerItemDecoration
import android.view.Window
import android.widget.TextView
import com.google.android.gms.ads.MobileAds


class QueuezMainActivity : AppCompatActivity() {

    //set up Queue Lists
    lateinit var myQueuezArray : MutableList<QueueObject>
    lateinit var joinedQueuezArray : MutableList<QueueObject>

    //Set adapter
    lateinit var adapter : QueuezListAdapter

    //Set up event listener so i can turn it off
    lateinit var childEvent : ChildEventListener
    lateinit var childEvent2 : ChildEventListener

    //Reference my modal
    lateinit var myModal : Dialog

    var retrievedQueueCount = 1

    //Set up the global user
    val user = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queuez_main)

        //Initialize queueArray
        myQueuezArray = arrayListOf()
        joinedQueuezArray = arrayListOf()

        createQueueMyQueuezBtn.setOnClickListener {

            //Launch Create Queuez activity
            val intent = Intent(this, CreateQueueActivity::class.java)
            startActivity(intent)

        }

        joinQueueBtn.setOnClickListener {

            //Launch Join Queuz activity
            val intent = Intent(this, JoinQueuezActivity::class.java)

            startActivity(intent)

        }

        //toggle btns at the top
        myQueuezTab.setOnClickListener {
            //Change background
            myQueuezTab.setBackgroundColor(Color.parseColor("#ffffff"))
            joinedQueuezTab.setBackgroundColor(Color.parseColor("#70CDFF"))

            //Change Text color
            myQueuezTab.setTextColor(Color.parseColor("#70CDFF"))
            joinedQueuezTab.setTextColor(Color.parseColor("#ffffff"))



            retrievedQueueCount = 1
            getQueuez()
        }

        joinedQueuezTab.setOnClickListener {
            //Change background
            joinedQueuezTab.setBackgroundColor(Color.parseColor("#ffffff"))
            myQueuezTab.setBackgroundColor(Color.parseColor("#70CDFF"))

            //Change Text color
            joinedQueuezTab.setTextColor(Color.parseColor("#70CDFF"))
            myQueuezTab.setTextColor(Color.parseColor("#ffffff"))

            retrievedQueueCount = 2
            getQueuez()
        }

        mainQueuezInfoBtn.setOnClickListener {

            //Create alert to tell user to enter email and password
            val builder = AlertDialog.Builder(this@QueuezMainActivity)
            // Display a message on alert dialog


            builder.setMessage("\"My Queuez\" will provide a list of the Queuez that you have created.\n\n\"Joined Queuez\" will provide a list of the Queuez you have joined.\n\n\"Create Queue\" will create a Queue to allow others to join.\n\n\"Join Queue\" will let you join a Queue if you have a Queue Code.")
            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        logoutBtn.setOnClickListener {

            super.finish()

        }

    }

    override fun onResume() {
        super.onResume()
        //Refresh recycle view
        getQueuez()

    }

    private fun getQueuez(){

        myQueuezArray = arrayListOf()
        joinedQueuezArray = arrayListOf()

        //Get the users MyQueuez
        val ref1 = FirebaseDatabase.getInstance().reference.child("Users").child("$user").child("MyQueuez")

        childEvent = ref1.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //Refresh recycle view
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //Refresh recycle view
                adapter.notifyDataSetChanged()

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                val queueId = p0.key.toString()
                val ref2 = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueId)

                ref2.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.value != null){
                            val queueInfo = p0.value as HashMap<*, *>
                            //Create a queue object
                            val newQueue = QueueObject()
                            newQueue.title = queueInfo["Title"] as String
                            newQueue.subtitle = queueInfo["Subtitle"] as String
                            newQueue.queueCode = queueInfo["QueueCode"] as String

                            //add queue to queue array
                            myQueuezArray.add(newQueue)

                            Log.d("MainQueuez", "${queueInfo["Title"]}")



                        }



                        //Refresh recycle view
                        adapter.notifyDataSetChanged()
                    }

                })

            }

        })


        //Get the users JoinedQueuez
        val ref3 = FirebaseDatabase.getInstance().reference.child("Users").child("$user").child("JoinedQueuez")

        childEvent2 = ref3.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                //Refresh recycle view
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //Refresh recycle view
                adapter.notifyDataSetChanged()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {


                val queueId = p0.key.toString()
                val ref4 = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueId)

                ref4.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if(p0.value != null){
                            val queueInfo = p0.value as HashMap<*, *>
                            //Create a queue object
                            val newQueue = QueueObject()
                            newQueue.title = queueInfo["Title"] as String
                            newQueue.subtitle = queueInfo["Subtitle"] as String
                            newQueue.queueCode = queueInfo["QueueCode"] as String

                            //add queue to queue array
                            joinedQueuezArray.add(newQueue)

                            Log.d("MainQueuez", "${queueInfo["Title"]}")

                        }



                        //Refresh recycle view
                        adapter.notifyDataSetChanged()
                    }

                })

            }

        })


        //Set up adapter based on the button that is selected
        if(retrievedQueueCount == 1){
            adapter = QueuezListAdapter(this@QueuezMainActivity, myQueuezArray){
                //this is executed when clicked on item
                queueObject ->

                //go to MyQueuez
                val myQueuezIntent = Intent(this, MyQueuezActivity::class.java)
                //Send the name and address of the location to the chatroom
                myQueuezIntent.putExtra("title", queueObject.title)
                myQueuezIntent.putExtra("subtitle", queueObject.subtitle)
                myQueuezIntent.putExtra("queueCode", queueObject.queueCode)

                startActivity(myQueuezIntent)

            }
        }else if(retrievedQueueCount == 2){
            adapter = QueuezListAdapter(this@QueuezMainActivity, joinedQueuezArray){
                //this is executed when clicked on item
                queueObject ->

                //go to MyQueuez
                val joinedQueuezIntent = Intent(this, HasJoinedQueuezActivity::class.java)
                //Send the name and address of the location to the chatroom
                joinedQueuezIntent.putExtra("title", queueObject.title)
                joinedQueuezIntent.putExtra("subtitle", queueObject.subtitle)
                joinedQueuezIntent.putExtra("queueCode", queueObject.queueCode)

                startActivity(joinedQueuezIntent)

            }
        }

        recyclerViewQueuezMain.adapter = adapter

        val layoutManager = LinearLayoutManager(this@QueuezMainActivity)

        recyclerViewQueuezMain.layoutManager = layoutManager
    }

    @SuppressLint("MissingSuperCall")
    override fun onPause() {
        super.onStop()
        FirebaseDatabase.getInstance().reference.child("Users").child("$user").child("MyQueuez").removeEventListener(childEvent)
        FirebaseDatabase.getInstance().reference.child("Users").child("$user").child("JoinedQueuez").removeEventListener(childEvent2)

    }

}
