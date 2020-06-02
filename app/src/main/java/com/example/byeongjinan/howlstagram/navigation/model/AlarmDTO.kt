package com.example.byeongjinan.howlstagram.navigation.model

// 15 alarm model 만들고 이벤트 처리하는 것 만들기
data class AlarmDTO (
    var destinationUid : String? =null,
    var userId : String? = null,
    var uid : String? = null,
//    어떤 타입의 메세지?
    var kind : Int? = null,
    var message : String? = null,
    var timestamp : Long? = null

)
