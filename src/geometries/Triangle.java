package geometries;

import primitives.Point;

/**
 * The Triangle class represents a triangle polygon in 3D space.
 */
public class Triangle extends Polygon
{
    /**
     * Constructs a new Triangle object with the specified vertices.
     *
     * @param p1 the first vertex of the triangle.
     * @param p2 the second vertex of the triangle.
     * @param p3 the third vertex of the triangle.
     */
    public Triangle(Point p1, Point p2, Point p3)
    {
        super(p1, p2, p3);
    }
}

