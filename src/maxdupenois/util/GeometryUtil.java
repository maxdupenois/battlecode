package maxdupenois.util;

import battlecode.common.BodyInfo;
import battlecode.common.MapLocation;
import battlecode.common.Direction;

import java.util.Arrays;

public class GeometryUtil {

  public static boolean isCollisionLikely(MapLocation myLocation, MapLocation objectLocation, Direction objectDirection, float objectRange, float collisionRange){
    MapVector vector = new MapVector(objectLocation, objectDirection, objectRange);
    return vector.isWithinRangeOfVector(myLocation, collisionRange);
  }

  public static MapLocation meanLocation(BodyInfo[] bodies){
    float meanX = 0f;
    float meanY = 0f;
    MapLocation loc;
    for(int b = 0; b < bodies.length; b++){
      loc = bodies[b].getLocation();
      meanX += loc.x;
      meanY += loc.y;
    }
    return new MapLocation(
        meanX/bodies.length, meanY/bodies.length
        );
  }
  public static MapLocation randomDestination(MapLocation currentLocation, float range){
    float distance = (float)Math.random() * range;
    return currentLocation.add(randomDirection(), distance);
  }

  public static Direction modifyDirection(Direction original, Direction target, float amount){
    float origRadians = original.radians;
    float targetRadians = target.radians;
    //If amount == 1 then we get all of target none of original
    //if amount == 0 then all of original, none of target
    //where A = amount
    //f(o, t) = (1 - A)o + At
    //        = o - Ao + At
    //        = o - A(o + t)
    float modifiedRadians = origRadians - amount * (origRadians + targetRadians);
    return new Direction(modifiedRadians);
  }

  public static Direction randomDirection(){
    return new Direction(
        (float)Math.random() * 2 * (float)Math.PI
        );
  }

  public static Direction meanDirection(Direction[] dirs){
    if(dirs.length == 0) return new Direction(0f);
    float radianSum = Arrays.
      stream(dirs).
      map(d -> d.radians).
      <Float>collect(
          () -> new Float(0),
          (Float sum, Float radians) -> new Float(sum.floatValue() + radians.floatValue()),
          (Float sum, Float otherSum) -> {}
          ).floatValue();
    return new Direction(0f).
      rotateRightDegrees(radianSum/(float)dirs.length);
  }

  public static void debug_printDirections(Direction[] dirs){
    StringBuffer b = new StringBuffer("DIRECTIONS: [");
    for(int d = 0; d < dirs.length; d++){
      b.append((dirs[d] == null ? "null" : dirs[d].radians));
      b.append(", ");
    }
    b.append("]");
    System.out.println(b.toString());
  }

}
