package bodt;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
*
* 画像に指定された矩形(x,y,w,h)と矩形中の物体の種別を表すクラス。
*
* @since  : 2018-04-04
* @author : takaomi.konishi
*
*/

public class BodtObjectData {
	private int m_nCategory;
	private Rectangle2D.Double m_Rect;

	public BodtObjectData(int Category,double x,double y,double w,double h)
	{
		m_nCategory = Category;
		m_Rect = new Rectangle2D.Double(x,y,w,h);
	}

	public BodtObjectData()
	{
		m_nCategory = -1;
		m_Rect = new Rectangle2D.Double(0,0,0,0);
	}

	public void Set(int Category,double x,double y,double w,double h)
	{
		m_nCategory = Category;
		m_Rect.x = x;
		m_Rect.y = y;
		m_Rect.width = w;
		m_Rect.height = h;

		if(w < 0)
		{
			m_Rect.x = x + w;
			m_Rect.width = Math.abs(w);
		}
		if(h < 0)
		{
			m_Rect.y = y + h;
			m_Rect.height = Math.abs(h);
		}
	}

	public void SetRect(int Category,double left,double top,double right,double bottom)
	{
		m_nCategory = Category;
		m_Rect.x = left;
		m_Rect.y = top;
		m_Rect.width = right-left;
		m_Rect.height = bottom - top;
	}

	public void Set(int Category, Rectangle2D.Double Rect)
	{
		m_nCategory = Category;
		m_Rect.x = Rect.x;
		m_Rect.y = Rect.y;
		m_Rect.width = Rect.width;
		m_Rect.height = Rect.height;

		if(m_Rect.width < 0)
		{
			m_Rect.x = Rect.x + Rect.width;
			m_Rect.width = Math.abs(m_Rect.width);
		}
		if(m_Rect.height < 0)
		{
			m_Rect.y = Rect.y + Rect.height;
			m_Rect.height = Math.abs(Rect.height);
		}
	}
	/* 矩形の中心位置を求める */
	public Point2D.Double GetCenter()
	{
		Point2D.Double c = new Point2D.Double();

		c.x = (int) m_Rect.getCenterX();
		c.y = (int) m_Rect.getCenterY();

		return c;
	}

	/* 指定した座標が矩形に含まれているかどうかを調べる */
	/* true:含む、false:含まない */
	public boolean isContain(double x,double y)
	{
		return m_Rect.contains(new Point2D.Double(x,y));
	}

	/* 領域の物体ID */
	public int GetCategory()
	{
		return m_nCategory;
	}

	/* 左X座標 */
	public double Left()
	{
		return m_Rect.x;
	}

	/* 上側Y座標 */
	public double Top()
	{
		return m_Rect.y;
	}

	/* 右X座標 */
	public double Right()
	{
		return m_Rect.x + m_Rect.width;
	}

	/* 下Y座標 */
	public double Bottom()
	{
		return m_Rect.y + m_Rect.height;
	}

	public Rectangle2D.Double GetRect()
	{
		return m_Rect;
	}

	public String toString()
	{
		return m_Rect.x + "," + m_Rect.y + "," + m_Rect.width + "," + m_Rect.height + "," + m_nCategory;

	}
}
