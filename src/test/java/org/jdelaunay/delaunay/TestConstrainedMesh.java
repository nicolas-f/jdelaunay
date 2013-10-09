/**
 *
 * jDelaunay is a library dedicated to the processing of Delaunay and constrained
 * Delaunay triangulations from PSLG inputs.
 *
 * This library is developed at French IRSTV institute as part of the AvuPur and Eval-PDU project,
 * funded by the French Agence Nationale de la Recherche (ANR) under contract
 * ANR-07-VULN-01 and ANR-08-VILL-0005-01 .
 *
 * jDelaunay is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2010-2012 IRSTV FR CNRS 2488
 *
 * jDelaunay is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * jDelaunay is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * jDelaunay. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.jdelaunay.delaunay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import org.jdelaunay.delaunay.error.DelaunayError;
import org.jdelaunay.delaunay.geometries.DEdge;
import org.jdelaunay.delaunay.geometries.DPoint;
import org.jdelaunay.delaunay.geometries.DTriangle;
import org.jdelaunay.delaunay.tools.Tools;

/**
 * This class checks that the constrained triangulation is well performed.
 * @author Alexis Guéganno
 */
public class TestConstrainedMesh extends BaseUtility {

	/**
	 * Test the generation of a constrained mesh on a really simple configuration
	 * We have one constraint edge and two other points (4 points, so), and
	 * the constraint is supposed to prevent the execution of the flip flap
	 * algorithm.
	 * @throws DelaunayError
	 */
	public void testSimpleConstraint() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr = new DEdge(0,3,0,8,3,0);
		mesh.addConstraintEdge(constr);
		mesh.addPoint(new DPoint(4,5,0));
		mesh.addPoint(new DPoint(4,1,0));
		mesh.processDelaunay();
//		show(mesh);
		DTriangle tri1 = new DTriangle(constr, new DEdge(0,3,0,4,5,0), new DEdge(4,5,0,8,3,0));
		DTriangle tri2 = new DTriangle(constr, new DEdge(0,3,0,4,1,0), new DEdge(4,1,0,8,3,0));
		assertTrue(mesh.getTriangleList().contains(tri1));
		assertTrue(mesh.getTriangleList().contains(tri2));
//		assertTrue(mesh.isMeshComputed());

		mesh = new ConstrainedMesh();
		constr = new DEdge(3,0,0,3,6,0);
		mesh.addConstraintEdge(constr);
		mesh.addPoint(new DPoint(1,3,0));
		mesh.addPoint(new DPoint(5,1,0));
		mesh.processDelaunay();
//		show(mesh);
		tri1 = new DTriangle(constr, new DEdge(1,3,0,3,6,0), new DEdge(1,3,0,3,0,0));
		tri2 = new DTriangle(constr, new DEdge(3,6,0,5,1,0), new DEdge(5,1,0,3,0,0));
		assertTrue(mesh.getTriangleList().size()==2);
		assertTrue(mesh.getTriangleList().contains(tri1));
		assertTrue(mesh.getTriangleList().contains(tri2));
		assertTrue(mesh.isMeshComputed());
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);
	}

	/**
	 * Another test case, with a flip-flap during the processing.
	 * The input contains one horizontal constraints with two points upper
	 * and two points lower than it
	 * @throws DelaunayError
	 */
	public void testOneConstraintFourPoints() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(0,3,0,8,3,0));
		mesh.addPoint(new DPoint(3,1,0));
		mesh.addPoint(new DPoint(5,0,0));
		mesh.addPoint(new DPoint(4,5,0));
		mesh.addPoint(new DPoint(6,4,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,8,3,0), new DEdge(8,3,0,3,1,0), new DEdge(3,1,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(5,0,0,8,3,0), new DEdge(8,3,0,3,1,0), new DEdge(3,1,0,5,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,4,5,0), new DEdge(4,5,0,6,4,0), new DEdge(6,4,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,8,3,0), new DEdge(8,3,0,6,4,0), new DEdge(6,4,0,0,3,0))));
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);
	}

	/**
	 * the same configuration as the previous test, with a larger input.
	 * @throws DelaunayError
	 */
	public void testOneConstraintFourPointsExtended() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(0,3,0,8,3,0));
		mesh.addConstraintEdge(new DEdge(10,0,0,10,6,0));
		mesh.addPoint(new DPoint(3,1,0));
		mesh.addPoint(new DPoint(5,0,0));
		mesh.addPoint(new DPoint(4,5,0));
		mesh.addPoint(new DPoint(6,4,0));
//		mesh.addPoint(new DPoint(9,6,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,8,3,0), new DEdge(8,3,0,3,1,0), new DEdge(3,1,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(5,0,0,8,3,0), new DEdge(8,3,0,3,1,0), new DEdge(3,1,0,5,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,4,5,0), new DEdge(4,5,0,6,4,0), new DEdge(6,4,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,8,3,0), new DEdge(8,3,0,6,4,0), new DEdge(6,4,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(5,0,0,8,3,0), new DEdge(8,3,0,10,0,0), new DEdge(10,0,0,5,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,6,0,8,3,0), new DEdge(8,3,0,10,0,0), new DEdge(10,0,0,10,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,6,0,8,3,0), new DEdge(8,3,0,6,4,0), new DEdge(6,4,0,10,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,6,0,4,5,0), new DEdge(4,5,0,6,4,0), new DEdge(6,4,0,10,6,0))));
		assertTrue(triangles.size()==8);
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);
	}

	/**
	 * Performs a test with many constraints and input points.
	 * The input constraints does not intersect here.
	 */
	public void testManyConstraints() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr = new DEdge(0,3,0,8,3,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(9,0,0,9,6,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(12,6,0,8,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(5,4,0,8,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(12,6,0,12,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(8,3,0,9,6,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(8,7,0,12,12,0);
		mesh.addConstraintEdge(constr);
		mesh.addPoint(new DPoint(4,5,0));
		mesh.addPoint(new DPoint(4,1,0));
		mesh.addPoint(new DPoint(10,3,0));
		mesh.addPoint(new DPoint(11,9,0));
		mesh.processDelaunay();
		List<DTriangle> triangles = mesh.getTriangleList();
//                show(mesh);
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(0,3,0,8,3,0),
			new DEdge(0,3,0,5,4,0),
			new DEdge(5,4,0,8,3,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(0,3,0,8,3,0),
			new DEdge(8,3,0,4,1,0),
			new DEdge(4,1,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(0,3,0,5,4,0),
			new DEdge(5,4,0,4,5,0),
			new DEdge(4,5,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(0,3,0,12,12,0),
			new DEdge(12,12,0,4,5,0),
			new DEdge(4,5,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(8,7,0,12,12,0),
			new DEdge(12,12,0,4,5,0),
			new DEdge(4,5,0,8,7,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(8,7,0,5,4,0),
			new DEdge(5,4,0,4,5,0),
			new DEdge(4,5,0,8,7,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(8,7,0,5,4,0),
			new DEdge(5,4,0,8,3,0),
			new DEdge(8,3,0,8,7,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(8,7,0,9,6,0),
			new DEdge(9,6,0,8,3,0),
			new DEdge(8,3,0,8,7,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(9,0,0,9,6,0),
			new DEdge(9,6,0,8,3,0),
			new DEdge(8,3,0,9,0,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(9,0,0,9,6,0),
			new DEdge(9,6,0,10,3,0),
			new DEdge(10,3,0,9,0,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(9,0,0,12,6,0),
			new DEdge(12,6,0,10,3,0),
			new DEdge(10,3,0,9,0,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(9,6,0,12,6,0),
			new DEdge(12,6,0,10,3,0),
			new DEdge(10,3,0,9,6,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(9,6,0,12,6,0),
			new DEdge(12,6,0,8,7,0),
			new DEdge(8,7,0,9,6,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(12,7,0,12,6,0),
			new DEdge(12,6,0,8,7,0),
			new DEdge(8,7,0,12,7,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(12,7,0,11,9,0),
			new DEdge(11,9,0,12,12,0),
			new DEdge(12,12,0,12,7,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(12,12,0,11,9,0),
			new DEdge(11,9,0,8,7,0),
			new DEdge(8,7,0,12,12,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(9,0,0,8,3,0),
			new DEdge(8,3,0,4,1,0),
			new DEdge(4,1,0,9,0,0))));
		assertTrue(triangles.contains(new DTriangle(
			new DEdge(11,9,0,8,7,0),
			new DEdge(12,7,0,11,9,0),
			new DEdge(8,7,0,12,7,0))));
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);

	}

	/**
	 * Perform constrained triangulation on a set of edges which is designed
	 * to cause the use of the remove ghost algorithm in ConstrainedMesh.
	 * Check that unnecessary edges and triangles are well removed.
	 */
	public void testRemoveGhost() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr = new DEdge(1,1,0,5,1,0);
		mesh.addConstraintEdge(constr);
		mesh.addPoint(new DPoint (3,0,0));
		mesh.addPoint(new DPoint (3,2,0));
		mesh.addPoint(new DPoint (3,4,0));
		mesh.addPoint(new DPoint (3,6,0));
		mesh.addPoint(new DPoint (3,8,0));
		mesh.addPoint(new DPoint (3,10,0));
		mesh.addPoint(new DPoint (3,12,0));
		mesh.addPoint(new DPoint (3,14,0));
		mesh.addPoint(new DPoint (3,16,0));
		mesh.processDelaunay();
//		show(mesh);
		assertFalse(mesh.getPoints().contains(new DPoint(0,0,0)));
		List<DTriangle> triangles = mesh.getTriangleList();
		for(DTriangle tri : triangles){
			assertFalse(tri.contains(new DPoint(0,0,0)));
		}
		for(DEdge ed : mesh.getEdges()){
			assertFalse(ed.contains(new DPoint(0,0,0)));
		}
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,1,0,3,2,0), new DEdge(3,2,0,3,4,0), new DEdge(3,4,0,1,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,1,0,3,6,0), new DEdge(3,6,0,3,4,0), new DEdge(3,4,0,1,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,1,0,3,8,0), new DEdge(3,8,0,3,6,0), new DEdge(3,6,0,1,1,0))));
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);
	}

	/**
	 * Checks that the objects (points and edges, to be accurate) are not duplicated
	 * in the mesh.
	 * @throws DelaunayError
	 */
	public void testObjectsUnicity() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr = new DEdge(0,3,0,8,3,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(9,0,0,9,6,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(12,6,0,8,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(5,4,0,8,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(12,6,0,12,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(8,3,0,9,6,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(8,7,0,12,12,0);
		mesh.addConstraintEdge(constr);
		mesh.addPoint(new DPoint(4,5,0));
		mesh.addPoint(new DPoint(4,1,0));
		mesh.addPoint(new DPoint(10,3,0));
		mesh.addPoint(new DPoint(11,9,0));
		mesh.processDelaunay();
		List<DTriangle> triangles = mesh.getTriangleList();
		int index;
		DEdge comp;
		List<DEdge> edges = mesh.getEdges();
		List<DPoint> points = mesh.getPoints();
		DPoint pt;
		for(DTriangle tri : triangles){
			index = edges.indexOf(tri.getEdge(0));
			comp = edges.get(index) ;
			assertTrue(tri.getEdge(0)==comp);
			pt=points.get(points.indexOf(tri.getPoint(0)));
			assertTrue(tri.getPoint(0) == pt);
			index = edges.indexOf(tri.getEdge(1));
			comp = edges.get(index) ;
			assertTrue(tri.getEdge(1)==comp);
			pt=points.get(points.indexOf(tri.getPoint(1)));
			assertTrue(tri.getPoint(1) == pt);
			index = edges.indexOf(tri.getEdge(2));
			comp = edges.get(index) ;
			assertTrue(tri.getEdge(2)==comp);
			pt=points.get(points.indexOf(tri.getPoint(2)));
			if(tri.getPoint(2) == pt){
				assertTrue(true);
			} else {
				System.out.println(pt);
				System.out.println(tri.getPoint(2));
				assertTrue(false);
			}
		}
	}

	/**
	 * Performs a test on an input where the two first points can't be used
	 * to build a triangle with a "ghost point" which would be placed before
	 * the first point of the input. Indeed, the second point can't be seen
	 * from this ghost point, because of the constraints given in input.
	 * @throws DelaunayError
	 */
	public void testProtectedSecondPoint() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr1 = new DEdge(0,2,0,5,6,0);
		DEdge constr2 = new DEdge(0,2,0,4,0,0);
		mesh.addConstraintEdge(constr2);
		mesh.addConstraintEdge(constr1);
		mesh.addPoint(new DPoint(2,3,0));
		mesh.addPoint(new DPoint(3,6,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles=mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(constr1,new DEdge(0,2,0,3,6,0),new DEdge(3,6,0,5,6,0))));
		assertTrue(triangles.contains(new DTriangle(constr1,new DEdge(0,2,0,2,3,0),new DEdge(2,3,0,5,6,0))));
		assertTrue(triangles.contains(new DTriangle(constr2,new DEdge(0,2,0,2,3,0),new DEdge(2,3,0,4,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(2,3,0,5,6,0),new DEdge(5,6,0,4,0,0),new DEdge(2,3,0,4,0,0))));
		assertTrue(triangles.size()==4);
	}

	/**
	 * Tests that we can manage the case when two points give two degenerated
	 * edges during the processing.
	 */
	public void testTwoColinearDegeneratedEdges() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr1 = new DEdge(2,2,0,7,4,0);
		DEdge constr2 = new DEdge (2,2,0,7,0,0);
		mesh.addConstraintEdge(constr2);
		mesh.addConstraintEdge(constr1);
		mesh.addPoint(new DPoint(1,1,0));
		mesh.addPoint(new DPoint(1,3,0));
		mesh.addPoint(new DPoint(4,2,0));
		mesh.addPoint(new DPoint(6,2,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,1,0,1,3,0), new DEdge(2,2,0,1,3,0), new DEdge(1,1,0,2,2,0))));
		assertTrue(triangles.contains(new DTriangle(constr1, new DEdge(2,2,0,1,3,0), new DEdge(1,3,0,7,4,0))));
		assertTrue(triangles.contains(new DTriangle(constr1, new DEdge(2,2,0,4,2,0), new DEdge(4,2,0,7,4,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,2,0,7,4,0), new DEdge(7,4,0,7,0,0), new DEdge(7,0,0,6,2,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,2,0,6,2,0), new DEdge(6,2,0,7,4,0), new DEdge(7,4,0,4,2,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,2,0,6,2,0), new DEdge(6,2,0,7,0,0), new DEdge(7,0,0,4,2,0))));
		assertTrue(triangles.contains(new DTriangle(constr2, new DEdge(2,2,0,4,2,0), new DEdge(4,2,0,7,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,1,0,7,0,0), constr2, new DEdge(1,1,0,2,2,0))));
		assertTrue(triangles.size()==8);
		List<DEdge> edges = mesh.getEdges();
		assertTrue(edges.size()==14);
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);

	}


	/**
	 * Performs a delaunay triangulation with an input without constraints.
	 */
	public void testSwapEdges() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DPoint p1=new DPoint(0,4,0);
		DPoint p2=new DPoint(2,8,0);
		DPoint p3=new DPoint(2,0,0);
		DPoint p4=new DPoint(4,4,0);
		mesh.addPoint(p1);
		mesh.addPoint(p2);
		mesh.addPoint(p3);
		mesh.addPoint(p4);
		mesh.processDelaunay();
		DTriangle tri1 = new DTriangle(new DEdge(p1, p2), new DEdge(p2, p4), new DEdge(p4, p1));
		DTriangle tri2 = new DTriangle(new DEdge(p1, p3), new DEdge(p3, p4), new DEdge(p4, p1));
		assertTrue(mesh.getTriangleList().contains(tri1));
		assertTrue(mesh.getTriangleList().contains(tri2));
		assertGIDUnicity(mesh);
		assertUseEachEdge(mesh);
		assertUseEachPoint(mesh);
	}


	/**
	 * Performs a delaunay triangulation with an input without constraints.
	 */
	public void testSwapEdgesBis() throws DelaunayError{
		ConstrainedMesh mesh = new ConstrainedMesh();
		DPoint p1=new DPoint(0,1,0);
		DPoint p3=new DPoint(4,0,0);
		DPoint p4=new DPoint(4,1,0);
		DPoint p2=new DPoint(3,3,0);
		mesh.addPoint(p1);
		mesh.addPoint(p2);
		mesh.addPoint(p3);
		mesh.addPoint(p4);
		mesh.processDelaunay();
//		show(mesh);
		DTriangle tri1 = new DTriangle(new DEdge(p1, p2), new DEdge(p2, p4), new DEdge(p4, p1));
		DTriangle tri2 = new DTriangle(new DEdge(p1, p3), new DEdge(p3, p4), new DEdge(p4, p1));
		assertTrue(mesh.getTriangleList().contains(tri1));
		assertTrue(mesh.getTriangleList().contains(tri2));
	}

	/**
	 * Make a classical triangulation on a random set of points. For benchmark purposes.
	 * @throws DelaunayError
	 */
	public void testDelaunayTriangulationRandomPoints() throws DelaunayError{
		List<DPoint> randomPoint = BaseUtility.getRandomPoints(1000);
		ConstrainedMesh mesh = new ConstrainedMesh();
		for(DPoint pt : randomPoint){
			mesh.addPoint(pt);
		}
		mesh.addConstraintEdge(new DEdge(5,5,0,10,10,0));
		double t = System.currentTimeMillis();
		mesh.processDelaunay();
//		show(mesh);
		double t2 = System.currentTimeMillis();
		System.out.println("Needed time : "+(t2 - t));
		assertTrue(true);
	}

	/**
	 * try tro triangulate a simple cross.
	 */
	public void testCross() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(0,0,0,2,2,0));
		mesh.addConstraintEdge(new DEdge(0,4,0,2,2,0));
		mesh.addConstraintEdge(new DEdge(4,0,0,2,2,0));
		mesh.addConstraintEdge(new DEdge(4,4,0,2,2,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,0,0,2,2,0), new DEdge(0,4,0,2,2,0), new DEdge(0,0,0,0,4,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,4,0,2,2,0), new DEdge(4,4,0,2,2,0), new DEdge(4,4,0,0,4,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,0,0,2,2,0), new DEdge(4,0,0,2,2,0), new DEdge(0,0,0,4,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,4,0,2,2,0), new DEdge(4,0,0,2,2,0), new DEdge(4,4,0,4,0,0))));
	}

	/**
	 * Process a triangulation where the three first input points have the same
	 * x-coordinate, and are linked to constraints.
	 */
	public void test3HorizontalPoints() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(0,3,0,3,1,0));
		mesh.addConstraintEdge(new DEdge(3,1,0,8,0,0));
		mesh.addConstraintEdge(new DEdge(0,6,0,3,7,0));
		mesh.addConstraintEdge(new DEdge(3,7,0,5,5,0));
		mesh.addConstraintEdge(new DEdge(0,10,0,4,9,0));
		mesh.addConstraintEdge(new DEdge(4,9,0,9,9,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,3,1,0),new DEdge(3,1,0,5,5,0),new DEdge(5,5,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(5,5,0,0,3,0),new DEdge(0,3,0,3,7,0),new DEdge(3,7,0,5,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,3,7,0),new DEdge(3,7,0,0,6,0),new DEdge(0,6,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,7,0,0,6,0),new DEdge(0,6,0,0,10,0),new DEdge(0,10,0,3,7,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,10,0,3,7,0),new DEdge(3,7,0,4,9,0),new DEdge(4,9,0,0,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,9,0,0,10,0),new DEdge(0,10,0,9,9,0),new DEdge(9,9,0,4,9,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(9,9,0,4,9,0),new DEdge(4,9,0,5,5,0),new DEdge(5,5,0,9,9,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,9,0,5,5,0),new DEdge(3,7,0,5,5,0),new DEdge(3,7,0,4,9,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(5,5,0,9,9,0),new DEdge(9,9,0,8,0,0),new DEdge(8,0,0,5,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(8,0,0,5,5,0),new DEdge(5,5,0,3,1,0),new DEdge(3,1,0,8,0,0))));

	}

	public void test4verticalPoint() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addPoint(new DPoint(0,1,0));
		mesh.addPoint(new DPoint(0,4,0));
		mesh.addPoint(new DPoint(0,9,0));
		mesh.addPoint(new DPoint(0,12,0));
		mesh.addPoint(new DPoint(2,1,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,1,0,2,1,0),new DEdge(2,1,0,0,4,0),new DEdge(0,4,0,0,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,9,0,2,1,0),new DEdge(2,1,0,0,4,0),new DEdge(0,4,0,0,9,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,9,0,2,1,0),new DEdge(2,1,0,0,12,0),new DEdge(0,12,0,0,9,0))));
	}
	/**
	 * Test case where we have three constraints 
	 * @throws DelaunayError
	 */
	public void test3VerticalConstraints() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(3,0,0,3,3,0));
		mesh.addConstraintEdge(new DEdge(3,5,0,3,8,0));
		mesh.addConstraintEdge(new DEdge(3,10,0,3,13,0));
		mesh.addPoint(new DPoint(0,1,0));
		mesh.addPoint(new DPoint(0,4,0));
		mesh.addPoint(new DPoint(0,9,0));
		mesh.addPoint(new DPoint(0,12,0));
		mesh.addPoint(new DPoint(6,6,0));
		mesh.addPoint(new DPoint(6,7,0));
//		show(mesh);
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,1,0,3,0,0),new DEdge(3,0,0,3,3,0),new DEdge(3,3,0,0,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,1,0,0,4,0),new DEdge(0,4,0,3,3,0),new DEdge(3,3,0,0,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,5,0,0,4,0),new DEdge(0,4,0,3,3,0),new DEdge(3,3,0,3,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,5,0,0,4,0),new DEdge(0,4,0,0,9,0),new DEdge(0,9,0,3,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,5,0,3,8,0),new DEdge(3,8,0,0,9,0),new DEdge(0,9,0,3,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,10,0,3,8,0),new DEdge(3,8,0,0,9,0),new DEdge(0,9,0,3,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,10,0,0,12,0),new DEdge(0,12,0,0,9,0),new DEdge(0,9,0,3,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,10,0,0,12,0),new DEdge(0,12,0,3,13,0),new DEdge(3,13,0,3,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,10,0,6,7,0),new DEdge(6,7,0,3,13,0),new DEdge(3,13,0,3,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,10,0,6,7,0),new DEdge(6,7,0,3,8,0),new DEdge(3,8,0,3,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,6,0,6,7,0),new DEdge(6,7,0,3,8,0),new DEdge(3,8,0,6,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,6,0,3,5,0),new DEdge(3,5,0,3,8,0),new DEdge(3,8,0,6,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,6,0,3,5,0),new DEdge(3,5,0,3,3,0),new DEdge(3,3,0,6,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,6,0,3,0,0),new DEdge(3,0,0,3,3,0),new DEdge(3,3,0,6,6,0))));
	}

	/**
	 * A test case where the first triangle can't be built, and were the 2 first built edges
	 * will be p1p2 and p2p3
	 * @throws DelaunayError
	 */
	public void testTwoNeighbourConstraints() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(0,2,0,2,4,0));
		mesh.addConstraintEdge(new DEdge(0,10,0,3,8,0));
		mesh.addConstraintEdge(new DEdge(0,10,0,1,13,0));
		mesh.addConstraintEdge(new DEdge(3,14,0,1,13,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,2,0,2,4,0), new DEdge(2,4,0,0,10,0), new DEdge(0,10,0,0,2,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,8,0,2,4,0), new DEdge(2,4,0,0,10,0), new DEdge(0,10,0,3,8,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,8,0,1,13,0), new DEdge(1,13,0,0,10,0), new DEdge(0,10,0,3,8,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(3,8,0,1,13,0), new DEdge(1,13,0,3,14,0), new DEdge(3,14,0,3,8,0))));
		assertTrue(triangles.size()==4);
	}

	/**
	 * An extension of the previous case.
	 * @throws DelaunayError
	 */
	public void testTwistedConstraint() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(0,10,0,3,8,0));
		mesh.addConstraintEdge(new DEdge(0,10,0,1,13,0));
		mesh.addConstraintEdge(new DEdge(3,14,0,1,13,0));
		mesh.addConstraintEdge(new DEdge(3,14,0,2,15,0));
		mesh.addConstraintEdge(new DEdge(6,10,0,3,8,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,10,0,3,8,0), new DEdge(3,8,0,1,13,0), new DEdge(1,13,0,0,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,10,0,3,8,0), new DEdge(3,8,0,1,13,0), new DEdge(1,13,0,6,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,10,0,3,14,0), new DEdge(3,14,0,1,13,0), new DEdge(1,13,0,6,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(2,15,0,3,14,0), new DEdge(3,14,0,1,13,0), new DEdge(1,13,0,2,15,0))));


	}

	public void testCommonLeftAndRightPoint() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
