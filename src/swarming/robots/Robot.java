package swarming.robots;
import maxdupenois.behaviours.Behaviour;
import maxdupenois.behaviours.movement.Traveller;
import java.util.ArrayList;
import java.util.Iterator;
import battlecode.common.*;

public abstract strictfp class Robot {
  protected Traveller traveller;
  protected RobotController rc;
  protected MapLocation currentLocation;
  protected RobotType type;
  protected ArrayList<Behaviour> beforeMoveBehaviours;
  protected ArrayList<Behaviour> afterMoveBehaviours;

  public Robot(RobotController rc){
    this.rc = rc;
    this.type = rc.getType();
    this.beforeMoveBehaviours = new ArrayList<Behaviour>();
    this.afterMoveBehaviours = new ArrayList<Behaviour>();
    this.traveller = new Traveller(rc, 2f);
  }

  public void setRobotController(RobotController rc) {
    this.rc = rc;
  }

  public RobotController getRobotController() {
    return this.rc;
  }

  public void removeBeforeMoveBehaviour(Behaviour b){
    this.beforeMoveBehaviours.remove(b);
  }

  public void addBeforeMoveBehaviour(Behaviour b){
    this.beforeMoveBehaviours.add(b);
  }

  public void switchBeforeMoveBehaviour(Behaviour oldB, Behaviour newB){
    removeBeforeMoveBehaviour(oldB);
    addBeforeMoveBehaviour(newB);
  }

  public void removeAfterMoveBehaviour(Behaviour b){
    this.afterMoveBehaviours.remove(b);
  }

  public void addAfterMoveBehaviour(Behaviour b){
    this.afterMoveBehaviours.add(b);
  }

  public void switchAfterMoveBehaviour(Behaviour oldB, Behaviour newB){
    removeAfterMoveBehaviour(oldB);
    addAfterMoveBehaviour(newB);
  }

  private void runBehaviours(ArrayList<Behaviour> behaviours) throws GameActionException {
    Iterator<Behaviour> iter = behaviours.iterator();
    while(iter.hasNext()){
      iter.next().execute();
    }
  }

  public void run() throws GameActionException {
    int remainingBytecodes;
    while(true) {
      this.currentLocation = this.rc.getLocation();
      runBehaviours(beforeMoveBehaviours);
      if(traveller.hasDestination()) traveller.continueToDestination();
      runBehaviours(afterMoveBehaviours);
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

}
