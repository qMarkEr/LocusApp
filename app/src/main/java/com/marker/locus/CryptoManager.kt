package com.marker.locus

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.room.Room
import androidx.room.RoomDatabase


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

@Entity(tableName = "key_store")
data class KeyStore(
    @PrimaryKey(autoGenerate = true)
    val id : Int? = null,
    var doc : String,
    var ss : String
)

@Dao
interface KeysDao {
    @Query("SELECT ss FROM key_store WHERE doc = :givenDoc")
    suspend fun getUserWithName(givenDoc: String) : String

    @Query("SELECT * FROM key_store WHERE doc = :givenDoc")
    suspend fun getWithName(givenDoc: String) : KeyStore
    @Insert
    suspend fun insertItem(ks: KeyStore)

    @Delete
    suspend fun deleteItem(ks: KeyStore)
}

@Database(
    entities = [
        KeyStore::class
    ],
    version = 1
)
abstract class MainDB : RoomDatabase() {
    abstract val dao: KeysDao
    companion object {
        fun createDataBase(context: Context): MainDB {
            return Room.databaseBuilder(
                context,
                MainDB::class.java,
                "key_store.db"
            ).build()
        }
    }
}