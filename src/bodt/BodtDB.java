package bodt;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * データ保存用DBクラス。SQLITE3を使用する。
 * 画像データも丸ごとDBに保存する。
 * 画像データはImageTable,カテゴリはCategoryTable,学習データはTrainTableで管理する。
 * Propertyテーブルはどんな内容の学習データかなど、コメントを記述するテーブル。
 * @author takaomi.konishi
 *
 */
public class BodtDB {

	/* DB接続ハンドル */
	private Connection m_Con;

	/* カテゴリテーブルクラス */
	private BodtCategoryTable m_CatTable;

	/* 訓練用データテーブルクラス */
	private BodtTrainDataTable m_TrainTable;

	/* 画像データ管理テーブルクラス */
	private BodtImageTable m_ImageTable;

	/* DBのコメントなどを管理するクラス。現在未使用。 */
	private BodtPropertyTable m_PropertyTable;

	/* 現在エクスポート中のYOLO学習ファイル名。進捗表示用 */
	private String m_ExportFileName;

	/** 現在エクスポート中のイメージID */
	private int m_ExportImageID;

	/**
	 * コンストラクタ。GUIとして使用する場合はこちらのコンストラクタをコールした後，
	 * CreateBodtDBを呼ぶ方式にしておく
	 * @author takaomi.konishi
	 */
	public BodtDB()
	{
		m_Con = null;
		m_CatTable = null;
		m_TrainTable = null;
		m_ImageTable = null;
		m_PropertyTable = null;
	}

	/**
	 * DBがオープンしているか調べる
	 * @return true:オープンしている
	 *          false:開いていない
	 */
	public boolean isOpenDB()
	{
		boolean bRet = false;
		if(m_Con != null)
		{
			bRet = true;
		}
		return bRet;
	}

	/**
	 * DBハンドルを返す
	 * @author takaomi.konishi
	 */
	public Connection GetCon()
	{
		return m_Con;
	}
	/**
	 * DBがない場合は新規作成する。ある場合はハンドルだけ取る。コマンドラインから起動する場合は本コンストラクタを
	 * 呼ぶだけで、CreateBodtDBは呼ばなくてよい。
	 * @param strDBName ファイル名。
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public BodtDB(String strDBName) throws ClassNotFoundException, SQLException
	{
        try
        {
            Class.forName("org.sqlite.JDBC");
            /* Windowsのフルパスは"jdbc:sqlite:D:/java/db/foo.db"で指定する */
            String strDBPath = "jdbc:sqlite:"+strDBName;
            m_Con = DriverManager.getConnection(strDBPath);
            /* insertが遅すぎるので，自動コミットはやらない */
            m_Con.setAutoCommit(false);

