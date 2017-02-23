package swarming.robots;
import maxdupenois.behaviours.movement.BasicMovement;
import battlecode.common.*;

public strictfp class Gardener extends Robot {
  public Gardener(RobotController rc){
    super(rc);
    setMovementBehaviour(new BasicMovement(this, 20));
    getMovementBehaviour().setDestination(new MapLocation(175, 175));
  }

  // For this version a garderner has three main goals
  // spawn scouts, ideally we want as many of these as possible
  // to act as a swarm, farm and build a wall to protect the archon
  // We'll tackle the wall last as we don't want to prevent archons from being able to spawn gardeners
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
  }
}
