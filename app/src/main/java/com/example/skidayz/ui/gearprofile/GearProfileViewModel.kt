package com.example.skidayz.ui.gearprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GearProfileViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = "This is a Gear Profile Fragment"
    }
    val text: LiveData<String> = _text
}