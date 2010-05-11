package org.jdelaunay.delaunay;

/**
 * Delaunay Package.
 *
 * @author Jean-Yves MARTIN, Erwan BOCHER
 * @date 2009-01-12
 * @version 1.0
 */

import java.awt.*;
import java.util.*;
import com.vividsolutions.jts.geom.Coordinate;

public class MyTriangle extends MyElement {

	protected MyPoint[] points;
	protected MyEdge[] edges;

	private double x_center, y_center;
	private double radius;

	/**
	 * Initialize data structure This method is called by every constructor
	 */
	private void init() {
		points = new MyPoint[3];
		edges = new MyEdge[3];
		x_center = 0;
		y_center = 0;
		radius = -1;
	}

	/**
	 * Create a new triangle with points and edges
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param e1
	 * @param e2
	 * @param e3
	 */
	public MyTriangle() {
		super();
		init();
	}

	/**
	 * Create a new triangle with edges Add the points from the edges
	 * 
	 * @param e1
	 * @param e2
	 * @param e3
	 */
	public MyTriangle(MyEdge e1, MyEdge e2, MyEdge e3) {
		super();
		init();

		edges[0] = e1;
		edges[1] = e2;
		edges[2] = e3;

		points[0] = e1.getStart();
		points[1] = e1.getEnd();
		if (e2.getStart() == points[1])
			points[2] = e2.getEnd();
		else
			points[2] = e2.getStart();

		connectEdges();
		recomputeCenter();
		radius = points[0].squareDistance_2D(x_center, y_center);
	}

	/**
	 * Create a new triangle with points and edges
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param e1
	 * @param e2
	 * @param e3
	 */
	public MyTriangle(MyPoint p1, MyPoint p2, MyPoint p3, MyEdge e1, MyEdge e2,
			MyEdge e3) {
		super();
		init();

		points[0] = p1;
		points[1] = p2;
		points[2] = p3;

		edges[0] = e1;
		edges[1] = e2;
		edges[2] = e3;

		connectEdges();
		recomputeCenter();
		radius = p1.squareDistance_2D(x_center, y_center);
	}

	/**
	 * Create a Triangle from another triangle NB : it doesn't update edges
	 * connection
	 * 
	 * @param aTriangle
	 */
	public MyTriangle(MyTriangle aTriangle) {
		super((MyElement) aTriangle);
		init();

		for (int i = 0; i < 3; i++) {
			points[i] = aTriangle.points[i];
			edges[i] = aTriangle.edges[i];
		}

		x_center = aTriangle.x_center;
		y_center = aTriangle.y_center;
		radius = aTriangle.radius;
	}

	/**
	 * Get the ith points
	 * 
	 * @param i
	 * @return
	 */
	public MyPoint point(int i) {
		return points[i];
	}

	/**
	 * Get the ith edge
	 * 
	 * @param i
	 * @return
	 */
	public MyEdge edge(int i) {
		return edges[i];
	}

	/**
	 * Get the ith edge
	 * 
	 * @param i
	 * @return
	 */
	public void setEdge(int i, MyEdge anEdge) {
		edges[i] = anEdge;
	}

	/**
	 * Get points
	 * 
	 * @return
	 */
	public MyPoint[] getPoints() {
		return points;
	}

	/**
	 * Get Edges
	 * 
	 * @return
	 */
	public MyEdge[] getEdges() {
		return edges;
	}

	/**
	 * Get the radius of the circle
	 * 
	 * @return
	 */
	public double radius() {
		return Math.sqrt(radius);
	}

	/**
	 * Recompute the center of the circle that joins the points : the
	 * CircumCenter
	 */
	protected void recomputeCenter() {
		MyPoint p1 = points[0];
		MyPoint p2 = points[1];
		MyPoint p3 = points[2];

		double p1Sq = p1.x * p1.x + p1.y * p1.y;
		double p2Sq = p2.x * p2.x + p2.y * p2.y;
		double p3Sq = p3.x * p3.x + p3.y * p3.y;

		double ux = p2.x - p1.x;
		double uy = p2.y - p1.y;
		double vx = p3.x - p1.x;
		double vy = p3.y - p1.y;

		double cp = ux * vy - uy * vx;
		double cx, cy, cz;
		cx = cy = cz = 0.0;

		if (cp != 0) {
			cx = (p1Sq * (p2.y - p3.y) + p2Sq * (p3.y - p1.y) + p3Sq
					* (p1.y - p2.y))
					/ (2 * cp);
			cy = (p1Sq * (p3.x - p2.x) + p2Sq * (p1.x - p3.x) + p3Sq
					* (p2.x - p1.x))
					/ (2 * cp);
			cz = 0.0;

			x_center = cx;
			y_center = cy;

			radius = points[0].squareDistance_2D(x_center, y_center);
		} else {
			x_center = 0.0;
			y_center = 0.0;
			radius = -1;
		}

	}

