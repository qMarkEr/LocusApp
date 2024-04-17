package com.marker.locus
data class PublicLocusInfo(
    var profilePicture : String,
    var userName : String
)

data class PrivateLocusInfo(
    var contacts : List<String>,
    var userName : String
)

class PrivateConvertor() {
    val contacts : List<String> = emptyList()
    val userName : String = ""
}

class PublicConvertor() {
    val profilePicture: String = ""
    val userName: String = ""
}