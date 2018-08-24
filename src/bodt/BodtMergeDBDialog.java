package bodt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class BodtMergeDBDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField m_srcDBTxtField;
	private String     m_srcDBName;
	private boolean m_isTreadActive;
	private JProgressBar m_ProgressBar;
	private JLabel m_ExportFileNameLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			BodtMergeDBDialog dialog = new BodtMergeDBDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public BodtMergeDBDialog() {
		m_isTreadActive = false;
		setTitle("DBマージ");
		setBounds(100, 100, 450, 164);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lbldb = new JLabel("連結するDB");
		lbldb.setBounds(12, 10, 128, 13);
		contentPanel.add(lbldb);

		m_srcDBTxtField = new JTextField();
		m_srcDBTxtField.setBounds(12, 25, 341, 19);
		contentPanel.add(m_srcDBTxtField);
		m_srcDBTxtField.setColumns(10);

		JButton button = new JButton("参照");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* マージするDB1の参照ボタンが押されたとき */
				JFileChooser FileChooser = new JFileChooser(".\\");
				int sel = FileChooser.showSaveDialog(null);
				if(sel == JFileChooser.APPROVE_OPTION)
				{
					File file = FileChooser.getSelectedFile();
					m_srcDBName = file.toString();
					m_srcDBTxtField.setText(m_srcDBName);
				}
			}
		});
		button.setBounds(365, 24, 57, 21);
		contentPanel.add(button);

		m_ProgressBar = new JProgressBar();
		m_ProgressBar.setBounds(12, 72, 410, 14);
		contentPanel.add(m_ProgressBar);

		m_ExportFileNameLabel = new JLabel("進捗");
		m_ExportFileNameLabel.setBounds(12, 54, 116, 13);
		contentPanel.add(m_ExportFileNameLabel);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("DBを連結する");
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(m_isTreadActive == false)
				{
					Thread ExportThread = new Thread(){
						public void run()
						{
							/* YOLO学習用データをエクスポートします */
							/* 現在使用しているDBに別のDBを連結する */
							try
							{
								BodtDB srcDB = new BodtDB(m_srcDBName);
								Connection srcCon = srcDB.GetCon();
								int nFileNum = srcDB.GetImageTable().GetMaxImageID();
								m_ProgressBar.setMaximum(nFileNum);
								BodtApp.db.Cat(srcCon);
							}
							catch (SQLException e1)
							{
								// TODO 自動生成された catch ブロック
								e1.printStackTrace();
							} catch (ClassNotFoundException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
							}
							m_isTreadActive = false;
						}
					};

					Thread Monitorthread = new Thread() {
						@Override
						public void run() {
							while(m_isTreadActive == true)
							{
								int ImageID = BodtApp.db.GetExportImgID();
								String FileName = BodtApp.db.GetExportFileName();
								m_ProgressBar.setValue(ImageID);
								m_ExportFileNameLabel.setText(FileName);
							}
							JOptionPane.showMessageDialog(null, "マージが完了しました。閉じるボタンを押してください");
						}
					};
					m_isTreadActive = true;
					Monitorthread.start();
					ExportThread.start();
				}
			}
		});

		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("閉じる");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_isTreadActive = false;
				Component c = (Component)e.getSource();
				Window w = SwingUtilities.getWindowAncestor(c);
				w.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}

}
