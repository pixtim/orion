package orion.sdk.graphics.drawables.primitives;

import orion.sdk.graphics.drawables.surfaces.Mesh;
import orion.sdk.math.FloatMatrix;
import orion.sdk.math.geometry.Box;


public class WireCube extends Mesh
{
	public WireCube()
	{
		super(6, Mesh.FaceType.QUAD, new Box(-1, -1, -1, 2, 2, 2));
		/*
		 * Positions
		 */
		FloatMatrix p1 = FloatMatrix.vector(-1, -1,  1, 1f);
		FloatMatrix p2 = FloatMatrix.vector( 1, -1,  1, 1f);
		FloatMatrix p3 = FloatMatrix.vector( 1,  1,  1, 1f);
		FloatMatrix p4 = FloatMatrix.vector(-1,  1,  1, 1f);
		
		FloatMatrix p5 = FloatMatrix.vector(-1, -1, -1, 1f);
		FloatMatrix p6 = FloatMatrix.vector( 1, -1, -1, 1f);
		FloatMatrix p7 = FloatMatrix.vector( 1,  1, -1, 1f);
		FloatMatrix p8 = FloatMatrix.vector(-1,  1, -1, 1f);
		
		/*
		 * Normals
		 */
		FloatMatrix n1 = FloatMatrix.vector(-1,  0,  0);
		FloatMatrix n2 = FloatMatrix.vector( 1,  0,  0);
		FloatMatrix n3 = FloatMatrix.vector( 0, -1,  0);
		FloatMatrix n4 = FloatMatrix.vector( 0,  1,  0);
		FloatMatrix n5 = FloatMatrix.vector( 0,  0, -1);
		FloatMatrix n6 = FloatMatrix.vector( 0,  0,  1);
		
		/*
		 * Textures
		 */
		FloatMatrix blank = FloatMatrix.vector(0, 0);
		
		/*
		 * Colors
		 */
		FloatMatrix red = FloatMatrix.vector(1, 0, 0, 1);
		FloatMatrix green = FloatMatrix.vector(0, 1, 0, 1);
		FloatMatrix blue = FloatMatrix.vector(0, 0, 1, 1);
		FloatMatrix yellow = FloatMatrix.vector(1, 1, 0, 1);
		FloatMatrix cyan = FloatMatrix.vector(0, 1, 1, 1);
		FloatMatrix magenta = FloatMatrix.vector(1, 0, 1, 1);
		
		/*
		 * Front
		 */
		setVertex(0 , p1, n6, blank, red);	setVertex(1 , p2, n6, blank, red);
		setVertex(2 , p3, n6, blank, red);	setVertex(3 , p4, n6, blank, red);
		setFace(0, 0, 1, 2, 3);
		
		/*
		 * Back
		 */
		setVertex(4 , p5, n5, blank, green);	setVertex(5, p6, n5, blank, green);
		setVertex(6 , p7, n5, blank, green);	setVertex(7, p8, n5, blank, green);
		setFace(1, 4, 5, 6, 7);
		
		/*
		 * Left
		 */
		setVertex( 8, p1, n1, blank, blue);	setVertex( 9, p5, n1, blank, blue);
		setVertex(10, p8, n1, blank, blue);	setVertex(11, p4, n1, blank, blue);
		setFace(2, 8, 9, 10, 11);
		
		/*
		 * Right
		 */
		setVertex(12, p2, n2, blank, yellow);	setVertex(13, p6, n2, blank, yellow);
		setVertex(14, p7, n2, blank, yellow);	setVertex(15, p3, n2, blank, yellow);
		setFace(3, 12, 13, 14, 15);
		
		/*
		 * Top
		 */
		setVertex(16, p4, n4, blank, cyan);	setVertex(17, p3, n4, blank, cyan);
		setVertex(18, p7, n4, blank, cyan);	setVertex(19, p8, n4, blank, cyan);
		setFace(4, 16, 17, 18, 19);
		
		/*
		 * Bottom
		 */
		setVertex(20, p1, n3, blank, magenta);	setVertex(21, p2, n3, blank, magenta);
		setVertex(22, p6, n3, blank, magenta);	setVertex(23, p5, n3, blank, magenta);
		setFace(5, 20, 21, 22, 23);
	}
}
