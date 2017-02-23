package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.Direction;

public interface MoverInterface {
  //Movement Event hooks
  void onReachingDestination(MapLocation location);
  void onFailingToReachDestination(MapLocation location);
  // used when we're part way through a movement
  void onReachingDestinationNode(MapLocation location);
  void onFailingToReachDestinationNode(MapLocation location);


  boolean canMove(MapLocation location);
  boolean hasMoved();
  void move(MapLocation location);
  MapLocation getCurrentLocation();
  float getStrideRadius();
}
