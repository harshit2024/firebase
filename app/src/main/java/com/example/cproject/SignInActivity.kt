package com.example.cproject



import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SignInActivity : AppCompatActivity() {
    private val RC_SIGN_IN:Int=123
    lateinit var signInButton: SignInButton
    private var progressBar: ProgressBar? = null
    lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient:GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


        auth=FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()



        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        val account = GoogleSignIn.getLastSignedInAccount(this)
      //  updateUI(account)

         signInButton = findViewById<SignInButton>(R.id.google_button)
        progressBar = findViewById<ProgressBar>(R.id.progressbar)
        signInButton.setOnClickListener {
            signIn()
        }
    }


    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Firebase Auth With google" + account.id)
            firebaseAuthWithGoogle(account.idToken)
            // Signed in successfully, show authenticated UI.

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {



               val credential=GoogleAuthProvider.getCredential(idToken,null)

        signInButton.visibility=View.GONE

        progressBar!!.visibility=View.VISIBLE

               GlobalScope.launch(Dispatchers.IO) {
                   val auth=auth.signInWithCredential(credential).await()
                   val firebaseUser=auth.user
                   withContext(Dispatchers.Main){
                       UpdateUI(firebaseUser)
                   }
               }
    }

    private fun UpdateUI(firebaseUser: FirebaseUser?) {

        if(firebaseUser!=null){

            val mainActivityIntent=Intent(this,MainActivity::class.java)
            startActivity(mainActivityIntent)

        }
        else{
            signInButton.visibility=View.VISIBLE

            progressBar!!.visibility=View.GONE

        }
    }
}