package maxdupenois.behaviours.movement;

import static maxdupenois.util.GeometryUtil.*;
import maxdupenois.behaviours.Behaviour;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.GameActionException;
import battlecode.common.Direction;
import battlecode.common.BulletInfo;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.Collector;
import java.util.stream.Collectors;

//The most basic form of movement behaviour,
//does noting with hooks, simply uses a traveller
//to head to a location
public strictfp class BoidMovementBehaviour implements Behaviour, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;
  private float range;
  private RobotType groupingType;
  private Team team;
  // Avoid crowding
  private float separation = 0.4f;
  // head towards center of mass
  private float cohesion = 0.8f;
  // same direction
  private float alignment = 0.2f;

  private float bulletAvoidance = 0.8f;

  private float enemyCohesion = 0.8f;
  private float enemySeparation = 0.6f;

  //At maximum range we have full cohesion no separation
  //at minimum range we have full separation no cohesion
  private float[] cohesionToSeparationRange = new float[]{
    2f, 10f
  };

  private float[] enemyCohesionToSeparationRange = new float[]{
    5f, 20f
  };

  private float[] bulletAwarenessRange = new float[]{
    5f, 10f
  };

  private Map<Integer, MapLocation> previousCompanionLocations;
  private MapLocation previousLocation;
  private int stuckCount;
  private int clearStuckCount;
  private static int MAX_STUCK_COUNT=2;
  private static int MOVES_TO_CLEAR_BEING_STUCK = 6;

  public BoidMovementBehaviour(RobotController robotController, RobotType groupingType, float range){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
    this.groupingType = groupingType;
    this.team = robotController.getTeam();
    this.range = range;
    this.previousCompanionLocations = new HashMap<Integer, MapLocation>();
    this.stuckCount = 0;
    this.clearStuckCount = 0;
  }

  public void onMapBoundaryFound(MapLocation destination) {
    //Force being stuck
    this.stuckCount = MAX_STUCK_COUNT;
  }


  public void execute() throws GameActionException {
    RobotInfo[] robots = this.robotController.senseNearbyRobots();
    BulletInfo[] bullets = this.robotController.senseNearbyBullets();
    RobotInfo[] companions = filterCompanions(robots);
    RobotInfo[] enemies = filterEnemies(robots);
    MapLocation currentLocation = robotController.getLocation();

    //debug_showCurrentDestination();

    checkIfStuck(currentLocation);

    boolean isMovingAwayFromBeingStuck = clearStuckCount > 0;
    boolean isStuck = stuckCount >= MAX_STUCK_COUNT;
    boolean hasNoDestinationAndNoCompanions = !this.traveller.hasDestination() && companions.length == 0;
    boolean hasCompanions = companions.length > 0;

    //Main logic switch, needs
    //to be refactored to be clearer
    if(isMovingAwayFromBeingStuck){
      //continue on to previous destination but
      //countdown the stuck count
      clearStuckCount --;
    } else if(isStuck) {
      bounceAwayFromBeingStuck(currentLocation);
    } else if(hasNoDestinationAndNoCompanions){
      moveToRandomLocation(currentLocation);
    } else if(hasCompanions) {
      applyBoidBehaviours(currentLocation, companions);

      //only apply enemy cohesion if we're in a group
      if(enemies.length > 0) applyEnemyCohesion(currentLocation, enemies);
    }

    //Secondary behaviours that should always apply,
    //e.g. bullet avoidance
    if(bullets.length > 0){
      applyBulletAvoidance(currentLocation, bullets);
    }
    if(enemies.length > 0){
      applyEnemySeparation(currentLocation, enemies);
    }

    //debug_showNewDestination();

    traveller.continueToDestination();

    this.previousCompanionLocations = buildPreviousCompanionLocations(companions);
    this.previousLocation = currentLocation;
  }

  private void checkIfStuck(MapLocation currentLocation){
    if(previousLocation == null) return;
    if(!previousLocation.isWithinDistance(currentLocation, 1f)) return;
    stuckCount++;
  }

  private void bounceAwayFromBeingStuck(MapLocation currentLocation){
    //Bounce!
    Direction dir = currentLocation.directionTo(this.traveller.getDestination());
    if(dir == null){
      //Shouldn't happen as this means we're stuck
      //but also where we want to be
      moveToRandomLocation(currentLocation);
    } else {
      MapLocation newDestination = currentLocation.add(dir.opposite(), this.range);
      this.traveller.setDestination(newDestination);
    }
    this.stuckCount = 0;
    // After identifying that we're stuck
    // we need to have a bit of time allowed
    // to move us away, otherwise we'll regroup
    // and get stuck on the wall again
    this.clearStuckCount = MOVES_TO_CLEAR_BEING_STUCK;
  }

  private void moveToRandomLocation(MapLocation currentLocation) {
    traveller.setDestination(randomDestination(currentLocation, this.range));
  }

  private Direction baseDirection(MapLocation currentLocation){
    Direction base = null;
    // directionTo returns null if the the current location
    // and the destination are the same so we deal with
    // that the same way we deal with there not being a
    // traveller destination yet
    if(traveller.hasDestination()){
      MapLocation destination = traveller.getDestination();
      base = currentLocation.directionTo(destination);
    }
    return (base == null ? randomDirection() : base);
  }

  private void applyBoidBehaviours(MapLocation currentLocation, RobotInfo[] companions){
    Direction dir = baseDirection(currentLocation);

    dir = modifyDirectionByCohesion(dir, companions);
    dir = modifyDirectionByAlignment(dir, companions);
    dir = modifyDirectionBySeparation(dir, companions);
    traveller.setDestination(currentLocation.add(dir, this.range));
  }

  private void applyBulletAvoidance(MapLocation currentLocation, BulletInfo[] bullets){
    // Because the target can have been set by boid behaviour we
    // can reuse the base direction method without losing
    // their modifications
    Direction dir = baseDirection(currentLocation);
    Direction bulletDir;
    MapLocation bulletLoc;
    // To make this simple as a starting version
    // we'll ignore speed and our own travel,
    // if the direction of a bullet takes it to
    // our current location we'll take that
    // as sufficient warning to bail

    float minRange = bulletAwarenessRange[0];
    float maxRange = bulletAwarenessRange[1];
    // How close we have to be consider a collision
    float collisionRange = 1f;
    float distanceToBullet;
    float actBulletAvoidance;
    float rangeDifference;
    float distanceWithinRange;
    Direction avoidanceDir;
    for(int b = 0; b < bullets.length; b++){
      bulletDir = bullets[b].getDir();
      bulletLoc = bullets[b].getLocation();
      distanceToBullet = currentLocation.distanceTo(bulletLoc);
      // NOTE: Weirdly better without this, they don't run
      // in to their own bullets:
      // Only care if bullet is on the way to me
      // if(distanceToBullet <= currentLocation.distanceTo(bulletLoc.add(bulletDir))) continue;
      //bullet range is the maximum awareness range
      if(!isCollisionLikely(
            currentLocation, bulletLoc, bulletDir,
            maxRange, collisionRange)) continue;

      //bullet avoidance scales from the full amount at min range
      //to 0 at max range
      //S = separation, M_0 = min range, M_1 max range
      //f(d) = S * (1 - MIN(MAX(d - M_0, 0), M_1 - M_0)/(M_1 - M_0))
      rangeDifference = maxRange - minRange;
      distanceWithinRange = Math.min(Math.max(distanceToBullet - minRange, 0), rangeDifference);
      actBulletAvoidance = bulletAvoidance * (1 - distanceWithinRange/rangeDifference);

      // Bullet direction might be in front or behind me
      // so simply going the opposite of that might
      // put me closer to the bullet, rather than
      // work it out we'll aim perpendicular to the bullet
      // Veer left or right randomly
      if (Math.random() > 0.5) {
        avoidanceDir = bulletDir.rotateRightDegrees(90);
      } else {
        avoidanceDir = bulletDir.rotateLeftDegrees(90);
      }

      dir = modifyDirection(dir, avoidanceDir, actBulletAvoidance);
    }
    traveller.setDestination(currentLocation.add(dir, this.range));
  }

  private Map<Integer, MapLocation> buildPreviousCompanionLocations(RobotInfo[] companions){
    return Arrays.
      stream(companions).
      <Map<Integer, MapLocation>>collect(
          () -> new HashMap<Integer, MapLocation>(),
          (Map<Integer, MapLocation> map, RobotInfo ri) -> map.put(ri.ID, ri.getLocation()),
          (Map<Integer, MapLocation> a, Map<Integer, MapLocation> b) -> {}
          );
  }

  // Move towards local enemies
  private void applyEnemyCohesion(MapLocation currentLocation, RobotInfo[] enemies) {
    // Because the target can have been set by boid behaviour we
    // can reuse the base direction method without losing
    // their modifications
    Direction dir = baseDirection(currentLocation);
    Direction bulletDir;
    MapLocation bulletLoc;
    // To make this simple as a starting version
    // we'll ignore speed and our own travel,
    // if the direction of a bullet takes it to
    // our current location we'll take that
    // as sufficient warning to bail

    MapLocation centreOfMass = meanLocation(enemies);
    Direction toLocalEnemies = currentLocation.directionTo(centreOfMass);
    if(toLocalEnemies == null) {
      //Uh panic? We're in the middle of the buggers
      //so any destination is as good as any other,
      //leave it as it is
      return;
    }
    float distance = currentLocation.distanceTo(centreOfMass);
    float minRange = enemyCohesionToSeparationRange[0];
    float maxRange = enemyCohesionToSeparationRange[1];
    //cohesion scales from the 0 at min range
    //to full amount at max range
    //C = cohesion, M_0 = min range, M_1 max range
    //f(d) = C * MIN(MAX(d - M_0, 0), M_1 - M_0)/(M_1 - M_0))
    float rangeDifference = maxRange - minRange;
    float distanceWithinRange = Math.min(Math.max(distance - minRange, 0), rangeDifference);
    float actualCoh = enemyCohesion * (distanceWithinRange/rangeDifference);
    dir = modifyDirection(dir, toLocalEnemies, actualCoh);

    traveller.setDestination(currentLocation.add(dir, this.range));
  }

  //Try to avoid getting to close to enemies
  private void applyEnemySeparation(MapLocation currentLocation, RobotInfo[] enemies) {
    // Because the target can have been set by boid behaviour we
    // can reuse the base direction method without losing
    // their modifications
    Direction dir = baseDirection(currentLocation);
    Direction bulletDir;
    MapLocation bulletLoc;
    // To make this simple as a starting version
    // we'll ignore speed and our own travel,
    // if the direction of a bullet takes it to
    // our current location we'll take that
    // as sufficient warning to bail

    MapLocation centreOfMass = meanLocation(enemies);
    Direction toLocalEnemies = currentLocation.directionTo(centreOfMass);
    if(toLocalEnemies == null) {
      //Uh panic? We're in the middle of the buggers
      //so any destination is as good as any other,
      //leave it as it is
      return;
    }
    float distance = currentLocation.distanceTo(centreOfMass);
    float minRange = enemyCohesionToSeparationRange[0];
    float maxRange = enemyCohesionToSeparationRange[1];
    //separation scales from the full amount at min range
    //to 0 at max range
    //S = separation, M_0 = min range, M_1 max range
    //f(d) = S * (1 - MIN(MAX(d - M_0, 0), M_1 - M_0)/(M_1 - M_0))
    float rangeDifference = maxRange - minRange;
    float distanceWithinRange = Math.min(Math.max(distance - minRange, 0), rangeDifference);
    float actualSep = enemySeparation * (1 - distanceWithinRange/rangeDifference);

    dir = modifyDirection(dir, toLocalEnemies.opposite(), actualSep);
    traveller.setDestination(currentLocation.add(dir, this.range));
  }

  //Try to avoid crowding companions/flockmates
  private Direction modifyDirectionBySeparation(Direction dir, RobotInfo[] companions) {
    MapLocation loc = this.robotController.getLocation();
    MapLocation centreOfMass = meanLocation(companions);
    Direction toLocalCompanions = loc.directionTo(centreOfMass);
    if(toLocalCompanions == null) return dir;
    float distance = loc.distanceTo(centreOfMass);
    float minRange = cohesionToSeparationRange[0];
    float maxRange = cohesionToSeparationRange[1];
    //separation scales from the full amount at min range
    //to 0 at max range
    //S = separation, M_0 = min range, M_1 max range
    //f(d) = S * (1 - MIN(MAX(d - M_0, 0), M_1 - M_0)/(M_1 - M_0))
    //e.g. range = [2, 12]
    //S = 0.5
    //d = 2
    //f(2) = 0.5 * (1 - MAX(2 - 2, 0)/(12 - 2))
    //f(2) = 0.5 * (1 - 0/10)
    //f(2) = 0.5
    //d = 12
    //f(12) = 0.5 * (1 - MAX(12 - 2, 0)/(12 - 2))
    //f(12) = 0.5 * (1 - 10/10)
    //f(12) = 0
    //f(7) = 0.5 * (1 - 5/10)
    //f(7) = 0.25
    float rangeDifference = maxRange - minRange;
    float distanceWithinRange = Math.min(Math.max(distance - minRange, 0), rangeDifference);
    float actualSep = separation * (1 - distanceWithinRange/rangeDifference);

    return modifyDirection(dir, toLocalCompanions.opposite(), actualSep);
  }

  //Try to move towards the centre of mass of companions/flockmates
  private Direction modifyDirectionByCohesion(Direction dir, RobotInfo[] companions){
    MapLocation loc = this.robotController.getLocation();
    MapLocation centreOfMass = meanLocation(companions);
    Direction toLocalCompanions = loc.directionTo(centreOfMass);
    if(toLocalCompanions == null) return dir;
    float minRange = cohesionToSeparationRange[0];
    float maxRange = cohesionToSeparationRange[1];
    float distance = loc.distanceTo(centreOfMass);
    //cohesion scales from the 0 at min range
    //to full amount at max range
    //C = cohesion, M_0 = min range, M_1 max range
    //f(d) = C * MIN(MAX(d - M_0, 0), M_1 - M_0)/(M_1 - M_0))
    float rangeDifference = maxRange - minRange;
    float distanceWithinRange = Math.min(Math.max(distance - minRange, 0), rangeDifference);
    float actualCoh = cohesion * (distanceWithinRange/rangeDifference);
    return modifyDirection(dir, toLocalCompanions, actualCoh);
  }

  //Try to go in the same direction as companions/flockmates
  private Direction modifyDirectionByAlignment(Direction dir, RobotInfo[] companions) {
    Direction[] companionDirections = estimateCompanionDirections(companions);
    return modifyDirection(dir, meanDirection(companionDirections), alignment);
  }

  private Direction[] estimateCompanionDirections(RobotInfo[] companions){
    //Direction is null if there's been no movement so
    //we need to filter them out
    Stream<Direction> stream = Arrays
      .stream(companions)
      .filter(c -> this.previousCompanionLocations.containsKey(c.ID))
      .map(c -> {
        MapLocation prev = this.previousCompanionLocations.get(c.ID);
        MapLocation current = c.getLocation();
        return prev.directionTo(current);
      })
      .filter(d -> d != null);
    Direction[] dirs = stream.toArray(Direction[]::new);
    return dirs;
  }

  private RobotInfo[] filterEnemies(RobotInfo[] robots){
    ArrayList<RobotInfo> enemies = new ArrayList<RobotInfo>();
    Team rbtTeam;
    for(int r = 0; r < robots.length; r++){
      rbtTeam = robots[r].getTeam();
      if(rbtTeam != this.team) continue;
      if(rbtTeam != Team.NEUTRAL) continue;
      enemies.add(robots[r]);
    }
    return enemies.toArray(new RobotInfo[enemies.size()]);
  }

  private RobotInfo[] filterCompanions(RobotInfo[] robots){
    ArrayList<RobotInfo> companions = new ArrayList<RobotInfo>();
    for(int r = 0; r < robots.length; r++){
      if(robots[r].getTeam() != this.team) continue;
      if(robots[r].getType() != this.groupingType) continue;
      companions.add(robots[r]);
    }
    return companions.toArray(new RobotInfo[companions.size()]);
  }


  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {}
  public void onFailingToReachDestination(MapLocation destination) {}
  public void onReachingDiversion(MapLocation destination) {}
  public void onDiversion(MapLocation destination) {}
  public void onNeedingToDivert(MapLocation destination) {}

  // Debug methods
  private void debug_showCurrentDestination(){
    if(!this.traveller.hasDestination()) return;
    this.robotController.setIndicatorLine(
        this.robotController.getLocation(),
        this.traveller.getDestination(), 0, 255, 0);
  }

  private void debug_showNewDestination(){
    if(!this.traveller.hasDestination()) return;
    this.robotController.setIndicatorLine(
        this.robotController.getLocation(),
        this.traveller.getDestination(), 0, 0, 255);
  }

  private void debug_printCompanionLocationsMap(Map<Integer, MapLocation> map){
    StringBuffer b = new StringBuffer("LOCATION MAP {\n");
    Set<Integer> keys = map.keySet();
    Iterator<Integer> iter = keys.iterator();
    Integer key;
    while(iter.hasNext()){
      key = iter.next();
      b.append("  ");
      b.append(key.intValue()+"");
      b.append(": ");
      b.append(map.get(key));
      b.append(",\n");
    }
    b.append("}");
    System.out.println(b.toString());
  }
}
