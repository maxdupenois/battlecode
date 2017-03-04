package maxdupenois.util;

import battlecode.common.MapLocation;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;
import battlecode.common.Team;

import java.util.Arrays;
import java.util.ArrayList;

// Used to memoise a bunch of tree shiznit
// we'll need to know;
public strictfp class Trees {
  private RobotController rc;
  private TreeInfo[] nearbyTrees;
  private TreeInfo[] myTrees;
  private TreeInfo[] enemyTrees;
  private TreeInfo[] neutralTrees;
  private TreeInfo[] interactableTrees;
  private TreeInfo[] myInteractableTrees;
  private Team team;
  private MapLocation myLocation;

  public Trees(RobotController rc){
    this.rc = rc;
    this.team = rc.getTeam();
    this.nearbyTrees = null;
    this.enemyTrees = null;
    this.neutralTrees = null;
    this.interactableTrees = null;
    this.myTrees = null;
    this.myInteractableTrees = null;
    this.myLocation = rc.getLocation();
  }

  public boolean hasMyTrees(){
    return getMyTrees().length > 0;
  }

  public boolean hasNearbyTrees(){
    return getNearbyTrees().length > 0;
  }

  public boolean hasInteractableTrees(){
    return getInteractableTrees().length > 0;
  }

  public boolean hasMyInteractableTrees(){
    return getMyInteractableTrees().length > 0;
  }

  public TreeInfo[] getMyInteractableTrees(){
    if(myInteractableTrees != null) return myInteractableTrees;
    this.myInteractableTrees = Arrays
      .stream(getMyTrees())
      .filter((t1) -> rc.canInteractWithTree(t1.ID))
      .toArray(TreeInfo[]::new);
    return myInteractableTrees;
  }

  public TreeInfo[] getInteractableTrees(){
    if(interactableTrees != null) return interactableTrees;
    this.interactableTrees = Arrays
      .stream(getNearbyTrees())
      .filter((t1) -> rc.canInteractWithTree(t1.ID))
      .toArray(TreeInfo[]::new);
    return interactableTrees;
  }

  public TreeInfo[] getNearbyTrees(){
    if(nearbyTrees != null) return nearbyTrees;
    this.nearbyTrees = Arrays
      .stream(rc.senseNearbyTrees())
      .sorted((t1, t2) -> (Float.compare(
            myLocation.distanceTo(t1.getLocation()),
            myLocation.distanceTo(t2.getLocation())
            )))
      .toArray(TreeInfo[]::new);

    return nearbyTrees;
  }

  public TreeInfo[] getNeutralTrees(){
    if(neutralTrees != null) return neutralTrees;
    this.neutralTrees = treesForTeam(Team.NEUTRAL);
    return neutralTrees;
  }

  public TreeInfo[] getEnemyTrees(){
    if(enemyTrees != null) return enemyTrees;
    this.enemyTrees = treesForTeam(this.team.opponent());
    return enemyTrees;
  }

  public TreeInfo[] getMyTrees(){
    if(myTrees != null) return myTrees;
    this.myTrees = treesForTeam(this.team);
    return myTrees;
  }

  private TreeInfo[] treesForTeam(Team team){
    return Arrays
      .stream(getNearbyTrees())
      .filter((t) -> t.team == team)
      .toArray(TreeInfo[]::new);
  }
}

