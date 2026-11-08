package it.fra.myunical.ui

/** Pages of the Esse3 web portal; the ones under /auth ask for the web login first. */
object Esse3Links {
    const val HOST = "unical.esse3.cineca.it"
    private const val BASE = "https://$HOST"

    /** True for pages of the Esse3 web portal, the only ones the saved credentials may be sent to. */
    fun isEsse3(url: String): Boolean = url.startsWith("https://$HOST/")

    const val HOME = "$BASE/Home.do"
    const val TAXES = "$BASE/auth/studente/Tasse/ListaFatture.do"
    const val EXAM_SESSIONS = "$BASE/auth/studente/Appelli/AppelliF.do"
    const val EXAM_BOOKINGS = "$BASE/auth/studente/Appelli/BachecaPrenotazioni.do"
    const val EXAM_RESULTS = "$BASE/auth/studente/Appelli/BachecaEsiti.do"
    const val LIBRETTO = "$BASE/auth/studente/Libretto/LibrettoHome.do"
    const val STUDY_PLAN = "$BASE/auth/studente/Piani/PianiHome.do"
    const val CERTIFICATES = "$BASE/auth/studente/Certificati/ListaCertificati.do"
    const val SELF_CERTIFICATIONS = "$BASE/auth/studente/Autocertificazioni/ListaAutocertificazioni.do"
}
