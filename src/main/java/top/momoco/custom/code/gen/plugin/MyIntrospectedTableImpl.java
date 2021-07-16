package top.momoco.custom.code.gen.plugin;

import top.momoco.custom.code.gen.util.IterablesUtil;
import top.momoco.custom.code.gen.util.NewGeneratorUtil;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3SimpleImpl;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.ResultMapWithoutBLOBsElementGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description sql 重写
 * @ClassName MyIntrospectedTableImpl
 * @Author chenghao65
 * @Date 2020/11/24
 * @Since 1.0.0
 */
public class MyIntrospectedTableImpl extends IntrospectedTableMyBatis3SimpleImpl {


    public static boolean isNotExists(String path, String fileName) {
        String bn = System.getProperty("user.dir");
        String file = bn + "." + path;
        String pathName = file.replace(".", "/").replace("\\", "/") + "/" + fileName;
        File f = new File(pathName);
        return !f.exists();
    }

    /***
     * 工具类
     * @param xmlElement
     * @param name
     * @param value
     */
    public static void getAttribute(XmlElement xmlElement, String name, String value) {
        xmlElement.addAttribute(new Attribute(name, value));
    }

    /***
     * 增加自定义 sql
     * @return
     */
    @Override
    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        NewGeneratorUtil.tableDetails.add(xmlMapperGenerator.getIntrospectedTable()) ;
        List<GeneratedXmlFile> xml = new ArrayList<>();
        if (this.xmlMapperGenerator != null) {
            Document document = new Document("-//mybatis.org//DTD Mapper 3.0//EN", "http://mybatis.org/dtd/mybatis-3-mapper.dtd");
            XmlElement answer = new XmlElement("mapper");
            String namespace = xmlMapperGenerator.getIntrospectedTable().getMyBatis3SqlMapNamespace();
            namespace = namespace.replace("Mapper","Dao") ;
            answer.addAttribute(new Attribute("namespace", namespace));
            AbstractXmlElementGenerator elementGenerator = new ResultMapWithoutBLOBsElementGenerator(true);
            elementGenerator.setContext(this.context);
            elementGenerator.setIntrospectedTable(xmlMapperGenerator.getIntrospectedTable());
            elementGenerator.addElements(answer);
            this.context.getCommentGenerator().addRootComment(answer);
            document.setRootElement(answer);
            XmlElement rootElement = document.getRootElement();
            List<XmlElement> sqlMapElement = getSqlMapElement(xmlMapperGenerator.getIntrospectedTable());
            sqlMapElement.forEach(x -> {
                rootElement.addElement(x);
            });
            GeneratedXmlFile gxf = new GeneratedXmlFile(document, getMyBatis3XmlMapperFileName(), getMyBatis3XmlMapperPackage(),
                    context.getSqlMapGeneratorConfiguration().getTargetProject(), false, context.getXmlFormatter());
            if (context.getPlugins().sqlMapGenerated(gxf, this)) {
                xml.add(gxf);
            }
        }
        return xml;
    }


    protected List<XmlElement> getSqlMapElement(IntrospectedTable introspectedTable) {
        List<XmlElement> xmlElementList = new ArrayList<>();
        xmlElementList.add(addBaseField(introspectedTable));
        xmlElementList.add(addQueryCondition(introspectedTable));
        xmlElementList.add(addUpdateCondition(introspectedTable));
        xmlElementList.add(addPrimaryKeyQuery(introspectedTable));
        xmlElementList.add(addListQuery(introspectedTable));
        xmlElementList.add(addPageListQuery(introspectedTable));
        xmlElementList.add(addCountQuery(introspectedTable));
        xmlElementList.add(addInsert(introspectedTable, false));
        xmlElementList.add(addInsert(introspectedTable, true));
        xmlElementList.add(addUpdate(introspectedTable));
        xmlElementList.add(addDeleteById(introspectedTable));
        return xmlElementList;
    }


    /***
     * update xml
     * @param introspectedTable
     * @return
     */
    private XmlElement addUpdate(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("update");
        getAttribute(xmlElement, "id", "update");
        getAttribute(xmlElement, "parameterType", NewGeneratorUtil.getPackage(NewGeneratorUtil.package_pojo)+"."+introspectedTable.getTableConfiguration().getDomainObjectName());
        String text = "  update " +introspectedTable.getTableConfiguration().getTableName() + "\n" +
                "        <include refid=\"updateCondition\"/>\n" +
                "        where id = #{id}";
        TextElement textElement = new TextElement(text);
        xmlElement.addElement(textElement);
        return xmlElement;
    }

    /**
     * @param introspectedTable
     * @return
     */
    private XmlElement addPageListQuery(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("select");
        getAttribute(xmlElement, "parameterType", NewGeneratorUtil.getPackage(NewGeneratorUtil.package_query)+"."+introspectedTable.getTableConfiguration().getDomainObjectName() + "Query");
        getAttribute(xmlElement, "resultMap", "BaseResultMap");
        getAttribute(xmlElement, "id", "queryForPage");
        String text = "  select\n" +
                "        <include refid=\"fields\"/>\n" +
                "        from " +introspectedTable.getTableConfiguration().getTableName() + "\n" +
                "        <include refid=\"condition\"/>\n" +
                "        ${sort} limit #{startRow}, #{endRow}";

        xmlElement.addElement(new TextElement(text));
        return xmlElement;
    }

    private XmlElement addListQuery(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("select");
        getAttribute(xmlElement, "parameterType", NewGeneratorUtil.getPackage(NewGeneratorUtil.package_query)+"."+introspectedTable.getTableConfiguration().getDomainObjectName() + "Query");
        getAttribute(xmlElement, "resultMap", "BaseResultMap");
        getAttribute(xmlElement, "id", "queryForList");

        String text = "  select\n" +
                "       <include refid=\"fields\"/>\n" +
                "       from " +introspectedTable.getTableConfiguration().getTableName() + "\n" +
                "       <include refid=\"condition\"/>\n" +
                "       ${sort}";

        xmlElement.addElement(new TextElement(text));
        return xmlElement;
    }


    /***
     * insert   batchInsert xml
     * @param introspectedTable
     * @return
     */
    private XmlElement addInsert(IntrospectedTable introspectedTable, boolean batchFlag) {

        XmlElement xmlElement = new XmlElement("insert");
        if (!batchFlag) {
            getAttribute(xmlElement, "id", "insert");
            getAttribute(xmlElement, "parameterType", NewGeneratorUtil.getPackage(NewGeneratorUtil.package_pojo)+"."+introspectedTable.getTableConfiguration().getDomainObjectName());
        } else {
            getAttribute(xmlElement, "id", "batchInsert");
            getAttribute(xmlElement, "useGeneratedKeys", "true");
        }
        String text = "insert into " + introspectedTable.getTableConfiguration().getTableName() + "  ( ";
        StringBuilder columns = new StringBuilder();
        StringBuilder fields = new StringBuilder();
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        IterablesUtil.forEach(allColumns, (index, column) -> {
            String end = ",";
            if (index == allColumns.size() - 1) {
                end = " )";
            }
            columns.append(column.getActualColumnName()).append(end);
            if (!batchFlag) {
                fields.append("#{" + column.getJavaProperty() + "}").append(end);
            } else {
                fields.append("#{item." + column.getJavaProperty() + "}").append(end);

            }
        });
        String id = "  SELECT @@IDENTITY\n" +
                "      AS id";
        if (!batchFlag) {
            text = text + columns.toString() + " values ( " + fields.toString();
            XmlElement sleKey = new XmlElement("selectKey");
            getAttribute(sleKey, "resultType", "long");
            getAttribute(sleKey, "keyProperty", "id");
            sleKey.addElement(new TextElement(id));
            xmlElement.addElement(new TextElement(text));
            xmlElement.addElement(sleKey);
        } else {
            XmlElement foreachXmlElement = new XmlElement("foreach");
            getAttribute(foreachXmlElement, "collection", "list");
            getAttribute(foreachXmlElement, "item", "item");
            getAttribute(foreachXmlElement, "index", "index");
            getAttribute(foreachXmlElement, "separator", ",");
            text = text + columns.toString() + " values  ";
            foreachXmlElement.addElement(new TextElement("( " + fields.toString()));
            xmlElement.addElement(new TextElement(text));
            xmlElement.addElement(foreachXmlElement);
        }
        return xmlElement;
    }

    /***
     * count xml 生成
     * @param introspectedTable
     * @return
     */
    private XmlElement addCountQuery(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("select");
        getAttribute(xmlElement, "id", "queryForCount");
        String query = NewGeneratorUtil.getPackage(NewGeneratorUtil.package_query)+"."+introspectedTable.getTableConfiguration().getDomainObjectName() + "Query";
        getAttribute(xmlElement, "parameterType", query);
        getAttribute(xmlElement, "resultType", "Integer");
        String text = "     select\n" +
                "      count(1)\n" +
                "      from " +introspectedTable.getTableConfiguration().getTableName() + "\n" +
                "      <include refid = \"condition\" /> ";
        TextElement textElement = new TextElement(text);
        xmlElement.addElement(textElement);
        return xmlElement;
    }

    /**
     * 删除xml
     *
     * @param introspectedTable
     * @return
     */
    private XmlElement addDeleteById(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("delete");
        getAttribute(xmlElement, "id", "deleteById");
        String text = "delete from " + introspectedTable.getTableConfiguration().getTableName() + " where id = #{id}";
        TextElement textElement = new TextElement(text);
        xmlElement.addElement(textElement);
        return xmlElement;
    }


    /**
     * 主键查询
     *
     * @param introspectedTable
     * @return
     */
    private XmlElement addPrimaryKeyQuery(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("select");
        getAttribute(xmlElement, "id", "getEntityById");
        getAttribute(xmlElement, "parameterType", "Long");
        getAttribute(xmlElement, "resultMap", "BaseResultMap");

        String text = "  select\n" +
                "      <include refid=\"fields\"/>\n" +
                "      from " + introspectedTable.getTableConfiguration().getTableName() + "\n" +
                "      where id = #{id}";
        TextElement textElement = new TextElement(text);
        xmlElement.addElement(textElement);
        return xmlElement;
    }

    /***
     * 生成 更新选择项
     * @param introspectedTable
     * @return
     */
    private XmlElement addUpdateCondition(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("sql");
        getAttribute(xmlElement, "id", "updateCondition");
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        XmlElement setElement = new XmlElement("set");
        IterablesUtil.forEach(allColumns, (index, x) -> {
            XmlElement ifFlag = new XmlElement("if");
            getAttribute(ifFlag, "test", x.getJavaProperty() + "!=null");
            String end = " ,";
            if (index == allColumns.size() - 1) {
                end = " ";
            }
            String andText = "  " + x.getActualColumnName() + " = #{" + x.getJavaProperty() + "} " + end;
            TextElement textElement = new TextElement(andText);
            ifFlag.addElement(textElement);
            setElement.addElement(ifFlag);
        });
        xmlElement.addElement(setElement);
        return xmlElement;

    }

    /**
     * 生成 queryCondition  xml
     *
     * @param introspectedTable
     * @return
     */
    private XmlElement addQueryCondition(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("sql");
        getAttribute(xmlElement, "id", "condition");
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        XmlElement trimElement = new XmlElement("trim");
        getAttribute(trimElement, "prefix", "where");
        getAttribute(trimElement, "prefixOverrides", "and|or");

        allColumns.stream().forEach(column -> {
            XmlElement ifFlag = new XmlElement("if");
            getAttribute(ifFlag, "test", column.getJavaProperty() + "!=null");
            String andText = " and " + column.getActualColumnName() + " = #{" + column.getJavaProperty() + "}";
            TextElement textElement = new TextElement(andText);
            ifFlag.addElement(textElement);
            trimElement.addElement(ifFlag);
        });
        xmlElement.addElement(trimElement);
        return xmlElement;
    }

    /**
     * 生成 fields 节点
     *
     * @param introspectedTable
     * @return
     */
    private XmlElement addBaseField(IntrospectedTable introspectedTable) {
        XmlElement xmlElement = new XmlElement("sql");
        getAttribute(xmlElement, "id", "fields");
        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        StringBuilder builder = new StringBuilder();
        allColumns.stream().forEach(x -> {
            builder.append(x.getActualColumnName()).append(",");
        });
        String fields = builder.toString();
        TextElement selStr = new TextElement(fields.substring(0, fields.length() - 1));
        xmlElement.addElement(selStr);
        return xmlElement;
    }


    /***
     *  拼接where
     * @param introspectedTable
     * @return
     */
    private XmlElement getWhere(IntrospectedTable introspectedTable) {

        List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
        XmlElement xmlElement = new XmlElement("where");
        allColumns.stream().forEach(column -> {
            XmlElement ifFlag = new XmlElement("if");
            getAttribute(ifFlag, "test", column.getJavaProperty() + " !=null");
            String andText = " and " + column.getActualColumnName() + " = #{" + column.getJavaProperty() + ", jdbcType=" + column.getJdbcTypeName() + "}";
            TextElement textElement = new TextElement(andText);
            ifFlag.addElement(textElement);
            xmlElement.addElement(ifFlag);
        });
        return xmlElement;

    }

}
