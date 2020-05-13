package com.example.byeongjinan.howlstagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.byeongjinan.howlstagram.LoginActivity
import com.example.byeongjinan.howlstagram.MainActivity
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.ContentDTO
import com.example.byeongjinan.howlstagram.navigation.model.FollowDTO
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
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
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
            mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE

            // 팔로우 버튼 클릭시 이벤트 처리 12
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!,3) // 한 행에 3개 씩 뜰 수 있도록 그리드형식(격자무늬)

        // 프로필 사진 클릭시 이벤트 처리하는 버튼 이벤트 12
        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage() // 이미지 주소 받아오기 12
        getFollowerAndFollowing() // 팔로잉 수 등을 불러오기 12

        return fragmentView
    }
    fun getFollowerAndFollowing(){
        // destination 내 페이지를 클릭 했을 때는 내 아이디 상대방 페이지를 클릭 했을 때는 상대방 아이디, 스냅샷으로 값을 실시간으로 불러오기 12
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO =  documentSnapshot.toObject(FollowDTO::class.java) // 스냅샷 데이터를 followDTO 유형으로 캐스팅해서 불러오기
            if (followDTO?.followingCount != null){
                fragmentView?.account_tv_following_count?.text = followDTO?.followingCount?.toString()
            }
            if (followDTO?.followerCount != null){
                fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount?.toString()
                // 팔로우를 이미 하고 있을 경우에는 버튼을 바꾸기(언팔로우 버튼으로)
                if (followDTO?.followers?.containsKey(currentUserUid!!)){
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                    fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(activity!!,R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                }else{
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                    if (uid != currentUserUid) {
                        // 상대방 유저 프래그먼트일 때 백그라운드 컬러를 바꾸도록 - 컬러필터를 날려버림
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                    }
                }
            }
        }

    }

    fun requestFollow(){
        // 크게 두가지 기능 Save data to my account 내가 상대방 누구를 팔로잉 하는지 보여주는 부분 12
        // 내가 누군가를 팔로워로 하는 과정이 담긴 트랜젝션 만들기
        var tsDocFollowing =  firestore?.collection("users")?.document(currentUserUid!!)
        // db에 넣기
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                // 아무도 팔로워 한 적이 없을 경우 (처음)
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] =true // 중복 팔로워 방지

                transaction.set(tsDocFollowing,followDTO) // 데이터가 db에 옮기는 과정
                return@runTransaction
            }
            if (followDTO.followings.containsKey(uid)){
                // 이미 팔로워 한 상태 It remove following third person a third person follow me 팔로우 지우기
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followers?.remove(uid) // 상대방 uid 제거(중복 방지 맵에서)
            }else{
                // 안 한 상태 It add following third person a third person do not follow me 팔로우 추가하기
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followers[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        // Save data to third person 상대방 계정에는 또 다른 타인이 팔로우 하는 부분이 있음??
        // 내가 팔로잉 할 상대방의 계정에 접근하는 코드
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if (followDTO == null){
                // 데이터 모델이 없을 경우 (빔) 생성
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true // 상대방의 계정 맵에 나의 아이디를 넣기

                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            if (followDTO!!.followers.containsKey(currentUserUid)){
                // 이미 팔로워 한 상태 It cancel my follower when I follow a third person 팔로우 지우기
                // 팔로우를 했을 경우 취소하도록
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!) // 나의 uid를 해당 중복 방지 맵에서 지우기
            }else{
                // 안 한 상태 It add my follower when I don't follow a third person 팔로우 추가하기
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }
    fun getProfileImage(){
        // 올린 프로필 이미지를 다운받는 기능 12
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