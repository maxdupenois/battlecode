package swarming;
import battlecode.common.*;
import swarming.robots.*;

import java.util.Random;

public strictfp class RobotPlayer {
    static RobotController rc;


    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

      //Looks like the seed might be constant
      //and its pissing me off so I'm going
      //to see if I can screw with it
      //Random rand = new Random((long)rc.getID());
      //float someRandom;
      //for(int i = rand.nextInt(10); i >= 0; i--){
      //  someRandom = (float) Math.random();
      //}
      // welp ^ that didn't work as the ids didn't change

      Robot robot;
      switch(rc.getType()){
        case ARCHON:
          robot = new Archon(rc);
          break;
        case SCOUT:
          robot = new Scout(rc);
          break;
        case SOLDIER:
          robot = new Soldier(rc);
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
