package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.GameActionException;
import battlecode.common.Direction;
import java.util.ArrayList;
import java.util.Stream;

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
  private float separation = 0.5;
  // same direction
  private float alignment = 0.5;
  // head towards center of mass
  private float cohesion = 0.5;

  private Map<int, MapLocation> previousCompanionLocations;

  public BoidMovementBehaviour(RobotController robotController, RobotType groupingType, float range){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
    this.groupingType = groupingType;
    this.team = robotController.getTeam();
    this.range = range;
    this.previousCompanionLocations = new HashMap<int, MapLocation>;
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
    this.previousCompanionLocations = Array.
      stream(companions).
      collect(Collectors.toMap(
            ri -> ri.getId(),
            ri -> ri.getLocation()
            ));
  }

  private Direction modifyDirection(Direction original, Direction modifier, float amount){
    return original.rotateRightDegrees(modifer.radians * amount);
  }

  //Try to avoid crowding companions/flockmates
  private Direction applySeparation(Direction dir, RobotInfo[] companions) {
    MapLocation loc = this.robotController.getLocation();
    return modifyDirection(dir, loc.directionTo(meanLocation(companions)), separation);
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

  private Direction meanDirection(Direction[] dirs){
    float radianSum = Array.
      stream(dirs).
      mapToFloat(d -> d.radians).
      sum();
    return new Direction(0f).
      rotateRightDegrees(radianSum/(float)dirs.length);
  }

  private Direction[] estimateCompanionDirections(RobotInfo[] companions){
    Direction[] dirs = new Direction[companions.length];
    RobotInfo comp;
    MapLocation previousLocation;
    for(int d = 0; d < dirs.length; d++){
      comp = companions[d];
      if(!this.previousCompanionLocations.containsKey(comp.getId())){
        dirs[d] = 0;
        continue;
      }
      previousLocation = this.previousCompanionLocations.get(comp.getId());
      dirs[d] = previousLocation.directionTo(comp.getLocation());
    }
    return dirs;
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
