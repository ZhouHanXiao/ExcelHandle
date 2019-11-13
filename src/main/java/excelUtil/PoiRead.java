package excelUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Poi写Excel
 *
 * @author jianggujin
 *
 */
public class PoiRead
{
    public static final int MONTH = Integer.parseInt(new SimpleDateFormat("MM").format(new Date())) ;

    public static void main(String[] args) throws IOException,
            InvalidFormatException
    {
        File xlsFile = new File("D:\\err.xls");
        // 获得工作簿
        Workbook workbook = WorkbookFactory.create(xlsFile);
        // 获得工作表个数
        int sheetCount = workbook.getNumberOfSheets();
        // 遍历工作表
        for (int i = 0; i < sheetCount; i++)
        {
            Sheet sheet = workbook.getSheetAt(i);
            // 获得行数
            int rows = sheet.getLastRowNum() + 1;
            // 获得列数，先获得一行，在得到改行列数
            Row tmp = sheet.getRow(0);
            if (tmp == null)
            {
                continue;
            }
            int cols = tmp.getPhysicalNumberOfCells();
            // 读取数据
            for (int row = 0; row < rows; row++)
            {
                Row r = sheet.getRow(row);
                for (int col = 0; col < cols; col++)
                {
                    System.out.printf("%10s", r.getCell(col).getStringCellValue());
                }
                System.out.println();
            }
        }
    }

    /**
     * 根据标志分类异常信息
     * @param filePath 异常文件路径
     * @param errorFlag 异常文件类别
     * @return
     */
    public static List<List<String>> readByFlag(String filePath,String errorFlag){
        List<List<String>> retList = new ArrayList<List<String>>();
        Sheet sheet = getErrorSheet(filePath);
        // 获得行数
        int rows = sheet.getLastRowNum() + 1;
        // 获得列数，先获得一行，在得到改行列数
        Row tmp = sheet.getRow(0);

        int cols = tmp.getPhysicalNumberOfCells();
        String bbmc =  "";
        for (int row = 0; row < rows; row++) {
            Row r = sheet.getRow(row);
            List rowList = new ArrayList<String>();
            String handlWay = "";
            bbmc = "";
            String dq = "";
            for (int col = 0; col < cols; col++) {
                String cell = "";
                try {
                    cell = r.getCell(col).getStringCellValue();
                } catch (Exception e) {
                    cell = r.getCell(col).getNumericCellValue() + "";
                }
                if(col == 0){
                    dq = cell;
                }
                if(errorFlag == "sbsjtb" && col == 9){
                    bbmc = r.getCell(3).getStringCellValue();
                    handlWay = sbsjtbread(cell,dq,row);
                }
                rowList.add(cell);
            }
            if(row == 0){
                rowList.add("处理方式");
                rowList.add("处理结果");
                rowList.add("备注");
            }
            rowList.add(handlWay);
            retList.add(rowList);
        }
        return retList;
    }

    /**
     * 申报数据同步异常分类
     * @param cell
     * @param dq
     * @param row
     * @return
     */
    private static String sbsjtbread(String cell, String dq,int row) {
        String handlWay = "";
        if(isCAError(cell)){
            handlWay = getCAHandleMan(row, cell);
        }else {
            handlWay = getErrorHanleWay(dq);
        }
        return handlWay;
    }

