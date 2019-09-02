package bodt;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;

/**
 * 画像管理クラス。画像データごとテーブルに保存する。
 * @author takaomi.konishi
 *
 */
public class BodtImageTable {
	/* DB接続ハンドル。コンストラクタから受け取る */
	private Connection m_Con;

	/**
	 * 画像管理クラスのコンストラクタ。
	 * @param con DBハンドル
	 */
	public BodtImageTable(Connection con)
	{
		m_Con = con;
		CreateTable();
	}

	/**
	 * 画像データテーブルを作成する。
	 * @return 0 正常
	 *         -1 作成失敗
	 */
	private int CreateTable()
	{
		String SQL = "CREATE TABLE if not exists [ImageTable] ([ImageID] INTEGER PRIMARY KEY AUTOINCREMENT,"
				   + "[FullPathFileName] VARCHAR(260),[ImageData] BLOB)";
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
	 * 画像データをDBに登録する。
	 * @param ImageFileName 画像ファイル名
	 * @return 0 登録できた
	 *          -1 登録失敗
	 */
	public int Insert(String ImageFileName)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("INSERT INTO ImageTable(FullPathFileName,ImageData) VALUES(?,?)");
			File file = new File(ImageFileName);
			stmt.setString(1,ImageFileName);
			stmt.setBinaryStream(2,new FileInputStream(file),(int)file.length());
			stmt.executeUpdate();
			stmt.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO 自動生成された catch ブロック
			nRet = -1;
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			nRet = -1;
			e.printStackTrace();
		}
		finally
		{
			if(stmt != null)
			{
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
		return nRet;
	}

	/**
	 * 画像データをDBに登録する。
	 * @param ImageFileName 画像ファイル名
	 * @return 0 登録できた
	 *          -1 登録失敗
	 */
	public int Insert(int nImgID, String ImageFileName, BufferedImage img)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("INSERT INTO ImageTable(ImageID,FullPathFileName,ImageData) VALUES(?, ?, ?)");

			stmt.setInt(1, nImgID);
			stmt.setString(2,ImageFileName);
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			//ファイル名の拡張子を取得
			String filename = ImageFileName.toUpperCase();
			if(filename.endsWith(".JPG") ||filename.endsWith(".JPEG")) {
				ImageIO.write(img, "jpg", os);
			}
			else if(filename.endsWith(".PNG")) {
				ImageIO.write(img, "png", os);
			}
			else if(filename.endsWith(".GIF")) {
				ImageIO.write(img, "gif", os);
			}
			os.flush();
			byte[] imageInByte = os.toByteArray();
			os.close();
			InputStream in = new ByteArrayInputStream(imageInByte);
			stmt.setBinaryStream(3,in,imageInByte.length);
			stmt.executeUpdate();
		}
		catch (FileNotFoundException e)
		{
			// TODO 自動生成された catch ブロック
			nRet = -1;
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			nRet = -1;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		finally
		{
			if(stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (SQLException e)
				{
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
		return nRet;
	}

	/**
	 * 画像データを更新する
	 * @param ImageID 画像ID
	 *         BufferedImage ImageData
	 * @return 0 正常
	 *         -1 更新失敗
	 */
	public int Update(int nImageID, BufferedImage ImageData)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		String ImageFileName = SelectImageFileName(nImageID);
		try
		{
			// 一度byte配列へ変換
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String filename = ImageFileName.toUpperCase();
			if(filename.endsWith(".JPG") ||filename.endsWith(".JPEG")) {
				ImageIO.write(ImageData, "jpg", baos);
			}
			else if(filename.endsWith(".PNG")) {
				ImageIO.write(ImageData, "png", baos);
			}
			else if(filename.endsWith(".GIF")) {
				ImageIO.write(ImageData, "gif", baos);
			}
			//ImageIO.write( ImageData, "jpg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();

			// byte配列をInputStreamに変換
			InputStream in = new ByteArrayInputStream(imageInByte);


			stmt = m_Con.prepareStatement("update ImageTable set ImageData=? where ImageID=?");
			stmt.setBinaryStream(1,in,imageInByte.length);
			stmt.setInt(2,nImageID);

			stmt.executeUpdate();
		}
		catch(SQLException e)
		{
			nRet = -1;
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		finally
		{
			if(stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (SQLException e)
				{
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}
		return nRet;
	}

	/**
	 * 画像データを削除する
	 * @param ImageFileName 画像ファイル名
	 * @return 0  削除できた
	 *          -1 削除失敗
	 */
	public int Delete(String ImageFileName)
	{
		int nRet = 0;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("delete from ImageTable where FullPathFileName = ?");
			stmt.setString(1,ImageFileName);

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
	 * 指定されたIDの画像を取得する
	 * @param nImageID ImageTableのImageID
	 * @return :正常時は画像のBufferedImage
	 *           異常時はnull
	 */
	public BufferedImage SelectImage(int nImageID)
	{
		BufferedImage img = null;
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("select ImageData from ImageTable where ImageID=?");
			stmt.setInt(1,nImageID);
			ResultSet rs = stmt.executeQuery();
			InputStream in = rs.getBinaryStream(1);
			img = ImageIO.read(in);

			rs.close();

		}
		catch (FileNotFoundException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		finally
		{
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}

		return img;
	}

	/**
	 * 指定されたIDの画像ファイル名を取得する
	 * @param nImageID ImageTableのImageID
	 * @return :正常時は画像のBufferedImage
	 *           異常時はnull
	 */
	public String SelectImageFileName(int nImageID)
	{
		BufferedImage img = null;
		PreparedStatement stmt = null;
		String filename = "";

		try
		{
			stmt = m_Con.prepareStatement("select FullPathFileName from ImageTable where ImageID=?");
			stmt.setInt(1,nImageID);
			ResultSet rs = stmt.executeQuery();
			filename = rs.getString(1);


		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return filename;
	}

	/**
	 * 指定されたIDの画像ファイル名を取得する
	 * @param nImageID ImageTableのImageID
	 * @return :正常時は画像のファイル名
	 *           異常時は空文字""
	 */
	public String SelectFileName(int nImageID)
	{
		String FileName = "";
		PreparedStatement stmt = null;
		try
		{
			stmt = m_Con.prepareStatement("select FullPathFileName from ImageTable where ImageID=?");
			stmt.setInt(1,nImageID);
			ResultSet rs = stmt.executeQuery();
			FileName = rs.getString(1);
		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		finally
		{
			if(stmt != null)
			{
				try
				{
					stmt.close();
				}
				catch (SQLException e)
				{
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}

		return FileName;
	}

	/**
	 * ImageTableのID最大値を取得する。
	 * @return :正常時は1以上の値
	 *           取得できない場合は0を返す
	 */
	public int GetMaxImageID()
	{
		int MaxID = 0;
		String SQL = "select max(ImageID) from ImageTable";
		Statement State = null;
		try
		{
			State = m_Con.createStatement();
			ResultSet rs = State.executeQuery(SQL);
			MaxID = rs.getInt(1);

		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return MaxID;
	}

	/**
	 * ImageTableのID最小値を取得する。
	 * @return :正常時は1以上の値
	 *           取得できない場合は0を返す
	 */
	public int GetMinImageID()
	{
		int MinID = 0;
		String SQL = "select min(ImageID) from ImageTable";
		Statement State = null;
		try
		{
			State = m_Con.createStatement();
			ResultSet rs = State.executeQuery(SQL);
			MinID = rs.getInt(1);

		}
		catch (SQLException e)
		{
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return MinID;
	}

}
