package my.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import my.tool.util.Constant;
import my.tool.util.Constant.ConfigureKey;

import org.apache.commons.lang3.StringUtils;

/**
 * excel 校验程序
 * @author DSJ
 *
 */
public class ExcelCheck implements Runnable {

	//文件的保存位置，不设置时候默认保存到d://javatool
	private String savePath = "d://javatool";
	//匹配成功保存的文件名和匹配失败保存的文件名
	private String SUCCESS_FILE = "success.txt";
	private String FAILED_FILE = "fail.txt";
	private String ERROR_FILE = "error.txt";
	//记录成功数和失败数
	public int TOTAL_NUM = 0;
	public int SUCCESS_NUM = 0;
	public int FAILED_NUM = 0;
	public String FILE_DICTIONARY = "";
	public boolean START = true;
	public boolean COMPLETE = false;
	//基准列,基准列作为行判断的依据
	private String BASECOLUMN;
	//这里设置只解析第一个sheet,也就是sheet[0]
	private static final int SHEET_NUM = 1;
	//解析基准文件得到的信息 Map<用户名, Map<字段名, 字段值>>，附：其中包括行号row的map
	private Map<String, Map<String, Object>> baseDetails = new HashMap<String, Map<String, Object>>();
	//解析基准文件出现基准列值为空或不存在的列
	private List<Map<String, Object>> baseDetails_ErrorList = new ArrayList<Map<String, Object>>();
	//解析校验文件出现基准列值为空或不存在的列
	private List<Map<String, Object>> checkDetails_ErrorList = new ArrayList<Map<String, Object>>();
	//作为一个key设置rownumber
	private static final String ROW_NUM = "ROW_NUM";
	//两个文件进行字段匹配
	private Map<String, String> fieldMatch = new HashMap<String, String>();
	//基准文件中原始的字段
	private Map<String, String> baseOriginalField = new HashMap<String, String>();
	//校验文件中原始的字段
	private Map<String, String> checkOriginalField = new HashMap<String, String>();
	//成功的用户SuccessRowInfo
	private List<SuccessRowInfo> successList = new ArrayList<SuccessRowInfo>();
	//匹配成功的行的信息 Map<用户名, Map<字段名, 字段值>>
	private Map<String, Map<String, Object>> successDetails = new HashMap<String, Map<String, Object>>();
	//匹配失败的行的信息 Map<用户名, Map<匹配失败字段, 匹配失败的字段描述>>
	private Map<String, Map<String, Object>> failedDetails = new HashMap<String, Map<String, Object>>();
	//未校验的信息，即校验文件中含有，而基准文件中不含有BASECOLUMN的行
	private List<Map<String, Object>> noCheckDetails = new ArrayList<Map<String, Object>>();

	private File baseFile;
	private File checkFile;

	public ExcelCheck(File baseFile, File checkFile) {
		this.baseFile = baseFile;
		this.checkFile = checkFile;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		execute(baseFile, checkFile);
	}

