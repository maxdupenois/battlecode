package maxdupenois.behaviours.movement;

//The most basic form of movement behaviour,
//does noting with hooks, simply uses a traveller
//to head to a location
public strictfp class SimpleMovementBehaviour implements MovementInterface, TravellerEventInterface {
  private Traveller traveller;
  private RobotController robotController;

  public SimpleMovementBehaviour(RobotController robotController){
    this.traveller = new Traveller(this, robotController);
    this.robotController = robotController;
  }

  public void move(){
    if(!traveller.hasDestination() || traveller.hasReachedDestination()){
      // Only thing we know is our starting location
      // so pick a random point based on that. We don't
      // know the map bounds the only thing we know
      // is that our coordinates are in it, don't believe
      // we can even guarantee that there's a (0, 0) and hence
      // a bounding box we can use.
      traveller.setDestination(
          this.robotController.getCurrentLocation().add(
            new Direction(
              (float)Math.random() * 2 * (float)Math.PI
              ),
            50
            )
          )
    }
    traveller.continueToDestination();
  }

  // Movement Interface methods
  public void onReachingDestination(MapLocation destination) {}
  public void onFailingToReachDestination(MapLocation destination) {}
  public void onReachingDestinationNode(MapLocation destination) {}
  public void onFailingToReachDestinationNode(MapLocation destination) {}
}
