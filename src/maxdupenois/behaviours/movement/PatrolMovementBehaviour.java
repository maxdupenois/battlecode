package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;

// Try a rectangular patrol
public strictfp class PatrolMovementBehaviour implements MovementInterface, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;
  private Direction[] directions = new Direction[]{
    Direction.NORTH, Direction.EAST,
    Direction.SOUTH, Direction.WEST
  };
  private int directionIndex;
  private MapLocation initialLocation;
  private float range;

  public PatrolMovementBehaviour(RobotController robotController, float range){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
    this.directionIndex = -1;
    this.range = range;
    this.initialLocation = robotController.getLocation();
  }

  private Direction currentDirection(){
    return this.directions[this.directionIndex];
  }

  private void changeDirection(){
    if(this.directionIndex + 1 >= this.directions.length){
      this.directionIndex = 0;
    } else {
      this.directionIndex = this.directionIndex + 1;
    }
  }

  public void move(){
    if(!traveller.hasDestination() || traveller.hasReachedDestination()){
      this.changeDirection();
      MapLocation newLocation = this.
        robotController.
        getLocation().
        add(this.currentDirection(), this.range);
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
  public void onReachingDestinationNode(MapLocation destination) {
    //System.out.println("FAILED TO HIT "+destination.toString());
    //try {
    //  System.out.println("ON MAP "+(robotController.onTheMap(destination) ? "Yes" : "NO!"));
    //} catch(GameActionException ex){
    //  System.out.println("----> Out of sensor range "+ex.getMessage());
    //}
  }
  public void onFailingToReachDestinationNode(MapLocation destination) {}
}
