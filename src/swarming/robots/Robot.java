package swarming.robots;
import battlecode.common.*;

public abstract strictfp class Robot {
  protected RobotController rc;
  protected MapLocation currentLocation;
  protected RobotType type;

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

  public void run() throws GameActionException {
    int remainingBytecodes;
    while(true) {
      this.currentLocation = this.rc.getLocation();
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
  void onReachingDestination(MapLocation destination) {}
  void onFailingDestination(MapLocation destination) {}

  public Direction randomDirection() {
    return new Direction((float)Math.random() * 2 * (float)Math.PI);
  }
}
