package com.example.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverter.data.Valute
import com.example.currencyconverter.recycler.ValuteAdapter
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    companion object {
        //сохранение url и листа валют на уровне приложения
        val url = "https://www.cbr-xml-daily.ru/daily_json.js"
        var valuteList = listOf<Valute>()
        val EDIT_TEXT_KEY = "EDIT_TEXT_KEY"
    }

    //переменные для View
    lateinit var recyclerView: RecyclerView
    lateinit var editText: EditText
    lateinit var materialToolbar: MaterialToolbar
    lateinit var updateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        //находим нужные окна
        recyclerView = findViewById(R.id.recycler)
        editText = findViewById(R.id.editTextNumberDecimal)
        materialToolbar = findViewById(R.id.toolBar)
        updateText = findViewById(R.id.update_text)

        //восстановление состояния, если было сохранено
        if (savedInstanceState != null) {
            editText.setText(savedInstanceState.getDouble(EDIT_TEXT_KEY).toString())
        }

        //создание нового адаптера и настройка RecyclerView
        if (valuteList.size == 0) {
            //если список валют приложения пуст - создаётся новый список
            ValuteAdapter.setAdapterAndValues(this, url, getDouble(editText))
        } else {
            //если список есть - используется текущий
            ValuteAdapter.setAdapterAndList(this, valuteList)
        }

        //слушатель для кнопки меню "Обновить"
        materialToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.refresh -> {
                    //создать новый список и новый адаптер
                    ValuteAdapter.setAdapterAndValues(this, url, getDouble(editText))
                }
                else -> {}
            }
            return@setOnMenuItemClickListener true
        }

        //слушатель для поля ввода
        editText.doOnTextChanged { text, start, before, count ->
            if (recyclerView.adapter != null) {
                //поменять данные объектов RV на основании данных в поле для ввода
                (recyclerView.adapter as ValuteAdapter).setValues(getDouble(editText))
            }
        }

    }

    //сохранить состояние
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //сохранение данных из поля для ввода
        outState.putDouble(EDIT_TEXT_KEY, getDouble(editText))
    }

    //получить число из поля для ввода
    fun getDouble(editTex: EditText?): Double {
        try {
            return editText.text.toString().toDouble()
        } catch (e: Exception) {
            //если поле пустое, вернуть 0.0
            return 0.0
        }
    }
}