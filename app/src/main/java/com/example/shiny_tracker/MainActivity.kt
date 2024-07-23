package com.example.shiny_tracker

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var sharedpreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: MyAdapter

    private lateinit var ycountView: TextView
    private lateinit var bcountView: TextView
    private lateinit var rcountView: TextView

    private fun loadItemsFromAsset(context: Context): List<Item> {
        val inputStream = context.assets.open("items.json")
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<Item>>() {}.type
        return Gson().fromJson(reader, type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedpreferences = getSharedPreferences("item_preferences", MODE_PRIVATE)
        val ycount = sharedpreferences.getInt("yellow_count", 0)
        val bcount = sharedpreferences.getInt("blue_count",0)
        val rcount = sharedpreferences.getInt("red_count",0)
        editor = sharedpreferences.edit()


        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView = findViewById(R.id.searchView)
        val editText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(resources.getColor(R.color.white))
        editText.setHintTextColor(resources.getColor(R.color.white))

        ycountView = findViewById(R.id.yellowc)
        bcountView = findViewById(R.id.bluec)
        rcountView = findViewById(R.id.redc)

        ycountView.text = ycount.toString()+"/587"
        bcountView.text = bcount.toString()+"/587"
        rcountView.text = rcount.toString()+"/587"

        searchView.setOnClickListener{
            searchView.isIconified = false
        }

        val items = loadItemsFromAsset(this).mapIndexed { index, item ->
            item.copy(
                yellow = getYellow(index),
                blue = getBlue(index),
                red = getRed(index)
            )
        }

        adapter = MyAdapter(items, this)
        recyclerView.adapter = adapter


        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun getYellow(position: Int): String {
        return sharedpreferences.getString("yellow_$position", "Off") ?: "Off"
    }

    private fun setYellow(position: Int, status: String) {
        editor.putString("yellow_$position", status)
        editor.apply()
    }

    private fun getBlue(position: Int): String {
        return sharedpreferences.getString("blue_$position", "Off") ?: "Off"
    }

    private fun setBlue(position: Int, status: String) {
        editor.putString("blue_$position", status)
        editor.apply()
    }

    private fun getRed(position: Int): String {
        return sharedpreferences.getString("red_$position", "Off") ?: "Off"
    }

    private fun setRed(position: Int, status: String) {
        editor.putString("red_$position", status)
        editor.apply()
    }

    private fun setYcount(count: Int) {
        editor.putInt("yellow_count",count)
        editor.apply()
        ycountView.text = sharedpreferences.getInt("yellow_count", 0).toString()+"/587"
    }

    private fun setBcount(count: Int) {
        editor.putInt("blue_count",count)
        editor.apply()
        bcountView.text = sharedpreferences.getInt("blue_count", 0).toString()+"/587"
    }

    private fun setRcount(count: Int) {
        editor.putInt("red_count",count)
        editor.apply()
        rcountView.text = sharedpreferences.getInt("red_count", 0).toString()+"/587"
    }

    inner class MyAdapter(private val itemList: List<Item>, private val activity: MainActivity) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
        private val filteredList = mutableListOf<Item>()

        init {
            filteredList.addAll(itemList)
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
            val textView1: TextView = itemView.findViewById(R.id.textView1)
            val yellow: ImageView = itemView.findViewById(R.id.yellow)
            val blue: ImageView = itemView.findViewById(R.id.blue)
            val red: ImageView = itemView.findViewById(R.id.red)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = filteredList[position]
            val Rposition = itemList.indexOf(item)

            val iconResId = holder.itemView.context.resources.getIdentifier("icon$Rposition", "drawable", holder.itemView.context.packageName)
            if (iconResId != 0) {
                holder.imageView.setImageResource(iconResId)
            } else {
                holder.imageView.setImageResource(R.drawable.icon0)
            }

            if(item.yellow == "On") {
                holder.yellow.setImageResource(R.drawable.yellow)
            } else {
                holder.yellow.setImageResource(R.drawable.white)
            }

            if(item.blue == "On") {
                holder.blue.setImageResource(R.drawable.blue)
            } else {
                holder.blue.setImageResource(R.drawable.white)
            }

            if(item.red == "On") {
                holder.red.setImageResource(R.drawable.red)
            } else {
                holder.red.setImageResource(R.drawable.white)
            }


            holder.textView1.text = item.name

            holder.yellow.setOnClickListener {
                val newStatus = if (item.yellow == "Off") "On" else "Off"
                item.yellow = newStatus
                setYellow(Rposition, newStatus)
                if(newStatus == "On") {
                    holder.yellow.setImageResource(R.drawable.yellow)
                    activity.setYcount(activity.sharedpreferences.getInt("yellow_count",0)+1)
                } else {
                    holder.yellow.setImageResource(R.drawable.white)
                    activity.setYcount(activity.sharedpreferences.getInt("yellow_count",0)-1)
                }
            }

            holder.blue.setOnClickListener {
                val newStatus = if (item.blue == "Off") "On" else "Off"
                item.blue = newStatus
                setBlue(Rposition, newStatus)
                if(newStatus == "On") {
                    holder.blue.setImageResource(R.drawable.blue)
                    activity.setBcount(activity.sharedpreferences.getInt("blue_count",0)+1)
                } else {
                    holder.blue.setImageResource(R.drawable.white)
                    activity.setBcount(activity.sharedpreferences.getInt("blue_count",0)-1)
                }
            }

            holder.red.setOnClickListener {
                val newStatus = if (item.red == "Off") "On" else "Off"
                item.red = newStatus
                setRed(Rposition, newStatus)
                if(newStatus == "On") {
                    holder.red.setImageResource(R.drawable.red)
                    activity.setRcount(activity.sharedpreferences.getInt("red_count",0)+1)
                } else {
                    holder.red.setImageResource(R.drawable.white)
                    activity.setRcount(activity.sharedpreferences.getInt("red_count",0)-1)
                }
            }
        }

        override fun getItemCount() = filteredList.size

        val filter: android.widget.Filter = object : android.widget.Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults = mutableListOf<Item>()
                val query = constraint.toString().toLowerCase().trim()

                if(query.isEmpty()) {
                    filteredResults.addAll(itemList)
                } else {
                    itemList.forEach{
                        if (it.realname.toLowerCase().contains(query)) {
                            filteredResults.add(it)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredResults
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                filteredList.addAll(results?.values as List<Item>)
                notifyDataSetChanged()
            }
        }
    }
}