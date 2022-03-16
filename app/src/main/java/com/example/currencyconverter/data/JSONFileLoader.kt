package com.example.currencyconverter.data

import android.widget.Toast
import com.example.currencyconverter.MainActivity
import com.example.currencyconverter.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.net.URL

class JSONFileLoader {

    companion object {
        //дата последнего обновления
        var updateDate: String = ""

        //получить список валют
        fun getListOfValute(url: String, activity: MainActivity): List<Valute> {
            try {
                //попробовать загрузить по URL или из файла
                val text = readTextFromURLorFile(url, activity)
                safeTextToFileAndCleanDir(text, activity.filesDir)
                return JSONReader.getListOfValute(text)
            } catch (e: Exception) {
                //в ином случае вывести сообщение и загрузить дефолтные значения
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(activity, activity.getString(R.string.load_url_and_str_ex), Toast.LENGTH_SHORT).show()
                }
                updateDate = Valute.defaultDate
                return Valute.defaultList
            }
        }

        //получить текст по URL или из файла
        fun readTextFromURLorFile(url: String?, activity: MainActivity): String {
            var result: String
            try {
                //попробовать прочитать URL и вывести сообщение
                result = URL(url).readText()
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(activity, activity.getString(R.string.load_norm), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                //в ином случае загрузить из файла и вывести сообщение
                result =
                    getJSONFileFromFilesDir(activity)!!.readText()
                CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(activity, activity.getText(R.string.load_url_ex), Toast.LENGTH_SHORT).show()
                    }}
            //дата обоновления читается из текста
            updateDate = readDateFromText(result)
            return result
        }

        //прочитать дату обновления из текста
        fun readDateFromText(text: String): String {
            val result = JSONObject(text).getString("Date")
            return result.substring(0, result.length - 6)
        }

        //получить сохранённый JSON-файл из памяти
        fun getJSONFileFromFilesDir(activity: MainActivity): File? {
            try {
                return activity.filesDir.listFiles().get(0)
            } catch (e: Exception) {
                return null
            }
        }

        //сохранить файл в директорию, предварительно её очистив
        fun safeTextToFileAndCleanDir(text: String, parentPath: File?): Boolean {
            try {
                val file = File(parentPath, readDateFromText(text))
                parentPath!!.listFiles().forEach {
                    it.delete()
                }
                file.writeText(text)
                file.setWritable(false)
                return file.createNewFile()
            } catch (e: Exception) {
                return false
            }
        }
    }

    class JSONReader {

        companion object {
            //получить список валют из JSON-текста
            fun getListOfValute(string: String): List<Valute> {
                val result = mutableListOf<Valute>()
                val jsonObject = JSONObject(string).getJSONObject("Valute")
                JSONObject(string).getJSONObject("Valute").keys().forEach {
                    jsonObject.getJSONObject(it).apply {
                        result.add(getValute(this))
                    }
                }
                return result
            }

            //перевод JSON-объекта в Valute
            fun getValute(jsonObject: JSONObject): Valute {
                return Valute(
                    jsonObject.getString("CharCode"),
                    jsonObject.getString("Name"),
                    jsonObject.getDouble("Value"),
                    jsonObject.getDouble("Previous")
                )
            }
        }
    }
}

