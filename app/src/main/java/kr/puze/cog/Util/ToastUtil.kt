package www.okit.co.Utils

import android.content.Context
import android.widget.Toast

class ToastUtil(context: Context) {
    var context = context

    fun short(message: String) {
        if (isEmpty(message))
            return
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun Long(message: String) {
        if (isEmpty(message))
            return
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun isEmpty(str: String?): Boolean {
        return str == null || str.trim { it <= ' ' }.isEmpty()
    }
}