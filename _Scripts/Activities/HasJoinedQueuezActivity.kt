package com.mycahkrason.queuez

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Window
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mycahkrason.queuez.Model.MemberObject
import kotlinx.android.synthetic.main.activity_has_joined_queuez.*
import kotlinx.android.synthetic.main.activity_my_queuez.*
import kotlinx.android.synthetic.main.activity_queuez_main.*
import java.util.HashMap

class HasJoinedQueuezActivity : AppCompatActivity() {

    //Sent from other activities
    var title : String? = null
    var subtitle : String? = null
    lateinit var queueCode : String
    lateinit var userDisplayName : String

    //Reference my modal
    lateinit var myModal : Dialog

    //Admob
    lateinit var mAdView : AdView

    //set up Members array to find out where you are
    lateinit var membersArray : MutableList<MemberObject>

    //Get user id
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_has_joined_queuez)

        //Admob - change to legit ad - from Manifest
        MobileAds.initialize(this, "ca-app-pub-8395326091077015~5058161197")

        mAdView = findViewById(R.id.hasJoinedAdView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //Get title, subtitle, and queueCode from the previous Activity
        title = intent.getStringExtra("title")
        subtitle = intent.getStringExtra("subtitle")
        queueCode = intent.getStringExtra("queueCode")

        hasJoinedQueueTitle.text = title
        hasJoinedQueueSubtitle.text = subtitle

        //get the users Placement in the Queue
        getCurrentNumber()

        //Set up the request Btn to be either Cancel Request - Rejoin Queue
        if(userId != null){
            FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("JoinedQueuez").child(queueCode).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    //
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if(p0.value != null) {
                        val currrentlyJoinedInfo = p0.value as HashMap<*, *>

                        val isCurrentlyJoined = currrentlyJoinedInfo["CurrentlyJoined"] as Boolean

                        //Set the Display name here so we don't have to reach out to the DB later
                        userDisplayName = currrentlyJoinedInfo["DisplayName"] as String

                        if(isCurrentlyJoined){
                            //You are in the Queue and need to be able to "Cancel Request"
                            cancelRequestBtn.text = "Cancel Request"

                        }else{
                            //You are not in the queue and will need the option to "Rejoin Queue"
                            cancelRequestBtn.text = "Rejoin Queue"

                        }


                    }

                }


            })

        }


        //Set up the Buttons
        cancelRequestBtn.setOnClickListener {

            //TODO: This needs to remove the user and also rejoin them
            if(userId != null){
                FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("JoinedQueuez").child(queueCode).addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        if(p0.value != null) {
                            val joinedInfo = p0.value as HashMap<*, *>

                            val isJoined = joinedInfo["CurrentlyJoined"] as Boolean

                            if(isJoined){


                                //Make an alert because a toast is lame
                                val builder = AlertDialog.Builder(this@HasJoinedQueuezActivity)
                                // Display a message on alert dialog
                                builder.setMessage("Are you sure you want to be removed from the queue?")

                                // Display a neutral button on alert dialog
                                builder.setNeutralButton("Dismiss") { _, _ ->
                                    //Do nothing
                                }

                                // Set a positive button and its click listener on alert dialog
                                builder.setPositiveButton("Yes") { dialog, which ->
                                    // Do something when user press the positive button

                                    cancelRequestBtn.text = "Rejoin Queue"

                                    //Remove from Queuez if user wants to
                                    FirebaseDatabase.getInstance().reference.child("Queuez").addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onCancelled(p0: DatabaseError) {
                                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {

                                            p0.children.forEach {

                                                if(queueCode == it.key as String){
                                                    //if you are here, we have found the queue
                                                    //Check to make sure there are some members
                                                    val queueDict = it.value as HashMap<*, *>
                                                    if(queueDict["Members"] != null){

                                                        val membersDict = queueDict["Members"] as HashMap<*, *>
                                                        membersDict.forEach{

                                                            val membersDict2 = it.value as HashMap<*, *>
                                                            if(membersDict2["UserID"] as String == userId) {

                                                                //Change Queue status to false
                                                                val mapToChangeQueueStatus = mapOf("CurrentlyJoined" to false)
                                                                FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("JoinedQueuez").child(queueCode).updateChildren(mapToChangeQueueStatus)

                                                                //Remove from Queues
                                                                val deleteMember = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").child(it.key as String)

                                                                deleteMember.removeValue { error, ref ->
                                                                    currentNumberDisplay.text = "Removed"
                                                                }

                                                            }

                                                        }
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

                            }else {
                                cancelRequestBtn.text = "Cancel Request"

                                //If you are here, that means CurrentlyJoined is false - you will want to Rejoin the queue

                                //Add to Member List
                                val memberMap = mapOf("DisplayName" to userDisplayName, "UserID" to userId)
                                val memberRef = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").push()
                                memberRef.updateChildren(memberMap)

                                //Add to Users JoinedQueuez list
                                val joinedQueuezMap = mapOf("CurrentlyJoined" to true, "DisplayName" to userDisplayName)
                                val joinedQueuezRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId!!).child("JoinedQueuez").child(queueCode)
                                joinedQueuezRef.updateChildren(joinedQueuezMap)

                                getCurrentNumber()

                            }

                        }

                    }


                })
            }
        }


        removeQueueBtn.setOnClickListener {

            //TODO: This will remove the queue from the Joined Queue list of the customer
            //Confirm that the user wants to Delete the Queue
            //Make an alert because a toast is lame
            val builder = AlertDialog.Builder(this@HasJoinedQueuezActivity)
            // Display a message on alert dialog
            builder.setMessage("Are you sure you want to remove this Queue from your list of \"Joined Queuez\"?")

            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Yes") { dialog, which ->
                // Do something when user press the positive button

                //Remove from Queuez
                FirebaseDatabase.getInstance().reference.child("Queuez").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        p0.children.forEach {

                            if(it.key as String == queueCode){
                                //you are in the correct queue - now you need to check for members
                                val queueDict = it.value as HashMap<*, *>
                                if(queueDict["Members"] != null){

                                    val memberDict = queueDict["Members"] as HashMap<*, *>
                                    memberDict.forEach {

                                        val membersDict2 = it.value as HashMap<*, *>
                                        if(membersDict2["UserID"] as String == userId) {
                                            Log.d("Lookforit", "you have found me")

                                            //Remove from Queues
                                            val deleteMember = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").child(it.key as String)

                                            //Remove from Users
                                            val deleteQueue = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("JoinedQueuez").child(queueCode)

                                            deleteMember.removeValue()
                                            deleteQueue.removeValue()
                                            returnAfterDelete()

                                        }else {
                                            //This means that the user is not a member
                                            //Remove from Users
                                            if (userId != null) {

                                                val deleteQueue = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("JoinedQueuez").child(queueCode)

                                                deleteQueue.removeValue()
                                                returnAfterDelete()

                                            }
                                        }


                                    }

                                }else{
                                    //if you are here, there are no members
                                    if(userId != null){

                                        val deleteQueue = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("JoinedQueuez").child(queueCode)

                                        deleteQueue.removeValue()
                                        returnAfterDelete()

                                    }

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

        hasJoinedInfoBtn.setOnClickListener {

            //Create alert to tell user to enter email and password
            val builder = AlertDialog.Builder( this@HasJoinedQueuezActivity)
            // Display a message on alert dialog
            builder.setMessage("\"Your Current Place\" is your current place in the Queue. As other members are assisted, your number will decrease until you are number 1.\n\n\"Cancel Request\" will remove you from the Queue, losing your place in the Queue, however the Queue will still be listed in your \"Joined Queuez\" list, so you may easily rejoin. Please note that rejoining will put you at the end of the Queue.\n\n\"Remove Queue\" will remove you from the Queue, and also remove the Queue from your \"Joined Queuez\" list.")
            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        hasJoinedBackBtn.setOnClickListener {

            super.finish()

        }


    }

    fun returnAfterDelete(){
        super.finish()
    }

    fun getCurrentNumber(){

        //clear out the array

        //check to see if there are any members in the queue at all
        FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                //
            }

            override fun onDataChange(p0: DataSnapshot) {
                //clear out the array
                membersArray = arrayListOf()

                if(p0.value != null) {
                    if(p0.hasChild("Members")){

                        FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").addChildEventListener(object: ChildEventListener{
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
                                //Todo - will probably need to remove the user from the array or something

                                //Remove them from the Array
                                if(p0.value != null){
                                    val memberInfo = p0.value as HashMap<*, *>

                                    //userId to remove
                                    val userIdRemove = memberInfo["UserID"] as String

                                    val theIndex = membersArray.indexOfFirst {

                                        it.memberID == userIdRemove

                                    }

                                    Log.d("Fuck", "$theIndex")
                                    //Remove the user from the array
                                    if(theIndex > -1) {
                                        membersArray.removeAt(theIndex)
                                    }

                                    //get and present the users index
                                    val findIndex = membersArray.indexOfFirst {

                                        it.memberID == userId

                                    }

                                    if(userIdRemove == userId){
                                        this@HasJoinedQueuezActivity.currentNumberDisplay.text = "Removed"
                                        //todo: modify the button
                                        cancelRequestBtn.text = "Rejoin Queue"

                                    }else if(findIndex != -1) {
                                        this@HasJoinedQueuezActivity.currentNumberDisplay.text = "${findIndex + 1}"
                                        //todo: modify the button
                                        cancelRequestBtn.text = "Cancel Request"

                                    }

                                }
                            }


                            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                                //Create an array of users
                                if(p0.value != null){
                                    val memberInfo = p0.value as HashMap<*, *>

                                    //Create a MemberObject
                                    val newMember = MemberObject()
                                    newMember.memberName = memberInfo["DisplayName"] as String
                                    newMember.memberID = memberInfo["UserID"] as String

                                    //add queue to queue array
                                    membersArray.add(newMember)
                                }

                                Log.d("Currently", "Does this get called everytime")

                                //get and present the users index
                                val foundIndex = membersArray.indexOfFirst {

                                    it.memberID == userId

                                }

                                if(foundIndex != -1){

                                    Log.d("Fuck", "$foundIndex")
                                    this@HasJoinedQueuezActivity.currentNumberDisplay.text = "${foundIndex + 1}"
                                }else{
                                    this@HasJoinedQueuezActivity.currentNumberDisplay.text = "Removed"
                                }

                            }

                        })

                    }else{
                        //There are no current members, meaning you are removed
                        currentNumberDisplay.text = "Removed"
                    }

                }

            }

        })





    }
}
