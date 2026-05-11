package com.fx.zfcar.net

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingOtherInfoTest {
    @Test
    fun parsesUserOtherInfoWithBlankTimestampFields() {
        val json = """
            {
              "id": 1,
              "group_id": 1,
              "username": "qyh",
              "nickname": "qyh",
              "password": "",
              "salt": "",
              "email": "",
              "cardmun": "",
              "mobile": "",
              "type": "",
              "category_id": "3031",
              "avatar": "",
              "avtvartime": "",
              "level": 0,
              "gender": 0,
              "birthday": "",
              "bio": "",
              "money": "0",
              "score": 0,
              "successions": 0,
              "maxsuccessions": 0,
              "prevtime": "",
              "logintime": "",
              "loginip": "",
              "loginfailure": 0,
              "joinip": "",
              "jointime": "",
              "createtime": "",
              "updatetime": "",
              "deletetime": "",
              "token": "",
              "status": "normal",
              "verification": "",
              "yzstatus": "1",
              "stype": "",
              "car_id": "",
              "carnum": "",
              "signtype": "",
              "signtime": "",
              "signfile": "",
              "epidemictype": "",
              "epidemictime": "",
              "epidemicfile": "",
              "jobtype": "",
              "provice_id": "",
              "city_id": "",
              "area_id": "",
              "endtime": "",
              "openid": "",
              "pid": "",
              "cardimg": "",
              "backcardimg": "",
              "practicetime": "",
              "fristpracticetime": "",
              "year": "",
              "jxstatus": "",
              "validtime": "",
              "qualificationId": "",
              "renzhen": "",
              "otherinfo": "",
              "renzhengtime": "",
              "school_id": "",
              "usualtime": "",
              "usualpaytype": "0",
              "usualpaytime": "",
              "check_adminid": "",
              "check_name": ""
            }
        """.trimIndent()

        val info = Gson().fromJson(json, TrainingOtherInfo::class.java)

        assertEquals("3031", info.category_id)
        assertEquals("1", info.yzstatus)
        assertEquals("qyh", info.username)
    }
}
