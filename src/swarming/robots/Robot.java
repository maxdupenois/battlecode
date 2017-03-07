package swarming.robots;
import maxdupenois.behaviours.Behaviour;
import java.util.ArrayList;
import java.util.Iterator;
import battlecode.common.*;

public abstract strictfp class Robot {
  protected RobotController rc;
  protected MapLocation currentLocation;
  protected RobotType type;
  protected ArrayList<Behaviour> behaviours;

  public Robot(RobotController rc){
    this.rc = rc;
    this.type = rc.getType();
    this.behaviours = new ArrayList<Behaviour>();
  }

  public void setRobotController(RobotController rc) {
    this.rc = rc;
  }

  public RobotController getRobotController() {
    return this.rc;
  }

  public void removeBehaviour(Behaviour b){
    this.behaviours.remove(b);
  }

  public void addBehaviour(Behaviour b){
    this.behaviours.add(b);
  }

  public void switchBehaviour(Behaviour oldB, Behaviour newB){
    removeBehaviour(oldB);
    addBehaviour(newB);
  }

  private void runBehaviours() throws GameActionException {
    Iterator<Behaviour> iter = behaviours.iterator();
    while(iter.hasNext()){
      iter.next().execute();
    }
  }

  public void run() throws GameActionException {
    int remainingBytecodes;
    while(true) {
      this.currentLocation = this.rc.getLocation();
      runBehaviours();
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
