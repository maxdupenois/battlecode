package maxdupenois.robots;
import battlecode.common.*;

public abstract strictfp class Robot {
  protected static float CLOSE_ENOUGH_DISTANCE = 0.5f;
  //Maximum diversions we'll allow us to take to get
  //to a location
  protected static int MAXIMUM_NODES_TO_DESTINATION = 10;
  protected RobotController rc;
  protected MapLocation currentLocation;
  protected RobotType type;
  private MapLocation[] destination = new MapLocation[MAXIMUM_NODES_TO_DESTINATION];
  private int destinationNodePointer = 0;

  public Robot(RobotController rc){
    this.rc = rc;
    this.type = rc.getType();
  }

  public void setRobotController(RobotController rc) {
    this.rc = rc;
  }

  public RobotController getRobotController() {
    return this.rc;
  }

  public void run() throws GameActionException {
    int remainingBytecodes;
    while(true) {
      this.currentLocation = this.rc.getLocation();
      remainingBytecodes = Clock.getBytecodesLeft();
      if(remainingBytecodes > 0){
        // No point if there's nothing left that can be done
        takeTurn(this.rc.getRoundNum(), remainingBytecodes);
      }
      // Close out the turn
      Clock.yield();
    }
  }

  void takeTurn(int round, int remainingBytecodes) throws GameActionException {}
  void onReachingDestination(MapLocation destination) {}
  void onFailingDestination(MapLocation destination) {}

  protected void setDestination(MapLocation destination){
    this.destination = new MapLocation[MAXIMUM_NODES_TO_DESTINATION];
    this.destinationNodePointer = 0;
    this.destination[0] = destination;
  }

  protected boolean hasReachedDestination(){
    return this.destination[this.destinationNodePointer] == null;
  }

  //TODO: Refactor out the mid code returns
  protected void continueToDestination() throws GameActionException {
    if(this.hasReachedDestination()) return;
    if(this.rc.hasMoved()) return;

    MapLocation currentDestinationNode = this.destination[this.destinationNodePointer];

    // The destination is basically a queue that
    // we add to if we're blocked and remove from when
    // we hit each point
    if(this.currentLocation.isWithinDistance(currentDestinationNode, CLOSE_ENOUGH_DISTANCE)){
      this.destination[this.destinationNodePointer] = null;
      this.destinationNodePointer = Math.max(0, this.destinationNodePointer - 1);
      if(this.hasReachedDestination()) this.onReachingDestination(currentDestinationNode);
      return;
    }
    if(this.rc.canMove(currentDestinationNode)) {
      this.rc.move(currentDestinationNode);
    } else {
      MapLocation newDestination = null;
      int nextNode = this.destinationNodePointer + 1;

      //We've already made too many diversions
      if(nextNode >= MAXIMUM_NODES_TO_DESTINATION){
        MapLocation originalAimedFor = this.destination[0];
        // clear destination
        this.setDestination(null);
        this.onFailingDestination(originalAimedFor);
        return;
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
      //
      // lets assume I have a direction heading towards my
      // current destination, currently one of the above is blocking that direction
      // I need a new location that is not going too far back on myself but is
      // avoiding these blockers, let's try rotating clockwise and scoring each direction
      // if there's no blockers then the closer the new direction is to the target then
      Direction currentDirection = this.currentLocation.directionTo(currentDestinationNode);
      Direction newDirection;
      MapLocation potentialDestination;
      int maxScore = -1;
      int score;
      int scoreThreshold = 160;
      int degrees = 10;
      //Bail if we get a 'good enough' score
      while(degrees < 360 && maxScore < scoreThreshold){
        score = 0;
        newDirection = currentDirection.rotateRightDegrees(degrees);
        potentialDestination = this.currentLocation.add(newDirection, this.type.strideRadius);
        // descending from 0 degrees off, worst at 180, improving again up to 360
        // f(x) = abs(180 - x)
        if(this.rc.canMove(potentialDestination)) {
          score = Math.abs(180 - degrees);
        } else {
          score = -1;
        }

        if(score > maxScore){
          maxScore = score;
          newDestination = potentialDestination;
        }
        degrees += 10;
      }
      if(newDestination != null) {
        this.destination[nextNode] = newDestination;
        this.destinationNodePointer = nextNode;
      } else {
        //irreparably blocked
        MapLocation originalAimedFor = this.destination[0];
        // clear destination
        this.setDestination(null);
        this.onFailingDestination(originalAimedFor);
      }
    }
  }

  public Direction randomDirection() {
    return new Direction((float)Math.random() * 2 * (float)Math.PI);
  }
}
