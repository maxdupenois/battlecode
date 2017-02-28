package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.GameActionException;
import battlecode.common.Direction;
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
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.BinaryOperator;

//The most basic form of movement behaviour,
//does noting with hooks, simply uses a traveller
//to head to a location
public strictfp class BoidMovementBehaviour implements MovementInterface, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;
  private float range;
  private RobotType groupingType;
  private Team team;
  // Avoid crowding
  private float separation = 0.3f;
  // same direction
  private float alignment = 0.3f;
  // head towards center of mass
  private float cohesion = 0.4f;
  //private MapLocation destinationOffOfMap = null;

  private Map<Integer, MapLocation> previousCompanionLocations;
  private MapLocation previousLocation;
  private int stuckCount;
  private int clearStuckCount;
  private static int MAX_STUCK_COUNT=2;
  private static int MOVES_TO_CLEAR_BEING_STUCK = 4;


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

  public void move(){
    RobotInfo[] companions = nearbyCompanions();
    MapLocation currentLocation = robotController.getLocation();
    if(this.traveller.hasDestination()){
      this.robotController.setIndicatorLine(currentLocation, this.traveller.getDestination(), 0, 255, 0);
    }

    if(previousLocation != null && previousLocation.isWithinDistance(currentLocation, 1f)){
      stuckCount++;
    }

    //Main logic switch, needs
    //to be refactored to be clearer
    if(clearStuckCount > 0){
      clearStuckCount --;
      //continue on to previous destination
    } else if(stuckCount >= MAX_STUCK_COUNT) {
      //Bounce!
      Direction dir = currentLocation.directionTo(this.traveller.getDestination());
      if(dir == null){
        moveToRandomLocation();
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
    } else if(!this.traveller.hasDestination() && companions.length == 0){
      moveToRandomLocation();
    } else if(companions.length > 0) {
      // Will need a better base direction
      Direction dir = null;
      // directionTo returns null if the the current location
      // and the destination are the same so we deal with
      // that the same way we deal with there not being a
      // traveller destination yet
      //TODO: MAKE PRETTY
      if(traveller.hasDestination()){
        dir = currentLocation.directionTo(traveller.getDestination());
      }
      if(dir == null){
        dir = new Direction((float)Math.random() * 2f * (float)Math.PI);
      }
      dir = applyCohesion(dir, companions);
      dir = applyAlignment(dir, companions);
      dir = applySeparation(dir, companions);
      MapLocation loc = this.robotController.getLocation();
      traveller.setDestination(loc.add(dir, this.range));
    }
    if(traveller.hasDestination()){
      robotController.setIndicatorLine(currentLocation, traveller.getDestination(), 0, 0, 255);
    }

    try {
      traveller.continueToDestination();
    } catch (GameActionException ex) {
      System.out.println("ERROR: "+ex.getMessage());
      //TODO: Consider where you actually want to catch this
    }
    this.previousCompanionLocations = Arrays.
      stream(companions).
      <Map<Integer, MapLocation>>collect(
          () -> new HashMap<Integer, MapLocation>(),
          (Map<Integer, MapLocation> map, RobotInfo ri) -> map.put(ri.ID, ri.getLocation()),
          (Map<Integer, MapLocation> a, Map<Integer, MapLocation> b) -> {}
          );
    this.previousLocation = currentLocation;
  }

  private Direction modifyDirection(Direction original, Direction target, float amount){
    float origRadians = original.radians;
    float targetRadians = target.radians;
    //If amount == 1 then we get all of target none of original
    //if amount == 0 then all of original, none of target
    //where A = amount
    //f(o, t) = (1 - A)o + At
    //        = o - Ao + At
    //        = o - A(o + t)
    float modifiedRadians = origRadians - amount * (origRadians + targetRadians);
    return new Direction(modifiedRadians);
  }

  //Try to avoid crowding companions/flockmates
  private Direction applySeparation(Direction dir, RobotInfo[] companions) {
    MapLocation loc = this.robotController.getLocation();
    Direction toLocalCompanions = loc.directionTo(meanLocation(companions));
    if(toLocalCompanions == null) return dir;
    return modifyDirection(dir, toLocalCompanions.opposite(), separation);
  }

  //Try to move towards the centre of mass of companions/flockmates
  private Direction applyCohesion(Direction dir, RobotInfo[] companions){
    MapLocation loc = this.robotController.getLocation();
    Direction toLocalCompanions = loc.directionTo(meanLocation(companions));
    if(toLocalCompanions == null) return dir;
    return modifyDirection(dir, toLocalCompanions, cohesion);
  }

  //Try to go in the same direction as companions/flockmates
  private Direction applyAlignment(Direction dir, RobotInfo[] companions) {
    Direction[] companionDirections = estimateCompanionDirections(companions);
    return modifyDirection(dir, meanDirection(companionDirections), alignment);
  }

  private void printDirections(Direction[] dirs){
    StringBuffer b = new StringBuffer("DIRECTIONS: [");
    for(int d = 0; d < dirs.length; d++){
      b.append((dirs[d] == null ? "null" : dirs[d].radians));
      b.append(", ");
    }
    b.append("]");
    System.out.println(b.toString());
  }

  private Direction meanDirection(Direction[] dirs){
    if(dirs.length == 0) return new Direction(0f);
    float radianSum = Arrays.
      stream(dirs).
      map(d -> d.radians).
      <Float>collect(
          () -> new Float(0),
          (Float sum, Float radians) -> new Float(sum.floatValue() + radians.floatValue()),
          (Float sum, Float otherSum) -> {}
          ).floatValue();
      //mapToDouble(d -> d.radians).
      //sum();
    return new Direction(0f).
      rotateRightDegrees(radianSum/(float)dirs.length);
  }

  private void printCompanionLocationsMap(Map<Integer, MapLocation> map){
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

  private Direction[] estimateCompanionDirections(RobotInfo[] companions){
    //printCompanionLocationsMap(this.previousCompanionLocations);
    //Direction is null if there's been no movement so
    //we need to filter them out
    Stream<Direction> stream = Arrays
      .stream(companions)
      .filter(c -> this.previousCompanionLocations.containsKey(c.ID))
      .map(c -> {
        MapLocation prev = this.previousCompanionLocations.get(c.ID);
        MapLocation current = c.getLocation();
        //System.out.println("PREVIOUS: "+prev.toString()+" CURRENT: "+current.toString()+" DIRECTION: "+prev.directionTo(current));
        return prev.directionTo(current);
      })
      .filter(d -> d != null);
    Direction[] dirs = stream.toArray(Direction[]::new);
    //printDirections(dirs);
    return dirs;

    //Direction[] dirs = new Direction[companions.length];
    //RobotInfo comp;
    //MapLocation previousLocation;
    //for(int d = 0; d < dirs.length; d++){
    //  comp = companions[d];
    //  if(!this.previousCompanionLocations.containsKey(comp.getId())){
    //    dirs[d] = 0;
    //    continue;
    //  }
    //  previousLocation = this.previousCompanionLocations.get(comp.getId());
    //  dirs[d] = previousLocation.directionTo(comp.getLocation());
    //}
    //return dirs;
  }

  //TODO: This could also be streamed
  private RobotInfo[] nearbyCompanions(){
    ArrayList<RobotInfo> companions = new ArrayList<RobotInfo>();
    RobotInfo[] robots = this.robotController.senseNearbyRobots();
    for(int r = 0; r < robots.length; r++){
      if(robots[r].getTeam() != this.team) continue;
      if(robots[r].getType() != this.groupingType) continue;
      companions.add(robots[r]);
    }
    return companions.toArray(new RobotInfo[companions.size()]);
  }

  //TODO: This could also be streamed
  private MapLocation meanLocation(RobotInfo[] robots){
    MapLocation myLocation = this.robotController.getLocation();
    float meanX = myLocation.x;
    float meanY = myLocation.y;
    MapLocation loc;
    for(int r = 0; r < robots.length; r++){
      loc = robots[r].getLocation();
      meanX += loc.x;
      meanY += loc.y;
    }
    return new MapLocation(
        meanX/robots.length, meanY/robots.length
        );
  }

  private void moveToRandomLocation() {
    if(!traveller.hasDestination() || traveller.hasReachedDestination()){
      Direction dir = new Direction(
          (float)Math.random() * 2 * (float)Math.PI
          );
      float distance = (float)Math.random() * this.range;
      MapLocation newLocation = this.
        robotController.
        getLocation().
        add(dir, distance);
      traveller.setDestination(newLocation);
    }
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {}
  public void onFailingToReachDestination(MapLocation destination) {}
  public void onReachingDestinationNode(MapLocation destination) {}
  public void onFailingToReachDestinationNode(MapLocation destination) {}
}
