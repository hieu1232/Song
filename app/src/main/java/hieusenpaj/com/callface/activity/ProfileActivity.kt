package hieusenpaj.com.callface.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import hieusenpaj.com.callface.R
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    var senderUserId: String? = null
    var receiverUserId: String? = null
    var currentSate = "new"
    var friendRequestRef: DatabaseReference? = null
    var contactRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        senderUserId = mAuth!!.currentUser!!.uid

        receiverUserId = intent.extras!!.getString("id")
        friendRequestRef = FirebaseDatabase.getInstance().reference.child("Friend Request")
        contactRef = FirebaseDatabase.getInstance().reference.child("Contact")


        Glide.with(applicationContext)
            .load(intent.extras!!.getString("image"))
            .into(iv)
        tv.text = intent.extras!!.getString("name")
        managerClickEvents()
    }

    private fun managerClickEvents() {
        friendRequestRef!!.child(senderUserId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.hasChild(receiverUserId)) {

                    val requestType =
                        p0.child(receiverUserId).child("request_type").value.toString()
                    if (requestType == "sent") {
                        currentSate = "request_sent"
                        btn_add_friend.text = "Cancel friend request"
                    } else if (requestType == "received") {
                        currentSate = "request_receiver"
                        btn_add_friend.text = "Accept friend request"
                        btn_decline_friend_request.visibility = View.VISIBLE
                        btn_decline_friend_request.setOnClickListener {
                            CancelFriendRequest("new", "Add Friend")
                        }
                    }

                } else {
                    contactRef!!.child(senderUserId)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {

                            }

                            override fun onDataChange(p0: DataSnapshot?) {
                                if (p0!!.hasChild(receiverUserId)) {
                                    currentSate = "friend"
                                    btn_add_friend.text = "Delete Friend"
                                } else {
                                    currentSate = "new"
                                }
                            }

                        })
                }
            }

        })

        if (senderUserId == receiverUserId) {
            btn_add_friend.visibility = View.GONE

        } else {
            btn_add_friend.setOnClickListener {
                if (currentSate == "new") {
                    SendFriendRequest()

                }
                if (currentSate == "request_sent") {
                    CancelFriendRequest("new", "Add Friend")

                }
                if (currentSate == "request_receiver") {
                    AppectFriendRequest()
                }
            }
        }

    }

    private fun AppectFriendRequest() {
        contactRef!!.child(senderUserId).child(receiverUserId).child("Contact").setValue("Saved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    contactRef!!.child(receiverUserId).child(senderUserId).child("Contact")
                        .setValue("Saved").addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            CancelFriendRequest("friend", "Delete Friend")
                            btn_decline_friend_request.visibility = View.GONE
                        }
                    }
                }
            }
    }

    private fun CancelFriendRequest(s: String, sBtn: String) {
        friendRequestRef!!.child(senderUserId).child(receiverUserId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    friendRequestRef!!.child(receiverUserId).child(senderUserId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                currentSate = s
                                btn_add_friend.text = sBtn
                            }
                        }
                }
            }
    }

    private fun SendFriendRequest() {
        friendRequestRef!!.child(senderUserId).child(receiverUserId).child("request_type")
            .setValue("sent").addOnCompleteListener { task ->
            if (task.isSuccessful) {
                friendRequestRef!!.child(receiverUserId).child(senderUserId).child("request_type")
                    .setValue("received").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentSate = "request_sent"
                        btn_add_friend.text = "Cancel friend request"
                        Toast.makeText(this, "yeu cau da duoc gui", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