	/**
	 * Reconnect triangle edges to rebuild topology
	 */
	protected void connectEdges() {
		for (int j = 0; j < 3; j++) {
			if (edges[j] == null)
				System.out.println("ERREUR");
			else {
				MyPoint start = edges[j].point[0];
				MyPoint end = edges[j].point[1];
				for (int k = 0; k < 3; k++) {
					if ((start != points[k]) && (end != points[k]))
						if (edges[j].isLeft(points[k])) {
							if (edges[j].left == null)
								edges[j].left = this;
							else
								edges[j].right = this;
						} else {
							if (edges[j].right == null)
								edges[j].right = this;
							else
								edges[j].left = this;
						}
				}
			}
		}
	}

	/**
	 * Reconnect triangle edges to rebuild topology
	 */
	protected void reconnectEdges() {
		for (int j = 0; j < 3; j++) {
			if (edges[j] == null)
				System.out.println("ERREUR");
			else {
				MyPoint start = edges[j].point[0];
				MyPoint end = edges[j].point[1];
				for (int k = 0; k < 3; k++) {
					if ((start != points[k]) && (end != points[k]))
						if (edges[j].isLeft(points[k])) {
							edges[j].left = this;
						} else {
							edges[j].right = this;
						}
				}
			}
		}
	}

	/**
	 * Check if the point is in or on the Circle 0 = outside 1 = inside 2 = on
	 * the circle
	 * 
	 * @param aPoint
	 * @return position 0 = outside 1 = inside 2 = on the circle
	 */
	public int inCircle(MyPoint aPoint) {
		// default is outside the circle
		int returnedValue = 0;

		// double distance = squareDistance(Center, aPoint);
		double ux = aPoint.x - x_center;
		double uy = aPoint.y - y_center;
		double distance = ux * ux + uy * uy;
		if (distance < radius - MyTools.epsilon2)
			// in the circle
			returnedValue = 1;
		else if (distance < radius + MyTools.epsilon2)
			// on the circle
			returnedValue = 2;

		return returnedValue;
	}

	/**
	 * Check if the point is inside the triangle / not
	 * 
	 * @param aPoint
	 * @return isInside
	 */
	public boolean isInside(MyPoint aPoint) {
		boolean isInside = true;

		int k = 0;
		while ((k < 3) && (isInside)) {
			MyEdge theEdge = edges[k];
			if (theEdge.left == this) {
				if (theEdge.isRight(aPoint))
					isInside = false;
			} else {
				if (theEdge.isLeft(aPoint))
					isInside = false;
			}
			k++;
		}

		return isInside;
	}

	/**
	 * Get Z value of a specific point in the triangle
	 * 
	 * @param aPoint
	 * @return isInside
	 */
	public double getSurfacePoint(MyPoint aPoint) {
		double ZValue = 0;

		MyPoint p1 = points[0];
		MyPoint p2 = points[1];
		MyPoint p3 = points[2];

		double ux = p2.x - p1.x;
		double uy = p2.y - p1.y;
		double uz = p2.z - p1.z;
		double vx = p3.x - p1.x;
		double vy = p3.y - p1.y;
		double vz = p3.z - p1.z;

		double a = uy * vz - uz * vy;
		double b = uz * vx - ux * vz;
		double c = ux * vy - uy * vx;
		double d = -a * p1.x - b * p1.y - c * p1.z;

		if (Math.abs(c) > MyTools.epsilon) {
			ZValue = (-a * aPoint.x - b * aPoint.y - d) / c;
		}

		return ZValue;
	}

	/**
	 * compute triangle area
	 * 
	 * @return area
	 */
	public double computeArea() {
		MyPoint p1 = points[0];
		MyPoint p2 = points[1];
		MyPoint p3 = points[2];

		double ux = p2.x - p1.x;
		double uy = p2.y - p1.y;
		double vx = p3.x - p1.x;
		double vy = p3.y - p1.y;
		double wx = p3.x - p2.x;
		double wy = p3.y - p3.y;

		double a = Math.sqrt(ux * ux + uy * uy);
		double b = Math.sqrt(vx * vx + vy * vy);
		double c = Math.sqrt(wx * wx + wy * wy);

		double area = Math.sqrt((a + b + c) * (b + c - a) * (c + a - b)
				* (a + b - c));

		return area;
	}

