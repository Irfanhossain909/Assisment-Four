package com.example.firestoredatabase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoredatabase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity() : AppCompatActivity(), DataAdapter.ItemClickListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val db = FirebaseFirestore.getInstance()
    private val dataCollection = db.collection("data")
    private val data = mutableListOf<Data>()
    private lateinit var adapter: DataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        adapter = DataAdapter(data, this)
        binding.recicalerview.adapter = adapter
        binding.recicalerview.layoutManager = LinearLayoutManager(this)

        binding.addBtn.setOnClickListener {
            val id = binding.idEtxt.text.toString()
            val name = binding.nameEtxt.text.toString()
            val email = binding.emailEtxt.text.toString()
            val subject = binding.subjectEtxt.text.toString()
            val birthday = binding.birthEtxt.text.toString()

            if (id.isNotEmpty() && name.isNotEmpty() && email.isNotEmpty() && subject.isNotEmpty() && birthday.isNotEmpty()) {
                // Check if we are adding or updating
                if (binding.addBtn.text == "ADD") {
                    addData(id, name, email, subject, birthday)
                } else {
                    updateData(id, name, email, subject, birthday)
                }
            }
        }
        fetchData()
    }

    private fun fetchData() {
        dataCollection.get()
            .addOnSuccessListener { querySnapshot ->
                data.clear()
                for (document in querySnapshot) {
                    val item = document.toObject(Data::class.java)
                    item.id = document.id
                    data.add(item)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Data fetch failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addData(id: String, name: String, email: String, subject: String, birthday: String) {
        val newData = Data(id = null, name = name, email = email, subject = subject, birthday = birthday)
        dataCollection.add(newData)
            .addOnSuccessListener { documentReference ->
                newData.id = documentReference.id
                data.add(newData)
                adapter.notifyDataSetChanged()
                clearInputFields()
                Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Data add failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateData(id: String, name: String, email: String, subject: String, birthday: String) {
        val updateData = Data(id, name, email, subject, birthday)
        dataCollection.document(id)
            .set(updateData)
            .addOnSuccessListener {
                clearInputFields()
                binding.addBtn.text = "ADD"
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Data updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Data update failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputFields() {
        binding.idEtxt.text?.clear()
        binding.nameEtxt.text?.clear()
        binding.emailEtxt.text?.clear()
        binding.subjectEtxt.text?.clear()
        binding.birthEtxt.text?.clear()
    }

    override fun onEditItemClick(data: Data) {
        binding.idEtxt.setText(data.id)
        binding.nameEtxt.setText(data.name)
        binding.emailEtxt.setText(data.email)
        binding.subjectEtxt.setText(data.subject)
        binding.birthEtxt.setText(data.birthday)
        binding.addBtn.text = "Update"
    }

    override fun onDeleteItemClick(data: Data) {
        dataCollection.document(data.id!!)
            .delete()
            .addOnSuccessListener {
                data.remove(data)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Data Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Data deletion failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
