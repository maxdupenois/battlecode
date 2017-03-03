package maxdupenois.util;

import battlecode.common.MapLocation;
import battlecode.common.Direction;

public class MapVector {
  private MapLocation start;
  private MapLocation finish;
  private Direction direction;
  private float magnitude;
  private boolean horizontal;
  private boolean vertical;
  private float A;
  private float B;
  private float C;

  public MapVector(MapLocation start, MapLocation finish){
    this.start = start;
    this.finish = finish;
    this.direction = start.directionTo(finish);
    this.magnitude = start.distanceTo(finish);
    fillEquation();
  }

  public MapVector(MapLocation start, Direction dir, float magnitude){
    this.start = start;
    this.finish = start.add(dir, magnitude);
    this.direction = dir;
    this.magnitude = magnitude;
    fillEquation();
  }

  private void fillEquation(){
    this.vertical = this.start.x == this.finish.x;
    this.horizontal = this.start.y == this.finish.y;

    if(isVertical()){
      // y can't effect us so B must be 0
      // x = -C / A
      // if A = 1, then x = -C, C = -x
      this.A = 1;
      this.B = 0;
      this.C = -(this.start.x);
    } else if(isHorizontal()){
      // x can't effect us so A must be 0
      // y = -C / B
      // if B = 1, then y = -C, C = -y
      this.A = 0;
      this.B = 1;
      this.C = -(this.start.y);
    } else {
      /* Ax + By + C =0
       * Ax1 + By1 = -C = Ax2 + By2
       * A(x1-x2) = B(y2-y1)
       * A^2 + B^2 = 1 :: A^2(y2-y1)^2 + B^2(y2-y1)^2 = (y2-y1)^2
       * A^2(x1-x2)^2 = B^2(y2-y1)^2
       * A^2(y2-y1)^2 +  A^2(x1-x2)^2 = (y2-y1)^2
       * A^2((y2-y1)^2 + (x1-x2)^2) = (y2-y1)^2
       * A = SQRT((y2-y1)^2/((y2-y1)^2 + (x1-x2)^2))
       * B = A(x1-x2)/(y2-y1)
       * C = - Ax1 - By1
      */
      float x1x2 = this.start.x - this.finish.x;
      float y2y1 = this.finish.y - this.start.y;

      float x1x2Sq = (float)Math.pow(x1x2, 2);
      float y2y1Sq = (float)Math.pow(y2y1, 2);
      this.A = (float)Math.sqrt(y2y1Sq / (y2y1Sq + x1x2Sq));
      this.B = (this.A * x1x2)/ y2y1;
      this.C = -(this.A * this.start.x) - (this.B * this.start.y);
    }
  }

  public MapLocation locationAtY(float y){
    // if horizontal then location at y is meaningless
    if(isHorizontal()) return null;
    //Ax + By + C = 0
    //x = (-By - C) / A
    float x = ( - (this.B * y) - this.C) / this.A;
    return new MapLocation(x, y);
  }

  public MapLocation locationAtX(float x){
    // if vertical then location at x is meaningless
    if(isVertical()) return null;
    //Ax + By + C = 0
    //y = (-Ax - C) / B
    float y = ( - (this.A * x) - this.C) / this.B;
    return new MapLocation(x, y);
  }

  public boolean isHorizontal(){
    return horizontal;
  }

  public boolean isVertical(){
    return vertical;
  }

  public boolean isWithinRangeOfVector(MapLocation location, float range){
    MapLocation onLine;
    if(this.isVertical()){
      onLine = this.locationAtY(location.y);
    } else {
      onLine = this.locationAtX(location.x);
    }
    boolean withinRangeOfLine = onLine.isWithinDistance(location, range);
    boolean withinVector = (start.distanceTo(onLine) <= this.magnitude);
    return withinRangeOfLine && withinVector;
  }

}
