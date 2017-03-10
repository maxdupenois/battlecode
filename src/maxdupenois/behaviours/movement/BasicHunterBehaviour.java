package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;
import battlecode.common.BulletInfo;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import static maxdupenois.util.GeometryUtil.*;
import maxdupenois.behaviours.Behaviour;
import java.util.ArrayList;

// Very similar to random movement
// up until it spots an enemy
public strictfp class BasicHunterBehaviour implements Behaviour, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;
  private float range;
  private RobotInfo target;
  private Team team;
  private Direction currentDirection;

  public BasicHunterBehaviour(RobotController robotController, float range, Traveller traveller){
    this.traveller = traveller;
    this.robotController = robotController;
    this.range =range;
    this.currentDirection = randomDirection();
    this.team = robotController.getTeam();
    this.target = null;
    traveller.subscribe(this);
    traveller.setDestination(randomDestination());
  }

  private void updateTarget() throws GameActionException {
    if(target == null) return;
    boolean canFindTarget = this.robotController.canSenseRobot(target.ID);
    if(canFindTarget) {
      // Update target info
      this.target = robotController.senseRobot(target.ID);
    } else {
      this.target = null;
    }
  }

  private RobotInfo[] findLocalEnemies(){
    RobotInfo[] localRobots = robotController.senseNearbyRobots();
    ArrayList<RobotInfo> enemies = new ArrayList<RobotInfo>();
    for(int r = 0; r < localRobots.length; r++){
      if(localRobots[r].getTeam() == Team.NEUTRAL) continue;
      if(localRobots[r].getTeam() == this.team) continue;
      enemies.add(localRobots[r]);
    }
    return enemies.toArray(new RobotInfo[enemies.size()]);
  }

  private void findNewTarget() throws GameActionException {
    RobotInfo[] enemies = findLocalEnemies();
    if(enemies.length == 0) return;
    //always attach the weakest, we'll see if this works
    RobotInfo weakest = enemies[0];
    float weakestHealth = weakest.getHealth();
    float enemyHealth;
    for(int r = 1; r < enemies.length; r++){
      enemyHealth = enemies[r].getHealth();
      if(enemyHealth < weakestHealth){
        weakestHealth = enemyHealth;
        weakest = enemies[r];
      }
    }
    this.target = weakest;
  }

  private void headTowardsTarget(){
    MapLocation targetLocation = target.getLocation();
    MapLocation myLocation = robotController.getLocation();
    Direction dir = myLocation.directionTo(targetLocation);
    float distance = myLocation.distanceTo(targetLocation);
    robotController.setIndicatorLine(myLocation, targetLocation, 100, 100, 255);
    // Don't try and be on them, just near enough to shoot
    traveller.setDestination(
        myLocation.add(dir, distance - 2)
        );
  }

  public void execute() throws GameActionException{
    updateTarget();
    if(target==null) findNewTarget();
    if(target != null){
      headTowardsTarget();
    } else if(traveller.hasNoDestinationOrHasFinished()){
      this.currentDirection = randomDirection();
      traveller.setDestination(randomDestination());
    }
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {
    // If we have a target then stay pointed at it
    if(target != null) return;
    this.currentDirection = randomDirection();
    traveller.setDestination(randomDestination());
  }

  public void onFailingToReachDestination(MapLocation destination) {
    // If we have a target then stay pointed at it
    if(target != null) return;
    //Try going the other way + some noise
    float noise = (float)Math.random() * (float)Math.PI/2f;
    noise = noise - (float)Math.PI/4f;
    this.currentDirection = new Direction(
        currentDirection.opposite().radians + noise
        );
    traveller.setDestination(randomDestination());
  }


  public void onReachingDiversion(MapLocation destination) {}
  public void onDiversion(MapLocation destination) {}
  public void onNeedingToDivert(MapLocation destination) {}
  public void onMapBoundaryFound(MapLocation destination) {}

  private MapLocation randomDestination(){
    // Ensure distance is at least half our range
    float distance = ((float)Math.random() * range/2f) + range/2f;
    return robotController.getLocation().add(
        currentDirection, distance);
  }
}
