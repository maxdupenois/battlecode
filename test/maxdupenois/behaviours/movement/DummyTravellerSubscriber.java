package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;

public class DummyTravellerSubscriber implements TravellerEventInterface {
  private MapLocation failedLocation;

  public boolean hasFailedLocation(){
    return failedLocation != null;
  }
  public MapLocation getFailedLocation(){
    return failedLocation;
  }
  public void onReachingDestination(MapLocation location) {}
  public void onFailingToReachDestination(MapLocation location) {
    this.failedLocation = location;
  }
  public void onReachingDestinationNode(MapLocation location) {}
  public void onFailingToReachDestinationNode(MapLocation location) {}
  public void onMapBoundaryFound(MapLocation destination) {}
}
