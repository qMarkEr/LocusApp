package com.marker.locus

import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.compose.runtime.snapshots.SnapshotStateMap
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
class CryptoManager {
    companion object {
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