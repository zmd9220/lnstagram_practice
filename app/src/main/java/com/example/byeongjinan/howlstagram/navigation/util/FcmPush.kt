package com.example.byeongjinan.howlstagram.navigation.util

import com.example.byeongjinan.howlstagram.navigation.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

// 18 푸쉬를 전송해주는 클래스
class FcmPush {
    // 안드로이드 폰 안에다가 서버 부분을 개발해줌
    var JSON = MediaType.parse("application/json; charset=utf-8") // header 값 생성
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "AAAAex57Uqk:APA91bHehjkH7uux3tIS5RxOoRCj16ajeN0vn1XYTrqI1KIkULe-T3pBUX2NcOPa1G9C7b9l287ysGIZ-eFmigkr-Brsf8LVMTexjlITTdCNuDvLTjql8wIyMV6yP22mdjpO4wao6w-c"
//    var serverKey = "AIzaSyCGyx5FkyV80MyWfG3w9c29CGXOvbENmUA"
    // gson, okhttp 사용
    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null

    // 싱글톤 패턴을 선언해서 이 클래스를 어디서든 손쉽게 사용할 수 있게 만들기
    companion object{
        var instance = FcmPush()
    }
    // 생성자
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    // 푸쉬 메세지 보내주는 함수
    fun sendMessage(destinationUid : String, title :  String, message : String){
        // 상대방의 uid를 이용해서 토큰을 얻어오기
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
            task -> if(task.isSuccessful){
            var token = task?.result?.get("pushToken").toString()

            // 가져온 토큰을 기반으로 push 모델 작성
            var pushDTO = PushDTO()
            pushDTO.to = token
            pushDTO.notification.title = title
            pushDTO.notification.body = message
            // okhttp를 통해서 메세지 전송
            // host에 담을 body 부분
            var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
            var request = Request.Builder()
                .addHeader("Content-Type","application/json") // json 형식의 헤더 생성
                .addHeader("Authorization","key="+serverKey) // 헤더에 api 키 넣기
                .url(url) // 보낼 주소
                .post(body) // 보낼 바디 post 형식으로
                .build() // 닫기

            // 구글 FCM 서버에 이 값을 넘겨주기
            okHttpClient?.newCall(request)?.enqueue(object : Callback{
                override fun onFailure(call: Call?, e: IOException?) {

                }

                override fun onResponse(call: Call?, response: Response?) {
                   println(response?.body()?.string()) // 성공시에만 메세지 나오도록
                }

            })
        }
        }

    }
}