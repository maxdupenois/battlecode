package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import java.util.ArrayList;
import java.util.Iterator;

public strictfp class Traveller {
  private float closeEnoughDistance;
  // Maximum diversions we'll allow us to take to get
  // to a location
  private int maximumNodesToDestination;
  private MapLocation[] destination;
  // Was hoping to avoid this but
  // we can end up in a state
  // where we go back and forth between locations
  // we've already hit
  private ArrayList<MapLocation> locationHistory;
  private int destinationNodePointer;
  private TravellerEventInterface eventSubscriber;
  private RobotController robotController;
  private float strideRadius;
  private boolean showDebug;

  public Traveller(
      TravellerEventInterface eventSubscriber,
      RobotController robotController
      ){
    this(eventSubscriber, robotController, 10, 5f);
  }

  public Traveller(
      TravellerEventInterface eventSubscriber,
      RobotController robotController,
      int maximumNodesToDestination
      ){

    this(
      eventSubscriber,
      robotController,
      maximumNodesToDestination,
      5f
      );
  }

  public Traveller(
      TravellerEventInterface eventSubscriber,
      RobotController robotController,
      int maximumNodesToDestination,
      float closeEnoughDistance
      ){
    this.maximumNodesToDestination = maximumNodesToDestination;
    this.closeEnoughDistance = closeEnoughDistance;
    this.destination = new MapLocation[this.maximumNodesToDestination];
    this.destinationNodePointer = 0;
    this.eventSubscriber = eventSubscriber;
    this.robotController = robotController;
    this.strideRadius = this.robotController.getType().strideRadius;
    this.showDebug = false;
    this.locationHistory = new ArrayList<MapLocation>();
  }

  // Used in testing so we're not beholden to
  // an actual robot type's radius
  public void setStrideRadius(float strideRadius){
    this.strideRadius = strideRadius;
  }

  public void debugOff(){
    this.showDebug = false;
  }

  public void debugOn(){
    this.showDebug = true;
  }

  public MapLocation getDestination(){
    return this.destination[0];
  }

  public void clearDestination(){
    setDestination(null);
  }

  public void setDestination(MapLocation destination){
    this.destination = new MapLocation[this.maximumNodesToDestination];
    this.destinationNodePointer = 0;
    this.destination[0] = destination;
    this.locationHistory.clear();
  }

  public boolean hasDestination(){
    return this.destination[0] != null;
  }

  public boolean hasReachedDestination(){
    return this.destination[this.destinationNodePointer] == null;
  }

  // We can't see enough of the map without prohibitive cost
  // to do something like A* but we still might be blocked.
  // This has a pretty naive approach of attempting to move
  // towards the destination with only small deviations
  // The destination is basically a queue that
  // we add to if we're blocked and remove from when
  // we hit each point
  //
  // For now we're checking multiple locations, but maybe
  // sensing would be better e.g.
  // TreeInfo[] trees = this.rc.senseNearbyTrees()
  // RobotInfo[] robots = this.rc.senseNearbyRobots()
  // MapLocation[] blockers = new MapLocation[trees.length + robots.length];
  // for(int i =0; i < trees.length; i++) { blockers[i] = trees[i].getLocation() }
  // for(int i =0; i < robots.length; i++) { blockers[trees.length + i] = trees[i].getLocation() }
  //
  // However for the moment the simple approach seems sensible
  // TODO: Refactor out the mid code returns, generally clean this
  // up
  public void continueToDestination() throws GameActionException {
    if(this.hasReachedDestination()) return;
    if(this.robotController.hasMoved()) return;
    MapLocation currentLocation = this.robotController.getLocation();
    MapLocation currentDestinationNode = this.destination[this.destinationNodePointer];

    Direction currentDirection = currentLocation.directionTo(currentDestinationNode);
    debug("Looking to reach "+currentDestinationNode.toString(), "oooooo\n");

    // This will pop the current node off the queue if we're close enough
    if(closeEnoughCheck(currentLocation, currentDestinationNode)) return;

    // Scale the node based on our stride radius
    MapLocation scaledDestination = scaleDestination(
        currentLocation, currentDestinationNode, currentDirection);
    debug("scaled destination "+scaledDestination.toString());

    if(this.robotController.canMove(scaledDestination)) {
      debug(":) Moving To "+scaledDestination.toString());
      locationHistory.add(currentLocation);
      this.robotController.move(scaledDestination);
    } else {
      debug(":( Looking for new node");
      findNewNode(currentLocation, scaledDestination, currentDirection);
    }
  }

  private void findNewNode(
      MapLocation currentLocation, MapLocation scaledDestination, Direction currentDirection) throws GameActionException{
    this.eventSubscriber.onFailingToReachDestinationNode(scaledDestination);
    if(!this.robotController.onTheMap(scaledDestination)) this.eventSubscriber.onMapBoundaryFound(scaledDestination);
    MapLocation newDestination = null;
    int nextNode = this.destinationNodePointer + 1;

    //While checking it will clear out the journey
    if(checkIfNumberOfDiversionsIsTooMany(nextNode)) return;

    // lets assume I have a direction heading towards my
    // current destination, currently something is
    // blocking that direction I need a new location that
    // is not going too far back on myself but is avoiding
    // these blockers, let's try rotating clockwise and
    // scoring each direction
    Direction newDirection;
    MapLocation potentialDestination;
    int maxScore = -1;
    int score;
    int scoreThreshold = 160;
    int degreeDifference = 5;
    int degrees = degreeDifference;
    //Bail if we get a 'good enough' score
    while(degrees < 360 && maxScore < scoreThreshold){
      score = 0;
      newDirection = currentDirection.rotateRightDegrees(degrees);
      potentialDestination = currentLocation.add(newDirection, this.strideRadius);
      // descending from 0 degrees off,
      // worst at 180, improving again up to 360
      // f(x) = abs(180 - x) however if the
      // destination appears in my list of existing destinations
      // then it's not going to work (we'll end up in a cycle)
      if(this.robotController.canMove(potentialDestination) &&
          !haveAlreadyVisited(potentialDestination)) {
        score = Math.abs(180 - degrees);
      } else {
        score = -1;
      }

      if(score > maxScore){
        maxScore = score;
        newDestination = potentialDestination;
      }
      degrees += degreeDifference;
    }
    if(newDestination != null) {
      this.destination[nextNode] = newDestination;
      this.destinationNodePointer = nextNode;
      debug("Found new node "+newDestination.toString()+" with score "+maxScore);
      debugPrintDestinationList();
    } else {
      //irreparably blocked
      MapLocation originalAimedFor = this.destination[0];
      debug("Failed to hit destination completely, clearing");
      this.clearDestination();
      this.eventSubscriber.onFailingToReachDestination(originalAimedFor);
    }
  }

  private boolean checkIfNumberOfDiversionsIsTooMany(int nextNode){
    if(nextNode < this.maximumNodesToDestination) return false;
    debug("We've diverted too often, bailing");
    MapLocation originalAimedFor = this.destination[0];
    this.clearDestination();
    this.eventSubscriber.onFailingToReachDestination(originalAimedFor);
    return true;
  }

  // The robot controller can automagically scale this,
  // but I'm not sure I can rely on that otherwise the
  // strategy planning becomes difficult (as does the testing)
  private MapLocation scaleDestination(
      MapLocation currentLocation,
      MapLocation currentDestinationNode,
      Direction currentDirection) {
    //Only scale if the distance is greater than the stride
    if(currentLocation.distanceTo(currentDestinationNode) <= this.strideRadius) {
      return currentDestinationNode;
    }
    return currentLocation.add(currentDirection, this.strideRadius);
  }

  private boolean haveAlreadyVisited(MapLocation loc){
    Iterator<MapLocation> iter = locationHistory.iterator();
    MapLocation visited;
    boolean found = false;
    while(!found && iter.hasNext()){
      visited = iter.next();
      found = isCloseEnough(loc, visited);
    }
    return found;
  }

  private boolean isCloseEnough(MapLocation loc1, MapLocation loc2){
    return loc1.isWithinDistance(loc2, this.closeEnoughDistance);
  }
  // Check to see if we're close enough to the current aimed for node
  // and remove it from the queue if we are
  private boolean closeEnoughCheck(
      MapLocation currentLocation, MapLocation currentDestinationNode){
    if(!isCloseEnough(currentLocation, currentDestinationNode)) return false;

    this.eventSubscriber.onReachingDestinationNode(currentDestinationNode);
    this.destination[this.destinationNodePointer] = null;
    this.destinationNodePointer = Math.max(0, this.destinationNodePointer - 1);
    debug("Close enough to "+currentDestinationNode);
    debugPrintDestinationList();

    if(this.hasReachedDestination()) {
      this.eventSubscriber.onReachingDestination(currentDestinationNode);
    }
    return true;
  }


  public void debugPrintDestinationList(){
    if(!this.showDebug) return;
    StringBuffer mem = new StringBuffer();
    mem.append("(");
    for(int x =0; x < this.destination.length; x++){
      mem.append(this.destination[x] == null ? "" : this.destination[x].toString());
      mem.append(", ");
    }
    mem.append(")");
    debug("Destination List "+mem.toString());
    debug("Node Pointer "+this.destinationNodePointer);
  }

  private void debug(String message){
    debug(message, "---> ");
  }

  private void debug(String message, String prepend){
    if(!this.showDebug) return;
    System.out.println(prepend+message);
  }

}
