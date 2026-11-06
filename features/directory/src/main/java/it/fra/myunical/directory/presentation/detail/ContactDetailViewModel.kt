package it.fra.myunical.directory.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.directory.models.ContactDetailUiState
import it.fra.myunical.domain.model.directory.Contact
import it.fra.myunical.domain.repository.DirectoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactDetailViewModel(
    private val contactId: String,
    private val directory: DirectoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ContactDetailUiState>(ContactDetailUiState.Loading)
    val state: StateFlow<ContactDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            // The search result is shown immediately, the full record adds the duties
            directory.cached(contactId)?.let { _state.value = ContactDetailUiState.Loaded(it, isComplete = false) }
            val contact = try {
                directory.getContact(contactId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            val shown = contact ?: (_state.value as? ContactDetailUiState.Loaded)?.contact
            if (shown == null) {
                _state.value = ContactDetailUiState.Error
                return@launch
            }
            _state.value = ContactDetailUiState.Loaded(shown, isComplete = true, isTeacherLoading = shown.isTeacher)
            if (shown.isTeacher) loadTeacher(shown)
        }
    }

    private suspend fun loadTeacher(contact: Contact) {
        val teacher = try {
            directory.getTeacherProfile(contact.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The address book data is still useful without the teacher page
            null
        }
        _state.update { current ->
            if (current is ContactDetailUiState.Loaded) current.copy(teacher = teacher, isTeacherLoading = false) else current
        }
    }
}
