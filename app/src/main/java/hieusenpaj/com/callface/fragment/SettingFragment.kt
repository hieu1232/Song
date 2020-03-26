package hieusenpaj.com.callface.fragment


import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import hieusenpaj.com.callface.R
import kotlinx.android.synthetic.main.fragment_setting.*
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class SettingFragment : Fragment() {
    var uri: Uri? = null
    var userProfileImRef: StorageReference? = null
    var downloadUrl: String? = null
    var userRef: DatabaseReference? = null
    var process :ProgressDialog?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_setting, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userProfileImRef = FirebaseStorage.getInstance().reference.child("Profile Images")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        process = ProgressDialog(context)
        setUp()
        iv_profile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
        btn_save.setOnClickListener {
            saveUserData()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            uri = data.data
            iv_profile.setImageURI(uri)
        }
    }


    private fun saveUserData() {
        val userName = ed_profile.text.toString()
        val userStatus = ed_status.text.toString()

        if (uri == null) {
            userRef!!.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    saveInfoOnlyWithoutImage()
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.child(FirebaseAuth.getInstance().currentUser!!.uid).hasChild("image")) {

                    } else {
                        Toast.makeText(context, "chon anh dau tien", Toast.LENGTH_SHORT).show()
                    }
                }

            })

        } else if (userName == "") {
            Toast.makeText(context, "name trong", Toast.LENGTH_SHORT).show()
        } else if (userStatus == "") {
            Toast.makeText(context, "status trong", Toast.LENGTH_SHORT).show()
        } else {
            process!!.setTitle("Cho ty...")
            process!!.show()
            val filePath = userProfileImRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            val uploadTask = filePath.putFile(uri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                downloadUrl = filePath.downloadUrl.toString()
                return@Continuation filePath.downloadUrl

            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUrl = task.result.toString()
                    var profileMap: HashMap<String, Any> = HashMap()
                    profileMap.put("uid", FirebaseAuth.getInstance().currentUser!!.uid)
                    profileMap.put("name", userName)
                    profileMap.put("status", userStatus)
                    profileMap.put("image", downloadUrl!!)

                    userRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .updateChildren(profileMap).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                process!!.dismiss()
                                Toast.makeText(context, "upload thanh cong", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else { // Handle failures
                    // ...
                }
            }
        }
    }
    private fun saveInfoOnlyWithoutImage(){
        val userName = ed_profile.text.toString()
        val userStatus = ed_status.text.toString()
        if (userName == "") {
            Toast.makeText(context, "name trong", Toast.LENGTH_SHORT).show()
        } else if (userStatus == "") {
            Toast.makeText(context, "status trong", Toast.LENGTH_SHORT).show()
        } else {
            process!!.setTitle("Cho ty...")
            process!!.show()
                    var profileMap: HashMap<String, Any> = HashMap()
                    profileMap.put("uid", FirebaseAuth.getInstance().currentUser!!.uid)
                    profileMap.put("name", userName)
                    profileMap.put("status", userStatus)
                    profileMap.put("image", downloadUrl!!)

                    userRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .updateChildren(profileMap).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                process!!.dismiss()
                                Toast.makeText(context, "upload thanh cong", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }


    }
    private fun setUp(){
        userRef!!.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if(p0!!.exists()){
                        val name = p0.child("name").value.toString()
                        val status = p0.child("status").value.toString()
                        val image = p0.child("image").value.toString()

                        ed_profile.text = Editable.Factory.getInstance().newEditable(name)
                        ed_status.text = Editable.Factory.getInstance().newEditable(status)
                        Glide.with(context)
                            .load(image)
                            .into(iv_profile)

                    }
                }

            })
    }

}
