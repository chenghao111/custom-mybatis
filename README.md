# custom-mybatis


[TOC]


### 基础代码生成 集成使用说明

> v1.3.6 修正windows版本正则失败的错误

### 功能点说明

> 基础代码集成工具，可辅助生成mysql相关业务代码。特点如下：
>
> 1 mapper.xml dao,service,manager,controller 接口以及常用实现。
>
> 2 zip代码包生成
>
> 3 支持直接写入代码到对应的模块中
>

#### 集成步骤

##### 1 maven jar引入

> ```
> 最佳实践：scope设置为test
> <dependency>
> <groupId>com.***.code</groupId>
> <artifactId>code-generate</artifactId>
> <version>1.3.6-SNAPSHOT</version>
> <scope>test</scope>
> </dependency>
> ```

##### 2 resources 目录资源下增加code.properties文件

> 因为每个项目对应的配置不一样，因此把部分配置抽离出来适应各项目。
>
> 最佳实践：
>
> 建议在模块test/resources目录下增加该配置文件。配置说明如下：

**配置文件说明**

```
#数据库连接配置 修改对应的数据库配置
generator.jdbc.driver=com.mysql.jdbc.Driver
generator.jdbc.url=jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&socketTimeout=60000
generator.jdbc.username=plus_user
generator.jdbc.password=0603@Plus
#代码包路径设置
#pojo包路径  ,模块名称
pojoPackage=com.***.domain.entity,domain
#Dao包路径  模块名称
mapperPackage=com.***.dao,repository
#manager包路径  ,模块名称
managerPackage=com.***.manager,manager
#managerImpl包路径  模块名称
managerImplPackage=com.***.manager.impl,manager
#service接口包路径   模块名称
serviceInterfacePackage=com.***.service,service
#controller包路径  模块名称
controllerPackage=com.***.controller,web
#query路径  模块名称
queryPackage =com.***.query,domain
#vo 包路径--controller数据接收返回实体  模块名称
voPackage=com.***.vo,domain
#dto 包路径--service数据接收返回实体  模块名称
dtoPackage=com.***.dto,domain
#common 公共包路径   模块名称
commonPackage=com.***.common,common
```

<img src="https://gitee.com/momococo/images/raw/master/img/20210120205021.png" alt="image-20210120205021408" style="zoom:50%;" />

##### 3 方法调用

> ```
> NewGeneratorUtil util = new NewGeneratorUtil();
> util.generator("chenghao111", "test", "n_%", Lists.newArrayList("tableName"), true);
> 注意：最后一个方法参数会直接写入工程对应的包内，覆盖原有文件，谨慎使用。
> ```
>
> 生成code.zip 解压如下图。 写入地址为：工程根目录/code.zip

<img src="https://gitee.com/momococo/images/raw/master/img/20210120211317.png" alt="image-20210120211317794" style="zoom:50%;" />

**调用测试代码**

```
任意类main方法中执行函数调用，如配置文件放在test目录下，请在test测试类新建main方法进行调用。``方法参数说明：共计``5``个参数。参数说明见下面核心代码。
    public static void main(String[] args) throws  Exception {
        NewGeneratorUtil util = new NewGeneratorUtil();
        util.generator("chenghao111", "test", "n_%",Lists.newArrayList("tabelName"), true);
    }
```

**核心方法参数说明**

```
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
        String sql = "SELECT table_name,table_comment FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = '" + database + "' AND table_name LIKE '" + tablePrefix + "';";
        // 生成mybatis代码配置模板
        genMybatisConfig(sql, selectTables, tablePrefix, tables, tableComment, generatorConfigXml, targetProject);
        // 生成表代码mapper
        genTableCode(generatorConfigXml);
        //压缩表代码 删除基础表信息
        zipTable(tables, pojoPath, mapperPath, zip, writeFlag);
        //生成通用代码
        genCommonCode(tables, ctime, author, zip, writeFlag);
        // 生成hmc请求
        genHmcCode(tableComment, zip);
        FileOutputStream fileOutputStream = new FileOutputStream(basePath + File.separator + "code.zip");
        IoUtil.write(fileOutputStream, true, outputStream.toByteArray());
      
    }
注意：最后一个方法参数会覆盖原有文件，谨慎使用。如果不需要直接写入对应模块，最后一个方法参数传入``false` `;
代码生成后，会在项目根目录下生成code.zip，包含本次代码生成的所有代码。
如需要直接写入对应模块，最后一个方法参数传入``true``；写入对应模块的同时，同样会生成code.zip ;
```



#### 常见问题issue


##### 1 写入路径异常，和模块地址不一致。

> ```
> 任意类main方法中执行函数调用，如配置文件放在test目录下，请在test测试类新建main方法进行调用。
> 通过输出System.getProperty("user.dir") 查看目录是否为工程根目录。 
> ```

##### 2 查询异常

> 查询依赖 Query类。
>
> Query类需要复制实体类的所有属性。目前这块还需要手动copy 。后续升级自动copy实体类字段 。 

##### 3 文件直接写入会覆盖原文件

>已有代码，如果有修改相关逻辑，不要直接写入。 

