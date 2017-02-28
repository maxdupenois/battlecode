package swarming.robots;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import battlecode.common.*;

public strictfp class Gardener extends Robot {
  private int scouts = 0;
  public Gardener(RobotController rc){
    super(rc);
    setMovementBehaviour(new SimpleRandomMovementBehaviour(rc, 20f));
  }

  // For this version a garderner has three main goals
  // spawn scouts, ideally we want as many of these as possible
  // to act as a swarm, farm and build a wall to protect the archon
  // We'll tackle the wall last as we don't want to prevent archons from being able to spawn gardeners
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    try {
      Direction dir = new Direction(
          (float)Math.random() * 2 * (float)Math.PI
          );
      if(scouts < 5 && this.rc.isBuildReady() && this.rc.canBuildRobot(RobotType.SCOUT, dir)){
        this.rc.buildRobot(RobotType.SCOUT, dir);
        scouts ++;
      }
    } catch (GameActionException ex) {
        System.out.println("ERROR");
      //Do not let an archon die!
      System.err.println(ex.getMessage());
    }
  }
}