//		mesh.addConstraintEdge(new DEdge(0,3,0,6,3,0));
		mesh.addConstraintEdge(new DEdge(0,3,0,10,6,0));
		mesh.addConstraintEdge(new DEdge(8,1,0,6,3,0));
		mesh.addConstraintEdge(new DEdge(8,5,0,6,3,0));
		mesh.addPoint(new DPoint(0,4,0));
		mesh.addPoint(new DPoint(6,0,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,6,3,0), new DEdge(6,3,0,6,0,0), new DEdge(6,0,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(8,1,0,6,3,0), new DEdge(6,3,0,6,0,0), new DEdge(6,0,0,8,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(8,1,0,6,3,0), new DEdge(6,3,0,8,5,0), new DEdge(8,5,0,8,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(8,1,0,10,6,0), new DEdge(10,6,0,8,5,0), new DEdge(8,5,0,8,1,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,10,6,0), new DEdge(10,6,0,8,5,0), new DEdge(8,5,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,10,6,0), new DEdge(10,6,0,0,4,0), new DEdge(0,4,0,0,3,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,3,0,6,3,0), new DEdge(6,3,0,8,5,0), new DEdge(8,5,0,0,3,0))));

	}

	/**
	 * This test contains a set of data that have been obtained from the chezine data.
	 * The points have been modified, in order to simplify the coordinate and
	 * help the understanding of the test.
	 * @throws DelaunayError
	 */
	public void testFromChezine() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		/*
		 * The points we use are, sorted :
		 * 0, -5, 80.0
		 * 4, -4.0, 80.0
		 * 6, 4.0, 80.0
		 * 6.0, 21.0, 80.0
		 * 7, 16.0, 80.0
		 * 10, 26.0, 80.0
		 * 11.0, 14, 80.0
		 * 12.0, 7, 80.0
		 * 16.0, 6, 80.0
		 */
		mesh.addConstraintEdge(new DEdge (0, -5, 80.0, 4, -4.0, 80.0));
		mesh.addConstraintEdge(new DEdge (4, -4.0, 80.0, 6, 4.0, 80.0));
		mesh.addConstraintEdge(new DEdge (6, 4.0, 80.0, 12.0, 7, 80.0));
		mesh.addConstraintEdge(new DEdge (12.0, 7, 80.0, 16.0, 6, 80.0));
		
		mesh.addConstraintEdge(new DEdge (6.0, 21.0, 80.0, 7, 16.0, 80.0));
		mesh.addConstraintEdge(new DEdge (6.0, 21.0, 80.0, 10, 26.0, 80.0));
		mesh.addConstraintEdge(new DEdge (7, 16.0, 80.0, 11.0, 14, 80.0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, -5, 80.0, 4, -4.0, 80.0), new DEdge(4, -4.0, 80.0, 6, 4.0, 80.0), new DEdge(6, 4.0, 80.0, 0, -5, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, -5, 80.0, 7, 16.0, 80.0), new DEdge(7, 16.0, 80.0, 6, 4.0, 80.0), new DEdge(6, 4.0, 80.0, 0, -5, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, -5, 80.0, 7, 16.0, 80.0), new DEdge(7, 16.0, 80.0, 6.0, 21.0, 80.0), new DEdge(6.0, 21.0, 80.0, 0, -5, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(11.0, 14, 80.0, 7, 16.0, 80.0), new DEdge(7, 16.0, 80.0, 6.0, 21.0, 80.0), new DEdge(6.0, 21.0, 80.0, 11.0, 14, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(11.0, 14, 80.0, 10, 26.0, 80.0), new DEdge(10, 26.0, 80.0, 6.0, 21.0, 80.0), new DEdge(6.0, 21.0, 80.0, 11.0, 14, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(11.0, 14, 80.0, 10, 26.0, 80.0), new DEdge(10, 26.0, 80.0, 16.0, 6, 80.0), new DEdge(16.0, 6, 80.0, 11.0, 14, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(11.0, 14, 80.0, 12.0, 7, 80.0), new DEdge(12.0, 7, 80.0, 16.0, 6, 80.0), new DEdge(16.0, 6, 80.0, 11.0, 14, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6, 4.0, 80.0, 12.0, 7, 80.0), new DEdge(12.0, 7, 80.0, 16.0, 6, 80.0), new DEdge(16.0, 6, 80.0, 6, 4.0, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6, 4.0, 80.0, 4, -4.0, 80.0), new DEdge(4, -4.0, 80.0, 16.0, 6, 80.0), new DEdge(16.0, 6, 80.0, 6, 4.0, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6, 4.0, 80.0, 12.0, 7, 80.0), new DEdge(12.0, 7, 80.0, 11.0, 14, 80.0), new DEdge(11.0, 14, 80.0, 6, 4.0, 80.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6, 4.0, 80.0, 7, 16.0, 80.0), new DEdge(7, 16.0, 80.0, 11.0, 14, 80.0), new DEdge(11.0, 14, 80.0, 6, 4.0, 80.0))));

	}

	/**
	 * A second test whose configuration comes from the chezine data.
	 * It tests that the triangulation is well performed when the first three points are colinear.
	 */
	public void testFromChezineBis() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (0, 0.6, 10, 13, -4, 10));

		mesh.addConstraintEdge(new DEdge (0, 63, 10.0, 17, 42, 10.0));

		mesh.addConstraintEdge(new DEdge (0, 77, 10.0, 3.0, 92.0, 10.0));
		mesh.addConstraintEdge(new DEdge (0, 118, 10.0, 13.0, 125, 10.0));
		mesh.addConstraintEdge(new DEdge (3.0, 92.0, 10.0, 13.0, 99, 10.0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, 0.6, 10, 13, -4, 10), new DEdge(13, -4, 10,17, 42, 10.0), new DEdge(17, 42, 10.0,0, 0.6, 10))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, 0.6, 10, 0, 63, 10.0), new DEdge(0, 63, 10.0 ,17, 42, 10.0), new DEdge(17, 42, 10.0,0, 0.6, 10))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, 77, 10.0, 0, 63, 10.0), new DEdge(0, 63, 10.0 ,17, 42, 10.0), new DEdge(17, 42, 10.0, 0, 77, 10.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, 77, 10.0, 13.0, 99, 10.0), new DEdge(13.0, 99, 10.0 ,17, 42, 10.0), new DEdge(17, 42, 10.0, 0, 77, 10.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, 77, 10.0, 13.0, 99, 10.0), new DEdge(13.0, 99, 10.0 ,3.0, 92.0, 10.0), new DEdge(3.0, 92.0, 10.0, 0, 77, 10.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0, 77, 10.0, 0, 118, 10.0), new DEdge(0, 118, 10.0,3.0, 92.0, 10.0), new DEdge(3.0, 92.0, 10.0, 0, 77, 10.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(13.0, 99, 10.0, 0, 118, 10.0), new DEdge(0, 118, 10.0,3.0, 92.0, 10.0), new DEdge(3.0, 92.0, 10.0, 13.0, 99, 10.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(13.0, 99, 10.0, 0, 118, 10.0), new DEdge(0, 118, 10.0,13.0, 125, 10.0), new DEdge(13.0, 125, 10.0, 13.0, 99, 10.0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(13.0, 99, 10.0, 17, 42, 10.0), new DEdge(17, 42, 10.0,13.0, 125, 10.0), new DEdge(13.0, 125, 10.0, 13.0, 99, 10.0))));

	}

	/**
	 * A test inherited from the former delaunay implementation.
	 * @throws DelaunayError
	 */
	public void testProcessStar() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addPoint(new DPoint(0, 0, 0));
		mesh.addPoint(new DPoint(10, 0, 0));
		mesh.addPoint(new DPoint(0, 10, 0));
		mesh.addPoint(new DPoint(10, 10, 0));
		mesh.addConstraintEdge(new DEdge(1, 5, 2, 4, 6, 2));
		mesh.addConstraintEdge(new DEdge(4, 6, 2, 5, 9, 2));
		mesh.addConstraintEdge(new DEdge(5, 9, 2, 6, 6, 2));
		mesh.addConstraintEdge(new DEdge(6, 6, 2, 9, 5, 2));
		mesh.addConstraintEdge(new DEdge(9, 5, 2, 6, 4, 2));
		mesh.addConstraintEdge(new DEdge(6, 4, 2, 5, 1, 2));
		mesh.addConstraintEdge(new DEdge(5, 1, 2, 4, 4, 2));
		mesh.addConstraintEdge(new DEdge(4, 4, 2, 1, 5, 2));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,0,0,5,1,2), new DEdge(5,1,2,10,0,0), new DEdge(10,0,0,0,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,0,0,5,1,2), new DEdge(5,1,2,4,4,2), new DEdge(4,4,2,0,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,0,0,1,5,2), new DEdge(1,5,2,4,4,2), new DEdge(4,4,2,0,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(0,0,0,1,5,2), new DEdge(1,5,2,0,10,0), new DEdge(0,10,0,0,0,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,6,2,1,5,2), new DEdge(1,5,2,0,10,0), new DEdge(0,10,0,4,6,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(4,6,2,5,9,2), new DEdge(5,9,2,0,10,0), new DEdge(0,10,0,4,6,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,10,0,5,9,2), new DEdge(5,9,2,0,10,0), new DEdge(0,10,0,10,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,10,0,5,9,2), new DEdge(5,9,2,6,6,2), new DEdge(6,6,2,10,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,10,0,9,5,2), new DEdge(9,5,2,6,6,2), new DEdge(6,6,2,10,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,4,2,9,5,2), new DEdge(9,5,2,10,0,0), new DEdge(10,0,0,6,4,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(10,10,0,9,5,2), new DEdge(9,5,2,10,0,0), new DEdge(10,0,0,10,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,4,2,5,1,2), new DEdge(5,1,2,10,0,0), new DEdge(10,0,0,6,4,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,4,2,5,1,2), new DEdge(5,1,2,4,4,2), new DEdge(4,4,2,6,4,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,4,2,4,6,2), new DEdge(4,6,2,4,4,2), new DEdge(4,4,2,6,4,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,5,2,4,6,2), new DEdge(4,6,2,4,4,2), new DEdge(4,4,2,1,5,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,4,2,4,6,2), new DEdge(4,6,2,6,6,2), new DEdge(6,6,2,6,4,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(5,9,2,4,6,2), new DEdge(4,6,2,6,6,2), new DEdge(6,6,2,5,9,2))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(6,4,2,9,5,2), new DEdge(9,5,2,6,6,2), new DEdge(6,6,2,6,4,2))));
	}

	public void testBoundaryIntegrity() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addPoint(new DPoint(1,6,0));
		mesh.addPoint(new DPoint(2,3,0));
		mesh.addPoint(new DPoint(4,7,0));
		mesh.addPoint(new DPoint(6,1,0));
		mesh.addPoint(new DPoint(7,5,0));
		mesh.addPoint(new DPoint(7,9,0));
		mesh.addPoint(new DPoint(9,11,0));
		mesh.addPoint(new DPoint(10,2,0));
		mesh.addPoint(new DPoint(12,10,0));
		mesh.addPoint(new DPoint(13,4,0));
		mesh.addPoint(new DPoint(13,7,0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.size()==12);
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,6,0,2,3,0), new DEdge(2,3,0,4,7,0),new DEdge(4,7,0,1,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,6,0,7,9,0), new DEdge(7,9,0,4,7,0),new DEdge(4,7,0,1,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(1,6,0,7,9,0), new DEdge(7,9,0,9,11,0),new DEdge(9,11,0,1,6,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(12,10,0,7,9,0), new DEdge(7,9,0,9,11,0),new DEdge(9,11,0,12,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(12,10,0,7,9,0), new DEdge(7,9,0,13,7,0),new DEdge(13,7,0,12,10,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(7,5,0,7,9,0), new DEdge(7,9,0,13,7,0),new DEdge(13,7,0,7,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(7,5,0,13,4,0), new DEdge(13,4,0,13,7,0),new DEdge(13,7,0,7,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(7,5,0,13,4,0), new DEdge(13,4,0,10,2,0),new DEdge(10,2,0,7,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(7,5,0,6,1,0), new DEdge(6,1,0,10,2,0),new DEdge(10,2,0,7,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(7,5,0,4,7,0), new DEdge(4,7,0,2,3,0),new DEdge(2,3,0,7,5,0))));
		assertTrue(triangles.contains(new DTriangle(new DEdge(7,5,0,4,7,0), new DEdge(4,7,0,7,9,0),new DEdge(7,9,0,7,5,0))));
	}

	public void testLongConstraintLine()throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	44, 13, 40,
							44, 37, 40));
		mesh.addConstraintEdge(new DEdge (	44, 13, 40,
							54, 0 , 40));
		mesh.addConstraintEdge(new DEdge (	44, 37, 40,
							64, 36, 40));
		mesh.addConstraintEdge(new DEdge	(	62, 60, 40,
							66, 40, 40));
		mesh.addConstraintEdge(new DEdge (	64, 36, 40,
							66, 40, 40));
		mesh.addConstraintEdge(new DEdge (	85, 40, 40,
							88, 20, 40));
		mesh.addConstraintEdge(new DEdge (	0 , 60, 50,
							5 , 40, 50));
		mesh.addConstraintEdge(new DEdge (	5 , 40, 50,
							24, 11, 50));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(true);

	}

	public void testChezineStress() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0, 15, 10,
							5, 13, 10));
		mesh.addConstraintEdge(new DEdge (	4, 0 , 10,
							5, 0 , 10));
		mesh.addConstraintEdge(new DEdge (	4, 0 , 10,
							10, 3, 10));
		mesh.addConstraintEdge(new DEdge (	5, 13, 10,
							8, 10, 10));
		mesh.addConstraintEdge(new DEdge (	8, 10, 10,
							10, 6, 10));
		mesh.addConstraintEdge(new DEdge (	10, 3, 10,
							12, 0, 10));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==7);
		assertTrue(tri.contains(new DTriangle(new DEdge(4,0,10,5,13,10), new DEdge(5,13,10,0,15,10), new DEdge(0,15,10,4,0,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(4,0,10,5,13,10), new DEdge(5,13,10,8,10,10), new DEdge(8,10,10,4,0,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(4,0,10,10,6,10), new DEdge(10,6,10,8,10,10), new DEdge(8,10,10,4,0,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(4,0,10,10,6,10), new DEdge(10,6,10,10,3,10), new DEdge(10,3,10,4,0,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(4,0,10,5,0,10), new DEdge(5,0,10,10,3,10), new DEdge(10,3,10,4,0,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(12,0,10,5,0,10), new DEdge(5,0,10,10,3,10), new DEdge(10,3,10,12,0,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(12,0,10,10,6,10), new DEdge(10,6,10,10,3,10), new DEdge(10,3,10,12,0,10))));
		
	}

	/**
	 * A test whose configuration is deducted from the chezine data.
	 * @throws DelaunayError
	 */
	public void testChezineStressBis() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0, 6, 10,
							5, 12, 10));
		mesh.addConstraintEdge(new DEdge (	2, 0 , 10,
							5, 0, 10));
		mesh.addConstraintEdge(new DEdge (	3, 5, 10,
							5, 4, 10));
		mesh.addConstraintEdge(new DEdge (	3, 5, 10,
							5, 7, 10));
		mesh.addConstraintEdge(new DEdge (	5, 0 , 10,
							7, 0 , 10));
		mesh.addConstraintEdge(new DEdge (	5, 4, 10,
							7, 0, 10));
		mesh.addConstraintEdge(new DEdge (	5, 7, 10,
							6, 10, 10));
		mesh.addConstraintEdge(new DEdge (	5, 12, 10,
							6, 10, 10));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==10);
		assertTrue(tri.contains(new DTriangle(new DEdge(0,6,10,2,0,10), new DEdge(2,0,10,3,5,10), new DEdge(3,5,10,0,6,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(0,6,10,5,7,10), new DEdge(5,7,10,3,5,10), new DEdge(3,5,10,0,6,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(0,6,10,5,7,10), new DEdge(5,7,10,5,12,10), new DEdge(5,12,10,0,6,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,10,10,5,7,10), new DEdge(5,7,10,5,12,10), new DEdge(5,12,10,6,10,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,10,10,5,7,10), new DEdge(5,7,10,7,0,10), new DEdge(7,0,10,6,10,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,4,10,5,7,10), new DEdge(5,7,10,7,0,10), new DEdge(7,0,10,5,4,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,4,10,5,0,10), new DEdge(5,0,10,7,0,10), new DEdge(7,0,10,5,4,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,4,10,5,0,10), new DEdge(5,0,10,2,0,10), new DEdge(2,0,10,5,4,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,4,10,5,7,10), new DEdge(5,7,10,3,5,10), new DEdge(3,5,10,5,4,10))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2,0,10,5,4,10), new DEdge(5,4,10,3,5,10), new DEdge(3,5,10,2,0,10))));
	}

	/**
	 * A triangle was forgotten due to a bug when adding a constraint in the mesh.
	 * @throws DelaunayError
	 */
	public void testForgottenTriangle() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0  , 4, 60.0,
							5  , 7, 60.0));
		mesh.addConstraintEdge(new DEdge (	4  , 10, 60.0,
							5  , 9, 60.0));
		mesh.addConstraintEdge(new DEdge (	5  , 7, 60.0,
							10 , 7, 60.0));
		mesh.addConstraintEdge(new DEdge (	5  , 10, 60.0,
							10 , 8, 60.0));
		mesh.addConstraintEdge(new DEdge (	12 , 0  , 60.0,
							15 , 2 , 60.0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==10);
		assertTrue(tri.contains(new DTriangle(new DEdge(0,4,60,4,10,60), new DEdge(4,10,60,5,7,60), new DEdge(5,7,60,0,4,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,9,60,4,10,60), new DEdge(4,10,60,5,7,60), new DEdge(5,7,60,5,9,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,9,60,10,8,60), new DEdge(10,8,60,5,7,60), new DEdge(5,7,60,5,9,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(10,7,60,10,8,60), new DEdge(10,8,60,5,7,60), new DEdge(5,7,60,10,7,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,9,60,4,10,60), new DEdge(4,10,60,5,10,60), new DEdge(5,10,60,5,9,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(5,9,60,10,8,60), new DEdge(10,8,60,5,10,60), new DEdge(5,10,60,5,9,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(10,7,60,10,8,60), new DEdge(10,8,60,15,2,60), new DEdge(15,2,60,10,7,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(10,7,60,12,0,60), new DEdge(12,0,60,15,2,60), new DEdge(15,2,60,10,7,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(10,7,60,12,0,60), new DEdge(12,0,60,5,7,60), new DEdge(5,7,60,10,7,60))));
		assertTrue(tri.contains(new DTriangle(new DEdge(0,4,60,12,0,60), new DEdge(12,0,60,5,7,60), new DEdge(5,7,60,0,4,60))));
		List<DEdge> edges = mesh.getEdges();
		assertTrue(edges.size()==18);
		assertTrue(edges.contains(new DEdge(0,4,60,4,10,60)));
		assertTrue(edges.contains(new DEdge(0,4,60,12,0,60)));
		assertTrue(edges.contains(new DEdge(0,4,60,5,7,60)));
		assertTrue(edges.contains(new DEdge(4,10,60,5,7,60)));
		assertTrue(edges.contains(new DEdge(12,0,60,5,7,60)));
		assertTrue(edges.contains(new DEdge(4,10,60,5,9,60)));
		assertTrue(edges.contains(new DEdge(5,9,60,5,7,60)));
		assertTrue(edges.contains(new DEdge(10,8,60,5,7,60)));
		assertTrue(edges.contains(new DEdge(10,7,60,5,7,60)));
		assertTrue(edges.contains(new DEdge(5,10,60,4,10,60)));
		assertTrue(edges.contains(new DEdge(5,10,60,5,9,60)));
		assertTrue(edges.contains(new DEdge(5,10,60,10,8,60)));
		assertTrue(edges.contains(new DEdge(5,9,60,10,8,60)));
		assertTrue(edges.contains(new DEdge(5,7,60,10,8,60)));
		assertTrue(edges.contains(new DEdge(10,7,60,10,8,60)));
		assertTrue(edges.contains(new DEdge(10,7,60,15,2,60)));
		assertTrue(edges.contains(new DEdge(10,7,60,12,0,60)));
		assertTrue(edges.contains(new DEdge(15,2,60,12,0,60)));
	}

	/**
	 * This test caused problem, the generation of the start boundary did not work.
	 * @throws DelaunayError
	 */
	public void testBuildStartBound() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0  , 5, 0,
							1, 3, 0));
		mesh.addConstraintEdge(new DEdge (	0  , 5, 0,
							7, 8, 0));
		mesh.addConstraintEdge(new DEdge (	1, 3, 0,
							6  , 1, 0));
		mesh.addConstraintEdge(new DEdge (	6  , 1, 0,
							8, 0 , 0));
		mesh.addConstraintEdge(new DEdge (	7, 8, 0,
							12 , 10, 0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==5);
		assertTrue(tri.contains(new DTriangle(new DEdge(7,8,0,0,5,0), new DEdge(0,5,0,1,3,0), new DEdge(1,3,0,7,8,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(7,8,0,6,1,0), new DEdge(6,1,0,1,3,0), new DEdge(1,3,0,7,8,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(7,8,0,6,1,0), new DEdge(6,1,0,8,0,0), new DEdge(8,0,0,7,8,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(7,8,0,12,10,0), new DEdge(12,10,0,8,0,0), new DEdge(8,0,0,7,8,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(1,3,0,6,1,0), new DEdge(6,1,0,8,0,0), new DEdge(8,0,0,1,3,0))));
		assertTrue(mesh.getEdges().size()==10);
	}

	/**
	 * A test with a vertical constraint that share its left point with another one.
	 * @throws DelaunayError
	 */
	public void testVerticalConstraintLinked() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	6, 2, 0,
							6, 7, 0));
		mesh.addConstraintEdge(new DEdge (	6, 2, 0,
							8, 0 , 0));
		mesh.addConstraintEdge(new DEdge (	6, 7, 0,
							11, 6, 0));
		mesh.addConstraintEdge(new DEdge (	0 , 12, 0,
							2 , 8, 0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==5);
		assertTrue(tri.contains(new DTriangle(new DEdge(6,7,0,11,6,0), new DEdge(11,6,0,0,12,0), new DEdge(0,12,0,6,7,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,7,0,2,8,0), new DEdge(2,8,0,0,12,0), new DEdge(0,12,0,6,7,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,7,0,2,8,0), new DEdge(2,8,0,6,2,0), new DEdge(6,2,0,6,7,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,7,0,11,6,0), new DEdge(11,6,0,6,2,0), new DEdge(6,2,0,6,7,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(8,0,0,11,6,0), new DEdge(11,6,0,6,2,0), new DEdge(6,2,0,8,0,0))));
		assertTrue(mesh.getEdges().size()==10);
	}

	/**
	 * A test with raw data from the catalunya level lines
	 * DPoint order :
	 * This tests may cause null pointer exception.
	 *
	 * @throws DelaunayError
	 */
	public void testFromCatalunya() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	1.5, 1, 0,
							6  , 0, 0));
		mesh.addConstraintEdge(new DEdge (	2, 8, 0,
							10 , 5, 0));
		mesh.addConstraintEdge(new DEdge (	2.5, 9, 0,
							19 , 5, 0));
		mesh.addConstraintEdge(new DEdge (	3, 13, 0,
							5  , 12, 0));
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==8);
		assertTrue(tri.contains(new DTriangle(new DEdge(2.5,9,0,2,8,0), new DEdge(2,8,0,3,13,0), new DEdge(3,13,0,2.5,9,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.5,9,0,5,12,0), new DEdge(5,12,0,3,13,0), new DEdge(3,13,0,2.5,9,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.5,9,0,5,12,0), new DEdge(5,12,0,19,5,0), new DEdge(19,5,0,2.5,9,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.5,9,0,10,5,0), new DEdge(10,5,0,19,5,0), new DEdge(19,5,0,2.5,9,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.5,9,0,10,5,0), new DEdge(10,5,0,2,8,0), new DEdge(2,8,0,2.5,9,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,0,0,10,5,0), new DEdge(10,5,0,2,8,0), new DEdge(2,8,0,6,0,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,0,0,10,5,0), new DEdge(10,5,0,19,5,0), new DEdge(19,5,0,6,0,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(6,0,0,1.5,1,0), new DEdge(1.5,1,0,2,8,0), new DEdge(2,8,0,6,0,0))));
	}

	public void testFromCatalunyaRicher() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	2.3, 5, 0,
							8, 9, 0));
		mesh.addConstraintEdge(new DEdge (	2.4, 48, 0,
							7, 46, 0));
		mesh.addConstraintEdge(new DEdge (	2.45, 62, 0,
							11, 58, 0));
		mesh.addConstraintEdge(new DEdge (	2.55, 66, 0,
							20, 57, 0));
		mesh.addConstraintEdge(new DEdge (	2.6, 90, 0,
							5, 82, 0));
//		show(mesh);
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==12);
		assertTrue(tri.contains(new DTriangle(new DEdge(2.6,90,0,2.55,66,0), new DEdge(2.55,66,0,2.45,62,0), new DEdge(2.45,62,0,2.6,90,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.6,90,0,2.55,66,0), new DEdge(2.55,66,0,5,82,0), new DEdge(5,82,0,2.6,90,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.6,90,0,20,57,0), new DEdge(20,57,0,5,82,0), new DEdge(5,82,0,2.6,90,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.55,66,0,20,57,0), new DEdge(20,57,0,5,82,0), new DEdge(5,82,0,2.55,66,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.55,66,0,20,57,0), new DEdge(20,57,0,11,58,0), new DEdge(11,58,0,2.55,66,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(7,46,0,20,57,0), new DEdge(20,57,0,11,58,0), new DEdge(11,58,0,7,46,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(8,9,0,20,57,0), new DEdge(20,57,0,7,46,0), new DEdge(7,46,0,8,9,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.45,62,0,2.4,48,0), new DEdge(2.4,48,0,11,58,0), new DEdge(11,58,0,2.45,62,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.45,62,0,2.55,66,0), new DEdge(2.55,66,0,11,58,0), new DEdge(11,58,0,2.45,62,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(7,46,0,2.4,48,0), new DEdge(2.4,48,0,11,58,0), new DEdge(11,58,0,7,46,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(7,46,0,2.4,48,0), new DEdge(2.4,48,0,8,9,0), new DEdge(8,9,0,7,46,0))));
		assertTrue(tri.contains(new DTriangle(new DEdge(2.3,5,0,2.4,48,0), new DEdge(2.4,48,0,8,9,0), new DEdge(8,9,0,2.3,5,0))));
		int index = mesh.getEdges().indexOf(new DEdge(2.3,5,0,8,9,0));
		assertTrue(mesh.getEdges().size()==21);
		assertTrue(mesh.getEdges().get(index).isLocked());

	}

	public void testLowestConstraintManagement() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (298508.5, 2258710.7, 0.0, 298541.30000000005, 2258672.200000001, 0.0));
		mesh.addConstraintEdge(new DEdge (298508.5, 2258710.7, 0.0, 298542.5, 2258759.5999999996, 0.0));
		mesh.addConstraintEdge(new DEdge (298509.10000000003, 2258861.1000000006, 0.0, 298516.9, 2258927.7, 0.0));
		mesh.addConstraintEdge(new DEdge (298509.10000000003, 2258861.1000000006, 0.0, 298540.29999999993, 2258842.299999999, 0.0));
		mesh.addConstraintEdge(new DEdge (298569.0999999999, 2258616.299999999, 0.0, 298571.99999999994, 2258623.999999999, 0.0));
		mesh.processDelaunay();
		assertTrue(mesh.getTriangleList().size()==8);
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298508.5, 2258710.7, 0.0,
								298541.30000000005, 2258672.200000001, 0.0),
							new DEdge(298541.30000000005, 2258672.200000001, 0.0,
								298542.5, 2258759.5999999996, 0.0),
							new DEdge(298542.5, 2258759.5999999996, 0.0,
								298508.5, 2258710.7, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298508.5, 2258710.7, 0.0,
								298509.10000000003, 2258861.1000000006, 0.0),
							new DEdge(298509.10000000003, 2258861.1000000006, 0.0,
								298542.5, 2258759.5999999996, 0.0),
							new DEdge(298542.5, 2258759.5999999996, 0.0,
								298508.5, 2258710.7, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298540.29999999993, 2258842.299999999, 0.0,
								298509.10000000003, 2258861.1000000006, 0.0),
							new DEdge(298509.10000000003, 2258861.1000000006, 0.0,
								298542.5, 2258759.5999999996, 0.0),
							new DEdge(298542.5, 2258759.5999999996, 0.0,
								298540.29999999993, 2258842.299999999, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298540.29999999993, 2258842.299999999, 0.0,
								298571.99999999994, 2258623.999999999, 0.0),
							new DEdge(298571.99999999994, 2258623.999999999, 0.0,
								298542.5, 2258759.5999999996, 0.0),
							new DEdge(298542.5, 2258759.5999999996, 0.0,
								298540.29999999993, 2258842.299999999, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298541.30000000005, 2258672.200000001, 0.0,
								298571.99999999994, 2258623.999999999, 0.0),
							new DEdge(298571.99999999994, 2258623.999999999, 0.0,
								298542.5, 2258759.5999999996, 0.0),
							new DEdge(298542.5, 2258759.5999999996, 0.0,
								298541.30000000005, 2258672.200000001, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298541.30000000005, 2258672.200000001, 0.0,
								298571.99999999994, 2258623.999999999, 0.0),
							new DEdge(298571.99999999994, 2258623.999999999, 0.0,
								298569.0999999999, 2258616.299999999, 0.0),
							new DEdge(298569.0999999999, 2258616.299999999, 0.0,
								298541.30000000005, 2258672.200000001, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298541.30000000005, 2258672.200000001, 0.0,
								298508.5, 2258710.7, 0.0),
							new DEdge(298508.5, 2258710.7, 0.0,
								298569.0999999999, 2258616.299999999, 0.0),
							new DEdge(298569.0999999999, 2258616.299999999, 0.0,
								298541.30000000005, 2258672.200000001, 0.0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
							new DEdge(298540.29999999993, 2258842.299999999, 0.0,
								298509.10000000003, 2258861.1000000006, 0.0),
							new DEdge(298509.10000000003, 2258861.1000000006, 0.0,
								298516.9, 2258927.7, 0.0),
							new DEdge(298516.9, 2258927.7, 0.0,
								298540.29999999993, 2258842.299999999, 0.0))));
	}

	public void testVerticalEdgePrecisionProblem() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (300638.4000000001, 2260120.0, 0.0, 300641.3, 2260119.5, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.29999999993, 2260093.5, 0.0, 300641.3, 2260113.9000000004, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.29999999993, 2260093.5, 0.0, 300641.3, 2260119.5, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.29999999993, 2260093.5, 0.0, 300641.4000000001, 2260085.4000000013, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.29999999993, 2260093.5, 0.0, 300671.9, 2260092.5999999996, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.3, 2260113.9000000004, 0.0, 300641.3, 2260119.5, 0.0));
		mesh.forceConstraintIntegrity();
		mesh.processDelaunay();
