package com.example.byeongjinan.howlstagram

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.Call
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentActivity.*
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null // 로그인시 파이어베이스 인증 되었는지
    var googleSignInClient : GoogleSignInClient? = null // 구글로그인시 사용
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? = null // 페이스북 콜백?
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        // 각각의 버튼 클릭시 로그인 방법들 정리
        email_login_button.setOnClickListener{
            signinAndSignup()
        }
        google_sign_in_button.setOnClickListener {
//             first step
            googleLogin()
        }
        facebook_login_button.setOnClickListener {
            // first step
            facebookLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
//        printHashKey()
        callbackManager = CallbackManager.Factory.create() // 선언
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    fun facebookLogin()
    {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email")) //페북으로 로그인시 제공받을 정보 리스트(기본정보, 이메일주소)
        LoginManager.getInstance()
            .registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    // second step
                    handleFacebookAccessToken(result?.accessToken) // 성공 했을 때 파이어베이스로 페이스북 데이터를 넘기는 부분
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                }

            })//로그인 성공했을 때 넘어오는 부분
    }
    fun handleFacebookAccessToken(token: AccessToken?){
        var credential =  FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential) // 여기는 구글과 같음
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) //응답 값을 받아서 페이지로 이동시키는 부분(3단계)
                {
                    // third step
                    moveMainPage(task.result?.user)
                    //Login page makes
                }
                else
                {
                    //Show the error messages
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode,resultCode,data) // 페북 로그인일 경우 해당 값 데이터 넣어서 처리
        if(requestCode ==  GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                var account = result.signInAccount
                //second step
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful)
                {
                    moveMainPage(task.result?.user)
                    //Login page makes
                }
                else
                {
                    //Show the error messages
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }
    fun signinAndSignup()
    {
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
            ?.addOnCompleteListener {
            task ->
                if(task.isSuccessful)
                {
                    moveMainPage(task.result?.user)
                    //creating a user account
                }
                else if(task.exception?.message.isNullOrEmpty()){
                    //Show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
                else
                {
                    //Login if you have account
                    signinEmail()
                }
        }
    }
    fun signinEmail()
    {
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful)
                {
                    moveMainPage(task.result?.user)
                    //Login page
                }
                else
                {
                    //Show the error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?)
    {
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            // 로그인 액티비티가 꺼지면서 메인 액티비티가 등장하도록 종료 11장
            finish()
        }
    }
}
