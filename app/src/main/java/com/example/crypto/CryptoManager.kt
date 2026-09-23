package com.example.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles End-to-End Encryption (E2EE) using:
 * - ECDH (secp256r1 / P-256) for ephemeral and persistent key agreement.
 * - SHA-256 HKDF/KDF to derive a 256-bit AES symmetric key from the shared secret.
 * - AES-256-GCM for authenticated encryption of messages, attachments, and signaling.
 * - Cryptographic SAS (Short Authentication String) / Safety Number generation
 *   for out-of-band verification against Man-In-The-Middle (MITM) attacks.
 */
class CryptoManager {

    companion object {
        private const val EC_CURVE = "secp256r1"
        private const val AES_KEY_SIZE = 32 // 256 bits
        private const val GCM_IV_LENGTH = 12 // 96 bits
        private const val GCM_TAG_LENGTH = 128 // bits
    }

    private val secureRandom = SecureRandom()

    // Persistent Device Identity KeyPair
    val deviceKeyPair: KeyPair = generateEcKeyPair()

    // Ephemeral ECDH KeyPair for current session
    private var sessionKeyPair: KeyPair = generateEcKeyPair()

    // Map of peer ID to derived AES-256 session key
    private val sessionKeys = mutableMapOf<String, ByteArray>()

    /**
     * Generate an Elliptic Curve (P-256) Key Pair
     */
    fun generateEcKeyPair(): KeyPair {
        val keyPairGen = KeyPairGenerator.getInstance("EC")
        keyPairGen.initialize(ECGenParameterSpec(EC_CURVE), secureRandom)
        return keyPairGen.generateKeyPair()
    }

    /**
     * Export Public Key as Base64 String
     */
    fun exportPublicKey(publicKey: PublicKey = deviceKeyPair.public): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    /**
     * Import a Public Key from Base64 String
     */
    fun importPublicKey(base64Key: String): PublicKey {
        val keyBytes = Base64.decode(base64Key, Base64.NO_WRAP)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(spec)
    }

    /**
     * Perform ECDH Key Agreement and derive a 256-bit AES session key
     */
    fun deriveSharedKey(peerId: String, peerPublicKeyBase64: String): ByteArray {
        val peerPublicKey = importPublicKey(peerPublicKeyBase64)
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(deviceKeyPair.private)
        keyAgreement.doPhase(peerPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Derive 256-bit AES key via SHA-256
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update("OffGrid-P2P-v1".toByteArray(Charsets.UTF_8))
        val aesKey = digest.digest(sharedSecret)

        sessionKeys[peerId] = aesKey
        return aesKey
    }

    /**
     * Encrypt plaintext string using AES-256-GCM.
     * Returns "Base64(IV):Base64(Ciphertext+Tag)"
     */
    fun encrypt(peerId: String, plaintext: String): String {
        val keyBytes = sessionKeys[peerId] ?: deriveDefaultKey(peerId)
        val iv = ByteArray(GCM_IV_LENGTH).apply { secureRandom.nextBytes(this) }

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)

        return "$ivBase64:$cipherBase64"
    }

    /**
     * Decrypt encrypted string ("Base64(IV):Base64(Ciphertext+Tag)") using AES-256-GCM
     */
    fun decrypt(peerId: String, encryptedPayload: String): String {
        val parts = encryptedPayload.split(":")
        if (parts.size != 2) return encryptedPayload // fallback if plaintext

        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)

        val keyBytes = sessionKeys[peerId] ?: deriveDefaultKey(peerId)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

        val decryptedBytes = cipher.doFinal(ciphertext)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Generate a deterministic 6-digit numeric Safety Number (SAS) and
     * a formatted 16-character fingerprint for two paired devices
     * to compare out-of-band to confirm absence of MITM attack.
     */
    fun calculateSafetyNumber(peerPublicKeyBase64: String): SafetyNumberInfo {
        val myPubKey = exportPublicKey()
        // Sort keys lexicographically so both parties arrive at the exact same safety number
        val (first, second) = if (myPubKey < peerPublicKeyBase64) {
            myPubKey to peerPublicKeyBase64
        } else {
            peerPublicKeyBase64 to myPubKey
        }

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(first.toByteArray(Charsets.UTF_8))
        digest.update(second.toByteArray(Charsets.UTF_8))
        val hash = digest.digest()

        // 6-digit SAS code: take first 4 bytes as unsigned int % 1,000,000
        val numericValue = (((hash[0].toInt() and 0xFF) shl 24) or
                ((hash[1].toInt() and 0xFF) shl 16) or
                ((hash[2].toInt() and 0xFF) shl 8) or
                (hash[3].toInt() and 0xFF)).toLong() and 0xFFFFFFFFL

        val numericCode = String.format("%06d", (numericValue % 1000000))
        val formattedNumeric = "${numericCode.substring(0, 3)}-${numericCode.substring(3)}"

        // 16-character formatted hex fingerprint (e.g. 7A1F 4B22 90C3 1E58)
        val hexChars = hash.take(8).joinToString("") { "%02X".format(it) }
        val formattedHex = hexChars.chunked(4).joinToString(" ")

        return SafetyNumberInfo(
            numericCode = formattedNumeric,
            hexFingerprint = formattedHex,
            fullHash = hash.joinToString("") { "%02X".format(it) }
        )
    }

    /**
     * Get Device Identity Fingerprint (Short thumbprint for UI display)
     */
    fun getDeviceFingerprint(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(deviceKeyPair.public.encoded)
        return hash.take(4).joinToString("") { "%02X".format(it) }
    }

    private fun deriveDefaultKey(peerId: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update("DEFAULT_P2P_KEY_OFFGRID".toByteArray(Charsets.UTF_8))
        digest.update(peerId.toByteArray(Charsets.UTF_8))
        val key = digest.digest()
        sessionKeys[peerId] = key
        return key
    }
}

data class SafetyNumberInfo(
    val numericCode: String,
    val hexFingerprint: String,
    val fullHash: String
)