            /** DBテーブルを作成する */
            m_CatTable =   new BodtCategoryTable(m_Con);
            m_TrainTable = new BodtTrainDataTable(m_Con);
            m_ImageTable = new BodtImageTable(m_Con);
            m_PropertyTable = new BodtPropertyTable(m_Con);
            m_Con.commit();

        }
        catch (ClassNotFoundException e)
        {
           	JOptionPane.showMessageDialog(null,"BODT0001-E sqlite-jdbc-3.21.0.jarがありません" );
            e.printStackTrace();
            throw e;
        }
        catch (SQLException e)
        {
			// TODO 自動生成された catch ブロック
        	JOptionPane.showMessageDialog(null,"BODT0002-E "+strDBName+"が開けませんでした");
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * データベースを新規作成する。DBがある場合はハンドルだけ取る。GUIから開く場合は本メソッドを使用する。
	 * @param strDBName 作成するDBパス
	 * @return 0 正常
	 *         -1 作成失敗
	 */
	public int CreateBodtDB(String strDBName)
	{
		int nRet = 0;
        try
        {
            Class.forName("org.sqlite.JDBC");
            /* Windowsのフルパスは"jdbc:sqlite:D:/java/db/foo.db"で指定する */
            String strDBPath = "jdbc:sqlite:"+strDBName;
          //  System.out.println(strDBPath+"に接続します");
            m_Con = DriverManager.getConnection(strDBPath);
          //  System.out.println(strDBPath+"に接続しました");
            m_Con.setAutoCommit(false);

            /** DBテーブルを作成する */
            m_CatTable =   new BodtCategoryTable(m_Con);
            m_TrainTable = new BodtTrainDataTable(m_Con);
            m_ImageTable = new BodtImageTable(m_Con);
            m_Con.commit();

        }
        catch (ClassNotFoundException e)
        {
           	JOptionPane.showMessageDialog(null,"BODT0001-E sqlite-jdbc-3.21.0.jarがありません" );
            e.printStackTrace();
            nRet = -1;
        }
        catch (SQLException e)
        {
			// TODO 自動生成された catch ブロック
        	JOptionPane.showMessageDialog(null,"BODT0002-E "+strDBName+"が開けませんでした");
        	e.printStackTrace();
			nRet = -1;
		}

        return nRet;
	}

	/** 画像管理テーブルのクラスを取得する。
	 * @return 画像管理テーブルクラス。DBがオープンされていない状態でコールした場合はnullを返す。
	 * @author takaomi.konishi
	 */
	public BodtImageTable GetImageTable()
	{
		return m_ImageTable;
	}

	/**
	 * 訓練データ管理テーブルのクラスを取得する。
	 * @return 訓練データ管理テーブルクラスを返却する。DBがオープンされていない場合はnullを返す。
	 * @author takaomi.konishi
	 */
	public BodtTrainDataTable GetTrainDataTable()
	{
		return m_TrainTable;
	}

	/**
	 * カテゴリ管理テーブルのクラスを取得する。
	 * @return カテゴリ管理テーブルクラス。DBがオープンされていない場合はnullを返す。
	 * @author takaomi.konishi
	 */
	public BodtCategoryTable GetCategoryTable()
	{
		return m_CatTable;
	}

	/**
	 * DBをコミットする。insert,createtableを実行した場合はコール元でコミットすること。
	 * @author : takaomi.konishi
	 */
	public void Commit()
	{
		try {
			if(m_Con != null)m_Con.commit();
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * 矩形で指定された画像をカテゴリごとに保存する
	 */
	public int ExportDBToClass(String strOutputDir)
	{
		int nRet = 0;

		if(m_Con == null)
		{
			JOptionPane.showMessageDialog(null, "データベースを作成するか，オープンしてください");
			return -1;
		}

		/* カテゴリの数を取得する */
		BodtCategoryTable catTable = BodtApp.db.GetCategoryTable();
		BodtTrainDataTable trainTable = BodtApp.db.GetTrainDataTable();
		BodtImageTable imgTable = BodtApp.db.GetImageTable();
		int nCatNum = catTable.GetCatNum();
		for(int i = 0; i < nCatNum;i++)
		{
			String OutputCatDir = String.format("%s/%04d", strOutputDir,i);
			File dir = new File(OutputCatDir);
			dir.mkdirs();
		}

		/* Trainテーブルのレコード数を取得 */
		ResultSet rs = trainTable.SelectImgIDAll();
		int nProgress = 0;
		try
		{
			while (rs.next())
			{
				int nImageID = rs.getInt(1);
				BufferedImage img = imgTable.SelectImage(nImageID);
				List<BodtObjectData> ObjecDatatList = trainTable.Select(nImageID);
				for(BodtObjectData d :ObjecDatatList)
				{
					nProgress++;
					int nCat = d.GetCategory();
					double x = d.GetRect().x;
					double y = d.GetRect().y;
					double w = d.GetRect().width;
					double h = d.GetRect().height;
					if( nCat < 0 || x < 0 || y < 0 || w < 0 || h < 0)
					{
						continue;
					}
					if(img.getWidth() < x + w || img.getHeight() < y + h)
					{
						continue;
					}
					BufferedImage cutimg = img.getSubimage((int)x, (int)y, (int)w, (int)h);
					String strUUID = UUID.randomUUID().toString();
					String extFileName = imgTable.SelectImageFileName(nImageID);
					String FullPath = "";
					extFileName = extFileName.toUpperCase();
					if(extFileName.endsWith(".JPG") ||extFileName.endsWith(".JPEG")) {
						FullPath = String.format("%s/%04d/%s.jpg",strOutputDir,nCat-1,strUUID);
					}
					else if(extFileName.endsWith(".PNG")) {
						FullPath = String.format("%s/%04d/%s.png",strOutputDir,nCat-1,strUUID);
					}
					else if(extFileName.endsWith(".GIF")) {
						FullPath = String.format("%s/%04d/%s.gif",strOutputDir,nCat-1,strUUID);
					}
					SaveImage(cutimg,FullPath);
					SetExportInfo(nProgress,FullPath+"をエクスポートしました");

				}
			}
			SetExportInfo(nProgress,"エクスポートが完了しました");
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}



		return nRet;
	}

	/**
	 * YOLO学習用のデータセットをエクスポートする
	 * @param strOutputDir 出力先ディレクトリ
	 * @return :0正常
	 *          -1失敗
	 */
	public int ExportDBToYolo(String strOutputDir)
	{
		int nRet = 0;
		//strOutputDir = "/home/darknet/<change_dir_name>";

		String strDataDir = strOutputDir + "/data";
		String strCfgDir = strOutputDir + "/cfg";
		String strBkDir = strOutputDir + "/backup";
		String strClassList = strCfgDir + "/basis.names";
		String strCfgFile = strCfgDir + "/basis.data";
		String strYoloExportDirName = "";

		if(m_Con == null)
		{
			JOptionPane.showMessageDialog(null, "データベースを作成するか，オープンしてください");
			return -1;
		}
		Random rnd = new Random();

		/** 以下の内容を出力する
		 * ./data/画像.jpg
		 * ./data/画像.txt
		 * ./data/basis.names(クラスリスト)
		 * ./train.txt
		 * ./test.txt
		 * ./cfg/basis.data(コンフィグファイル)
		 */

		/* dataディレクトリの作成 */
		File DataDir = new File(strDataDir);
		DataDir.mkdirs();
		String FullPath = strOutputDir.toString();
		int nLast = FullPath.length();
		if(nLast > 0 && FullPath.substring(FullPath.length() - 1) == "/")
		{
			FullPath = FullPath.substring(0,FullPath.length()-1);
		}
		String [] Dirs = FullPath.split("/");
		nLast = Dirs.length;
		strYoloExportDirName = Dirs[nLast-1];

		/* cfgディレクトリの作成 */
		File CfgDir = new File(strCfgDir);
		CfgDir.mkdirs();

		/* backupディレクトリの作成 */
		File backupdir = new File(strBkDir);
		backupdir.mkdirs();

		/* train.txtを開く */
		PrintWriter TrainTxtWriter = null;
		PrintWriter TestTxtWriter = null;
		try
		{
			String strUUID = UUID.randomUUID().toString();
			String strTrainTxt = String.format("%s/train.txt", strOutputDir);
			String strTestTxt = String.format("%s/test.txt", strOutputDir);
			File file = new File(strTrainTxt);
			TrainTxtWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			file = new File(strTestTxt);
			TestTxtWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			/* データを出力する */
			BodtImageTable ImgTbl = BodtApp.db.GetImageTable();
			int MaxID = ImgTbl.GetMaxImageID();
			for(int ImageID = 1; ImageID <= MaxID; ImageID++)
			{
				strUUID = UUID.randomUUID().toString();

				String strJpgName = "/home/darknet/baps3/data/"+strUUID;
				String FullPathJpg = strOutputDir+"/data/" + strUUID;
				String FullPathTxt =   strOutputDir+"/data/" + strUUID +".txt";


				/* 画像テーブルから画像データを取得する */
				BufferedImage image = ImgTbl.SelectImage(ImageID);
				String extFileName = ImgTbl.SelectImageFileName(ImageID);
				extFileName = extFileName.toUpperCase();
				if(extFileName.endsWith(".JPG") ||extFileName.endsWith(".JPEG")) {
					strJpgName = strJpgName + ".jpg";
					FullPathJpg = FullPathJpg + ".jpg";
				}
				else if(extFileName.endsWith(".PNG")) {
					strJpgName = strJpgName + ".png";
					FullPathJpg = FullPathJpg + ".png";
				}
				else if(extFileName.endsWith(".GIF")) {
					strJpgName = strJpgName + ".gif";
					FullPathJpg = FullPathJpg + ".gif";
				}


				/* 座標データを出力する */
				nRet = SaveRectTxtForYolo(ImageID,image.getWidth(),image.getHeight(),FullPathTxt);
				if(nRet == -1)
				{
					/* コール元ダイアログに表示するための画像ファイル名をセットする */
					SetExportInfo(ImageID,strJpgName+"はスキップします");
					File text = new File(FullPathTxt);
					text.delete();
					continue;
				}
				else
				{
					SetExportInfo(ImageID,strJpgName+"をエクスポートしました");
				}

				/* 画像データを出力する */
				nRet = SaveImage(image,FullPathJpg);
				if(nRet != 0)
				{
					JOptionPane.showMessageDialog(null, strJpgName+"が作成できません");
				}

				int nSel = rnd.nextInt(100);
				/* データの8割くらいをトレーニングデータとする */
				if(nSel < 90)
				{
					/* トレーニングファイルリストを出力する */
					TrainTxtWriter.print(strJpgName+"\n");
				}
				else
				{
					/* 訓練用ファイルリストを出力する */
					TestTxtWriter.print(strJpgName+"\n");
				}
			}
			TrainTxtWriter.close();
			TestTxtWriter.close();
		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		/* カテゴリ一覧を出力する */
		nRet = SaveCategoryTxtForYolo(strClassList);

		/* コンフィグファイルを生成する */
		nRet = SaveCfgFileForYolo(strYoloExportDirName,strCfgFile);


		return nRet;
	}

	/**
	 * M2Det学習用のデータセットをエクスポートする
	 * @param strOutputDir 出力先ディレクトリ
	 * @return :0正常
	 *          -1失敗
	 */
	public int ExportDBToM2Det(String strOutputDir)
	{
		int nRet = 0;
		String strDataDir = strOutputDir + "/data";
		String strCfgDir = strOutputDir + "/cfg";
		String strBkDir = strOutputDir + "/backup";
		String strClassList = strCfgDir + "/basis.names";
		String strCfgFile = strCfgDir + "/basis.data";
		String strYoloExportDirName = "";

		if(m_Con == null)
		{
			JOptionPane.showMessageDialog(null, "データベースを作成するか，オープンしてください");
			return -1;
		}
		Random rnd = new Random();

		/** 以下の内容を出力する
		 * ./data/画像.jpg
		 * ./data/画像.txt
		 * ./data/basis.names(クラスリスト)
		 * ./train.txt
		 * ./test.txt
		 * ./cfg/basis.data(コンフィグファイル)
		 */

		/* dataディレクトリの作成 */
		File DataDir = new File(strDataDir);
		DataDir.mkdirs();
		String FullPath = strOutputDir.toString();
		int nLast = FullPath.length();
		if(nLast > 0 && FullPath.substring(FullPath.length() - 1) == "/")
		{
			FullPath = FullPath.substring(0,FullPath.length()-1);
		}
		String [] Dirs = FullPath.split("/");
		nLast = Dirs.length;
		strYoloExportDirName = Dirs[nLast-1];

		/* cfgディレクトリの作成 */
		File CfgDir = new File(strCfgDir);
		CfgDir.mkdirs();

		/* backupディレクトリの作成 */
		File backupdir = new File(strBkDir);
		backupdir.mkdirs();

		/* train.txtを開く */
		PrintWriter TrainTxtWriter = null;
		PrintWriter TestTxtWriter = null;
		try
		{
			String strUUID = UUID.randomUUID().toString();
			String strTrainTxt = String.format("%s/%s_train.txt", strOutputDir,strUUID);
			String strTestTxt = String.format("%s/%s_test.txt", strOutputDir,strUUID);
			File file = new File(strTrainTxt);
			TrainTxtWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			file = new File(strTestTxt);
			TestTxtWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));

			/* データを出力する */
			BodtImageTable ImgTbl = BodtApp.db.GetImageTable();
			int MaxID = ImgTbl.GetMaxImageID();
			for(int ImageID = 1; ImageID <= MaxID; ImageID++)
			{
				strUUID = UUID.randomUUID().toString();

				String strJpgName = "./"+strYoloExportDirName+"/data/"+strUUID;
				String FullPathJpg = strDataDir + "/" + strUUID;
				String FullPathTxt =  strDataDir + "/" + strUUID+".txt";


				/* 画像テーブルから画像データを取得する */
				BufferedImage image = ImgTbl.SelectImage(ImageID);
				String extFileName = ImgTbl.SelectImageFileName(ImageID);
				extFileName = extFileName.toUpperCase();
				if(extFileName.endsWith(".JPG") ||extFileName.endsWith(".JPEG")) {
					strJpgName = strJpgName + ".jpg";
					FullPathJpg = FullPathJpg + ".jpg";
				}
				else if(extFileName.endsWith(".PNG")) {
					strJpgName = strJpgName + ".png";
					FullPathJpg = FullPathJpg + ".png";
				}
				else if(extFileName.endsWith(".GIF")) {
					strJpgName = strJpgName + ".gif";
					FullPathJpg = FullPathJpg + ".gif";
				}


				/* 座標データを出力する */
				nRet = SaveRectTxtForM2Det(ImageID,image.getWidth(),image.getHeight(),FullPathTxt);
				if(nRet == -1)
				{
					/* コール元ダイアログに表示するための画像ファイル名をセットする */
					SetExportInfo(ImageID,strJpgName+"はスキップします");
					File text = new File(FullPathTxt);
					text.delete();
					continue;
				}
				else
				{
					SetExportInfo(ImageID,strJpgName+"をエクスポートしました");
				}

				/* 画像データを出力する */
				nRet = SaveImage(image,FullPathJpg);
				if(nRet != 0)
				{
					JOptionPane.showMessageDialog(null, strJpgName+"が作成できません");
				}

				int nSel = rnd.nextInt(100);
				/* データの8割くらいをトレーニングデータとする */
				if(nSel < 90)
				{
					/* トレーニングファイルリストを出力する */
					TrainTxtWriter.print(strJpgName+"\n");
				}
				else
				{
					/* 訓練用ファイルリストを出力する */
					TestTxtWriter.print(strJpgName+"\n");
				}
			}
			TrainTxtWriter.close();
			TestTxtWriter.close();
		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		/* カテゴリ一覧を出力する */
		nRet = SaveCategoryTxtForYolo(strClassList);

		/* コンフィグファイルを生成する */
		nRet = SaveCfgFileForYolo(strYoloExportDirName,strCfgFile);


		return nRet;
	}
	/**
	 * 画像を保存する
	 * @param image 画像のバイナリデータ
	 * @param strJpgName 出力先ファイル名
	 * @return 0 作成できた
	 *         -1 失敗
	 */
	private int SaveImage(BufferedImage image,String strJpgName)
	{
		int nRet = 0;
		try
		{
			OutputStream out=new FileOutputStream(strJpgName);//ファイルとアプリを繋ぐ
			String extFileName = strJpgName.toUpperCase();
			if(extFileName.endsWith(".JPG") ||extFileName.endsWith(".JPEG")) {
				ImageIO.write(image, "jpg", out);//指定の形式で出力
			}
			else if(extFileName.endsWith(".PNG")) {
				ImageIO.write(image, "png", out);//指定の形式で出力
			}
			else if(extFileName.endsWith(".GIF")) {
				ImageIO.write(image, "gif", out);//指定の形式で出力
			}


		}
		catch(IOException e)
		{
		      //例外処理
			nRet = -1;
		}
		return nRet;
	}

	/**
	 * 現在エクスポート中のファイル名を取得する。
	 * @return ファイル名
	 * @author takaomi.konishi
	 */
	public synchronized String GetExportFileName()
	{
		return m_ExportFileName;
	}

	/**
	 * エクスポートするファイル名をセットする
	 * @param  ImageID エクスポートするイメージID
	 *          FileName エクスポートするファイル名
	 * @author takaomi.konishi
	 */
	public synchronized void SetExportInfo(int ImageID,String FileName)
	{
		m_ExportImageID = ImageID;
		m_ExportFileName = FileName;
	}

	/**
	 * 現在エクスポート中のイメージID
	 * @return エクスポート中のイメージID
	 */
	public synchronized int GetExportImgID()
	{
		return m_ExportImageID;
	}


	/**
	 * YOLO用の学習データを作成する
	 * @param ImageID 作成したい画像ID
	 * @param s_w オリジナル画像の幅
	 * @param s_h オリジナル画像の高さ
	 * @param strTxtFile 出力ファイル名
	 * @return 0 書き込み成功
	 *         -1 書き込み失敗
	 */
	private int SaveRectTxtForYolo(int ImageID,float s_w,float s_h,String strTxtFile)
	{
		int nRet = 0;
		BodtTrainDataTable TrainDataTbl = BodtApp.db.GetTrainDataTable();
		File file = new File(strTxtFile);
		NORMAL:try
		{
			List<BodtObjectData> ld = TrainDataTbl.Select(ImageID);
			if(ld.size() == 0)
			{
				nRet = -1;
				break NORMAL;
			}
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for(BodtObjectData d: ld)
			{
				/* 矩形の中心のX、Yと矩形の幅と高さを求めて正規化する */
				Rectangle2D.Double r = d.GetRect();
				if(r.getX() < 0. || r.getY() < 0.0 ||  r.getWidth() < 0.0 || r.getHeight() < 0.0)
				{
					nRet = -1;
					break;
				}
				double c_x = r.getCenterX()/s_w;
				double c_y = r.getCenterY()/s_h;
				double c_w = r.getWidth()/s_w;
				double c_h = r.getHeight()/s_h;


				int cat = d.GetCategory()-1;
				if(cat < 0) {
					continue;
				}
				String Record = cat + " " + c_x + " " + c_y + " " + c_w + " " + c_h;
				/* ファイルに書き込む */
				pw.print(Record+"\n");
			}
			pw.close();
		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			nRet = -1;
		}

	return nRet;
	}


	/**
	 * YOLO用の学習データを作成する
	 * @param ImageID 作成したい画像ID
	 * @param s_w オリジナル画像の幅
	 * @param s_h オリジナル画像の高さ
	 * @param strTxtFile 出力ファイル名
	 * @return 0 書き込み成功
	 *         -1 書き込み失敗
	 */
	private int SaveRectTxtForM2Det(int ImageID,float s_w,float s_h,String strTxtFile)
	{
		int nRet = 0;
		BodtTrainDataTable TrainDataTbl = BodtApp.db.GetTrainDataTable();
		File file = new File(strTxtFile);
		NORMAL:try
		{
			List<BodtObjectData> ld = TrainDataTbl.Select(ImageID);
			if(ld.size() == 0)
			{
				nRet = -1;
				break NORMAL;
			}
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			int err = 0;
			for(BodtObjectData d: ld)
			{
				/* 矩形の中心のX、Yと矩形の幅と高さを求めて正規化する */
				Rectangle2D.Double r = d.GetRect();
				if(r.getX() < 0. || r.getY() < 0.0 ||  r.getWidth() < 0.0 || r.getHeight() < 0.0)
				{
					nRet = -1;
					break;
				}
				//アノテーションは、left,top,right,bottom、categoryの番号で出力する
				double left = r.x;
				double top = r.y;
				double right = left + r.width;
				double bottom = top + r.height;
				int cat = d.GetCategory()-1;
				if(cat < 0) {
					continue;
				}
				String Record = String.valueOf(left) + "," + top + "," + right + "," + bottom + "," + cat;
				/* ファイルに書き込む */
				pw.print(Record+"\n");
			}
			pw.close();
		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			nRet = -1;
		}

	return nRet;
	}


	/**
	 * カテゴリリストを作成する
	 * @param FileName:出力先ファイル名
	 * @return 0:正常
	 *         -1:書き込み失敗
	 */
	private int SaveCategoryTxtForYolo(String FileName)
	{
		BodtCategoryTable CatTbl = BodtApp.db.GetCategoryTable();
		File file = new File(FileName);
		int nRet = 0;
		try
		{
			List<String> CatList = CatTbl.SelectAll();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for(String s : CatList)
			{
				pw.print(s+"\n");
			}

			pw.close();
		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			nRet = -1;
		}

		return nRet;
	}

	/**
	 * cfgファイルを作成する
	 * @param strTrainTxt train.txtのパス
	 * @param strTestTxt test.txtのパス
	 * @param strNamesPath basis.namesのパス
	 * @param strOutputFile cfg.dataの出力先パス
	 * @return :0 正常
	 *           -1 書き込み失敗
	 */
	private int SaveCfgFileForYolo(String Path,String strOutputFile)
	{
		int nRet = 0;
		/*
		 * コンフィグの内容は以下
		 * classes= 20
		 * train  = <path-to-voc>/train.txt
		 * valid  = <path-to-voc>2007_test.txt
		 * names = data/voc.names
		 * backup = backup
		 */
		File file = new File(strOutputFile);

		try
		{
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			/* クラス数取得 */
			int nClassNum = BodtApp.db.GetCategoryTable().SelectAll().size();
			pw.print("classes="+nClassNum+"\n");
			pw.print("train  = /home/darknet/baps3/train.txt\n");
			pw.print("valid  = /home/darknet/baps3/test.txt\n");
			pw.print("names  = /home/darknet/baps3/cfg/basis.names\n");
			pw.print("backup  = /home/darknet/baps3/backup\n");
			pw.close();
			SaveSettingFileForYolo(Path, nClassNum);

		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			nRet = -1;
		}



		return nRet;
	}

	private int SaveSettingFileForYolo(String Path, int classNum) {

		int nRet = 0;
		int outputCell = 3 * (classNum + 5);
		String template = yolov3Cfg.cfg;
		template = template.replace("@class_num@", String.valueOf(classNum));
		template = template.replace("@output@", String.valueOf(outputCell));

		File file = new File(Path + "/cfg/yolov3.cfg");

		try
		{
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			/* クラス数取得 */
			pw.print(template);
			pw.close();

		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			nRet = -1;
		}



		return nRet;
	}

	/**
	 * DBの画像テーブルと訓練テーブルを連結する。
	 * コピー元のDBの画像IDと訓練テーブルの画像ID，訓練IDは
	 * 連結元のDBからの連番に変わる。
	 * カテゴリテーブルはコピー先のDBのものを採用する
	 * プロパティテーブルもコピー先DBのものを採用する
	 * @param strDBName コピー元DBのフルパス
	 * @throws SQLException
	 */
	public int Cat(Connection srcCon) throws SQLException
	{
		int nRet = 0;
		try
		{

			/* コピー先（自分）のイメージIDの最大値を取得する */
			int nMaxImgID = m_ImageTable.GetMaxImageID();

			/* コピー元DBイメージテーブルを取得する */
			String SQL = "select ImageID,FullPathFileName, ImageData, length(ImageData) from ImageTable";
			Statement State = null;
			State = srcCon.createStatement();
			ResultSet rs = State.executeQuery(SQL);
			/* イメージテーブルにコピー元テーブルをコピーする */
			while( rs.next() )
			{
				int srcImgID = rs.getInt(1);
				int ImageID = srcImgID + nMaxImgID;
				String FileName = rs.getString(2);
				SetExportInfo(srcImgID,"イメージテーブルコピー中");
				InputStream in = rs.getBinaryStream(3);
				BufferedImage img = ImageIO.read(in);
				m_ImageTable.Insert(ImageID, FileName, img);
			}

			/* コピー元DB訓練テーブルにコピーする */
			SQL = "select * from TrainDataTable";
			State = srcCon.createStatement();
			rs = State.executeQuery(SQL);
			SetExportInfo(0,"訓練テーブルコピー中");
			int srcImgID = 0;
			while(rs.next())
			{
				/*String SQL = "CREATE TABLE if not exists [TrainDataTable] ("
						+"[TrainDataID] INTEGER,"
						+"[ImageID] INTEGER REFERENCES [ImageTable]([ImageID]) ON DELETE CASCADE,"
						+"[CategoryID] INTEGER NOT NULL DEFAULT '-1' REFERENCES [CategoryTable]([CategoryID]) ON DELETE CASCADE,"
						+"[x] REAL NOT NULL DEFAULT '-1',"
						+"[y] REAL NOT NULL DEFAULT '-1',"
						+"[w] REAL NOT NULL DEFAULT '-1',"
						+"[h] REAL NOT NULL DEFAULT '-1',"
						+"PRIMARY KEY([TrainDataID])"
						+")";*/
				srcImgID = rs.getInt(2);
				int ImageID = srcImgID + nMaxImgID;
				SetExportInfo(srcImgID,"訓練テーブルコピー中");
				int CategoryID = rs.getInt(3);
				float x = rs.getFloat(4);
				float y = rs.getFloat(5);
				float w = rs.getFloat(6);
				float h = rs.getFloat(7);
				m_TrainTable.Insert(ImageID, CategoryID, x, y, w, h);
			}
			m_Con.commit();
			SetExportInfo(srcImgID,"コピーが完了しました");

		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			m_Con.rollback();
			nRet = -1;
		}
		catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return nRet;
	}
	/**
	 * DBを閉じる。
	 * @author takaomi.konishi
	 */
	public void Close()
	{
		try
		{
			m_Con.close();
			m_CatTable = null;
			m_TrainTable = null;
			m_ImageTable = null;
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
}
