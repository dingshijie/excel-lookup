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
 * excel У�����
 * @author DSJ
 *
 */
public class ExcelCheck implements Runnable {

	//�ļ��ı���λ�ã�������ʱ��Ĭ�ϱ��浽d://javatool
	private String savePath = "d://javatool";
	//ƥ��ɹ�������ļ�����ƥ��ʧ�ܱ�����ļ���
	private String SUCCESS_FILE = "success.txt";
	private String FAILED_FILE = "fail.txt";
	private String ERROR_FILE = "error.txt";
	//��¼�ɹ�����ʧ����
	public int TOTAL_NUM = 0;
	public int SUCCESS_NUM = 0;
	public int FAILED_NUM = 0;
	public String FILE_DICTIONARY = "";
	public boolean START = true;
	public boolean COMPLETE = false;
	//��׼��,��׼����Ϊ���жϵ�����
	private String BASECOLUMN;
	//��������ֻ������һ��sheet,Ҳ����sheet[0]
	private static final int SHEET_NUM = 1;
	//������׼�ļ��õ�����Ϣ Map<�û���, Map<�ֶ���, �ֶ�ֵ>>���������а����к�row��map
	private Map<String, Map<String, Object>> baseDetails = new HashMap<String, Map<String, Object>>();
	//������׼�ļ����ֻ�׼��ֵΪ�ջ򲻴��ڵ���
	private List<Map<String, Object>> baseDetails_ErrorList = new ArrayList<Map<String, Object>>();
	//����У���ļ����ֻ�׼��ֵΪ�ջ򲻴��ڵ���
	private List<Map<String, Object>> checkDetails_ErrorList = new ArrayList<Map<String, Object>>();
	//��Ϊһ��key����rownumber
	private static final String ROW_NUM = "ROW_NUM";
	//�����ļ������ֶ�ƥ��
	private Map<String, String> fieldMatch = new HashMap<String, String>();
	//��׼�ļ���ԭʼ���ֶ�
	private Map<String, String> baseOriginalField = new HashMap<String, String>();
	//У���ļ���ԭʼ���ֶ�
	private Map<String, String> checkOriginalField = new HashMap<String, String>();
	//�ɹ����û�SuccessRowInfo
	private List<SuccessRowInfo> successList = new ArrayList<SuccessRowInfo>();
	//ƥ��ɹ����е���Ϣ Map<�û���, Map<�ֶ���, �ֶ�ֵ>>
	private Map<String, Map<String, Object>> successDetails = new HashMap<String, Map<String, Object>>();
	//ƥ��ʧ�ܵ��е���Ϣ Map<�û���, Map<ƥ��ʧ���ֶ�, ƥ��ʧ�ܵ��ֶ�����>>
	private Map<String, Map<String, Object>> failedDetails = new HashMap<String, Map<String, Object>>();
	//δУ�����Ϣ����У���ļ��к��У�����׼�ļ��в�����BASECOLUMN����
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

