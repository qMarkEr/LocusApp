package com.marker.locus

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.compose.runtime.snapshots.SnapshotStateMap
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import javax.crypto.spec.SecretKeySpec


@OptIn(ExperimentalEncodingApi::class)
class CryptoManager {
    companion object {
        var sharedPref: SharedPreferences? = null
        fun loadKey(doc : String) {
            val str = sharedPref?.getString(doc, "")?.let { Base64.decode(it) }
            if (str != null) {
                sharedSecret[doc] = SecretKeySpec(str, 0, str.size, "AES")
            }
        }
        fun addKey(doc : String, key : String) {
            sharedPref?.edit()?.putString(doc, key)?.apply()
        }

        @SuppressLint("CommitPrefEdits")
        fun deleteKey(doc : String) {
            sharedPref?.edit()?.remove(doc)?.apply()
        }
        fun aesEncrypt(data: ByteArray, doc : String): String {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivParameterSpec = IvParameterSpec(ByteArray(16)) // Use a secure IV in production
            cipher.init(Cipher.ENCRYPT_MODE, sharedSecret[doc], ivParameterSpec)
            return Base64.encode(cipher.doFinal(data))
        }
        fun aesDecrypt(encryptedData: ByteArray, doc : String): ByteArray {
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val ivParameterSpec = IvParameterSpec(ByteArray(16)) // Use the same IV as used in encryption
            cipher.init(Cipher.DECRYPT_MODE, sharedSecret[doc], ivParameterSpec)
            return cipher.doFinal(encryptedData)
        }
        var sharedSecret : SnapshotStateMap<String, SecretKey> = SnapshotStateMap()
    }
}