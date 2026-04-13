package com.cooking.app

import android.app.Application
import com.cooking.app.data.repository.AppRepository

class CookingApp : Application() {

    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(applicationContext)
    }
}
