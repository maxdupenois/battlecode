package swarming.robots;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;
import battlecode.common.*;

public strictfp class Scout extends Robot {
  private Team team;
  public Scout(RobotController rc){
    super(rc);
    this.team = rc.getTeam();
    if(Math.random() >= 0.5){
      setMovementBehaviour(new SimpleRandomMovementBehaviour(rc, 30f));
    } else {
      setMovementBehaviour(new PatrolMovementBehaviour(rc, 40f));
    }
  }

  // SCOUTS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    RobotInfo[] localRobots = this.rc.senseNearbyRobots();
    Direction dir;
    for(int r = 0; r < localRobots.length; r++){
      if(localRobots[r].getTeam() == this.team) continue;
      if(localRobots[r].getTeam() == Team.NEUTRAL) continue;
      if(!this.rc.canFireSingleShot()) continue;
      dir = this.rc.getLocation().directionTo(localRobots[r].getLocation());
      this.rc.fireSingleShot(dir);
    }
  }
}
