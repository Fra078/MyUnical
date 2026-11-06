package it.appm.myunical

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import it.appm.myunical.data.MyUnical
import it.appm.myunical.di.appModule
import it.appm.myunical.di.coilModule
import it.appm.myunical.di.domainModule
import it.fra.myunical.datastore.di.datastoreModule
import it.fra.myunical.login.di.loginModule
import it.fra.myunical.network.di.networkModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MyUnicalApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyUnicalApplication)
            modules(domainModule, networkModule, datastoreModule, appModule, loginModule,
                coilModule
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = get()


}