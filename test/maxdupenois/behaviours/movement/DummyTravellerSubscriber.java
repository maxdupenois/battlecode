package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;

public class DummyTravellerSubscriber implements TravellerEventInterface {
  public void onReachingDestination(MapLocation location) {}
  public void onFailingToReachDestination(MapLocation location) {}
  public void onReachingDestinationNode(MapLocation location) {}
  public void onFailingToReachDestinationNode(MapLocation location) {}
}
