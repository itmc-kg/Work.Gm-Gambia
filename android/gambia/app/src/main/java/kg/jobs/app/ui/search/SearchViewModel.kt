package kg.jobs.app.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kg.jobs.app.model.Vacancy
import kg.jobs.app.repository.LocalPrefData
import kg.jobs.app.repository.SearchRepository
import kg.jobs.app.ui.BaseViewModel
import kotlinx.coroutines.launch

class SearchViewModel(val repo: SearchRepository,
                      val localPrefData: LocalPrefData,
                      val firestore: FirebaseFirestore) : BaseViewModel() {
    enum class State { LOADING, ACCEPT, DECLINE, ERROR, SUCCESS }

    private val _vacancy = MutableLiveData<Vacancy>()
    val vacancy: LiveData<Vacancy> = _vacancy

    private val _applyState = MutableLiveData<Pair<State, Vacancy>>()
    val applyState: LiveData<Pair<State, Vacancy>> = _applyState

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val _loadData = MutableLiveData<Boolean>()
    val loadData: LiveData<Boolean> = _loadData

    private var listener: ListenerRegistration? = null

    init {
        _state.postValue(State.LOADING)
        val spheres = localPrefData.filterSpheres()
        val region = localPrefData.filterRegion()
        val salaryFrom = localPrefData.filterSalaryFrom()


        val vacancyRef = if (spheres.isNotEmpty()) {
            firestore.collection("vacancies")
                    .whereEqualTo("status", "opened")
                    .whereIn("sphereId", spheres)
                    .whereEqualTo("country", localPrefData.country())
        } else {
            firestore.collection("vacancies")
                    .whereEqualTo("status", "opened")
                    .whereEqualTo("country", localPrefData.country())
        }

        listener = vacancyRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                _state.postValue(State.ERROR)
                Log.w("Exception", "listen:error", e)
                return@addSnapshotListener
            }
            launch {
                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            val vacancy = repo.vacancy(dc.document)
                            if (vacancy.salary.start >= salaryFrom || vacancy.salary.id == "undefined") {
                                Log.e("LOAD", "---")
                                _loadData.postValue(true)
                                if (vacancy.employer?.regionId == region
                                        || (localPrefData.country() == "KG" && region == "all_kg") ||
                                        (localPrefData.country() == "GM" && region == "all_gm")) {
                                    if (spheres.isEmpty() || vacancy.sphere.id in spheres) {
                                        Log.e("TAG -- ", vacancy.position)
                                        if (!repo.hasApplication(vacancy)) {
                                            _vacancy.value = vacancy
                                            _state.postValue(State.SUCCESS)
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
                _loadData.postValue(false)
                _state.postValue(State.SUCCESS)
            }
        }
    }

    fun accept(vacancy: Vacancy) {
        launch {
            _state.postValue(State.LOADING)
            _applyState.postValue(Pair(State.LOADING, vacancy))
            val result = repo.apply(vacancy)
            _applyState.postValue(Pair(if (result) State.ACCEPT else State.ERROR, vacancy))
        }
    }

    fun decline(vacancy: Vacancy) {
        launch {
            _state.postValue(State.LOADING)
            _applyState.postValue(Pair(State.LOADING, vacancy))
            val result = repo.decline(vacancy)
            _applyState.postValue(Pair(if (result) State.DECLINE else State.ERROR, vacancy))
        }
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}