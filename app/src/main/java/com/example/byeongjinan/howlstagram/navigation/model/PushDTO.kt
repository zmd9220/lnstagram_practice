package com.example.byeongjinan.howlstagram.navigation.model


data class PushDTO(
    // to 푸쉬 받을 사람의 tokenID, notification 보낼 내용
    var to : String? = null,
    var notification : Notification = Notification()
){
    data class Notification(
        // body 내용 title 제목 구글 FCM 서버는 이 양식으로 보내야지 푸쉬라고 이해
        var body : String? = null,
        var title : String? = null
    )

}