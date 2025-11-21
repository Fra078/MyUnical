package it.appm.myunical.ui.screen.libretto

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.appm.myunical.data.MyUnical
import it.appm.myunical.data.room.LibrettoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibrettoViewModel : ViewModel() {

    var loading by mutableStateOf(true)
    var list : List<LibrettoItem> by mutableStateOf(emptyList())
    var cfu by mutableIntStateOf(-1)
    var maxCfu by mutableIntStateOf(-1)
    var mediaP by mutableFloatStateOf(-1f)
    var votoLaurea by mutableFloatStateOf(-1f)

    init {
        updateLibretto()
        MyUnical.dataRepository.getLibretto().getAll()
            .filter { it.isNotEmpty() }
            .onEach { items ->
                var tempCfu = 0
                var tempMaxCfu = 0
                var tempMediaP = 0f
                var cfuMedia = 0
                items.forEach {
                    tempMaxCfu += it.cfu

                    if (it.vote != null || !it.date.isNullOrBlank())
                        tempCfu += it.cfu

                    if (it.vote != null) {
                        cfuMedia += it.cfu
                        tempMediaP += if (it.laude) it.cfu * 33
                                      else it.vote * it.cfu
                    }
                }
                cfu = tempCfu
                maxCfu = tempMaxCfu
                mediaP = tempMediaP / cfuMedia
                votoLaurea = mediaP * 11 / 3
                list = items

            }.flowOn(Dispatchers.Main).launchIn(viewModelScope)
    }

    fun updateLibretto(){
        loading = true
        viewModelScope.launch(Dispatchers.IO) {
            MyUnical.dataRepository.aggiornaLibretto()
            withContext(Dispatchers.Main){
                loading = false
            }
        }
    }

}