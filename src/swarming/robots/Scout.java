package swarming.robots;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;
import maxdupenois.behaviours.movement.BoidMovementBehaviour;
import maxdupenois.behaviours.shooting.NoFriendlyFire;
import battlecode.common.*;

public strictfp class Scout extends Robot {
  private Team team;
  private NoFriendlyFire firingBehaviour;
  public Scout(RobotController rc){
    super(rc);
    this.team = rc.getTeam();
    this.firingBehaviour = new NoFriendlyFire(rc, team);
    setMovementBehaviour(new BoidMovementBehaviour(rc, RobotType.SCOUT, 30f));
    //if(Math.random() >= 0.5){
    //  setMovementBehaviour(new SimpleRandomMovementBehaviour(rc, 30f));
    //} else {
    //  setMovementBehaviour(new PatrolMovementBehaviour(rc, 40f));
    //}
  }

  // SCOUTS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    firingBehaviour.fire(
        (rbt) -> rbt.canFireSingleShot(),
        (rbt, dir) -> rbt.fireSingleShot(dir)
        );
  }
}
