package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;

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

  public void move(){
    if(!traveller.hasDestination() || traveller.hasReachedDestination()){
      // Only thing we know is our starting location
      // so pick a random point based on that. We don't
      // know the map bounds the only thing we know
      // is that our coordinates are in it, don't believe
      // we can even guarantee that there's a (0, 0) and hence
      // a bounding box we can use.
      Direction dir = new Direction(
          (float)Math.random() * 2 * (float)Math.PI
          );
      float distance = (float)Math.random() * this.range;
      MapLocation newLocation = this.
        robotController.
        getLocation().
        add(dir, distance);
      traveller.setDestination(newLocation);
    }
    try {
      traveller.continueToDestination();
    } catch (GameActionException ex) {
      System.err.println(ex.getMessage());
      //TODO: Consider where you actually want to catch this
    }
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {}
  public void onFailingToReachDestination(MapLocation destination) {}
  public void onReachingDiversion(MapLocation destination) {}
  public void onDiversion(MapLocation destination) {}
  public void onNeedingToDivert(MapLocation destination) {}
  public void onMapBoundaryFound(MapLocation destination) {}
}
