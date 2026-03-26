package al.sabil

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import al.sabil.data.AppDatabase
import al.sabil.data.DatabaseSeeder
import java.util.Locale

class AlSabiilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        forceArabicLocale(this)
        
        // Initialize database and start seeding
        val database = AppDatabase.getInstance(this)
        DatabaseSeeder.seedDatabase(this, database)
    }

    override fun attachBaseContext(base: Context) {
        val locale = Locale("ar")
        val config = Configuration(base.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    companion object {
        @Suppress("DEPRECATION")
        fun forceArabicLocale(context: Context) {
            val locale = Locale("ar")
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            // updateConfiguration is deprecated but is the only way to force locale
            // on the running activity's resources. attachBaseContext handles the Application.
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}
