package org.lynx

import com.google.crypto.tink.hybrid.HybridConfig
import com.google.inject.Guice
import javafx.scene.image.Image
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.javafx.JavaFx
import org.lynx.http.server.HttpServer
import org.lynx.service.CryptographyService
import org.lynx.service.DatabaseService
import org.lynx.service.MessageService
import org.lynx.service.UserService
import org.lynx.view.LoginView
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tornadofx.*
import kotlin.reflect.KClass

fun main(args: Array<String>) {
    launch<Application>(args)
}

val ApplicationScope = CoroutineScope(SupervisorJob() + Dispatchers.JavaFx)

class Application : App(LoginView::class, Style::class) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Application::class.java)
    }

    private var server: HttpServer
    private var databaseService: DatabaseService

    init {
        System.setProperty("prism.lcdtext", "false");
        val guice = Guice.createInjector(LynxModule())

        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = guice.getInstance(type.java)
        }

        databaseService = (FX.dicontainer as DIContainer).getInstance()
        val messageService: MessageService = (FX.dicontainer as DIContainer).getInstance()
        val userService: UserService = (FX.dicontainer as DIContainer).getInstance()
        val cryptographyService: CryptographyService = (FX.dicontainer as DIContainer).getInstance()
        server = HttpServer(8888, messageService, cryptographyService, userService)
        HybridConfig.register();
    }

    override fun start(stage: Stage) {
        log.info("Lynx chat started")
        server.start()

        with(stage) {
            setOnCloseRequest {
                shutdown()
            }
            width = 450.0
            height = 600.0
            icons.add(Image("img/LynxIcon.png"))
        }
        super.start(stage)
    }


    fun shutdown() {
        log.info("Shutdown Lynx chat")
        server.stop()
    }
}