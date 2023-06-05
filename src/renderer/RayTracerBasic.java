package renderer;

import geometries.Intersectable.GeoPoint;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * The RayTracerBasic class is an implementation of the RayTracerBase abstract class
 * that provides a basic ray tracing algorithm.
 */
public class RayTracerBasic extends RayTracerBase
{
	/**
	 * The maximum recursion level for color calculation.
	 */
	private static final int MAX_CALC_COLOR_LEVEL = 10;
	
	/**
	 * The minimum threshold for the reflection and transmission coefficients during color calculation.
	 */
	private static final double MIN_CALC_COLOR_K = 0.001;
	
	/**
	 * The initial value for the reflection and transmission coefficients during color calculation.
	 */
	private static final Double3 INITIAL_K = Double3.ONE;
	
	/**
	 * Constructs a RayTracerBasic object with the specified scene.
	 *
	 * @param s The scene to be rendered.
	 */
	public RayTracerBasic(Scene s)
	{
		super(s);
	}
	
	/**
	 * Traces a ray using the basic ray tracing algorithm.
	 *
	 * @param ray The ray to be traced.
	 * @return The color at the intersection point.
	 */
	@Override
	public Color traceRay(Ray ray)
	{
		GeoPoint closestPoint = findClosestIntersection(ray);
		
		return closestPoint == null ?
			   scene.background
									:
			   calcColor(closestPoint, ray);
	}
	
	/**
	 * Calculates the color at the intersection point {@code gp} along the ray {@code ray}.
	 * This method uses recursive ray tracing with reflection and transmission.
	 *
	 * @param gp  The intersection point
	 * @param ray The ray
	 * @return The color at the intersection point
	 */
	private Color calcColor(GeoPoint gp, Ray ray)
	{
		return calcColor(gp, ray, MAX_CALC_COLOR_LEVEL, INITIAL_K)
				.add(scene.ambientLight.getIntensity());
	}
	
	/**
	 * Calculates the color at the intersection point {@code gp} along the ray {@code ray}.
	 * This method incorporates local effects and global effects such as reflection and transmission.
	 *
	 * @param gp    The intersection point
	 * @param ray   The ray
	 * @param level The current recursion level
	 * @param k     The accumulated attenuation factor
	 * @return The color at the intersection point
	 */
	private Color calcColor(GeoPoint gp, Ray ray, int level, Double3 k)
	{
		Color color = calcLocalEffects(gp, ray, k);
		return level == 1 ?
			   color :
			   color.add(calcGlobalEffects(gp, ray, level, k));
	}
	
	/**
	 * Calculates the global effects (reflection and transmission) at the intersection point {@code gp}
	 * along the ray {@code ray} for a given recursion {@code level} and attenuation factor {@code k}.
	 *
	 * @param gp    The intersection point
	 * @param ray   The ray
	 * @param level The current recursion level
	 * @param k     The accumulated attenuation factor
	 * @return The color contribution from global effects
	 */
	private Color calcGlobalEffects(GeoPoint gp, Ray ray, int level, Double3 k)
	{
		Vector v = ray.getDir();
		Vector n = gp.geometry.getNormal(gp.point);
		Material material = gp.geometry.getMaterial();
		return calcGlobalEffect(constructReflectedRay(gp, v, n), level, k, material.kR)
				.add(calcGlobalEffect(constructRefractedRay(gp, v, n), level, k, material.kT));
	}
	
