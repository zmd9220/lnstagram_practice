package com.example.byeongjinan.howlstagram.navigation

import android.content.Intent
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
import com.example.byeongjinan.howlstagram.LoginActivity
import com.example.byeongjinan.howlstagram.MainActivity
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.fragment_user.view.*
// 유저 프래그먼트로 내 계정에 대한 정보와 상대방의 계정에 대한 정보를 띄울 수 있음
// 10장
class UserFragment : Fragment() {
    // 프래그먼트에서 사용할 변수들을 전역변수로 선언
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    companion object {
        // companion 코틀린에 없는 static 기능의 변수 선언시 대체 수단 12
        var PICK_PROFILE_FROM_ALBUM = 10
    }
    // 어떤 uid 인지 체크해서 내가 아닌 다른 아이디의 경우와 내 아이디 인 경우로 나눠서 처리 (11)
    var currentUserUid : String? = null
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
        currentUserUid = auth?.currentUser?.uid


        if(uid == currentUserUid){
            // 내 아이디 인 경우
            fragmentView?.account_btn_follow_signout?.text= getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                // 로그아웃이므로 액티비티 종료와 로그인 액티비티로 다시 넘어가도록(호출)
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        }else{
            // 다른 아이디
            fragmentView?.account_btn_follow_signout?.text= getString(R.string.follow)
            // 누구의 유저 페이지 인지 보여주는 텍스트뷰, 백버튼 활성화
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener{
                // 뒤로가기
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            // 툴바 이미지 로고를 숨기고
            mainactivity?.toolbar_title_image?.visibility = View.GONE
            // 아이디 표시 해줌(들어온 아이디)
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3) // 한 행에 3개 씩 뜰 수 있도록 그리드형식(격자무늬)

        // 프로필 사진 클릭시 이벤트 처리하는 버튼 이벤트 12
        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage() // 이미지 주소 받아오기
        return fragmentView
    }
    fun getProfileImage(){
        // 올린 프로필 이미지를 다운받는 기능
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
           // 사진이 실시간으로 변환되는 것을 체크하기 위해서 스냅샷을 받아옴
            if(documentSnapshot == null )return@addSnapshotListener // 안정성을 위해 데이터 못받았으면 예외처리
            if(documentSnapshot.data != null){
                // 정상적일 경우 이미지 주소를 받아오기
                var url =  documentSnapshot?.data!!["image"] // image 라는 키에 해당하는 값 즉 이미지 주소
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!) // circleCrop 이미지의 옵션을 원형으로
            }
        }
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