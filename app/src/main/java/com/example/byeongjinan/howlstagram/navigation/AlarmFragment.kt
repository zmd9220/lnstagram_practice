package com.example.byeongjinan.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.byeongjinan.howlstagram.R
import com.example.byeongjinan.howlstagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

// 16 알람 리스트 페이지 만들기
class AlarmFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)

        // recyclerView 값을 리프레쉬(새로고침) data set changed 값 넣기
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }
    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
    {

        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()
        //  어뎁터가 생성될 때 db에서 데이터 읽어오기 (생성자)
        init {
            // 현재 유저의 uid를 가져옴
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            // uid가 같은것의 코멘트만 가져옴
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents)
                {
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//           item comment 디자인을 그대로 재활용하여 뷰홀더 생성
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }
        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//            종류에 따라 메세지를 다르게 표시할 수 있도록 코드 넣어주기 16
            var view = holder.itemView
            // switch 문과 유사한 when 문
            when(alarmDTOList[position].kind){
                // 0 = like, 1 = comment, 2 = follow
                0 ->{
                    var str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    view.commentviewitem_textview_profile.text = str_0
                }
                1 ->{
                    // 어떤 유저가 코멘트 달렸는지 of 이후에는 메세지 내용
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    view.commentviewitem_textview_profile.text = str_0
                }
                2 ->{
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    view.commentviewitem_textview_profile.text = str_0
                }
            }
        }

    }
}