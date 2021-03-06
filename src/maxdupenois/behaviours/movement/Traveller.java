package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import maxdupenois.util.Debug;

public strictfp class Traveller {
  private static int MAX_STUCK_COUNT = 5;
  private float closeEnoughDistance;
  private MapLocation endDestination;
  private MapLocation destination;
  // Was hoping to avoid this but
  // we can end up in a state
  // where we go back and forth between locations
  // we've already hit
  private ArrayList<MapLocation> locationHistory;
  private ArrayList<TravellerEventInterface> eventSubscribers;
  private RobotController robotController;
  private float strideRadius;
  private boolean showDebug;
  private int robotID;
  private int stuckCount;

  public Traveller(RobotController robotController){
    this(robotController, 5f);
  }

  public Traveller(
      RobotController robotController,
      float closeEnoughDistance
      ){
    this.closeEnoughDistance = closeEnoughDistance;
    this.destination = null;
    this.endDestination = null;
    this.eventSubscribers = new ArrayList<TravellerEventInterface>();
    this.robotController = robotController;
    this.strideRadius = this.robotController.getType().strideRadius;
    this.showDebug = false;
    this.locationHistory = new ArrayList<MapLocation>();
    this.robotID = this.robotController.getID();
  }

  public void subscribe(TravellerEventInterface subscriber){
    eventSubscribers.add(subscriber);
  }

  public void unsubscribe(TravellerEventInterface subscriber){
    eventSubscribers.remove(subscriber);
  }

  // Used in testing so we're not beholden to
  // an actual robot type's radius
  public void setStrideRadius(float strideRadius){
    this.strideRadius = strideRadius;
  }

  public boolean isDiverted(){
    if(this.destination == null) return false;
    return (!this.destination.equals(this.endDestination));
  }

  public MapLocation getDestination(){
    return this.endDestination;
  }

  public void clearDestination(){
    setDestination(null);
  }

  public void setDestination(MapLocation destination){
    this.destination = destination;
    this.endDestination = destination;
    this.locationHistory.clear();
  }

  public boolean hasNoDestinationOrHasFinished(){
    return !hasDestination() || hasReachedDestination();
  }

  public boolean hasDestination(){
    return this.destination != null;
  }

  public boolean hasReachedDestination(){
    return (
        this.destination == null &&
        this.endDestination != null
        );
  }

  public Direction getDirection(){
    if(!hasDestination()) return null;
    return robotController.
        getLocation().
        directionTo(destination);
  }

  // We can't see enough of the map without prohibitive cost
  // to do something like A* but we still might be blocked.
  // This has a pretty naive approach of attempting to move
  // towards the destination with only small deviations
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
    if(!this.hasDestination()) return;
    if(this.hasReachedDestination()) return;
    if(this.robotController.hasMoved()) return;
    MapLocation currentLocation = this.robotController.getLocation();

    Direction currentDirection = currentLocation.directionTo(destination);
    debug_out("Looking to reach "+destination.toString(), "**");

    // This will switch out the diversion if we're close enough
    // and will let us finish if we hit the actual destination
    if(closeEnoughCheck(currentLocation, destination)) return;

    // Scale the node based on our stride radius
    MapLocation scaledDestination = scaleDestination(
        currentLocation, destination, currentDirection);
    debug_out("scaled destination "+scaledDestination.toString());

    if(this.robotController.canMove(scaledDestination)) {
      stuckCount = 0;
      debug_out(":) Moving To "+scaledDestination.toString());
      locationHistory.add(currentLocation);
      this.robotController.move(scaledDestination);
    } else {
      stuckCount++;
      if(stuckCount > MAX_STUCK_COUNT){
        completeFailure();
      } else {
        debug_out(":( Looking for diversion");
        findNewNode(currentLocation, scaledDestination, currentDirection);
      }
    }
  }

  private void findNewNode(
      MapLocation currentLocation, MapLocation scaledDestination, Direction currentDirection) throws GameActionException{
    notifyOfNeedToDivert(scaledDestination);
    if(!this.robotController.onTheMap(scaledDestination)) notifyOfMapBoundaryFound(scaledDestination);
    MapLocation newDestination = null;

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
      notifyOfDiversion(newDestination);
      this.destination = newDestination;
      debug_out("Found diversion "+newDestination.toString()+" with score "+maxScore);
      debug_printDestinations();
    } else {
      completeFailure();
    }
  }

  private void completeFailure(){
    //irreparably blocked
    MapLocation originalAimedFor = this.endDestination;
    debug_out("Failed to hit destination completely, clearing");
    this.clearDestination();
    notifyOfFailingToReachDestination(originalAimedFor);
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

  // Check to see if we're close enough to the current destination
  // and aim for end destination if we've meen diverted
  private boolean closeEnoughCheck(
      MapLocation currentLocation,
      MapLocation currentDestination){
    if(!isCloseEnough(currentLocation, currentDestination)) return false;

    if(this.isDiverted()){
      //end diversion
      this.destination = this.endDestination;
      notifyOfReachingDiversion(currentDestination);
    } else {
      //end destination reached
      this.destination = null;
      notifyOfReachingDestination(currentDestination);
    }
    debug_out("Close enough to "+currentDestination);
    debug_printDestinations();

    return true;
  }

  private void notifySubscribers(Consumer<TravellerEventInterface> func){
    Iterator<TravellerEventInterface> iter = eventSubscribers.iterator();
    while(iter.hasNext()) func.accept(iter.next());
  }

  private void notifyOfNeedToDivert(MapLocation location){
    notifySubscribers((sub) -> sub.onNeedingToDivert(location));
  }

  private void notifyOfMapBoundaryFound(MapLocation location){
    notifySubscribers((sub) -> sub.onMapBoundaryFound(location));
  }

  private void notifyOfReachingDestination(MapLocation location){
    notifySubscribers((sub) -> sub.onReachingDestination(location));
  }

  private void notifyOfFailingToReachDestination(MapLocation location){
    notifySubscribers((sub) -> sub.onFailingToReachDestination(location));
  }

  private void notifyOfReachingDiversion(MapLocation location){
    notifySubscribers((sub) -> sub.onReachingDiversion(location));
  }

  private void notifyOfDiversion(MapLocation location){
    notifySubscribers((sub) -> sub.onDiversion(location));
  }

  public void debug_printDestinations(){
    if(!this.showDebug) return;
    StringBuffer mem = new StringBuffer();
    mem.append("(");
    if(isDiverted()){
      mem.append(this.destination.toString());
      mem.append(", ");
    }
    mem.append(this.endDestination.toString());
    mem.append(")");
    debug_out("Destination List "+mem.toString());
  }


  public void debug_dbgOff(){
    this.showDebug = false;
  }

  public void debug_dbgOn(){
    this.showDebug = true;
  }

  public void debug_out(String message){
    if(!showDebug) return;
    Debug.debug_out(message);
  }

  public void debug_out(String message, String prepend){
    if(!showDebug) return;
    Debug.debug_out(message, prepend);
  }
}
