package maxdupenois.behaviours.movement;

import static maxdupenois.util.GeometryUtil.*;
import battlecode.common.MapLocation;
import battlecode.common.Direction;

import java.util.Arrays;

public strictfp class MovementUtil {
  // Used as a starting point for any direction
  // modifications we need to make, first check to
  // see if we have an actual direction and pick a
  // random one if we don't
  public static Direction baseDirection(Traveller traveller){
    Direction dir = traveller.getDirection();
    if(dir != null) return dir;
    return randomDirection();
  }

}