	/**
	 * Calculates the color contribution from a specific global effect (reflection or transmission)
	 * along the ray {@code ray} for a given recursion {@code level}, attenuation factor {@code k},
	 * and reflection/transmission coefficient {@code kx}.
	 *
	 * @param ray   The ray
	 * @param level The current recursion level
	 * @param k     The accumulated attenuation factor
	 * @param kx    The reflection/transmission coefficient
	 * @return The color contribution from the global effect
	 */
	private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx)
	{
		Double3 kkx = k.product(kx);
		if (kkx.lowerThan(MIN_CALC_COLOR_K))
		{return Color.BLACK;}
		
		GeoPoint gp = findClosestIntersection(ray);
		if (gp == null)
		{return scene.background.scale(kx);}
		
		return isZero(gp.geometry.getNormal(gp.point)
							  .dotProduct(ray.getDir())) ?
			   Color.BLACK :
			   calcColor(gp, ray, level - 1, kkx).scale(kx);
	}
	
	/**
	 * Constructs a reflected ray at the given intersection point {@code gp} with the given
	 * incident ray direction {@code v} and surface normal {@code n}.
	 *
	 * @param gp The intersection point
	 * @param v  The incident ray direction
	 * @param n  The surface normal
	 * @return The reflected ray
	 */
	private Ray constructReflectedRay(GeoPoint gp, Vector v, Vector n)
	{
		double vn = alignZero(v.dotProduct(n));
		
		Vector temp = v.subtract(n.scale(2 * vn));
		
		return new Ray(gp.point, temp, n);
	}
	
	/**
	 * Constructs a refracted ray at the given intersection point {@code gp} with the given
	 * incident ray direction {@code v} and surface normal {@code n}.
	 *
	 * @param gp The intersection point
	 * @param v  The incident ray direction
	 * @param n  The surface normal
	 * @return The refracted ray
	 */
	private Ray constructRefractedRay(GeoPoint gp, Vector v, Vector n)
	{
		return new Ray(gp.point, v, n);
	}
	
	/**
	 * Finds the closest intersection point between the given ray and the geometries in the scene.
	 *
	 * @param ray The ray to intersect with the geometries
	 * @return The closest intersection point, or null if no intersection is found
	 */
	private GeoPoint findClosestIntersection(Ray ray)
	{
		List<GeoPoint> intersections = scene.geometries.findGeoIntersections(ray);
		if (intersections == null)
		{
			return null;
		}
		
		return ray.findClosestGeoPoint(intersections);
	}
	
	
	/**
	 * Calculates the local effects (diffuse and specular) at a given intersection point.
	 *
	 * @param gp  The intersection point
	 * @param ray The ray that intersected the geometry
	 * @param k   The attenuation factor
	 * @return The color representing the local effects at the intersection point
	 */
	private Color calcLocalEffects(GeoPoint gp, Ray ray, Double3 k)
	{
		Color color = gp.geometry.getEmission();
		
		Vector v = ray.getDir();
		Vector n = gp.geometry.getNormal(gp.point);
		double nv = alignZero(n.dotProduct(v));
		
		if (nv == 0)
		{return color;}
		
		Material material = gp.geometry.getMaterial();
		for (LightSource lightSource : scene.lights)
		{
			Vector l = lightSource.getL(gp.point);
			double nl = alignZero(n.dotProduct(l));
			
			if (nl * nv > 0)
			{ // sign(nl) == sign(nv)
				if (unshaded(gp, lightSource, l, n))
				{
					Color iL = lightSource.getIntensity(gp.point);
					color = color.add(
							iL.scale(calcDiffusive(material, nl)),
							iL.scale(calcSpecular(material, n, l, nl, v)));
				}
			}
		}
		
		return color;
	}
	
	/**
	 * Calculates the specular reflection at a given intersection point.
	 *
	 * @param material The material of the intersected geometry
	 * @param n        The surface normal at the intersection point
	 * @param l        The direction from the intersection point to the light source
	 * @param nl       The dot product between the surface normal and the light direction
	 * @param v        The view direction (direction of the ray)
	 * @return The specular reflection color
	 */
	private Double3 calcSpecular(Material material, Vector n, Vector l, double nl, Vector v)
	{
		Vector r = l.add(n.scale(-2 * nl));
		double minusVR = -alignZero(r.dotProduct(v));
		if (minusVR <= 0)
		{
			return Double3.ZERO; // view from direction opposite to r vector
		}
		
		return material.kS.scale(Math.pow(minusVR, material.nShininess));
	}
	
	/**
	 * Calculates the diffuse reflection at a given intersection point.
	 *
	 * @param material The material of the intersected geometry
	 * @param nl       The dot product between the surface normal and the light direction
	 * @return The diffuse reflection color
	 */
	private Double3 calcDiffusive(Material material, double nl)
	{
		return material.kD.scale(nl >= 0 ?
								 nl :
								 (-1) * nl);
	}
	
	/**
	 * Checks if a point on a surface is unshaded by shadow rays.
	 *
	 * @param gp    The intersection point on the surface
	 * @param light The light source
	 * @param l     The direction from the intersection point to the light source
	 * @param n     The surface normal at the intersection point
	 * @return {@code true} if the point is unshaded, {@code false} otherwise
	 */
	private boolean unshaded(GeoPoint gp, LightSource light, Vector l, Vector n)
	{
		Vector lightDirection = l.scale(-1); // shadow ray
		
		Ray lightRay = new Ray(gp.point, lightDirection, n);
		
		double distance = light.getDistance(lightRay.getP0());
		List<GeoPoint> intersections = scene.geometries.findGeoIntersections(lightRay, distance);
		
		if (intersections == null)
		{return true;}
		
		for (GeoPoint intersection : intersections)
		{
			if (Double3.ZERO.equals(intersection.geometry.getMaterial().kT))
			{return false;}
		}
		
		return true;
	}
}
