package excelUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * Poi写Excel
 *
 * @author jianggujin
 *
 */
public class PoiWrite
{
    public static void main(String[] args) throws IOException
    {
        // 创建工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet = workbook.createSheet("sheet1");

        for (int row = 0; row < 10; row++)
        {
            HSSFRow rows = sheet.createRow(row);
            for (int col = 0; col < 10; col++)
            {
                // 向工作表中添加数据
                rows.createCell(col).setCellValue("data" + row + col);
            }
        }
        File xlsFile = new File("poi.xls");
        FileOutputStream xlsStream = new FileOutputStream(xlsFile);
        workbook.write(xlsStream);
    }

    public static void write(List<List<String>> writeList,String filePath){
        // 创建工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet = workbook.createSheet("sheet1");
        for(int row=0;row<writeList.size();row++){
            HSSFRow rows = sheet.createRow(row);
            sheet.setColumnWidth(0,1500);
            sheet.setColumnWidth(1,7000);
            sheet.setColumnWidth(2,7000);
            sheet.setColumnWidth(3,7000);
            sheet.setColumnWidth(4,2000);
            for (int col = 0; col < writeList.get(row).size(); col++){
//                sheet.setColumnWidth(col,3000);
                rows.createCell(col).setCellValue(writeList.get(row).get(col));
            }
        }
        File xlsFile = new File(filePath + ".xls");
        FileOutputStream xlsStream = null;
        try {
            xlsStream = new FileOutputStream(xlsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            workbook.write(xlsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}