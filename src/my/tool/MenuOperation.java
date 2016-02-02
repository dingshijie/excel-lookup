package my.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import my.tool.util.Constant.ConfigureKey;

/**
 * 菜单上的操作
 * @author DSJ
 *
 */
public class MenuOperation {

	/**
	 * 读取configurate.properties文件指定的内容
	 * @param key 指定的key
	 * @return
	 */
	public String readConfigureFile(ConfigureKey key) {
		Properties prop = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream("configure.properties");
		try {
			prop.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop.get(key.name()).toString();
	}

	/**
	 * 设置configurate.properties文件内容
	 * @param key 
	 * @param value
	 * @return
	 */
	public void setConfigureFileContent(ConfigureKey key, Object value) {
		Properties prop = new Properties();
		try {
			InputStream in = ClassLoader.getSystemResourceAsStream("configure.properties");
			prop.load(in);
			in.close();
			prop.put(key.name(), value);
			OutputStream os = new FileOutputStream(new File(ClassLoader.getSystemResource("configure.properties")
					.getPath()));
			prop.store(os, "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 读取fieldMatch.properties文件的内容
	 * @return
	 */
	public Properties readFieldMatchFile() {
		Properties prop = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream("fieldMatch.properties");
		Map<String, String> map = null;
		try {
			prop.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop;
	}

	/**
	 * 设置fieldMatch.properties文件内容
	 * @param key 
	 * @param value
	 * @return
	 */
	public void setFieldMatchFileContent(String key, Object value) {
		Properties prop = new Properties();
		try {
			InputStream in = ClassLoader.getSystemResourceAsStream("fieldMatch.properties");
			prop.load(in);
			in.close();
			prop.put(key, value);
			OutputStream os = new FileOutputStream(new File(ClassLoader.getSystemResource("fieldMatch.properties")
					.getPath()));
			prop.store(os, "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 删除fieldMatch.properties文件内容
	 * @param key 
	 * @return
	 */
	public void deleteFieldMatchFileContent(String key) {
		Properties prop = new Properties();
		try {
			InputStream in = ClassLoader.getSystemResourceAsStream("fieldMatch.properties");
			prop.load(in);
			in.close();
			prop.remove(key);
			OutputStream os = new FileOutputStream(new File(ClassLoader.getSystemResource("fieldMatch.properties")
					.getPath()));
			prop.store(os, "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
