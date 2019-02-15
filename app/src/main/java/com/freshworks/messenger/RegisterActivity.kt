package com.freshworks.messenger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_register)



        btn_register.setOnClickListener {
            performRegister()
        }

        tv_login_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btn_select_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }


    }

    var selectedPhotoUri: Uri? = null


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitMap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            civ_profilePic.setImageBitmap(bitMap!!)

            btn_select_image.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = et_email.text.toString()
        val password = et_password.text.toString()
        val username = et_user_name_main.text.toString()

        if (selectedPhotoUri == null) {
            Snackbar.make(parentRegister, "Please Choose a profile picture", Snackbar.LENGTH_LONG).show()
            return

        }

        if (email.isEmpty()) {
            Snackbar.make(parentRegister, "Please Enter Email", Snackbar.LENGTH_LONG).show()
            return
        }
        if (username.isEmpty()) {
            Snackbar.make(parentRegister, "Please Enter Username", Snackbar.LENGTH_LONG).show()
            return
        }


        if (password.isEmpty()) {
            Snackbar.make(parentRegister, "Please Enter Password", Snackbar.LENGTH_LONG).show()
            return
        }


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (!it.isSuccessful) {
                return@addOnCompleteListener
            } else {
                Toast.makeText(this, "Suces ${it.result!!.user!!.uid}", Toast.LENGTH_LONG).show()
                uploadImageToFirebaseStorage()
            }
        }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }

    }

    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null) return
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Toast.makeText(this, "cool", Toast.LENGTH_LONG).show()
                ref.downloadUrl.addOnSuccessListener {

                    saveUserToDb(it.toString())

                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun saveUserToDb(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val username = et_user_name_main.text.toString()
        val user = User(uid.toString(), username, profileImageUrl)

        ref.setValue(user).addOnFailureListener {
            Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
        }
            .addOnSuccessListener {
                startActivity(Intent(this, LatestMessagesActivity::class.java))
                finish()
            }

    }

}

data class User(val uid: String, val username: String, val profileImageUrl: String)
