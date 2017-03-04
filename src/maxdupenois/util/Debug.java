package maxdupenois.util;

import battlecode.common.BodyInfo;
import battlecode.common.MapLocation;
import battlecode.common.Direction;


public strictfp class Debug {
  public static void debug_out(String message){
    debug_out(message, "---> ");
  }

  public static void debug_out(String message, String prepend){
    System.out.println("  "+prepend+message);
  }
}
