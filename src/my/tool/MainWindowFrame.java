package my.tool;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;

import my.tool.util.Constant.ConfigureKey;
import my.tool.util.CustomFileFilter;

import org.apache.commons.lang3.StringUtils;

public class MainWindowFrame {

	private JFrame frmJavatool;
	//��׼�ļ�·��
	private String baseFilePath = "";
	//�������ļ�·��
	private String checkFilePath = "";
	//��׼�ļ�·����ʾ��label
	private JLabel lbl_basepath;
	//У���ļ�·����ʾ��label
	private JLabel lbl_checkpath;
	//��־��·��
	private String log_dictionary = "";
	//��ʾ����
	private JLabel lbl_totalNum;
	//��ʾ�ɹ���
	private JLabel lbl_successNum;
	//��ʾʧ����
	private JLabel lbl_failedNum;

	private ExcelCheck excelCheck;

	private MenuOperation menuOperation = new MenuOperation();

	JLabel openLogFileLabel;
	JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MainWindowFrame window = new MainWindowFrame();
					window.frmJavatool.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindowFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJavatool = new JFrame();
		frmJavatool.setTitle("JavaTool");
		frmJavatool.setBounds(400, 150, 469, 486);
		frmJavatool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJavatool.setLocationRelativeTo(null);
		frmJavatool.getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u9009\u62E9\u6587\u4EF6",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(25, 37, 404, 233);
		frmJavatool.getContentPane().add(panel);
		panel.setLayout(null);

		JButton btnSelectBaseFile = new JButton("ѡ��...");
		btnSelectBaseFile.setFont(new Font("����", Font.PLAIN, 12));
		//ѡ���׼�ļ���ť�¼�ע���
		btnSelectBaseFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// ע�����굥���¼�������ֻ����ѡ���ļ�������ȡ�ļ�����
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//�����ļ���������
				CustomFileFilter cFileFilter = new CustomFileFilter();
				jfc.addChoosableFileFilter(cFileFilter);
				jfc.setFileFilter(cFileFilter);
				//����Ĭ��·��Ϊ����
				FileSystemView fsv = FileSystemView.getFileSystemView();
				jfc.setCurrentDirectory(fsv.getHomeDirectory());

