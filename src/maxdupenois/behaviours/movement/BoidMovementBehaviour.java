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
  private float alignment = 0.8f;
  // head towards center of mass
  private float cohesion = 0.5f;

  private Map<Integer, MapLocation> previousCompanionLocations;

  public BoidMovementBehaviour(RobotController robotController, RobotType groupingType, float range){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
    this.groupingType = groupingType;
    this.team = robotController.getTeam();
    this.range = range;
    this.previousCompanionLocations = new HashMap<Integer, MapLocation>();
  }

  private class MapCollector implements Collector<RobotInfo, Map<Integer, MapLocation>, Map<Integer, MapLocation>> {
    public Supplier<Map<Integer, MapLocation>> supplier(){
      return () -> new HashMap<Integer, MapLocation>();
    }

    public BiConsumer<Map<Integer, MapLocation>, RobotInfo> accumulator(){
      return (map, ri) -> map.put(ri.ID, ri.getLocation());
    }

    public BinaryOperator<Map<Integer, MapLocation>> combiner(){
      return (a, b) -> a;
    }

    public Function<Map<Integer, MapLocation>, Map<Integer, MapLocation>> finisher(){
      return (a) -> a;
    }

    public Set<Collector.Characteristics> characteristics(){
      Set<Collector.Characteristics> set = new HashSet<Collector.Characteristics>();
      set.add(Collector.Characteristics.UNORDERED);
      return set;
    }
  }

  public void move(){
    RobotInfo[] companions = nearbyCompanions();
    if(companions.length == 0){
      moveToRandomLocation();
    } else {
      // Will need a better base direction
      Direction dir = new Direction(0);
      dir = applyCohesion(dir, companions);
      dir = applyAlignment(dir, companions);
      dir = applySeparation(dir, companions);
      MapLocation loc = this.robotController.getLocation();
      traveller.setDestination(loc.add(dir, this.range));
    }

    try {
      traveller.continueToDestination();
    } catch (GameActionException ex) {
      System.err.println(ex.getMessage());
      //TODO: Consider where you actually want to catch this
    }
    //this.previousCompanionLocations.clear();
    //for(int i=0; i < companions.length; i++){
    //  this.previousCompanionLocations.put(
    //      companions[i].ID, companions[i].getLocation()
    //      );
    //}
    //Collector<RobotInfo, ?, Map<Integer, MapLocation>> collector;
    //collector = Collector.of(
    //    () -> new HashMap<Integer, MapLocation>(),
    //    (mem, ri) -> mem.put(ri.ID, ri.getLocation()),
    //    (a, b) -> a
    //    );

    this.previousCompanionLocations = Arrays.
      stream(companions).
      <Map<Integer, MapLocation>>collect(
          () -> new HashMap<Integer, MapLocation>(),
          (Map<Integer, MapLocation> map, RobotInfo ri) -> map.put(ri.ID, ri.getLocation()),
          (Map<Integer, MapLocation> a, Map<Integer, MapLocation> b) -> {}
          );
    //Collectors.toMap(
    //        ri -> ri.ID,
    //        RobotInfo::getLocation,
    //        (a, b) -> a
    //        )
  }

  private Direction modifyDirection(Direction original, Direction modifier, float amount){
    return original.rotateRightDegrees(modifier.radians * amount);
  }

  //Try to avoid crowding companions/flockmates
  private Direction applySeparation(Direction dir, RobotInfo[] companions) {
    MapLocation loc = this.robotController.getLocation();
    Direction toLocalCompanions = loc.directionTo(meanLocation(companions));
    return modifyDirection(dir, toLocalCompanions.opposite(), separation);
  }

  //Try to move towards the centre of mass of companions/flockmates
  private Direction applyCohesion(Direction dir, RobotInfo[] companions){
    MapLocation loc = this.robotController.getLocation();
    return modifyDirection(dir, loc.directionTo(meanLocation(companions)), cohesion);
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
  //private void <T> printArray(T[] arr, Function<T> printer){
  //}

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
