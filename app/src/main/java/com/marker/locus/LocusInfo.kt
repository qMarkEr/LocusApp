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
    var userName : String,
    var activeContacts : MutableList<String>
)

class PrivateConvertor {
    var activeContacts : List<String> = emptyList()
    val contacts : List<String> = emptyList()
    val userName : String = ""
}

data class KeyExtractor (
    var key : String = ""
)