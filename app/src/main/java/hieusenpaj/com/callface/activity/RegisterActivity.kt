package hieusenpaj.com.callface.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import hieusenpaj.com.callface.R
import kotlinx.android.synthetic.main.activity_register.*
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {
    var checker = ""
    var phoneNumber = ""
    var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    var mAuth: FirebaseAuth? = null
    var mVerificationId: String? = null
    var loadingBar: ProgressDialog? = null
    override fun onStart() {
        super.onStart()
        var user  = FirebaseAuth.getInstance().currentUser
        if(user != null){
            val intent = Intent(this,
                MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        loadingBar = ProgressDialog(this)
        ccp.registerCarrierNumberEditText(phoneText)
        setUp()

        continueNextButton.setOnClickListener {
            if (continueNextButton.text == "Submit" || checker == "Code Sent") {
                var code = codeText.text.toString()
                if (code==""){
                    Toast.makeText(this,"chen du code",Toast.LENGTH_SHORT).show()
                }else{
                    loadingBar!!.setTitle("Code verification")
                    loadingBar!!.setCancelable(false)
                    loadingBar!!.show()

                   var credential = PhoneAuthProvider.getCredential(mVerificationId!!,code)
                    signInWithPhoneAuthCredential(credential)
                }

            } else {
                phoneNumber = ccp.fullNumberWithPlus
                if (phoneNumber != "") {

                    loadingBar!!.setTitle("Phone number verification")
                    loadingBar!!.setCancelable(false)
                    loadingBar!!.show()
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS,this,callback!!)
                } else {
                    Toast.makeText(this, "deo on", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setUp() {
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                signInWithPhoneAuthCredential(p0!!)
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Toast.makeText(this@RegisterActivity,"Loi sdt",Toast.LENGTH_SHORT).show()
                phoneAuth.visibility = View.VISIBLE
                continueNextButton.text = "Continue"
                codeText.visibility = View.GONE
                loadingBar!!.dismiss()
        }

            override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(p0, p1)

                mVerificationId = p0
                resendToken = p1
                phoneAuth.visibility = View.GONE
                checker ="Code Sent"
                continueNextButton.text = "Submit"
                codeText.visibility = View.VISIBLE
                loadingBar!!.dismiss()
                Toast.makeText(this@RegisterActivity,"code da duoc gui,check lai",Toast.LENGTH_SHORT).show()

            }

        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("hieu", "signInWithCredential:success")

                    val user = task.result?.user
                    loadingBar!!.dismiss()
                    Toast.makeText(this,"thanh cong",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,
                        MainActivity::class.java)
                    startActivity(intent)

                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("hieu", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }
}
