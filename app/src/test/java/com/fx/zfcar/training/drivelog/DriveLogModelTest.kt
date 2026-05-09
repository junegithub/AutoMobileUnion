package com.fx.zfcar.training.drivelog

import org.junit.Assert.assertEquals
import org.junit.Test

class DriveLogModelTest {
    @Test
    fun validateStageOneAllowsManualPlateWithoutCarId() {
        val form = DriveCheckConstants.LocalFormData(
            car_id = 0,
            carnum = "鲁A12345",
            driver_name = "张三",
            type = "危险品"
        )

        val result = DriveLogModel().validateStageOne(form)

        assertEquals(null, result)
    }

    @Test
    fun convertToTravelPostRequestKeepsSignatureUrlsAndArrivalTime() {
        val local = DriveCheckConstants.LocalFormData(
            car_id = 0,
            carnum = "鲁A12345",
            driver_name = "张三",
            type = "危险品",
            gettime = "2026-05-09-10:30",
            dsingimg = "https://safe.ezbeidou.com/driver.png",
            ysingimg = "https://safe.ezbeidou.com/copilot.png"
        )

        val request = DriveLogModel().convertToTravelPostRequest(local)

        assertEquals("2026-05-09-10:30", request.gettime)
        assertEquals("https://safe.ezbeidou.com/driver.png", request.dsingimg)
        assertEquals("https://safe.ezbeidou.com/copilot.png", request.ysingimg)
    }
}
