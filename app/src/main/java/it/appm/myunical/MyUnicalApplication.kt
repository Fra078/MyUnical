package it.appm.myunical

import android.app.Application
import it.appm.myunical.data.MyUnical

class MyUnicalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MyUnical.setup(this)
    }
}