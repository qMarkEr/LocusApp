package com.marker.locus
data class PublicLocusInfo(
    var profilePicture : String = "",
    var userName : String = "",
    var receiveToken : String = ""
)

data class ContactLocusInfo(
    var profilePicture : String = "",
    var userName : String = "",
    var publicName : String = "",
    var receiveToken: String = ""
)
data class PrivateLocusInfo(
    var contacts : MutableList<String>,
    var userName : String
)

class PrivateConvertor {
    val contacts : List<String> = emptyList()
    val userName : String = ""
}

data class KeyExtractor (
    var key : String = ""
)