package com.fx.zfcar.util

import android.content.Context
import java.io.IOException
import java.io.InputStreamReader

object  AssetFileReader {
    /**
     * 【基础版】读取assets下TXT文件的全部内容（小文件推荐）
     * @param context 上下文（Activity/Fragment/Application）
     * @param fileName assets下的文件名称（如"test.txt"）
     * @return 文本内容（读取失败返回空字符串）
     */
    fun readTxtFile(context: Context, fileName: String): String {
        val stringBuilder = StringBuilder()
        try {
            // 打开assets文件输入流
            context.assets.open(fileName).use { inputStream ->
                // 按字符流读取（解决中文乱码问题）
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val buffer = CharArray(1024)
                    var length: Int
                    // 循环读取内容
                    while (reader.read(buffer).also { length = it } != -1) {
                        stringBuilder.append(buffer, 0, length)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // 读取失败返回空字符串，也可根据需求抛异常/返回null
            return ""
        }
        return stringBuilder.toString()
    }
}