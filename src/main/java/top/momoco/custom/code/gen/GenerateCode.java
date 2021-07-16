package top.momoco.custom.code.gen;

import top.momoco.custom.code.gen.util.NewGeneratorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础代码----生成器
 *
 * @author chenghao65
 * @create 2020/11/18
 * @since 1.0.0
 */
public class GenerateCode {
    /**
     * 要查询的库
     */
    private static String DATABASE = "dataBaseName";
    /***
     * 需要生成的表前缀
     */
    private static String TABLE_PREFIX = "pre_%";
     //需要insert后返回主键的表配置，key:表名,value:主键名
    private static List<String> selectTables = new ArrayList<>() ;

    static {
        // 需要生成的表数据  不在该列表中 就不生成数据  为null 则生成全部
        selectTables.add("tableName");
    }


    // 生成代码入口
    public static void main(String[] args) throws Exception {
        NewGeneratorUtil util = new NewGeneratorUtil() ;
        util.generator( "chenghao65", DATABASE, TABLE_PREFIX,selectTables,false);
    }

}
