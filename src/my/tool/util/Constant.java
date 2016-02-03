package my.tool.util;

/**
 * 一些常量
 * @author JZ
 *
 */
public class Constant {

	/**
	 * configure.properties中的key
	 * @author JZ
	 *
	 */
	public enum ConfigureKey {
		/**
		 * 保存路径
		 */
		savePath,
		/**
		 * 基准列
		 */
		baseColumn
	}

	/**
	 * 配置文件路径
	 */
	public static final String configureFilePath = "config/configure.properties";
	/**
	 * 字段匹配文件路径
	 */
	public static final String fieldMatchFilePath = "config/fieldMatch.properties";

}
