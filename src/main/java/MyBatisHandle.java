import dao.TpYcclHzMapper;
import entity.TpYcclHz;
import entity.TpYcclHzExample;
import excelUtil.ReadExcel;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @Auther: ZAZ
 * @Date: 2020/1/11 11:45
 * @Description:
 */
public class MyBatisHandle {

    public static void main(String[] args){
        test();
    }

    public static SqlSessionFactory getSqlSessionFactory(){
        String resource = "mybatis-config.xml";
        InputStream inputStream = MyBatisHandle.class.getClassLoader().getResourceAsStream(resource);
        return  new SqlSessionFactoryBuilder().build(inputStream);
    }


    public static void test(){
        // 1、获取sqlSessionFactory对象
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        // 2、获取sqlSession对象
        SqlSession openSession = sqlSessionFactory.openSession();
        try{
            TpYcclHzMapper mapper = openSession.getMapper(TpYcclHzMapper.class);
            TpYcclHz tpYcclHz = mapper.selectByPrimaryKey("123");
            System.out.println(tpYcclHz.getUuid());

        }catch (Exception e){
            e.getStackTrace();
        }finally {
            openSession.close();
        }

    }


    /**
     *  处理Excel数据
     * @param dataList
     * @param yclx 异常类型：01-申报异常；02-申报两次以上异常；03-扣款异常；04-数据同步异常；05-发票异常；08-漏报检查异常
     */
    public static void handleExcelData(List<List<String>> dataList, String yclx,Integer nf,Integer yf){
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession openSession = sqlSessionFactory.openSession();
        try{
            TpYcclHzMapper mapper = openSession.getMapper(TpYcclHzMapper.class);
            for(int i=0;i<dataList.size();i++){
                List<String> cols = dataList.get(i);
                if("02".equals(yclx) && cols.size() == 12){
                    TpYcclHzExample example = new TpYcclHzExample();
                    example.createCriteria()
                            .andSsdqEqualTo(cols.get(0))
                            .andFwjgmcEqualTo(cols.get(1))
                            .andQymcEqualTo(cols.get(2))
                            .andDzbdmcEqualTo(cols.get(3))
                            .andSkssqqEqualTo(cols.get(4))
                            .andSkssqzEqualTo(cols.get(5))
                            .andNsqxEqualTo(cols.get(6))
                            .andSbyyEqualTo(cols.get(10))
                            .andYclxEqualTo(yclx).andYxbzEqualTo("Y")
                            .andSsnfEqualTo(nf).andSsyfEqualTo(yf);
                    example.setOrderByClause("CREATETIME DESC");
                    List<TpYcclHz> res = mapper.selectByExample(example);
                    if(res != null && res.size()>0){
                        TpYcclHz hz = res.get(0);
                        if(hz.getCljg() != null && !"".equals(hz.getCljg())){
                            cols.add(hz.getCljg());
                        }
                        if(hz.getBz() != null && !"".equals(hz.getBz())){
                            cols.add(hz.getBz());
                        }
                    }
                }
            }
        }finally {
            openSession.close();
        }
    }

    /**
     * excel导入数据库
     * @param filePath
     * @param yclx
     * @param nf
     * @param yf
     */
    public static void importExcel(String filePath, String yclx,Integer nf, Integer yf){
        File file = new File(filePath);
        if(file.exists() && !file.isDirectory()){
            ReadExcel readExcel = new ReadExcel();
            List excellist = readExcel.readExcel(file);
            List<TpYcclHz> ycclHzs = new ArrayList<TpYcclHz>();
            if(excellist != null && excellist.size()>0){
                for(int i=0;i<excellist.size();i++){
                    List row = (List) excellist.get(i);
                    if(row != null && row.size()>12){
                        if("地区".equals((String) row.get(0))){
                            continue;
                        }
                        if("02".equals(yclx)){//申报两次异常
                            TpYcclHz tmp = new TpYcclHz();
                            tmp.setUuid(uuid());
                            tmp.setSsdq((String) row.get(0));
                            tmp.setFwjgmc((String) row.get(1));
                            tmp.setQymc((String) row.get(2));
                            tmp.setDzbdmc((String) row.get(3));
                            tmp.setSkssqq((String) row.get(4));
                            tmp.setSkssqz((String) row.get(5));
                            tmp.setNsqx((String) row.get(6));
                            tmp.setSbyy((String) row.get(10));
                            tmp.setClfs((String) row.get(11));
                            tmp.setCljg((String) row.get(12));//处理结果或者备注
                            if(row.size()>13){
                                tmp.setBz((String) row.get(13));//备注  有可能没有
                            }
                            tmp.setYclx(yclx);
                            tmp.setYxbz("Y");
                            tmp.setSsnf(nf);
                            tmp.setSsyf(yf);
                            tmp.setCreateTime(new Date());
                            ycclHzs.add(tmp);
                        }
                    }
                }
            }
            //开始去重
            if(ycclHzs.size()>0){
                System.out.println("-----开始去重，去重前数据 "+ycclHzs.size()+" 条-----");
                removeDuplication(ycclHzs,nf,yf,yclx);
                System.out.println("-----去重结束，去重后剩余数据 "+ycclHzs.size()+" 条,开始批量入库-----");
                try {
                    addBatchModel(ycclHzs);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println("---------文件不存在----------");
            }
        }
    }



    /**
     * 导入去重
     * @param list
     */
    public static void removeDuplication(List<TpYcclHz> list, Integer nf, Integer yf, String yclx){
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession openSession = sqlSessionFactory.openSession();
        try{
            TpYcclHzMapper mapper = openSession.getMapper(TpYcclHzMapper.class);
            int cfCount = 0;
            for (int i=0;i<list.size();i++) {
                TpYcclHz hz = list.get(i);
                TpYcclHzExample example = new TpYcclHzExample();
                example.createCriteria().andSsdqEqualTo(hz.getSsdq()).andFwjgmcEqualTo(hz.getFwjgmc()).andQymcEqualTo(hz.getQymc())
                        .andDzbdmcEqualTo(hz.getDzbdmc()).andSkssqqEqualTo(hz.getSkssqq()).andSkssqzEqualTo(hz.getSkssqz())
                        .andNsqxEqualTo(hz.getNsqx()).andSbyyEqualTo(hz.getSbyy()).andCljgEqualTo(hz.getCljg()).andSsnfEqualTo(nf)
                        .andSsyfEqualTo(yf).andYxbzEqualTo("Y").andYclxEqualTo(yclx);
                List<TpYcclHz> tpYcclHzs = mapper.selectByExample(example);
                if(tpYcclHzs != null && tpYcclHzs.size()>0){
                    list.remove(hz);
                    cfCount++;
                    i--;
                }
            }
            System.out.println("------发现重复数据 "+cfCount+" 条-----");
        }catch (Exception e){
            e.getStackTrace();
        }finally {
            openSession.close();
        }
    }

    /**
     * 批量入库
     * @param list
     */
    public static void addBatchModel(List<TpYcclHz> list) throws SQLException {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession openSession = sqlSessionFactory.openSession();
        Connection connection = null;
        try {
            connection = openSession.getConnection();
            connection.setAutoCommit(false);
            TpYcclHzMapper mapper = openSession.getMapper(TpYcclHzMapper.class);
            mapper.insertBatchModel(list);
            connection.commit();
        }catch (Exception e){
            System.out.println("---------发生异常，回滚--------");
            connection.rollback();
            e.getStackTrace();
        }finally {
            if(connection != null){
                connection.close();
            }
            openSession.close();
        }

    }


    public static String uuid() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }
}