				int option = jfc.showDialog(new JLabel(), "��");
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					if (file != null) {
						baseFilePath = file.getAbsolutePath();
						lbl_basepath.setForeground(Color.BLACK);
						lbl_basepath.setText(baseFilePath);
					}
				}
			}
		});
		btnSelectBaseFile.setBounds(117, 33, 93, 23);
		panel.add(btnSelectBaseFile);

		JLabel BaseFileLabel = new JLabel("ѡ���׼�ļ�");
		BaseFileLabel.setFont(new Font("����", Font.PLAIN, 12));
		BaseFileLabel.setBounds(26, 37, 81, 15);
		panel.add(BaseFileLabel);

		JLabel checkFileLabel = new JLabel("ѡ������ļ�");
		checkFileLabel.setFont(new Font("����", Font.PLAIN, 12));
		checkFileLabel.setBounds(26, 84, 81, 15);
		panel.add(checkFileLabel);

		JButton btnSelectCheckFile = new JButton("ѡ��...");
		//�����ļ���ť��������¼�ע���
		btnSelectCheckFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// ע�����굥���¼�������ֻ����ѡ���ļ�������ȡ�ļ�����
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//�����ļ���������
				CustomFileFilter cFileFilter = new CustomFileFilter();
				jfc.addChoosableFileFilter(cFileFilter);
				jfc.setFileFilter(cFileFilter);
				//����Ĭ��·��Ϊ����
				FileSystemView fsv = FileSystemView.getFileSystemView();
				jfc.setCurrentDirectory(fsv.getHomeDirectory());

				int option = jfc.showDialog(new JLabel(), "��");
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					if (file != null) {
						checkFilePath = file.getAbsolutePath();
						lbl_checkpath.setForeground(Color.BLACK);
						lbl_checkpath.setText(checkFilePath);
					}
				}
			}
		});
		btnSelectCheckFile.setFont(new Font("����", Font.PLAIN, 12));
		btnSelectCheckFile.setBounds(117, 80, 93, 23);
		panel.add(btnSelectCheckFile);

		JButton btnStart = new JButton("��ʼУ��");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//�ж��ļ��Ƿ����
				File baseFile = new File(baseFilePath);
				if (!baseFile.isFile()) {
					lbl_basepath.setForeground(Color.RED);
					lbl_basepath.setText("��ѡ���׼�ļ�");
					return;
				}
				File checkFile = new File(checkFilePath);
				if (!checkFile.isFile()) {
					lbl_checkpath.setForeground(Color.RED);
					lbl_checkpath.setText("��ѡ��У���ļ�");
					return;
				}
				//TODO У��
				new Progress(progressBar, openLogFileLabel).start();
				excelCheck = new ExcelCheck(baseFile, checkFile);
				Thread t2 = new Thread(excelCheck);
				t2.start();
			}
		});
		btnStart.setFont(new Font("����", Font.PLAIN, 12));
		btnStart.setBounds(26, 130, 93, 23);
		panel.add(btnStart);

		lbl_basepath = new JLabel("");
		lbl_basepath.setFont(new Font("����", Font.PLAIN, 12));
		lbl_basepath.setBounds(220, 37, 400, 15);
		panel.add(lbl_basepath);

		lbl_checkpath = new JLabel("");
		lbl_checkpath.setFont(new Font("����", Font.PLAIN, 12));
		lbl_checkpath.setBounds(220, 84, 400, 15);
		panel.add(lbl_checkpath);

		progressBar = new JProgressBar();
		progressBar.setBounds(26, 190, 363, 19);
		panel.add(progressBar);

		Panel panelresult = new Panel();
		panelresult.setBounds(25, 276, 404, 138);
		frmJavatool.getContentPane().add(panelresult);
		panelresult.setLayout(null);

		JLabel successNumLabel = new JLabel("�ɹ���");
		successNumLabel.setFont(new Font("����", Font.PLAIN, 12));
		successNumLabel.setBounds(30, 45, 36, 15);
		panelresult.add(successNumLabel);

		JLabel fieldNumLabel = new JLabel("ʧ����");
		fieldNumLabel.setFont(new Font("����", Font.PLAIN, 12));
		fieldNumLabel.setBounds(30, 70, 36, 15);
		panelresult.add(fieldNumLabel);

		openLogFileLabel = new JLabel("<HTML><U>����־�ļ���</U></HTML>");
		openLogFileLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (openLogFileLabel.isEnabled()) {
					try {
						Desktop.getDesktop().open(new File(log_dictionary));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		openLogFileLabel.setEnabled(false);
		openLogFileLabel.setForeground(Color.BLUE);
		openLogFileLabel.setFont(new Font("����", Font.PLAIN, 12));
		openLogFileLabel.setBounds(30, 95, 97, 30);
		panelresult.add(openLogFileLabel);

		lbl_successNum = new JLabel("");
		lbl_successNum.setFont(new Font("����", Font.PLAIN, 12));
		lbl_successNum.setBounds(73, 45, 54, 15);
		panelresult.add(lbl_successNum);

		lbl_failedNum = new JLabel("");
		lbl_failedNum.setFont(new Font("����", Font.PLAIN, 12));
		lbl_failedNum.setBounds(73, 70, 54, 15);
		panelresult.add(lbl_failedNum);

		JLabel totalNumberLabel = new JLabel("��  ��");
		totalNumberLabel.setFont(new Font("����", Font.PLAIN, 12));
		totalNumberLabel.setBounds(30, 20, 36, 15);
		panelresult.add(totalNumberLabel);

		lbl_totalNum = new JLabel("");
		lbl_totalNum.setFont(new Font("����", Font.PLAIN, 12));
		lbl_totalNum.setBounds(73, 20, 54, 15);
		panelresult.add(lbl_totalNum);

		JMenuBar findMenuBar = new JMenuBar();
		findMenuBar.setBounds(10, 6, 40, 21);
		frmJavatool.getContentPane().add(findMenuBar);

		JMenu findMenu = new JMenu("�鿴");
		findMenuBar.add(findMenu);

		JMenuItem findBaseColMenuItem = new JMenuItem("�鿴��׼��");
		findBaseColMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//�鿴��׼��
				String baseColumn = menuOperation.readConfigureFile(ConfigureKey.baseColumn);
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(null, baseColumn, "�����õĻ�׼��", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		findMenu.add(findBaseColMenuItem);
		//��һ���ָ��
		findMenu.addSeparator();

		JMenuItem findDicMenuItem = new JMenuItem("�鿴��־�ļ�λ��");
		findDicMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//�鿴��־�ļ�λ��
				String savePath = menuOperation.readConfigureFile(ConfigureKey.savePath);
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(null, savePath, "��־�ļ�λ��", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		findMenu.add(findDicMenuItem);

		//��һ���ָ��
		findMenu.addSeparator();

		JMenuItem findFiledmatchMenuItem = new JMenuItem("�鿴��ƥ����Ϣ");
		findFiledmatchMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FieldMatchDialog dialog = new FieldMatchDialog();
				dialog.setVisible(true);
			}
		});
		findMenu.add(findFiledmatchMenuItem);

		JMenuBar setMenuBar = new JMenuBar();
		setMenuBar.setBounds(52, 6, 40, 21);
		frmJavatool.getContentPane().add(setMenuBar);

		JMenu setMenu = new JMenu("����");
		setMenuBar.add(setMenu);

		JMenuItem setBaseColMenuItem = new JMenuItem("���û�׼��");
		setBaseColMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String value = JOptionPane.showInputDialog(frmJavatool, "�����û�׼����", "���û�׼��",
						JOptionPane.INFORMATION_MESSAGE);
				if (StringUtils.isNotBlank(value)) {
					menuOperation.setConfigureFileContent(ConfigureKey.baseColumn, value.trim());
				}
			}
		});
		setMenu.add(setBaseColMenuItem);
		//��һ���ָ��
		setMenu.addSeparator();
		JMenuItem setBaseDocMenuItem = new JMenuItem("������־����λ��");
		setBaseDocMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// ע�����굥���¼�������ֻ����ѡ���ļ�������ȡ�ļ�����
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//����Ĭ��·��Ϊ����
				FileSystemView fsv = FileSystemView.getFileSystemView();
				jfc.setCurrentDirectory(fsv.getHomeDirectory());

				int option = jfc.showDialog(new JLabel(), "ѡ��");
				if (option == JFileChooser.APPROVE_OPTION) { //��ȷ����
					File file = jfc.getSelectedFile();
					if (file != null) {
						String value = file.getAbsolutePath();
						if (StringUtils.isNotBlank(value)) {
							menuOperation.setConfigureFileContent(ConfigureKey.savePath, value.trim());
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(null, "�����óɹ�", "��ʾ", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			}
		});
		setMenu.add(setBaseDocMenuItem);

		JMenuBar helpMenuBar = new JMenuBar();
		helpMenuBar.setBounds(94, 6, 40, 21);
		frmJavatool.getContentPane().add(helpMenuBar);

		JMenu helpMenu = new JMenu("����");
		helpMenuBar.add(helpMenu);
	}

	private class Progress extends Thread {
		final JProgressBar progressBar;
		final JLabel openLogFileLabel;

		Progress(JProgressBar progressBar, JLabel openLogFileLabel) {
			this.progressBar = progressBar;
			this.openLogFileLabel = openLogFileLabel;
			progressBar.setValue(35);
			progressBar.setStringPainted(true);
		}

		@Override
		public void run() {
			boolean totol = false;
			while (excelCheck == null) {
				//TODO ��excelCheck��Ϊnullʱ��Ž��������
			}
			while (excelCheck.START) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block  
					e.printStackTrace();
				}
				int value = (int) (1.0 * 100 * (excelCheck.SUCCESS_NUM + excelCheck.FAILED_NUM) / excelCheck.TOTAL_NUM);
				progressBar.setValue(value);
				lbl_successNum.setText(String.valueOf(excelCheck.SUCCESS_NUM));
				lbl_failedNum.setText(String.valueOf(excelCheck.FAILED_NUM));
				if (!totol && excelCheck.TOTAL_NUM != 0) {
					lbl_totalNum.setText(String.valueOf(excelCheck.TOTAL_NUM));
					totol = true;
				}
			}
			progressBar.setIndeterminate(false);
			progressBar.setString("��ɣ�");
			lbl_successNum.setText(String.valueOf(excelCheck.SUCCESS_NUM));
			lbl_failedNum.setText(String.valueOf(excelCheck.FAILED_NUM));
			lbl_totalNum.setText(String.valueOf(excelCheck.TOTAL_NUM));
			//������־����־��ťCOMPLETE=trueʱ��ʾ��־��¼���
			while (!excelCheck.COMPLETE) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block  
					e.printStackTrace();
				}
			}
			log_dictionary = excelCheck.FILE_DICTIONARY;
			openLogFileLabel.setEnabled(true);
		}
	}
}
