package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;

//The most basic form of movement behaviour,
//does noting with hooks, simply uses a traveller
//to head to a location
public strictfp class SimpleMovementBehaviour implements MovementInterface, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;

  public SimpleMovementBehaviour(RobotController robotController){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
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
      MapLocation newLocation = this.
        robotController.
        getLocation().
        add(dir, 100f);
      System.out.println("Heading to "+newLocation.toString());
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
  public void onReachingDestinationNode(MapLocation destination) {}
  public void onFailingToReachDestinationNode(MapLocation destination) {}
}
