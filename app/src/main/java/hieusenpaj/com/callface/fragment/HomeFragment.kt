package hieusenpaj.com.callface.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import hieusenpaj.com.callface.R
import hieusenpaj.com.callface.`object`.Contacts
import hieusenpaj.com.callface.activity.CallingActivity
import kotlinx.android.synthetic.main.contact_design.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_notification.*


class HomeFragment : Fragment() {
    var mAuth: FirebaseAuth? = null
    var currentUserId: String? = null
    var contactRef: DatabaseReference? = null
    var userRef: DatabaseReference? = null
    var callBy  = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth!!.currentUser!!.uid
        contactRef = FirebaseDatabase.getInstance().reference.child("Contact")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        return inflater.inflate(R.layout.fragment_home, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_home.layoutManager = LinearLayoutManager(context)
        checkForReceiverCall()

        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(contactRef!!.child(currentUserId), Contacts::class.java)
            .build()
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.contact_design, parent, false)
                return ViewHolder(v)

            }

            override fun onBindViewHolder(p0: ViewHolder, p1: Int, p2: Contacts) {
                val listUser = getRef(p1).key
                userRef!!.child(listUser).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot!!.exists()) {
                            val image =
                                dataSnapshot.child("image").value.toString()
                            Glide.with(context)
                                .load(image)
                                .into(p0.iv)
                            val name =
                                dataSnapshot.child("name").value.toString()
                            p0.tv.text = name


                        }
                    }

                })
                p0.btnCall.setOnClickListener {
                    val intent = Intent(context,CallingActivity::class.java)
                    intent.putExtra("id",listUser)
                    context!!.startActivity(intent)
                }
            }
        }
        recycler_home.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()
    }

    private fun checkForReceiverCall() {
        userRef!!.child(currentUserId).child("Ringing").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                if(p0!!.hasChild("ringing")){
                    callBy = p0.child("ringing").value.toString()
                    val intent = Intent(context,CallingActivity::class.java)
                    intent.putExtra("id",callBy)
                    context!!.startActivity(intent)


                }
            }

        })
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iv = v.iv
        val tv = v.tv
        val btnCall = v.btn_call

    }



}
