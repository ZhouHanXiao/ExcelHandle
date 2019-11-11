import excelUtil.PoiRead;
import excelUtil.PoiWrite;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ErrorHandle {
    public static void main(String[] args){
        Date now = new Date();
        String filePathDate = new SimpleDateFormat("yyyyMMdd").format(now);
        String filePathEnd = new SimpleDateFormat("yyyy年MM月dd日_HH时").format(now);
        System.out.println(filePathDate);

        sberrHandle(filePathDate,filePathEnd);
//        sberrTwoHandle(filePathDate,filePathEnd);
        kkerrHandle(filePathDate,filePathEnd);
        lbjcHandle(filePathDate,filePathEnd);
//        dkfpErrHandle(filePathDate,filePathEnd);
//        sbsjtbHandle(filePathDate,filePathEnd);
    }

    public static void sberrHandle(String filePathDate,String filePathEnd){
        String oldFilePath = "D:\\异常信息\\" + filePathDate + "\\err.xls";
        List<List<String>> dataList = PoiRead.read(oldFilePath);
        PoiWrite.write(dataList,"D:\\异常信息\\" + filePathDate + "\\申报情况监控信息_" + filePathEnd);
    }

    public static void kkerrHandle(String filePathDate,String filePathEnd){
        String oldFilePath = "D:\\异常信息\\" + filePathDate + "\\kkerr.xls";
        List<List<String>> dataList = PoiRead.readKkyc(oldFilePath);
        PoiWrite.write(dataList,"D:\\异常信息\\" + filePathDate + "\\扣款失败_" + filePathEnd);
    }

    public static void lbjcHandle(String filePathDate,String filePathEnd){
        String oldFilePath = "D:\\异常信息\\" + filePathDate + "\\lbjcerr.xls";
        List<List<String>> dataList = PoiRead.readlbjcyc(oldFilePath);
        PoiWrite.write(dataList,"D:\\异常信息\\" + filePathDate + "\\漏报检查异常信息列表_" + filePathEnd);
    }

    public static void sberrTwoHandle(String filePathDate,String filePathEnd){
        String oldFilePath = "D:\\异常信息\\" + filePathDate + "\\sberrtwo.xls";
        List<List<String>> dataList = PoiRead.readSberrTwo(oldFilePath);
        PoiWrite.write(dataList,"D:\\异常信息\\" + filePathDate + "\\申报情况监控信息(当天解决—申报状态为申报成功)_" + filePathEnd);
    }

    public static void dkfpErrHandle(String filePathDate,String filePathEnd){
        String[] oldfileName = {"dkfperr.xls","xxfperr.xls","jxfperr.xls","tyjderr.xls"};
        String[] fileName = {"代开发票采集信息","销项发票采集信息","进项发票采集信息","通用机打发票采集信息"};
//        String[] fileName = {"通用机打发票采集信息"};
//        String[] oldfileName = {"tyjderr.xls"};
        for(int i = 0;i<oldfileName.length;i++){
            String oldFilePath = "D:\\异常信息\\" + filePathDate + "\\" + oldfileName[i];
            List<List<String>> dataList = PoiRead.readfperr(oldFilePath);
            PoiWrite.write(dataList,"D:\\异常信息\\" + filePathDate + "\\" + fileName[i] + "_采集失败_" + filePathEnd);
        }
    }

    public static void sbsjtbHandle(String filePathDate,String filePathEnd){
        String oldFilePath = "D:\\异常信息\\" + filePathDate + "\\sbsjtberr.xls";
        List<List<String>> dataList = PoiRead.readByFlag(oldFilePath,"sbsjtb");
        PoiWrite.write(dataList,"D:\\异常信息\\" + filePathDate + "\\申报数据同步信息_失败_" + filePathEnd);
    }
}
