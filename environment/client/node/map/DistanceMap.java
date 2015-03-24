package client.node.map;

import java.awt.Point;

public abstract class DistanceMap {

	public abstract double distance(Point a, Point b);
	public abstract int distance(int rowFrom, int colFrom, int rowTo, int colTo);

}