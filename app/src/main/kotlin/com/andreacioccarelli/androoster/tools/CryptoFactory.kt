package com.andreacioccarelli.androoster.tools

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object CryptoFactory {

    fun sha256(str: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
            val messageDigest = md.digest(str.toByteArray())
            val number = BigInteger(1, messageDigest)
            return number.toString(16)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("Cannot encrypt using SHA-256. ${e.stackTrace}")
        }

    }

    fun sha1(str: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-1")
            val messageDigest = md.digest(str.toByteArray())
            val number = BigInteger(1, messageDigest)
            return number.toString(16)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("Cannot encrypt using SHA-1. ${e.stackTrace}")
        }

    }

    fun md5(str: String): String {
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("md5")
            val messageDigest = md.digest(str.toByteArray())
            val number = BigInteger(1, messageDigest)
            return number.toString(16)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("Cannot encrypt using md5. ${e.stackTrace}")
        }
    }

    fun serialEncrypt(str: String): String {
        return sha1(md5(str))
    }


}
