package com.example.cloud_cards.Activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.cloud_cards.Adapters.DataAdapter
import com.example.cloud_cards.Database.AppDatabase
import com.example.cloud_cards.Entities.*
import com.example.cloud_cards.R
import com.example.cloud_cards.Utils.ColorUtils
import com.example.cloud_cards.Utils.DataUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_personal_card.*
import java.util.*
import kotlin.collections.ArrayList

class CreatePersonalCardActivity : AppCompatActivity() {

    private val selectedItems = ArrayList<DataItem>()
    private var cardColor = ColorUtils.getColorList()[0]
    private lateinit var db: AppDatabase
    private lateinit var templateUserList: List<UserBoolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_personal_card)

        templateUserList = intent.getParcelableArrayListExtra("templateUserList")!!

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_add)
        toolbar.inflateMenu(R.menu.create_card_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.done -> {
                    saveTemplate()
                }
            }
            true
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }

        db = AppDatabase.getInstance(this)

        selectedItems.clear()

        setDataToListView()

        card_color.setOnClickListener {
            cardColor =
                ColorUtils.getColorList()[(0 until ColorUtils.getColorList().count()).random()]
            card_color.setCardBackgroundColor(Color.parseColor(cardColor))
        }
    }

    private fun saveTemplate() {
        val ownerUser = db.userDao().getOwnerUser()
        val newTemplateUser = DataUtils.parseDataToUser(selectedItems)

        if (templateUserList.contains(newTemplateUser)) {
            val templateUser = templateUserList[templateUserList.indexOf(newTemplateUser)]
            val card = Card(
                UUID.randomUUID().toString(),
                CardType.personal,
                cardColor,
                card_title.text.toString(),
                templateUser.uuid
            )
            db.cardDao().insertCard(card)
            finish()
            return
        }

        newTemplateUser.parentId = ownerUser!!.uuid
        newTemplateUser.uuid = UUID.randomUUID().toString()

        val businessCard = BusinessCard(CardType.personal, newTemplateUser)

        FirebaseFirestore.getInstance()
            .collection("users").document(newTemplateUser.parentId)
            .collection("cards").document(newTemplateUser.uuid)
            .set(businessCard)

        db.idPairDao().insertPair(IdPair(newTemplateUser.uuid, newTemplateUser.parentId))

        val card = Card(
            UUID.randomUUID().toString(),
            CardType.personal,
            cardColor,
            card_title.text.toString(),
            newTemplateUser.uuid
        )
        db.cardDao().insertCard(card)

        finish()
    }

    private fun setDataToListView() {
        val user = db.userDao().getOwnerUser()
        if (user != null) {
            val data = DataUtils.setUserData(user)

            val adapter = DataAdapter(data, View.VISIBLE, this)
            data_list.choiceMode = ListView.CHOICE_MODE_MULTIPLE
            data_list.adapter = adapter
            data_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

                val item = data[position]
                item.checked = !item.checked
                adapter.notifyDataSetChanged()

                if (selectedItems.contains(item)) selectedItems.remove(item)
                else selectedItems.add(item)
            }
        }
    }
}