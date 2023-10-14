package com.example.firestoredatabase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoredatabase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class MainActivity : AppCompatActivity(),DataAdapter.ItemClickListener {
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

        adapter = DataAdapter(data,this)

        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addBtn.setOnClickListener {
            val title = binding.titleET.text.toString()
            val description = binding.descriptionET.text.toString()

            if(title.isNotEmpty() && description.isNotEmpty()){
                addData(title,description)
            }
        }
        fetchData()
    }

    private fun fetchData() {
        dataCollection.get()
            .addOnSuccessListener {
                data.clear()
                for (document in it){
                    val item = document.toObject(Data::class.java)
                    item.id = document.id
                    data.add(item)
                }
            }
    }

    private fun addData(title: String, description: String) {
        val newData = Data(title = title,description = description)
        dataCollection.add(newData)
            .addOnSuccessListener {
                newData.id = it.id
                data.add(newData)
                binding.addBtn.text = "Add"
                adapter.notifyDataSetChanged()
                binding.titleET.text?.clear()
                binding.descriptionET.text?.clear()
                Toast.makeText(this,"Data added successfully",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Data added failed",Toast.LENGTH_SHORT).show()
            }
    }

    override fun onEditItemClick(data: Data) {
       binding.titleET.setText(data.title)
       binding.descriptionET.setText(data.description)
        binding.addBtn.text = "Update"

        binding.addBtn.setOnClickListener {
            val updateTitle = binding.titleET.text.toString()
            val updateDescription = binding.descriptionET.text.toString()

            if (updateTitle.isNotEmpty() && updateDescription.isNotEmpty()){
                val updateData = Data(data.id, updateTitle,updateDescription)

                dataCollection.document(data.id!!).set(updateData)
                    .addOnSuccessListener {
                        binding.titleET.text?.clear()
                        binding.descriptionET.text?.clear()
                        Toast.makeText(this,"Data Updated",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Data Updated Failed",Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDeleteItemClick(data: Data) {
        dataCollection.document(data.id!!).delete()
            .addOnSuccessListener {
                adapter.notifyDataSetChanged()
                fetchData()
                Toast.makeText(this,"Data Deleted",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,"Data Deletion Failed!",Toast.LENGTH_SHORT).show()
            }

    }
}