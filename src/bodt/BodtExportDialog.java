package bodt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

public class BodtExportDialog extends JDialog{
	private final JPanel contentPanel = new JPanel();
	private JTextField m_OutputDirTxt;
	private JProgressBar m_ProgressBar;
	private JLabel m_ExportFileNameLabel;
	private boolean m_isTreadActive;
	private int m_ExportKind;
	public static final int EXPORT_YOLO = 0;
	public static final int EXPORT_CLASS = 1;
	public static final int EXPORT_M2DET = 2;
	/**
	 * Launch the application.
	 */


	/**
	 * Create the dialog.
	 */
	public BodtExportDialog(int nKind,String Title) {
		m_ExportKind = nKind;
		m_isTreadActive = false;
		setTitle(Title);
		setBounds(100, 100, 450, 192);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		m_OutputDirTxt = new JTextField();
		m_OutputDirTxt.setBounds(12, 24, 313, 22);
		contentPanel.add(m_OutputDirTxt);
		m_OutputDirTxt.setColumns(10);

		JLabel label = new JLabel("出力先ディレクトリ");
		label.setBounds(12, 0, 169, 22);
		contentPanel.add(label);

		JButton button = new JButton("参照");
		/* YOLO学習用データ保存先ディレクトリの選択 */
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser FileChooser = new JFileChooser(".\\");
				FileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int sel = FileChooser.showSaveDialog(null);
				if(sel == JFileChooser.APPROVE_OPTION)
				{
					File file = FileChooser.getSelectedFile();
					m_OutputDirTxt.setText(file.toString());
				}
			}
		});
		button.setBounds(337, 23, 78, 25);
		contentPanel.add(button);

		m_ExportFileNameLabel = new JLabel("");
		m_ExportFileNameLabel.setBounds(22, 59, 351, 16);
		contentPanel.add(m_ExportFileNameLabel);

		m_ProgressBar = new JProgressBar();
		m_ProgressBar.setBounds(12, 80, 403, 22);
		contentPanel.add(m_ProgressBar);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("出力する");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String strOutputDir = m_OutputDirTxt.getText();
						if(strOutputDir.equals(""))
						{
							JOptionPane.showMessageDialog(null, "出力先ディレクトリを指定してください");
							return;
						}
						File dir = new File(strOutputDir);
						if(!dir.isDirectory())
						{
							JOptionPane.showMessageDialog(null, "ディレクトリがありません");
							return;
						}
						if(!BodtApp.db.isOpenDB())
						{
							JOptionPane.showMessageDialog(null, "DBを開いてください");
							return ;
						}
						m_ProgressBar.setMinimum(0);

						// 画像取り込み用スレッド生成・実行
						if(m_isTreadActive == false)
						{
							Thread ExportThread = new Thread(){
								public void run(){
									int nFileNum = 0;
									/* YOLO学習用データをエクスポートします */
									switch(m_ExportKind)
									{
									case EXPORT_YOLO:
										nFileNum = BodtApp.db.GetImageTable().GetMaxImageID();
										m_ProgressBar.setMaximum(nFileNum);
										BodtApp.db.ExportDBToYolo(strOutputDir);
										break;

									case EXPORT_CLASS:
										nFileNum = BodtApp.db.GetTrainDataTable().GetRecordNum();
										System.out.println("recordnum:"+nFileNum);
										m_ProgressBar.setMaximum(nFileNum);
										BodtApp.db.ExportDBToClass(strOutputDir);
										break;
									case EXPORT_M2DET:
										nFileNum = BodtApp.db.GetImageTable().GetMaxImageID();
										m_ProgressBar.setMaximum(nFileNum);
										BodtApp.db.ExportDBToM2Det(strOutputDir);
										break;

									default:
										break;
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
									JOptionPane.showMessageDialog(null, "エクスポートが完了しました。閉じるボタンを押してください");
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
			}
			{
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
	}

}
