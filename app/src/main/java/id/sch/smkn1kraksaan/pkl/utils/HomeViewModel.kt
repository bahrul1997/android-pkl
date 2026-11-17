package id.sch.smkn1kraksaan.pkl.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.models.DashboardData
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _dashboard = MutableLiveData<DashboardData?>()
    val dashboard: LiveData<DashboardData?> = _dashboard

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDashboard() {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = ApiClient.getApiService().getDashboard()

                if (response.isSuccessful && response.body()?.status == true) {
                    _dashboard.value = response.body()!!.data
                } else {
                    _error.value = response.body()?.message ?: "Gagal memuat dashboard"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}