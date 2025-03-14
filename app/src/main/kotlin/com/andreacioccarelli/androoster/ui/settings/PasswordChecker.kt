package com.andreacioccarelli.androoster.ui.settings

object PasswordChecker {

    fun isWeak(auth_password: String): Boolean {
        when (auth_password.lowercase()) {
            "1234", "4321", "0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999",


            "12345", "54321", "43210", "00000", "11111", "22222", "33333", "44444", "55555", "66666", "77777", "88888", "99999",


            "123456", "654321", "543210", "000000", "111111", "222222", "333333", "444444", "555555", "666666", "777777", "888888", "999999",


            "123456789", "1234567890", "1234567", "123123", "987654321", "123321",


            "password", "qwerty", "qwertyuiop", "asdf", "asdfghjkl", "lmno", "google", "starwars", "monkey", "football", "mynoob", "aaaaaa", "aaaa", "aaaaa" -> return true
        }


        return false
    }
}
