package swarming.robots;
import maxdupenois.behaviours.movement.MoverInterface;
import maxdupenois.behaviours.movement.MovementInterface;
import battlecode.common.*;

public abstract strictfp class Robot implements MoverInterface{
  protected RobotController rc;
  protected MapLocation currentLocation;
  protected RobotType type;
  protected MovementInterface movementBehaviour = null;

  public Robot(RobotController rc){
    this.rc = rc;
    this.type = rc.getType();
  }

  public void setRobotController(RobotController rc) {
    this.rc = rc;
  }

  public RobotController getRobotController() {
    return this.rc;
  }

  public boolean hasMovementBehaviour(){
    return this.movementBehaviour != null;
  }

  public MovementInterface getMovementBehaviour(){
    return this.movementBehaviour;
  }

  public void setMovementBehaviour(MovementInterface movementBehaviour){
    this.movementBehaviour = movementBehaviour;
  }

  public void run() throws GameActionException {
    int remainingBytecodes;
    while(true) {
      this.currentLocation = this.rc.getLocation();
      if(this.movementBehaviour.hasDestination() && !this.movementBehaviour.hasReachedDestination()){
        this.movementBehaviour.continueToDestination();
      }
      remainingBytecodes = Clock.getBytecodesLeft();
      if(remainingBytecodes > 0){
        // No point if there's nothing left that can be done
        takeTurn(this.rc.getRoundNum(), remainingBytecodes);
      }
      // Close out the turn
      Clock.yield();
    }
  }

  void takeTurn(int round, int remainingBytecodes) throws GameActionException {}

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {}
  public void onFailingToReachDestination(MapLocation destination) {}
  public void onReachingDestinationNode(MapLocation destination) {}
  public void onFailingToReachDestinationNode(MapLocation destination) {}

  public float getStrideRadius(){
    return this.type.strideRadius;
  }

  public MapLocation getCurrentLocation(){
    return this.currentLocation;
  }

  public boolean hasMoved(){
    return this.rc.hasMoved();
  }

  public boolean canMove(MapLocation location){
    return this.rc.canMove(location);
  }

  public void move(MapLocation location){
    try {
      this.rc.move(location);
    } catch(GameActionException ex){
      //TODO: Not sure if I want to catch this here, have a think
      System.err.println(ex.getMessage());
    }
  }

  public Direction randomDirection() {
    return new Direction((float)Math.random() * 2 * (float)Math.PI);
  }
}
