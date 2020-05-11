package com.example.byeongjinan.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.fragment_user.view.*

// 10장
class UserFragment : Fragment() {
    // 프래그먼트에서 사용할 변수들을 전역변수로 선언
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        // uid 값 세팅 스트링으로 해서 이전 화면에서 넘어온 값을 받도록 (직전 화면-메인 화면 에 있던 uid 정보를 받아와서)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance() // 초기화
        auth = FirebaseAuth.getInstance()

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3) // 한 행에 3개 씩 뜰 수 있도록 그리드형식(격자무늬)
        return fragmentView
    }
    // 리사이클러 뷰가 사용할 어댑터
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        // 컨텐츠 담을 변수 배열
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            // 생성자 DB에서 데이터 꺼내옴
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                // Sometimes, This code return null of querySnapshot when it signout 혹시 모르는 경우를 대비해 예외처리)
                if (querySnapshot == null) return@addSnapshotListener

                // Get data
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!) // null safety 없앰
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged() // 리사이클러뷰 데이터 새로고침 되도록
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels/3 // 폭의 1/3
            var imageview = ImageView(parent.context) // 여기에 폭을 넣으면 폭의 1/3이 된 정사각형 이미지뷰가 만들어짐
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)
        }

        // 넘어온 이미지뷰를 리사이클러뷰 뷰홀더에 인자로 넘겨줌
        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 데이터 맵핑
            var imageview = (holder as CustomViewHolder).imageview
            // 이미지 어떤 타입으로 받을지 = requestOptions, 이미지 중앙으로 받기 centerCrop
            Glide.with(holder.imageview.context).load(contentDTOs[position].imageURL).apply(RequestOptions().centerCrop()).into(imageview)
        }

    }
}