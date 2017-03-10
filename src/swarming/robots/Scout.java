package swarming.robots;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;
import maxdupenois.behaviours.movement.BoidMovementBehaviour;
import maxdupenois.behaviours.shooting.NoFriendlyFire;
import battlecode.common.*;

public strictfp class Scout extends Robot {
  private Team team;
  public Scout(RobotController rc){
    super(rc);
    this.team = rc.getTeam();
    //addBeforeMoveBehaviour(new BoidMovementBehaviour(
    //      rc, RobotType.SCOUT, 30f, traveller
    //      ));
    addAfterMoveBehaviour(new NoFriendlyFire(
        rc, team,
        (rbt) -> rbt.canFireSingleShot(),
        (rbt, dir) -> rbt.fireSingleShot(dir)
        ));
  }

  // SCOUTS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {}
}
