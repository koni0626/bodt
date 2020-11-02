package bodt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * BasisOCR学習データ作成ツールメイン画面クラス
 * @author takaomi.konishi
 *
 */
public class BodtMainFrame extends JFrame implements KeyListener{
	/* アプリケーションのデフォルトウインドウサイズ */
	public static final int GET_DEFAULT_WINDOW_WIDTH = 1024;
	public static final int GET_DEFAULT_WINDOW_HEIGHT = 768;
	/* Frame配下のメインパネル */
	private JPanel contentPane;

	/* カテゴリリスト画面 */
	private JList m_CategoryList;

	/* カテゴリリスト表示用データ */
	private DefaultListModel m_CategoryListModel;

	/* カテゴリ登録用テキストボックス */
	private JTextField m_CategoryTextField;

	/* カテゴリ登録用ボタン */
	private JButton m_AddCategoryButton;

	/* 画像ファイル名表示領域 */
	private JLabel m_FileLabel;

	/* 画像描画用クラス */
	BodtDrawPanel m_DrawPanel;

	/**
	 * Create the frame.
	 */
	public BodtMainFrame() {
		String lafClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
		try{
		  UIManager.setLookAndFeel(lafClassName);
		  SwingUtilities.updateComponentTreeUI(this);
		}catch(Exception e){
		  e.printStackTrace();
		}
		setTitle("BasisOCR学習データ作成ツール");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/* ウインドウ起動時は1024×728で起動する */
		java.awt.GraphicsEnvironment env = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		java.awt.Rectangle desktopBounds = env.getMaximumWindowBounds();
		int c_x = (desktopBounds.x + desktopBounds.x + desktopBounds.width)/2;
		int c_y = (desktopBounds.y + desktopBounds.y + desktopBounds.height)/2;
		setBounds(c_x -GET_DEFAULT_WINDOW_WIDTH/2 , c_y - GET_DEFAULT_WINDOW_HEIGHT/2, GET_DEFAULT_WINDOW_WIDTH, GET_DEFAULT_WINDOW_HEIGHT);
		addKeyListener(this);
		setFocusable(true);
		/* 最下層のパネルはボックス横向き */
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);
		/*******************************************************/
		/*                  メニューバーの作成                 */
		/*******************************************************/
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menu = new JMenu("ファイル");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("プロジェクトを作成する");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* プロジェクトを作成するを選択した場合 */
				OnCreateProject();
			}
		});
		menu.add(menuItem);

		JMenuItem OpenPrjItem = new JMenuItem("プロジェクトを開く");
		OpenPrjItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* プロジェクトを開くを選択した場合 */
				OnOpenProject();
			}
		});
		menu.add(OpenPrjItem);

		JMenuItem DBMergeItem = new JMenuItem("DBをマージする");
		DBMergeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* プロジェクトを開くを選択した場合 */
				OnMergeDB();
			}
		});
		menu.add(DBMergeItem);

		JMenuItem YoloExportItem = new JMenuItem("YOLO用の学習データセットをエクスポートする");
		YoloExportItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* 学習データをエクスポートするを選んだ場合 */
				OnOpenYoloExportDialog();
			}
		});
		menu.add(YoloExportItem);

		JMenuItem M2DetExportItem = new JMenuItem("M2Det用の学習データセットをエクスポートする");
		M2DetExportItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* 学習データをエクスポートするを選んだ場合 */
				OnOpenM2DetExportDialog();
			}
		});
		menu.add(M2DetExportItem);
		
		JMenuItem EfficientDetExportItem = new JMenuItem("EfficientDet用の学習データセットをエクスポートする");
		EfficientDetExportItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* 学習データをエクスポートするを選んだ場合 */
				OnOpenEfficientDetExportDialog();
			}
		});
		menu.add(EfficientDetExportItem);

		JMenuItem CatExportItem = new JMenuItem("領域選択した画像をカテゴリごとに保存する");
		CatExportItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* 学習データをエクスポートするを選んだ場合 */
				OnOpenCatExportDialog();
			}
		});
		menu.add(CatExportItem);

		JMenuItem SaveItem = new JMenuItem("プロジェクトを保存する");
		SaveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* 学習データをエクスポートするを選んだ場合 */
				BodtApp.db.Commit();
				JOptionPane.showMessageDialog(null, "コミットしました");
			}
		});
		menu.add(SaveItem);

		/*******************************************************/
		/*                   左側のパネル作成                  */
		/*******************************************************/
		JPanel LeftPanel = new JPanel();
		LeftPanel.setLayout(new BoxLayout(LeftPanel, BoxLayout.Y_AXIS));
		/* ラベルを上に */
		JLabel label_1 = new JLabel("カテゴリ一覧");
		label_1.setBorder(new EmptyBorder(0, 0, 0, 0));
		LeftPanel.add(label_1);
		/* リストボックスを追加 */
		m_CategoryListModel = new DefaultListModel();
		JList m_CategoryList = new JList(m_CategoryListModel);
		m_CategoryList.setBorder(new LineBorder(new Color(0, 0, 0)));
		//m_CategoryList.setBounds(12, 24, 172, 561);
		JScrollPane listScrollPane = new JScrollPane(m_CategoryList);
		listScrollPane.setBorder(new EmptyBorder(0, 0, 5, 0));
		LeftPanel.add(listScrollPane);

		/* カテゴリ入力用のテキストボックス追加 */
		m_CategoryTextField = new JTextField();
		m_CategoryTextField.setMaximumSize(new Dimension(500,20));
		LeftPanel.add(m_CategoryTextField);

		m_AddCategoryButton = new JButton("カテゴリを追加する");
		/* 起動直後のボタンは非活性 */
		m_AddCategoryButton.setEnabled(false);
		/* ボタンクリックのアクションを登録 */
		m_AddCategoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/* カテゴリの追加ボタンを押した場合 */
				OnAddCategoryButton();
			}
		});
		LeftPanel.add(m_AddCategoryButton);
		contentPane.add(LeftPanel,BorderLayout.WEST);


		/*******************************************************/
		/*                 真ん中のパネル作成                  */
		/*******************************************************/
		JPanel CenterPanel = new JPanel();
		CenterPanel.setLayout(new BoxLayout(CenterPanel, BoxLayout.Y_AXIS));
		CenterPanel.setBorder(new EmptyBorder(0, 10, 5, 5));

		/* 上部ラベル表示 */
		m_FileLabel = new JLabel("画像");
		CenterPanel.add(m_FileLabel);

		/* 画像表示の背景パネル作成 */
		JScrollPane BackGroundPanel = new BodtBasisPanel("./img/basis.jpg");
		BackGroundPanel.setLayout(null);
		/* 画像表示専用パネルの作成 */
		m_DrawPanel = new BodtDrawPanel();
		BackGroundPanel.setViewportView(m_DrawPanel);
		m_DrawPanel.setFocusable(true);
		/* 画像表示パネルを画像表示の背景パネルに追加 */
		BackGroundPanel.add(m_DrawPanel);
		/* 画像表示背景パネルを真ん中のパネルに追加 */
		CenterPanel.add(BackGroundPanel);
		/* 真ん中のパネルをフレームのパネルに追加 */
		contentPane.add(CenterPanel);

		/* 下部のボタンパネル作成 */
		JPanel ButtonPanel = new JPanel();
		ButtonPanel.setMaximumSize(new Dimension(1920,20));
		ButtonPanel.setLayout(new GridLayout(1,3));
		JButton NextButton = new JButton("次の画像(n)");
		NextButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				int imgID = m_DrawPanel.OnPressKeyNext();
				//String FileName = BodtApp.db.GetImageTable().SelectFileName(imgID);
				//m_FileLabel.setText(FileName);

			}
		}));

		JButton EnterButton = new JButton("確定(enter)");
		EnterButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				m_DrawPanel.OnPressKeyEnter();
			}
		}));

		JButton SearchButton = new JButton("検索(s)");
		SearchButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				m_DrawPanel.OnPressKeySearch();
			}
		}));

		JButton InfoButton = new JButton("情報(i)");
		InfoButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				m_DrawPanel.OnPressKeyInfo();
			}
		}));

		JButton ClearButton = new JButton("クリア(c)");
		ClearButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				m_DrawPanel.OnClearRect();
			}
		}));

		JButton PrevButton = new JButton("前の画像(p)");
		PrevButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				int imgID = m_DrawPanel.OnPressKeyPrev();
				String FileName = BodtApp.db.GetImageTable().SelectFileName(imgID);
				//m_FileLabel.setText(FileName);
			}
		}));

		JButton RotateButton = new JButton("回転(r)");
		RotateButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				m_DrawPanel.OnPressRotateKey();
			}
		}));

		JButton MoveButton = new JButton("移動");
		MoveButton.addActionListener((new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO 自動生成されたメソッド・スタブ
				int imgID = m_DrawPanel.OnPressMoveKey();
				String FileName = BodtApp.db.GetImageTable().SelectFileName(imgID);
			}
		}));
		ButtonPanel.add(PrevButton);
		ButtonPanel.add(ClearButton);
		ButtonPanel.add(EnterButton);
		ButtonPanel.add(SearchButton);
		ButtonPanel.add(InfoButton);
		ButtonPanel.add(NextButton);
		ButtonPanel.add(RotateButton);
		ButtonPanel.add(MoveButton);
		CenterPanel.add(ButtonPanel);
	}

	/**
	 * 新規作成を選んだ時に呼ばれる。
	 * ・BodtImportImageDialogをコールし，画像データをDBにインポートする。
	 * ・インポートが完了したら，画面に画像を表示する。
	 * ・カテゴリ登録ボタンを活性化する。
	 * @author takaomi.konishi
	 */
	private void OnCreateProject()
	{
		try {
			BodtImportImageDialog dialog = new BodtImportImageDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			/* ここで画像表示とか行う */
			m_DrawPanel.Reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_AddCategoryButton.setEnabled(true);
		//m_YoloExport.setEnabled(true);
	}

	/**
	 * 「プロジェクトを開く」をクリックされたときに呼ばれる。
	 * ・ファイルダイアログで選択されたデータベースを開く。
	 * ・カテゴリ一覧，画像データ，訓練データを読み込み,表示する。
	 * @author takaomi.konishi
	 */
	private void OnOpenProject()
	{
		JFileChooser FileChooser = new JFileChooser("D:\\temp");
		int sel = FileChooser.showOpenDialog(null);
		if(sel == JFileChooser.APPROVE_OPTION)
		{
			File file = FileChooser.getSelectedFile();
			/* CreateDB関数は，DBがすでにある場合は開くだけの動作になる */
			BodtApp.db.CreateBodtDB(file.toString());
			BodtCategoryTable CatTbl = BodtApp.db.GetCategoryTable();
			List<String>NameList = CatTbl.SelectAll();
			m_CategoryListModel.clear();
			for(String s : NameList)
			{
				m_CategoryListModel.addElement(s);
			}
			m_AddCategoryButton.setEnabled(true);
			//m_YoloExport.setEnabled(true);
			m_DrawPanel.Reset();
		}
	}

	/**
	 * 「DBをマージする」をクリックされたときに呼ばれる
	 * @author takaomi.konishi
	 */
	private void OnMergeDB()
	{
		try
		{
			BodtMergeDBDialog dialog = new BodtMergeDBDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			/* ここで画像表示とか行う */
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 「yolo用学習データをエクスポートする」をクリックしたときに呼ばれる。
	 * @author takaomi.konishi
	 */
	private void OnOpenYoloExportDialog()
	{
		try {
			BodtExportDialog dialog = new BodtExportDialog(BodtExportDialog.EXPORT_YOLO,"YOLO用学習データのエクスポート");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			/* ここで画像表示とか行う */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void OnOpenCatExportDialog()
	{
		try {
			BodtExportDialog dialog = new BodtExportDialog(BodtExportDialog.EXPORT_CLASS,"切り出し画像のエクスポート");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			/* ここで画像表示とか行う */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void OnOpenM2DetExportDialog()
	{
		try {
			BodtExportDialog dialog = new BodtExportDialog(BodtExportDialog.EXPORT_M2DET,"M2Det用学習データのエクスポート");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			/* ここで画像表示とか行う */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void OnOpenEfficientDetExportDialog()
	{
		try {
			BodtExportDialog dialog = new BodtExportDialog(BodtExportDialog.EXPORT_EFFICIENTDET,"EfficientDet用学習データのエクスポート");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModal(true);
			dialog.setVisible(true);
			/* ここで画像表示とか行う */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * カテゴリ追加ボタンをされたときにコールされる。
	 * ・カテゴリ表示リストに追加されたカテゴリを登録する
	 * ・DBのカテゴリテーブルにカテゴリを追加する。
	 * ・空文字，二重登録の場合は何もせず即座にリターンする
	 * ・二重登録は大文字，小文字を区別しない。APPLEとappleは同じとみなす
	 * @author takaomi.konishi
	 */
	public void OnAddCategoryButton()
	{
		BodtCategoryTable tbl = BodtApp.db.GetCategoryTable();
		if(tbl != null)
		{
			String strCategory = m_CategoryTextField.getText();
			if(strCategory.equals(""))
			{
				/* 空文字は何もしない */
				return;
			}

			/* 重複チェック */
			List<String>CatList = tbl.SelectAll();
			boolean bDupChk = false;
			for(String s:CatList)
			{
				if(s.equals(strCategory))
				{
					/* すでに追加済みのカテゴリ */
					bDupChk = true;
					break;
				}
			}
			if(bDupChk == false)
			{
				m_CategoryListModel.addElement(strCategory);
				tbl.Insert(strCategory);
				BodtApp.db.Commit();
				m_CategoryTextField.setText("");
			}
			else
			{
				JOptionPane.showMessageDialog(null, strCategory+"はすでに登録されています");
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		int  key = e.getKeyCode();
		switch(key)
		{
		case KeyEvent.VK_ENTER:
			m_DrawPanel.OnPressKeyEnter();
			System.out.println("press enter");
			break;

		case KeyEvent.VK_N:
			m_DrawPanel.OnPressKeyNext();
			break;

		case KeyEvent.VK_P:
			 m_DrawPanel.OnPressKeyPrev();
			break;

		case KeyEvent.VK_C:
			m_DrawPanel.OnClearRect();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
	}

}
