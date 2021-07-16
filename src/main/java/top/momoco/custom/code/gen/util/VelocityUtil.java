package top.momoco.custom.code.gen.util;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class VelocityUtil {

	/**
	 * 根据模板生成文件
	 * @param inputVmFilePath 模板路径
	 * @param outputFilePath 输出文件路径
	 * @param context
	 * @throws Exception
	 */
	public static void generate(String inputVmFilePath, String outputFilePath, VelocityContext context) throws Exception {
		try {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty("resource.loader", "class");
			ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			ve.init();
			Template template = ve.getTemplate(inputVmFilePath, "utf-8");
			File outputFile = new File(outputFilePath);
			FileWriterWithEncoding writer = new FileWriterWithEncoding(outputFile, "utf-8");
			template.merge(context, writer);
			writer.close();
		}catch (ResourceNotFoundException  e){
			Properties properties = new Properties();
			properties.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, getPath(inputVmFilePath));
			Velocity.init(properties);
			Template template = Velocity.getTemplate(getFile(inputVmFilePath), "utf-8");
			File outputFile = new File(outputFilePath);
			FileWriterWithEncoding writer = new FileWriterWithEncoding(outputFile, "utf-8");
			template.merge(context, writer);
			writer.close();
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public static void generate(String inputVmFilePath, String outputFilePath, VelocityContext context, ZipOutputStream zip) throws Exception {
		try {
			String fileName = outputFilePath.substring(outputFilePath.lastIndexOf(File.separator)+1,outputFilePath.length()) ;
			generate(inputVmFilePath,outputFilePath,context);
			zip.putNextEntry(new ZipEntry("result" + File.separator +fileName));
			String pojoString = FileUtil.readString(outputFilePath, StandardCharsets.UTF_8);
			IOUtils.write(pojoString, zip);
			FileUtil.del(outputFilePath);
			zip.closeEntry();
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static void generate(String inputVmFilePath, String outputFilePath, VelocityContext context, ZipOutputStream zip,String  writeOut) throws Exception {
		try {
			String fileName = outputFilePath.substring(outputFilePath.lastIndexOf(File.separator)+1,outputFilePath.length()) ;
			generate(inputVmFilePath,outputFilePath,context);
			zip.putNextEntry(new ZipEntry("result" + File.separator +fileName));
			String pojoString = FileUtil.readString(outputFilePath, StandardCharsets.UTF_8);
			IOUtils.write(pojoString, zip);
			zip.closeEntry();
			FileUtil.copy(outputFilePath,writeOut,true) ;
			FileUtil.del(outputFilePath);
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * 根据文件绝对路径获取目录
	 * @param filePath
	 * @return
	 */
	public static String getPath(String filePath) {
		String path = "";
		if (StringUtils.isNotBlank(filePath)) {
			path = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		}
		return path;
	}

	/**
	 * 根据文件绝对路径获取文件
	 * @param filePath
	 * @return
	 */
	public static String getFile(String filePath) {
		String file = "";
		if (StringUtils.isNotBlank(filePath)) {
			file = filePath.substring(filePath.lastIndexOf("/") + 1);
		}
		return file;
	}

}
