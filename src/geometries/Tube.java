package geometries;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * The Tube class represents a tube in 3D space.
 *
 * @author Yair and Noam
 */
public class Tube extends RadialGeometry
{
	/**
	 * The axis ray of the tube.
	 */
	protected Ray axisRay;
	
	/**
	 * Constructs a new Tube object with the specified radius and axis ray.
	 *
	 * @param radius the radius of the Tube object.
	 * @param axisRay the axis ray of the Tube object.
	 */
	public Tube(double radius, Ray axisRay)
	{
		super(radius);
		this.axisRay = new Ray(axisRay.getP0(), axisRay.getDir());
	}
	
	/**
	 * Returns the axis ray of the tube.
	 *
	 * @return the axis ray of the tube.
	 */
	public Ray getAxisRay()
	{
		return axisRay;
	}
	
	/**
	 * Returns the normal vector to the tube surface at the specified point.
	 *
	 * @param p the point at which to compute the normal vector.
	 * @return the normal vector to the tube surface at the specified point.
	 */
	@Override
	public Vector getNormal(Point p)
	{
		Point p0 = axisRay.getP0();
		Vector v0 = axisRay.getDir();
		
		Vector v = p.subtract(p0);
		double t = v.dotProduct(v0);
		
		// on the round surface
		Point o = axisRay.getP0();
		
		try
		{
			v0 = v0.scale(t);
			o = o.add(v0);
		}
		catch (IllegalArgumentException e) // in case of t == 0
		{ }
		
		Vector normal = p.subtract(o);
		
		return normal.normalize();
	}
	
	/**
	 * Creates the bounding box for the intersectable object.
	 * This method must be implemented by the subclasses to define the specific bounding box.
	 */
	@Override
	protected void createBox()
	{
		box.minX = Double.NEGATIVE_INFINITY;
		box.minY = Double.NEGATIVE_INFINITY;
		box.minZ = Double.NEGATIVE_INFINITY;
		
		box.maxX = Double.POSITIVE_INFINITY;
		box.maxY = Double.POSITIVE_INFINITY;
		box.maxZ = Double.POSITIVE_INFINITY;
	}
	
	/**
	 * Helper method to find the geometric intersections of a ray with the shape.
	 *
	 * @param ray the ray to intersect with the shape
	 * @param maxDistance the maximum distance for intersection
	 * @return null indicating no intersections
	 */
	@Override
	protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance)
	{
		return null;
	}
}

