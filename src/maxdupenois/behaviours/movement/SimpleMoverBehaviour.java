package maxdupenois.behaviours.movement;
import maxdupenois.behaviours.Behaviour;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;
import battlecode.common.Direction;

//Not a behaviour, just accepts destinations to move
//to
public strictfp class SimpleMoverBehaviour implements Behaviour, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;
  private boolean moving;

  public SimpleMoverBehaviour(RobotController robotController){
    this.traveller = new Traveller(this, robotController);
    this.moving = false;
  }

  public void execute() throws GameActionException {
    if(!traveller.hasDestination() ||
        traveller.hasReachedDestination()) return;
    traveller.continueToDestination();
  }

  public void moveTo(MapLocation destination){
    traveller.setDestination(destination);
    this.moving = true;
  }

  public boolean isMoving(){
    return moving;
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {
    this.moving = false;
  }
  public void onFailingToReachDestination(MapLocation destination) {
    this.moving = false;
  }
  public void onReachingDiversion(MapLocation destination) {}
  public void onDiversion(MapLocation destination) {}
  public void onNeedingToDivert(MapLocation destination) {}
  public void onMapBoundaryFound(MapLocation destination) {}
}

