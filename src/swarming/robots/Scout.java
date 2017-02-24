package swarming.robots;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import battlecode.common.*;

public strictfp class Scout extends Robot {
  private Team team;
  public Scout(RobotController rc){
    super(rc);
    this.team = rc.getTeam();
    setMovementBehaviour(new SimpleRandomMovementBehaviour(rc, 30f));
  }

  // SCOUTS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    RobotInfo[] localRobots = this.rc.senseNearbyRobots();
    Direction dir;
    for(int r = 0; r < localRobots.length; r++){
      if(localRobots[r].getTeam() == this.team) continue;
      if(!this.rc.canFireSingleShot()) continue;
      dir = this.rc.getLocation().directionTo(localRobots[r].getLocation());
      this.rc.fireSingleShot(dir);
    }
  }
}
