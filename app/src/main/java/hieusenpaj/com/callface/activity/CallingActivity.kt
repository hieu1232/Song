package hieusenpaj.com.callface.activity

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.callface.R
import kotlinx.android.synthetic.main.activity_calling.*

class CallingActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var senderUserId: String? = null
    var senderImage: String? = null
    var senderName: String? = null
    var receiverUserId: String? = null
    var receiverImage: String? = null
    var receiverName: String? = null
    var checker = ""
    var callingId = ""
    var receiveId = ""
    var userRef: DatabaseReference? = null
    var mediaPlayer:MediaPlayer?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        mediaPlayer = MediaPlayer.create(this,R.raw.ringing)

        mAuth = FirebaseAuth.getInstance()
        senderUserId = mAuth!!.currentUser!!.uid
        receiverUserId = intent.extras!!.getString("id")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        setUp()
        iv_cancel_call.setOnClickListener {
            checker = "clicked"
            cancelCalling()
            mediaPlayer!!.stop()
        }
        iv_make_call.setOnClickListener {

            val hashMap :HashMap<String,Any> = HashMap()
            hashMap.put("picked","picked")
            userRef!!.child(senderUserId).child("Ringing").updateChildren(hashMap)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful){
                        startActivity(Intent(this,VideoChatActivity::class.java))


                    }
                }
            mediaPlayer!!.stop()
        }
    }


    private fun setUp() {
        userRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.child(receiverUserId).exists()) {
                    receiverImage = p0.child(receiverUserId).child("image").value.toString()
                    receiverName = p0.child(receiverUserId).child("name").value.toString()
                    Glide.with(this@CallingActivity)
                        .load(receiverImage)
                        .into(iv)
                    tv.text = receiverName

                }
                if (p0.child(senderUserId).exists()) {
                    senderImage = p0.child(senderUserId).child("image").value.toString()
                    senderName = p0.child(senderUserId).child("name").value.toString()
                }

            }

        })
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer!!.start()
        userRef!!.child(receiverUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (checker != "clicked" && !p0!!.hasChild("Calling") && !p0.hasChild("Ringing")) {

                    val calling: HashMap<String, Any> = HashMap()
                    calling["calling"] = receiverUserId!!
                    userRef!!.child(senderUserId).child("Calling").updateChildren(calling)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val ringing: HashMap<String, Any> = HashMap()
                                ringing["ringing"] = senderUserId!!
                                userRef!!.child(receiverUserId).child("Ringing")
                                    .updateChildren(ringing)
                            }
                        }
                }
            }

        })
        userRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.child(senderUserId).hasChild("Ringing") && !p0.child(senderUserId).hasChild(
                        "Calling"
                    )
                ) {
                    iv_make_call.visibility = View.VISIBLE
                }
                if (p0.child(receiverUserId).child("Ringing").hasChild("picked")){
                    mediaPlayer!!.stop()
                   val intent = Intent(this@CallingActivity,VideoChatActivity::class.java)
                    startActivity(intent)

                }
            }

        })

    }

    private fun cancelCalling() {
        userRef!!.child(senderUserId).child("Calling")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists() && p0.hasChild("calling")) {
                        callingId = p0.child("calling").value.toString()

                        userRef!!.child(callingId).child("Ringing").removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    userRef!!.child(senderUserId).child("Calling").removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                onBackPressed()
                                                Toast.makeText(this@CallingActivity,"sent",Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                    }else{
                        onBackPressed()
                    }
                }

            })



        userRef!!.child(senderUserId).child("Ringing")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.exists() && p0.hasChild("ringing")) {
                        receiveId = p0.child("ringing").value.toString()

                        userRef!!.child(receiveId).child("Calling").removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    userRef!!.child(senderUserId).child("Ringing").removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                onBackPressed()
                                                Toast.makeText(this@CallingActivity,"recevei",Toast.LENGTH_SHORT).show()

                                            }
                                        }
                                }
                            }
                    }else{
                        onBackPressed()
                    }
                }

            })

    }
}
