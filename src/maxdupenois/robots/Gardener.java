package maxdupenois.robots;
import battlecode.common.*;

public strictfp class Gardener extends Robot {
  public Gardener(RobotController rc){
    super(rc);
  }

  // For this version a garderner has three main goals
  // spawn scouts, ideally we want as many of these as possible
  // to act as a swarm, farm and build a wall to protect the archon
  // We'll tackle the wall last as we don't want to prevent archons from being able to spawn gardeners
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    if(this.hasReachedDestination()) {
      this.setDestination(new MapLocation(175f, 175f));
    } else {
      this.continueToDestination();
    }
    //if(rc.hasMoved()) return;

    //Direction dir = randomDirection();
    //if(rc.canMove(dir)){
    //  System.out.println("Derp!");
    //  rc.move(dir);
    //}
  }
}
