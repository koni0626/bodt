package bodt;

import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/**
 * 訓練学習データテーブルクラス
 * @author takaomi.konishi
 *
 */
public class BodtTrainDataTable {
	private Connection m_Con;
	private PreparedStatement m_UpdateState;
	private PreparedStatement m_SelectState;
	private PreparedStatement m_SelectImgIDState;
	private PreparedStatement m_SearchImgIDState;
	private PreparedStatement m_SearchImgIDStateT;

	/**
	 * 訓練学習データテーブルクラス
	 * @param con DBハンドル
	 */
	public BodtTrainDataTable(Connection con)
	{
		m_Con = con;
		m_UpdateState = null;
		CreateTable();
	}

	/**
	 * 訓練学習データテーブルを作成する。
	 * @return 0 正常
	 *         -1 作成失敗
	 */
	private int CreateTable()
	{
		String SQL = "CREATE TABLE if not exists [TrainDataTable] ("
					+"[TrainDataID] INTEGER,"
					+"[ImageID] INTEGER REFERENCES [ImageTable]([ImageID]) ON DELETE CASCADE,"
					+"[CategoryID] INTEGER NOT NULL DEFAULT '-1' REFERENCES [CategoryTable]([CategoryID]) ON DELETE CASCADE,"
					+"[x] REAL NOT NULL DEFAULT '-1',"
					+"[y] REAL NOT NULL DEFAULT '-1',"
					+"[w] REAL NOT NULL DEFAULT '-1',"
					+"[h] REAL NOT NULL DEFAULT '-1',"
					+"PRIMARY KEY([TrainDataID])"
					+")";

		Statement State = null;
		int nRet = 0;
		try
		{
			State = m_Con.createStatement();
			State.execute(SQL);
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			nRet = -1;
		}

		return nRet;
	}

	/**
	 * 訓練データをDBに登録する。
	 * @param strCatName
	 * @return 0 登録できた
	 *          -1 登録失敗
	 */
	public int Insert(int ImageID,int CategoryID,double x,double y,double w,double h)
	{
		int nRet = 0;
		try
		{
			if(m_UpdateState == null)
			{
				m_UpdateState = m_Con.prepareStatement("insert into TrainDataTable(ImageID,CategoryID,x,y,w,h)"
		    		+ " VALUES(?,?,?,?,?,?)");
			}
			m_UpdateState.setInt(1, ImageID);
			m_UpdateState.setInt(2, CategoryID);
			m_UpdateState.setDouble(3, x);
			m_UpdateState.setDouble(4, y);
			m_UpdateState.setDouble(5, w);
			m_UpdateState.setDouble(6, h);
			m_UpdateState.executeUpdate();
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			nRet = -1;
			e.printStackTrace();
		}

		return nRet;
	}

	/**
	 * 訓練データを更新する
	 * @param TrainDataID：更新する訓練データのID
	 * @param new_ImageID :変更後画像ID
	 * @param new_CategoryID :変更後カテゴリID
	 * @param new_RectID :変更後矩形ID
	 * @return 0 正常
	 *         -1 更新失敗
	 */

