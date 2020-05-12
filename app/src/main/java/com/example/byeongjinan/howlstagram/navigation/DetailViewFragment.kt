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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

// 8장에서 만듬
class DetailViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null // db 접근하기 위해 변수 선언
    var uid : String? = null // uid 받아 오는 부분을 글로벌 변수로 사용 (공통으로) 9장 추가
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance() // db 정보 얻기 초기화
//        9장에서 만든 uid 전역 변수화
        uid = FirebaseAuth.getInstance().currentUser?.uid

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

                    // 사인 아웃시 파이어 베이스의 스냅샷에서 에러가 발생하여 앱이 크러쉬(강제종료)되는데 이를 방지하기 위해 넣음(11)
                    // Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

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

            // This code is when the button is clicked (좋아요 버튼 누를 시 처리)
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }
            // This code is when the page is loaded 클릭 이벤트로만 끝나는 것이 아닌 좋아요 카운트와 하트가 색칠되거나 비게 되거나 이벤트를 구현
            if(contentDTOs!![position].favorites.containsKey(uid)){
                // This is like status 좋아요 누른 상태 (꽉찬 하트)
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)

            }else{
                // This is unlike status 좋아요 안 누른 상태(빈 하트)
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)

            }
//            // User ProfileImage 이미지이므로 다운로드 받아야함(글라이드 사용) 8장 기준으론 아직 로드할 부분(프로파일 이미지 담아 있는 부분)이 없으므로 유지
//            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageURL)
//                .into(viewholder.detailviewitem_profile_image)

            // This code is when the profile image is clicked 프로필 이미지 클릭 됐을 때 처리하기(상대방 정보 페이지로 넘어가도록) 11장
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[position].uid)
                bundle.putString("UserId",contentDTOs[position].userID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }


        }
        // 9장 좋아요 버튼 만들고 이벤트 처리하기
        fun favoriteEvent(position : Int){
            // 내가 선택한 컨텐츠의 uid를 불러와서 보여주는 이벤트에 사용할 변수
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            // 데이터를 입력하기 위해선 트랜젝션을 불러와야함
            firestore?.runTransaction { transaction ->
                // 트랜젝션을 하기 위해 먼저 uid 값을 불러오기 -> 전역변수로 옮김
//                var uid = FirebaseAuth.getInstance().currentUser?.uid
                // 트랙젝션의 데이터를 contentDTO 모델로 캐스팅
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                // if 문으로 좋아요 버튼을 이미 클릭 했을 경우와 안했을 경우로 나눠서 처리
                if(contentDTO!!.favorites.containsKey(uid)){
                    // 누른 경우 When the button is clicked 버튼이 눌려있는 걸 취소하는 기능
                    contentDTO?.favoriteCount=contentDTO?.favoriteCount -1
                    contentDTO?.favorites.remove(uid)

                }else{
                    // 안누른 경우 When the button is not clicked 클릭 되는 기능(좋아요 추가 되는 기능)추가
                    contentDTO?.favoriteCount=contentDTO?.favoriteCount+1
                    contentDTO?.favorites[uid!!] = true
                }
                // 트랜젝션한 결과를 서버에 전송
                transaction.set(tsDoc,contentDTO)
            }
        }
    }
}