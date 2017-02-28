package swarming.robots;
import battlecode.common.*;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;

public strictfp class Archon extends Robot {
  public int gardeners = 0;
  public Archon(RobotController rc){
    super(rc);
    setMovementBehaviour(new PatrolMovementBehaviour(rc, 20f));
  }

  //Archon's Strategy should be split between building
  //as many gardeners as it can and trying to stay safe
  //TODO: implement the safety strategy
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    try {
      Direction dir = new Direction(
          (float)Math.random() * 2 * (float)Math.PI
          );
      if(gardeners < 100 && this.rc.isBuildReady() && this.rc.canHireGardener(dir)){
        this.rc.hireGardener(dir);
        gardeners ++;
      }
    } catch (GameActionException ex) {
        System.out.println("ERROR");
      //Do not let an archon die!
      System.err.println(ex.getMessage());
    }
  }
}
