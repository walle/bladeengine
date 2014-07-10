/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.bladecoder.bladeengine.polygonalpathfinder;

import java.util.ArrayList;

import org.bladecoder.bladeengine.assets.EngineAssetManager;
import org.bladecoder.bladeengine.pathfinder.AStarPathFinder;
import org.bladecoder.bladeengine.pathfinder.NavContext;
import org.bladecoder.bladeengine.pathfinder.NavGraph;
import org.bladecoder.bladeengine.pathfinder.PathFinder;
import org.bladecoder.bladeengine.util.EngineLogger;
import org.bladecoder.bladeengine.util.PolygonUtils;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Finds the shortest path between 2 points in a world defined by a walkzone and
 * several obstacles.
 * 
 * @author rgarcia
 */
public class PolygonalNavGraph implements NavGraph<NavNodePolygonal>, Serializable{
	private static final Vector2 tmp = new Vector2();
	private static final Vector2 tmp2 = new Vector2();

	private Polygon walkZone;
	private ArrayList<Polygon> obstacles = new ArrayList<Polygon>();
	private ArrayList<Polygon> dinamicObstacles = new ArrayList<Polygon>();

	final private PathFinder pathfinder = new AStarPathFinder(this, 100,
			new ManhattanDistance());
	final private NavPathPolygonal resultPath = new NavPathPolygonal();
	final private NavNodePolygonal startNode = new NavNodePolygonal();
	final private NavNodePolygonal targetNode = new NavNodePolygonal();
	final private ArrayList<NavNodePolygonal> graphNodes = new ArrayList<NavNodePolygonal>();

	public ArrayList<Vector2> findPath(float sx, float sy, float tx, float ty) {
		resultPath.clear();

		Vector2 source = new Vector2(sx, sy);
		Vector2 target = new Vector2(tx, ty);

		// 1. First verify if both the start and target points of the path are
		// inside the polygon. If the end point is outside the polygon clamp it
		// back inside.
		if (!PolygonUtils.isPointInside(walkZone, sx, sy, true)) {
			EngineLogger.debug("PolygonalPathFinder: Source not in polygon!");
			return resultPath.getPath();
		}

		if (!PolygonUtils.isPointInside(walkZone, tx, ty, true)) {
			PolygonUtils.getClampedPointInside(walkZone, tx, ty, target);

			// if(!PolygonUtils.isPointInside(walkZone, tx, ty, true)) {
			// EngineLogger.debug("PolygonalPathFinder: CLAMPED FAILED!!");
			// return resultPath;
			// }
		}

		// 2. Then start by checking if both points are in line-of-sight. If
		// they are, there’s no need for pathfinding, just walk there!
		if (inLineOfSight(source.x, source.y, target.x, target.y)) {
			EngineLogger.debug("PolygonalPathFinder: Direct path found");

			resultPath.getPath().add(source);
			resultPath.getPath().add(target);

			return resultPath.getPath();
		}

		// 3. Otherwise, add the start and end points of your path as new
		// temporary nodes to the graph.
		// AND Connect them to every other node that they can see on the graph.
		addStartEndNodes(source.x, source.y, target.x, target.y);

		// 5. Run your A* implementation on the graph to get your path. This
		// path is guaranteed to be as direct as possible!
		pathfinder.findPath(null, startNode, targetNode, resultPath);

		return resultPath.getPath();
	}

	public void createInitialGraph() {
		graphNodes.clear();

		// 1.- Add WalkZone convex nodes
		float verts[] = walkZone.getTransformedVertices();

		for (int i = 0; i < verts.length; i += 2) {
			if (!PolygonUtils.isVertexConcave(walkZone, i)) {
				graphNodes.add(new NavNodePolygonal(verts[i], verts[i + 1]));
			}
		}

		// 2.- Add obstacle concave nodes
		for (Polygon o : obstacles) {
			verts = o.getTransformedVertices();

			for (int i = 0; i < verts.length; i += 2) {
				if (PolygonUtils.isVertexConcave(o, i)
						&& PolygonUtils.isPointInside(walkZone, verts[i],
								verts[i + 1], false)) {
					graphNodes
							.add(new NavNodePolygonal(verts[i], verts[i + 1]));
				}
			}
		}

		// 3.- CALC LINE OF SIGHTs
		for (int i = 0; i < graphNodes.size() - 1; i++) {
			NavNodePolygonal n1 = graphNodes.get(i);

			for (int j = i + 1; j < graphNodes.size(); j++) {
				NavNodePolygonal n2 = graphNodes.get(j);

				if (inLineOfSight(n1.x, n1.y, n2.x, n2.y)) {
					n1.neighbors.add(n2);
					n2.neighbors.add(n1);
				}
			}
		}
		
		// 4.- ADD DINAMIC OBSTACLES
		for(Polygon p:dinamicObstacles)
			addObstacleToGrapth(p);
	}