	/**
	 * check if one of the triangle's angle is less than minimum
	 * 
	 * @return minAngle
	 */
	public int badAngle(double tolarance) {
		double minAngle = 400;
		int returndeValue = -1;
		for (int k = 0; k < 3; k++) {
			int k1 = (k + 1) % 3;
			int k2 = (k1 + 1) % 3;

			MyPoint p1 = points[k];
			MyPoint p2 = points[k1];
			MyPoint p3 = points[k2];

			double ux = p2.x - p1.x;
			double uy = p2.y - p1.y;
			double vx = p3.x - p1.x;
			double vy = p3.y - p1.y;

			double dp = ux * vx + uy * vy;

			double angle = Math.acos(Math.sqrt(((dp * dp))
					/ ((ux * ux + uy * uy) * (vx * vx + vy * vy))))
					* (180d / Math.PI);
			if (angle < minAngle) {
				minAngle = angle;
				if (minAngle < tolarance)
					returndeValue = k;
			}
		}
		return returndeValue;
	}

	/**
	 * Check if triangle topology is correct / not
	 * 
	 * @return correct
	 */
	public boolean checkTopology() {
		boolean correct = true;

		// check if we do not have a point twice
		int j = 0;
		while ((j < 3) && (correct)) {
			int foundPoint = 0;
			for (int k = 0; k < 3; k++) {
				if (points[j] == points[k])
					foundPoint++;
			}
			if (foundPoint != 1)
				correct = false;
			j++;
		}

		// check if we do not have an edge twice
		j = 0;
		while ((j < 3) && (correct)) {
			int foundEdge = 0;
			for (int k = 0; k < 3; k++) {
				if (edges[j] == edges[k])
					foundEdge++;
			}
			if (foundEdge != 1)
				correct = false;
			j++;
		}

		// check if each edge is connected to the triangle
		j = 0;
		while ((j < 3) && (correct)) {
			int foundEdge = 0;
			if (edges[j].left == this)
				foundEdge++;
			if (edges[j].right == this)
				foundEdge++;
			if (foundEdge != 1)
				correct = false;
			j++;
		}

		// Check if each edge is connected to a point of the triangle
		j = 0;
		while ((j < 3) && (correct)) {
			MyEdge aEdge = edges[j];
			int foundPoint = 0;
			for (int k = 0; k < 3; k++) {
				if (aEdge.getStart() == points[k])
					foundPoint++;
				else if (aEdge.getEnd() == points[k])
					foundPoint++;
			}
			if (foundPoint != 2)
				correct = false;
			j++;
		}

		// check if each edge is connected on the right side of the triangle
		j = 0;
		if (false)
			while ((j < 3) && (correct)) {
				MyPoint start = edges[j].getStart();
				MyPoint end = edges[j].getEnd();
				boolean found = false;
				int k = 0;
				while ((k < 3) && (correct) && (!found)) {
					if ((start != points[k]) && (end != points[k])) {
						if (edges[j].isLeft(points[k])) {
							if (edges[j].left != this)
								correct = false;
							if (edges[j].right == this)
								correct = false;
						} else {
							if (edges[j].right != this)
								correct = false;
							if (edges[j].left == this)
								correct = false;
						}
						found = true;
					}
					k++;
				}
				j++;
			}
		return correct;
	}

	/**
	 * Check if the triangles respects Delaunay constraints. the parameter
	 * thePoints is the list of points of the mesh
	 * 
	 * @param thePoints
	 * @return
	 */
	public boolean checkDelaunay(ArrayList<MyPoint> thePoints) {
		boolean correct = true;
		ListIterator<MyPoint> iterPoint = thePoints.listIterator();
		while ((iterPoint.hasNext()) && (correct)) {
			MyPoint aPoint = iterPoint.next();
			if ((aPoint != points[0]) && (aPoint != points[1])
					&& (aPoint != points[2])) {
				if (inCircle(aPoint) == 1)
					correct = false;
			}
		}

		return correct;
	}

	public boolean isFlatTriangle() {
		if (radius > 1e6)
			return true;
		else
			return false;
	}

	/**
	 * Check if the triangle is flat or not
	 * 
	 * @return isFlat
	 */
	public boolean isFlatSlope() {
		boolean isFlat = true;
		int i = 0;
		while ((i < 3) && (isFlat)) {
			if (!edges[i].isFlatSlope())
				isFlat = false;
			else
				i++;
		}
		return isFlat;
	}

