package hieusenpaj.com.callface.activity

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.opentok.android.*
import hieusenpaj.com.callface.R
import kotlinx.android.synthetic.main.activity_video_chat.*
import pub.devrel.easypermissions.EasyPermissions
import java.util.jar.Manifest

class VideoChatActivity : AppCompatActivity(),Session.SessionListener,PublisherKit.PublisherListener {
    companion object {
        val API_Key = "46514442"
        val SESSION_ID = "1_MX40NjUxNDQ0Mn5-MTU4MTk5MjQ4NjM0OX5TbmExOWFwclh0ekRJeUNjdDFRUktheWZ-fg"
        val TOKEN = "T1==cGFydG5lcl9pZD00NjUxNDQ0MiZzaWc9NjcwZWViYjQ3ZGQyZjUwYzAwMmEzOGMxZTI0YmUyMGQ0ODYxOGMxNzpzZXNzaW9uX2lkPTFfTVg0ME5qVXhORFEwTW41LU1UVTRNVGs1TWpRNE5qTTBPWDVUYm1FeE9XRndjbGgwZWtSSmVVTmpkREZSVWt0aGVXWi1mZyZjcmVhdGVfdGltZT0xNTgxOTkyNTAwJm5vbmNlPTAuNjUyOTMwMTE0ODM0Njc2NCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTg0NTgwODk4JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9"
        val LOG_TAG = VideoChatActivity::class.java.simpleName
        val RC_PER = 1234

    }

    var mAuth: FirebaseAuth? = null
    var userId: String? = null
    var userRef: DatabaseReference? = null
    var session : Session?=null
    var publisher :Publisher?=null
    var subscriber:Subscriber?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

        mAuth = FirebaseAuth.getInstance()
        userId = mAuth!!.currentUser!!.uid
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        iv_cancel_call.setOnClickListener {
            userRef!!.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {


                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.child(userId).hasChild("Ringing")) {
                        userRef!!.child(userId).child("Ringing").removeValue()
                        if(publisher!=null){
                            publisher!!.destroy()
                        }
                        if(subscriber!=null){
                            subscriber!!.destroy()
                        }

                    }
                    if (p0.child(userId).hasChild("Calling")) {
                        if(publisher!=null){
                            publisher!!.destroy()
                        }
                        if(subscriber!=null){
                            subscriber!!.destroy()
                        }
                        userRef!!.child(userId).child("Calling").removeValue()
                    }else{
                        if(publisher!=null){
                            publisher!!.destroy()
                        }
                        if(subscriber!=null){
                            subscriber!!.destroy()
                        }
                    }
                    onBackPressed()
                }

            })
        }

        session = Session.Builder(this, API_Key, SESSION_ID).build()
        session!!.setSessionListener(this)
        session!!.connect(TOKEN)

    }

    //session

    override fun onStreamDropped(p0: Session?, p1: Stream?) {
        if(subscriber!=null){
            subscriber==null
            subsciber_container.removeAllViews()
        }

    }

    override fun onStreamReceived(p0: Session?, p1: Stream?) {
        if(subscriber== null){
            subscriber = Subscriber.Builder(this,p1).build()
            session!!.subscribe(subscriber)
            subsciber_container.addView(subscriber!!.view)
        }
    }

    override fun onConnected(p0: Session?) {
        publisher = Publisher.Builder(this).build()
        publisher!!.setPublisherListener(this)
        publisher_container.addView(publisher!!.view)

        if(publisher!!.view  is GLSurfaceView){
            (publisher!!.view as GLSurfaceView).setZOrderOnTop(true)

        }
        session!!.publish(publisher)
    }

    override fun onDisconnected(p0: Session?) {
    }

    override fun onError(p0: Session?, p1: OpentokError?) {
    }



    //publisher

    override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {



    }

    override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {

    }

    override fun onError(p0: PublisherKit?, p1: OpentokError?) {
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        super.onPointerCaptureChanged(hasCapture)
    }

}


