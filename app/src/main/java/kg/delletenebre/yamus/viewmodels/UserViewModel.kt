package kg.delletenebre.yamus.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UserViewModel : ViewModel() {

    val likedTracksIds = MutableLiveData<List<String>>().apply {
        value = listOf()
    }



    class Factory() : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserViewModel() as T
        }
    }
}