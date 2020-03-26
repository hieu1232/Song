package hieusenpaj.com.callface.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import hieusenpaj.com.callface.R
import hieusenpaj.com.callface.`object`.Contacts
import kotlinx.android.synthetic.main.activity_find_people.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.item_find_people.view.*
import java.awt.font.TextAttribute

class FindPeopleActivity() : AppCompatActivity() {
    private var str=""
    var userRef : DatabaseReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_people)

        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        recycler.layoutManager = LinearLayoutManager(this)
        ed_search.addTextChangedListener( object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(ed_search.text.toString()  == ""){
                    Toast.makeText(this@FindPeopleActivity,"dien ten de tim kiem",Toast.LENGTH_SHORT).show()

                }else{
                    str = p0.toString()
                    onStart()
                }
            }

        })


    }
    class FindFriendsViewHolder(v:View) : RecyclerView.ViewHolder(v){
        val iv = v.iv
        val tv = v.tv
        val card =v.card_view

    }

    override fun onStart() {
        super.onStart()
        var options: FirebaseRecyclerOptions<Contacts> ?=null
        if(str == ""){
            options = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userRef!!,Contacts::class.java)
                .build()
        }else{
            options = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(userRef!!.orderByChild("name").startAt(str),Contacts::class.java)
                .build()
        }
        val firebaseListAdapter = object :
            FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): FindFriendsViewHolder {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_find_people,parent,false)
                val viewHolder = FindFriendsViewHolder(v)
                return viewHolder

            }

            override fun onBindViewHolder(p0: FindFriendsViewHolder, p1: Int, p2: Contacts) {
                p0.tv.text = p2.name
                Glide.with(applicationContext)
                    .load(p2.image)
                    .into(p0.iv)
                p0.card.setOnClickListener {
                    val intent = Intent(this@FindPeopleActivity,ProfileActivity::class.java)
                    intent .putExtra("id",p2.uid)
                    intent.putExtra("name",p2.name)
                    intent.putExtra("image",p2.image)
                    startActivity(intent)
                }
            }

        }
        recycler.adapter = firebaseListAdapter
        firebaseListAdapter.startListening()
    }
}
