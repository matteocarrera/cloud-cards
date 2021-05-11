package com.example.cloud_cards.Utils

import com.example.cloud_cards.Entities.User

class Json {
    companion object {
        fun toJson(user : User) : String {
            return user.toString()
        }

        fun fromJson(json : String) : User {
            val userData = json.split('|')
            val user = User()
            user.isScanned = true
            user.photo = userData[0]
            user.name = userData[1]
            user.surname = userData[2]
            user.patronymic = userData[3]
            user.company = userData[4]
            user.jobTitle = userData[5]
            user.mobile = userData[6]
            user.mobileSecond = userData[7]
            user.email = userData[8]
            user.emailSecond = userData[9]
            user.address = userData[10]
            user.addressSecond = userData[11]
            user.vk = userData[15]
            user.facebook = userData[16]
            user.instagram = userData[17]
            user.twitter = userData[18]
            user.notes = userData[19]
            return user
        }
    }
}