		//1����ȡ��������ļ�����savePath��BASECOLUMN��ƥ���ֶ���Ϣ
		readConfigureFile();
		readFieldMatchFile();
		//2�����û�׼��Ϣ
		analysisBaseFile(baseFile);
		//3��У��
		check(checkFile);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block  
			e.printStackTrace();
		}
		START = false;
		//4������У������־
		saveLog();
		COMPLETE = true;
	}

	/**
	 * ��ȡ�����ļ�������savePath��BASECOLUMN
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
	 * ��ȡƥ���ļ�����ƥ����
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
	 * ������׼�ļ�����ȡ��׼�ļ�����ϸ��Ϣ�����뵽baseDetails
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
					//��ȡ������
					int row = sheet.getRows();
					//��ȡ�������������ǽ���һ����Ϊ������
					int col = sheet.getRow(0).length;
					//����0��ȡ�������ֶ�ƥ��,������¼ԭʼ��ƥ����
					String[] fields = setField(sheet.getRow(0), col, baseOriginalField);
					//ѭ������
					for (int j = 1; j < row; j++) {
						Map<String, Object> info = new HashMap<String, Object>();
						info.put(ROW_NUM, j + 1);
						Cell[] cell = sheet.getRow(j);
						for (int i = 0; i < cell.length && i < col; i++) {
							String fieldValue = StringUtils.deleteWhitespace(cell[i].getContents());
							info.put(fields[i], fieldValue);
						}
						//������Ҫ��ÿһ�У�����Ϊkey, ���е�key��Ҫ��Ӧ���ڵ�value,Ϊʲôһ��Ҫ����Ϊ��null,��Ϊ�������ã���Ϊnull,����ҵ�񣬸���Ϊnull��Ϊ����Ҫƥ��,������©����������checkMap
						for (int i = cell.length; i < col; i++) {
							info.put(fields[i], "");
						}
						//�ҵ���׼��,����map
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
	 * У���ļ�����У��ɹ����û��б���뵽successList���ɹ�����Ϣ���뵽SuccessDetails��ʧ�ܵ���Ϣ���뵽failedDetails
	 * @param checkFile
	 */
	private void check(File checkFile) {
		try {
			InputStream in = new FileInputStream(checkFile);
			Workbook wb = Workbook.getWorkbook(in);
			for (int k = 0; k < SHEET_NUM; k++) {
				Sheet sheet = wb.getSheet(k);
				if (sheet != null) {
					//��ȡ������
					int row = sheet.getRows();
					//����������,����Ҫȥ����һ��
					TOTAL_NUM = row - 1;
					//��ȡ������,�������ǽ���һ����Ϊ������
					int col = sheet.getRow(0).length;
					//����0��ȡ�������ֶ�ƥ��,������¼ԭʼ��ƥ����
					String[] fields = setField(sheet.getRow(0), col, checkOriginalField);
					//ѭ������
					for (int j = 1; j < row; j++) {
						Map<String, Object> info = new HashMap<String, Object>();
						info.put(ROW_NUM, j + 1);
						Cell[] cell = sheet.getRow(j);
						for (int i = 0; i < cell.length && i < col; i++) {
							String fieldValue = StringUtils.deleteWhitespace(cell[i].getContents());
							info.put(fields[i], fieldValue);
						}
						//������Ҫ��ÿһ�У�����Ϊkey, ���е�key��Ҫ��Ӧ���ڵ�value,Ϊʲôһ��Ҫ����Ϊ��null,��Ϊ�������ã���Ϊnull,����ҵ�񣬸���Ϊnull��Ϊ����Ҫƥ��,������©����������checkMap
						for (int i = cell.length; i < col; i++) {
							info.put(fields[i], "");
						}
						//�ҵ���׼��,����ƥ��
						Object baseColumnValue = info.get(BASECOLUMN);
						if (baseColumnValue == null || StringUtils.isBlank(baseColumnValue.toString())) {
							//ʧ����+1
							FAILED_NUM += 1;
							//�Ҳ�����׼�У�ֱ�Ӵ���У�������б���
							checkDetails_ErrorList.add(info);
						} else {
							Map<String, Object> baseMap = baseDetails.get(baseColumnValue.toString());
							//baseMapΪ�յ�ʱ�򲻱�У�飬ֱ�Ӵ���ΪУ����ļ���������ȥУ��
							if (baseMap == null) {
								//ʧ����+1
								FAILED_NUM += 1;
								noCheckDetails.add(info);
							} else {
								checkMap(baseMap, info, baseColumnValue.toString());//У��
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
	 * ������һ�У�������
	 * @param cell
	 * @param col
	 * @param originalField ���ڼ�¼ԭʼ��ƥ����
	 * @return
	 */
	private String[] setField(Cell[] cell, int col, Map<String, String> originalField) {
		String[] fields = new String[col];
		for (int i = 0; i < col && i < cell.length; i++) {
			String fieldName = StringUtils.deleteWhitespace(cell[i].getContents());
			if (StringUtils.isNotBlank(fieldName)) {
				boolean res = StringUtils.isNotBlank(fieldMatch.get(fieldName));//�ж��Ƿ�������ƥ����
				fields[i] = res ? fieldMatch.get(fieldName) : fieldName;
				//��ƥ�����ʱ��Ҫ����¼ԭʼ��ƥ�����ԭ����key��value,value���key
				originalField.put(fields[i], fieldName);
			}
		}
		return fields;
	}

	@SuppressWarnings("unused")
	private class SuccessRowInfo {
		private int baseRowNum;//��׼�ļ����к�
		private String baseColumnValue;//��׼�ļ���baseColumn��value
		private int checkRowNum;//У���ļ����к�
		private String checkColumnValue;//У���ļ���baseColumn��value

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
			return " [У���ļ��е�" + checkRowNum + "��, " + BASECOLUMN + "ֵΪ��" + checkColumnValue + "���׼�ļ���" + ", ��"
					+ baseRowNum + "��, " + BASECOLUMN + "ֵΪ��" + baseColumnValue + "��Ӧ��У�����]";
		}

	}

	/**
	 * У��
	 * @param baseMap ��׼map
	 * @param checkMap ��У���map
	 * @param baseColumnValue ��׼ֵ
	 * @return У����
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
				failedMap.put(key, "{��׼�ļ�" + baseOriginalField.get(key) + "=" + baseValue + ",У���ļ�"
						+ checkOriginalField.get(key) + "=" + checkValue + "}");
			}
		}
		if (!failedMap.isEmpty()) {
			//ʧ����+1
			FAILED_NUM += 1;
			//��ӵ�ʧ�ܵ���ϸ��Ϣ�б���
			failedDetails.put(baseColumnValue, failedMap);
		} else {
			//ʧ����+1
			SUCCESS_NUM += 1;
			//��ӳɹ����б�
			SuccessRowInfo info = new SuccessRowInfo();
			info.setBaseColumnValue(baseMap.get(BASECOLUMN).toString());
			info.setBaseRowNum(Integer.parseInt(baseMap.get(ROW_NUM).toString()));
			info.setCheckColumnValue(checkMap.get(BASECOLUMN).toString());
			info.setCheckRowNum(Integer.parseInt(checkMap.get(ROW_NUM).toString()));
			successList.add(info);
			//��ӵ��ɹ�����ϸ��Ϣ��
			successDetails.put(baseColumnValue, checkMap);
		}
	}

	/**
	 * ������־
	 */
	private void saveLog() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHʱmm��ss��");
		Date date = new Date();
		File file = new File(savePath + "/" + sdf.format(date));
		if (!file.exists()) {
			file.mkdirs();
		}
		FILE_DICTIONARY = file.getAbsolutePath();
		File successFile = createFile(file, SUCCESS_FILE);
		File failedFile = createFile(file, FAILED_FILE);
		File errorFile = createFile(file, ERROR_FILE);
		//��У��ɹ���д�뵽�ļ���
		if (successList.size() > 0) {
			writeLogToFile(successFile, "У��ɹ�������\r\n", getContentFromList(successList));
		}
		//��У��ʧ�ܵ�1��δƥ���ϣ� 2��У�鲻ƥ��ģ�д�뵽�ļ���
		if (noCheckDetails.size() > 0) {
			writeLogToFile(failedFile,
					"У���ļ���" + checkOriginalField.get(BASECOLUMN) + "�е�ֵ�ڻ�׼�ļ���" + baseOriginalField.get(BASECOLUMN)
							+ "���ڲ�������\r\n", getContentFromList(noCheckDetails, checkOriginalField));
		}
		if (!failedDetails.isEmpty()) {
			writeLogToFile(failedFile, "У��ʧ�ܵ�����\r\n", getContentFromMap(failedDetails, checkOriginalField));
		}
		//�ļ��г��ִ������1��BASECOLUMNֵΪ�յ�
		if (baseDetails_ErrorList.size() > 0) {
			writeLogToFile(errorFile, "��׼�ļ���" + baseOriginalField.get(BASECOLUMN) + "��ֵΪ�յ�����\r\n",
					getContentFromList(baseDetails_ErrorList, baseOriginalField));
		}
		if (checkDetails_ErrorList.size() > 0) {
			writeLogToFile(errorFile, "У���ļ���" + checkOriginalField.get(BASECOLUMN) + "��ֵΪ�յ�����\r\n",
					getContentFromList(checkDetails_ErrorList, checkOriginalField));
		}

	}

	/**
	 * ����־��д����
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
	 * �����ļ�
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
	 * ��list����ת����String��ʽ
	 * @param file
	 * @param list
	 * @return
	 */
	private String getContentFromList(List<Map<String, Object>> list, Map<String, String> originalField) {
		StringBuffer sb = new StringBuffer();
		for (Map<String, Object> info : list) {
			sb.append("��" + info.get(ROW_NUM) + "��," + originalField.get(BASECOLUMN) + "=" + info.get(BASECOLUMN) + "[");
			info.remove(ROW_NUM);
			info.remove(BASECOLUMN);//ɾ���� ����ֵ���������ظ�����
			Iterator<String> iterator = info.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = info.get(key).toString();
				sb.append(originalField.get(key) + "=" + value + ",");
			}
			int len = sb.length();
			sb.replace(len - 1, len, "]\r\n");//�����һλ","�滻��"]"
		}
		return sb.toString();
	}

	/**
	 * ��list����ת����String��ʽ
	 * @param successList
	 * @return
	 */
	private String getContentFromList(List<SuccessRowInfo> list) {
		StringBuffer sb = new StringBuffer();
		for (SuccessRowInfo info : list) {
			sb.append(info.toString()).append("\r\n");//���ϻس�����
		}
		return sb.toString();
	}

	/**
	 * ��map����Ϣת����String
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

			sb.append("��" + valueMap.get(ROW_NUM) + "��," + originalField.get(BASECOLUMN) + "=" + key + "[");
			valueMap.remove(ROW_NUM);
			valueMap.remove(BASECOLUMN);//ɾ���� ����ֵ���������ظ�����

			Iterator<String> it = valueMap.keySet().iterator();
			while (it.hasNext()) {
				String filedName = it.next();
				String filedValue = valueMap.get(filedName).toString();
				sb.append(originalField.get(filedName) + "=" + filedValue + ",");
			}
			int len = sb.length();
			sb.replace(len - 1, len, "]\r\n");//�����һλ","�滻��"]"
		}
		return sb.toString();
	}

}