	/**
	 * Get the point of the triangle that does not belong to the edge
	 * 
	 * @return isFlat
	 */
	protected MyPoint getAlterPoint(MyEdge anEdge) {
		MyPoint start = anEdge.getStart();
		MyPoint end = anEdge.getEnd();
		return getAlterPoint(start, end);
	}

	/**
	 * Get the point of the triangle that does not belong to the 2 points
	 * 
	 * @return isFlat
	 */
	protected MyPoint getAlterPoint(MyPoint start, MyPoint end) {
		MyPoint alterPoint = null;

		int i = 0;
		while ((i < 3) && (alterPoint == null)) {
			if ((points[i] != start) && (points[i] != end))
				alterPoint = points[i];
			else
				i++;
		}

		return alterPoint;
	}

	/**
	 * Get the edge of the triangle that includes the two point
	 * 
	 * @return alterEdge
	 */
	protected MyEdge getEdgeFromPoints(MyPoint p1, MyPoint p2) {
		MyEdge alterEdge = null;
		int i = 0;
		while ((i < 3) && (alterEdge == null)) {
			MyEdge testEdge = edges[i];
			if ((testEdge.getStart() == p1) && (testEdge.getEnd() == p2))
				alterEdge = testEdge;
			else if ((testEdge.getStart() == p2) && (testEdge.getEnd() == p1))
				alterEdge = testEdge;
			i++;
		}

		return alterEdge;
	}

	/**
	 * Get the barycenter of the triangle
	 * 
	 * @return isFlat
	 */
	public MyPoint getBarycenter() {
		double x = 0, y = 0, z = 0;
		for (int i = 0; i < 3; i++) {
			x += points[i].x;
			y += points[i].y;
			z += points[i].z;
		}
		x /= 3;
		y /= 3;
		z /= 3;
		return new MyPoint(x, y, z);
	}

	/**
	 * Get the Circuncenter of the triangle
	 * 
	 * @return coord
	 */
	public Coordinate getCircuncenter() {
		return new Coordinate(x_center, y_center, 0.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String myString = new String("Triangle : \n");
		for (int i = 1; i < 3; i++)
			myString += points[i] + "\n";
		return myString;
	}

	/**
	 * Set the edge color Must be used only when using package drawing
	 * 
	 * @param g
	 */
	protected void setColor(Graphics g) {
		if (isFlatSlope())
			g.setColor(Color.green);
		else
			g.setColor(Color.yellow);
	}

	/**
	 * Display the triangle in a JPanel Must be used only when using package
	 * drawing
	 * 
	 * @param g
	 * @param decalageX
	 * @param decalageY
	 * @param minX
	 * @param minY
	 * @param scaleX
	 * @param scaleY
	 */
	protected void displayObject(Graphics g, int decalageX, int decalageY,
			double minX, double minY, double scaleX, double scaleY) {
		int[] xPoints, yPoints;
		xPoints = new int[3];
		yPoints = new int[3];

		xPoints[0] = (int) ((points[0].x - minX) * scaleX + decalageX);
		xPoints[1] = (int) ((points[1].x - minX) * scaleX + decalageX);
		xPoints[2] = (int) ((points[2].x - minX) * scaleX + decalageX);

		yPoints[0] = (int) ((points[0].y - minY) * scaleY + decalageY);
		yPoints[1] = (int) ((points[1].y - minY) * scaleY + decalageY);
		yPoints[2] = (int) ((points[2].y - minY) * scaleY + decalageY);

		g.fillPolygon(xPoints, yPoints, 3);

		for (int i = 0; i < 3; i++) {
			edges[i].setColor(g);
			edges[i].displayObject(g, decalageX, decalageY, minX, minY, scaleX,
					scaleY);
		}
	}

	/**
	 * Display the triangle in a JPanel Must be used only when using package
	 * drawing
	 * 
	 * @param g
	 * @param decalageX
	 * @param decalageY
	 */
	protected void displayObjectCircles(Graphics g, int decalageX, int decalageY) {
		double r = Math.sqrt(radius);
		g.setColor(Color.red);
		g.drawOval((int) (x_center) + decalageX, decalageY - (int) (y_center),
				1, 1);
		g.drawOval((int) (x_center - r) + decalageX, decalageY
				- (int) (y_center + r), (int) r * 2, (int) r * 2);
	}
}