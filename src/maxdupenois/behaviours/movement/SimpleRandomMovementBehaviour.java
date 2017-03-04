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
  private Direction currentDirection;

  public SimpleRandomMovementBehaviour(RobotController robotController, float range){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
    this.range =range;
    this.currentDirection = randomDirection();
    traveller.setDestination(randomDestination());
  }

  private MapLocation randomDestination(){
    // Ensure distance is at least half our range
    float distance = ((float)Math.random() * range/2f) + range/2f;
    return robotController.getLocation().add(
        currentDirection, distance);
  }

  public void move() throws GameActionException{
    traveller.continueToDestination();
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {
    this.currentDirection = randomDirection();
    traveller.setDestination(randomDestination());
  }

  public void onFailingToReachDestination(MapLocation destination) {
    //Try going the other way + some noise
    float noise = (float)Math.random() * (float)Math.PI/2f;
    noise = noise - (float)Math.PI/4f;
    this.currentDirection = new Direction(
        currentDirection.opposite().radians + noise
        );
    traveller.setDestination(randomDestination());
  }

  public void onReachingDiversion(MapLocation destination) {}
  public void onDiversion(MapLocation destination) {}
  public void onNeedingToDivert(MapLocation destination) {}
  public void onMapBoundaryFound(MapLocation destination) {}
}
