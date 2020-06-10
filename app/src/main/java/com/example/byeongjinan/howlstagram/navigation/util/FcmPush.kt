package com.example.byeongjinan.howlstagram.navigation.util

import okhttp3.MediaType

// 18 푸쉬를 전송해주는 클래스
class FcmPush {
    // 안드로이드 폰 안에다가 서버 부분을 개발해줌
    var JSON = MediaType.parse("application/json; charset=utf-8") // header 값 생성
    var url = "https://fcm.googleapis.com/fcm/send"
}