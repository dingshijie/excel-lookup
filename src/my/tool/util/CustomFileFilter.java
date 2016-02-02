package my.tool.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * 自定义文件过滤
 * @author DSJ
 *
 */
public class CustomFileFilter extends FileFilter {

	@Override
	public String getDescription() {
		return "Excel工作表（*.xls;）";
	}

	@Override
	public boolean accept(File file) {
		String name = file.getName();
		String suffix = name.toLowerCase();
		return suffix.endsWith(".xls");
	}
}