    /**
     * 发票采集异常分类
     * @param filePath
     * @return
     */
    public static List<List<String>> readfperr(String filePath){
        List<List<String>> retList = new ArrayList<List<String>>();
        Sheet sheet = getErrorSheet(filePath);
        // 获得行数
        int rows = sheet.getLastRowNum() + 1;
        // 获得列数，先获得一行，在得到改行列数
        Row tmp = sheet.getRow(0);

        int cols = tmp.getPhysicalNumberOfCells();
        String bbmc =  "";
        for (int row = 0; row < rows; row++){
            Row r = sheet.getRow(row);
            List rowList = new ArrayList<String>();
            String handlWay = "";
            bbmc = r.getCell(3).getStringCellValue();
            String dq = "";
            for (int col = 0; col < cols; col++){
                String cell = "";
                try{
                    cell = r.getCell(col).getStringCellValue();
                }catch (Exception e){
                    cell = r.getCell(col).getNumericCellValue() + "";
                }
                if(col == 0){
                    dq = cell;
                }
                if(col == 6){
                    if(isCAError(cell)){
                        handlWay = getCAHandleMan(row, cell);
                    }else if(isHulve(cell)){
                        handlWay = "忽略";
                    }else {
                        handlWay = getErrorHanleWay(dq);
                    }
                }
                rowList.add(cell);
            }
            if(row == 0){
                rowList.add("处理方式");
                rowList.add("处理结果");
                rowList.add("备注");
            }
            rowList.add(handlWay);
            retList.add(rowList);
        }
        return retList;
    }

    private static String getCAHandleMan(int row, String cell) {
        String handlWay;
        if (row % 5 == 2) {
            handlWay = "柏辰," + cell;
        } else {
            if (row % 5 == 1) {
                handlWay = "曹津梁," + cell;
            } else {
                handlWay = "刘耀阳," + cell;
            }
        }
        if(cell.contains("延期")){
            handlWay = "霍利，核实处理";
        }
        return handlWay;
    }

    /**
     * 申报异常，重发两次之后还是异常分类
     * @param filePath
     * @return
     */
    public static List<List<String>> readSberrTwo(String filePath){
        List<List<String>> retList = new ArrayList<List<String>>();

        Sheet sheet = getErrorSheet(filePath);
        int rows = sheet.getLastRowNum() + 1;
        // 获得列数，先获得一行，在得到改行列数
        Row tmp = sheet.getRow(0);

        int cols = tmp.getPhysicalNumberOfCells();
        String bbmc =  "";
        for (int row = 0; row < rows; row++){
            Row r = sheet.getRow(row);
            List rowList = new ArrayList<String>();
            String handlWay = "";
            bbmc = r.getCell(3).getStringCellValue();
            for (int col = 0; col < cols; col++){
                String cell = "";
                try{
                    cell = r.getCell(col).getStringCellValue();
                }catch (Exception e){
                    cell = r.getCell(col).getNumericCellValue() + "";
                }
                if(col == 0){
                    handlWay = getErrorHanleWay(cell) + "(当天解决-申报状态为申报成功)";
                }
                rowList.add(cell);
            }
            if(bbmc.contains("个人所得税生产经营所得纳税申报表") || bbmc.contains("扣缴所得税报告表")){
                handlWay = "郝战海，核实处理";
            }
            if(row == 0){
                rowList.add("处理方式");
                rowList.add("处理结果");
                rowList.add("备注");
            }
            rowList.add(handlWay);
            retList.add(rowList);
        }
        return retList;
    }

