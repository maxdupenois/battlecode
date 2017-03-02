package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;
import static maxdupenois.util.GeometryUtil.*;

//The most basic form of movement behaviour,
//does noting with hooks, simply uses a traveller
//to head to a location
public strictfp class SimpleRandomMovementBehaviour implements MovementInterface, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;
  private float range;

  public SimpleRandomMovementBehaviour(RobotController robotController, float range){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
    this.range =range;
  }

  public void move() throws GameActionException{
    if(!traveller.hasDestination() || traveller.hasReachedDestination()){
      // Only thing we know is our starting location
      // so pick a random point based on that. We don't
      // know the map bounds the only thing we know
      // is that our coordinates are in it, don't believe
      // we can even guarantee that there's a (0, 0) and hence
      // a bounding box we can use.
      MapLocation newLocation = randomDestination(
          this.robotController.getLocation(),
          this.range
          );
      traveller.setDestination(newLocation);
    }
    traveller.continueToDestination();
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {}
  public void onFailingToReachDestination(MapLocation destination) {}
  public void onReachingDiversion(MapLocation destination) {}
  public void onDiversion(MapLocation destination) {}
  public void onNeedingToDivert(MapLocation destination) {}
  public void onMapBoundaryFound(MapLocation destination) {}
}
