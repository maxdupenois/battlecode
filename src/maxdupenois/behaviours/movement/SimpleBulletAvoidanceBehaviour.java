package maxdupenois.behaviours.movement;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;
import battlecode.common.BulletInfo;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import static maxdupenois.util.GeometryUtil.*;
import maxdupenois.behaviours.Behaviour;
import java.util.ArrayList;

public strictfp class SimpleBulletAvoidanceBehaviour implements Behaviour {
  private Traveller traveller;
  private RobotController robotController;
  private RobotInfo target;
  private Team team;
  private Direction currentDirection;
  //TODO: replace this with properties object
  private float minBulletAwarenessRange;
  private float maxBulletAwarenessRange;
  private float collisionRange;
  private float bulletAvoidanceModifier;
  private float sidewaysJumpRange;

  public SimpleBulletAvoidanceBehaviour(RobotController robotController, Traveller traveller){
    this(robotController, traveller, 5f, 10f, 1f, 0.6f, 2f);
  }

  public SimpleBulletAvoidanceBehaviour(RobotController robotController, Traveller traveller, float minBulletAwarenessRange, float maxBulletAwarenessRange, float collisionRange, float bulletAvoidanceModifier, float sidewaysJumpRange){
    this.traveller = traveller;
    this.robotController = robotController;
    this.minBulletAwarenessRange = minBulletAwarenessRange;
    this.maxBulletAwarenessRange = maxBulletAwarenessRange;
    // How close we have to be consider a collision
    this.collisionRange = collisionRange;
    this.bulletAvoidanceModifier = bulletAvoidanceModifier;
    this.sidewaysJumpRange = sidewaysJumpRange;
  }

  public void execute() throws GameActionException {
    BulletInfo[] bullets = robotController.senseNearbyBullets();
    if(bullets.length == 0) return;
    applyBulletAvoidance(bullets);
  }

  private void applyBulletAvoidance(BulletInfo[] bullets){
    MapLocation currentLocation = robotController.getLocation();
    Direction dir = MovementUtil.baseDirection(traveller);
    Direction bulletDir;
    MapLocation bulletLoc;
    // To make this simple as a starting version
    // we'll ignore speed and our own travel,
    // if the direction of a bullet takes it to
    // our current location we'll take that
    // as sufficient warning to bail

    float distanceToBullet;
    float actBulletAvoidance;
    float rangeDifference;
    float distanceWithinRange;
    Direction avoidanceDir;
    for(int b = 0; b < bullets.length; b++){
      bulletDir = bullets[b].getDir();
      bulletLoc = bullets[b].getLocation();
      distanceToBullet = currentLocation.distanceTo(bulletLoc);
      // NOTE: Weirdly better without this, they don't run
      // in to their own bullets:
      // Only care if bullet is on the way to me
      if(distanceToBullet <= currentLocation.distanceTo(bulletLoc.add(bulletDir))) continue;
      //bullet range is the maximum awareness range
      if(!isCollisionLikely(
            currentLocation, bulletLoc, bulletDir,
            maxBulletAwarenessRange, collisionRange)) continue;

      //bullet avoidance scales from the full amount at min range
      //to 0 at max range
      //S = separation, M_0 = min range, M_1 max range
      //f(d) = S * (1 - MIN(MAX(d - M_0, 0), M_1 - M_0)/(M_1 - M_0))
      rangeDifference = maxBulletAwarenessRange - minBulletAwarenessRange;
      distanceWithinRange = Math.min(Math.max(distanceToBullet - minBulletAwarenessRange, 0), rangeDifference);
      actBulletAvoidance = bulletAvoidanceModifier * (1 - distanceWithinRange/rangeDifference);

      // Bullet direction might be in front or behind me
      // so simply going the opposite of that might
      // put me closer to the bullet, rather than
      // work it out we'll aim perpendicular to the bullet
      // Veer left or right randomly
      if (Math.random() > 0.5) {
        avoidanceDir = bulletDir.rotateRightDegrees(90);
      } else {
        avoidanceDir = bulletDir.rotateLeftDegrees(90);
      }

      dir = modifyDirection(dir, avoidanceDir, actBulletAvoidance);
    }
    traveller.setDestination(currentLocation.add(dir, sidewaysJumpRange));
  }
}
