package com.mycahkrason.queuez

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Window
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mycahkrason.queuez.Adapters.MembersListAdapter
import com.mycahkrason.queuez.Model.MemberObject
import com.mycahkrason.queuez.Model.QueueObject
import kotlinx.android.synthetic.main.activity_my_queuez.*
import kotlinx.android.synthetic.main.activity_queuez_main.*
import java.util.*


class MyQueuezActivity : AppCompatActivity() {

    //Sent from other activities
    var title : String? = null
    var subtitle : String? = null
    lateinit var queueCode : String

    //Admob
    lateinit var mAdView : AdView

    //Get user id
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    //Reference my modal
    lateinit var myModal : Dialog

    //set up Queue Lists
    lateinit var membersArray : MutableList<MemberObject>

    //Set up adapter
    lateinit var adapter: MembersListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_queuez)

        //Admob - change to legit ad - from Manifest
        MobileAds.initialize(this, "ca-app-pub-8395326091077015~5058161197")

        mAdView = findViewById(R.id.myQueuezAdView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //Get title, subtitle, and queueCode from the previous Activity
        title = intent.getStringExtra("title")
        subtitle = intent.getStringExtra("subtitle")
        queueCode = intent.getStringExtra("queueCode")

        myQueueTitleDisplay.text = title
        myQueueSubtitleDisplay.text = subtitle

        //get members
        getMembers()

        //Delete the queue
        deleteQueueBtn.setOnClickListener {

            //Make an alert because a toast is lame
            // Initialize a new instance of
            val builder = AlertDialog.Builder(this@MyQueuezActivity)
            // Display a message on alert dialog
            builder.setMessage("Are you sure you want to delete this Queue")

            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Yes"){dialog, which ->
                // Do something when user press the positive button

                //TODO: go through every user in the Deleted List and remove the queue from their Joined List
                FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("DeleteList").addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        //Loop through everyone in Deleted List
                        if(p0.value != null){
                            val deleteListInfo = p0.value as HashMap<*, *>
                            p0.children.forEach {
                                val deleteIdList = it.value as HashMap<*, *>
                                val deleteId = deleteIdList["UserID"] as String
                                val deleteJoinedQueueId = FirebaseDatabase.getInstance().reference.child("Users").child(deleteId).child("JoinedQueuez").child(queueCode)
                                deleteJoinedQueueId.removeValue()
                            }
                        }

                        //Delete from Users MyQueuez
                        if(userId != null){
                            val ref1 = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("MyQueuez").child(queueCode)
                            ref1.removeValue()
                        }

                        //Delete from Queuez
                        val ref2 = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode)
                        ref2.removeValue()

                    }

                })

                super.finish()

            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        myQueuezInfoBtn.setOnClickListener {

            //Create alert to tell user to enter email and password
            val builder = AlertDialog.Builder( this@MyQueuezActivity)
            // Display a message on alert dialog
            builder.setMessage("The Queue Code for this Queue is as follows: \n\n \"$queueCode\" \n\nDistribute this code to people you want to join your Queue. \n\nClick on a member to remove them from the Queue.\n\nPress \"Delete Queue\" if you would like to delete the Queue and have it removed from your \"My Queuez\" list.")

            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        myQueuezbackBtn.setOnClickListener {

            super.finish()

        }

    }

    fun getMembers(){

        membersArray = arrayListOf()

        val ref = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                //TODO: Remove them from the Array
                if(p0.value != null){
                    val memberInfo = p0.value as HashMap<*, *>

                    //userId to remove
                    val userIdRemove = memberInfo["UserID"] as String

                    val theIndex = membersArray.indexOfFirst {
//                        Log.d("FindMe", it.memberID)
                        it.memberID == userIdRemove

                    }

                    if(theIndex >= 0){
                        membersArray.removeAt(theIndex)
                    }
                }


                //Refresh recycle view
                adapter.notifyDataSetChanged()

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                if(p0.value != null){
                    val memberInfo = p0.value as HashMap<*, *>

                    //Create a MemberObject
                    val newMember = MemberObject()
                    newMember.memberName = memberInfo["DisplayName"] as String
                    newMember.memberID = memberInfo["UserID"] as String

                    //add queue to queue array
                    membersArray.add(newMember)
                }


                //Refresh recycle view
                adapter.notifyDataSetChanged()

            }



        })

        //Set up adapter
        adapter = MembersListAdapter(this, membersArray){


            //code is executed when clicked
            val idToRemove = it.memberID

            //Alert the user and let them decide if they want to delete the member
            //Make an alert because a toast is lame
            val builder = AlertDialog.Builder(this@MyQueuezActivity)
            // Display a message on alert dialog
            builder.setMessage("Are you sure you want to remove this member from the Queue?")

            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Yes") { dialog, which ->
                // Do something when user press the positive button

                Log.d("FindMe", "You chose to delete the member")

                //TODO:delete the member from Queuez Memebers
                FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                         p0.children.forEach {
                             val deleteMemberDict = it.value as HashMap<*, *>
                             if(deleteMemberDict["UserID"] == idToRemove){


                                 val idForMember = it.key as String

                                 val deleteThis = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").child(idForMember)
                                 deleteThis.removeValue()


                                 if (idToRemove != null) {
                                     val changeCurrentlyJoinedMap = mapOf("CurrentlyJoined" to false )
                                     val changeToFalse = FirebaseDatabase.getInstance().reference.child("Users").child(idToRemove).child("JoinedQueuez").child(queueCode)
                                     changeToFalse.updateChildren(changeCurrentlyJoinedMap)
                                 }

                             }

                        }

                    }

                })


            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        recyclerMyQueuez.adapter = adapter
        val layoutManager = LinearLayoutManager(this@MyQueuezActivity)
        recyclerMyQueuez.layoutManager = layoutManager
    }
}
