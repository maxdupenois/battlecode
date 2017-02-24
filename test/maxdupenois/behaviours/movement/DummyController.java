package maxdupenois.behaviours.movement;
import battlecode.common.*;
import java.util.ArrayList;

public strictfp class DummyController implements RobotController {
  private int[][] map;
  private MapLocation currentLocation;
  private ArrayList<MapLocation> history;
  public DummyController(int[][] map, MapLocation initialLocation){
    MapLocation converted = convertLocation(initialLocation);
    this.map = map;
    this.history = new ArrayList<MapLocation>();
    this.currentLocation = converted;
    this.history.add(converted);
  }

  public boolean hasMoved() { return false; }
  public boolean canMove(MapLocation loc) {
    MapLocation converted = convertLocation(loc);
    //On map
    if(converted.y < 0) return false;
    if(converted.y >= this.map.length) return false;
    if(converted.x < 0) return false;
    if(converted.x >= this.map[0].length) return false;

    return (map[(int)converted.y][(int)converted.x] != 1);
  }

  private MapLocation convertLocation(MapLocation loc){
    return new MapLocation(Math.round(loc.x), Math.round(loc.y));
  }

  public void move(MapLocation loc) {
    MapLocation converted = convertLocation(loc);
    if(!canMove(converted)) {
      this.history.add(this.currentLocation);
    } else {
      this.currentLocation = converted;
      this.history.add(converted);
    }
  }

  public MapLocation[] getHistory(){
    return this.history.toArray(new MapLocation[this.history.size()]);
  }

  public MapLocation getLocation(){
    return this.currentLocation;
  }

  public float getStrideRadius(){
    return 1.0f; //So that it can go diagonally
  }

  public String[] getPrintableMaps(){
    String[] maps = new String[this.history.size()];
    for(int i = 0; i < maps.length; i++){
      maps[i] = getPrintableHistoryAt(i);
    }
    return maps;
  }

  private String getPrintableHistoryAt(int index) {
    MapLocation loc = this.history.get(index);
    StringBuffer total = new StringBuffer();
    StringBuffer row;
    char c;
    for(int y = 0; y < this.map.length; y++){
      row = new StringBuffer();
      for(int x = 0; x < this.map[y].length; x++) {
        if((int)loc.x == x && (int)loc.y == y){
          c = 'R';
        } else if(this.map[y][x] == 1) {
          c = 'X';
        } else {
          c = ' ';
        }
        row.append("| "+c);
      }
      total.append(row);
      total.append("\n");
      for(int i = 0;i < this.map[y].length; i++) {
        total.append("---");
      }
      total.append("\n");
    }
    return total.toString();
  }

  //Things I'm not (yet) using
  public void broadcast(int channel, int data) {}
  public void broadcastBoolean(int channel, boolean data) {}
  public void broadcastFloat(int channel, float data) {}
  public void broadcastInt(int channel, int data) {}
  public void buildRobot(RobotType type, Direction dir) {}
  public boolean canBuildRobot(RobotType type, Direction dir){ return true; }
  public boolean canChop(int id){ return true; }
  public boolean canChop(MapLocation loc){ return true; }
  public boolean canFirePentadShot(){ return true; }
  public boolean canFireSingleShot(){ return true; }
  public boolean canFireTriadShot(){ return true; }
  public boolean canHireGardener(Direction dir){ return true; }
  public boolean canInteractWithTree(int id){ return true; }
  public boolean canInteractWithTree(MapLocation loc){ return true; }
  public boolean canMove(Direction dir){ return true; }
  public boolean canMove(Direction dir, float distance){ return true; }
  public boolean canPlantTree(Direction dir){ return true; }
  public boolean canSenseAllOfCircle(MapLocation center, float radius){ return true; }
  public boolean canSenseBullet(int id){ return true; }
  public boolean canSenseBulletLocation(MapLocation loc){ return true; }
  public boolean canSenseLocation(MapLocation loc){ return true; }
  public boolean canSensePartOfCircle(MapLocation center, float radius){ return true; }
  public boolean canSenseRadius(float radius){ return true; }
  public boolean canSenseRobot(int id){ return true; }
  public boolean canSenseTree(int id){ return true; }
  public boolean canShake(){ return true; }
  public boolean canShake(int id){ return true; }
  public boolean canShake(MapLocation loc){ return true; }
  public boolean canStrike(){ return true; }
  public boolean canWater(){ return true; }
  public boolean canWater(int id){ return true; }
  public boolean canWater(MapLocation loc){ return true; }
  public void chop(int id) {}
  public void chop(MapLocation loc) {}
  public void disintegrate() {}
  public void donate(float bullets) {}
  public void firePentadShot(Direction dir) {}
  public void fireSingleShot(Direction dir) {}
  public void fireTriadShot(Direction dir) {}
  public int getAttackCount() { return 1; }
  public int getBuildCooldownTurns() { return 1; }
  public long  getControlBits() { return 1; }
  public float getHealth() { return 1f; }
  public int getID() { return 1; }
  public MapLocation[] getInitialArchonLocations(Team t) { return new MapLocation[0]; }
  public int getMoveCount() { return 1; }
  public int getOpponentVictoryPoints() { return 1; }
  public int getRobotCount() { return 1; }
  public int getRoundLimit() { return 1; }
  public int getRoundNum() { return 1; }
  public Team getTeam() { return Team.A; }
  public float getTeamBullets() { return 1f; }
  public long[]  getTeamMemory() { return new long[0]; }
  public int getTeamVictoryPoints() { return 1; }
  public int getTreeCount() { return 1; }
  public RobotType getType() { return RobotType.GARDENER; }
  public float getVictoryPointCost() { return 1f; }
  public boolean hasAttacked() { return true; }
  public boolean hasRobotBuildRequirements(RobotType type) { return true; }
  public boolean hasTreeBuildRequirements() { return true; }
  public void  hireGardener(Direction dir) {}
  public boolean isBuildReady() { return true; }
  public boolean isCircleOccupied(MapLocation center, float radius) { return true; }
  public boolean isCircleOccupiedExceptByThisRobot(MapLocation center, float radius) { return true; }
  public boolean isLocationOccupied(MapLocation loc) { return true; }
  public boolean isLocationOccupiedByRobot(MapLocation loc) { return true; }
  public boolean isLocationOccupiedByTree(MapLocation loc) { return true; }
  public void move(Direction dir) {}
  public void move(Direction dir, float distance) {}
  public boolean onTheMap(MapLocation loc) { return true; }
  public boolean onTheMap(MapLocation center, float radius) { return true; }
  public void plantTree(Direction dir) {}
  public int readBroadcast(int channel) { return 1; }
  public boolean readBroadcastBoolean(int channel) { return true; }
  public float readBroadcastFloat(int channel) { return 1f; }
  public int readBroadcastInt(int channel) { return 1; }
  public void  resign() {}
  public MapLocation[] senseBroadcastingRobotLocations() { return new MapLocation[0]; }
  public BulletInfo senseBullet(int id) { return new BulletInfo(id, new MapLocation(0, 0), new Direction(0), 1f, 1f); }

  public BulletInfo[] senseNearbyBullets() { return new BulletInfo[0]; }
  public BulletInfo[] senseNearbyBullets(float radius) { return new BulletInfo[0]; }
  public BulletInfo[] senseNearbyBullets(MapLocation center, float radius) { return new BulletInfo[0]; }
  public RobotInfo[] senseNearbyRobots() { return new RobotInfo[0]; }
  public RobotInfo[] senseNearbyRobots(float radius) { return new RobotInfo[0]; }
  public RobotInfo[] senseNearbyRobots(float radius, Team team) { return new RobotInfo[0]; }
  public RobotInfo[] senseNearbyRobots(MapLocation center, float radius, Team team) { return new RobotInfo[0]; }
  public TreeInfo[]  senseNearbyTrees() { return new TreeInfo[0]; }
  public TreeInfo[]  senseNearbyTrees(float radius) { return new TreeInfo[0]; }
  public TreeInfo[]  senseNearbyTrees(float radius, Team team) { return new TreeInfo[0]; }
  public TreeInfo[]  senseNearbyTrees(MapLocation center, float radius, Team team) { return new TreeInfo[0]; }
  public RobotInfo senseRobot(int id) { return new RobotInfo(id, Team.B, RobotType.SOLDIER, new MapLocation(0, 0), 1f, 1, 1); }
  public RobotInfo senseRobotAtLocation(MapLocation loc) { return new RobotInfo(1, Team.B, RobotType.SOLDIER, loc, 1f, 1, 1); }
  public TreeInfo senseTree(int id) { return new TreeInfo(id, Team.B, new MapLocation(0, 0), 1f, 1f, 10, RobotType.GARDENER); }
  public TreeInfo senseTreeAtLocation(MapLocation loc) { return new TreeInfo(1, Team.B, loc, 1f, 1f, 10, RobotType.GARDENER); }
  public void setIndicatorDot(MapLocation loc, int red, int green, int blue) {}
  public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {}
  public void setTeamMemory(int index, long value) {}
  public void setTeamMemory(int index, long value, long mask) {}
  public void shake(int id) {}
  public void shake(MapLocation loc) {}
  public void strike() {}
  public void water(int id) {}
  public void water(MapLocation loc) {}
}
