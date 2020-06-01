package com.example.byeongjinan.howlstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*


// 14 커멘트 액티비티 클래스
class CommentActivity : AppCompatActivity() {

    var contentUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        // contentUid 라는 인자의(키값으로) 값을 지난 페이지에서 넘겨받아옴
        contentUid = intent.getStringExtra("contentUid")

        // 전송 버튼 이벤트 처리 14
        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            comment_edit_message.setText("")
        }
    }
}
