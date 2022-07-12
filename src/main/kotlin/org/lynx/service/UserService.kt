package org.lynx.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.inject.Inject
import com.google.inject.Singleton
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import retrofit2.Response
import tornadofx.*
import java.util.UUID
import org.lynx.ApplicationScope
import org.lynx.domain.User
import org.lynx.domain.Users
import org.lynx.http.client.AuthCredentialsRequest
import org.lynx.http.client.AuthTokenResponse
import org.lynx.http.client.UserResponse

interface UserService {

    fun logout()

    fun login(username: String, password: String, domain: String)

    fun getCurrentUser(): User

    fun getCurrentUserName(): String

    fun getCurrentUserId(): UUID

    fun addOnChangeUserStateListener(listener: ChangeListener<User?>)

    suspend fun updateUserList()

    val usersList: ObservableList<User>

    var authenticatedUsername: SimpleStringProperty

    val isAuthenticated: SimpleBooleanProperty

    val loginEnded: SimpleBooleanProperty

    val loginResult: SimpleStringProperty
}

@Singleton
class UserServiceImpl : UserService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    @Inject
    private lateinit var networkService: NetworkService

    private var currentUser: SimpleObjectProperty<User?> = SimpleObjectProperty(null)

    override val isAuthenticated = SimpleBooleanProperty(false)

    override val loginEnded = SimpleBooleanProperty(true)

    override val loginResult = SimpleStringProperty("")

    override var authenticatedUsername= SimpleStringProperty("")

    override val usersList: ObservableList<User> = observableListOf()

    override fun getCurrentUser(): User = currentUser.value!!

    override fun getCurrentUserName(): String {
        return if (currentUser.isNotNull.value) {
            getCurrentUser().username
        } else {
            authenticatedUsername.value
        }
    }

    override fun getCurrentUserId(): UUID = getCurrentUser().id.value

    override fun addOnChangeUserStateListener(listener: ChangeListener<User?>) = currentUser.addListener(listener)

    override fun logout() {
        currentUser.value = null
        isAuthenticated.value = false
    }

    override fun login(username: String, password: String, domain: String) {
        loginEnded.value = false
        ApplicationScope.launch {
            try {
                val serverHttpClient = networkService.buildLynxChatServerHttpClient(domain, null)
                val credential = AuthCredentialsRequest(username, password)
                val response = serverHttpClient.authentication(credential)
                when (response.body()) {
                    is AuthTokenResponse -> {
                        log.info("Authenticated: ${response.body()!!.username} ${response.body()!!.token}")
                        val authToken = response.body()!!
                        networkService.buildLynxChatServerHttpClient(domain, authToken.token)
                        authenticatedUsername.value = username
                        loginResult.value = "Authorization successful"
                        try {
                            updateUserList()
                        } catch (e: Exception) {
                            log.error("Failed requesting users")
                            throw e
                        }
                        isAuthenticated.value = true
                    }
                    else -> {
                        log.error("Error while authorizing ${response.raw()}")
                        throw Exception("Error while authorizing ${response.raw()}")
                    }
                }
            } catch (e: Exception) {
                loginResult.value = "Authorization failed"
                log.error("Login error", e)
            } finally {
                loginEnded.value = true
            }
        }
    }

    override suspend fun updateUserList() {
        val serverHttpClient = networkService.getLynxChatServerHttpClient()
        val response: Response<List<UserResponse>>
        try {
            response = serverHttpClient.getUsers()
        } catch (e: Exception) {
            log.error("Error while requesting users", e)
            return
        }
        when (response.body()) {
            is List<UserResponse> -> {
                log.info("Received users list")
                val users: List<UserResponse> = response.body()!!
                transaction {
                    log.debug("Updating users list")
                    usersList.clear()
                    for (user in users) {
                        val userSearchResult = User.find { Users.username eq user.name }
                        if (!userSearchResult.empty()) {
                            log.info("Updated user ${user.name}")
                            Users.update({ Users.username eq user.name }) {
                                it[online] = user.online
                                it[domains] = Gson().toJson(user.domain)
                            }
                            usersList.add(userSearchResult.first())
                        } else {
                            usersList.add(User.new {
                                username = user.name
                                domains = Gson().toJson(user.domain)
                                online = user.online
                                hasNewMessage = false
                            })
                            log.debug("Add new user ${user.name}")
                        }
                    }
                }
                log.info("Users updated")
                for (user in usersList) {
                    networkService.clearLynxChatHttpClientsForAbonent(user.username)
                    val userDomains = Gson().fromJson<List<String>?>(
                        user.domains,
                        object : TypeToken<List<String>>() {}.type
                    )
                    for (domain in userDomains) {
                        networkService.buildLynxChatHttpClient(user.username, domain)
                        log.debug("Created http client for ${user.username} with $domain")
                    }
                    log.info("Created ${userDomains.size} http clients for ${user.username}")
                }
            }
            else -> {
                log.error("Failed to update user list. ${response.raw()}")
            }
        }
        transaction {
            currentUser.value = User.find { Users.username eq authenticatedUsername.value }.first()
        }
    }


}