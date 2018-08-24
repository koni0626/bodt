package bodt;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * 画像編集画面クラス。
 * @author takaomi.konishi
 *
 */
public class BodtDrawPanel extends JPanel implements MouseListener,MouseMotionListener,KeyListener ,MouseWheelListener,ActionListener {

	public static final int MOUSE_RIGHT_BUTTON = 3;

	/* まだカテゴリが決まっていない場合の仮ID */
	public static final int NON_COMMIT_CATEGORY = -1;

	/* まだ描画していない場合の矩形の座標は(x,y,w,h)=(-1,-1,-1,-1) */
	public static final int NON_WRITE_RECT = -1;

	/* 画像ID。基本的には連番。ただし削除したときはどうしようか */
	private int m_ImageID;

	/* 画像IDの最大値 */
	private int m_MaxImgID;

	/* 画像IDの最小値。基本は1 */
	private int m_MinImgID;

	/* 現在レンダリングしている画像バッファ */
	private BufferedImage m_RenderImg;

	/* オリジナル画像バッファ */
	private BufferedImage m_OriginalImg;

	private List<BodtObjectData> m_RenderRect;

	/* 現在描画中(赤色)の矩形の座標 */
	private Rectangle2D.Double m_DrawRect;

	/* 十字カーソル表示用のマウスポジション */
	private Point m_MousePos;

	/* 現在描画中かどうかのステータス */
	private boolean m_Drawing;

	/* 画像拡大率 */
	private double m_Scale;

	/**
	 * コンストラクタ。
	 * @since 2018-04-04
	 * @author takaomi.konishi
	 */
	public BodtDrawPanel()
	{
		m_ImageID = 0;
		m_MaxImgID = 0;
		m_MinImgID = 0;
		m_Drawing = false;
		m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);
		m_RenderImg = null;
		m_RenderRect = new ArrayList<BodtObjectData>();
		m_Scale = 1.0;
		m_MousePos = new Point(0,0);
		addKeyListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * 画面をリセットする
	 * @retun 画像イメージ
	 * @since  : 2018-04-04
	 * @author : takaomi.konishi
	 */
	public void Reset()
	{
		m_ImageID = 1;
		//m_Scale = 1.0f;
		m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);
		m_OriginalImg = BodtApp.db.GetImageTable().SelectImage(m_ImageID);
		/* 画像サイズを調節 */
		AdjustScale();
		/* オリジナル画像をレンダリングイメージにコピー */
		m_RenderImg = ImageScale(m_OriginalImg,m_Scale);
		/* 矩形領域取得 */
		List<BodtObjectData> ObjectList = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
		m_RenderRect = new ArrayList<BodtObjectData>();
		for(BodtObjectData d:ObjectList)
		{
			BodtObjectData data = new BodtObjectData();
			Rectangle2D.Double r = d.GetRect();
			/* オリジナル画像の中心位置 */
			r = RectScale(r,m_Scale);
			data.Set(d.GetCategory(),r);

			m_RenderRect.add(data);
		}
		/* 画像IDの最大値取得 */
		m_MaxImgID = BodtApp.db.GetImageTable().GetMaxImageID();
		/* 画像IDの最小値取得 */
		m_MinImgID = BodtApp.db.GetImageTable().GetMinImageID();
		int cc_x = this.getParent().getWidth()/2;
		int cc_y = this.getParent().getHeight()/2;

		int img_cx = m_RenderImg.getWidth()/2;
		int img_cy = m_RenderImg.getHeight()/2;
		int cx = cc_x - img_cx;
		int cy = cc_y - img_cy;
		if (cx < 0) cx = 0;
		if (cy < 0) cy = 0;

