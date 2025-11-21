package it.appm.myunical.data.cineca

enum class LoginState {
    LOADING,
    WAIT_CREDENTIALS,
    LOGGING_IN,
    LOGIN_FAILED_CAUSE_NO_CONNECTION, //Nessuna connessione
    LOGIN_FAILED_CAUSE_WRONG_CREDENTIALS, //Credenziali errate
    LOGIN_FAILED_CAUSE_CHANGE_PASSWORD, //L'utente deve aggiornare la password dal sito esse3
    READY
}