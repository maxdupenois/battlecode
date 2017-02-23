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
      if(this.hasMovementBehaviour()){
        this.movementBehaviour.move();
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

}
