package befree.ramen_eating_and_walking

import android.provider.Settings.Secure.getString
import java.util.*

class UserProfile {

    var id = UUID.randomUUID().toString()
    var uid = ""
    var userName = ""
    var email = ""
    var origin = "(未設定)"
    var activityArea = "(未設定)"
    var likes = "(未設定)"
    var introduction = "(未設定)"
    var image = ""

}