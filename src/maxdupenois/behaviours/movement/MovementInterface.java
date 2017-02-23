package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.GameActionException;

public interface MovementInterface {
  void clearDestination();
  void setDestination(MapLocation destination);
  void continueToDestination() throws GameActionException;
  boolean hasReachedDestination();
}
