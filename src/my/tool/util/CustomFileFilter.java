package my.tool.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * �Զ����ļ�����
 * @author DSJ
 *
 */
public class CustomFileFilter extends FileFilter {

	@Override
	public String getDescription() {
		return "Excel������*.xls;��";
	}

	@Override
	public boolean accept(File file) {
		String name = file.getName();
		String suffix = name.toLowerCase();
		return suffix.endsWith(".xls");
	}
}
