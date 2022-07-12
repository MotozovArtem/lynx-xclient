package org.lynx

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.lynx.service.CryptographyService
import org.lynx.service.CryptographyServiceImpl
import org.lynx.service.DatabaseService
import org.lynx.service.DatabaseServiceImpl
import org.lynx.service.MessageService
import org.lynx.service.MessageServiceImpl
import org.lynx.service.NetworkService
import org.lynx.service.NetworkServiceImpl
import org.lynx.service.SettingsService
import org.lynx.service.SettingsServiceImpl
import org.lynx.service.UserService
import org.lynx.service.UserServiceImpl

class LynxModule : AbstractModule() {
    override fun configure() {
        bind(MessageService::class.java).to(MessageServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CryptographyService::class.java).to(CryptographyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DatabaseService::class.java).to(DatabaseServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(UserService::class.java).to(UserServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(NetworkService::class.java).to(NetworkServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SettingsService::class.java).to(SettingsServiceImpl::class.java).`in`(Scopes.SINGLETON)
    }
}