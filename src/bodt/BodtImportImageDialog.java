package bodt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

/**
 * 画像データをDBに登録するダイアログ
 * @author takaomi.konishi
 *
 */
public class BodtImportImageDialog extends JDialog {

	/* ダイアログのパネル */
	private final JPanel contentPanel = new JPanel();

	/* 保存先データベースの入力テキストボックス */
	private JTextField m_DBTextField;

	/* 画像ディレクトリのあるパスを指定するテキストボックス */
	private JTextField m_ImgTextField;

	/* 取り込む画像全体の枚数と取り込みの完了した画像の枚数を表示するラベル */
	private JLabel m_ProgresMsgLabel;

	/* コピーしたファイル名を表示するラベル */
	private JLabel m_CopyFileNameLabel;

	/* 保存先データベース名 */
	private String m_DBPath;

	/* 画像取得先のパス */
	private String m_ImgPath;

	/* 取得した画像のフルパスのリスト */
	private List<String> m_ImgFileList;

	/* 取り込んでいる画像の進捗を表すプログレスバー */
	private JProgressBar m_ProgressBar;

	/* インポートボタンを押した後，画像をDBに取り込んでいる間はtrue，         */
	/* 取り込み完了前，取り込み完了後はfalseになる，スレッド状態を表すフラグ。*/
	/* 途中でキャンセルされた場合もfalseになり，スレッドは終了する。          */
	private boolean m_isTreadActive;