		super.setBounds(cx, cy,m_RenderImg.getWidth(),m_RenderImg.getHeight());
		requestFocus();
		repaint();
	}

	/* 画像を拡大，縮小する。*/
	public void ReSizePane()
	{
		/* 親ウインドウの中心X */
		int cc_x = this.getParent().getWidth()/2;
		/* 親ウインドウの中心Y */
		int cc_y = this.getParent().getHeight()/2;
		/* オリジナル画像の中心X */
		int img_cx = m_RenderImg.getWidth()/2;
		/* オリジナル画像の中心Y */
		int img_cy = m_RenderImg.getHeight()/2;
		int cx = cc_x - img_cx;
		int cy = cc_y - img_cy;
		if (cx < 0) cx = 0;
		if (cy < 0) cy = 0;

		super.setBounds(cx, cy,m_RenderImg.getWidth(),m_RenderImg.getHeight());
		super.revalidate();

	}
	  @Override public Dimension getPreferredSize() {
		  Dimension d  = null;
		  if(m_RenderImg != null)
		  {
			  d =  new Dimension(m_RenderImg.getWidth(),m_RenderImg.getHeight());
		  }
		  else
		  {
			  d =  new Dimension(0,0);
		  }

		    return d;
		  }
	/**
	 * 作成中の赤色の矩形を描画する。
	 * @param g2 paintComponentのGraphics2Dを指定する
	 * @author takaomi.konishi
	 */
	private void DrawRect(Graphics2D g2)
	{
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(1.0f));

		if(m_DrawRect.x == NON_WRITE_RECT     || m_DrawRect.y== NON_WRITE_RECT ||
		   m_DrawRect.width == NON_WRITE_RECT || m_DrawRect.height == NON_WRITE_RECT)
		{
			/* 矩形が初期値のままなら矩形は書かない */
			return ;
		}

		Rectangle2D.Double AbsRect = (Rectangle2D.Double)m_DrawRect.clone();
		if(m_DrawRect.width < 0)
		{
			AbsRect.x = m_DrawRect.x + m_DrawRect.width;
			AbsRect.width = Math.abs(m_DrawRect.width);
		}
		if(m_DrawRect.height < 0)
		{
			AbsRect.y = m_DrawRect.y + m_DrawRect.height;
			AbsRect.height = Math.abs(m_DrawRect.height);
		}

		g2.drawRect((int)AbsRect.x,(int)AbsRect.y,(int)AbsRect.width,(int)AbsRect.height);
	}

	/**
	 * 十字キーを描画する
	 * @param g2 paintComponentのGraphicsを指定する。
	 * @author takaomi.konishi
	 */
	private void DrawCross(Graphics2D g2)
	{
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(1.0f));
		/* 横線描画 */
		g2.drawLine(0, m_MousePos.y, this.getWidth(), m_MousePos.y);
		/* 縦線描画 */
		g2.drawLine(m_MousePos.x,0, m_MousePos.x, this.getHeight());
	}

	/**
	 * ENTERキーで確定された領域を描画する
	 * @author takaomi.konishi
	 * @param g2 paintComponentのGraphics2Dを指定する
	 */
	public void DrawCommitData(Graphics2D g2,int o_w,int o_h)
	{
		g2.setColor(Color.GREEN);
		g2.setStroke(new BasicStroke(2.0f));
		for(BodtObjectData d:m_RenderRect)
		{
			Rectangle2D.Double r = d.GetRect();
			if(d.GetCategory() != NON_COMMIT_CATEGORY)
			{
				/* カテゴリも確定している場合は緑太枠表示 */
				String strCatName = BodtApp.db.GetCategoryTable().GetCategoryName(d.GetCategory());
				g2.setColor(Color.GREEN);
				g2.fillRect((int)r.x-1,(int)r.y-20, (int)r.width+2, 20);
				g2.setColor(Color.WHITE);
				g2.drawString(strCatName, (int)r.x,(int)r.y-5);
				g2.setColor(Color.GREEN);
				g2.setStroke(new BasicStroke(2.0f));
			}
			else
			{
				/* カテゴリ入力待ちの場合は黄色表示 */
				g2.setColor(Color.YELLOW);
				g2.setStroke(new BasicStroke(2.0f));
			}

			g2.drawRect((int)r.x,(int)r.y,(int)r.width,(int)r.height);

		}
	}

	/**
	 * 画像を表示する
	 * @param g2 paintComponentのGraphics2Dを指定する
	 * @author takaomi.konishi
	 */
	public void RenderImage(Graphics2D g2)
	{
		g2.drawImage(m_RenderImg, 0,0, this);
	}

	/**
	 * 描画関数
	 * @author takaomi.konishi
	 */
	public void paintComponent(Graphics g)
	{

		ReSizePane();
		Graphics2D g2 = (Graphics2D)g;

		if(m_RenderImg != null)
		{
			//BufferedImage Original = BodtApp.db.GetImageTable().SelectImage(m_ImageID);

			RenderImage(g2);
			/* 登録済みのデータを描画する */
			DrawCommitData(g2,0,0);
			this.DrawRect(g2);
			this.DrawCross(g2);
		}
	}

	/**********************************************/
	/*        マウス操作のオーバライド群          */
	/**********************************************/
	/**
	 * マウスがクリックされたときに呼ばれる。
	 * @author takaomi.konishi
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		/* e.getButton()の値が1のとき：左クリック       */
		/* e.getButton()の値が2のとき：ホイールクリック */
		/* e.getButton()の値が3のとき :右クリック       */
		if(e.getButton() == MOUSE_RIGHT_BUTTON)
		{
			/* 右クリックされたのでポップアップを表示する。 */
			ShowCategoryPopup(e.getComponent(),e.getX(),e.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		Point NowPoint = e.getPoint();
		m_MousePos = (Point)e.getPoint().clone();

		m_DrawRect.x = NowPoint.x;
		m_DrawRect.y = NowPoint.y;
		m_DrawRect.width = 0;
		m_DrawRect.height = 0;
		m_Drawing = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		//JOptionPane.showMessageDialog(null, "リリース");
		//m_Drawing = false;
		m_MousePos = (Point)e.getPoint().clone();
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		m_MousePos = (Point)e.getPoint().clone();
		requestFocus();
		setFocusable(true);
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ

		m_MousePos = (Point)e.getPoint().clone();

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		m_MousePos = (Point)e.getPoint().clone();
		if(m_Drawing == false) {
			return;
		}
		Point NowPoint = e.getPoint();

		m_DrawRect.width = NowPoint.x - m_DrawRect.x;
		m_DrawRect.height =NowPoint.y -  m_DrawRect.y;
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		m_MousePos = (Point)e.getPoint().clone();
		repaint();
	}

	/**********************************************/
	/*        キー操作のオーバライド群            */
	/**********************************************/

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
			OnPressKeyEnter();
			break;

		case KeyEvent.VK_N:
			OnPressKeyNext();
			break;

		case KeyEvent.VK_P:
			OnPressKeyPrev();
			break;

		case KeyEvent.VK_C:
			OnClearRect();
			break;

		case KeyEvent.VK_S:
			OnPressKeySearch();
			break;
		case KeyEvent.VK_R:
			OnPressRotateKey();
			break;
		case KeyEvent.VK_I:
			OnPressKeyInfo();
			break;
		case KeyEvent.VK_1:
			//最小化
			break;
		case KeyEvent.VK_2:
			//最大化
			break;


		default:
			break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ
	}

	/**********************************************/
	/*   オーバーライド関数群から呼ばれる内部関数 */
	/**********************************************/
	/**
	 * mouseClickedで右クリックされたときに呼ぶ。
	 * 右クリックされた位置が確定済み領域(黄色，または緑のとき)の場合に，
	 * ポップアップを表示し，カテゴリを指定する。
	 * @param c popupを表示する親のコンポーネント
	 * @param x popupを表示するX座標
	 * @param y popupを表示するY座標
	 * @author takaomi.konishi
	 */
	private void ShowCategoryPopup(Component c,int x,int y)
	{
		/* 右クリックされた座標を含む矩形を検索 */
		List<BodtObjectData> ObjectList = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
		for(BodtObjectData d:ObjectList)
		{
			Rectangle2D.Double r = RectScale(d.GetRect(),m_Scale);
			if(r.contains(new Point(x, y)))
			{
				/* ポップアップを表示する */
				 JPopupMenu popup = new JPopupMenu();
				 /* カテゴリ一覧を取得する */
				 List<String> CategoryList = BodtApp.db.GetCategoryTable().SelectAll();
				 for(String s:CategoryList)
				 {
					 /* カテゴリをメニューに全部追加 */
					 JMenuItem Menu = new JMenuItem(s);
					 /* カテゴリの名前と表示中の画像IDをパラメーターとして渡す */
					 String Param = d.GetRect().x + "," +
							 		 d.GetRect().y + "," +
							 		 d.GetRect().width+ "," +
							 		 d.GetRect().height+ "," +
							 		 s;

					 Menu.setActionCommand(Param);
					 /* 選択されたメニューを取得するクラスを登録しておく */
					 Menu.addActionListener(this);
					 popup.add(Menu);
				 }
				 /* ポップアップ表示 */
			      popup.show(c, x, y);
			}
		}
	}

	/**
	 * 引数に指定された画像をスケールして返却する。(ディープコピー)
	 * @param src_img 変換元画像
	 * @param scale 拡大サイズ
	 * @return 拡大した画像
	 */
	private BufferedImage ImageScale(BufferedImage src_img,double scale)
	{
		int w = (int) (src_img.getWidth() * scale);
		int h =  (int) (src_img.getHeight() * scale);
		BufferedImage dst_img = new BufferedImage(w,h, m_OriginalImg.getType());
		dst_img.getGraphics().drawImage(src_img.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING), 0, 0, w, h, null);
		return dst_img;
	}

	/**
	 * 画像を画面サイズに応じてオートスケールする
	 */
	private void AdjustScale()
	{
		double p_w = this.getParent().getWidth();
		double p_h = this.getParent().getHeight();
		double s_w = m_OriginalImg.getWidth();
		double s_h = m_OriginalImg.getHeight();
		double d_w = p_w/s_w;
		double d_h = p_h/s_h;
		if(d_w > d_h)
		{
			m_Scale = d_h;
		}
		else
		{
			m_Scale = d_w;
		}
	}
	/**
	 * 次の画像へのボタンを押されたときに呼ばれる
	 * @author takaomi.konishi
	 */
	public int OnPressKeyNext()
	{
		m_MaxImgID = BodtApp.db.GetImageTable().GetMaxImageID();
		if((m_ImageID+ 1) <= m_MaxImgID)
		{
			m_ImageID++;
			/* 次の画像取得 */
			m_OriginalImg = BodtApp.db.GetImageTable().SelectImage(m_ImageID);
			/* 画像サイズを調節 */
			AdjustScale();
			/* オリジナル画像をレンダリングイメージにコピー */
			m_RenderImg = ImageScale(m_OriginalImg,m_Scale);
			/* 矩形領域取得 */
			List<BodtObjectData> ObjectList = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
			m_RenderRect = new ArrayList<BodtObjectData>();
			for(BodtObjectData d:ObjectList)
			{
				BodtObjectData data = new BodtObjectData();
				Rectangle2D.Double r = d.GetRect();
				/* オリジナル画像の中心位置 */
				r = RectScale(r,m_Scale);
				data.Set(d.GetCategory(),r);

				m_RenderRect.add(data);
			}

			/* 描画途中の画像は消す */
			m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);

			repaint();
			this.getParent().repaint();
			//System.out.println(m_ImageID);
		}
		return m_ImageID;
	}


	/**
	 * 前の画像へのボタンを押されたときに呼ばれる
	 * @author takaomi.konishi
	 */
	public int OnPressKeyPrev()
	{
		if((m_ImageID - 1) >= m_MinImgID)
		{
			m_ImageID--;
			/* 前の画像取得 */
			m_OriginalImg = BodtApp.db.GetImageTable().SelectImage(m_ImageID);
			/* 画像サイズを調節 */
			AdjustScale();

			/* オリジナル画像をレンダリングイメージにコピー */
			m_RenderImg = ImageScale(m_OriginalImg,m_Scale);
			/* 矩形領域取得 */
			List<BodtObjectData> ObjectList = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
			m_RenderRect = new ArrayList<BodtObjectData>();
			for(BodtObjectData d:ObjectList)
			{
				BodtObjectData data = new BodtObjectData();
				Rectangle2D.Double r = d.GetRect();
				/* オリジナル画像の中心位置 */
				r = RectScale(r,m_Scale);
				data.Set(d.GetCategory(),r);

				m_RenderRect.add(data);
			}
			/* 描画途中の矩形は消す */
			m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);

			repaint();
			this.getParent().repaint();
		}
		return m_ImageID;
	}

	/**
	 * 画像を回転する
	 * @return なし
	 */
	public void OnPressRotateKey()
	{
		BodtImageTable image_table = BodtApp.db.GetImageTable();

		/* 画像を取得する */
		if(m_OriginalImg == null)
		{
			return;
		}
		AffineTransform affine = new AffineTransform();
		affine.setToRotation(Math.toRadians(90), m_OriginalImg.getHeight()/2, m_OriginalImg.getHeight()/2);
		AffineTransformOp op = new AffineTransformOp(affine, AffineTransformOp.TYPE_BICUBIC);
		BufferedImage outBuff = new BufferedImage(m_OriginalImg.getHeight(), m_OriginalImg.getWidth(), m_OriginalImg.getType());
		op.filter(m_OriginalImg, outBuff);
		m_OriginalImg = outBuff;
		image_table.Update(m_ImageID, m_OriginalImg);
		BodtApp.db.Commit();
		System.out.println(m_OriginalImg.getWidth());
		m_RenderImg = ImageScale(m_OriginalImg,m_Scale);
		repaint();
	}

	/**
	 * 現在表示されている画像の領域を全クリアする
	 */
	public void OnClearRect()
	{
		if((m_ImageID >= m_MinImgID) && (m_ImageID+ 1 <= m_MaxImgID))
		{
			/* 前の画像取得 */
			BodtApp.db.GetTrainDataTable().Delete(m_ImageID);
			BodtApp.db.Commit();
			/* 描画途中の矩形は消す */
			m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);
			m_RenderRect.clear();
			repaint();
		}
	}

	/**
	 * ENTERキーが押されたときに呼ばれる。ENTERが押されると領域を固定する。
	 * 矩形領域の面積が0の場合は登録しない。
	 * この時点ではカテゴリはまだ未定。
	 * @author takaomi.konishi
	 */
	public void OnPressKeyEnter()
	{
		/* DBにデータを保存する */
		if(m_DrawRect.width*m_DrawRect.height <= 0)
		{
			return;
		}
		/* トレーニングテーブルを取得 */
		BodtTrainDataTable train_table = BodtApp.db.GetTrainDataTable();
		//System.out.println(diff);
		Rectangle2D.Double r = RectScale(m_DrawRect,1.0/m_Scale);

		/* トレーニングデータの登録する。カテゴリは未確定のため-1を指定する */
		train_table.Insert(m_ImageID, -1, r.x, r.y, r.width, r.height);
		BodtApp.db.Commit();
		m_RenderRect.add(new BodtObjectData(-1, m_DrawRect.x, m_DrawRect.y, m_DrawRect.width, m_DrawRect.height));
		/* 領域が確定したのでレンダリング中（赤色四角)を消す */
		m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);

		repaint();
	}

	/**
	 * iキーが押されたときに呼ばれる。
	 * 現在の状況をメッセージダイアログで表示する。
	 * この時点ではカテゴリはまだ未定。
	 * @author takaomi.konishi
	 */
	public void OnPressKeyInfo()
	{
		BodtTrainDataTable train_table = BodtApp.db.GetTrainDataTable();
		int Now = train_table.SearchImageID(m_ImageID);
		int Max = BodtApp.db.GetImageTable().GetMaxImageID();
		JOptionPane.showMessageDialog(null, String.format("現在の進捗\n%d/%d", Now,Max));
	}

	/**
	 * Sキーが押されたときに呼ばれる。
	 * 未指定のデータを検索して表示する。
	 * この時点ではカテゴリはまだ未定。
	 * @author takaomi.konishi
	 */
	public void OnPressKeySearch()
	{
		/* トレーニングテーブルを取得 */
		BodtTrainDataTable train_table = BodtApp.db.GetTrainDataTable();
		/* まだ教師データがないものを探す */
		m_ImageID = train_table.SearchImageID(m_ImageID);
		/* 前の画像取得 */
		m_Scale = 1.0f;
		/* 次の画像取得 */
		m_OriginalImg = BodtApp.db.GetImageTable().SelectImage(m_ImageID);
		/* オリジナル画像をレンダリングイメージにコピー */
		m_RenderImg = ImageScale(m_OriginalImg,m_Scale);
		/* 矩形領域取得 */
		m_RenderRect = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
		/* 描画途中の矩形は消す */
		m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);

		repaint();

	}
	/**
	 * マウスホイールが押されたときにコールされる。
	 * 手前に回転させた場合は拡大、反対の場合は縮小する。
	 * @author takaomi.konishi
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO 自動生成されたメソッド・スタブ
	//	System.out.println("wheel:"+e.getWheelRotation());

		int wh = e.getWheelRotation()*-1;
		if(wh < 0 && (m_Scale > 0.2))
		{
			/* -1の時が上回転 */
			m_Scale = (float) (m_Scale + wh*0.1);
		}

		if(wh > 0 &&
			m_OriginalImg.getWidth()*(m_Scale+0.1) < this.getParent().getWidth() &&
			m_OriginalImg.getHeight()*(m_Scale+0.1) < this.getParent().getHeight())
		{
			/* 1のときが下(手前)回転 */
			m_Scale = (float) (m_Scale + wh*0.1);
		}
		/* 画像を拡大する */
		m_RenderImg = ImageScale(m_OriginalImg,m_Scale);

		/* コミット矩形を拡大する */
		List<BodtObjectData> ObjectList = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
		m_RenderRect = new ArrayList<BodtObjectData>();
		for(BodtObjectData d:ObjectList)
		{
			BodtObjectData data = new BodtObjectData();
			Rectangle2D.Double r = d.GetRect();
			/* オリジナル画像の中心位置 */
			r = RectScale(r,m_Scale);
			data.Set(d.GetCategory(),r);

			m_RenderRect.add(data);
		}
		m_DrawRect = new Rectangle2D.Double(NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT,NON_WRITE_RECT);
		repaint();
		revalidate();
	}

	/**
	 * 矩形をリサイズする
	 * @param r 変更前Rectangle
	 * @param fScale 拡大率
	 * @return 変更後のRectangle
	 */
	public Rectangle2D.Double RectScale(Rectangle2D.Double r,double fScale)
	{
		Rectangle2D.Double RtnRect = new Rectangle2D.Double();
		double oc_x = m_OriginalImg.getWidth();
		float oc_y = m_OriginalImg.getHeight();

		double[][] ScaleMat = BodtMatrix.Ident();
		double[][] TransMat = BodtMatrix.Ident();
		double[][] TransMat1 = BodtMatrix.Ident();
		double[][] MixMat = BodtMatrix.Ident();
		/* 拡大マトリックス作成 */
		BodtMatrix.SetScaleMat(ScaleMat, fScale, fScale);
		/* 平行移動マトリックス生成(原点移動) */
		BodtMatrix.SetTransMat(TransMat, oc_x,oc_y);
		/* 平行移動マトリックス(原点から戻る) */
		BodtMatrix.SetTransMat(TransMat1, -oc_x,-oc_y);
		/* マトリックス合成 */
		MixMat = BodtMatrix.Mul(ScaleMat, TransMat);
		MixMat = BodtMatrix.Mul(MixMat,TransMat1);

		double left = r.x;
		double top = r.y;
		double right = r.x + r.width;
		double bottom = r.y + r.height;

		double[] vec1 = BodtMatrix.Coord2D(MixMat, left, top);
		double[] vec2 = BodtMatrix.Coord2D(MixMat, right, bottom);

		RtnRect.x = vec1[0];
		RtnRect.y = vec1[1];
		RtnRect.width = vec2[0] - vec1[0];
		RtnRect.height = vec2[1] - vec1[1];

		return RtnRect;

	}

	/**
	 * 確定した領域を右クリックされたときにコールされる。
	 * カテゴリを指定する。
	 * @author takaomi.konishi
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO 自動生成されたメソッド・スタブ
		String strMenuName = e.getActionCommand();
		//System.out.println(strMenuName);
		String [] Tokens = strMenuName.split(",");
		double x = Double.valueOf(Tokens[0]);
		double y = Double.valueOf(Tokens[1]);
		double w = Double.valueOf(Tokens[2]);
		double h = Double.valueOf(Tokens[3]);
		String strCatName = Tokens[4];

		/* メニューアイテム名からカテゴリIDを取得する */
		int CategoryID = BodtApp.db.GetCategoryTable().GetCategoryID(strCatName);
		/* トレーニングデータのIDを取得する */
		int nTrainDataID = BodtApp.db.GetTrainDataTable().SelectTrainID(m_ImageID, x, y, w, h);
		/* 訓練データベースに画像IDとカテゴリIDと座標を登録する */
		BodtApp.db.GetTrainDataTable().Update(nTrainDataID,CategoryID);

		/* コミット矩形を拡大する */
		List<BodtObjectData> ObjectList = BodtApp.db.GetTrainDataTable().Select(m_ImageID);
		m_RenderRect = new ArrayList<BodtObjectData>();

		for(BodtObjectData d:ObjectList)
		{
			BodtObjectData data = new BodtObjectData();
			Rectangle2D.Double r = d.GetRect();
			Rectangle2D.Double tmp;
			/* オリジナル画像の中心位置 */
			tmp = (Rectangle2D.Double) RectScale(r,m_Scale).clone();
			/* ここで丸め誤差が発生している？ */
			data.Set(d.GetCategory(),tmp);
			m_RenderRect.add(data);
		}

		BodtApp.db.Commit();

		repaint();
	}
}
