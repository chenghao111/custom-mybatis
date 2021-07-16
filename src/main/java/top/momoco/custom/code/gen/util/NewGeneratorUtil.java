package top.momoco.custom.code.gen.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * mybatis    编码规范文件生成
 *
 * @author chenghao65
 * @create 2020/11/24
 * @since 1.0.0
 */
public class NewGeneratorUtil {
    public static List<IntrospectedTable> tableDetails = new ArrayList<>();
    // generatorConfig模板路径
    private static String generatorConfig_vm = "/template/generatorConfig.vm";
    public static String package_pojo = PropertiesFileUtil.getInstance("code").get("pojoPackage");
    private static String package_mapper = PropertiesFileUtil.getInstance("code").get("mapperPackage");
    private static String package_manager = PropertiesFileUtil.getInstance("code").get("managerPackage");
    private static String package_service = PropertiesFileUtil.getInstance("code").get("serviceInterfacePackage");
    public static String package_query = PropertiesFileUtil.getInstance("code").get("queryPackage");
    private static String package_controller = PropertiesFileUtil.getInstance("code").get("controllerPackage");
    private static String package_manager_impl = PropertiesFileUtil.getInstance("code").get("managerImplPackage");
    private static String package_vo = PropertiesFileUtil.getInstance("code").get("voPackage");
    private static String package_dto = PropertiesFileUtil.getInstance("code").get("dtoPackage");
    private static String package_common = PropertiesFileUtil.getInstance("code").get("commonPackage");
    private static String JDBC_DRIVER = PropertiesFileUtil.getInstance("code").get("generator.jdbc.driver");
    private static String JDBC_URL = PropertiesFileUtil.getInstance("code").get("generator.jdbc.url");
    private static String JDBC_USERNAME = PropertiesFileUtil.getInstance("code").get("generator.jdbc.username");
    private static String JDBC_PASSWORD = PropertiesFileUtil.getInstance("code").get("generator.jdbc.password");
    String basePath = System.getProperty("user.dir");
    String writePath = basePath + File.separator + "temp";

    public static String getPackage(String pathConfig) {
        return pathConfig.split(",")[0];
    }

    public static String getPackagePath(String pathConfig) {
        return pathConfig.split(",")[0].replace(".", File.separator);
    }

    public static String getModelPackage(String pathConfig) {
        return pathConfig.split(",")[1];
    }

    /**
     * @param author       作者
     * @param database     数据库
     * @param tablePrefix  模糊查询表前缀  eg： n_%
     * @param selectTables 具体的多个精准匹配的表
     * @param writeFlag    是否直接写入工程
     * @throws Exception
     */
    public void generator(
            String author,
            String database,
            String tablePrefix,
            List<String> selectTables, boolean writeFlag) throws Exception {
        // 本项目包地址
        System.setProperty("author", author);
        String os = System.getProperty("os.name");
        String targetProject = "";
        if (!new File(writePath).exists()) {
            new File(writePath).mkdirs();
        }
        String pojoPath = basePath + File.separator + "temp" + File.separator + package_pojo.split(",")[0].replace(".", File.separator);
        String mapperPath = basePath + File.separator + "temp" + File.separator + package_mapper.split(",")[0].replace(".", File.separator);
        String generatorConfigXml = writePath + File.separator + "generatorConfig.xml";
        targetProject = basePath + targetProject;
        String sql = "SELECT table_name,table_comment FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = '" + database + "' AND table_name LIKE '" + tablePrefix + "';";
        //code 代码压缩
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        List<Map<String, Object>> tables = new ArrayList<>();
        Map<String, String> tableComment = new HashMap<>();
        String ctime = new SimpleDateFormat("yyyy/M/d").format(new Date());
        // 生成mybatis代码配置模板
        genMybatisConfig(sql, selectTables, tablePrefix, tables, tableComment, generatorConfigXml, targetProject);
        // 生成表代码mapper
        genTableCode(generatorConfigXml);
        //压缩表代码 删除基础表信息
        zipTable(tables, pojoPath, mapperPath, zip, writeFlag);
        //生成通用代码
        genCommonCode(tables, ctime, author, zip, writeFlag);
        System.out.println("========== 开始压缩代码 ==========");
        FileOutputStream fileOutputStream = new FileOutputStream(basePath + File.separator + "code.zip");
        IoUtil.write(fileOutputStream, true, outputStream.toByteArray());
        System.out.println("========== 结束压缩代码 ==========");
        System.out.println("========== 代码生成结束 ==========");
    }

