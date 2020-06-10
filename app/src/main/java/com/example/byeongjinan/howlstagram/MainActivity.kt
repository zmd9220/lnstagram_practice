package com.example.byeongjinan.howlstagram

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.byeongjinan.howlstagram.navigation.*
import com.example.byeongjinan.howlstagram.navigation.util.FcmPush
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)

        // 6장 사진 업로드 할때 필요한 권한인 파일을 읽어올 수 있는 권한 얻기
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

        // 8장 메인 화면을 띄우면 DetailViewFragment가 맨 먼저 나올 수 있도록(메인 화면 디폴트) 설정(Default)
        bottom_navigation.selectedItemId = R.id.action_home
        registerPushToken()
    }

    // test code
//    override fun onStop() {
//        super.onStop()
//        FcmPush.instance.sendMessage("kZQI7vX8SNX04hDTemTfzhc5snf2","hi","bye")
//    }


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        // 디폴트 값이 기본 아이템 리스트에서 사용하도록 위에 세팅 11
        setToolbarDefault()
        when(p0.itemId){
            R.id.action_home->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_search->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }
            R.id.action_add_photo->{
                // 먼저 외부 저장소를 가져오기(읽기) 권한을 가지고 있는지 확인하고 권한이 있으면 AddPhotoActivity로 넘어가기
                if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.action_favorite_alarm->{
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                return true
            }
            R.id.action_account->{
                var userFragment = UserFragment()
                // 10장 유저 프래그먼트에 uid 값을 넘기는 코드 추가(uid 인자값 받아야하므로)
                var bundle = Bundle()
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("destinationUid",uid)
                userFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                return true
            }
        }
        return false
    }
    // toolbar_username, toolbar_btn, toolbar_back 아이콘이나 뷰들을 기본적으로 보이지 않는 상태로 두기(필요한 곳에서만 등장)
    fun setToolbarDefault(){
        toolbar_username.visibility = View.GONE
        toolbar_btn_back.visibility = View.GONE
        toolbar_title_image.visibility = View.VISIBLE
    }

    // 개별로 푸쉬를 보내기 위한(특정 유저를 나누기 위한) 사용자 별 토큰을 생성 17
    fun registerPushToken()
    {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            task ->
            val token = task.result?.token
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val map = mutableMapOf<String,Any>()
            map["pushToken"] = token!!

            // db 에다가 해당 uid 의 토큰을 저장
            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
        }
    }

    // UserFragment에서 가져온 값대로 실행 (forResult) 12
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            // 사진을 선택 했을 경우 처리해 주기
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            // 파이어 베이스 저장소에 유저 프로필 이미지 폴더 생성후 그 안에 uid 이름으로 저장하도록(이미지를)
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            // continueWithTask로 이미지 다운로드 주소를 얻고
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                // 주소 얻는데 성공했으면
                var map = HashMap<String,Any>() //
                map["image"] = uri.toString() // 키값을 넣고
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
                // db에 넣는 절차
            }

        }
    }
}
