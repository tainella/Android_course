package com.example.third_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.third_activity.server.API
import com.example.third_activity.server.APIService
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val listAdapter = ListAdapter(this::openCard)

    //выполняет запросы к серверу
    private val okhttp = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    //конвертация джейсона из сервера
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        //.addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(okhttp)
        .baseUrl("https:jutter.online/LambdaTestApi/")
        .build()

    //сервис
    private val service = APIService(retrofit.create(API::class.java))
    //запуск многопоточности
    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Main + job

    var realm: Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(baseContext)
        val config = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .name("hustle_app.db")
            .build()
        realm = Realm.getInstance(config)

        rvList.adapter = listAdapter
        rvList.layoutManager = GridLayoutManager(baseContext, 2)

        launchUI { //UI поток, интерфейс можно менять только в нем
            val list = withIO { service.getList() } //withIO помогает получать данные из другого потока, так быстрее
            pbLoading.visibility = View.GONE
            rvList.visibility = View.VISIBLE
            listAdapter.addList(list)
        }

        realm?.executeTransaction {
            val food = FoadModel("id", "хлеб", 5)
            it.copyToRealmOrUpdate()
        } //? может быть нулем, не вызываем функцию

        val list = realm?.where(FoodModel::class.java)?.findAll()
        print(list)
    }

    private fun openCard(item: String, count: Int) {
        val intent = Intent(this, CardActivity::class.java)
        intent.putExtra("count", count)
        intent.putExtra("item", item)
        startActivity(intent)
    }

    //упрощение работы с многопоточностью
    fun CoroutineScope.launchUI(callback: suspend () -> Unit) = launch(Dispatchers.Main) { callback() }
    suspend fun <T> withIO(callback: suspend () -> T) = withContext(Dispatchers.Main) { callback() }
}