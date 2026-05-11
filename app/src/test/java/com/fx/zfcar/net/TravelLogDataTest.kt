package com.fx.zfcar.net

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class TravelLogDataTest {

    @Test
    fun `parses travel log when gettime is a date string`() {
        val json = """
            {
              "list": [],
              "rows": {
                "id": 297,
                "car_id": 49,
                "driver_name": "小面筋",
                "addtime": "2026-04-27",
                "carnum": "鲁Y88888",
                "user_id": 209,
                "type": "0",
                "gotime": "2026-04-26",
                "gettime": "2026-04-28",
                "sresult": "0",
                "gresult": "0",
                "eresult": "0",
                "updatetime": "2026-04-27"
              }
            }
        """.trimIndent()

        val data = Gson().fromJson(json, TravelLogData::class.java)

        assertEquals("2026-04-28", data.rows?.gettime)
    }
}
