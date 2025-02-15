package code.name.monkey.retromusic.extensions

import android.widget.EditText

fun isValidEmail(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}

fun isPhoneNumber(input: String): Boolean {
    val phoneRegex = "^(03|05|07|08|09)+([0-9]{8})\\b".toRegex()
    return input.matches(phoneRegex)
}

fun areEditTextsEqual(editText1: EditText, editText2: EditText): Boolean {
    val text1 = editText1.text.toString().trim()
    val text2 = editText2.text.toString().trim()
    return text1 == text2
}