    /**
     * 申报异常分类
     * @param filePath
     * @return
     */
    public static List<List<String>> read(String filePath){
        List<List<String>> retList = new ArrayList<List<String>>();
        Sheet sheet = getErrorSheet(filePath);
        // 获得行数
        int rows = sheet.getLastRowNum() + 1;
        // 获得列数，先获得一行，在得到改行列数
        Row tmp = sheet.getRow(0);
        int cols = tmp.getPhysicalNumberOfCells();
        String bbmc =  "";
        for (int row = 0; row < rows; row++)
        {
            Row r = sheet.getRow(row);
            List rowList = new ArrayList<String>();
            String handlWay = "";
            String dq = "";
            bbmc = r.getCell(4).getStringCellValue();
            for (int col = 0; col < cols; col++)
            {
                String cell = "";
                try{
                    cell = r.getCell(col).getStringCellValue();

                }catch (Exception e){
                    cell = r.getCell(col).getNumericCellValue() + "";
                }
                rowList.add(cell);
                if(col == 0){
                    dq  = cell;
                }
                if (col == 13){
//                    System.out.println("cell = " + cell);
                    if(bbmc.contains("个人所得税生产经营所得纳税申报表") || bbmc.contains("扣缴所得税报告表")){
                        if(cell.contains("密码将会锁定120分钟，请谨慎使用") || cell.contains("金三客户端返回的信息为8至20位") || cell.contains("当前还未设置密码")){
                            handlWay = "刘娟，告知客户";
                        }else if(cell.contains("金三客户端返回的信息为服务器通信异常")
                                ||cell.contains("系统异常，请稍后重试")
                                ||cell.contains("网络异常，未收到反馈信息")
                                ||cell.contains("更新人员信息发生错误")
                                ||cell.contains("正常工资薪金初始化失败")
                                ||cell.contains("请按照税款申报进行申报")
                                ||cell.contains("请稍后进行“人员报送”获取最新的反馈信")
                                ||cell.contains("SB-E-0003-调用接口失败")
                                ||cell.contains("人员信息报送获取反馈失败，金三返回的异常信息为调用接口失败")
                        ){
                            handlWay = "郝战海，重发";
                        }else if(isCAError(cell)){
                            handlWay = getCAHandleMan(row,cell);
                        }
                        else {
                            handlWay = "郝战海，核实处理";
                        }
                    }
                    if(handlWay != ""){
                        continue;
                    }
                    if(isRepost(cell)){
                        handlWay = "周晓阳，重发";
                    }else if(isCAError(cell)){
                        handlWay = getCAHandleMan(row, cell);
                    }else if (isHulve(cell)){
                        handlWay = "忽略";
                    }else if(cell.contains("企业应申报清册未找到财务报表信息,可能是税种认定发生变更，请至客户管理中重发企业信息采集")
                    ){
                        handlWay = "夏倩倩，重发基本信息采集，跟进";
                    }else if(cell.contains("企业应申报清册未找到财务报表信息,可能是税种认定发生变更，请至客户管理中重发企业信息采集")
                    ){
                        handlWay = "俞娣，重发基本信息采集，跟进";
                    }else if(isProgramerHandle(cell)){
                        handlWay = getErrorHanleWay(dq);
                    }else if(isProducterHandl(cell)){
                        handlWay = getHandlWay(dq);
                    }else if(cell.equals("")){
                        handlWay = "赵明明，核实处理";
                    }
                    if(cell.contains("税局未找到该报表申报页面，请核实是否需要申报") && dq.contains("湖北") && bbmc.contains("财务报表")){
                        handlWay = "忽略";
                    }
                }
            }
            if(handlWay == "" && (bbmc.contains("扣缴所得税报告表")||bbmc.contains("个人所得税生产经营所得纳税申报表"))){
//                System.out.println("handlWay = " + handlWay);
                handlWay = "郝战海，核实处理";
            }
            if(row == 0){
                rowList.add("处理方式");
                rowList.add("处理结果");
                rowList.add("备注");
            }
            rowList.add(handlWay);
            retList.add(rowList);
        }
        return retList;
    }

