package bodt;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.net.URL;

import javax.swing.JScrollPane;

public class BodtBasisPanel extends JScrollPane {
	private Image m_RenderImg;

	public BodtBasisPanel(String ImgName)
	{
		URL url=this.getClass().getClassLoader().getResource("basis.jpg");
	//	System.out.println(url);
		try {
			m_RenderImg=this.createImage((ImageProducer) url.getContent());
		}catch(Exception ex){
			ex.printStackTrace();
			// System.out.println("Resource Error!");
			m_RenderImg=null;
		}

	}

	public void paintComponent(Graphics g)
	{
		if(m_RenderImg != null)
		{
			int w = 170;
			int h = 95;

			for(int y = 0; y < this.getHeight() + h;y = y+ h)
			{
				for(int x = 0 ; x < this.getWidth() + w;x = x + w)
				{
					g.drawImage(m_RenderImg, x, y, this);
				}
			}
		}
	}
}
