package www.okit.co.Utils

import android.content.Context
import android.content.SharedPreferences

class PrefUtil(context: Context) {
    private var preferences: SharedPreferences = context.getSharedPreferences("Data", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = preferences.edit()

    var isLogin : Boolean
        get() = preferences.getBoolean("isLogin", false)
        set(value) {
            editor.putBoolean("isLogin", value)
            editor.apply()
        }

    var isAdmin : Boolean
        get() = preferences.getBoolean("isAdmin", false)
        set(value) {
            editor.putBoolean("isAdmin", value)
            editor.apply()
        }

    var phone: String
        get() = preferences.getString("phone", "").toString()
        set(value) {
            editor.putString("phone", value)
            editor.apply()
        }
}