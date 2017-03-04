package swarming.robots;
import maxdupenois.behaviours.movement.SimpleMoverBehaviour;
import maxdupenois.behaviours.movement.SimpleRandomMovementBehaviour;
import maxdupenois.util.Trees;
import static maxdupenois.util.GeometryUtil.randomDestination;
import static maxdupenois.util.GeometryUtil.normaliseRadians;
import battlecode.common.*;

import java.util.Arrays;
import java.util.ArrayList;
import maxdupenois.util.Debug;

public strictfp class Gardener extends Robot {
  private int buildIndex;
  private SimpleMoverBehaviour mover;
  private Direction buildDirection;
  private int startRound;
  private static int TARGET_NUM_TREES = 5;
  private static int MOVEMENT_ROUND_COUNT = 20;
  // Ignore tanks and lumberjacks for the moment
  private RobotType[] buildQueue = new RobotType[]{
    RobotType.SCOUT, RobotType.SOLDIER, RobotType.SOLDIER
  };

  public Gardener(RobotController rc){
    super(rc);
    this.mover = new SimpleMoverBehaviour(rc);
    this.buildDirection = Direction.EAST;
    //Start with a simpe random movement, we'll
    //switch to the simple mover later
    setMovementBehaviour(new SimpleRandomMovementBehaviour(rc, 40f));
    this.startRound = -1;
    this.buildIndex = 0;
  }

  private RobotType currentBuildItem(){
    return buildQueue[buildIndex];
  }

  private void advanceBuildQueue(){
    this.buildIndex = (buildIndex + 1 < buildQueue.length ? buildIndex + 1 : 0);
  }

  // The gardener is our main spawner but also our resource
  // producer, ideally we want them planting (or claiming)
  // and creating new robots pretty constantly
  void takeTurn(int round, int remainingBytecodes) throws GameActionException {
    if(startRound < 0) startRound = round;

    // Don't start trying to garden till we've
    // moved around a bit
    int roundsPassed = round - startRound;
    if(roundsPassed < MOVEMENT_ROUND_COUNT) return;
    if(roundsPassed == MOVEMENT_ROUND_COUNT){
      setMovementBehaviour(mover);
    }
    debug_buildDirection();
    build();
    garden();
    buyVictoryPoints();
  }

  private static int MIN_BULLETS_TO_DONATE = 100;
  private void buyVictoryPoints() throws GameActionException {
    // Only do this if I have at least 100 bullets
    float bullets = rc.getTeamBullets();
    if(bullets < MIN_BULLETS_TO_DONATE) return;
    float cost = rc.getVictoryPointCost();
    // never spend more than a third of my bullets
    // because why not;
    float maxSpend = bullets / 3f;
    float actSpend = maxSpend - ( maxSpend % cost );
    rc.donate(actSpend);
  }

  private void debug_buildDirection(){
    rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(buildDirection, 10f), 100, 100,255);
  }

  //Let's have gardeners build similar
  //to sam's trees but not with the requirement
  //that they have a blank space from which to work
  private void garden() throws GameActionException {
    Trees trees = new Trees(rc);
    if(!trees.hasMyInteractableTrees()){
      plantNextTree(new TreeInfo[0]);
      return;
    }

    TreeInfo[] interactableTrees = trees.getMyInteractableTrees();

    //Plant till we have enough trees
    if(interactableTrees.length < TARGET_NUM_TREES){
      plantNextTree(interactableTrees);
    }

    if(rc.canWater()){
      TreeInfo lowestHealthTree = interactableTrees[0];
      for(int t=1; t < interactableTrees.length; t++){
        if(interactableTrees[t].health < lowestHealthTree.health){
          lowestHealthTree = interactableTrees[t];
        }
      }
      // Should be true but paranoia
      if(rc.canWater(lowestHealthTree.ID)) rc.water(lowestHealthTree.ID);
    }
  }


  private void plantNextTree(TreeInfo[] currentTrees) throws GameActionException {
    MapLocation myLocation = rc.getLocation();

    if(currentTrees.length == 0){
      //Try North, west, etc.
      Direction[] directions = new Direction[]{
        Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST
      };
      Direction dir = null;
      for(int d = 0; d < directions.length && dir == null; d++){
        if(rc.canPlantTree(directions[d])) dir = directions[d];
      }
      //meh
      if(dir == buildDirection) this.buildDirection = buildDirection.rotateRightDegrees(45);

      if(dir != null) {
        rc.plantTree(dir);
      } else {
        moveTo(randomDestination(rc.getLocation(), 10f));
      }
    } else {
      Direction[] currentTreeDirections = Arrays
        .stream(currentTrees)
        .map((t) -> myLocation.directionTo(t.getLocation()))
        .sorted((d1, d2) -> Float.compare(
              normaliseRadians(d1.radians),
              normaliseRadians(d2.radians)
              ))
        .toArray(Direction[]::new);
      ArrayList<Direction> spaces = new ArrayList<Direction>();

      //These have been sorted from lowest to highest
      //So we need spaces of width at least 1 to build something
      //Triangles, unfortunately equilaterals not right angles
      //so don't get to sohcahtoa it up
      //On train and can't remember how this works without
      //sohcatoa, fortunately an equilateral is two identical
      //right angled triangles, assuming we need a space of 1
      //we have a hypoteneuse of 1 and an opposite side of 0.5
      //where o = min required angle to fit in a tree or a robot
      //sin (o/2) = 0.5/1
      //o = 2 * (sin^(-1) 0.5)
      float requiredGap = 2f * (float)Math.asin(0.5f);
      float gapMidpoint = (float)(requiredGap/2f);
      Direction previousDirection = currentTreeDirections[0];
      Direction treeDirection;
      float radianGap;
      int spaceCount;
      for(int d = 1; d < currentTreeDirections.length; d++){
        treeDirection = currentTreeDirections[d];
        radianGap = normaliseRadians(treeDirection.radians) - normaliseRadians(previousDirection.radians) - requiredGap;
        spaceCount = (int)Math.floor(radianGap / requiredGap);

        for(int s = 0; s < spaceCount; s++){
          spaces.add(new Direction(
              previousDirection.radians
              + (requiredGap * (s + 1))
              ));
        }

        previousDirection = treeDirection;
      }

      // Final space is last to first, as this may go past
      // the 360 we need to check
      float first = normaliseRadians(currentTreeDirections[0].radians);
      float last = normaliseRadians(currentTreeDirections[currentTreeDirections.length - 1].radians);
      // minus required gap as half of it is made up by one
      // half of first and the other by the last
      if(last > first){
        radianGap = ((float)Math.PI * 2f - last) + first - requiredGap;
      } else if(last == first){
        //If there's only one
        radianGap = (float)Math.PI * 2f - requiredGap;
      } else {
        radianGap = (first - last) - requiredGap;
      }

      // System.out.println("RADIAN GAP: "+radianGap);
      float spaceDivision = radianGap / requiredGap;
      //Close enough to use round
      if(Math.abs(spaceDivision - Math.round(spaceDivision)) < 0.0001){
        spaceCount = (int)Math.round(spaceDivision);
      } else {
        spaceCount = (int)Math.floor(spaceDivision);
      }

      for(int s = 0; s < spaceCount; s++){
        spaces.add(new Direction(
            last + (requiredGap * (s + 1f))
            ));
      }

      Direction[] spaceArray = spaces.toArray(new Direction[spaces.size()]);

      if(spaceArray.length > 0){
        //ensure that we defnitely have a build direction
        this.buildDirection = spaceArray[0];
        if(spaceArray.length > 1 && rc.canPlantTree(spaceArray[1])){
          rc.plantTree(spaceArray[1]);
        }
      } else {
        //Damn we've lost our build space, this shouldn't happen
        System.out.println("AARGH lost build space, attempting to go anywhere else");
        //moveTo(randomDestination(rc.getLocation(), 10f));
      }
    }
  }

  private float toDegrees(float radians){
    return normaliseRadians(radians) * (360f/((float)Math.PI * 2f));
  }

  private String dirString(Direction dir){
    float normalised = normaliseRadians(dir.radians);
    return "rads="+normalised+" deg="+toDegrees(normalised);
  }

  private boolean isMoving(){
    return mover.isMoving();
  }

  private void moveTo(MapLocation location){
    mover.moveTo(location);
  }

  private void build() throws GameActionException {
    if(!this.rc.isBuildReady()) return;
    RobotType type = currentBuildItem();
    if(this.rc.canBuildRobot(type, buildDirection)){
      this.rc.buildRobot(type, buildDirection);
      advanceBuildQueue();
    }
  }
}