	public void execute(File baseFile, File checkFile) {

		//1、读取相关配置文件设置savePath、BASECOLUMN和匹配字段信息
		readConfigureFile();
		readFieldMatchFile();
		//2、设置基准信息
		analysisBaseFile(baseFile);
		//3、校验
		check(checkFile);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		}
		START = false;
		//4、创建校验结果日志
		saveLog();
		COMPLETE = true;
	}

	/**
	 * 读取配置文件，设置savePath和BASECOLUMN
	 */
	private void readConfigureFile() {
		Properties prop = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream(Constant.configureFilePath);
		try {
			prop.load(in);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		savePath = prop.get(ConfigureKey.savePath.name()).toString();
		BASECOLUMN = prop.get(ConfigureKey.baseColumn.name()).toString();
	}

	/**
	 * 读取匹配文件设置匹配项
	 */
	private void readFieldMatchFile() {
		Properties prop = new Properties();
		InputStream in = ClassLoader.getSystemResourceAsStream(Constant.fieldMatchFilePath);
		try {
			prop.load(in);
			in.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator<Object> iterator = prop.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = prop.getProperty(key).toString();
			fieldMatch.put(key, value);
		}
	}

	/**
	 * 解析基准文件，获取基准文件的详细信息，存入到baseDetails
	 * @param baseFile
	 */
	private void analysisBaseFile(File baseFile) {
		baseDetails = new HashMap<String, Map<String, Object>>();
		try {
			InputStream in = new FileInputStream(baseFile);
			Workbook wb = Workbook.getWorkbook(in);
			for (int k = 0; k < SHEET_NUM; k++) {
				Sheet sheet = wb.getSheet(k);
				if (sheet != null) {
					//获取总行数
					int row = sheet.getRows();
					//获取总列数这里我们将第一列认为总列数
					int col = sheet.getRow(0).length;
					//将第0行取出进行字段匹配,并反记录原始的匹配项
					String[] fields = setField(sheet.getRow(0), col, baseOriginalField);
					//循环行数
					for (int j = 1; j < row; j++) {
						Map<String, Object> info = new HashMap<String, Object>();
						info.put(ROW_NUM, j + 1);
						Cell[] cell = sheet.getRow(j);
						for (int i = 0; i < cell.length && i < col; i++) {
							String fieldValue = StringUtils.deleteWhitespace(cell[i].getContents());
							info.put(fields[i], fieldValue);
						}
						//这里我要对每一列，列名为key, 所有的key都要对应对于的value,为什么一定要设置为空null,因为若不设置，则为null,根据业务，该列为null认为不需要匹配,导致有漏洞，有体现checkMap
						for (int i = cell.length; i < col; i++) {
							info.put(fields[i], "");
						}
						//找到基准列,设置map
						Object baseColumnValue = info.get(BASECOLUMN);
						if (baseColumnValue == null || StringUtils.isBlank(baseColumnValue.toString())) {
							baseDetails_ErrorList.add(info);
						} else {
							baseDetails.put(baseColumnValue.toString(), info);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 校验文件，将校验成功的用户列表存入到successList，成功的信息存入到SuccessDetails，失败的信息存入到failedDetails
	 * @param checkFile
	 */
	private void check(File checkFile) {
		try {
			InputStream in = new FileInputStream(checkFile);
			Workbook wb = Workbook.getWorkbook(in);
			for (int k = 0; k < SHEET_NUM; k++) {
				Sheet sheet = wb.getSheet(k);
				if (sheet != null) {
					//获取总行数
					int row = sheet.getRows();
					//设置总行数,这里要去除第一列
					TOTAL_NUM = row - 1;
					//获取总列数,这里我们将第一列认为总列数
					int col = sheet.getRow(0).length;
					//将第0行取出进行字段匹配,并反记录原始的匹配项
					String[] fields = setField(sheet.getRow(0), col, checkOriginalField);
					//循环行数
					for (int j = 1; j < row; j++) {
						Map<String, Object> info = new HashMap<String, Object>();
						info.put(ROW_NUM, j + 1);
						Cell[] cell = sheet.getRow(j);
						for (int i = 0; i < cell.length && i < col; i++) {
							String fieldValue = StringUtils.deleteWhitespace(cell[i].getContents());
							info.put(fields[i], fieldValue);
						}
						//这里我要对每一列，列名为key, 所有的key都要对应对于的value,为什么一定要设置为空null,因为若不设置，则为null,根据业务，该列为null认为不需要匹配,导致有漏洞，有体现checkMap
						for (int i = cell.length; i < col; i++) {
							info.put(fields[i], "");
						}
						//找到基准列,进行匹配
						Object baseColumnValue = info.get(BASECOLUMN);
						if (baseColumnValue == null || StringUtils.isBlank(baseColumnValue.toString())) {
							//失败数+1
							FAILED_NUM += 1;
							//找不到基准列，直接存入校验错误的列表中
							checkDetails_ErrorList.add(info);
						} else {
							Map<String, Object> baseMap = baseDetails.get(baseColumnValue.toString());
							//baseMap为空的时候不比校验，直接存入为校验的文件，否则则去校验
							if (baseMap == null) {
								//失败数+1
								FAILED_NUM += 1;
								noCheckDetails.add(info);
							} else {
								checkMap(baseMap, info, baseColumnValue.toString());//校验
							}
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block  
							e.printStackTrace();
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析第一行，设置列
	 * @param cell
	 * @param col
	 * @param originalField 用于记录原始的匹配项
	 * @return
	 */
	private String[] setField(Cell[] cell, int col, Map<String, String> originalField) {
		String[] fields = new String[col];
		for (int i = 0; i < col && i < cell.length; i++) {
			String fieldName = StringUtils.deleteWhitespace(cell[i].getContents());
			if (StringUtils.isNotBlank(fieldName)) {
				boolean res = StringUtils.isNotBlank(fieldMatch.get(fieldName));//判断是否有设置匹配项
				fields[i] = res ? fieldMatch.get(fieldName) : fieldName;
				//有匹配项的时候要反记录原始的匹配项，即原来的key变value,value变成key
				originalField.put(fields[i], fieldName);
			}
		}
		return fields;
	}

	@SuppressWarnings("unused")
	private class SuccessRowInfo {
		private int baseRowNum;//基准文件的行号
		private String baseColumnValue;//基准文件在baseColumn的value
		private int checkRowNum;//校验文件的行号
		private String checkColumnValue;//校验文件在baseColumn的value

		public int getBaseRowNum() {
			return baseRowNum;
		}

		public void setBaseRowNum(int baseRowNum) {
			this.baseRowNum = baseRowNum;
		}

		public String getBaseColumnValue() {
			return baseColumnValue;
		}

		public void setBaseColumnValue(String baseColumnValue) {
			this.baseColumnValue = baseColumnValue;
		}

		public int getCheckRowNum() {
			return checkRowNum;
		}

		public void setCheckRowNum(int checkRowNum) {
			this.checkRowNum = checkRowNum;
		}

		public String getCheckColumnValue() {
			return checkColumnValue;
		}

		public void setCheckColumnValue(String checkColumnValue) {
			this.checkColumnValue = checkColumnValue;
		}

		@Override
		public String toString() {
			return " [校验文件中第" + checkRowNum + "行, " + BASECOLUMN + "值为：" + checkColumnValue + "与基准文件中" + ", 第"
					+ baseRowNum + "行, " + BASECOLUMN + "值为：" + baseColumnValue + "对应列校验相等]";
		}

	}

	/**
	 * 校验
	 * @param baseMap 基准map
	 * @param checkMap 带校验的map
	 * @param baseColumnValue 基准值
	 * @return 校验结果
	 */
	private void checkMap(Map<String, Object> baseMap, Map<String, Object> checkMap, String baseColumnValue) {
		Iterator<String> iterator = checkMap.keySet().iterator();
		Map<String, Object> failedMap = new HashMap<String, Object>();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equals(ROW_NUM)) {
				continue;
			}
			Object checkValue = checkMap.get(key).toString();
			Object baseValue = baseMap.get(key);
			if (baseValue != null && !baseValue.toString().equals(checkValue)) {
				if (failedMap.isEmpty()) {
					failedMap.put(ROW_NUM, checkMap.get(ROW_NUM));
				}
				failedMap.put(key, "{基准文件" + baseOriginalField.get(key) + "=" + baseValue + ",校验文件"
						+ checkOriginalField.get(key) + "=" + checkValue + "}");
			}
		}
		if (!failedMap.isEmpty()) {
			//失败数+1
			FAILED_NUM += 1;
			//添加到失败的详细信息列表中
			failedDetails.put(baseColumnValue, failedMap);
		} else {
			//失败数+1
			SUCCESS_NUM += 1;
			//添加成功的列表
			SuccessRowInfo info = new SuccessRowInfo();
			info.setBaseColumnValue(baseMap.get(BASECOLUMN).toString());
			info.setBaseRowNum(Integer.parseInt(baseMap.get(ROW_NUM).toString()));
			info.setCheckColumnValue(checkMap.get(BASECOLUMN).toString());
			info.setCheckRowNum(Integer.parseInt(checkMap.get(ROW_NUM).toString()));
			successList.add(info);
			//添加到成功的详细信息中
			successDetails.put(baseColumnValue, checkMap);
		}
	}

	/**
	 * 保存日志
	 */
	private void saveLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH时mm分ss秒");
		Date date = new Date();
		File file = new File(savePath + "/" + sdf.format(date));
		if (!file.exists()) {
			file.mkdirs();
		}
		FILE_DICTIONARY = file.getAbsolutePath();
		File successFile = createFile(file, SUCCESS_FILE);
		File failedFile = createFile(file, FAILED_FILE);
		File errorFile = createFile(file, ERROR_FILE);
		//将校验成功的写入到文件中
		if (successList.size() > 0) {
			writeLogToFile(successFile, "校验成功列如下\r\n", getContentFromList(successList));
		}
		//将校验失败的1、未匹配上， 2、校验不匹配的）写入到文件中
		if (noCheckDetails.size() > 0) {
			writeLogToFile(failedFile,
					"校验文件中" + checkOriginalField.get(BASECOLUMN) + "列的值在基准文件中" + baseOriginalField.get(BASECOLUMN)
							+ "对于不上如下\r\n", getContentFromList(noCheckDetails, checkOriginalField));
		}
		if (!failedDetails.isEmpty()) {
			writeLogToFile(failedFile, "校验失败的数据\r\n", getContentFromMap(failedDetails, checkOriginalField));
		}
		//文件中出现错误的行1、BASECOLUMN值为空的
		if (baseDetails_ErrorList.size() > 0) {
			writeLogToFile(errorFile, "基准文件中" + baseOriginalField.get(BASECOLUMN) + "列值为空的如下\r\n",
					getContentFromList(baseDetails_ErrorList, baseOriginalField));
		}
		if (checkDetails_ErrorList.size() > 0) {
			writeLogToFile(errorFile, "校验文件中" + checkOriginalField.get(BASECOLUMN) + "列值为空的如下\r\n",
					getContentFromList(checkDetails_ErrorList, checkOriginalField));
		}

	}

	/**
	 * 向日志中写内容
	 * @param file
	 * @param fileContent
	 */
	private void writeLogToFile(File file, String title, String fileContent) {

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			osw.write(title);
			osw.flush();
			osw.write(fileContent);
			osw.flush();
			fos.close();
			osw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 创建文件
	 * @param file
	 * @param newFileName 
	 * @return
	 */
	private File createFile(File file, String newFileName) {

		File successFile = new File(file, newFileName);
		try {
			if (!successFile.exists()) {
				successFile.createNewFile();
			}
			return successFile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将list内容转换成String形式
	 * @param file
	 * @param list
	 * @return
	 */
	private String getContentFromList(List<Map<String, Object>> list, Map<String, String> originalField) {
		StringBuffer sb = new StringBuffer();
		for (Map<String, Object> info : list) {
			sb.append("第" + info.get(ROW_NUM) + "行," + originalField.get(BASECOLUMN) + "=" + info.get(BASECOLUMN) + "[");
			info.remove(ROW_NUM);
			info.remove(BASECOLUMN);//删除这 两个值放置下面重复出现
			Iterator<String> iterator = info.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = info.get(key).toString();
				sb.append(originalField.get(key) + "=" + value + ",");
			}
			int len = sb.length();
			sb.replace(len - 1, len, "]\r\n");//将最后一位","替换成"]"
		}
		return sb.toString();
	}

	/**
	 * 将list内容转换成String形式
	 * @param successList
	 * @return
	 */
	private String getContentFromList(List<SuccessRowInfo> list) {
		StringBuffer sb = new StringBuffer();
		for (SuccessRowInfo info : list) {
			sb.append(info.toString()).append("\r\n");//加上回车换行
		}
		return sb.toString();
	}

	/**
	 * 将map中信息转换成String
	 * @param map
	 * @param originalField
	 * @return
	 */
	private String getContentFromMap(Map<String, Map<String, Object>> map, Map<String, String> originalField) {
		StringBuffer sb = new StringBuffer();
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Map<String, Object> valueMap = map.get(key);

			sb.append("第" + valueMap.get(ROW_NUM) + "行," + originalField.get(BASECOLUMN) + "=" + key + "[");
			valueMap.remove(ROW_NUM);
			valueMap.remove(BASECOLUMN);//删除这 两个值放置下面重复出现

			Iterator<String> it = valueMap.keySet().iterator();
			while (it.hasNext()) {
				String filedName = it.next();
				String filedValue = valueMap.get(filedName).toString();
				sb.append(originalField.get(filedName) + "=" + filedValue + ",");
			}
			int len = sb.length();
			sb.replace(len - 1, len, "]\r\n");//将最后一位","替换成"]"
		}
		return sb.toString();
	}

}
