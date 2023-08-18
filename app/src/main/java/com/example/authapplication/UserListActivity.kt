package com.example.authapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class UserListActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

    private val firestoreDb = Firebase.firestore.collection("users")

    private val userListAdapter = UserListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        progressBar = findViewById(R.id.progressBar)

        val userListView = findViewById<RecyclerView>(R.id.usersListView)
        userListView.adapter = userListAdapter
        userListView.layoutManager = LinearLayoutManager(this)

        getUsersFromFirestore()

        val addUserButton = findViewById<View>(R.id.addNewUserButton)

        addUserButton.setOnClickListener {

            var selectedDateOfBirth: Calendar? = null

            val addNewUserView = layoutInflater.inflate(R.layout.layout_add_new_user, null, false)

            val newUserNameView = addNewUserView.findViewById<EditText>(R.id.newUserNameView)
            val dateOfBirthView = addNewUserView.findViewById<TextView>(R.id.dateOfBirthView)
            val updateDateOfBirthButton =
                addNewUserView.findViewById<View>(R.id.updateDateOfBirthButton)
            val maleRadioButton = addNewUserView.findViewById<RadioButton>(R.id.maleRadioButton)
            val femaleRadioButton = addNewUserView.findViewById<RadioButton>(R.id.femaleRadioButton)
            val otherRadioButton = addNewUserView.findViewById<RadioButton>(R.id.otherRadioButton)

            updateDateOfBirthButton.setOnClickListener {
                val datePickerDialog = DatePickerDialog(this@UserListActivity)
                datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                    selectedDateOfBirth = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    dateOfBirthView.text = selectedDateOfBirth!!.toDDMMYYYY()
                }
                datePickerDialog.show()
            }

            AlertDialog
                .Builder(this@UserListActivity)
                .setView(addNewUserView)
                .setPositiveButton("Add User") { _, _ ->

                    val newUserName = newUserNameView.text.toString()
                    if (newUserName.isEmpty() || newUserName.isBlank()) {
                        showToast("Name must not be empty")
                        return@setPositiveButton
                    }

                    if (selectedDateOfBirth == null) {
                        showToast("Select a date of birth")
                        return@setPositiveButton
                    }

                    firestoreDb.add(
                        mapOf(
                            "name" to newUserName,
                            "dateOfBirth" to selectedDateOfBirth!!.timeInMillis,
                            "gender" to if (maleRadioButton.isChecked) "m" else if (femaleRadioButton.isChecked) "f" else "o"
                        )
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            getUsersFromFirestore()
                        }
                    }
                }
                .show()
        }
    }

    private fun getUsersFromFirestore() {

        userListAdapter.resetList()
        progressBar?.visibility = View.VISIBLE

        firestoreDb.get().addOnCompleteListener {

            progressBar?.visibility = View.GONE

            if (it.isSuccessful) {
                userListAdapter.addUsers(it.result.toList())
            }

        }
    }
}

class UserListAdapter : RecyclerView.Adapter<UserItemViewHolder>() {

    private val users = mutableListOf<QueryDocumentSnapshot>()

    fun addUsers(users: List<QueryDocumentSnapshot>) {
        this.users.addAll(users)
        notifyDataSetChanged()
    }

    fun resetList() {
        users.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        return UserItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_user_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {

        val user = users[position]

        holder.nameView.text = user.getString("name")
        holder.dobView.text = Calendar.getInstance().apply { timeInMillis = user.getLong("dateOfBirth") ?: 0L }.toDDMMYYYY()
        holder.genderView.text = when (user.getString("gender")) {
            "m" -> "Male"
            "f" -> "Female"
            else -> "Other"
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }
}




















class UserItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val nameView: TextView = view.findViewById(R.id.nameView)
    val dobView: TextView = view.findViewById(R.id.dobView)
    val genderView: TextView = view.findViewById(R.id.genderView)
}

fun Calendar.toDDMMYYYY(): String {
    return "${get(Calendar.DAY_OF_MONTH)} / ${get(Calendar.MONTH) + 1} / ${get(Calendar.YEAR)}"
}









