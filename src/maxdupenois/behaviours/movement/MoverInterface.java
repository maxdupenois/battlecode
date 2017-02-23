package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.Direction;

public interface MoverInterface {
  //Movement hooks
  void onReachingDestination(MapLocation location);
  void onFailingToReachDestination(MapLocation location);

  boolean canMove(MapLocation location);
  boolean hasMoved();
  void move(MapLocation location);
  MapLocation getCurrentLocation();
  float getStrideRadius();
}
