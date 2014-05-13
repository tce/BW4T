package nl.tudelft.bw4t.map;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * Stupid replacement for Rectangle2D.Double. The problem with
 * Rectangle2D.Double is that the XML serializer can not handle it, it gets
 * stuck in an infinite recursion if you try to serialize it.
 * 
 * @author W.Pasman
 * 
 */
public class Rectangle implements Serializable {
	private Rectangle2D.Double rectangle = new Rectangle2D.Double();

	public Rectangle() {
	}

	/**
	 * 
	 * @param x
	 *            CENTER x
	 * @param y
	 *            CENTER y
	 * @param w
	 *            width of the rectangle
	 * @param h
	 *            height of the rectangle
	 */
	public Rectangle(double x, double y, double w, double h) {
		setWidth(w);
		setHeight(h);
		setX(x);
		setY(y);
	}

	public double getX() {
		return rectangle.getCenterX();
	}

	public void setX(double x) {
		// can't set center directly..
		rectangle.x = x - getWidth() / 2;
	}

	public double getY() {
		return rectangle.getCenterY();
	}

	public void setY(double y) {
		// can't set center directly..
		rectangle.y = y - getHeight() / 2;
	}

	public double getWidth() {
		return rectangle.getWidth();
	}

	/**
	 * Set new width. Moves the left side such that center remains at same
	 * place.
	 * 
	 * @param w
	 *            the width of the rectangle
	 */
	public void setWidth(double w) {
		double oldwidth = getWidth();
		rectangle.width = w;
		setX(getX() - (w - oldwidth) / 2.);
	}

	public double getHeight() {
		return rectangle.getHeight();
	}

	/**
	 * Set new height.
	 * 
	 * @param h
	 *            the height of the rectangle
	 */
	public void setHeight(double h) {
		double oldheight = getHeight();
		rectangle.height = h;
		setY(getY() - (h - oldheight) / 2.);
	}

	public boolean contains(Point p) {
		return rectangle.contains(p.asPoint2D());
	}

	@Override
	public int hashCode() {
		return rectangle.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rectangle other = (Rectangle) obj;
		if (rectangle == null) {
			if (other.rectangle != null)
				return false;
		} else if (!rectangle.equals(other.rectangle))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return rectangle.toString();
	}

}