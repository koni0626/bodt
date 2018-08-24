package bodt;

import java.util.Arrays;

/**
 * 行列計算クラス
 * @author takaomi.konishi
 *
 */
public class BodtMatrix {
	public static final int ROW_MAX = 3;
	public static final int COL_MAX = 3;

	public BodtMatrix()
	{

	}

	/**
	 * 3×3の単位行列を生成する
	 * @return 単位行列
	 * @author takaomi.konishi
	 */
	public static double[][] Ident()
	{
		double[][] Mat = new double[ROW_MAX][COL_MAX];

		for(int y = 0; y < COL_MAX; y++)
		{
			for(int x = 0; x < ROW_MAX; x++)
			{
				Mat[y][x] = 0.0f;
			}
			Mat[y][y] = 1.0f;
		}
		return Mat;
	}

	/**
	 * 行列の掛け算を行う
	 * @param mat
	 * @return 計算結果
	 */
	public static double[][] Mul(double[][] mat1,double[][] mat2)
	{
		double[][] a = mat1;
		double[][] b = mat2;
		double[][] c = new double[3][3];

		for(int y = 0 ;y < ROW_MAX; y++)
		{
			for(int z = 0; z < 3;z++)
			{
				c[y][z] = 0.0f;
				for(int x = 0;x < COL_MAX; x++)
				{
					c[y][z] = c[y][z] + a[y][x] * b[x][z];
				}
			}
		}
		return c;
	}

	/**
	 * 平行移動のマトリックスを作成する
	 * @param Mat 変更前マトリックス
	 * @param x 平行移動するX座標
	 * @param y 平行移動するY座標
	 * @author takaomi.konishi
	 */
	public static void SetTransMat(double [][] Mat,double x,double y)
	{
		Mat[0][0] = 1.0f; Mat[0][1] = 0.0f; Mat[0][2] = 0.0f;
		Mat[1][0] = 0.0f; Mat[1][1] = 1.0f; Mat[1][2] = 0.0f;
		Mat[2][0] = x;   Mat[2][1] = y;  Mat[2][2] = 1.0f;
	}

	/**
	 * スケーリングのマトリックスを作成する
	 * @param Mat 変更前マトリックス
	 * @param xScale X方向の拡大率
	 * @param yScale Y方向の拡大率
	 * @author takaomi.konishi
	 */
	public static void SetScaleMat(double[][] Mat ,double xScale,double yScale)
	{
		Mat[0][0] = xScale; Mat[0][1] = 0.0f; Mat[0][2] = 0.0f;
		Mat[1][0] = 0.0f; Mat[1][1] = yScale; Mat[1][2] = 0.0f;
		Mat[2][0] = 0.0f; Mat[2][1] = 0.0f; Mat[2][2] = 1.0f;
	}

	/**
	 * 回転用のマトリックスを作成する
	 * @param Mat変更前マトリックス
	 * @param r 回転量(角度10°とか20°とかのほう)
	 */
	public static void SetRotateMat(double [][] Mat,double r)
	{
		Mat[0][0] = (double) Math.cos(r);  Mat[0][1] = (double) Math.sin(r); Mat[0][2] = 0.0f;
		Mat[1][0] = -(double) Math.sin(r); Mat[1][1] = (double) Math.cos(r); Mat[1][2] = 0.0f;
		Mat[2][0] = 0.0f;                  Mat[2][1] = 0.0f;                  Mat[2][2] = 0.0f;
	}

	/**
	 * x,y座標をMatで変換する
	 * @param Mat 計算用マトリックス
	 * @param x 変換前x座標
	 * @param y 変換前Y座標
	 * @return 変換後X,Y座標のdouble型配列[0]:X,[1]:Y
	 */
	public static double[] Coord2D(double[][] Mat,double x,double y)
	{
		double a[] = new double[3];
		double c[] = new double[3];
		double ans[] = new double[2];

		Arrays.fill(a, 0.0f);
		//Arrays.fill(b, 0.0f);
		Arrays.fill(c, 0.0f);

		a[0] = x; a[1] = y; a[2] = 1.0f;

		for(int i = 0 ;i < 3; i++)
		{
			for(int j = 0;j < 3; j++)
			{
				c[i] = c[i] + a[j]*Mat[j][i];
			}
		}
		ans[0] = c[0];
		ans[1] = c[1];
		return ans;
	}

	/**
	 * テスト用Main関数
	 * @param args
	 */
	public static void main(String args[])
	{
		double[][] ScaleMat = BodtMatrix.Ident();
		double[][] MoveMat = BodtMatrix.Ident();
		double[][] TransMat = BodtMatrix.Ident();
		double[][] MoveMat1 = BodtMatrix.Ident();

		double[] vec;
		BodtMatrix.SetScaleMat(ScaleMat, 2.0f, 2.0f);
		/* -2,3に移動 */
		BodtMatrix.SetTransMat(MoveMat, -1.0f, -1.0f);
		BodtMatrix.SetTransMat(MoveMat1, 1.0f, 1.0f);
		//縮小してから移動する
		TransMat = BodtMatrix.Mul(ScaleMat,MoveMat);
		//元に戻る
		TransMat = BodtMatrix.Mul(TransMat,MoveMat1);

		vec = BodtMatrix.Coord2D(TransMat, 2.0f, 2.0f);
	//	vec = BodtMatrix.Coord2D(ScaleMat, -3.0f, 2.0f);
	//	vec = BodtMatrix.Coord2D(MoveMat, -3.0f, 2.0f);
		System.out.println(vec[0]+","+vec[1]);
	}
}