	public int Update(int TrainDataID,int CategoryID)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("update TrainDataTable set CategoryID=? where TrainDataID=?");
			stmt.setInt(1 , CategoryID);
			stmt.setInt(2 , TrainDataID);

			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			nRet = -1;
			e.printStackTrace();
		}
		return nRet;
	}

	/**
	 * 訓練データを削除する
	 * @param TrainDataID 削除する訓練データのID
	 * @return 0  削除できた
	 *          -1 削除失敗
	 */
	public int Delete(int ImageID)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("delete from TrainDataTable where ImageID = ?");
			stmt.setInt(1 , ImageID);
			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			nRet = -1;
			e.printStackTrace();
		}
		return nRet;
	}

	/**
	 * 画像に紐づく領域をすべて取得する
	 * @param ImageID 画像ID
	 * @return
	 */
	public List<BodtObjectData> Select(int ImageID)
	{
		List<BodtObjectData> ObjectList = null;
		try
		{
			if(m_SelectState == null)
			{
				m_SelectState = m_Con.prepareStatement("select CategoryID,x,y,w,h from TrainDataTable where ImageID = ?");
			}
			m_SelectState.setInt(1 , ImageID);
			ResultSet rs = m_SelectState.executeQuery();
			ObjectList = new ArrayList<BodtObjectData>();

			while( rs.next() )
			{
				BodtObjectData d = new BodtObjectData();
				int CategoryID = rs.getInt(1);
				double x = rs.getDouble(2);
				double y = rs.getDouble(3);
				double w = rs.getDouble(4);
				double h = rs.getDouble(5);
				d.Set(CategoryID, new Rectangle2D.Double(x,y,w,h));
				ObjectList.add(d);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return ObjectList;
	}

	/**
	 * 指定したパラメーターのトレーニングデータを検索する
	 * @param ImageID 画像ID
	 * @param x 画像IDに指定した矩形のX座標
	 * @param y 画像IDに指定した矩形のY座標
	 * @param w 画像IDに指定した矩形の幅
	 * @param h 画像IDに指定した矩形の高さ
	 * @return トレーニングID
	 */
	public int SelectTrainID(int ImageID,Double x,Double y,Double w,Double h)
	{
		int TrainDataID = 0;
		try
		{
			if(m_SelectImgIDState == null)
			{
				m_SelectImgIDState = m_Con.prepareStatement("select TrainDataID from TrainDataTable where ImageID = ? and x=? and y=? and w=? and h=?");
			}
			m_SelectImgIDState.setInt(1 , ImageID);
			m_SelectImgIDState.setDouble(2 , x);
			m_SelectImgIDState.setDouble(3 , y);
			m_SelectImgIDState.setDouble(4 , w);
			m_SelectImgIDState.setDouble(5 , h);
			ResultSet rs = m_SelectImgIDState.executeQuery();
			TrainDataID = rs.getInt(1);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return TrainDataID;
	}

	/**
	 * 指定したイメージIDより大きな未指定データを検索する
	 * @param ImageID
	 * @return
	 */
	public int SearchImageID(int ImageID)
	{
		int NextImageID = 0;
		ResultSet rs;
		try
		{
			if(m_SearchImgIDState == null)
			{
				m_SearchImgIDState = m_Con.prepareStatement("select ImageID from TrainDataTable where ImageID > ? and x< 0");
			}
			m_SearchImgIDState.setInt(1 , ImageID);

			rs = m_SearchImgIDState.executeQuery();
			if(rs.getRow() > 0)
			{
				NextImageID = rs.getInt(1);
			}
			else
			{
				Statement State = m_Con.createStatement();
				rs = State.executeQuery("select Max(ImageID) from TrainDataTable");
				/* TODO:実際のイメージを超えてたらエラー。後で直す */
				NextImageID = rs.getInt(1)+1;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return NextImageID;
	}
	
	/**
	 * 指定したイメージIDより大きな画像で、指定されたカテゴリIDがある次の画像を探す
	 * @param ImageID
	 * @return
	 */
	public int SearchImageID(int ImageID, int CategoryID)
	{
		int NextImageID = 0;
		System.out.println(ImageID);
		ResultSet rs;
		try
		{
			if(m_SearchImgIDStateT == null)
			{
				m_SearchImgIDStateT = m_Con.prepareStatement("select max(ImageID) from TrainDataTable where ImageID > ? and CategoryID=?");
			}
			m_SearchImgIDStateT.setInt(1 , ImageID);
			m_SearchImgIDStateT.setInt(2 , CategoryID);

			rs = m_SearchImgIDStateT.executeQuery();
			NextImageID = rs.getInt(1) + 1;
			System.out.println(NextImageID);
			if(NextImageID == 1)
			{
				NextImageID = ImageID;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return NextImageID;
	}
	public int GetRecordNum()
	{
		int nRecNum = 0;
		Statement state = null;
		if(m_Con == null)
		{
			return nRecNum;
		}

		try
		{
			state = m_Con.createStatement();
			ResultSet rs = state.executeQuery("select count() from TrainDataTable");
			nRecNum = rs.getInt(1);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return nRecNum;
	}

	public ResultSet SelectImgIDAll()
	{
		ResultSet rs = null;
		Statement state = null;
		if(m_Con == null)
		{
			return rs;
		}

		try
		{
			state = m_Con.createStatement();
			rs = state.executeQuery("select distinct ImageID from TrainDataTable");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return rs;
	}
}
