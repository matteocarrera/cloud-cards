package com.example.alpha_bank_qr.Fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alpha_bank_qr.Database.AppDatabase
import com.example.alpha_bank_qr.Entities.User
import com.example.alpha_bank_qr.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_camera.*
import me.dm7.barcodescanner.zxing.ZXingScannerView


class CameraFragment : Fragment() {

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext())
        Dexter.withContext(view.context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener, ZXingScannerView.ResultHandler {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    scanner.setResultHandler(this)
                    scanner.startCamera()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(view.context, "Необходим доступ к камере!", Toast.LENGTH_SHORT)
                        .show()
                    requireActivity().onBackPressed()
                }

                // Обрабатываем результат сканирования QR
                override fun handleResult(rawResult: Result?) {
                    if (rawResult != null) {
                        val databaseRef =
                            FirebaseDatabase.getInstance().getReference(rawResult.toString())
                        databaseRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val jsonUser = dataSnapshot.value.toString()
                                val user = Gson().fromJson(jsonUser, User::class.java)
                                val allUsers = db.userDao().getAllUsers()
                                var userExists = false
                                allUsers.forEach {
                                    if (it.uuid == user.uuid) userExists = true
                                }
                                if (userExists) {
                                    Toast.makeText(
                                        view.context,
                                        "Такая визитка уже существует!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    requireActivity().onBackPressed()
                                } else {
                                    db.userDao().insertUser(user)
                                    Toast.makeText(
                                        view.context,
                                        "Визитка успешно считана!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    requireActivity().onBackPressed()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                println("Ошибка считывания: " + databaseError.code)
                            }
                        })
                    }
                }
            }).check()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanner.stopCamera()
    }

    override fun onPause() {
        super.onPause()
        scanner.stopCamera()
    }

    override fun onStop() {
        super.onStop()
        scanner.stopCamera()
    }
}