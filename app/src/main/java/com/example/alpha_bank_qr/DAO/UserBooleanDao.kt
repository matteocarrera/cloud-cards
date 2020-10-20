package com.example.alpha_bank_qr.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.alpha_bank_qr.Entities.UserBoolean

@Dao
interface UserBooleanDao {
    @Insert
    fun insertUserBoolean(userBoolean: UserBoolean)

    @Delete
    fun deleteUserBoolean(userBoolean: UserBoolean)

    @Query("SELECT * FROM usersBoolean")
    fun getAllUsersBoolean(): List<UserBoolean>

    @Query("SELECT * FROM usersBoolean WHERE parentId = :id")
    fun getTemplateUsers(id: String): List<UserBoolean>

    @Query("SELECT * FROM usersBoolean WHERE parentId != :id")
    fun getContactUsers(id: String): List<UserBoolean>
}