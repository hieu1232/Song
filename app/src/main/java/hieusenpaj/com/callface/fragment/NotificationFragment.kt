package hieusenpaj.com.callface.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import hieusenpaj.com.callface.R
import hieusenpaj.com.callface.`object`.Contacts
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.item_notification.view.*

/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : Fragment() {
    var mAuth: FirebaseAuth? = null
    var currentUserId: String? = null
    var receiverUserId: String? = null
    var friendRequestRef: DatabaseReference? = null
    var contactRef: DatabaseReference? = null
    var userRef: DatabaseReference? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid
        friendRequestRef = FirebaseDatabase.getInstance().reference.child("Friend Request")
        contactRef = FirebaseDatabase.getInstance().reference.child("Contact")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.layoutManager = LinearLayoutManager(context)

        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(friendRequestRef!!.child(currentUserId), Contacts::class.java)
            .build()
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
                val viewHolder = ViewHolder(v)
                return viewHolder

            }

            override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: Contacts) {
                val listUser = getRef(p1).key
                val requestRef = getRef(p1).child("request_type").ref
                requestRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot!!.exists()) {
                            val type = dataSnapshot.value.toString()
                            if (type == "sent") {
                                p0.card.visibility = View.GONE
                            } else {
                                p0.card.visibility = View.VISIBLE
                                userRef!!.child(listUser)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError?) {

                                        }

                                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                            if (dataSnapshot!!.hasChild("image")) {
                                                val image =
                                                    dataSnapshot.child("image").value.toString()
                                                Glide.with(context)
                                                    .load(image)
                                                    .into(p0.iv)

                                            }
                                            val name =
                                                dataSnapshot.child("name").value.toString()
                                            p0.tv.text = name
                                            p0.btnAppect.setOnClickListener {
                                                AppectFriendRequest(currentUserId!!, listUser)
                                            }
                                            p0.btnCancel.setOnClickListener {
                                                CancelFriendRequest(currentUserId!!, listUser)
                                            }

                                        }

                                    })
                            }
                        } else {

                        }
                    }

                })

            }

        }
        recycler.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iv = v.iv
        val tv = v.tv
        val btnAppect = v.btn_add_friend
        val btnCancel = v.btn_decline_friend_request
        val card = v.card_view
    }

    private fun AppectFriendRequest(senderUserId: String, receiverUserId: String) {
        contactRef!!.child(senderUserId).child(receiverUserId).child("Contact").setValue("Saved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    contactRef!!.child(receiverUserId).child(senderUserId).child("Contact")
                        .setValue("Saved").addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                CancelFriendRequest("friend", "Delete Friend")

                            }
                        }
                }
            }
    }

    private fun CancelFriendRequest(senderUserId: String, receiverUserId: String) {
        friendRequestRef!!.child(senderUserId).child(receiverUserId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    friendRequestRef!!.child(receiverUserId).child(senderUserId).removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                            }
                        }
                }
            }
    }

}