    /***
     * 调用MyBatisGenerator 生成表信息
     * @param generatorConfigXml
     */
    private void genTableCode(String generatorConfigXml) throws Exception {
        System.out.println("========== 开始运行MybatisGenerator ==========");
        List<String> warnings = new ArrayList<>();
        File configFile = new File(generatorConfigXml);
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        System.out.println("========== 结束运行MybatisGenerator ==========");
    }

    /***
     * 压缩table信息
     * @param tables
     * @param pojoPath
     * @param mapperPath
     * @param zip
     * @throws Exception
     */
    private void zipTable(List<Map<String, Object>> tables, String pojoPath, String mapperPath, ZipOutputStream zip, boolean writeFlag) throws Exception {
        for (Map<String, Object> map : tables) {
            String pojoName = pojoPath + File.separator + map.get("model_name") + ".java";
            String mapperName = mapperPath + File.separator + map.get("model_name") + "Mapper.xml";
            if (writeFlag) {
                //copy 文件到目标文件
                String pojoModelName = getModelPackage(package_pojo);
                String mapperModelPackage = getModelPackage(package_mapper);
                String wPath = File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
                String xmlPath = File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator+"mapper"+File.separator;
                FileUtil.copy(pojoName, basePath + File.separator + pojoModelName + wPath + getPackagePath(package_pojo) + File.separator + map.get("model_name") + ".java", true);
                FileUtil.copy(mapperName, basePath + File.separator + mapperModelPackage + xmlPath  + map.get("model_name") + "Mapper.xml", true);
            }
            zip.putNextEntry(new ZipEntry("entity" + File.separator + map.get("model_name") + ".java"));
            String pojoString = FileUtil.readString(pojoName, StandardCharsets.UTF_8);
            IOUtils.write(pojoString, zip);
            FileUtil.del(pojoName);
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("mapper" + File.separator + map.get("model_name") + "Mapper.xml"));
            String mapperString = FileUtil.readString(mapperName, StandardCharsets.UTF_8);
            IOUtils.write(mapperString, zip);
            zip.closeEntry();
            FileUtil.del(mapperName);
        }
    }

    /***
     * 生成config
     * @param sql
     * @param lastInsertIdTables
     * @param tablePrefix
     * @param tables
     * @param tableComment
     * @param generatorConfigXml
     * @param targetProject
     * @throws Exception
     */
    private void genMybatisConfig(String sql, List<String> lastInsertIdTables, String tablePrefix, List<Map<String, Object>> tables, Map<String, String> tableComment,
                                  String generatorConfigXml, String targetProject) throws Exception {
        System.out.println("========== 开始生成generatorConfig.xml文件 ==========");
        VelocityContext context = new VelocityContext();
        Map<String, Object> table;
        // 查询定制前缀项目的所有表
        JdbcUtil jdbcUtil = new JdbcUtil(JDBC_DRIVER, JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
        List<Map> result = jdbcUtil.selectByParams(sql, null);
        for (Map map : result) {
            table = new HashMap<>(3);
            table.put("table_name", map.get("TABLE_NAME"));
            //  如果指定的table 不为null 则只生成指定的table
            if (lastInsertIdTables != null) {
                if (!lastInsertIdTables.contains(map.get("TABLE_NAME"))) {
                    continue;
                }
            }
            String pre = tablePrefix.replace("%", "");
            String modelName = StringUtil.lineToHump(((String) map.get("TABLE_NAME")).replace(pre, ""));
            table.put("model_name", modelName);
            table.put("comment", map.get("TABLE_COMMENT"));
            tableComment.put(modelName, ObjectUtils.toString(map.get("TABLE_COMMENT")));
            tables.add(table);
        }
        jdbcUtil.release();
        context.put("tables", tables);
        String m_package_pojo = package_pojo.split(",")[0];
        String m_package_mapper = package_mapper.split(",")[0];
        context.put("generator_javaModelGenerator_targetPackage", m_package_pojo);
        context.put("generator_sqlMapGenerator_targetPackage", m_package_mapper);
        context.put("targetProject", targetProject + File.separator + "temp");
        context.put("targetProject_sqlMap", targetProject + File.separator + "temp");
        context.put("generator_jdbc_url", StringEscapeUtils.escapeXml(JDBC_URL));
        context.put("generator_jdbc_driver", JDBC_DRIVER);
        context.put("generator_jdbc_password", JDBC_PASSWORD);
        context.put("generator_jdbc_username", JDBC_USERNAME);
        context.put("last_insert_id_tables", lastInsertIdTables);
        //  生成自定义 pojo 以及xml
        VelocityUtil.generate(generatorConfig_vm, generatorConfigXml, context);
        System.out.println("========== 结束生成generatorConfig.xml文件 ==========");

    }



    /**
     * 生成通用代码
     *
     * @param tables
     * @param ctime
     * @param author
     * @throws Exception
     */
    private void genCommonCode(List<Map<String, Object>> tables, String ctime, String author, ZipOutputStream zip, boolean writeFlag) throws Exception {
        System.out.println("========== 开始生成业务代码 ==========");
        for (int i = 0; i < tables.size(); i++) {
            String model = ObjectUtils.toString(tables.get(i).get("model_name"));
            String serviceOut = writePath + File.separator + model + "Service.java";
            String controllerOut = writePath + File.separator + model + "Controller.java";
            String serviceImplOut = writePath + File.separator + model + "ServiceImpl.java";
            String managerImplOut = writePath + File.separator + model + "ManagerImpl.java";
            String managerOut = writePath + File.separator + model + "Manager.java";
            String daoOut = writePath + File.separator + model + "Dao.java";
            String queryOut = writePath + File.separator + model + "Query.java";
            String wPath = File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
            // 生成 controller
            VelocityContext context = new VelocityContext();
            context.put("table", StringUtil.toLowerCaseFirstOne(model));
            context.put("Table", model);
            context.put("cdate", ctime);
            context.put("author", author);
            context.put("package_pojo", getPackage(package_pojo));
            context.put("package_query", getPackage(package_query));
            context.put("package_service", getPackage(package_service));
            context.put("package_controller", getPackage(package_controller));
            context.put("package_manager", getPackage(package_manager));
            context.put("package_mapper", getPackage(package_mapper));
            context.put("package_manager_impl", getPackage(package_manager_impl));
            context.put("package_vo", getPackage(package_vo));
            context.put("package_dto", getPackage(package_dto));
            context.put("package_common", getPackage(package_common));
            if (writeFlag) {
                String serviceOutDes = basePath + File.separator + getModelPackage(package_service) + wPath + getPackagePath(package_service) + File.separator + model + "Service.java";
                String controllerOutDes = basePath + File.separator + getModelPackage(package_controller) + wPath + getPackagePath(package_controller) + File.separator + model + "Controller.java";
                String serviceImplOutDes = basePath + File.separator + getModelPackage(package_service) + wPath + getPackagePath(package_service) +File.separator+ "impl" + File.separator + model + "ServiceImpl.java";
                String managerOutDes = basePath + File.separator + getModelPackage(package_manager) + wPath + getPackagePath(package_manager) + File.separator + model + "Manager.java";
                String managerImplOutDes = basePath + File.separator + getModelPackage(package_manager_impl) + wPath + getPackagePath(package_manager_impl) + File.separator + model + "ManagerImpl.java";
                String daoOutDes = basePath + File.separator + getModelPackage(package_mapper) + wPath + getPackagePath(package_mapper) + File.separator + model + "Dao.java";
                String queryOutDes = basePath + File.separator + getModelPackage(package_query) + wPath + getPackagePath(package_query) + File.separator + model + "Query.java";
                VelocityUtil.generate("/template/Controller.vm", controllerOut, context, zip, controllerOutDes);
                VelocityUtil.generate("/template/Service.vm", serviceOut, context, zip, serviceOutDes);
                VelocityUtil.generate("/template/ServiceImpl.vm", serviceImplOut, context, zip, serviceImplOutDes);
                VelocityUtil.generate("/template/Manager.vm", managerOut, context, zip, managerOutDes);
                VelocityUtil.generate("/template/ManagerImpl.vm", managerImplOut, context, zip, managerImplOutDes);
                VelocityUtil.generate("/template/Dao.vm", daoOut, context, zip, daoOutDes);
                VelocityUtil.generate("/template/Query.vm", queryOut, context, zip, queryOutDes);
            }else{
                VelocityUtil.generate("/template/Controller.vm", controllerOut, context, zip);
                VelocityUtil.generate("/template/Service.vm", serviceOut, context, zip);
                VelocityUtil.generate("/template/ServiceImpl.vm", serviceImplOut, context, zip);
                VelocityUtil.generate("/template/Manager.vm", managerOut, context, zip);
                VelocityUtil.generate("/template/ManagerImpl.vm", managerImplOut, context, zip);
                VelocityUtil.generate("/template/Dao.vm", daoOut, context, zip);
                VelocityUtil.generate("/template/Query.vm", queryOut, context, zip);
            }



        }
        System.out.println("========== 结束生成code ==========");
    }



}