	private boolean inLineOfSight(float p1X, float p1Y, float p2X, float p2Y) {

		tmp.set(p1X, p1Y);
		tmp2.set(p2X, p2Y);

		if (!PolygonUtils.inLineOfSight(tmp, tmp2, walkZone, false)) {
			return false;
		}

		for (Polygon o : obstacles) {
			if (!PolygonUtils.inLineOfSight(tmp, tmp2, o, true)) {
				return false;
			}
		}
		
		for (Polygon o : dinamicObstacles) {
			if (!PolygonUtils.inLineOfSight(tmp, tmp2, o, true)) {
				return false;
			}
		}

		return true;
	}

	private void addStartEndNodes(float sx, float sy, float tx, float ty) {
		startNode.x = sx;
		startNode.y = sy;
		targetNode.x = tx;
		targetNode.y = ty;

		startNode.neighbors.clear();

		for (NavNodePolygonal n : graphNodes) {

			n.neighbors.removeValue(targetNode, true);

			if (inLineOfSight(startNode.x, startNode.y, n.x, n.y)) {
				startNode.neighbors.add(n);
			}

			if (inLineOfSight(targetNode.x, targetNode.y, n.x, n.y)) {
				n.neighbors.add(targetNode);
			}
		}

	}

	public Polygon getWalkZone() {
		return walkZone;
	}

	public void setWalkZone(Polygon walkZone) {
		this.walkZone = walkZone;
	}

	public void addObstacle(Polygon obstacle) {
		obstacles.add(obstacle);
	}

	public ArrayList<Polygon> getObstacles() {
		return obstacles;
	}
	
	public ArrayList<NavNodePolygonal> getGraphNodes() {
		return graphNodes;
	}

	@Override
	public boolean blocked(NavContext<NavNodePolygonal> context,
			NavNodePolygonal targetNode) {
		return false;
	}

	@Override
	public float getCost(NavContext<NavNodePolygonal> context,
			NavNodePolygonal targetNode) {
		return 1;
	}
	
	private void addObstacleToGrapth(Polygon poly) {
		float verts[] = poly.getTransformedVertices();
		for (int i = 0; i < verts.length; i += 2) {
			if (PolygonUtils.isVertexConcave(poly, i)
					&& PolygonUtils.isPointInside(walkZone, verts[i],
							verts[i + 1], false)) {
				NavNodePolygonal n1 = new NavNodePolygonal(verts[i], verts[i + 1]);
				
				for (int j = 0; j < graphNodes.size(); j++) {
					NavNodePolygonal n2 = graphNodes.get(j);

					if (inLineOfSight(n1.x, n1.y, n2.x, n2.y)) {
						n1.neighbors.add(n2);
						n2.neighbors.add(n1);
					}
				}
				
				graphNodes.add(n1);
			}
		}		
	}

	public void addDinamicObstacle(Polygon poly) {
		dinamicObstacles.add(poly);

		addObstacleToGrapth(poly);
	}
	
	public void removeDinamicObstacle(Polygon poly) {
		float verts[] = poly.getTransformedVertices();
		dinamicObstacles.remove(poly);

		for (int i = 0; i < verts.length; i += 2) {
			if (PolygonUtils.isVertexConcave(poly, i)
					&& PolygonUtils.isPointInside(walkZone, verts[i],
							verts[i + 1], false)) {
				for (int j = 0; j < graphNodes.size(); j++) {
					NavNodePolygonal n = graphNodes.get(j);

					if (n.x == verts[i] && n.y == verts[i+1]) {
						graphNodes.remove(n);
						j--;
						
						for(NavNodePolygonal n2:graphNodes) {
							n2.neighbors.removeValue(n, true);
						}
						
					}
				}
			}
		}
	}
	
	@Override
	public void write(Json json) {
		Polygon p = new Polygon(walkZone.getVertices());
		p.setPosition(walkZone.getX()/walkZone.getScaleX(), walkZone.getY()/walkZone.getScaleY());
		json.writeValue("walkZone", p);
		
		ArrayList<Polygon> tmp = new ArrayList<Polygon>();
		
		for(Polygon poly:obstacles) {
			// To SAVE space not writing worldVertices
			p = new Polygon(poly.getVertices());
			p.setPosition(poly.getX()/poly.getScaleX(), poly.getY()/poly.getScaleY());
			tmp.add(p);
		}
		
		json.writeValue("obstacles", tmp, ArrayList.class, Polygon.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {
		walkZone = json.readValue("walkZone", Polygon.class, jsonData);
		obstacles = json.readValue("obstacles", ArrayList.class, Polygon.class,
				jsonData);
		
		for(Polygon poly:obstacles) {
			poly.setScale(EngineAssetManager.getInstance().getScale(), EngineAssetManager.getInstance().getScale());
			poly.setPosition(poly.getX() * EngineAssetManager.getInstance().getScale() , 
					poly.getY() * EngineAssetManager.getInstance().getScale());
		}
	}
}
