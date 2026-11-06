package it.appm.myunical

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import it.appm.myunical.di.coilModule
import it.appm.myunical.di.domainModule
import it.fra.myunical.career.di.careerModule
import it.fra.myunical.datastore.di.datastoreModule
import it.fra.myunical.directory.di.directoryModule
import it.fra.myunical.documents.di.documentsModule
import it.fra.myunical.login.di.loginModule
import it.fra.myunical.network.di.networkModule
import it.fra.myunical.news.di.newsModule
import it.fra.myunical.schedule.di.scheduleModule
import it.fra.myunical.taxes.di.taxesModule
import it.fra.myunical.today.di.todayModule
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
            modules(
                // Data and domain
                networkModule, datastoreModule, domainModule, coilModule,
                // Features
                loginModule, todayModule, careerModule, scheduleModule, taxesModule,
                newsModule, documentsModule, directoryModule
            )
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = get()


}