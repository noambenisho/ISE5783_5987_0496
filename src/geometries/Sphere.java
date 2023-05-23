package geometries;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.LinkedList;
import java.util.List;
import static java.lang.Math.sqrt;

/**
 * The Sphere class represents a sphere in 3D space.
 *
 * @author Yair and Noam
 */
public class Sphere extends RadialGeometry
{
    /**
     * The center point of the sphere.
     */
    private Point center;

    /**
     * Constructs a new Sphere object with the specified radius.
     *
     * @param radius the radius of the Sphere object.
     */
    public Sphere(double radius, Point _center)
    {
        super(radius);
        center = _center;
    }

    /**
     * Returns the center point of the sphere.
     *
     * @return the center point of the sphere.
     */
    public Point getCenter()
    {
        return center;
    }

    /**
     * Returns the normal vector to the sphere surface at the specified point.
     *
     * @param p the point at which to compute the normal vector.
     * @return the normal vector to the sphere surface at the specified point.
     */
    @Override
    public Vector getNormal(Point p)
    {
        Vector temp = p.subtract(center);

        return temp.normalize();
    }
    
    /**
     * Finds the intersection points of the given ray with this sphere, if any exist.
     *
     * @param ray the ray to find intersections with
     * @return a list of intersection points, or {@code null} if there are no intersections
     */
    @Override
    public List<GeoPoint> findGeoIntersectionsHelper(Ray ray)
    {
        Vector u;
        double tm, th;

        try
        {
            u = center.subtract(ray.getP0()); // if the ray start at the center we go to catch

            tm = u.dotProduct(ray.getDir());

            double d = u.lengthSquared() - tm * tm; // we will use sqrt only once in this function

            if (d >= radius * radius) // there are no intersections
            {
                return null;
            }

            th = sqrt(radius * radius - d);
        }
        catch(IllegalArgumentException e)
        {
            tm = 0;
            th = radius;
        }

        double t1 = tm + th;
        double t2 = tm - th;

        // we take only the positive values because the direction of ray
        if(t1 <= 0 && t2 <= 0)
        {
            return null;
        }

        List<GeoPoint> intersections = new LinkedList<>();

        // might be only 1 intersection
        if (t1 > 0)
        {
            intersections.add(new GeoPoint(this, ray.getPoint(t1)));
        }

        if (t2 > 0)
        {
            intersections.add(new GeoPoint(this, ray.getPoint(t2)));
        }

        return intersections;
    }
}

