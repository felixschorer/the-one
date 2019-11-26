/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package core;

import java.util.Objects;

/**
 * Class to hold 2D coordinates and perform simple arithmetics and
 * transformations
 */
public class Coord implements Cloneable, Comparable<Coord> {
	private double x;
	private double y;

	private double xRenderOffset;
	private double yRenderOffset;

	/**
	 * Constructor.
	 * @param x Initial X-coordinate
	 * @param y Initial Y-coordinate
	 */
	public Coord(double x, double y) {
		this(x, y, 0, 0);
	}

	public Coord(double x, double y, double xRenderOffset, double yRenderOffset) {
		setLocation(x,y);
		setDisplayOffset(xRenderOffset, yRenderOffset);
	}

	/**
	 * Sets the location of this coordinate object
	 * @param x The x coordinate to set
	 * @param y The y coordinate to set
	 */
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void setDisplayOffset(double x, double y) {
		xRenderOffset = x;
		yRenderOffset = y;
	}

	/**
	 * Sets this coordinate's location to be equal to other
	 * coordinates location
	 * @param c The other coordinate
	 */
	public void setLocation(Coord c) {
		this.x = c.x;
		this.y = c.y;
	}

	/**
	 * Moves the point by dx and dy
	 * @param dx How much to move the point in X-direction
	 * @param dy How much to move the point in Y-direction
	 */
	public void translate(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}

	/**
	 * Returns the distance to another coordinate
	 * @param other The other coordinate
	 * @return The distance between this and another coordinate
	 */
	public double distance(Coord other) {
		double dx = this.x - other.x;
		double dy = this.y - other.y;

		return Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * Returns the x coordinate
	 * @return x coordinate
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Returns the y coordinate
	 * @return y coordinate
	 */
	public double getY() {
		return this.y;
	}

	public double getDisplayX() {
		return x + xRenderOffset;
	}

	public double getDisplayY() {
		return y + yRenderOffset;
	}

	/**
	 * Returns a text representation of the coordinate (rounded to 2 decimals)
	 * @return a text representation of the coordinate
	 */
	public String toString() {
		return String.format("(%.2f,%.2f)",x,y);
	}

	/**
	 * Returns a clone of this coordinate
	 */
	public Coord clone() {
		Coord clone = null;
		try {
			clone = (Coord) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return clone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Coord coord = (Coord) o;
		return Double.compare(coord.x, x) == 0 &&
				Double.compare(coord.y, y) == 0 &&
				Double.compare(coord.xRenderOffset, xRenderOffset) == 0 &&
				Double.compare(coord.yRenderOffset, yRenderOffset) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, xRenderOffset, yRenderOffset);
	}

	/**
	 * Compares this coordinate to other coordinate. Coordinate whose y
	 * value is smaller comes first and if y values are equal, the one with
	 * smaller x value comes first.
	 * @return -1, 0 or 1 if this node is before, in the same place or
	 * after the other coordinate
	 */
	public int compareTo(Coord other) {
		if (this.y < other.y) {
			return -1;
		}
		else if (this.y > other.y) {
			return 1;
		}
		else if (this.x < other.x) {
			return -1;
		}
		else if (this.x > other.x) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
