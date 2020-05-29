package com.example.byeongjinan.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_grid.view.*


// 13장 유저 프레그먼트와 리사이클러뷰의 기본 틀은 같으므로 복사 붙여넣기
class GridFragment : Fragment(){
    var firestore : FirebaseFirestore? = null
    var fragmentView : View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid,container,false)
        firestore = FirebaseFirestore.getInstance()

        // 리사이클러뷰 어댑터와 바로 연결
        fragmentView?.gridfragment_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.gridfragment_recyclerview?.layoutManager = GridLayoutManager(activity,3)
        return fragmentView
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        // 컨텐츠 담을 변수 배열
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            // 생성자 DB에서 데이터 꺼내옴
            firestore?.collection("images")?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                // Sometimes, This code return null of querySnapshot when it signout 혹시 모르는 경우를 대비해 예외처리)
                if (querySnapshot == null) return@addSnapshotListener

                // Get data
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!) // null safety 없앰
                }

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
            Glide.with(holder.imageview.context).load(contentDTOs[position].imageURL).apply(
                RequestOptions().centerCrop()).into(imageview)
        }

    }
}