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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_queue.*

class CreateQueueActivity : AppCompatActivity() {

    //Variables
    lateinit var title : String
    lateinit var subtitle : String

    //Reference my modal
    lateinit var myModal : Dialog

    //Admob
    lateinit var mAdView : AdView

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_queue)

        //Admob - change to legit ad - from Manifest
        MobileAds.initialize(this, "ca-app-pub-8395326091077015~5058161197")

        mAdView = findViewById(R.id.createQueueAdView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        createQueueBtn.setOnClickListener {
            title = titleInput.text.toString()
            subtitle = subtitleInput.text.toString()

            if(title.isEmpty()){
                //Create alert to tell user to enter email and password
                val builder = AlertDialog.Builder(this@CreateQueueActivity)
                // Display a message on alert dialog
                builder.setMessage("A Title must be entered.")
                // Display a neutral button on alert dialog
                builder.setNeutralButton("Dismiss") { _, _ ->
                    //Do nothing
                }
                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()
                // Display the alert dialog on app interface
                dialog.show()

            }else {

                val ref1 = FirebaseDatabase.getInstance().reference.child("Queuez").push()
                val newQueueId = ref1.key

                //Add info to Queuez
                val queueMap = mapOf("Title" to title, "Subtitle" to subtitle, "QueueCode" to newQueueId)
                ref1.updateChildren(queueMap)

                //Update your "My Queuez"
                val userMyQueuezMap = mapOf(newQueueId to 1)
                if (userId != null){
                    FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("MyQueuez").updateChildren(userMyQueuezMap)
                }

                //go to MyQueuez
                val myQueuezIntent = Intent(this, MyQueuezActivity::class.java)
                //Send the name and address of the location to the Queue
                myQueuezIntent.putExtra("title", title)
                myQueuezIntent.putExtra("subtitle", subtitle)
                myQueuezIntent.putExtra("queueCode", newQueueId)

                startActivity(myQueuezIntent)
            }
        }

        createQueueInfoBtn.setOnClickListener {

            //Create alert to tell user to enter email and password
            val builder = AlertDialog.Builder( this@CreateQueueActivity)
            // Display a message on alert dialog
            builder.setMessage("The \"Queue Title\" is required, and will be the title of the Queue.\n\nThe \"Queue Subtitle\" is optional, but may be helpful if your \"Queue Title\" is the same for multiple Queuez.")
            // Display a neutral button on alert dialog
            builder.setNeutralButton("Dismiss") { _, _ ->
                //Do nothing
            }
            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()
            // Display the alert dialog on app interface
            dialog.show()

        }

        createQueueBackBtn.setOnClickListener {

            super.finish()

        }

        subtitleInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }

        titleInput.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyBoard(v)
            }
        }

    }

    //hide the keyboard
    fun hideKeyBoard(v : View){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
}
