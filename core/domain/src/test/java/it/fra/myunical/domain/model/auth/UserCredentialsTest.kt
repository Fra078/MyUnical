package it.fra.myunical.domain.model.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class UserCredentialsTest {

    @Test
    fun `username keeps only letters and digits in upper case`() {
        assertEquals("RSSMRA00A01H501U", UserCredentials.sanitizeUsername(" rssmra 00a01-h501u\n"))
        assertEquals("RSSMRA", UserCredentials.sanitizeUsername("rss:mra"))
    }

    @Test
    fun `password keeps spaces and drops control characters`() {
        assertEquals(" pa ss ", UserCredentials.sanitizePassword(" pa ss \n"))
        assertEquals("pàss€", UserCredentials.sanitizePassword("pàss€\t\r"))
    }
}
