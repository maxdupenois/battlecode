package swarming.robots;
import maxdupenois.behaviours.movement.BoidMovementBehaviour;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
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
    addBehaviour(new SimpleRandomMovementBehaviour(
          rc, 30f
          ));
    addBehaviour(new NoFriendlyFire(
        rc, team,
        (rbt) -> rbt.canFireSingleShot(),
        (rbt, dir) -> rbt.fireSingleShot(dir)
        ));
  }

  // SOLDIERS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {}
}