    /**
     * 扣款异常分类
     * @param filePath 文件路径
     * @return
     */
    public  static List<List<String>> readKkyc(String filePath) {
        List<List<String>> retList = new ArrayList<List<String>>();

        Sheet sheet = getErrorSheet(filePath);
        // 获得行数
        int rows = sheet.getLastRowNum() + 1;
        // 获得列数，先获得一行，在得到改行列数
        Row tmp = sheet.getRow(0);
        int cols = tmp.getPhysicalNumberOfCells();
        String bbmc = "";
        for (int row = 0; row < rows; row++) {
            Row r = sheet.getRow(row);
            List rowList = new ArrayList<String>();
            String handlWay = "";
            String dq = "";
            bbmc = r.getCell(3).getStringCellValue();
            for (int col = 0; col < cols; col++) {
                String cell = "";
                try{
                    cell = r.getCell(col).getStringCellValue();
                }catch (Exception e){
                    cell= r.getCell(col).getNumericCellValue() + "";
                }
                rowList.add(cell);
                if (col == 0) {
                    dq = cell;
                }
                if (col == 12) {
                    if(bbmc.contains("个人所得税生产经营所得纳税申报表") || bbmc.contains("扣缴所得税报告表")
                    ){
                        if(cell.contains("密码将会锁定120分钟，请谨慎使用") || cell.contains("金三客户端返回的信息为8至20位") || cell.contains("当前还未设置密码")){
                            handlWay = "刘娟，告知客户";
                        }else if(cell.contains("金三客户端返回的信息为服务器通信异常")
                                ||cell.contains("系统异常，请稍后重试")
                                ||cell.contains("网络异常，未收到反馈信息")
                                ||cell.contains("更新人员信息发生错误")
                                ||cell.contains("正常工资薪金初始化失败")
                                ||cell.contains("请按照税款申报进行申报")
                                ||cell.contains("请稍后进行“人员报送”获取最新的反馈信")
                                ||cell.contains("SB-E-0003-调用接口失败")
                                ||cell.contains("人员信息报送获取反馈失败，金三返回的异常信息为调用接口失败")
                        ){
                            handlWay = "郝战海，重发";
                        }else if (isCAError(cell)){
                            handlWay = getCAHandleMan(row,cell);
                        }else {
                            handlWay = "郝战海，核实处理";
                        }
                    }else if(isCAError(cell)){
                        handlWay = getCAHandleMan(row,cell);
                    }else if(cell.contains("余额不足")
                            ||cell.contains("没有需要缴款的申报数据")
                            ||cell.contains("验证超过最大次数")
                            ||cell.contains("页面空白")
                    ){
                        handlWay = "周晓阳，重发";
                    }else if(isProducterHandl(cell)){
                        handlWay = getHandlWay(dq);
                    }else if(isProgramerHandle(cell)){
                        handlWay = getErrorHanleWay(dq);
                    }else if(cell.equals("")){
                        handlWay = "赵明明，核实处理";
                    }
                }
            }
            if(row == 0){
                rowList.add("处理方式");
                rowList.add("处理结果");
                rowList.add("备注");
            }
            rowList.add(handlWay);
            retList.add(rowList);
        }
        return retList;
    }

    /**
     * 漏报检查异常分类
     * @param filePath
     * @return
     */
    public  static List<List<String>> readlbjcyc(String filePath) {
        List<List<String>> retList = new ArrayList<List<String>>();
        Sheet sheet = getErrorSheet(filePath);
        // 获得行数
        int rows = sheet.getLastRowNum() + 1;
        // 获得列数，先获得一行，在得到改行列数
        Row tmp = sheet.getRow(0);
        int cols = tmp.getPhysicalNumberOfCells();
        String bbmc = "";
        for (int row = 0; row < rows; row++) {
            Row r = sheet.getRow(row);
            List rowList = new ArrayList<String>();
            String handlWay = "";
            String dq = "";
            for (int col = 0; col < cols; col++) {
                String cell = r.getCell(col).getStringCellValue();
                rowList.add(cell);
                if (col == 0) {
                    dq = cell;
                }
                if (col == 7) {
                    if(isCAError(cell)){
                        handlWay = getCAHandleMan(row,cell);
                    }else if(isRepost(cell)){
                        handlWay = "赵明明，重发";
                    }else if(isProducterHandl(cell)){
                        handlWay = getHandlWay(dq);
                    }else if(isProgramerHandle(cell)){
                        handlWay = getErrorHanleWay(dq);
                    }else if(cell.equals("")){
                        handlWay = "赵明明，核实处理";
                    }
                }
            }
            if(row == 0){
                rowList.add("处理方式");
                rowList.add("处理结果");
                rowList.add("备注");
            }
            rowList.add(handlWay);
            retList.add(rowList);
        }
        return retList;
    }