//		show(mesh);
		assertTrue(mesh.getTriangleList().size()==6);
	}

	/**
	 * This configuration, that comes from the Nantes landuse, caused problems in the triangulation
	 * computation.
	 * @throws DelaunayError
	 */
	public void testParcellaireExcerpt() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (300640.3, 2260085.2, 0.0, 300641.4000000001, 2260085.4000000013, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.10000000003, 2259945.6000000006, 0.0, 300641.3, 2259944.8000000007, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.29999999993, 2260093.5, 0.0, 300641.4000000001, 2260085.4000000013, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.29999999993, 2260093.5, 0.0, 300641.70000000007, 2260059.000000001, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.3, 2260113.9000000004, 0.0, 300641.3, 2260119.5, 0.0));
		mesh.addConstraintEdge(new DEdge (300641.70000000007, 2260059.000000001, 0.0, 300670.80000000005, 2260054.4000000013, 0.0));
		mesh.forceConstraintIntegrity();
		mesh.processDelaunay();
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.size()==13);
//		show(mesh);
	}

	/**
	 * This configuration caused some problems, as an intersection was not seen by the
	 * intersection algorithm.
	 * @throws DelaunayError
	 */
	public void testMistyIntersection() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (296448.7, 2254716.8, 0.0, 296449.60000000003, 2254721.9000000013, 0.0));
		mesh.addConstraintEdge(new DEdge (296448.8, 2254721.9000000004, 0.0, 296449.0999999999, 2254720.5999999987, 0.0));
		mesh.addConstraintEdge(new DEdge (296449.0999999999, 2254720.5999999987, 0.0, 296450.9, 2254714.3, 0.0));
		mesh.forceConstraintIntegrity();
		assertTrue(mesh.getConstraintEdges().size()==5);
		mesh.processDelaunay();
