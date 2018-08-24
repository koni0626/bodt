package bodt;

/**
*
* 画像中のテキストを抽出するAIのための訓練データを作成するアプリケーション。<br>
* Bodtとは，Basis Ocr Data Trainの略。
* 画像取り込みコマンドを作るか，GUIからインポートコマンドを作るか。
*
* @since  : 2018-04-04
* @author : takaomi.konishi
*
*/

public class BodtApp {
	/**
	 *
	 *　@since  : 2018-04-04 / takaomi.konishi
	 * @param Args:[-d ディレクトリパス | -p プロジェクトファイル]
	 *
	 */
	//public static BodtTrainDataManager TrainDataManager;
	public static BodtDB db;
	public static void main(String Args[])
	{

		/*		try
		{
			BodtDB db = new BodtDB("D:\\temp\\img.db");
			long start = System.currentTimeMillis();
			db.AddImage();
			long end = System.currentTimeMillis();
			System.out.println("実行時間" + (end - start));
			db.Close();

		} catch (ClassNotFoundException | SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}*/

	//	TrainDataManager = new BodtTrainDataManager();
		db = new BodtDB();

		BodtMainFrame frame = new BodtMainFrame();

		frame.setVisible(true);
		//System.out.println("終了処理中");
		//db.Commit();
	}
}
