package org.lynx.service

import com.google.crypto.tink.subtle.Base64
import com.google.crypto.tink.subtle.X25519
import com.google.inject.Singleton
import org.jetbrains.exposed.sql.transactions.transaction
import org.lynx.domain.AbonentKey
import org.lynx.domain.AbonentKeys
import org.lynx.domain.Key
import org.lynx.domain.User
import org.lynx.domain.Users
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

interface CryptographyService {
    fun encrypt(data: String, key: ByteArray, iv: ByteArray): ByteArray

    fun decrypt(dataAsBase64: String, key: ByteArray, iv: ByteArray): String

    fun encryptFile(dataAsBase64: String, key: ByteArray, iv: ByteArray): ByteArray

    fun decryptFile(encryptedDataAsBase64: String, key: ByteArray, iv: ByteArray): ByteArray

    fun generateSharedKeyForAbonent(username: String, publicKey: String): ByteArray

    fun getPublicKeyAsBase64(): String

    fun getSharedKeyForAbonent(abonent: String): ByteArray?

    fun generateIv(): ByteArray
}


@Singleton
class CryptographyServiceImpl : CryptographyService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CryptographyServiceImpl::class.java)
        const val CRYPTOGRAPHY_ALGORITHM = "AES/CBC/PKCS5Padding"
    }

    private lateinit var privateKey: ByteArray

    private lateinit var publicKey: ByteArray

    init {
        transaction {
            val phoneKey = Key.findById(1L)
            if (phoneKey == null) {
                generateKey()
                saveGeneratedKey()
            } else {
                generateKeyPairFromPhoneKey(phoneKey)
            }
        }
    }

    private fun generateKey() {
        try {
            privateKey = X25519.generatePrivateKey()
            publicKey = X25519.publicFromPrivate(privateKey)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun saveGeneratedKey() {
        transaction {
            Key.new(1L) {
                publicKey = Base64.encodeToString(this@CryptographyServiceImpl.privateKey, Base64.NO_WRAP)
                privateKey = Base64.encodeToString(this@CryptographyServiceImpl.publicKey, Base64.NO_WRAP)
            }
        }
    }

    private fun generateKeyPairFromPhoneKey(key: Key) {
        publicKey = Base64.decode(key.publicKey, Base64.NO_WRAP)
        privateKey = Base64.decode(key.privateKey, Base64.NO_WRAP)
    }

    override fun encrypt(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        return try {
            val aesKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(CRYPTOGRAPHY_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(iv))
            cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            log.error("Error while encrypting message", e)
            throw e
        }
    }

    override fun decrypt(dataAsBase64: String, key: ByteArray, iv: ByteArray): String {
        return try {
            val aesKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(CRYPTOGRAPHY_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(iv))
            String(
                cipher.doFinal(Base64.decode(dataAsBase64, Base64.NO_WRAP)),
                StandardCharsets.UTF_8
            )
        } catch (e: java.lang.Exception) {
            log.error("Error while decrypting message", e)
            throw e
        }
    }

    override fun encryptFile(dataAsBase64: String, key: ByteArray, iv: ByteArray): ByteArray {
        return try {
            val aesKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(CRYPTOGRAPHY_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, IvParameterSpec(iv))
            cipher.doFinal(dataAsBase64.toByteArray())
        } catch (e: Exception) {
            log.error("Error while encrypting message", e)
            throw e
        }
    }

    override fun decryptFile(
        encryptedDataAsBase64: String,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        return try {
            val aesKey = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance(CRYPTOGRAPHY_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, aesKey, IvParameterSpec(iv))
            cipher.doFinal(Base64.decode(encryptedDataAsBase64, Base64.NO_WRAP))
        } catch (e: Exception) {
            log.error("Error while encrypting message", e)
            throw e
        }
    }

    override fun generateSharedKeyForAbonent(username: String, publicKey: String): ByteArray {
        val serverPublicKey = Base64.decode(publicKey, Base64.NO_WRAP)
        val generatedKey: ByteArray
        try {
            generatedKey = X25519.computeSharedSecret(privateKey, serverPublicKey)
            log.info("Abonent shared key generated, but not saved")
            try {
                transaction {
                    val abonent = User.find { Users.username eq username }.first()
                    AbonentKey.new {
                        abonentId = abonent
                        abonentName = username
                        abonentPublicKey = publicKey
                        sharedKey = Base64.encodeToString(generatedKey, Base64.NO_WRAP)
                        abonentId
                    }
                }
                log.info("Abonent shared key saved")
            } catch (e: Exception) {
                log.error("Failed to save shared key", e)
            }
        } catch (e: Exception) {
            log.error("Cannot generate shared key", e)
            throw e
        }
        return generatedKey
    }

    override fun getPublicKeyAsBase64(): String = Base64.encodeToString(publicKey, Base64.NO_WRAP)

    override fun getSharedKeyForAbonent(abonent: String): ByteArray? {
        var abonentKey: AbonentKey? = null
        transaction {
            abonentKey = AbonentKey.find { AbonentKeys.abonentName eq abonent }.firstOrNull()
        }
        return if (abonentKey == null) {
            null
        } else {
            Base64.decode(abonentKey?.sharedKey, Base64.NO_WRAP)
        }
    }

    override fun generateIv(): ByteArray {
        val randomSecureRandom = SecureRandom.getInstance("SHA1PRNG")
        val iv = ByteArray(16)
        randomSecureRandom.nextBytes(iv)
        return iv
    }
}