	/**
	 * Create the dialog.
	 */
	public BodtImportImageDialog() {
		//this.setModal(true);
		m_ImgFileList = null;
		m_isTreadActive = false;

		setTitle("プロジェクトの新規作成");
		setBounds(100, 100, 455, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel label = new JLabel("作成するデータベース名");
			label.setBounds(12, 13, 204, 16);
			contentPanel.add(label);
		}
		{
			m_DBTextField = new JTextField();
			m_DBTextField.setBounds(12, 32, 343, 22);
			contentPanel.add(m_DBTextField);
			m_DBTextField.setColumns(10);
		}
		{
			/* 保存先データベースパスの参照ダイアログ表示 */
			JButton button = new JButton("参照");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					/* 入力されたDBパスを取得する */
					JFileChooser FileChooser = new JFileChooser(".\\");
					int sel = FileChooser.showSaveDialog(null);
					if(sel == JFileChooser.APPROVE_OPTION)
					{
						File file = FileChooser.getSelectedFile();
						m_DBPath = file.toString();
						m_DBTextField.setText(m_DBPath);
					}
				}
			});
			button.setBounds(356, 31, 74, 25);
			contentPanel.add(button);
		}
		{
			JLabel label = new JLabel("学習データ作成画像ディレクトリ");
			label.setBounds(12, 72, 187, 16);
			contentPanel.add(label);
		}
		{
			m_ImgTextField = new JTextField();
			m_ImgTextField.setColumns(10);
			m_ImgTextField.setBounds(12, 97, 343, 22);
			contentPanel.add(m_ImgTextField);
			m_DBTextField.setText("D:\\temp\\test.db");
			m_ImgTextField.setText("D:\\temp\\data");
		}
		{
			/* 画像パスの参照ダイアログ表示 */
			JButton button = new JButton("参照");
			button.addActionListener(new ActionListener() {
				/** 入力された画像パスを取得する */
				public void actionPerformed(ActionEvent e) {
					JFileChooser FileChooser = new JFileChooser(".\\");
					FileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int sel = FileChooser.showSaveDialog(null);
					if(sel == JFileChooser.APPROVE_OPTION)
					{
						File file = FileChooser.getSelectedFile();
						m_ImgPath = file.toString();
						m_ImgTextField.setText(m_ImgPath);
					}
				}
			});
			button.setBounds(356, 96, 74, 25);
			contentPanel.add(button);
		}
		{
			m_ProgressBar = new JProgressBar();
			m_ProgressBar.setBounds(12, 156, 418, 22);
			contentPanel.add(m_ProgressBar);
		}
		{
			m_ProgresMsgLabel = new JLabel("");
			m_ProgresMsgLabel.setBounds(155, 132, 138, 16);
			m_ProgresMsgLabel.setHorizontalAlignment(JLabel.CENTER);
			contentPanel.add(m_ProgresMsgLabel);
		}
		{
			m_CopyFileNameLabel = new JLabel("");
			m_CopyFileNameLabel.setBounds(12, 189, 418, 16);
			contentPanel.add(m_CopyFileNameLabel);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("インポート");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						/** パラメーターチェック */
						m_DBPath = m_DBTextField.getText();
						m_ImgPath = m_ImgTextField.getText();
						if(m_DBPath.equals(""))
						{
							JOptionPane.showMessageDialog(null, "DBパスが指定されていません","BDOT", JOptionPane.ERROR_MESSAGE);
							return ;
						}
						if(m_ImgPath.equals(""))
						{
							JOptionPane.showMessageDialog(null, "画像パスが指定されていません", "BDOT",JOptionPane.ERROR_MESSAGE);
							return ;
						}
						/* DBを作成する */
						BodtApp.db.CreateBodtDB(m_DBPath);

						GetImgList(new File(m_ImgPath));
						int nFileNum = m_ImgFileList.size();
						m_ProgressBar.setMinimum(0);
						m_ProgressBar.setMaximum(nFileNum);

						// 画像取り込み用スレッド生成・実行
						if(m_isTreadActive == false)
						{
							Thread thread = new Thread() {
								@Override
								public void run() {
									for(int i = 0; i < nFileNum;i++)
									{
										if(m_isTreadActive == true)
										{
											m_ProgressBar.setValue(i+1);
											BodtApp.db.GetImageTable().Insert(m_ImgFileList.get(i));
											m_ProgresMsgLabel.setText((i+1)+"/"+nFileNum);
											m_CopyFileNameLabel.setText(m_ImgFileList.get(i));
										}
									}
									BodtApp.db.Commit();
									m_CopyFileNameLabel.setText("DBにファイルをコピーしました");
									m_isTreadActive = false;
								}
							};
							m_isTreadActive = true;
							thread.start();
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

	/**
	 * 引数に指定されたディレクトリ下にあるファイル(サブフォルダも含む)のうち，
	 * png,jpg,jpeg,PNG,JPG,JPEGの拡張子を持つファイルの一覧を取得する。
	 * @param RootDir 訓練画像のあるルートディレクトリ
	 * @since  : 2018-04-04
	 * @author : takaomi.konishi
	 */
	public void GetImgList(File RootDir)
	{
		m_ImgFileList = new ArrayList<String>();
		GetImgListCore(RootDir);
	}

	/**
	 * 引数に指定されたディレクトリ下にあるファイル(サブフォルダも含む)のうち，
	 * png,jpg,jpeg,PNG,JPG,JPEGの拡張子を持つファイルの一覧を取得する。
	 * @param RootDir 訓練画像のあるルートディレクトリ
	 * @since  : 2018-04-04
	 * @author : takaomi.konishi
	 */
	private int GetImgListCore(File DirPath)
	{
		File[] FileList = DirPath.listFiles();
		if(FileList == null)
		{
			return -1;
		}

		for(File f : FileList) {
			if(f.isDirectory())
			{
				/* ファイルがディレクトリである */
				this.GetImgListCore(f);
			}
			else if(f.isFile())
			{
				/* ファイルである */
				/* ファイルがJPGかPNGかを調べる */
				 String[] strToken = f.toString().split("\\.");
				 int nLast = strToken.length - 1;
				 if(nLast > 0)
				 {
				    if(strToken[nLast].equalsIgnoreCase("png") ||
				       strToken[nLast].equalsIgnoreCase("jpg") ||
				       strToken[nLast].equalsIgnoreCase("jpeg"))
				    {
				    	m_ImgFileList.add(f.toString());

				    }
				 }
			}
		}
		return 0;
	}

}