//		show(mesh);
		List<DTriangle> triangles = mesh.getTriangleList();
		assertTrue(triangles.size()==6);

	}

	/**
	 * A point were missing, due to a problem when managing vertical constraints.
	 * @throws DelaunayError
	 */
	public void testMissingIntersectionPoint() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (296458.5, 2254707.7, 0.0, 296459.50000000006, 2254696.6000000006, 0.0));
		mesh.addConstraintEdge(new DEdge (296459.00000000006, 2254700.6000000006, 0.0, 296459.0, 2254703.700000001, 0.0));
		mesh.addConstraintEdge(new DEdge (296459.00000000006, 2254700.6000000006, 0.0, 296459.3, 2254697.5, 0.0));
		mesh.forceConstraintIntegrity();
		assertTrue(mesh.getConstraintEdges().size()==5);
		mesh.processDelaunay();
//		show(mesh);
	}

	/**
	 * A problematic configuration from buildings of the Nantes area. Two overlapping
	 * constraint edges were not merged during the processing of the constraints intersections.
	 * @throws DelaunayError
	 */
	public void testProblemConfigBati() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (311784.2, 2251488.4, 25.7, 311792.0, 2251487.1, 25.9));
		mesh.addConstraintEdge(new DEdge (311784.9, 2251484.8, 27.0, 311785.4, 2251488.2, 25.7));
		mesh.addConstraintEdge(new DEdge (311784.9, 2251484.8, 27.0, 311791.9, 2251483.8, 27.9));
		mesh.addConstraintEdge(new DEdge (311785.4, 2251488.2, 25.7, 311792.0, 2251487.1, 25.9));
		mesh.forceConstraintIntegrity();
		mesh.processDelaunay();
