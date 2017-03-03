package maxdupenois.behaviours.shooting;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.RobotInfo;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.GameActionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.BiConsumer;

public strictfp class NoFriendlyFire {
  private RobotController rc;
  private Team team;
  public NoFriendlyFire(RobotController rc, Team team){
    this.rc = rc;
    this.team = team;
  }

  public interface BiConsumerThrowsGameException<T, R> {
    public void acceptOrThrow(T t, R r) throws GameActionException;
  }

  private RobotInfo[][] findLocalRobots(){
    RobotInfo[] localRobots = this.rc.senseNearbyRobots();
    ArrayList<RobotInfo> friendlies = new ArrayList<RobotInfo>();
    ArrayList<RobotInfo> enemies = new ArrayList<RobotInfo>();
    for(int r = 0; r < localRobots.length; r++){
      if(localRobots[r].getTeam() == Team.NEUTRAL) continue;
      if(localRobots[r].getTeam() == this.team) {
        friendlies.add(localRobots[r]);
      } else {
        enemies.add(localRobots[r]);
      }
    }
    return new RobotInfo[][] {
      friendlies.toArray(new RobotInfo[friendlies.size()]),
      enemies.toArray(new RobotInfo[enemies.size()])
    };
  }

  public void fire(
      Predicate<RobotController> canFireFunc,
      BiConsumerThrowsGameException<RobotController, Direction> fireFunc
      ) throws GameActionException {
    RobotInfo[][] localRobots = findLocalRobots();
    RobotInfo[] friendlies = localRobots[0];
    RobotInfo[] enemies = localRobots[1];

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
    for(int e = 0; e < enemies.length && canFireFunc.test(this.rc); e++){
      //if(!this.rc.canFireSingleShot()) continue;
      enemy = enemies[e];
      enemyLocation = enemy.getLocation();

      // This should never be null because
      // that would mean I am on the enemies location
      // kinky
      enemyDirection = myLocation.directionTo(enemyLocation);
      enemyDistance = myLocation.distanceTo(enemyLocation);
      wouldHitFriend = false;
      for(int f = 0; f < friendlies.length && !wouldHitFriend; f++){
        friend = friendlies[f];
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
      if(!wouldHitFriend) fireFunc.acceptOrThrow(this.rc, enemyDirection);
    }
  }
}
