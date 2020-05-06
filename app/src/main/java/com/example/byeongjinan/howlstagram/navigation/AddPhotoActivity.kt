package com.example.byeongjinan.howlstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
// 위치 스캔 코드생성 (시스템 내부 확인) + storage라는 변수(저장소), 이미지사진 url를 담을 수 있는 변수 생성
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    // 유저 정보를 가져올 수 있도록 하는 변수 7장추가
    var auth : FirebaseAuth? = null
    // 데이터 매스를 사용할 수 있도록 firestore 변수에 정보 가져오기 7장추가
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initiate storage, auth, firestore 정보 가져옴 7장추가(2줄)
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //Open the album 이 액티비티를 실행하자마자 바로 화면을 오픈해주는 코드
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)

        //add image upload event 버튼에다 이벤트 넣어주기
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }
// 선택한 이미지를 받는 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK)  {
                //결과 값이 사진을 선택 했을 때
                //이미지의 경로가 이쪽으로 넘어옴 This is path to the selected image
                photoUri = data?.data //데이터의 경로를 담기
                addphoto_image.setImageURI(photoUri) //이미지 뷰에다가 선택한 이미지 표시
            }else{
                //Exit the addPhotoActivity if you leave the album without selecting it 취소 버튼나 잘못 클릭 등으로 나갈 때
                finish() // 액티비티 닫기
            }
        }
    }
    fun contentUpload(){
        //Make filename 파일이름을 만들어 주는 코드
        var timestamp =  SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE" + timestamp + "_.png" // 중복생성 x(시간으로)

        var storageRef = storage?.reference?.child("images")?.child(imageFileName) //폴더명


        //FileUpload
        // 성공시 토스트(화면에 성공탭 잠깐 떴다 사라지는 거) !!-null satefy제거? (6장)
//        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//            Toast.makeText(this,getString(R.string.upload_success),Toast.LENGTH_LONG).show()
//        }
        // 데이터 베이스를 입력해줄 코드를 넣어주기 Callback method (7장)
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            // 이미지 업로드 완료 됐으면 이미지 다운로드 주소를 받아오는 코드
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // 주소를 받아오자 마자 데이터 모델을 만들기
                var contentDTO = ContentDTO()

                // Insert downloadURI of image
                contentDTO.imageURL = uri.toString()

                // Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                // Insert userID
                contentDTO.userID = auth?.currentUser?.email

                // Insert explain of content 내용 설명 사용자 입력글과 설명글이 들어감
                contentDTO.explain = addphoto_edit_explain.text.toString()

                // Insert timestamp 밀리세컨드
                contentDTO.timestamp = System.currentTimeMillis()
            }
        }
    }

}
