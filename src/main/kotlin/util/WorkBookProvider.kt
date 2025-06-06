package util

import org.apache.poi.xssf.usermodel.XSSFWorkbook

class WorkBookProvider {

    fun provideXSSFWorkbook() : XSSFWorkbook {
        return XSSFWorkbook()
    }
}