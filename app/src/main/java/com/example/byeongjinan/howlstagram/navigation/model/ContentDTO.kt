package com.example.byeongjinan.howlstagram.navigation.model

// 사진을 체계적으로 데이터로써 관리할 컨텐츠 데이터 모델 만들기
// 컨텐츠의 설명을 관리하는 explain 변수 이미지 주소를 관리하는 imageURL 변수 어느 아이디가 올렸는지 관리할 uid 변수 올린 유저의 이미지를 관리할 userID
// 몇시 몇분에 올렸는지 확인할 timestamp 좋아요를 몇개 눌렸는지 알려주는 favoriteCount 중복 좋아요를 방지할 수 있도록 좋아요를 누른 유저를 관리할 수 있는 favorites(map 자료구조)
data class ContentDTO(
    var explain: String? = null,
    var imageURL: String? = null,
    var uid: String? = null,
    var userID: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0,
//    좋아요 버튼 만들기 map -> mutableMap으로 (9장)
    var favorites: MutableMap<String, Boolean> = HashMap()
) {
    // 이 게시글에 달린 덧글을 관리하는 클래스 uid=id userID=이메일? comment 댓글 timestamp 올린 시간
    data class Comment(
        var uid: String? = null,
        var userID: String? = null,
        var comment: String? = null,
        var timestamp: Long? = null
    )
}