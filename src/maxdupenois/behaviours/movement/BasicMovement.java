package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.GameActionException;

public strictfp class BasicMovement implements MovementInterface {
  private float closeEnoughDistance;
  //Maximum diversions we'll allow us to take to get
  //to a location
  private int maximumNodesToDestination;
  private MapLocation[] destination;
  private int destinationNodePointer;
  private MoverInterface mover;

  public BasicMovement(MoverInterface mover){
    this(mover, 10, 5f);
  }
  public BasicMovement(MoverInterface mover, int maximumNodesToDestination){
    this(mover, maximumNodesToDestination, 5f);
  }
  public BasicMovement(MoverInterface mover, int maximumNodesToDestination, float closeEnoughDistance){
    this.maximumNodesToDestination = maximumNodesToDestination;
    this.closeEnoughDistance = closeEnoughDistance;
    this.destination = new MapLocation[this.maximumNodesToDestination];
    this.destinationNodePointer = 0;
    this.mover = mover;
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

      //senseNearbyRobots, senseNearbyTrees
      // We might be being blocked, we can't see the whole
      // map so a proper A* won't really work
      //
      //For now we're checking multiple locations, but maybe
      //sensing would be better
      //TreeInfo[] trees = this.rc.senseNearbyTrees()
      //RobotInfo[] robots = this.rc.senseNearbyRobots()
      //MapLocation[] blockers = new MapLocation[trees.length + robots.length];
      //for(int i =0; i < trees.length; i++) { blockers[i] = trees[i].getLocation() }
      //for(int i =0; i < robots.length; i++) { blockers[trees.length + i] = trees[i].getLocation() }
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

  //TODO: Refactor out the mid code returns
  public void continueToDestination() throws GameActionException {
    if(this.hasReachedDestination()) return;
    if(this.mover.hasMoved()) return;
    MapLocation currentLocation = this.mover.getCurrentLocation();
    MapLocation currentDestinationNode = this.destination[this.destinationNodePointer];

    Direction currentDirection = currentLocation.directionTo(currentDestinationNode);
    float strideRadius = this.mover.getStrideRadius();

    // The destination is basically a queue that
    // we add to if we're blocked and remove from when
    // we hit each point
    if(currentLocation.isWithinDistance(currentDestinationNode, this.closeEnoughDistance)){
      this.mover.onReachingDestinationNode(currentDestinationNode);
      this.destination[this.destinationNodePointer] = null;
      this.destinationNodePointer = Math.max(0, this.destinationNodePointer - 1);
      if(this.hasReachedDestination()) this.mover.onReachingDestination(currentDestinationNode);
      return;
    }
    // The robot controller automagically scales this, but I'm not sure
    // I can rely on that otherwise the strategy planning becomes difficult
    MapLocation scaledDestination = currentLocation.add(currentDirection, strideRadius);
    if(this.mover.canMove(scaledDestination)) {
      this.mover.move(scaledDestination);
    } else {
      this.mover.onFailingToReachDestinationNode(scaledDestination);
      MapLocation newDestination = null;
      int nextNode = this.destinationNodePointer + 1;

      //We've already made too many diversions
      if(nextNode >= this.maximumNodesToDestination){
        MapLocation originalAimedFor = this.destination[0];
        this.clearDestination();
        this.mover.onFailingToReachDestination(originalAimedFor);
        return;
      }
      //
      // lets assume I have a direction heading towards my
      // current destination, currently one of the above is blocking that direction
      // I need a new location that is not going too far back on myself but is
      // avoiding these blockers, let's try rotating clockwise and scoring each direction
      // if there's no blockers then the closer the new direction is to the target then
      Direction newDirection;
      MapLocation potentialDestination;
      int maxScore = -1;
      int score;
      int scoreThreshold = 160;
      int degreeDifference =5;
      int degrees = degreeDifference;
      //Bail if we get a 'good enough' score
      while(degrees < 360 && maxScore < scoreThreshold){
        score = 0;
        newDirection = currentDirection.rotateRightDegrees(degrees);
        potentialDestination = currentLocation.add(newDirection, strideRadius);
        // descending from 0 degrees off, worst at 180, improving again up to 360
        // f(x) = abs(180 - x)
        if(this.mover.canMove(potentialDestination)) {
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
        // clear destination
        clearDestination();
        this.mover.onFailingToReachDestination(originalAimedFor);
      }
    }
  }


}
