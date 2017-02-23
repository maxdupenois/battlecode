package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class Traveller {
  private float closeEnoughDistance;
  //Maximum diversions we'll allow us to take to get
  //to a location
  private int maximumNodesToDestination;
  private MapLocation[] destination;
  private int destinationNodePointer;
  private TravellerEventInterface eventSubscriber;
  private RobotController robotController;
  private float strideRadius;

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
  }

  public void clearDestination(){
    setDestination(null);
  }

  public void setDestination(MapLocation destination){
    this.destination = new MapLocation[this.maximumNodesToDestination];
    this.destinationNodePointer = 0;
    this.destination[0] = destination;
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

    // The destination is basically a queue that
    // we add to if we're blocked and remove from when
    // we hit each point
    if(currentLocation.isWithinDistance(currentDestinationNode, this.closeEnoughDistance)){
      this.eventSubscriber.onReachingDestinationNode(currentDestinationNode);
      this.destination[this.destinationNodePointer] = null;
      this.destinationNodePointer = Math.max(0, this.destinationNodePointer - 1);
      if(this.hasReachedDestination()) this.eventSubscriber.onReachingDestination(currentDestinationNode);
      return;
    }

    // The robot controller can automagically scale this,
    // but I'm not sure I can rely on that otherwise the
    // strategy planning becomes difficult (as does the testing)
    MapLocation scaledDestination = currentLocation.add(currentDirection, this.strideRadius);
    if(this.robotController.canMove(scaledDestination)) {
      this.robotController.move(scaledDestination);
    } else {
      this.eventSubscriber.onFailingToReachDestinationNode(scaledDestination);
      MapLocation newDestination = null;
      int nextNode = this.destinationNodePointer + 1;

      //We've already made too many diversions
      if(nextNode >= this.maximumNodesToDestination){
        MapLocation originalAimedFor = this.destination[0];
        this.clearDestination();
        this.eventSubscriber.onFailingToReachDestination(originalAimedFor);
        return;
      }

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
        // f(x) = abs(180 - x)
        if(this.robotController.canMove(potentialDestination)) {
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
      } else {
        //irreparably blocked
        MapLocation originalAimedFor = this.destination[0];
        this.clearDestination();
        this.eventSubscriber.onFailingToReachDestination(originalAimedFor);
      }
    }
  }

  //public void printDestinationList(){
  //  StringBuffer mem = new StringBuffer();
  //  mem.append("(");
  //  for(int x =0; x < this.destination.length; x++){
  //    mem.append(this.destination[x] == null ? "" : this.destination[x].toString());
  //    mem.append(", ");
  //  }
  //  mem.append(")");
  //  System.out.println("---> Destination List "+mem.toString());
  //  System.out.println("---> Node Pointer "+this.destinationNodePointer);
  //}

}
