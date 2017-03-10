package swarming.robots;
import battlecode.common.*;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;

public strictfp class Archon extends Robot {
  private int numberOfArchons;
  //private int gardeners = 0;
  private static int MIN_BULLETS_AFTER_DONATION = 200;
  private static float ALLOWED_PROPORTION_OF_BULLETS_TO_DONATE = 0.33f;
  public Archon(RobotController rc){
    super(rc);
    addBeforeMoveBehaviour(new PatrolMovementBehaviour(rc, 20f, traveller));
    this.numberOfArchons = rc.getInitialArchonLocations(rc.getTeam()).length;
  }

  // Archon's Strategy should be split between building
  // as many gardeners as it can and trying to stay safe
  // and buying victory points
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    Direction dir = new Direction(
        (float)Math.random() * 2 * (float)Math.PI
        );
    //To not build millions of gardeners we'll
    //only build when the round is an odd number
    boolean buildableRound = round % 2 == 1;
    if(buildableRound &&
        this.rc.isBuildReady() &&
        this.rc.canHireGardener(dir)){
      this.rc.hireGardener(dir);
      //gardeners++;
    }
    buyVictoryPoints();
  }

  private void buyVictoryPoints() throws GameActionException {
    // Only do this if I have at least a given nnumber of bullets
    float bullets = rc.getTeamBullets();
    if(bullets < MIN_BULLETS_AFTER_DONATION) return;
    float cost = rc.getVictoryPointCost();
    // need to account for all archons potentially
    // spending bullets, there is some fuzziness here
    // because if the previous archon spent then I have
    // less to work with anyway but I don't mind because
    // I don't want to slow my building down too much
    float proportionICanSpend = ALLOWED_PROPORTION_OF_BULLETS_TO_DONATE / numberOfArchons;
    float maxSpend = bullets * proportionICanSpend;
    float actSpend = maxSpend - ( maxSpend % cost );
    if(actSpend > 0 && bullets - actSpend < MIN_BULLETS_AFTER_DONATION) {
      rc.donate(actSpend);
    }
  }

}
