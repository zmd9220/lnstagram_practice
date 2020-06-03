package com.example.byeongjinan.howlstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.AlarmDTO
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.* // 제대로 import 되었는지 확인


// 14 커멘트 액티비티 클래스
class CommentActivity : AppCompatActivity() {

//    전역변수 추가 15
    var destinationUid : String? = null
    var contentUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        // contentUid 라는 인자의(키값으로) 값을 지난 페이지에서 넘겨받아옴
        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        // 리사이클러뷰와 어댑터 연결 (클래스 완성후)
        comment_recyclerview.adapter = CommentRecyclerviewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        // 전송 버튼 이벤트 처리 14
        comment_btn_send?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_edit_message.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            // 커멘트 닫은 부분에 알람이벤트 추가 15
            commentAlarm(destinationUid!!,comment_edit_message.text.toString())
            comment_edit_message.setText("")
        }
    }
    // 코멘트 알람을 알려주는 이벤트 15
    fun commentAlarm(destinationUid : String, message : String)
    {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid =destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.kind = 1
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var comments : ArrayList<ContentDTO.Comment> = arrayListOf() // 기본 초기화
        // 초기화 함수로 파이어베이스에서 데이터 가져오기
        init {
             FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").orderBy("timestamp"
             ).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                 comments.clear()
                 if(querySnapshot == null)return@addSnapshotListener // 안정성

                 for(snapshot in querySnapshot.documents!!){
                     comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                 }
                 notifyDataSetChanged() // 값 체인지 된거 새로고침
             }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // 리사이클러 뷰에 쓸 아이템 레이아웃 불러오기 (item_comment)
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 서버에서 넘어온 메세지와 아이디를 매핑해주기
            var view = holder.itemView
            view.commentviewitem_textview_comment.text = comments[position].comment
            view.commentviewitem_textview_profile.text = comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!) // 커멘트를 단 프로필 이미지 주소를 가져옴
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                    }
                }
        }

    }
}
