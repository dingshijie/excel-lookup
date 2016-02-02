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
	//基准文件路径
	private String baseFilePath = "";
	//被检验文件路径
	private String checkFilePath = "";
	//基准文件路径显示的label
	private JLabel lbl_basepath;
	//校验文件路径显示的label
	private JLabel lbl_checkpath;
	//日志的路径
	private String log_dictionary = "";
	//显示总数
	private JLabel lbl_totalNum;
	//显示成功数
	private JLabel lbl_successNum;
	//显示失败数
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

		JButton btnSelectBaseFile = new JButton("选择...");
		btnSelectBaseFile.setFont(new Font("宋体", Font.PLAIN, 12));
		//选择基准文件按钮事件注册绑定
		btnSelectBaseFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// 注册绑定鼠标单击事件，设置只可以选择文件，兵获取文件名称
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//设置文件过滤条件
				CustomFileFilter cFileFilter = new CustomFileFilter();
				jfc.addChoosableFileFilter(cFileFilter);
				jfc.setFileFilter(cFileFilter);
				//设置默认路径为桌面
				FileSystemView fsv = FileSystemView.getFileSystemView();
				jfc.setCurrentDirectory(fsv.getHomeDirectory());

				int option = jfc.showDialog(new JLabel(), "打开");
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

		JLabel BaseFileLabel = new JLabel("选择基准文件");
		BaseFileLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		BaseFileLabel.setBounds(26, 37, 81, 15);
		panel.add(BaseFileLabel);

		JLabel checkFileLabel = new JLabel("选择检验文件");
		checkFileLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		checkFileLabel.setBounds(26, 84, 81, 15);
		panel.add(checkFileLabel);

		JButton btnSelectCheckFile = new JButton("选择...");
		//检验文件按钮被鼠标点击事件注册绑定
		btnSelectCheckFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// 注册绑定鼠标单击事件，设置只可以选择文件，兵获取文件名称
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				//设置文件过滤条件
				CustomFileFilter cFileFilter = new CustomFileFilter();
				jfc.addChoosableFileFilter(cFileFilter);
				jfc.setFileFilter(cFileFilter);
				//设置默认路径为桌面
				FileSystemView fsv = FileSystemView.getFileSystemView();
				jfc.setCurrentDirectory(fsv.getHomeDirectory());

				int option = jfc.showDialog(new JLabel(), "打开");
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
		btnSelectCheckFile.setFont(new Font("宋体", Font.PLAIN, 12));
		btnSelectCheckFile.setBounds(117, 80, 93, 23);
		panel.add(btnSelectCheckFile);

		JButton btnStart = new JButton("开始校验");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//判断文件是否存在
				File baseFile = new File(baseFilePath);
				if (!baseFile.isFile()) {
					lbl_basepath.setForeground(Color.RED);
					lbl_basepath.setText("请选择基准文件");
					return;
				}
				File checkFile = new File(checkFilePath);
				if (!checkFile.isFile()) {
					lbl_checkpath.setForeground(Color.RED);
					lbl_checkpath.setText("请选择校验文件");
					return;
				}
				//TODO 校验
				new Progress(progressBar, openLogFileLabel).start();
				excelCheck = new ExcelCheck(baseFile, checkFile);
				Thread t2 = new Thread(excelCheck);
				t2.start();
			}
		});
		btnStart.setFont(new Font("宋体", Font.PLAIN, 12));
		btnStart.setBounds(26, 130, 93, 23);
		panel.add(btnStart);

		lbl_basepath = new JLabel("");
		lbl_basepath.setFont(new Font("宋体", Font.PLAIN, 12));
		lbl_basepath.setBounds(220, 37, 400, 15);
		panel.add(lbl_basepath);

		lbl_checkpath = new JLabel("");
		lbl_checkpath.setFont(new Font("宋体", Font.PLAIN, 12));
		lbl_checkpath.setBounds(220, 84, 400, 15);
		panel.add(lbl_checkpath);

		progressBar = new JProgressBar();
		progressBar.setBounds(26, 190, 363, 19);
		panel.add(progressBar);

		Panel panelresult = new Panel();
		panelresult.setBounds(25, 276, 404, 138);
		frmJavatool.getContentPane().add(panelresult);
		panelresult.setLayout(null);

		JLabel successNumLabel = new JLabel("成功数");
		successNumLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		successNumLabel.setBounds(30, 45, 36, 15);
		panelresult.add(successNumLabel);

		JLabel fieldNumLabel = new JLabel("失败数");
		fieldNumLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		fieldNumLabel.setBounds(30, 70, 36, 15);
		panelresult.add(fieldNumLabel);

		openLogFileLabel = new JLabel("<HTML><U>打开日志文件夹</U></HTML>");
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
		openLogFileLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		openLogFileLabel.setBounds(30, 95, 97, 30);
		panelresult.add(openLogFileLabel);

		lbl_successNum = new JLabel("");
		lbl_successNum.setFont(new Font("宋体", Font.PLAIN, 12));
		lbl_successNum.setBounds(73, 45, 54, 15);
		panelresult.add(lbl_successNum);

		lbl_failedNum = new JLabel("");
		lbl_failedNum.setFont(new Font("宋体", Font.PLAIN, 12));
		lbl_failedNum.setBounds(73, 70, 54, 15);
		panelresult.add(lbl_failedNum);

		JLabel totalNumberLabel = new JLabel("总  数");
		totalNumberLabel.setFont(new Font("宋体", Font.PLAIN, 12));
		totalNumberLabel.setBounds(30, 20, 36, 15);
		panelresult.add(totalNumberLabel);

		lbl_totalNum = new JLabel("");
		lbl_totalNum.setFont(new Font("宋体", Font.PLAIN, 12));
		lbl_totalNum.setBounds(73, 20, 54, 15);
		panelresult.add(lbl_totalNum);

		JMenuBar findMenuBar = new JMenuBar();
		findMenuBar.setBounds(10, 6, 40, 21);
		frmJavatool.getContentPane().add(findMenuBar);

		JMenu findMenu = new JMenu("查看");
		findMenuBar.add(findMenu);

		JMenuItem findBaseColMenuItem = new JMenuItem("查看基准列");
		findBaseColMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//查看基准列
				String baseColumn = menuOperation.readConfigureFile(ConfigureKey.baseColumn);
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(null, baseColumn, "已设置的基准列", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		findMenu.add(findBaseColMenuItem);
		//加一个分割符
		findMenu.addSeparator();

		JMenuItem findDicMenuItem = new JMenuItem("查看日志文件位置");
		findDicMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//查看日志文件位置
				String savePath = menuOperation.readConfigureFile(ConfigureKey.savePath);
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(null, savePath, "日志文件位置", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		findMenu.add(findDicMenuItem);

		//加一个分割符
		findMenu.addSeparator();

		JMenuItem findFiledmatchMenuItem = new JMenuItem("查看列匹配信息");
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

		JMenu setMenu = new JMenu("设置");
		setMenuBar.add(setMenu);

		JMenuItem setBaseColMenuItem = new JMenuItem("设置基准列");
		setBaseColMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String value = JOptionPane.showInputDialog(frmJavatool, "请设置基准列名", "设置基准列",
						JOptionPane.INFORMATION_MESSAGE);
				if (StringUtils.isNotBlank(value)) {
					menuOperation.setConfigureFileContent(ConfigureKey.baseColumn, value.trim());
				}
			}
		});
		setMenu.add(setBaseColMenuItem);
		//加一个分割符
		setMenu.addSeparator();
		JMenuItem setBaseDocMenuItem = new JMenuItem("设置日志保存位置");
		setBaseDocMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// 注册绑定鼠标单击事件，设置只可以选择文件，兵获取文件名称
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//设置默认路径为桌面
				FileSystemView fsv = FileSystemView.getFileSystemView();
				jfc.setCurrentDirectory(fsv.getHomeDirectory());

				int option = jfc.showDialog(new JLabel(), "选择");
				if (option == JFileChooser.APPROVE_OPTION) { //按确定键
					File file = jfc.getSelectedFile();
					if (file != null) {
						String value = file.getAbsolutePath();
						if (StringUtils.isNotBlank(value)) {
							menuOperation.setConfigureFileContent(ConfigureKey.savePath, value.trim());
							Toolkit.getDefaultToolkit().beep();
							JOptionPane.showMessageDialog(null, "已设置成功", "提示", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			}
		});
		setMenu.add(setBaseDocMenuItem);

		JMenuBar helpMenuBar = new JMenuBar();
		helpMenuBar.setBounds(94, 6, 40, 21);
		frmJavatool.getContentPane().add(helpMenuBar);

		JMenu helpMenu = new JMenu("帮助");
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
				//TODO 当excelCheck不为null时候才进行下面的
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
			progressBar.setString("完成！");
			lbl_successNum.setText(String.valueOf(excelCheck.SUCCESS_NUM));
			lbl_failedNum.setText(String.valueOf(excelCheck.FAILED_NUM));
			lbl_totalNum.setText(String.valueOf(excelCheck.TOTAL_NUM));
			//启用日志打开日志按钮COMPLETE=true时表示日志记录完毕
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
