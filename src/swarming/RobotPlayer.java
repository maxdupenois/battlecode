package swarming;
import battlecode.common.*;
import swarming.robots.*;

public strictfp class RobotPlayer {
    static RobotController rc;


    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

      Robot robot;

      switch(rc.getType()){
        case ARCHON:
          robot = new Archon(rc);
          break;
        case GARDENER:
          robot = new Gardener(rc);
          break;
        default:
          System.err.println("Unrecognised robot type "+rc.getType());
          robot = null;
          Clock.yield();
      }
      if(robot != null) robot.run();
    }

}
