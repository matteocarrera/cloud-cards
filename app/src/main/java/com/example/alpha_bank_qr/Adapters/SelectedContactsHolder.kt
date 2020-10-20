package com.example.alpha_bank_qr.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alpha_bank_qr.Entities.User
import com.example.alpha_bank_qr.R
import com.example.alpha_bank_qr.Utils.ImageUtils
import kotlinx.android.synthetic.main.saved_card_list_item.view.*

class SelectedContactsHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.selected_saved_card_list_item,
            parent,
            false
        )
    ) {

    private val photo: ImageView = itemView.findViewById(R.id.photo)
    private val id: TextView = itemView.findViewById(R.id.contact_id)
    private val name: TextView = itemView.findViewById(R.id.name)
    private val jobTitle: TextView = itemView.findViewById(R.id.job_title)
    private val company: TextView = itemView.findViewById(R.id.company)

    @SuppressLint("SetTextI18n")
    fun bind(user: User) {
        if (user.photo == "") {
            photo.visibility = View.GONE
            itemView.letters.visibility = View.VISIBLE
            itemView.circle.visibility = View.VISIBLE
            itemView.letters.text = """${user.name.take(1)}${user.surname.take(1)}"""
        } else {
            photo.visibility = View.VISIBLE
            itemView.letters.visibility = View.GONE
            itemView.circle.visibility = View.GONE
            ImageUtils.getImageFromFirebase(user.photo, photo)
        }
        id.text = user.uuid
        name.text = """${user.name} ${user.surname}"""
        if (user.jobTitle.isNotEmpty()) jobTitle.text = user.jobTitle
        else jobTitle.text = "Должность не указана"
        if (user.company.isNotEmpty()) company.text = user.company
        else company.text = "Компания не указана"
    }

}