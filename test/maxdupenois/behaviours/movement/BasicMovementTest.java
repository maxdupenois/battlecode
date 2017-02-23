package maxdupenois.behaviours.movement;

import battlecode.common.*;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class BasicMovementTest {

  public class DummyMover implements MoverInterface {
    private int[][] map;
    private MapLocation currentLocation;
    private ArrayList<MapLocation> history;
    public DummyMover(int[][] map, MapLocation initialLocation){
      MapLocation converted = convertLocation(initialLocation);
      this.map = map;
      this.history = new ArrayList<MapLocation>();
      this.currentLocation = converted;
      this.history.add(converted);
    }

    public void onReachingDestination(MapLocation location) {}
    public void onFailingToReachDestination(MapLocation location) {}
    public boolean hasMoved() { return false; }
    public boolean canMove(MapLocation loc) {
      MapLocation converted = convertLocation(loc);
      //On map
      if(loc.y < 0) return false;
      if(loc.y >= this.map.length) return false;
      if(loc.x < 0) return false;
      if(loc.x >= this.map[0].length) return false;

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

    public MapLocation getCurrentLocation(){
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
  }

  private MapLocation endDestination;
  private DummyMover mover;
  private BasicMovement movement;

  @Before
  public void initialize(){
    mover = new DummyMover(new int[][]{
      new int[]{ 0, 0, 0, 0, 0 },
      new int[]{ 0, 1, 0, 0, 0 },
      new int[]{ 0, 1, 1, 0, 0 },
      new int[]{ 0, 0, 0, 0, 0 },
      new int[]{ 0, 0, 0, 0, 0 },
    }, new MapLocation(0, 0));
    endDestination = new MapLocation(4, 4);
    movement = new BasicMovement(mover, 20, 0.5f);
    movement.setDestination(endDestination);
  }

  @Test
  public void testPathFinding(){

    //Just ensure we don't loop eternally
    int counter = 40;
    while(!movement.hasReachedDestination() && counter > 0){
      try {
        movement.continueToDestination();
      } catch (GameActionException ex) {
        //blah
      }
      counter--;
    }

    //String[] maps = mover.getPrintableMaps();
    //for(int i=0; i<maps.length; i++){
    //  System.out.println(maps[i]);
    //}

    // We should end up where we want to be
    assertEquals(mover.getCurrentLocation(), endDestination);
  }
}
