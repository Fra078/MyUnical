package it.appm.myunical.ui.screen.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.appm.myunical.data.MyUnical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    val carriera = MyUnical.dataRepository.carriera
    val userPhoto = MyUnical.dataRepository.userPhoto
    val news = MyUnical.dataRepository.getNewsDao().getNews()
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    init {
        updateContent()
    }

    fun updateContent(){
        viewModelScope.launch(Dispatchers.IO){
            _isUpdating.value = true
            MyUnical.dataRepository.aggiornaCarriera()
            MyUnical.dataRepository.aggiornaNews()
            _isUpdating.value = false
        }
    }
}