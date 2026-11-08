package it.fra.myunical.domain.model.auth

data class UserCredentials(val username: String, val password: String) {

    /** Credentials as sent to Esse3, see [sanitizeUsername] and [sanitizePassword]. */
    fun sanitized() = UserCredentials(sanitizeUsername(username), sanitizePassword(password))

    companion object {
        const val FISCAL_CODE_LENGTH = 16

        /**
         * The username is the fiscal code: letters and digits in upper case. Spaces and dashes come
         * from pasting or autofill, and a `:` would break the Basic authentication header.
         */
        fun sanitizeUsername(value: String): String =
            value.filter(Char::isLetterOrDigit).uppercase()

        /**
         * Passwords are kept as typed, spaces included: only line breaks and other control characters,
         * which a pasted value may carry and nobody can type, are dropped.
         */
        fun sanitizePassword(value: String): String = value.filterNot(Char::isISOControl)
    }
}
