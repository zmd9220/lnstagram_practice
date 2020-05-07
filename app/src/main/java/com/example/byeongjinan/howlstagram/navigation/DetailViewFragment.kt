package com.example.byeongjinan.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

// 8장에서 만듬
class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null // db 접근하기 위해 변수 선언
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance() // db 정보 얻기 초기화

//        클래스 완성이 끝나고 메인 함수에 리사이클러뷰의 설정(클래스에서 지정해놓음)과 레이아웃 매니저(화면을 세로로 배치)를 적용
        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    // 디테일뷰 리사이클러뷰 만들기 8장
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        // 데이터 모델들(아이디, 이메일 등 정보들) 받아 올 수 있는 배열 생성
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {

            // db 접근해서 데이터를 받아올 수 있는 코일? 을 만듬
            // 먼저 db images에 넣어 뒀던 정보들을 시간 순으로 정렬해서 스냅샷을 찍음 그리고 배열을 초기화(기존 있던 내용 말고 다시 받도록 하기 위해)
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    // for문으로 스냅샷에 들어간 내용들을 하나하나 씩 읽어들이기
                    for (snapshot in querySnapshot!!.documents) {
                        //캐스팅하기 DTO방식으로
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    // 값이 새로고침 되도록
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            컨테이너(parent)에 넣어 주기 (item detail 레이아웃 대로)
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            // CustomViewHolder라는 클래스에 담아서 리턴하기
            return CustomViewHolder(view)
        }

        //        null safety 안되도록 왜 복잡하게 커스텀뷰홀더를 만들어서 복잡하게 해야되는지?
//        리사이클러뷰를 사용할 때 메모리를 적게 사용하기 위해서 커스텀뷰홀더 클래스를 만들어 달라는 일종의 약속이므로 문법이랑은 상관없음
        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        //        리사이클러뷰 갯수
        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        //        서버에서 넘어온 데이터들을 맵핑 시켜주는 부분
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //           holder를 커스텀뷰 홀더로 캐스팅
            var viewholder = (holder as CustomViewHolder).itemView
            // UserId 뷰홀더에서 텍스트 뷰 부분에 dto에 있는 유저 아이디를 가져옴 근데 dtos가 배열이므로 position으로 현재 해당하는 배열 값 가져오기
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![position].userID

            // Image 글라이드라는 아까 넣었던 라이브러리를 이용해서 컨텐츠 dto에 있는 이미지 url로 다운로드 받아 viewholder에 해당하는 부분으로 집어넣기
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageURL)
                .into(viewholder.detailviewitem_imageview_content)

            // Explain of content
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![position].explain

            // Likes
            viewholder.detailviewitem_favoritecounter_textview.text =
                "Likes " + contentDTOs!![position].favoriteCount

            // User ProfileImage 이미지이므로 다운로드 받아야함(글라이드 사용) 8장 기준으론 아직 로드할 부분(프로파일 이미지 담아 있는 부분)이 없으므로 유지
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageURL)
                .into(viewholder.detailviewitem_profile_image)
        }
    }
}