//		show(mesh);
		assertTrue(mesh.getTriangleList().size()==3);
		assertTrue(mesh.getConstraintEdges().size()==4);
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(311784.2, 2251488.4, 25.7,311785.4, 2251488.2, 25.7)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(311785.4, 2251488.2, 25.7,311792.0, 2251487.1, 25.9)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(311785.4, 2251488.2, 25.7,311784.9, 2251484.8, 27.0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(311784.9, 2251484.8, 27.0,311791.9, 2251483.8, 27.9)));
	}

	public void testinsertEdgeOnePoint() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge(5,5,0,5,5,0));
		assertTrue(mesh.getConstraintEdges().isEmpty());
		assertTrue(mesh.getPoints().size()==1);
	}

	/**
	 * The triangulation is broken when an edge where extremities are both the same
	 * is encountered.
	 * @throws DelaunayError
	 */
	public void testProblemParcCourbe() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (294226.6664782842, 2262000.0, 0.0, 294240.0, 2261994.374985099, 0.0));
		mesh.addConstraintEdge(new DEdge (294226.6664782842, 2262000.0, 0.0, 294240.0, 2262015.6519720037, 0.0));
		mesh.addConstraintEdge(new DEdge (294240.0, 2261994.374985099, 0.0, 294260.0, 2261996.7213647845, 0.0));
		mesh.addConstraintEdge(new DEdge (294240.0, 2262015.6519720037, 0.0, 294260.0, 2262048.163176333, 0.0));
		mesh.addConstraintEdge(new DEdge (294260.0, 2261996.7213647845, 0.0, 294300.0, 2261968.5714285714, 0.0));
		mesh.addConstraintEdge(new DEdge (294260.0, 2262048.163176333, 0.0, 294340.0, 2262079.375104308, 0.0));
		mesh.addConstraintEdge(new DEdge (294273.1999999893, 2261630.0, 0.0, 294273.1999999893, 2261630.0, 0.0));
		mesh.addConstraintEdge(new DEdge (294273.1999999893, 2261630.0, 0.0, 294337.19999999995, 2261623.999999999, 0.0));
		mesh.addConstraintEdge(new DEdge (294300.0, 2261968.5714285714, 0.0, 294320.0, 2261965.7970565795, 0.0));
		mesh.forceConstraintIntegrity();
		mesh.processDelaunay();
		assertTrue(true);
	}

	/**
	 * A problematic configuration that appeared when merging data from many sources.
	 * @throws DelaunayError
	 */
	public void testProblemFusion() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (299371.8, 2258651.5, 52.4, 299374.2, 2258651.2, 52.4));
		mesh.addConstraintEdge(new DEdge (299372.0990874965, 2258656.8206091495, 52.4, 299379.8, 2258656.9000000004, 0.0));
		mesh.addConstraintEdge(new DEdge (299373.0, 2258651.200000001, 0.0, 299379.9, 2258651.200000001, 0.0));
		mesh.addConstraintEdge(new DEdge (299374.152991453, 2258645.700000001, 0.0, 299374.2, 2258651.2, 52.4));
		mesh.forceConstraintIntegrity();
		mesh.processDelaunay();
//		show(mesh);
		assertTrue(mesh.getTriangleList().size()==7);
		assertTrue(mesh.getConstraintEdges().size()==5);
	}

	/**
	 * A problematic configuration that appeared when merging data from many sources.
	 * @throws DelaunayError
	 */
	public void testConfigurationFusion() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (302313.2, 2256208.7, 0.0, 302320.4000000001, 2256209.500000001, 0.0));
		mesh.addConstraintEdge(new DEdge (302313.7, 2256215.9, 48.2, 302314.1, 2256208.8, 48.0));
		mesh.addConstraintEdge(new DEdge (302313.7, 2256215.9, 48.2, 302320.1, 2256216.6, 48.0));
		mesh.addConstraintEdge(new DEdge (302314.1, 2256208.8, 48.0, 302321.2, 2256209.6, 48.0));
		mesh.forceConstraintIntegrity();
		mesh.processDelaunay();
//		show(mesh);
		assertCoherence(mesh);
	}

	public void testConfigRoadFusion() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (293068.19999999995, 2258485.5, 0.0, 293072.4, 2258484.2, 0.0));
		mesh.addConstraintEdge(new DEdge (293068.7, 2258469.0, 57.7, 293071.8, 2258476.7, 58.2));
		mesh.addConstraintEdge(new DEdge (293068.7, 2258469.0, 57.7, 293079.4, 2258464.7, 58.0));
		mesh.addConstraintEdge(new DEdge (293069.2, 2258468.9000000004, 0.0, 293071.7, 2258475.3000000007, 0.0));
		mesh.addConstraintEdge(new DEdge (293069.2, 2258468.9000000004, 0.0, 293074.1, 2258451.5999999996, 0.0));
		mesh.forceConstraintIntegrity();
//		try{
		mesh.processDelaunay();
//		} catch(Exception e){
//
//		}
		assertTrue(mesh.getTriangleList().size()==11);
//		show(mesh);
	}

	public void testConfigRoadFusionBis() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (303972.0, 2256261.1, 37.3, 304006.4, 2256262.5, 36.7));
		mesh.addConstraintEdge(new DEdge (303974.1, 2256255.5, 37.1, 303998.8, 2256209.9, 36.5));
		mesh.addConstraintEdge(new DEdge (303991.7, 2256233.6, 42.6, 304001.6, 2256238.8, 41.8));
		mesh.addConstraintEdge(new DEdge (303996.6, 2256232.6, 39.6, 304003.4, 2256236.1, 39.6));
		mesh.addConstraintEdge(new DEdge (303996.7, 2256230.3, 41.1, 303997.6, 2256230.7, 41.1));
		mesh.addConstraintEdge(new DEdge (303996.7, 2256230.3, 41.1, 303998.2, 2256227.3, 42.5));
		mesh.forceConstraintIntegrity();
//		try{
		mesh.processDelaunay();
//		} catch(Exception e){
//
//		}
		assertTrue(mesh.getTriangleList().size()==15);
//		show(mesh);
	}
	/**
	 * This configuration caused a problem because of a bad insertion in the boundary.
	 * @throws DelaunayError
	 */
	public void testCantBuildTriangle() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0.97, 1, 10,
							7.10, 3.5, 11));
		mesh.addConstraintEdge(new DEdge (	1.27, 73, 20,
							5.55, 71, 21));
		mesh.addConstraintEdge(new DEdge (	1.45, 115, 30,
							4.22, 107, 31));
		mesh.addConstraintEdge(new DEdge (	1.49, 125, 40,
							2.81, 135, 41));
		mesh.addConstraintEdge(new DEdge (	5.19, 1024, 50,
							7.87, 1024, 51));
//		show(mesh);
		try{
			mesh.processDelaunay();
			assertTrue(true);
		} catch (DelaunayError d){
			assertFalse(true);
		}
//		show(mesh);
	}

	/**
	 * This test and the next two ones have been created because the configurations
	 * they contain caused DelaunayErrors or NullPointerExceptions. We don't check
	 * the content of the generated mesh, but just check we don't catch
	 * any delaunay error.
	 * @throws DelaunayError
	 */
	public void testCantBuildTriangleBis() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0.0, 0.0, 0,
							4.04, -3.89, 0));
		mesh.addConstraintEdge(new DEdge (	0.83, 199,0,
							5.77, 202,0));
		mesh.addConstraintEdge(new DEdge (	0.97, 235, 0,
							7.10, 238, 0));
		mesh.addConstraintEdge(new DEdge (	1.27, 307, 0,
							5.55, 306,0));
		mesh.addConstraintEdge(new DEdge (	1.35, 326, 0,
							18.41, 317, 0));
		mesh.addConstraintEdge(new DEdge (	1.45, 350, 0,
							4.22, 342, 0));
		mesh.addConstraintEdge(new DEdge (	1.49, 360, 0,
							2.81, 370, 0));
//		show(mesh);
		try{
			mesh.processDelaunay();
			assertTrue(true);
		} catch (DelaunayError d){
			assertFalse(true);
		}
//		show(mesh);
	}

	public void testCantBuildTriangleTer() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0.0, 0.0, 0,
							4.04, -3.89, 0));
		mesh.addConstraintEdge(new DEdge (	0.83, 199, 0,
							5.77, 203, 0));
		mesh.addConstraintEdge(new DEdge (	0.97, 235.18, 0,
							7.10, 239, 0));
		mesh.addConstraintEdge(new DEdge (	1.27, 307.82, 0,
							5.55, 306, 0));
		mesh.addConstraintEdge(new DEdge (	1.49, 360, 0,
							2.81, 370, 0));
		mesh.addConstraintEdge(new DEdge (	1.78, 430, 0,
							3.75, 427, 0));
		mesh.addConstraintEdge(new DEdge (	2.22, 537, 0,
							14.17, 533, 0));
		mesh.addConstraintEdge(new DEdge (	2.45, 593, 0,
							9.41, 592, 0));
		try{
			mesh.processDelaunay();
			assertTrue(true);
		} catch (DelaunayError d){
			assertFalse(true);
		}
		
	}

	public void testCantBuildTriangleQuattro() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0.27000000001862645, 218.3300000000745, 660.0,
							1.7199999999720603, 213.97000000067055, 660.0));
		mesh.addConstraintEdge(new DEdge (	0.8400000000256114, 150.29000000003725, 660.0,
							2.7899999999790452, 152.97000000067055, 660.0));
		mesh.addConstraintEdge(new DEdge (	0.9299999999930151, 312.910000000149, 710.0,
							3.2100000000209548, 310.3500000005588, 710.0));
		mesh.addConstraintEdge(new DEdge (	1.7199999999720603, 213.97000000067055, 660.0,
							5.349999999976717, 191.30000000074506, 660.0));
		mesh.addConstraintEdge(new DEdge (	2.0200000000186265, 91.84000000078231, 680.0,
							9.940000000002328, 104.0100000007078, 680.0));
		mesh.addConstraintEdge(new DEdge (	2.650000000023283, 0.0, 720.0,
							3.070000000006985, 24.12000000011176, 720.0));
		try{
			mesh.processDelaunay();
//			show(mesh);
			assertTrue(true);
		} catch (DelaunayError d){
			assertFalse(true);
		}
		List<DTriangle> tri = mesh.getTriangleList();
		assertTrue(tri.size()==14);
		assertTrue(tri.contains(new DTriangle(	new DEdge(0.27000000001862645, 218.3300000000745, 660.0,
									5.349999999976717, 191.30000000074506, 660.0),
								new DEdge(5.349999999976717, 191.30000000074506, 660.0,
									0.8400000000256114, 150.29000000003725, 660.0),
								new DEdge(0.8400000000256114, 150.29000000003725, 660.0,
									0.27000000001862645, 218.3300000000745, 660.0))));
		assertTrue(tri.contains(new DTriangle(	new DEdge(0.27000000001862645, 218.3300000000745, 660.0,
									3.2100000000209548, 310.3500000005588, 710.0),
								new DEdge(3.2100000000209548, 310.3500000005588, 710.0,
									1.7199999999720603, 213.97000000067055, 660.0),
								new DEdge(1.7199999999720603, 213.97000000067055, 660.0,
									0.27000000001862645, 218.3300000000745, 660.0))));
		assertTrue(tri.contains(new DTriangle(	new DEdge(2.0200000000186265, 91.84000000078231, 680.0,
									2.650000000023283, 0.0, 720.0),
								new DEdge(2.650000000023283, 0.0, 720.0,
									0.8400000000256114, 150.29000000003725, 660.0),
								new DEdge(0.8400000000256114, 150.29000000003725, 660.0,
									2.0200000000186265, 91.84000000078231, 680.0))));

	}

	/**
	 * A test designed with cross constraints, from girona level lines.
	 * x order :
	 *
	 * 0.0
	 * 0.77
	 * 1.19
	 * 1.45
	 * 1.82
	 * 2.58
	 * 3.20
	 * 3.70
	 * 4.88
	 * 5.97
	 * 6.45
	 * 6.65
	 * 6.89
	 * 7.76
	 * 9.5
	 * 
	 * @throws DelaunayError
	 */
	public void testCrossedConstraints() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (	0.0, 4.18, 770.0,
							2.58, 8.06, 770.0));
		mesh.addConstraintEdge(new DEdge (	0.77, 5.33, 770.0,
							1.19, 9.87, 763.0));
		mesh.addConstraintEdge(new DEdge (	0.77, 5.33, 770.0,
							1.45, 4.79, 773.0));
		mesh.addConstraintEdge(new DEdge (	1.19, 9.87, 763.0,
							6.65, 13.48, 763.0));
		mesh.addConstraintEdge(new DEdge (	1.45, 4.79, 773.0,
							3.70, 2.91, 780.0));
		mesh.addConstraintEdge(new DEdge (	1.82, 0.0, 780.0,
							3.20, 1.5, 780.0));
		mesh.addConstraintEdge(new DEdge (	3.20, 1.5, 780.0,
							4.88, 6.25, 780.0));
		mesh.addConstraintEdge(new DEdge (	3.70, 2.91, 780.0,
							5.97, 3.29, 787.0));
		mesh.addConstraintEdge(new DEdge (	5.97, 3.29, 787.0,
							6.89, 5.47, 787.0));
		mesh.addConstraintEdge(new DEdge (	6.45, 2.04, 790.0,
							7.76, 5.12, 790.0));
		mesh.addConstraintEdge(new DEdge (	6.89, 5.47, 787.0,
							9.5 , 8.04, 787.0));
//		show(mesh);
		mesh.forceConstraintIntegrity();
//		show(mesh);
		try{
			mesh.processDelaunay();
			assertTrue(true);
		} catch (DelaunayError d){
			assertFalse(true);
		}
//		show(mesh);
	}

	/**
	 * Process a single encroached DEdge in a mesh. We don't call refineMesh here,
         * we just call splitEncroachedEdge on a specific edge, so this edge will 
         * be the only one to be split, even if the edges of the boundary should be too.
	 * @throws DelaunayError
	 */
	public void testSplitEncroachedEdges() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge e1 = new DEdge(0,3,0,8,3,0);
		mesh.addConstraintEdge(e1);
		mesh.addPoint(new DPoint(3, 0, 0));
		mesh.addPoint(new DPoint(2, 4.5, 0));
		mesh.processDelaunay();
		mesh.splitEncroachedEdge(e1,1);
