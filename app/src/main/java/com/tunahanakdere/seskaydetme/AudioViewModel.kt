package com.tunahanakdere.seskaydetme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val _audioList = MutableLiveData<MutableList<String>>()
    val audioList: LiveData<MutableList<String>> get() = _audioList

    init {
        _audioList.value = SharedPreferencesUtil.loadAudioFilePaths(getApplication()).toMutableList()
    }

    fun addAudio(filePath: String) {
        _audioList.value?.add(filePath)
        _audioList.value = _audioList.value // Listeyi güncelle
        saveAudioList()
    }

    fun removeAudio(filePath: String) {
        _audioList.value?.remove(filePath)
        _audioList.value = _audioList.value // Listeyi güncelle
        saveAudioList()
    }

    private fun saveAudioList() {
        _audioList.value?.let { list ->
            SharedPreferencesUtil.saveAudioFilePaths(getApplication(), list)
        }
    }
}
