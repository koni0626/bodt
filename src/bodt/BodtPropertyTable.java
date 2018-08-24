package bodt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BodtPropertyTable {
	Connection m_Con;

	public BodtPropertyTable(Connection con)
	{
		m_Con = con;
		CreateTable();
	}

	/**
	 * プロパティテーブルを作成する。
	 * @return 0 正常
	 *         -1 作成失敗
	 */
	private int CreateTable()
	{
		String SQL = "create table if not exists [PropertyTable] ([Comment] VARCHAR(1024),[SrcImagePath] VARCHAR(260))";
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

	private int InsertProperty(String Comment,String SrcPath)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
		    stmt = m_Con.prepareStatement("REPLACE INTO PropertyTable(Comment,SrcImagePath) VALUES(?,?)");
			stmt.setString(1, Comment);
			stmt.setString(2, SrcPath);
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
}
