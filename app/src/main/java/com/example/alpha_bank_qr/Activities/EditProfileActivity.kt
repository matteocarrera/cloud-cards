package com.example.alpha_bank_qr.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.alpha_bank_qr.Entities.User
import com.example.alpha_bank_qr.QRDatabaseHelper
import com.example.alpha_bank_qr.R
import com.example.alpha_bank_qr.Utils.DataUtils
import com.example.alpha_bank_qr.Utils.ProgramUtils
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_card.back
import kotlinx.android.synthetic.main.activity_edit_profile.*


class EditProfileActivity : AppCompatActivity() {

    private val fieldsHints = ArrayList<String>()
    private var fieldsNames = ArrayList<EditText>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        fieldsNames = arrayListOf(
            company, job_title, mobile_second, email_second, address, address_second, vk, facebook, instagram, twitter)

        setDataToEdittext()

        initializeFields()

        back.setOnClickListener {
            ProgramUtils.goToActivityAnimated(this, ProfileActivity::class.java)
            finish()
        }
        add_field.setOnClickListener { setFieldsDialog() }
        save.setOnClickListener { saveUser() }
        change_photo.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
        }

        for (i in fieldsNames.indices) {
            fieldsNames[i].setOnTouchListener { _, event -> deleteField(fieldsNames[i], event) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (result != null)photo.setImageURI(result.uri)
        }
    }

    private fun initializeFields() {
        for (i in fieldsNames.indices) {
            checkForVisibility(fieldsNames[i])
        }
        fieldsHints.sort()
    }

    private fun deleteField(editText: EditText, event: MotionEvent): Boolean {
        val DRAWABLE_RIGHT = 2
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= editText.right - editText.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
                editText.visibility = View.GONE
                editText.text.clear()
                initializeFields()
                changeFieldVisibility(editText.hint.toString(), View.GONE)
                return true
            }
        }
        return false
    }

    private fun checkForVisibility(editText: EditText) {
        if (editText.visibility == View.GONE &&
            !fieldsHints.contains(editText.hint.toString())) fieldsHints.add(editText.hint.toString())
        else if (editText.visibility == View.VISIBLE) fieldsHints.remove(editText.hint.toString())
    }

    private fun changeFieldVisibility(name : String, visibility : Int) {
        when (name) {
            "Компания" -> company.visibility = visibility
            "Должность" -> job_title.visibility = visibility
            "email (другой)" -> email_second.visibility = visibility
            "Мобильный номер (другой)" -> mobile_second.visibility = visibility
            "Адрес" -> address.visibility = visibility
            "Адрес (другой)" -> address_second.visibility = visibility
            "vk" -> vk.visibility = visibility
            "facebook" -> facebook.visibility = visibility
            "instagram" -> instagram.visibility = visibility
            "twitter" -> twitter.visibility = visibility
        }
    }

    private fun setFieldsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить поле")

        val fields = fieldsHints.toTypedArray()
        builder.setItems(fields) { _, item ->
            val selectedText = fields[item]
            changeFieldVisibility(selectedText, View.VISIBLE)
            initializeFields()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun setDataToEdittext() {
        val dbHelper = QRDatabaseHelper(this)
        val cursor = dbHelper.getOwnerUser()
        if (cursor!!.count != 0) {
            cursor.moveToFirst()
            photo.setImageDrawable(DataUtils.getImageInDrawable(cursor, "photo"))
            qr.setImageDrawable(DataUtils.getImageInDrawable(cursor, "qr"))
            surname.setText(cursor.getString(cursor.getColumnIndex("surname")))
            name.setText(cursor.getString(cursor.getColumnIndex("name")))
            patronymic.setText(cursor.getString(cursor.getColumnIndex("patronymic")))
            company.setText(cursor.getString(cursor.getColumnIndex("company")))
            job_title.setText(cursor.getString(cursor.getColumnIndex("job_title")))
            mobile.setText(cursor.getString(cursor.getColumnIndex("mobile")))
            mobile_second.setText(cursor.getString(cursor.getColumnIndex("mobile_second")))
            email.setText(cursor.getString(cursor.getColumnIndex("email")))
            email_second.setText(cursor.getString(cursor.getColumnIndex("email_second")))
            address.setText(cursor.getString(cursor.getColumnIndex("address")))
            address_second.setText(cursor.getString(cursor.getColumnIndex("address_second")))
            vk.setText(cursor.getString(cursor.getColumnIndex("vk")))
            facebook.setText(cursor.getString(cursor.getColumnIndex("facebook")))
            instagram.setText(cursor.getString(cursor.getColumnIndex("instagram")))
            twitter.setText(cursor.getString(cursor.getColumnIndex("twitter")))
            notes.setText(cursor.getString(cursor.getColumnIndex("notes")))
        }
        fieldsNames.forEach {
            if (it.text.toString() == "") it.visibility = View.GONE
        }
    }

    private fun saveUser() {
        val dbHelper = QRDatabaseHelper(this)
        val cursor = dbHelper.getOwnerUser()

        when {
            name.text.toString() == "" -> ProgramUtils.setError(this, "Введите имя!")
            surname.text.toString() == "" -> ProgramUtils.setError(this, "Введите фамилию!")
            mobile.text.toString() == "" -> ProgramUtils.setError(this, "Введите мобильный номер!")
            else -> {
                if (cursor!!.count != 0) {
                    // Сохраняем уже в существующий профиль с обновлением данных
                    cursor.moveToFirst()
                    val user = getUserData()
                    user.id = cursor.getInt(cursor.getColumnIndex("id"))
                    dbHelper.updateUser(user)
                } else {
                    // Создаем новый профиль для основного пользователя
                    val user = getUserData()
                    dbHelper.addUser(user)
                }
                ProgramUtils.goToActivityAnimated(this, ProfileActivity::class.java)
                finish()
            }
        }
    }

    private fun getUserData() : User {
        return User(0,
            DataUtils.getImageInByteArray(photo.drawable),
            DataUtils.getImageInByteArray(qr.drawable),
            1,
            0,
            name.text.toString(),
            surname.text.toString(),
            patronymic.text.toString(),
            company.text.toString(),
            job_title.text.toString(),
            mobile.text.toString(),
            mobile_second.text.toString(),
            email.text.toString(),
            email_second.text.toString(),
            address.text.toString(),
            address_second.text.toString(),
            vk.text.toString(),
            facebook.text.toString(),
            instagram.text.toString(),
            twitter.text.toString(),
            notes.text.toString())
    }

    private fun goToActivity(cls : Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}