//		show(mesh);
		assertTrue(mesh.getTriangleList().size()==16);
		assertTrue(mesh.getConstraintEdges().size()==5);
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(0,3,0,2,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(4,3,0,2,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(4,3,0,6,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(7,3,0,6,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(7,3,0,8,3,0)));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(0,3,0,1.5,1.5,0),
						new DEdge(1.5,1.5,0,2,3,0),
						new DEdge(2,3,0,0,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(0,3,0,2,4.5,0),
						new DEdge(2,4.5,0,2,3,0),
						new DEdge(2,3,0,0,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(3.5,4.125,0,2,4.5,0),
						new DEdge(2,4.5,0,2,3,0),
						new DEdge(2,3,0,3.5,4.125,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(3.5,4.125,0,4,3,0),
						new DEdge(4,3,0,2,3,0),
						new DEdge(2,3,0,3.5,4.125,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(3.5,4.125,0,4,3,0),
						new DEdge(4,3,0,5,3.75,0),
						new DEdge(5,3.75,0,3.5,4.125,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,4,3,0),
						new DEdge(4,3,0,5,3.75,0),
						new DEdge(5,3.75,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,6.5,3.375,0),
						new DEdge(6.5,3.375,0,5,3.75,0),
						new DEdge(5,3.75,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,6.5,3.375,0),
						new DEdge(6.5,3.375,0,7,3,0),
						new DEdge(7,3,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(8,3,0,6.5,3.375,0),
						new DEdge(6.5,3.375,0,7,3,0),
						new DEdge(7,3,0,8,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,1.5,1.5,0),
						new DEdge(1.5,1.5,0,2,3,0),
						new DEdge(2,3,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,1.5,1.5,0),
						new DEdge(1.5,1.5,0,3,0,0),
						new DEdge(3,0,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,5.5,1.5,0),
						new DEdge(5.5,1.5,0,3,0,0),
						new DEdge(3,0,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,5.5,1.5,0),
						new DEdge(5.5,1.5,0,6,3,0),
						new DEdge(6,3,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6.75,2.25,0,5.5,1.5,0),
						new DEdge(5.5,1.5,0,6,3,0),
						new DEdge(6,3,0,6.75,2.25,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6.75,2.25,0,7,3,0),
						new DEdge(7,3,0,6,3,0),
						new DEdge(6,3,0,6.75,2.25,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6.75,2.25,0,7,3,0),
						new DEdge(7,3,0,8,3,0),
						new DEdge(8,3,0,6.75,2.25,0))));
		assertTrue(mesh.getEdges().size()==29);
		assertTrue(mesh.getPoints().size()==14);
		assertGIDUnicity(mesh);
                assertCoherence(mesh);
	}
        
        /**
         * We must be sure that we have the good z coordinate when we split an encroached edge.
         * @throws DelaunayError 
         */
        public void testZManagementUponSplit() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge e1 = new DEdge(0,3,0,8,3,4);
		mesh.addConstraintEdge(e1);
		mesh.addPoint(new DPoint(3, 0, 0));
		mesh.addPoint(new DPoint(2, 4.5, 0));
		mesh.processDelaunay();
		mesh.splitEncroachedEdge(e1,2);
                assertTrue(mesh.getPoints().contains(new DPoint(2,3,1)));
                assertTrue(mesh.getPoints().contains(new DPoint(4,3,2)));
                
        }

	/**
	 * Test the removal of an encroached DEdge, with a watershed that will
	 * block a split.
	 * @throws DelaunayError
	 */
	public void testEncroachedThreshold()  throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge e1 = new DEdge(0,3,0,8,3,0);
		mesh.addConstraintEdge(e1);
		mesh.addPoint(new DPoint(3, 0, 0));
		mesh.addPoint(new DPoint(1, 3.5, 0));
		mesh.processDelaunay();
		mesh.splitEncroachedEdge(e1,1.5);
//		show(mesh);
		assertTrue(mesh.getTriangleList().size()==13);
		assertTrue(mesh.getConstraintEdges().size()==4);
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(0,3,0,2,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(4,3,0,2,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(4,3,0,6,3,0)));
		assertTrue(mesh.getConstraintEdges().contains(new DEdge(8,3,0,6,3,0)));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(0,3,0,1.5,1.5,0),
						new DEdge(1.5,1.5,0,2,3,0),
						new DEdge(2,3,0,0,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,1.5,1.5,0),
						new DEdge(1.5,1.5,0,2,3,0),
						new DEdge(2,3,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,1.5,1.5,0),
						new DEdge(1.5,1.5,0,3,0,0),
						new DEdge(3,0,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(4,3,0,5.5,1.5,0),
						new DEdge(5.5,1.5,0,3,0,0),
						new DEdge(3,0,0,4,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,5.5,1.5,0),
						new DEdge(5.5,1.5,0,4,3,0),
						new DEdge(4,3,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,5.5,1.5,0),
						new DEdge(5.5,1.5,0,8,3,0),
						new DEdge(8,3,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,6.25,3.125,0),
						new DEdge(6.25,3.125,0,8,3,0),
						new DEdge(8,3,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,6.25,3.125,0),
						new DEdge(6.25,3.125,0,4.5,3.25,0),
						new DEdge(4.5,3.25,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(6,3,0,4,3,0),
						new DEdge(4,3,0,4.5,3.25,0),
						new DEdge(4.5,3.25,0,6,3,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(2.75,3.375,0,4,3,0),
						new DEdge(4,3,0,4.5,3.25,0),
						new DEdge(4.5,3.25,0,2.75,3.375,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(2.75,3.375,0,4,3,0),
						new DEdge(4,3,0,2,3,0),
						new DEdge(2,3,0,2.75,3.375,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(2.75,3.375,0,1,3.5,0),
						new DEdge(1,3.5,0,2,3,0),
						new DEdge(2,3,0,2.75,3.375,0))));
		assertTrue(mesh.getTriangleList().contains(new DTriangle(
						new DEdge(0,3,0,1,3.5,0),
						new DEdge(1,3.5,0,2,3,0),
						new DEdge(2,3,0,0,3,0))));
		assertTrue(mesh.getEdges().size()==24);
		assertTrue(mesh.getEdges().contains(new DEdge(1,3.5,0,0,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(1,3.5,0,2,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(1,3.5,0,2.75,3.375,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(2,3,0,2.75,3.375,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(4,3,0,2.75,3.375,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(4.5,3.25,0,2.75,3.375,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(4,3,0,4.5,3.25,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6,3,0,4.5,3.25,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6.25,3.125,0,4.5,3.25,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6.25,3.125,0,6,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6.25,3.125,0,8,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6,3,0,8,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6,3,0,4,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(2,3,0,4,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(2,3,0,0,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(2,3,0,1.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(0,3,0,1.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(4,3,0,1.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(3,0,0,1.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(3,0,0,4,3,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(3,0,0,5.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(4,3,0,5.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(6,3,0,5.5,1.5,0)));
		assertTrue(mesh.getEdges().contains(new DEdge(8,3,0,5.5,1.5,0)));
		assertTrue(mesh.getPoints().size()==12);
		assertTrue(mesh.getPoints().contains(new DPoint(3,0,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(1,3.5,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(2,3,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(4,3,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(8,3,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(0,3,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(6,3,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(2.75,3.375,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(4.5,3.25,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(6.25,3.125,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(1.5,1.5,0)));
		assertTrue(mesh.getPoints().contains(new DPoint(5.5,1.5,0)));
		assertGIDUnicity(mesh);
                assertCoherence(mesh);
	}

	public void testRiverAndDitches() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		mesh.addConstraintEdge(new DEdge (345796.89949322917, 6695019.257556161, 52.592440873703026,
			345808.80317515164, 6695018.931396081, 52.37237604556536));
		mesh.addConstraintEdge(new DEdge (345799.8904126589, 6694489.176938893, 45.12430134735259,
			345821.92794979573, 6694496.094938288, 43.901845003633035));
		mesh.addConstraintEdge(new DEdge (345808.8031721221, 6695018.9313959, 52.37237609411774,
			345838.4291617969, 6695015.827422162, 51.84860106986137));
		mesh.addConstraintEdge(new DEdge (345821.9279487509, 6694496.094938227, 43.9018450064769,
			345834.85441884934, 6694494.844559039, 43.92765514350822));
//		mesh.addConstraintEdge(new DEdge (345834.85441884934, 6694494.844559039, 43.92765514350822,
//			345850.3040588013, 6694494.145992298, 43.95839132303459));
//		mesh.addConstraintEdge(new DEdge (345850.3040588013, 6694494.145992298, 43.95839132303459,
//			345870.69678723137, 6694495.925688318, 43.99907413296467));
//		mesh.addConstraintEdge(new DEdge (345870.69678723137, 6694495.925688318, 43.99907413296467,
//			345877.91723171604, 6694496.897077267, 44.01355341402248));
		mesh.forceConstraintIntegrity();
		assertTrue(mesh.getConstraintEdges().size()==6);
		mesh.processDelaunay();
//		show(mesh);
	}
        
        public void testProceedSwaps() throws DelaunayError {
		ConstrainedMesh mesh = new ConstrainedMesh();
		DEdge constr = new DEdge(0,3,0,8,3,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(9,0,0,9,6,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(12,6,0,8,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(5,4,0,8,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(12,6,0,12,7,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(8,3,0,9,6,0);
		mesh.addConstraintEdge(constr);
		constr = new DEdge(8,7,0,12,12,0);
		mesh.addConstraintEdge(constr);
		mesh.addPoint(new DPoint(4,5,0));
		mesh.addPoint(new DPoint(4,1,0));
		mesh.addPoint(new DPoint(10,3,0));
		mesh.addPoint(new DPoint(11,9,0));
		mesh.processDelaunay();
                List<DEdge> edges = mesh.getEdges();
                int i1 = edges.indexOf(new DEdge(8,3,0,8,7,0));
                int i2 = edges.indexOf(new DEdge(8,7,0,12,7,0));
                assertTrue(i1>=0);
                assertTrue(i2>=0);
                LinkedList<DEdge> mem = new LinkedList<DEdge>();
                mem.add(edges.get(i1));
                mem.add(edges.get(i2));
                mesh.proceedSwaps(mem.iterator());
                assertTrue(mesh.getEdges().contains(new DEdge(12,6,0,11,9,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(5,4,0,9,6,0)));
        }
        
        /**
         * Test that when applying twice the same swaps in the same order, we don't
         * come back in the original state.
         * @throws DelaunayError 
         */
        public void testProceedSwapsAndRevert() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addConstraintEdge(new DEdge(0, 3, 0, 4, 1, 0));
                mesh.addConstraintEdge(new DEdge(0, 6, 0, 3, 7, 0));
                mesh.addConstraintEdge(new DEdge(3, 7, 0, 7, 5, 0));
		mesh.processDelaunay();
                List<DEdge> edges = mesh.getEdges();
                int i1 = edges.indexOf(new DEdge(0,3,0,3,7,0));
                int i2 = edges.indexOf(new DEdge(3,7,0,4,1,0));
                assertTrue(i1>=0);
                assertTrue(i2>=0);
                LinkedList<DEdge> mem = new LinkedList<DEdge>();
                mem.add(edges.get(i1));
                mem.add(edges.get(i2));
                mesh.proceedSwaps(mem.iterator());
                mesh.proceedSwaps(mem.iterator());
                assertFalse(mesh.getEdges().contains(new DEdge(0,3,0,3,7,0)) && 
                        mesh.getEdges().contains(new DEdge(3,7,0,4,1,0)));
        }
        
        /**
         * Test that when applying twice the same swaps in the reverse order, we 
         * come back in the original state.
         * @throws DelaunayError 
         */
        public void testProceedSwapsAndRevertBis() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addConstraintEdge(new DEdge(0, 3, 0, 4, 1, 0));
                mesh.addConstraintEdge(new DEdge(0, 6, 0, 3, 7, 0));
                mesh.addConstraintEdge(new DEdge(3, 7, 0, 7, 5, 0));
		mesh.processDelaunay();
                List<DEdge> edges = mesh.getEdges();
                int i1 = edges.indexOf(new DEdge(0,3,0,3,7,0));
                int i2 = edges.indexOf(new DEdge(3,7,0,4,1,0));
                assertTrue(i1>=0);
                assertTrue(i2>=0);
                LinkedList<DEdge> mem = new LinkedList<DEdge>();
                mem.add(edges.get(i1));
                mem.add(edges.get(i2));
                mesh.proceedSwaps(mem.iterator());
                mesh.proceedSwaps(mem.descendingIterator());
                assertTrue(mesh.getEdges().contains(new DEdge(0,3,0,3,7,0)) && 
                        mesh.getEdges().contains(new DEdge(3,7,0,4,1,0)));
        }
        
        public void testPointInTriangleInit() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,0,0));
                mesh.addPoint(new DPoint(6,0,0));
                mesh.addPoint(new DPoint(3,5,0));
                mesh.processDelaunay();
                DTriangle tri = mesh.getTriangleList().get(0);
                DEdge e1 = mesh.getEdges().get(0);
                DEdge e2 = mesh.getEdges().get(1);
                DEdge e3 = mesh.getEdges().get(2);
                DEdge con = mesh.initPointInTriangle(new DPoint(3,2,0), tri, new LinkedList<DEdge>());
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==3);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2,0,0,0,0), 
                        new DEdge(0,0,0,3,5,0), 
                        new DEdge(3,5,0,3,2,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2,0,6,0,0), 
                        new DEdge(6,0,0,3,5,0), 
                        new DEdge(3,5,0,3,2,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2,0,0,0,0), 
                        new DEdge(0,0,0,6,0,0), 
                        new DEdge(6,0,0,3,2,0))));
                assertTrue(tri == tris.get(0) || tri == tris.get(1)||tri == tris.get(2));
                List<DEdge> eds = mesh.getEdges();
                assertTrue(eds.size()==6);
                assertTrue(mesh.getPoints().size()==4);
                assertTrue(con!=null);
                assertCoherence(mesh);
                assertGIDUnicity(mesh);

        }
        
        public void testPointInEdgeInit() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,2,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(7,3,0));
                mesh.addPoint(new DPoint(2,4,0));
                mesh.processDelaunay();
                List<DEdge> edges = mesh.getEdges();
                int index = edges.indexOf(new DEdge(2,4,0,4,0,0));
                DEdge e = edges.get(index);
                DEdge con = mesh.initPointOnEdge(new DPoint(3,2,0), e, new LinkedList<DEdge>());
                assertNotNull(con);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==4);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,2,0,3,2,0),
                        new DEdge(3,2,0,4,0,0),
                        new DEdge(4,0,0,0,2,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,2,0,3,2,0),
                        new DEdge(3,2,0,2,4,0),
                        new DEdge(2,4,0,0,2,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(7,3,0,3,2,0),
                        new DEdge(3,2,0,2,4,0),
                        new DEdge(2,4,0,7,3,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(7,3,0,3,2,0),
                        new DEdge(3,2,0,4,0,0),
                        new DEdge(4,0,0,7,3,0))));
                edges = mesh.getEdges();
                assertTrue(edges.size()==8);
                assertTrue(edges.contains(new DEdge(0,2,0,4,0,0)));
                assertTrue(edges.contains(new DEdge(0,2,0,2,4,0)));
                assertTrue(edges.contains(new DEdge(0,2,0,3,2,0)));
                assertTrue(edges.contains(new DEdge(2,4,0,3,2,0)));
                assertTrue(edges.contains(new DEdge(4,0,0,3,2,0)));
                assertTrue(edges.contains(new DEdge(7,3,0,3,2,0)));
                assertTrue(edges.contains(new DEdge(7,3,0,2,4,0)));
                assertTrue(edges.contains(new DEdge(7,3,0,4,0,0)));
        }
        
        /**
         * For insertion on an edge, we must test branching too.
         */
        public void testPointInEdgeBranches() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,0,0));
                mesh.addPoint(new DPoint(3,0,0));
                mesh.addPoint(new DPoint(2,3,0));
                mesh.processDelaunay();
                List<DEdge> edges = mesh.getEdges();
                int index = edges.indexOf(new DEdge(0,0,0,2,3,0));
                DEdge e = edges.get(index);
                DEdge con = mesh.initPointOnEdge(new DPoint(1,1.5,0), e, new LinkedList<DEdge>());
                assertNotNull(con);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==2);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,0,0,1,1.5,0),
                        new DEdge(1,1.5,0,3,0,0),
                        new DEdge(3,0,0,0,0,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(2,3,0,1,1.5,0),
                        new DEdge(1,1.5,0,3,0,0),
                        new DEdge(3,0,0,2,3,0))));
                edges = mesh.getEdges();
                assertTrue(edges.size()==5);
                mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,0,0));
                mesh.addPoint(new DPoint(3,0,0));
                mesh.addPoint(new DPoint(2,3,0));
                mesh.processDelaunay();
                edges = mesh.getEdges();
                index = edges.indexOf(new DEdge(0,0,0,2,3,0));
                e = edges.get(index);
                e.swap();
                con = mesh.initPointOnEdge(new DPoint(1,1.5,0), e, new LinkedList<DEdge>());
                assertNotNull(con);
                tris = mesh.getTriangleList();
                assertTrue(tris.size()==2);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,0,0,1,1.5,0),
                        new DEdge(1,1.5,0,3,0,0),
                        new DEdge(3,0,0,0,0,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(2,3,0,1,1.5,0),
                        new DEdge(1,1.5,0,3,0,0),
                        new DEdge(3,0,0,2,3,0))));
                edges = mesh.getEdges();
                assertTrue(edges.size()==5);
        }
        
        public void testPointInsertion() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,0,0));
                mesh.addPoint(new DPoint(9,0,0));
                mesh.addPoint(new DPoint(1,2,0));
                mesh.addPoint(new DPoint(6,2,0));
                mesh.addPoint(new DPoint(3,5,0));
                mesh.addPoint(new DPoint(9,5,0));
                mesh.processDelaunay();
                DTriangle tri = new DTriangle(
                        new DEdge(1,2,0,6,2,0), 
                        new DEdge(6,2,0,3,5,0), 
                        new DEdge(3,5,0,1,2,0));
                int index = mesh.getTriangleList().indexOf(tri);
                DPoint cc = new DPoint(tri.getCircumCenter());
                DPoint ccmem = new DPoint(tri.getCircumCenter());
                mesh.insertPointInTriangle(cc, mesh.getTriangleList().get(index), 0.00001);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==7);
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(0,0,0,1,2,0),
                                new DEdge(new DPoint(1,2,0),ccmem),
                                new DEdge(ccmem, new DPoint(0,0,0)))));
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(0,0,0,6,2,0),
                                new DEdge(new DPoint(6,2,0),ccmem),
                                new DEdge(ccmem, new DPoint(0,0,0)))));
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(3,5,0,6,2,0),
                                new DEdge(new DPoint(6,2,0),ccmem),
                                new DEdge(ccmem, new DPoint(3,5,0)))));
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(3,5,0,1,2,0),
                                new DEdge(new DPoint(1,2,0),ccmem),
                                new DEdge(ccmem, new DPoint(3,5,0)))));
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(6,2,0,3,5,0),
                                new DEdge(3,5,0,9,5,0),
                                new DEdge(9,5,0,6,2,0))));
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(9,0,0,6,2,0),
                                new DEdge(6,2,0,9,5,0),
                                new DEdge(9,5,0,9,0,0))));
                assertTrue(tris.contains(
                        new DTriangle(
                                new DEdge(9,0,0,6,2,0),
                                new DEdge(6,2,0,0,0,0),
                                new DEdge(0,0,0,9,0,0))));
                assertCoherence(mesh);
                assertTrue(mesh.getEdges().size()==13);
        }
        
        public void testPointInsertionOnEdge() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,3,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(1,1,0));
                mesh.addPoint(new DPoint(2,5,0));
                mesh.addPoint(new DPoint(5,6,0));
                mesh.addPoint(new DPoint(8,2,0));
                mesh.processDelaunay();
                DTriangle tri = new DTriangle(
                        new DEdge(1,1,0,2,5,0), 
                        new DEdge(2,5,0,4,0,0), 
                        new DEdge(4,0,0,1,1,0));
                int index = mesh.getTriangleList().indexOf(tri);
                DPoint cc = new DPoint(3,2.5,0);
                DPoint ccmem = new DPoint(3,2.5,0);
                mesh.insertPointInTriangle(cc, mesh.getTriangleList().get(index), 0.00001);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==6);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2.5,0,1,1,0), 
                        new DEdge(1,1,0,0,3,0),
                        new DEdge(0,3,0,3,2.5,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2.5,0,2,5,0), 
                        new DEdge(2,5,0,0,3,0),
                        new DEdge(0,3,0,3,2.5,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2.5,0,2,5,0), 
                        new DEdge(2,5,0,5,6,0),
                        new DEdge(5,6,0,3,2.5,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2.5,0,8,2,0), 
                        new DEdge(8,2,0,5,6,0),
                        new DEdge(5,6,0,3,2.5,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2.5,0,8,2,0), 
                        new DEdge(8,2,0,4,0,0),
                        new DEdge(4,0,0,3,2.5,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(3,2.5,0,1,1,0), 
                        new DEdge(1,1,0,4,0,0),
                        new DEdge(4,0,0,3,2.5,0))));
                assertCoherence(mesh);
                assertTrue(mesh.getEdges().size()==12);
        }
        
        public void testRevertInsertionInTriangle() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                DPoint pt1 = new DPoint(0,0,0);
                mesh.addPoint(pt1);
                DPoint pt2 = new DPoint(6,0,0);
                mesh.addPoint(pt2);
                DPoint pt3 = new DPoint(3,5,0);
                mesh.addPoint(pt3);
                mesh.processDelaunay();
                DTriangle tri = mesh.getTriangleList().get(0);
                DEdge e1 = mesh.getEdges().get(0);
                DEdge e2 = mesh.getEdges().get(1);
                DEdge e3 = mesh.getEdges().get(2);
                mesh.initPointInTriangle(new DPoint(3,2,0), tri, new LinkedList<DEdge>());
                DPoint pt;
                //as we can't know which point is not in tri anymore, we search after it.
                if(!tri.belongsTo(pt1)){
                        pt=pt1;
                } else if(!tri.belongsTo(pt2)){
                        pt = pt2;
                } else {
                        pt = pt3;
                }
                //We revert our insertion here.
                mesh.revertPointInTriangleInsertion(tri, new DPoint(3,2,0), pt, e1, e3);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==1);
                assertEquals(new DTriangle(
                        new DEdge(0,0,0,6,0,0),
                        new DEdge(6,0,0,3,5,0),
                        new DEdge(3,5,0,0,0,0)), tris.get(0));
                assertTrue(mesh.getPoints().size()==3);
                assertTrue(mesh.getPoints().contains(new DPoint(0,0,0)));
                assertTrue(mesh.getPoints().contains(new DPoint(6,0,0)));
                assertTrue(mesh.getPoints().contains(new DPoint(3,5,0)));
                assertTrue(mesh.getEdges().size()==3);
                assertTrue(mesh.getEdges().contains(new DEdge(0,0,0,6,0,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(3,5,0,6,0,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(0,0,0,3,5,0)));
                assertTrue(mesh.getPoints().get(0)==pt1);
                assertTrue(mesh.getPoints().get(1)==pt3);
                assertTrue(mesh.getPoints().get(2)==pt2);
                assertCoherence(mesh);
                DTriangle dt = mesh.getTriangleList().get(0);
                DEdge ed = dt.getEdge(0);
                assertTrue(ed.getLeft() == dt || ed.getRight()==dt);
                assertTrue(ed == e1 || ed == e2 || ed == e3);
                ed = dt.getEdge(2);
                assertTrue(ed.getLeft() == dt || ed.getRight()==dt);
                assertTrue(ed == e1 || ed == e2 || ed == e3);
                ed = dt.getEdge(1);
                assertTrue(ed.getLeft() == dt || ed.getRight()==dt);
                assertTrue(ed == e1 || ed == e2 || ed == e3);
                
        } 
        
        /**
         * We insert a point on an edge, and then we revert this insertion.
         * @throws DelaunayError 
         */
        public void testRevertPointInEdgeInit() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                DPoint p1 =new DPoint(0,2,0);
                mesh.addPoint(p1);
                DPoint p2 =new DPoint(4,0,0);
                mesh.addPoint(p2);
                DPoint p3 =new DPoint(7,3,0);
                mesh.addPoint(p3);
                DPoint p4 =new DPoint(2,4,0);
                mesh.addPoint(p4);
                mesh.processDelaunay();
                DTriangle tri1 = mesh.getTriangleList().get(0);
                DTriangle tri2 = mesh.getTriangleList().get(1);
                List<DEdge> edges = mesh.getEdges();
                int index = edges.indexOf(new DEdge(2,4,0,4,0,0));
                DEdge e = edges.get(index);
                DPoint ex;
                if(e.getStartPoint().equals(new DPoint(2,4,0))){
                        ex = p2;
                } else {
                        ex = p4;
                }
                index = edges.indexOf(new DEdge(0,2,0,4,0,0));
                DEdge e1 = edges.get(index);
                index = edges.indexOf(new DEdge(2,4,0,0,2,0));
                DEdge e2 = edges.get(index);
                DEdge ll;
                if(e1.isExtremity(ex)){
                        ll = e1;
                } else {
                        ll = e2;
                }
                index = edges.indexOf(new DEdge(7,3,0,4,0,0));
                DEdge e4 = edges.get(index);
                index = edges.indexOf(new DEdge(2,4,0,7,3,0));
                DEdge e5 = edges.get(index);
                DEdge lr;
                if(e4.isExtremity(ex)){
                        lr = e4;
                } else {
                        lr = e5;
                }
                DEdge lef, rig;
                if(e.getLeft().belongsTo(new DPoint(0,2,0))){
                        lef = ll;
                        rig = lr;
                } else {
                        lef = lr;
                        rig = ll;
                }
                mesh.initPointOnEdge(new DPoint(3,2,0), e, new LinkedList<DEdge>());
                mesh.revertPointOnEdgeInsertion(e, new DPoint(3,2,0), ex, lef, rig);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==2);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,2,0,4,0,0),
                        new DEdge(4,0,0,2,4,0), 
                        new DEdge(2,4,0,0,2,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(7,3,0,4,0,0),
                        new DEdge(4,0,0,2,4,0), 
                        new DEdge(2,4,0,7,3,0))));
                edges = mesh.getEdges();
                assertTrue(edges.size()==5);
                assertTrue(edges.contains(new DEdge(0,2,0,4,0,0)));
                assertTrue(edges.contains(new DEdge(0,2,0,2,4,0)));
                assertTrue(edges.contains(new DEdge(2,4,0,4,0,0)));
                assertTrue(edges.contains(new DEdge(7,3,0,4,0,0)));
                assertTrue(edges.contains(new DEdge(2,4,0,7,3,0)));
                List<DPoint> points = mesh.getPoints();
                assertTrue(points.size()==4);
                assertTrue(points.contains(new DPoint(0,2,0)));
                assertTrue(points.contains(new DPoint(2,4,0)));
                assertTrue(points.contains(new DPoint(4,0,0)));
                assertTrue(points.contains(new DPoint(7,3,0)));
                assertCoherence(mesh);
                for(DEdge edg : mesh.getTriangleList().get(0).getEdges()){
                assertTrue(edg.getLeft() == mesh.getTriangleList().get(0) || 
                        edg.getRight()==mesh.getTriangleList().get(0));    
                }
                for(DEdge edg : mesh.getTriangleList().get(1).getEdges()){
                assertTrue(edg.getLeft() == mesh.getTriangleList().get(1) || 
                        edg.getRight()==mesh.getTriangleList().get(1));    
                }
        }
        
        public void testRevertPointOnEdgeBranches() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                DPoint p1 = new DPoint(0,0,0);
                mesh.addPoint(p1);
                DPoint p2 = new DPoint(3,0,0);
                mesh.addPoint(p2);
                DPoint p3 = new DPoint(2,3,0);
                mesh.addPoint(p3);
                mesh.processDelaunay();
                List<DEdge> edges = mesh.getEdges();
                int index = edges.indexOf(new DEdge(0,0,0,2,3,0));
                DEdge e = edges.get(index);
                //we force the direction
                if(e.getRight() == null){
                        e.swap();
                }
                index = edges.indexOf(new DEdge(3,0,0,2,3,0));
                DEdge last = edges.get(index);
                mesh.initPointOnEdge(new DPoint(1,1.5,0), e, new LinkedList<DEdge>());
                mesh.revertPointOnEdgeInsertion(e, new DPoint(1,1.5,0), p3, null, last);
                assertTrue(mesh.getTriangleList().size()==1);
                assertTrue(mesh.getTriangleList().contains(
                        new DTriangle(
                                new DEdge(0,0,0,3,0,0),
                                new DEdge(3,0,0,2,3,0),
                                new DEdge(2,3,0,0,0,0))));
                assertTrue(mesh.getEdges().size()==3);
                assertTrue(mesh.getEdges().contains(new DEdge(0,0,0,3,0,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(2,3,0,3,0,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(0,0,0,2,3,0)));
                assertTrue(mesh.getPoints().size()==3);
                assertTrue(mesh.getPoints().contains(new DPoint(0,0,0)));
                assertTrue(mesh.getPoints().contains(new DPoint(2,3,0)));
                assertTrue(mesh.getPoints().contains(new DPoint(3,0,0)));
                assertCoherence(mesh);
                mesh = new ConstrainedMesh();
                p1 = new DPoint(0,0,0);
                mesh.addPoint(p1);
                p2 = new DPoint(3,0,0);
                mesh.addPoint(p2);
                p3 = new DPoint(2,3,0);
                mesh.addPoint(p3);
                mesh.processDelaunay();
                edges = mesh.getEdges();
                index = edges.indexOf(new DEdge(0,0,0,2,3,0));
                e = edges.get(index);
                //we force the direction
                if(e.getLeft() == null){
                        e.swap();
                }
                index = edges.indexOf(new DEdge(3,0,0,0,0,0));
                last = edges.get(index);
                mesh.initPointOnEdge(new DPoint(1,1.5,0), e, new LinkedList<DEdge>());
                mesh.revertPointOnEdgeInsertion(e, new DPoint(1,1.5,0), p1, last, null);
                assertTrue(mesh.getTriangleList().size()==1);
                assertTrue(mesh.getTriangleList().contains(
                        new DTriangle(
                                new DEdge(0,0,0,3,0,0),
                                new DEdge(3,0,0,2,3,0),
                                new DEdge(2,3,0,0,0,0))));
                assertTrue(mesh.getEdges().size()==3);
                assertTrue(mesh.getEdges().contains(new DEdge(0,0,0,3,0,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(2,3,0,3,0,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(0,0,0,2,3,0)));
                assertTrue(mesh.getPoints().size()==3);
                assertTrue(mesh.getPoints().contains(new DPoint(0,0,0)));
                assertTrue(mesh.getPoints().contains(new DPoint(2,3,0)));
                assertTrue(mesh.getPoints().contains(new DPoint(3,0,0)));
                assertCoherence(mesh);
        }
        
        public void testRevertibleInsertionOnEdge() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,3,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(1,1,0));
                mesh.addPoint(new DPoint(2,5,0));
                mesh.addPoint(new DPoint(5,6,0));
                mesh.addPoint(new DPoint(6,0,0));
                mesh.processDelaunay();
                DTriangle tri = new DTriangle(
                        new DEdge(1,1,0,2,5,0), 
                        new DEdge(2,5,0,4,0,0), 
                        new DEdge(4,0,0,1,1,0));
                int index = mesh.getTriangleList().indexOf(tri);
                DPoint cc = new DPoint(3,2.5,0);
                DPoint ccmem = new DPoint(3,2.5,0);
                DEdge ret = mesh.insertIfNotEncroached(cc, mesh.getTriangleList().get(index), 0.00001);
                assertNotNull(ret);
                List<DTriangle> tris = mesh.getTriangleList();
                assertTrue(tris.size()==4);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,3,0,1,1,0),
                        new DEdge(1,1,0,2,5,0),
                        new DEdge(2,5,0,0,3,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(4,0,0,1,1,0),
                        new DEdge(1,1,0,2,5,0),
                        new DEdge(2,5,0,4,0,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(4,0,0,5,6,0),
                        new DEdge(5,6,0,2,5,0),
                        new DEdge(2,5,0,4,0,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(4,0,0,5,6,0),
                        new DEdge(5,6,0,6,0,0),
                        new DEdge(6,0,0,4,0,0))));
                
        }
        
        public void testRevertibleInsertionInTriangle() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,4,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(2,7,0));
                mesh.addPoint(new DPoint(5,3,0));
                mesh.addPoint(new DPoint(6,11,0));
                mesh.processDelaunay();
                List<DTriangle> tris = mesh.getTriangleList();
                int index = tris.indexOf(new DTriangle(
                        new DEdge(0,4,0,2,7,0),
                        new DEdge(2,7,0,5,3,0),
                        new DEdge(5,3,0,0,4,0)));
                DTriangle tri = tris.get(index);
                DEdge enc = mesh.insertIfNotEncroached(new DPoint(3,4,0), tri, 0.00001);
                assertNotNull(enc);
                tris = mesh.getTriangleList();
                assertTrue(tris.size()==3);
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(4,0,0,0,4,0), 
                        new DEdge(0,4,0,5,3,0),  
                        new DEdge(5,3,0,4,0,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(0,4,0,2,7,0), 
                        new DEdge(2,7,0,5,3,0),  
                        new DEdge(5,3,0,0,4,0))));
                assertTrue(tris.contains(new DTriangle(
                        new DEdge(6,11,0,2,7,0), 
                        new DEdge(2,7,0,5,3,0),  
                        new DEdge(5,3,0,6,11,0))));
        }
        
        /**
         * Tries to insert a circumcenter with or without test about the creation
         * of encroached edges.
         * @throws DelaunayError 
         */
        public void testCircumCenterInsertion() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,4,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(2,7,0));
                mesh.addPoint(new DPoint(5,3,0));
                mesh.addPoint(new DPoint(6,11,0));
                mesh.processDelaunay();
                List<DTriangle> tris = mesh.getTriangleList();
                int index = tris.indexOf(new DTriangle(
                        new DEdge(0,4,0,2,7,0),
                        new DEdge(2,7,0,5,3,0),
                        new DEdge(5,3,0,0,4,0)));
                DTriangle tri = tris.get(index);
                mesh.insertTriangleCircumCenter(tri, true,0.01);
                assertTrue(mesh.getTriangleList().size()==3);
                mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,4,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(2,7,0));
                mesh.addPoint(new DPoint(5,3,0));
                mesh.addPoint(new DPoint(6,11,0));
                mesh.processDelaunay();
                tris = mesh.getTriangleList();
                index = tris.indexOf(new DTriangle(
                        new DEdge(0,4,0,2,7,0),
                        new DEdge(2,7,0,5,3,0),
                        new DEdge(5,3,0,0,4,0)));
                tri = tris.get(index);
                mesh.insertTriangleCircumCenter(tri, false, 0.01);
                assertTrue(mesh.getTriangleList().size()==5);
                DTriangle dt =new DTriangle(new DEdge(0,4,0,2,7,0),new DEdge(2,7,0,5,3,0),
                                new DEdge(5,3,0,0,4,0));
                DPoint pt = new DPoint(dt.getCircumCenter());
                assertTrue(mesh.getPoints().contains(pt));
                
        }

        public void testLengthThreshold() throws DelaunayError{
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,2,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(2,7,0));
                mesh.processDelaunay();
                DTriangle tri = mesh.getTriangleList().get(0);
                DEdge ret = mesh.insertIfNotEncroached(new DPoint(1,2,0), tri, 0.5);
                assertTrue(mesh.getPoints().size()==3 && ret!=null);
                ret = mesh.insertIfNotEncroached(new DPoint(1,2,0), tri, 2);
                assertTrue(mesh.getPoints().size()==3 && ret==null);
                mesh.insertPointInTriangle(new DPoint(1,2,0), tri, 0.5);
                assertTrue(mesh.getPoints().size()==4);
                mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,2,0));
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(2,7,0));
                mesh.insertPointInTriangle(new DPoint(1,2,0), tri, 2);
                assertTrue(mesh.getPoints().size()==3);                
        }
        
        public void testSplitReferences() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                DEdge ed = new DEdge(0,2,0,8,2,0);
                ed.setLocked(true);
                mesh.addConstraintEdge(ed);
                mesh.addPoint(new DPoint(4,0,0));
                mesh.addPoint(new DPoint(4,4,0));
                mesh.processDelaunay();
                mesh.splitEncroachedEdge(ed, 3);
                assertTrue(mesh.getTriangleList().size()==4);
                int i = mesh.getTriangleList().indexOf(new DTriangle(
                        new DEdge(0,2,0,4,4,0), 
                        new DEdge(4,4,0,4,2,0), 
                        new DEdge(4,2,0,0,2,0)));
                DTriangle dt1 = mesh.getTriangleList().get(i);
                i = mesh.getTriangleList().indexOf(new DTriangle(
                        new DEdge(4,0,0,0,2,0), 
                        new DEdge(0,2,0,4,2,0), 
                        new DEdge(4,2,0,4,0,0)));
                DTriangle dt2 = mesh.getTriangleList().get(i);
                i = mesh.getTriangleList().indexOf(new DTriangle(
                        new DEdge(8,2,0,4,4,0), 
                        new DEdge(4,4,0,4,2,0), 
                        new DEdge(4,2,0,8,2,0)));
                DTriangle dt3 = mesh.getTriangleList().get(i);
                i = mesh.getTriangleList().indexOf(new DTriangle(
                        new DEdge(8,2,0,4,0,0), 
                        new DEdge(4,0,0,4,2,0), 
                        new DEdge(4,2,0,8,2,0)));
                DTriangle dt4 = mesh.getTriangleList().get(i);
                List<DEdge> edges = mesh.getEdges();
                i=edges.indexOf(new DEdge(0,2,0,4,0,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt2 && ed.getRight()==null) ||(ed.getLeft() == null && ed.getRight()==dt2));
                i=edges.indexOf(new DEdge(8,2,0,4,0,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt4 && ed.getRight()==null) ||(ed.getLeft() == null && ed.getRight()==dt4));
                i=edges.indexOf(new DEdge(4,2,0,4,0,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt4 && ed.getRight()==dt2) ||(ed.getLeft() == dt2 && ed.getRight()==dt4));
                i=edges.indexOf(new DEdge(4,2,0,0,2,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt1 && ed.getRight()==dt2) ||(ed.getLeft() == dt2 && ed.getRight()==dt1));
                i=edges.indexOf(new DEdge(4,2,0,8,2,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt3 && ed.getRight()==dt4) ||(ed.getLeft() == dt4 && ed.getRight()==dt3));
                i=edges.indexOf(new DEdge(4,2,0,4,4,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt3 && ed.getRight()==dt1) ||(ed.getLeft() == dt1 && ed.getRight()==dt3));
                i=edges.indexOf(new DEdge(0,2,0,4,4,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt1 && ed.getRight()==null) ||(ed.getLeft() == null && ed.getRight()==dt1));
                i=edges.indexOf(new DEdge(8,2,0,4,4,0));
                ed=edges.get(i);
                assertTrue((ed.getLeft() == dt3 && ed.getRight()==null) ||(ed.getLeft() == null && ed.getRight()==dt3));
        }
        
        public void testConstraintsSound() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(306585,2251427,0));
                DEdge ed = new DEdge (306587.10275431135, 2251422.3385208487, 0.0, 306592.04039404186, 2251422.017777558, 0.0);
                List<Double> ds= new ArrayList<Double>();
                ds.add((ed.getEndPoint().getY()-ed.getStartPoint().getY())/(ed.getEndPoint().getX()-ed.getStartPoint().getX()));
                mesh.addConstraintEdge(ed);
                ed = new DEdge (306587.26591558, 2251423.94937276, 0.0, 306587.65842925466, 2251428.8153878693, 0.0);
                ds.add((ed.getEndPoint().getY()-ed.getStartPoint().getY())/(ed.getEndPoint().getX()-ed.getStartPoint().getX()));
                mesh.addConstraintEdge(ed);
                ed=new DEdge (306587.65842925466, 2251428.8153878693, 0.0, 306588.0509429294, 2251433.681402979, 0.0);
                ds.add((ed.getEndPoint().getY()-ed.getStartPoint().getY())/(ed.getEndPoint().getX()-ed.getStartPoint().getX()));
                mesh.addConstraintEdge(ed);
                ed = new DEdge (306588.4434566041, 2251438.547418088, 0.0, 306588.83597027883, 2251443.4134331974, 0.0);
                ds.add((ed.getEndPoint().getY()-ed.getStartPoint().getY())/(ed.getEndPoint().getX()-ed.getStartPoint().getX()));
                mesh.addConstraintEdge(ed);
                mesh.processDelaunay();
                assertTrue(true);
        }

        public void testFlipFlap() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,2,0));
                mesh.addPoint(new DPoint(6,2,0));
                mesh.addPoint(new DPoint(3,0,0));
                mesh.addPoint(new DPoint(3,4,0));
                mesh.processDelaunay();
                int index = mesh.getEdges().indexOf(new DEdge(3,0,0,3,4,0));
                DEdge ed = mesh.getEdges().get(index);
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(0,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                assertTrue(mesh.getTriangleList().get(index).getGID()==1);
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(6,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                assertTrue(mesh.getTriangleList().get(index).getGID()==2);
                mesh.flipFlap(ed);
                assertFalse(mesh.getEdges().contains(new DEdge(3,0,0,3,4,0)));
                assertTrue(mesh.getEdges().contains(new DEdge(0,2,0,6,2,0)));
                mesh.flipFlap(ed);
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(0,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                assertTrue(mesh.getTriangleList().get(index).getGID()==2);
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(6,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                assertTrue(mesh.getTriangleList().get(index).getGID()==1);
        }
        

        /**
         * We test here that the the flip-flap operation is well reverted when using 
         * revertFlipFlap. We don't check only the geometries, but also the references
         * to the objects. The operation must be transparent for both edges and triangles.
         * @throws DelaunayError 
         */
        public void testFlipFlapBis() throws DelaunayError {
                ConstrainedMesh mesh = new ConstrainedMesh();
                mesh.addPoint(new DPoint(0,2,0));
                mesh.addPoint(new DPoint(6,2,0));
                mesh.addPoint(new DPoint(3,0,0));
                mesh.addPoint(new DPoint(3,4,0));
                mesh.processDelaunay();
                //We retrieve the index of the edge we'll flap and revert. 
                //We make a copy of it, to store its reference more than its geometry.
                int index = mesh.getEdges().indexOf(new DEdge(3,0,0,3,4,0));
                DEdge ed = mesh.getEdges().get(index);
                //We retrieve the references to the 4 other edges. They are not supposed
                //to change. Their geometries neither.
                index = mesh.getEdges().indexOf(new DEdge(3,0,0,0,2,0));
                DEdge ed11 = mesh.getEdges().get(index);
                index = mesh.getEdges().indexOf(new DEdge(0,2,0,3,4,0));
                DEdge ed12 = mesh.getEdges().get(index);
                index = mesh.getEdges().indexOf(new DEdge(3,0,0,6,2,0));
                DEdge ed21 = mesh.getEdges().get(index);
                index = mesh.getEdges().indexOf(new DEdge(6,2,0,3,4,0));
                DEdge ed22 = mesh.getEdges().get(index);
                //We retrieve the references to the two triangles.
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(0,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                DTriangle tri1 = mesh.getTriangleList().get(index);
                assertTrue(mesh.getTriangleList().get(index).getGID()==1);
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(6,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                DTriangle tri2 = mesh.getTriangleList().get(index);
                assertTrue(mesh.getTriangleList().get(index).getGID()==2);
                mesh.flipFlap(ed);
                mesh.revertFlipFlap(ed);
                //We check the references to the triangles
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(0,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                assertTrue(mesh.getTriangleList().get(index)==tri1);
                assertTrue(mesh.getTriangleList().get(index).getGID()==1);
                index = mesh.getTriangleList().indexOf(new DTriangle(new DPoint(6,2,0), new DPoint(3,0,0), new DPoint(3,4,0)));
                assertTrue(mesh.getTriangleList().get(index).getGID()==2);
                assertTrue(mesh.getTriangleList().get(index)==tri2);
                //We check the references to the edges
                index = mesh.getEdges().indexOf(new DEdge(3,0,0,0,2,0));
                assertTrue(ed11 == mesh.getEdges().get(index));
                index = mesh.getEdges().indexOf(new DEdge(0,2,0,3,4,0));
                assertTrue(ed12 == mesh.getEdges().get(index));
                index = mesh.getEdges().indexOf(new DEdge(3,0,0,6,2,0));
                assertTrue(ed21 == mesh.getEdges().get(index));
                index = mesh.getEdges().indexOf(new DEdge(6,2,0,3,4,0));
                assertTrue(ed22 == mesh.getEdges().get(index));
        }

        double[] readDouble(StringTokenizer st, int length) {
            double[] db = new double[length];
            for(int i=0; i<length; i++) {
                db[i] = Double.valueOf(st.nextToken());
            }
            return db;
        }
        private static DPoint getOrCreatePoint(Quadtree tree, Coordinate pt) throws DelaunayError {
            List res = tree.query(new Envelope(pt));
            for(Object found : res) {
                if(found instanceof DPoint) {
                    if(((DPoint) found).getCoordinate().distance(pt) < 1e-12) {
                        return (DPoint) found;
                    }
                }
            }
            DPoint newDpoint = new DPoint(pt);
            tree.insert(new Envelope(pt), newDpoint);
            return newDpoint;
        }
        public void testPrecision() throws Exception {
            ConstrainedMesh mesh = new ConstrainedMesh();
            //mesh.setIncrementalMeshCheck(true);
            List<DEdge> edges = new LinkedList<DEdge>();
            Envelope testEnv = new Envelope(306425, 306575, 2252700, 2253500);
            File eFile = new File(TestConstrainedMesh.class.getResource("dedge.csv").toURI());
            Quadtree ptQuad = new Quadtree();
            if(eFile.exists()) {
                FileReader edFile = new FileReader(eFile);
                try {
                    BufferedReader bufferedReader = new BufferedReader(edFile);
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        StringTokenizer st = new StringTokenizer(line, ",");
                        double[] pts = readDouble(st, 6);
                        Coordinate pt = new Coordinate(pts[0], pts[1], pts[2]);
                        Coordinate pt2 = new Coordinate(pts[3], pts[4], pts[5]);
                        if(testEnv.contains(pt) && testEnv.contains(pt2)) {
                            edges.add(new DEdge(getOrCreatePoint(ptQuad, pt), getOrCreatePoint(ptQuad, pt2)));
                        }
                    }
                } finally {
                    edFile.close();
                }
            }
            mesh.setConstraintEdges(edges);
            mesh.forceConstraintIntegrity();
            mesh.processDelaunay();
            assertTrue(Tools.isMeshPieceWiseLinearComplex(mesh));
        }

}
