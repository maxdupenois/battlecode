package swarming.robots;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import maxdupenois.behaviours.movement.PatrolMovementBehaviour;
import maxdupenois.behaviours.movement.BoidMovementBehaviour;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.Iterator;

public strictfp class Scout extends Robot {
  private Team team;
  public Scout(RobotController rc){
    super(rc);
    this.team = rc.getTeam();
    setMovementBehaviour(new BoidMovementBehaviour(rc, RobotType.SCOUT, 30f));
    //if(Math.random() >= 0.5){
    //  setMovementBehaviour(new SimpleRandomMovementBehaviour(rc, 30f));
    //} else {
    //  setMovementBehaviour(new PatrolMovementBehaviour(rc, 40f));
    //}
  }

  // SCOUTS! :)
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    RobotInfo[] localRobots = this.rc.senseNearbyRobots();
    Direction dir;
    ArrayList<RobotInfo> myTeam = new ArrayList<RobotInfo>();
    ArrayList<RobotInfo> enemies = new ArrayList<RobotInfo>();
    for(int r = 0; r < localRobots.length; r++){
      if(localRobots[r].getTeam() == Team.NEUTRAL) continue;
      if(localRobots[r].getTeam() == this.team) {
        myTeam.add(localRobots[r]);
      } else {
        enemies.add(localRobots[r]);
      }
    }
    Iterator<RobotInfo> enemyIter = enemies.iterator();
    Iterator<RobotInfo> friendlyIter;
    boolean wouldHitFriend = false;
    RobotInfo enemy;
    RobotInfo friend;

    MapLocation myLocation = this.rc.getLocation();
    MapLocation enemyLocation;
    MapLocation friendLocation;

    Direction enemyDirection;
    Direction friendDirection;

    float enemyDistance;
    // approx 1 degree in radians
    float maxDirectionSimilarity = (float)0.017451;
    while(enemyIter.hasNext() && this.rc.canFireSingleShot()){
      //if(!this.rc.canFireSingleShot()) continue;
      enemy = enemyIter.next();
      enemyLocation = enemy.getLocation();

      // This should never be null because
      // that would mean I am on the enemies location
      // kinky
      enemyDirection = myLocation.directionTo(enemyLocation);
      enemyDistance = myLocation.distanceTo(enemyLocation);
      friendlyIter = myTeam.iterator();
      wouldHitFriend = false;
      while(friendlyIter.hasNext() && !wouldHitFriend){
        friend = friendlyIter.next();
        friendLocation = friend.getLocation();
        // This should never be null because
        // that would mean I am on my friends location
        // less kinky, little too friendly
        friendDirection = myLocation.directionTo(friendLocation);
        // Closer to me than the enemy and in the same
        // direction
        wouldHitFriend = (
            myLocation.distanceTo(friendLocation) <= enemyDistance &&
            friendDirection.equals(enemyDirection, maxDirectionSimilarity)
            );
      }
      if(!wouldHitFriend) this.rc.fireSingleShot(enemyDirection);
    }
  }
}
