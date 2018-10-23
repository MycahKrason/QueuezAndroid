package com.mycahkrason.queuez

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_create_queue.*
import kotlinx.android.synthetic.main.activity_join_queuez.*
import java.util.HashMap

class JoinQueuezActivity : AppCompatActivity() {

    //Variables
    lateinit var displayName : String
    lateinit var queueCode : String

    lateinit var title: String
    lateinit var subtitle: String

    //Reference my modal
    lateinit var myModal : Dialog

    //Admob
    lateinit var mAdView : AdView

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_queuez)

        //Admob - change to legit ad - from Manifest
        MobileAds.initialize(this, "ca-app-pub-8395326091077015~5058161197")

        mAdView = findViewById(R.id.joinQueueAdView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        joinQueueActivityBtn.setOnClickListener {

            displayName = displayNameInput.text.toString()
            queueCode = queueCodeInput.text.toString()

            if(displayName.isEmpty() || queueCode.isEmpty()){

                //Create alert
                val builder = AlertDialog.Builder(this@JoinQueuezActivity)
                // Display a message on alert dialog
                builder.setMessage("A Display Name and Queue Code must be entered.")
                // Display a neutral button on alert dialog
                builder.setNeutralButton("Dismiss") { _, _ ->
                    //Do nothing
                }
                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()
                // Display the alert dialog on app interface
                dialog.show()

            }else{
                FirebaseDatabase.getInstance().reference.child("Queuez").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {

                        p0.children.forEach {

//                            Log.d("JoinQueue", "${it.key}")
                            if(it.key == queueCode){

                                //TODO: check to see if the user is already a member of the queue
                                val queueInfo = it.value as HashMap<*, *>

                                title = queueInfo["Title"] as String
                                subtitle = queueInfo["Subtitle"] as String

                                //Check for members
                                if(queueInfo["Members"] != null){
                                    val memberDict = queueInfo["Members"] as HashMap<*, *>


                                    memberDict.values.forEach {
                                        val memberInfoDict = it as HashMap<*, *>
                                        val checkedId = memberInfoDict["UserID"]
                                        //TODO: check to see if checkedID is equeal to the user id - if it is, alert the user that they are already in the queue
                                        if(checkedId == userId){
                                            //Create alert
                                            val builder = AlertDialog.Builder(this@JoinQueuezActivity)
                                            // Display a message on alert dialog
                                            builder.setMessage("You are already a member of this Queue.")
                                            // Display a neutral button on alert dialog
                                            builder.setNeutralButton("Dismiss") { _, _ ->
                                                //Do nothing
                                            }
                                            // Finally, make the alert dialog using builder
                                            val dialog: AlertDialog = builder.create()
                                            // Display the alert dialog on app interface
                                            dialog.show()
                                            return
                                        }

                                    }


                                }

                                //Check for Deleted Entry
                                if(queueInfo["DeleteList"] != null){
                                    val memberDict = queueInfo["DeleteList"] as HashMap<*, *>


                                    memberDict.values.forEach {
                                        val memberInfoDict = it as HashMap<*, *>
                                        val checkedId = memberInfoDict["UserID"]
                                        //TODO: check to see if checkedID is equeal to the user id - if it is, then the user has already been added to the delete list
                                        if(checkedId == userId){
                                            //Dont do anything
                                        }else{
                                            //Add to Delete List
                                            val deleteMap = mapOf("UserID" to userId)
                                            val deleteRef = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("DeleteList").push()
                                            deleteRef.updateChildren(deleteMap)
                                        }

                                    }


                                }else{
                                    //Add to Delete List
                                    val deleteMap = mapOf("UserID" to userId)
                                    val deleteRef = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("DeleteList").push()
                                    deleteRef.updateChildren(deleteMap)
                                }

                                //TODO: if not, add the user to the queue

                                //Add to Member List
                                val memberMap = mapOf("DisplayName" to displayName, "UserID" to userId)
                                val memberRef = FirebaseDatabase.getInstance().reference.child("Queuez").child(queueCode).child("Members").push()
                                memberRef.updateChildren(memberMap)

                                //Add to Users JoinedQueuez list
                                val joinedQueuezMap = mapOf("CurrentlyJoined" to true, "DisplayName" to displayName)
                                val joinedQueuezRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId!!).child("JoinedQueuez").child(queueCode)
                                joinedQueuezRef.updateChildren(joinedQueuezMap)


                                goToHasJoineedQueuez()
                                return
                            }

                        }

                        //Alert the user that the queue was not found/wrong queue code
                        //Create alert
                        val builder = AlertDialog.Builder(this@JoinQueuezActivity)
                        // Display a message on alert dialog
                        builder.setMessage("The Queue Code you have entered is incorrect.\nMake sure there are no spaces before or after the code.")
                        // Display a neutral button on alert dialog
                        builder.setNeutralButton("Dismiss") { _, _ ->
                            //Do nothing
                        }
                        // Finally, make the alert dialog using builder
                        val dialog: AlertDialog = builder.create()
                        // Display the alert dialog on app interface
                        dialog.show()

                    }

                })


            }

        }


        joinQueuezInfoBtn.setOnClickListener {

            //Create alert to tell user to enter email and password
            val builder = AlertDialog.Builder( this@JoinQueuezActivity)
            // Display a message on alert dialog
            builder.setMessage("The \"Display Name\" is the name you will be known by in the Queue. This will be visible to the entity who created the Queue.\n\nThe \"Queue Code\" is the unique password for the Queue you are trying to join. This will typically be distributed by the entity that created the Queue.")
            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        joineQueuezBackBtn.setOnClickListener {

            super.finish()

        }

        displayNameInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }

        queueCodeInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }


    }

    fun goToHasJoineedQueuez(){
        val intent = Intent(this, HasJoinedQueuezActivity::class.java)

        //Send the name and address of the location to the Queue
        intent.putExtra("title", title)
        intent.putExtra("subtitle", subtitle)
        intent.putExtra("queueCode", queueCode)

        startActivity(intent)
    }

    //hide the keyboard
    fun hideKeyBoard(v : View){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}
