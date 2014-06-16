package simulation.main;

import simulation.entity.SpatialEntity;
import simulation.physics.GravitationalPhysics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by mjanes on 6/16/2014.
 */
public class Setup {

    private static double sZDistance = 3000;

    public static List<SpatialEntity> create() {
        List<SpatialEntity> entities = new ArrayList<>();

        entities.addAll(setup(2000, 3000, 1500, 1, 500));

        return entities;
    }


    /**
     *
     * @param numEntities           Number of entities + one central entity, that will be created
     * @param massDistribution      Mass of each entity, other than center, will be between 0 and massDistribution
     * @param radius                Radius of the circle the entities will be uniformly distributed in.
     * @param speedDistribution     A larger number makes the entities go faster, smaller goes slower. 1 is balanced to
     *                              roughly make an entity with speed one half of speedDistribution item orbit the
     *                              center mass according to Kepler's laws of planetary motion. Admittedly that math is
     *                              fuzzy.
     * @param xyTozRatio            Entities will be given a random z value between 0 and radius / xyTozRatio
     *
     * @return All entities
     */
    public static List<SpatialEntity> setup(int numEntities, double massDistribution, double radius, double speedDistribution, double xyTozRatio) {
        List<SpatialEntity> entities = new ArrayList<>();


        sZDistance = 25000; // distance the center object is from the user

        SpatialEntity center = new SpatialEntity(0, 0, sZDistance, 1000000);
        entities.add(center);

        double zDistribution = radius / xyTozRatio;

        // Trying to handle rotation speeds
        // http://www.astronomynotes.com/gravappl/s8.htm
        // https://en.wikipedia.org/wiki/Kepler's_laws_of_planetary_motion
        // v = Sqrt[(G M)/r].

        final double rotationFactor;
        final double modifiedGravitationalConstant = GravitationalPhysics.GRAVITATIONAL_CONSTANT / 3500;
        rotationFactor = speedDistribution * modifiedGravitationalConstant * (center.getMass() + (massDistribution * numEntities / 2));

        long time = System.currentTimeMillis();

        IntStream.range(0, numEntities).
                forEach(i -> {

                    double theta = Math.random() * 2 * Math.PI;
                    double r = radius * Math.sqrt(Math.random());
                    double x = center.getX() + r * Math.cos(theta);
                    double y = center.getY() + r * Math.sin(theta);
                    double z = sZDistance + (Math.random() * zDistribution) - zDistribution / 2;
                    double mass = Math.random() * massDistribution;
                    SpatialEntity newSpatialEntity = new SpatialEntity(x, y, z, mass);

                    double forceX = -y * mass * Math.random() * rotationFactor / center.getDistance(newSpatialEntity);
                    double forceY = x * mass * Math.random() * rotationFactor / center.getDistance(newSpatialEntity);
                    double forceZ = Math.random() * zDistribution / 10;

                    newSpatialEntity.applyForceX(forceX);
                    newSpatialEntity.applyForceY(forceY);
                    newSpatialEntity.applyForceZ(forceZ);
                    entities.add(newSpatialEntity);
                });

        return entities;
    }

}
