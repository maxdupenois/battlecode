package swarming.robots;
import maxdupenois.behaviours.movement.BoidMovementBehaviour;
import maxdupenois.behaviours.shooting.NoFriendlyFire;
import battlecode.common.*;

// currently basically the same as a scout but this
// will likely change
public strictfp class Soldier extends Robot {
  private Team team;
  private NoFriendlyFire firingBehaviour;
  public Soldier(RobotController rc){
    super(rc);
    this.team = rc.getTeam();
    this.firingBehaviour = new NoFriendlyFire(rc, team);
    setMovementBehaviour(new BoidMovementBehaviour(rc, RobotType.SOLDIER, 20f));
  }

  // SCOUTS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    firingBehaviour.fire(
        (rbt) -> rbt.canFireSingleShot(),
        (rbt, dir) -> rbt.fireSingleShot(dir)
        );
  }
}
