package swarming.robots;
import battlecode.common.*;

public strictfp class Archon extends Robot {
  public Archon(RobotController rc){
    super(rc);
  }

  //Archon's Strategy should be split between building
  //as many gardeners as it can and trying to stay safe
  //TODO: implement the safety strategy
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    try {
      Direction dir = this.randomDirection();
      if(this.rc.isBuildReady() && this.rc.canHireGardener(dir)){
        this.rc.hireGardener(dir);
      }
    } catch (GameActionException ex) {
      //Do not let an archon die!
      System.err.println(ex.getMessage());
    }
  }
}