    /**
     * 根据地区信息分发给开发
     * @param dq
     * @return
     */
    public static String getErrorHanleWay(String dq){
        String handlWay = "";
        if(dq.contains("江苏")){
            handlWay = "周晗，核实处理";
        }else if(dq.contains("宁波")){
            handlWay = "王峰，核实处理";
        }else if(dq.contains("上海")){
            handlWay = "王冲，核实处理";
        }else if(dq.contains("浙江")){
            handlWay = "王冲，核实处理";
        }else if(dq.contains("湖北")){
            handlWay = "严鹏，核实处理";
        }else if(dq.contains("山东")){
            handlWay = "朱阿壮，核实处理";
        }else if(dq.contains("青岛")){
            handlWay = "朱阿壮，核实处理";
        }else if(dq.contains("安徽")){
            handlWay = "杨鹏，核实处理";
        }
        return handlWay;
    }

    /**
     * 根据地区信息分发给产品
     * @param dq
     * @return
     */
    public static String getHandlWay(String dq){
        String handlWay = "";
        if(dq.contains("江苏")){
            handlWay = "俞娣，核实处理";
        }else if(dq.contains("宁波")){
            handlWay = "周晓阳，核实处理";
        }else if(dq.contains("上海")){
            handlWay = "周晓阳，核实处理";
        }else if(dq.contains("浙江")){
            handlWay = "夏倩倩，核实处理";
        }else if(dq.contains("湖北")){
            handlWay = "俞娣，核实处理";
        }else if(dq.contains("山东")){
            handlWay = "濮阳，核实处理";
        }else if(dq.contains("青岛")){
            handlWay = "濮阳，核实处理";
        }else if(dq.contains("安徽")){
            handlWay = "周晓阳，核实处理";
        }
        return handlWay;
    }

