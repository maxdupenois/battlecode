package maxdupenois.behaviours.movement;

import battlecode.common.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class TravellerTest {
  private MapLocation endDestination;
  private DummyController robotController;
  private DummyTravellerSubscriber eventSubscriber;
  private Traveller traveller;

  @Before
  public void initialize(){
    robotController = new DummyController(new int[][]{
      new int[]{ 0, 0, 0, 0, 0 },
      new int[]{ 0, 1, 0, 1, 0 },
      new int[]{ 0, 1, 1, 1, 0 },
      new int[]{ 0, 0, 0, 0, 0 },
      new int[]{ 0, 0, 0, 0, 0 },
    }, new MapLocation(2, 1));
    eventSubscriber = new DummyTravellerSubscriber();
    traveller = new Traveller(
        robotController, 0.9f
        );
    traveller.subscribe(eventSubscriber);
    // Enough to go diagonally but not
    // jump a square
    traveller.setStrideRadius(1.49f);
  }

  @Test
  public void testPathFinding(){
    endDestination = new MapLocation(4, 4);
    traveller.setDestination(endDestination);
    traveller.debug_dbgOff();

    runJourney();
    //debugPrintMaps();
    // We should end up where we want to be
    assertEquals(endDestination, robotController.getLocation());
  }

  @Test
  public void testMapBoundaryFinding(){
    endDestination = new MapLocation(5, 0);
    traveller.setDestination(endDestination);
    traveller.debug_dbgOff();

    runJourney();

    //We should have a failed location that is the
    //same as our attempted end destination
    assertEquals(endDestination, eventSubscriber.getFailedLocation());
  }

  private void debugPrintMaps(){
    String[] maps = robotController.getPrintableMaps();
    for(int i=0; i<maps.length; i++){
      System.out.println(maps[i]);
    }
  }

  private void runJourney(){
    //Just ensure we don't loop eternally
    int counter = 30;
    while(
        traveller.hasDestination() &&
        !traveller.hasReachedDestination() &&
        counter > 0
        ){
      try {
        traveller.continueToDestination();
      } catch (GameActionException ex) {
        //blah
      }
      counter--;
    }
  }
}
