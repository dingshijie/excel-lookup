package my.tool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

public class FieldMatchDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6640515799286871100L;
	private final JPanel contentPanel = new JPanel();
	private JTable table;

	private MenuOperation menuOperation = new MenuOperation();
	private JTextField oldField;
	private JTextField newField;

	private JPopupMenu jPopupMenu = new JPopupMenu();
	private JMenuItem delete = new JMenuItem("ɾ��");

	/**
	 * Create the dialog.
	 */
	public FieldMatchDialog() {
		setTitle("��ƥ����Ϣ");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 335, 422);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{

			table = new JTable(new DefaultTableModel(new Object[][] {}, new String[] { "ԭʼ����", "ƥ�������" }));
			table.setBounds(10, 28, 279, 170);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			table.setShowHorizontalLines(true);
			setMatchField();
			JScrollPane jScrollPane = new JScrollPane(table);
			jScrollPane.setBounds(10, 109, 299, 232);

			contentPanel.add(jScrollPane);

		}

		JLabel label = new JLabel("����µ�ƥ����");
		label.setFont(new Font("����", Font.PLAIN, 12));
		label.setBounds(10, 10, 86, 15);
		contentPanel.add(label);

		oldField = new JTextField();
		oldField.setBounds(78, 35, 120, 21);
		contentPanel.add(oldField);
		oldField.setColumns(10);

		JLabel oldFiledlabel = new JLabel("ԭʼ����");
		oldFiledlabel.setFont(new Font("����", Font.PLAIN, 12));
		oldFiledlabel.setBounds(10, 39, 58, 15);
		contentPanel.add(oldFiledlabel);

		JLabel newFieldlabel = new JLabel("ƥ������");
		newFieldlabel.setFont(new Font("����", Font.PLAIN, 12));
		newFieldlabel.setBounds(10, 67, 58, 15);
		contentPanel.add(newFieldlabel);

		newField = new JTextField();
		newField.setBounds(78, 66, 120, 21);
		contentPanel.add(newField);
		newField.setColumns(10);

		JButton addFieldMatch = new JButton("���");
		addFieldMatch.setFont(new Font("����", Font.PLAIN, 12));
		addFieldMatch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (StringUtils.isNotBlank(oldField.getText()) && StringUtils.isNotBlank(newField.getText())) {
					menuOperation.setFieldMatchFileContent(oldField.getText(), newField.getText());
					setMatchField();
					oldField.setText("");
					newField.setText("");
				}
			}
		});
		addFieldMatch.setBounds(208, 35, 59, 47);
		contentPanel.add(addFieldMatch);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("�ر�");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dispose();
					}
				});

				JButton delButton = new JButton("ɾ��ѡ����");
				delButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						//TODO ɾ��ѡ����
						int row = table.getSelectedRow();
						System.out.println(row);
						if (row == -1) {
							JOptionPane.showMessageDialog(null, "��ѡ��Ҫɾ�����У�", "��ʾ", JOptionPane.WARNING_MESSAGE);
						} else {
							DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
							String key = tableModel.getValueAt(row, 0).toString();//��ȡѡ���еĵ�һ�е�����
							menuOperation.deleteFieldMatchFileContent(key);
							tableModel.removeRow(row);
							setMatchField();
						}
					}
				});
				delButton.setFont(new Font("����", Font.PLAIN, 12));
				buttonPane.add(delButton);
				cancelButton.setFont(new Font("����", Font.PLAIN, 12));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setMatchField() {
		clearMatchField();
		Properties prop = menuOperation.readFieldMatchFile();
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		SortedMap sortedMap = new TreeMap(prop);//��properties����
		Iterator<Object> iterator = sortedMap.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = sortedMap.get(key).toString();
			Vector v = new Vector();
			v.add(key);
			v.add(value);
			tableModel.addRow(v);//���һ������
		}
	}

	/**
	 * ���table������
	 */
	private void clearMatchField() {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		while (tableModel.getRowCount() > 0) {
			tableModel.removeRow(tableModel.getRowCount() - 1);
		}
	}

}
