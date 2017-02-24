package swarming.robots;
import battlecode.common.*;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;

public strictfp class Archon extends Robot {
  public Archon(RobotController rc){
    super(rc);
    setMovementBehaviour(new PatrolMovementBehaviour(rc, 50f));
  }

  //Archon's Strategy should be split between building
  //as many gardeners as it can and trying to stay safe
  //TODO: implement the safety strategy
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    //try {
    //  Direction dir = new Direction(
    //      (float)Math.random() * 2 * (float)Math.PI
    //      );
    //  if(this.rc.isBuildReady() && this.rc.canHireGardener(dir)){
    //    this.rc.hireGardener(dir);
    //  }
    //} catch (GameActionException ex) {
    //    System.out.println("ERROR");
    //  //Do not let an archon die!
    //  System.err.println(ex.getMessage());
    //}
  }
}
