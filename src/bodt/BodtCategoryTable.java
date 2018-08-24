package bodt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/**
 * カテゴリテーブルクラス
 * @author takaomi.konishi
 *
 */
public class BodtCategoryTable {
	/* DB接続ハンドル。コンストラクタから受け取る */
	private Connection m_Con;

	/* カテゴリ一覧をすべて取得するときのselect文のプリペアステートメント。 */
	PreparedStatement m_SelectAllState;

	/* カテゴリ名をIDから取得するときのselect文のプリペアステートメント。 */
	PreparedStatement m_SelectIDState;
	/**
	 * カテゴリテーブルコンストラクタ
	 * @param con DBのファイルハンドル
	 */
	public BodtCategoryTable(Connection con)
	{
		m_Con = con;
		this.CreateTable();
	}

	/**
	 * カテゴリテーブルを作成する。
	 * @return 0 正常
	 *         -1 作成失敗
	 */
	private int CreateTable()
	{
		String SQL = "CREATE TABLE if not exists [CategoryTable] ([CategoryID] INTEGER PRIMARY KEY AUTOINCREMENT,[Name] VARCHAR(256))";
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
	 * カテゴリをDBに登録する。
	 * @param strCatName
	 * @return 0 登録できた
	 *          -1 登録失敗
	 */
	public int Insert(String strCatName)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
		    stmt = m_Con.prepareStatement("INSERT INTO CategoryTable(Name) VALUES(?)");
			stmt.setString(1, strCatName);
		    stmt.executeUpdate();
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
	 * カテゴリを更新する
	 * @param strOldCatName 更新前カテゴリ名
	 * @param strNewCatName 更新後カテゴリ名
	 * @return 0 正常
	 *          -1 更新失敗
	 */
	public int Update(String strOldCatName,String strNewCatName)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("update CategoryTable set Name=? where Name = ?");
			stmt.setString(1 , strNewCatName);
			stmt.setString(2 , strOldCatName);
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
	 * カテゴリを削除する
	 * @param strCatName 削除するカテゴリ名
	 * @return 0  削除できた
	 *          -1 削除失敗
	 */
	public int Delete(String strCatName)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("delete from CategoryTable where Name = ?");
			stmt.setString(1 , strCatName);
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
	 * カテゴリ一覧をリスト形式で返却する
	 * @return カテゴリのリスト。エラー時はnullが返る。
	 */
	public List<String> SelectAll()
	{
		List<String> ObjectList = null;
		try
		{
			if(m_SelectAllState == null)
			{
				m_SelectAllState = m_Con.prepareStatement("select Name from CategoryTable");
			}
			//m_SelectState.setInt(1 , ImageID);
			ResultSet rs = m_SelectAllState.executeQuery();
			ObjectList = new ArrayList<String>();
			while( rs.next() )
			{
				String CategoryName = rs.getString(1);
				ObjectList.add(CategoryName);
			}
		//	rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return ObjectList;
	}

	/**
	 * カテゴリIDからカテゴリ名を取得する
	 * @param nCatID
	 * @return 正常時はカテゴリ名。エラー時は""が返る。
	 */
	public String GetCategoryName(int nCatID)
	{
		String CatName = "";
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("select Name from CategoryTable where CategoryID=?");
			stmt.setInt(1 , nCatID);
			ResultSet rs = stmt.executeQuery();
			CatName = rs.getString(1);
		//	rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return CatName;

	}

	/**
	 * カテゴリ名からカテゴリIDを取得する。
	 * @param CatName
	 * @return 正常時は1以上の整数値(ID)。異常時は-1を返す。
	 */
	public int GetCategoryID(String CatName)
	{
		int CatID = -1;
		try
		{
			if(m_SelectIDState == null)
			{
				m_SelectIDState = m_Con.prepareStatement("select CategoryID from CategoryTable where Name=?");
			}
			m_SelectIDState.setString(1 , CatName);
			ResultSet rs = m_SelectIDState.executeQuery();
			CatID = rs.getInt(1);
			//rs.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return CatID;

	}

	/**
	 * カテゴリーテーブルの数を取得する
	 * @return カテゴリテーブルの数
	 */
	public int GetCatNum()
	{
		Statement State = null;
		int RecNum = 0;
		try
		{
			State = m_Con.createStatement();

			ResultSet rs = State.executeQuery("select count() from CategoryTable");
			RecNum = rs.getInt(1);

		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return RecNum;
	}
}
