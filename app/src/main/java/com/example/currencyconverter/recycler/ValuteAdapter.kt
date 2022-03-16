package com.example.currencyconverter.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverter.MainActivity
import com.example.currencyconverter.R
import com.example.currencyconverter.data.JSONFileLoader
import com.example.currencyconverter.data.Valute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ValuteAdapter(var valuteList: List<Valute>) :
    RecyclerView.Adapter<ValuteAdapter.ValuteHolder>() {

    companion object {

        //установка адаптера в RecyclerView и настройка списка валют
        fun setAdapterAndValues(activity: MainActivity, url: String, double: Double) {

            //в IO потоке
            CoroutineScope(Dispatchers.IO).launch {
                //получаем список валют
                val valuteList = JSONFileLoader.getListOfValute(url, activity)
                //создаём адаптер и передаём ему список
                val adapter = ValuteAdapter(valuteList)

                //в Main-потоке
                CoroutineScope(Dispatchers.Main).launch {
                    //адаптер настраивает значения для вывода в окнах на переданного значения
                    adapter.setValues(double)
                    //прикрепляем адаптер к RecyclerView
                    activity.recyclerView.adapter = adapter
                    //прикрепляем список валют к контексту приложения (для удобства)
                    MainActivity.valuteList = adapter.valuteList
                    //выводим дату последнего обновления
                    activity.updateText.setText("${activity.getString(R.string.update)} ${JSONFileLoader.updateDate}")
                }
            }
        }

        //установка адаптера в RecyclerView на основании списка валют
        fun setAdapterAndList(activity: MainActivity, list: List<Valute>) {
            activity.recyclerView.adapter = ValuteAdapter(list)
        }
    }

    //последняя полученная цифра из поля ввода
    var lastDouble = 0.0

    //создание ViewHolder для адаптора
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ValuteHolder {
        return ValuteHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.main_recycler_item, parent, false)
        )
    }

    //настройка ViewHolder на основании данных списка валют
    override fun onBindViewHolder(holder: ValuteHolder, position: Int) {
        holder.bind(valuteList[position])
        //слушатель на чек - выключающий поля с данными
        holder.checkBox.setOnCheckedChangeListener { compoundButton, b ->
            //если чек выключен - данные не обновляются
            holder.valute.isChecked = b
            holder.value.isEnabled = b
            holder.previous.isEnabled = b
            //если чек включается - данные должны обновиться сразу
            if (b == true) {
                holder.valute.setValues(lastDouble)
                holder.value.text = String.format("%.3f", holder.valute.valueValue)
                holder.previous.text = String.format("%.3f", holder.valute.valuePrevious)
            }
        }
    }

    override fun getItemCount(): Int {
        return valuteList.size
    }

    //изменить выводимые на экран значения в списке валют и показать их на экране
    fun setValues(double: Double) {
        valuteList.forEach {
            it.setValues(double)
            lastDouble = double
        }
        notifyDataSetChanged()
    }

    //изменить список валют
    fun setList(list: List<Valute>) {
        valuteList = list
        notifyDataSetChanged()
    }

    inner class ValuteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //данные и ссылки у ViewHolder
        lateinit var valute: Valute
        val charCode = itemView.findViewById<TextView>(R.id.charSet)
        val name = itemView.findViewById<TextView>(R.id.name)
        val value = itemView.findViewById<TextView>(R.id.value)
        val previous = itemView.findViewById<TextView>(R.id.previous)
        val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)

        //настройка ViewHolder по данным из дата-класса
        fun bind(valute: Valute) {
            this.valute = valute
            charCode.text = valute.charCode
            name.text = valute.name
            value.text = String.format("%.3f", valute.valueValue)
            previous.text = String.format("%.3f", valute.valuePrevious)
            checkBox.isChecked = valute.isChecked
            value.isEnabled = valute.isChecked
            previous.isEnabled = valute.isChecked
        }
    }
}