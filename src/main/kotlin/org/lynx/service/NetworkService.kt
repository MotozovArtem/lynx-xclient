package org.lynx.service

import com.google.common.base.Strings
import com.google.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.lynx.http.client.LynxChatClient
import org.lynx.http.client.LynxChatServerClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

interface NetworkService {
    fun buildLynxChatServerHttpClient(domain: String, authenticationToken: String?): LynxChatServerClient

    fun buildLynxChatHttpClient(abonentName: String, abonentDomain: String): LynxChatClient

    fun clearLynxChatHttpClientsForAbonent(abonentName: String)

    fun getLynxChatHttpClientByAbonent(abonentName: String): List<LynxChatClient>

    fun getLynxChatServerHttpClient(): LynxChatServerClient
}

@Singleton
class NetworkServiceImpl : NetworkService {

    private val lynxChatClientMap: MutableMap<String, MutableList<LynxChatClient>> = mutableMapOf()

    private lateinit var lynxChatServerClient: LynxChatServerClient

    private val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("localhost", 9050))

    override fun buildLynxChatServerHttpClient(domain: String, authenticationToken: String?): LynxChatServerClient {
        if (Strings.isNullOrEmpty(authenticationToken)) {
            val httpClient = OkHttpClient()
                .newBuilder()
                .readTimeout(3, TimeUnit.MINUTES)
                .connectTimeout(3, TimeUnit.MINUTES)
                .addInterceptor(HttpLoggingInterceptor())
                .addInterceptor { chain ->
                    val original: Request = chain.request()
                    val request: Request = original.newBuilder()
                        .header("Accept", "application/json")
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .proxy(proxy)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://$domain")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            lynxChatServerClient = retrofit.create(LynxChatServerClient::class.java)
        } else {
            val httpClient = OkHttpClient()
                .newBuilder()
                .readTimeout(3, TimeUnit.MINUTES)
                .connectTimeout(3, TimeUnit.MINUTES)
                .proxy(proxy)
                .addInterceptor(HttpLoggingInterceptor())
                .addInterceptor { chain: Interceptor.Chain ->
                    val original: Request = chain.request()
                    val request: Request =
                        original.newBuilder()
                            .header("Authorization", "Bearer $authenticationToken")
                            .method(original.method, original.body).build()
                    chain.proceed(request)
                }
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("http://$domain")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            lynxChatServerClient = retrofit.create(LynxChatServerClient::class.java)
        }
        return lynxChatServerClient
    }

    override fun buildLynxChatHttpClient(abonentName: String, abonentDomain: String): LynxChatClient {
        val httpClient = OkHttpClient()
            .newBuilder()
            .readTimeout(3, TimeUnit.MINUTES)
            .connectTimeout(3, TimeUnit.MINUTES)
            .addInterceptor(HttpLoggingInterceptor())
            .proxy(proxy)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$abonentDomain")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val lynxChatClient = retrofit.create(LynxChatClient::class.java)
        if (lynxChatClientMap.containsKey(abonentName)) {
            lynxChatClientMap.computeIfPresent(abonentName) { _, list ->
                list.apply {
                    add(lynxChatClient)
                }
            }
        } else {
            val httpClients = mutableListOf(lynxChatClient)
            lynxChatClientMap[abonentName] = httpClients
        }
        return lynxChatClient
    }

    override fun clearLynxChatHttpClientsForAbonent(abonentName: String) {
        lynxChatClientMap[abonentName]?.clear()
    }

    override fun getLynxChatHttpClientByAbonent(abonentName: String): List<LynxChatClient> =
        lynxChatClientMap[abonentName]!!

    override fun getLynxChatServerHttpClient(): LynxChatServerClient = lynxChatServerClient

}