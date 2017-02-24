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
      new int[]{ 0, 1, 0, 0, 0 },
      new int[]{ 0, 1, 1, 0, 0 },
      new int[]{ 0, 0, 0, 0, 0 },
      new int[]{ 0, 0, 0, 0, 0 },
    }, new MapLocation(0, 0));
    endDestination = new MapLocation(4, 4);
    eventSubscriber = new DummyTravellerSubscriber();
    traveller = new Traveller(
        eventSubscriber, robotController, 20, 0.9f
        );
    traveller.setDestination(endDestination);
    // Enough to go diagonally but not
    // jump a square
    traveller.setStrideRadius(1.5f);
  }

  @Test
  public void testPathFinding(){

    //Just ensure we don't loop eternally
    int counter = 10;
    while(!traveller.hasReachedDestination() && counter > 0){
      try {
        traveller.continueToDestination();
      } catch (GameActionException ex) {
        //blah
      }
      counter--;
    }

    //String[] maps = robotController.getPrintableMaps();
    //for(int i=0; i<maps.length; i++){
    //  System.out.println(maps[i]);
    //}

    // We should end up where we want to be
    assertEquals(endDestination, robotController.getLocation());
  }
}
