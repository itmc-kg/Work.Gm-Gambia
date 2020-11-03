package kg.jobs.app

import androidx.core.app.NotificationManagerCompat
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.fabric.sdk.android.Fabric
import kg.jobs.app.model.Employer
import kg.jobs.app.notification.NotificationCenter
import kg.jobs.app.repository.*
import kg.jobs.app.repository.application.ApplicationDataSourceFactory
import kg.jobs.app.repository.vacancy.VacancyDataSourceFactory
import kg.jobs.app.ui.chats.ChatsViewModel
import kg.jobs.app.ui.country.ChooseCountryViewModel
import kg.jobs.app.ui.employer.VacanciesViewModel
import kg.jobs.app.ui.employer.edit.EditEmployerViewModel
import kg.jobs.app.ui.employer.show.EmployerDetailViewModel
import kg.jobs.app.ui.employer.vacancy.detail.OwnerVacancyDetailViewModel
import kg.jobs.app.ui.employer.vacancy.detail.VacancyApplicationsViewModel
import kg.jobs.app.ui.employer.vacancy.detail.applicationDetail.ApplicationDetailViewModel
import kg.jobs.app.ui.filter.FilterViewModel
import kg.jobs.app.ui.filter.PreFilterViewModel
import kg.jobs.app.ui.login.CountriesViewModel
import kg.jobs.app.ui.login.LoginViewModel
import kg.jobs.app.ui.messenger.ChatViewModel
import kg.jobs.app.ui.profile.ProfileViewModel
import kg.jobs.app.ui.profile.edit.EditProfileViewModel
import kg.jobs.app.ui.profile.show.ProfileDetailViewModel
import kg.jobs.app.ui.role.RoleViewModel
import kg.jobs.app.ui.search.SearchViewModel
import kg.jobs.app.ui.search.show.VacancyDetailViewModel
import kg.jobs.app.ui.vacancy.EditVacancyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

class App : MultiDexApplication() {
    private val employers = mutableMapOf<String, Employer>()
    private val module: Module = module {
        single(named("employers")) { employers }

        single { FirebaseFirestore.getInstance() }
        single { LocalPrefData(get()) }
        single { FirebaseStorage.getInstance().reference }
        single { NotificationCenter(get(), NotificationManagerCompat.from(get())) }

        factory(named("uid")) { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
        factory(named("lang")) {
            if (LangPref(applicationContext).isLang(applicationContext)){
                LangPref(applicationContext).getLang(applicationContext)
            }else {
                val prefData: LocalPrefData = get()
                when (prefData.country()) {
                    "KG" -> "ru"
                    else -> "en"
                }
            }
        }

        single { ImageRepository(get()) }
        factory { CountryRepository(get()) }
        factory { LoginRepository(get()) }
        factory { ProfileRepository(get(named("uid")), get(), get()) }
        factory { FilterRepository(get(named("lang")), get(), get()) }
        factory { SearchRepository(get(named("uid")), get(named("lang")), get(), get(), get(named("employers"))) }
        factory { EmployerRepository(get(named("uid")), get(named("lang")), get(), get(), get(named("employers"))) }
        factory { VacancyRepository(get(), get(named("uid")), get(), get(), get(named("employers"))) }
        factory { VacancyDataSourceFactory(get(named("uid")), get(), get(named("lang"))) }
        factory { ApplicationDataSourceFactory(get(named("uid")), get(), get(named("lang")), get(named("employers"))) }
        factory { RoleRepository(get(named("uid")), get(), get()) }
        factory { VacancyApplicationsRepository(get(named("uid")), get()) }
        factory { ChatRepository(get(named("uid")), get(), get(named("employers"))) }

        viewModel { CountriesViewModel(get()) }
        viewModel { LoginViewModel(get()) }
        viewModel { EditProfileViewModel(get(), get(), get()) }
        viewModel { PreFilterViewModel(get(), get()) }
        viewModel { params ->
            ProfileDetailViewModel(get(named("uid")),
                    (params.get(0) as String).takeIf { it.isNotEmpty() } ?: get(named("uid")),
                    get(), get())
        }
        viewModel { ProfileViewModel(get(), get()) }
        viewModel { SearchViewModel(get(), get(), get()) }
        viewModel { VacancyDetailViewModel(get(), get()) }
        viewModel { params ->
            EmployerDetailViewModel(get(named("uid")),
                    (params.get(0) as String).takeIf { it.isNotEmpty() } ?: get(named("uid")),
                    get(), get())
        }
        viewModel { VacanciesViewModel(get(), get()) }
        viewModel { EditVacancyViewModel(get(), get()) }
        viewModel { EditEmployerViewModel(get(), get(), get()) }
        viewModel { RoleViewModel(get(named("uid")), get(), get()) }
        viewModel { OwnerVacancyDetailViewModel(get(), get()) }
        viewModel { FilterViewModel(get(), get()) }
        viewModel { VacancyApplicationsViewModel(get(), get(), get()) }
        viewModel { ApplicationDetailViewModel(get(), get()) }
        viewModel { params -> ChatsViewModel(params[0], params[1], get(named("uid")), get(), get()) }
        viewModel { params -> ChatViewModel(get(named("uid")), get(), params[0]) }
        viewModel { ChooseCountryViewModel(get(named("uid")), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(module))
        }
        Fresco.initialize(this)
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}