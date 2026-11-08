package it.fra.myunical.datastore.sources

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import it.fra.myunical.datastore.security.CredentialCipher
import it.fra.myunical.domain.model.auth.UserCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AuthLocalDataSourceImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Reversible and recognisable; "broken" values cannot be decrypted, like a key lost in a restore. */
    private val cipher = object : CredentialCipher {
        override fun encrypt(plain: String) = "enc:" + plain.reversed()
        override fun decrypt(encrypted: String) = encrypted.removePrefix("enc:").takeIf { encrypted.startsWith("enc:") }?.reversed()
    }

    private val file by lazy { folder.newFile("auth.preferences_pb").also { it.delete() } }

    private fun dataStore() = PreferenceDataStoreFactory.create(
        migrations = listOf(AuthLocalDataSourceImpl.PlaintextMigration(cipher)),
        scope = scope,
        produceFile = { file }
    )

    @After
    fun tearDown() = scope.cancel()

    @Test
    fun `stores nothing readable and reads the credentials back`() = runBlocking {
        val source = AuthLocalDataSourceImpl(dataStore(), cipher)
        source.saveCredentials(UserCredentials("RSSMRA00A01H501U", "s3cret pass"))

        assertEquals(UserCredentials("RSSMRA00A01H501U", "s3cret pass"), source.getCredentials())
        assertEquals("RSSMRA00A01H501U", source.observeUsername().first())
        val onDisk = file.readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(onDisk.contains("RSSMRA00A01H501U"))
        assertFalse(onDisk.contains("s3cret"))
    }

    @Test
    fun `encrypts the plain text credentials of older versions`() = runBlocking {
        // Written without migrations, as the old version did; one DataStore per file at a time
        val oldScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        PreferenceDataStoreFactory.create(scope = oldScope, produceFile = { file }).edit {
            it[stringPreferencesKey("username")] = "RSSMRA00A01H501U"
            it[stringPreferencesKey("password")] = "old"
        }
        oldScope.coroutineContext[Job]!!.cancelAndJoin()
        val source = AuthLocalDataSourceImpl(dataStore(), cipher)

        assertEquals(UserCredentials("RSSMRA00A01H501U", "old"), source.getCredentials())
        assertFalse(file.readBytes().toString(Charsets.ISO_8859_1).contains("RSSMRA00A01H501U"))
    }

    @Test
    fun `values that cannot be decrypted read as signed out`() = runBlocking {
        val dataStore = dataStore()
        dataStore.edit {
            it[stringPreferencesKey("username_encrypted")] = "garbage"
            it[stringPreferencesKey("password_encrypted")] = "garbage"
        }
        val source = AuthLocalDataSourceImpl(dataStore, cipher)

        assertNull(source.getCredentials())
        assertNull(source.observeUsername().first())
    }
}