    /**
     * 获取Excel中的sheet 1
     * @param filePath
     * @return
     */
    public static Sheet getErrorSheet(String filePath){
        File xlsFile = new File(filePath);
        // 获得工作簿
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(xlsFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return workbook.getSheetAt(0);
    }

    /**
     * 是否是CA异常
     * @param cell
     * @return
     */
    public static boolean isCAError(String cell){
        if(cell.contains("0680")
                ||cell.contains("CA已经过期")
                ||cell.contains("江苏CA")
                ||cell.contains("短信")
                ||cell.contains("系统检测到CA密码录入错误")
                ||cell.contains("国信CA验签异常")
                ||cell.contains("我局金三核心征管系统已于")
                ||cell.contains("和CA中包含企业名称不符合")
                ||cell.contains("与登陆成功后企业名称")
                ||cell.contains("与登录成功后企业名称")
                ||cell.contains("SB-E-0003-错误提示CertApi")
                ||cell.contains("密码窗口无法关闭")
                ||cell.contains("证书列表窗口无法关闭")
                ||cell.contains("系统打开CA")
        ){
            return true;
        }
        return false;
    }

    /**
     * 是否是需要忽略的异常
     * @param cell
     * @return
     */
    public static boolean isHulve(String cell){
        if(cell.contains("该企业绑定的验证手机号不在税局短信验证列表中")
                ||cell.contains("该企业用户没有权限登录电子税务局")
                ||cell.contains("未在电子税务局开户")
                ||cell.contains("无有效的纳税人登记信息")
                ||cell.contains("金三核心征管系统已于4月1日完成升级")
                ||cell.contains("密码错误")
                ||cell.contains("您的身份证号码不符合规则")
                ||cell.contains("您没有密码")
                ||cell.contains("信息0000")
                ||cell.contains("社会信用代码或密码错误")
                ||cell.contains("无法查询办税信息，请确认税务状态是否正常")
                ||cell.contains("系统打开CA错误，请确认CA是否被拔")
                ||cell.contains("信息纳税人识别号与密码不匹配")
                ||cell.contains("信息无该纳税人的注册信息")
                ||cell.contains("选择的账期已经结账")
                ||cell.contains("SYS-QZSB-0001")
                ||cell.contains("当前纳税人不存在有就地分摊二级分支机构")
                ||cell.contains("税局不可编辑，未能填入，请确认申报表数据")
                ||cell.contains("若是零申报请设为零申报，程序会填0.01")
                ||cell.contains("未获取到进项信息")
                ||cell.contains("与税费种认定中的【认定有效期】不符")
        ){
            return true;
        }
        return false;
    }

    /**
     * 是否是产品处理的异常
     * @param cell
     * @return
     */
    public static boolean isProducterHandl(String cell){
        if(cell.contains("以下数据填写完毕后比对不一致")
                || cell.contains("税局没有匹配到报表名称")
                || cell.contains("指标值检查错误")
                || cell.contains("上期未申报")
                || cell.contains("你企业尚未进行财务会计制度备案或原备案不准确")
                || cell.contains("=不允许提交")
                || cell.contains("正式申报校验失败")
                || cell.contains("适用会计制度不匹配")
                || cell.contains("数据不一致，本系统")
                || cell.contains("你企业填报的【货物及劳务】列第8行应<【货物及劳务】列第7行，请确认并修改")
                || cell.contains("税局无法填写货物相关栏次，请确认是否有对应税种认定")
                || cell.contains("必须大于")
                || cell.contains("不能申报文化事业建设费")
                || cell.contains("报表未鉴定")
                || cell.contains("申报失败,申报比对异常")
                || cell.contains("不应享受该减免")
                || cell.contains("保存失败!错误原因")
                || cell.contains("税款所属期不匹配，申报终止")
                || cell.contains("请确认是否有对应税种认定")
                || cell.contains("必须小于")
                || cell.contains("该企业用户没有权限登录电子税务局")
                || cell.contains("本期已缴金额为")
                || cell.contains("发现未兼容的税局申报状态")
                || cell.contains("提示验证码输入错误次数过多")
                || cell.contains("提示验证码错误")
                || (cell.contains("校验出错:") && cell.contains("校验失败"))
                || cell.contains("税局无该项免税项目，请核实")
                || cell.contains("并按照政策规定据实填报")
                || cell.contains("申报信息写征管系统失败，失败原因")
                || cell.contains("增值税申报应纳税额又发生了新的变化")
                || cell.contains("=附列列资料")
                || cell.contains("法人身份证件类型不能为空")
                || cell.contains("应等于")
                || cell.contains("操作时保存失败。信息")
                || cell.contains("申报清册界面没有查询到此申报表")
                || cell.contains("请前往办税大厅申报")
                || cell.contains("表单数据校验异常")
                || cell.contains("税局反馈：调用系统服务出错")
                || cell.contains("不允许申报")
                || cell.contains("对应数据不一致")
                || cell.contains("在税种认定中未找到该报表")
                || cell.contains("税款申报失败!--系统")
                || cell.contains("填写不一致，请修改")
                || cell.contains("纳税人无有效的财务会计制度备案")
                || cell.contains("不配比，请检查申报表")
                || cell.contains("税局未找到该报表申报页面，请核实是否需要申报")
                || cell.contains("且开具增值税专用发票，请填写附列资料")
                || cell.contains("用户或密码错误")
                || cell.contains("以下指标填写完毕后比对不一致")
                || cell.contains("申报报表不一致")
                || cell.contains("保存时电子税务局反馈异常：|| -->利润表")
                || cell.contains("减免性质重复")
                || cell.contains("系统检测到您是新注册用户，请您按如下指引操作方能办理业务")
                || cell.contains("账号或密码错误")
                || cell.contains("应小于等于")
                || cell.contains("应大于等于")
                || cell.contains("登记的电话地址与实际不符，请及时变更后再进行申报")
                || cell.contains("信息无该纳税人的注册信息")
                || cell.contains("未查询到基金费大类")
                || cell.contains("密码错误，剩余")
                || cell.contains("尚未申报")
                || cell.contains("未找到已申报的申报表，请确认是否已申报成功")
                || cell.contains("税费申报及缴纳界面没有查询到申报表")
                || cell.contains("指标值不一致")
                || cell.contains("应小于第")
                || cell.contains("系统已自动发起申报和扣款操作，为避免您重复扣款")
                || cell.contains("应该等于第")
                || cell.contains("该纳税人重复申报")
                || cell.contains("指标值对比失败")
                || cell.contains("必须等于【")
                || cell.contains("您企业未按规定向税务机关报备财务")
                || cell.contains("纳税人登记信息不存在")
                || cell.contains("请先进行居民企业企业所得税核定")
                || cell.contains("we指标值")
                || cell.contains("无当前申报期限选项")
                || cell.contains("该缴纳义务人未进行文化事业建设费信息登记")
                || cell.contains("专用发票通知单注明的进项税额不等于本期在税务机关")
                || cell.contains("征期内未扣款请进入申报作废")
                || cell.contains("]，本系统[")
                || cell.contains("小于主税中")
                || cell.contains("税局未找到减免性质")
                || cell.contains("当前纳税人申报属期内不存在有符合条件的税（费）种认定信息")
                || cell.contains("输入已缴额大于预缴总额")
                || cell.contains("请在办税服务厅申报处理")
                || cell.contains("金额合计不等于")
                || cell.contains("税额不等于")
                || cell.contains("您未维护即征即退标志")
                || cell.contains("没有查询到对应的税费种认定")
                || cell.contains("请核实纳税人资格")
                || cell.contains("界面未查到成功记录， 但")
                || cell.contains("请稍后获取反馈，并确认是否申报成功")
                || cell.contains("结果：提示请填写")
                || cell.contains("不能为空，请重新输入")
                || cell.contains("请确认是否相符")
                || cell.contains("财务报表未申报")
                || cell.contains("请重新输入")
                || cell.contains("密码错误")
                || cell.contains("税款所属期不匹配")
                || cell.contains("税局页面未找到附表")
                || cell.contains("税局不存在此表")
                || cell.contains("请至当地办税服务大厅办理")
                || cell.contains("不能小于")
                ||cell.contains("不能扣款")
                ||cell.contains("与本系统应缴金额")
                ||cell.contains("帐户已冻结")
                ||cell.contains("三方协议不存在")
                ||cell.contains("税额比对不一致")
                ||cell.contains("您当前企业没有三方协议信息")
                ||cell.contains("未签订三方协议")
                ||cell.contains("您尚未签署三方协议")
                ||cell.contains("面无三方协议")
                ||cell.contains("江苏国税涉税查询页面未获取到")
                ||cell.contains("账户状态错")
                ||cell.contains("交易处理失败")
                ||cell.contains("账号、户名不符")
                ||cell.contains("协议不存在或信息有误")
                ||cell.contains("税款不一致")
                ||cell.contains("税务机关与欠税的税款所属税务机关不一致")
                ||cell.contains("无法使用协议扣款")
                ||cell.contains("交易失败")
                ||cell.contains("账户状态不允许扣款")
                ||cell.contains("三方协议未签约")
                ||cell.contains("申报表已经缴款")
                ||cell.contains("在已缴税款页面未查询到该报表的缴款信息")
                ||cell.contains("无法实现网上缴款")
                ||cell.contains("您没有密码")
                ||cell.contains("扣款时发现税局系统未申报")
                ||cell.contains("登记的电话地址与实际不符")
                ||cell.contains("的密码已输错")
                ||cell.contains("请先通过注册功能注册后")
                ||cell.contains("发现未兼容的纳税期限名称")
                ||cell.contains("系统检测到您是新注册用户")
                ||cell.contains("本期已缴税额不一致")
                ||cell.contains("您的附加税也已经自动申报成功")
                ||cell.contains("申报表比对结果不通过")
                ||cell.contains("开具其他发票销售额比对失败")
                ||cell.contains("逾期请至当地办税服务大厅申报")
                ||cell.contains("没有可以选择的基金费大类")
                ||cell.contains("应税凭证不为空")
                ||cell.contains("当前登录账号电子税务局显示企业名称")
        ){
            return true;
        }
        return false;
    }

    /**
     * 是否是开发处理的异常
     * @param cell
     * @return
     */
    public static boolean isProgramerHandle(String cell){
        if(cell.contains("程序异常")
                || cell.contains("超时")
                || cell.contains("执行申报任务时出现问题")
                || cell.contains("填写失败")
                || cell.contains("部分指标金额与税局页面不一致")
                || cell.contains("系统没有带申报数据到客户端")
                || cell.contains("errorNumber")
                || cell.contains("登陆时出现问题")
                || cell.contains("在提交申报页面没有找到")
                || cell.contains("数据出现异常，请点击右侧“问题反馈”按钮")
                || cell.contains("系统内部异常，异常信息")
                || cell.contains("未获取三方协议信息")
                || cell.contains("任务处理异常")
                || cell.contains("正式提交申报时电子税务局反馈异常")
                || cell.contains("HRESULT E_FAIL")
                || cell.contains("其他错误")
                || cell.contains("系统异常")
                || cell.contains("获取税款所属期输入框失败")
                || cell.contains("查询缴款信息时未查到信息")
                || cell.contains("网上申报缴税界面未查询到需要扣款信息")
                || cell.contains("网上缴税界面未查询到需要扣款信息")
                || cell.contains("系统出错或者未配置")
                || cell.contains("您的申报关键信息签名失败")
                || cell.contains("状态不正常")
                || cell.contains("扣款失败系统缴款信息存在延迟")
                || cell.contains("页面空白")
                || cell.contains("】查找加载失败")
                || cell.contains("出现系统级异常")
                || cell.contains("等待报表列表加载失败")
                || cell.contains("未获取到异常原因")
                || cell.contains("确认平台未签名")
                || cell.contains("9999_JSON2")
                || cell.contains("税局页面打开失败，请稍后重新尝试")
                || cell.contains("税局异常信息：未保存成功")
                || cell.contains("获取验证码图片失败")
                || cell.contains("进入首页面失败")
        ){
            return true;
        }
        return false;
    }

    /**
     * 是不是重发的异常
     * @param cell
     * @return
     */
    public static boolean isRepost(String cell){
        if(cell.contains("抄")
                || cell.contains("先申报")
                || cell.contains("先报送")
                || cell.contains("请先完成增值税申报")
                || cell.contains("CA是否被拔出")
                || cell.contains("未找到需要申报的社保信息")
                || cell.contains("进入报税处理功能，并完成上报汇总，完成后再进行增值税申报。")
                || cell.contains("您没有密码，请使用手机号登录以后进行设置")
                || cell.contains("选择企业失败，请检查企业名称是否准确")
                || cell.contains("SB-E-0024-本月应申报界面没有查询到申报表")
                || cell.contains("无有效的纳税人登记信息，请联系电子税务局服务人员")
                || cell.contains("HRESULT E_FAIL")
                || cell.contains("请先完成增值税申报后再申报附加税")
                || cell.contains("系统异常,请联系运维人员")
                || cell.contains("调用金三核心系统超时")
                || cell.contains("数据加载超时")
                || cell.contains("税务网站连接超时")
                || cell.contains("电子税务局返回结果时超时")
                || cell.contains("接口链接超时")
                || cell.contains("等待报表主页加载完成超时")
                || cell.contains("电子税务局验证码图片合成超过最大次数")
                || cell.contains("重复获取验证码超过限定次数")
                || cell.contains("信息0000")
                || cell.contains("社保申报表加载失败")
                || cell.contains("系统出现异常，请联系运维人员")
                || cell.contains("验证码自动识别失败")
                || cell.contains("重新获取超过最大次数")
                || cell.contains("页面空白")
                || cell.contains("新验证超过最大次数")
                || cell.contains("未找到已申报的申报表，请确认是否已申报成功")
                ||cell.contains("余额不足")
                ||cell.contains("没有需要缴款的申报数据")
                ||cell.contains("验证超过最大次数")
        ){
            return true;
        }
        return false;
    }

}