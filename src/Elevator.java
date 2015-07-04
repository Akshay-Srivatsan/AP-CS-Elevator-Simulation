import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * One elevator object represents one real elevator. The main class can tell it
 * to move, load passengers, or change direction.
 * 
 * @author Akshay
 * 
 */
public class Elevator
{
	static final int WIDTH = 250;
	static final int HEIGHT = Floor.HEIGHT;

	public byte id;
	public int floor;
	private ElevatorSimulation.Direction direction;
	private int destination;

	/*
	 * If a direction is planned, the elevator will use it when it gets to the
	 * current destination.
	 */
	private ElevatorSimulation.Direction plannedDirection;
	public boolean open;

	private ArrayList<Passenger> people = new ArrayList<Passenger>();

	/**
	 * The initializer for an elevator.
	 * 
	 * @param id
	 *            The elevator number.
	 * @param floor
	 *            The floor to start on.
	 */
	Elevator(int id, int floor)
	{
		this.id = (byte) id;
		this.floor = floor;
		this.direction = ElevatorSimulation.Direction.NONE;
		this.destination = this.floor;

		this.plannedDirection = ElevatorSimulation.Direction.NONE;
		this.open = false;
	}

	/**
	 * The method that decides which way to move and whether to stop.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	public void move(Floor[] building)
	{
		if (!reevaluatePath(building))
		{
			return;
		}
		if ((this.floor + 1 > ElevatorSimulation.NUM_FLOORS) && (this.direction == ElevatorSimulation.Direction.UP))
		{
			System.out.println("Highest Floor - Stopping");
			this.direction = ElevatorSimulation.Direction.NONE;
		}
		if ((this.direction == ElevatorSimulation.Direction.DOWN) && (this.floor - 1 < ElevatorSimulation.LOWEST_FLOOR))
		{
			System.out.println("Lowest Floor - Stopping");
			this.direction = ElevatorSimulation.Direction.NONE;
		}
		if (this.direction == ElevatorSimulation.Direction.DOWN)
		{
			moveDown(building);
		}
		if (this.direction == ElevatorSimulation.Direction.UP)
		{
			moveUp(building);
		}
		if (this.floor == this.destination)
		{
			stopElevator();
		}
		System.out.println("Destination: " + this.destination);
	}

	/**
	 * Stops the elevator and opens it.
	 */
	private void stopElevator()
	{
		System.out.println("At destination floor - Stopping");
		this.direction = ElevatorSimulation.Direction.NONE;
		this.plannedDirection = ElevatorSimulation.Direction.NONE;
		this.open = true;
	}

	/**
	 * This method moves the elevator up one floor and may stop it at that floor
	 * if necessary.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	public void moveUp(Floor[] building)
	{
		System.out.println("Moving up to floor " + (this.floor + 1));
		/* This check is if somebody showed up at the last minute. */
		if (building[this.floor].goingUp)
		{
			System.out.println("Opening at floor " + this.floor);
			this.open = true;
		}

		this.floor = Math.min(this.floor + 1, ElevatorSimulation.NUM_FLOORS);
		if (building[this.floor].goingUp || needToStop())
		{
			System.out.println("Opening at floor " + this.floor);
			this.open = true;
		}
	}

	/**
	 * This method moves the elevator down one floor and may stop it at that
	 * floor if necessary.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	public void moveDown(Floor[] building)
	{
		System.out.println("Moving down to floor " + (this.floor - 1));
		/* This check is if somebody showed up at the last minute. */
		if (building[this.floor].goingDown)
		{
			System.out.println("Opening at floor " + this.floor);
			this.open = true;
		}

		this.floor = Math.max(this.floor - 1, ElevatorSimulation.LOWEST_FLOOR);
		if (building[this.floor].goingDown || needToStop())
		{
			System.out.println("Opening at floor " + this.floor);
			this.open = true;
		}
	}

	/**
	 * Gets the destination of the elevator.
	 * 
	 * @return The destination of the elevator.
	 */
	public int getDestination()
	{
		return this.destination;
	}

	/**
	 * Gets the direction of the elevator.
	 * 
	 * @return The direction of the elevator.
	 */
	public ElevatorSimulation.Direction getDirection()
	{
		return this.direction;
	}

	/**
	 * Sets the direction of the elevator.
	 * 
	 * @param direction
	 *            The direction to travel in.
	 */
	public void setDirection(ElevatorSimulation.Direction direction)
	{
		this.direction = direction;
		System.out.println("Changing Directions to " + direction);
	}

	/**
	 * Gets the planned direction of the elevator.
	 * 
	 * @return The planned direction of the elevator.
	 */
	public ElevatorSimulation.Direction getPlannedDirection()
	{
		return this.plannedDirection;
	}

	/**
	 * Loads a list of passengers into the elevator. Should be used with
	 * ElevatorSimulation.Floor.getPeopleToLoad().
	 * 
	 * @param peopleToLoad
	 *            The list of people to load.
	 */
	public void loadPassengers(ArrayList<Passenger> peopleToLoad)
	{
		Iterator<Passenger> loader = peopleToLoad.iterator();
		Passenger next = null;
		if (!loader.hasNext())
		{
			System.out.println("Nobody to load.");
		}
		while (loader.hasNext())
		{
			next = loader.next();
			this.plannedDirection = next.direction;
			/* Transfer people from input list to elevator list. */
			this.people.add(next);
			/* This finds the destination that is farther away. */
			if (this.plannedDirection == ElevatorSimulation.Direction.UP && next.destination > this.destination)
			{
				System.out.println("New upward destination has been set");
				this.destination = next.destination;
			}
			if (this.plannedDirection == ElevatorSimulation.Direction.DOWN && next.destination < this.destination)
			{
				System.out.println("New downward destination has been set");
				this.destination = next.destination;
			}
			System.out.println("Loading passenger " + next.id + " from floor " + this.floor + " going to floor " + next.destination + ".");
		}
		this.open = false;
	}

	/**
	 * Removes all the passengers who need to get out at the current floor from
	 * the elevator.
	 * 
	 * @return A list of people to unload.
	 */
	public ArrayList<Passenger> unloadPassengers()
	{
		ArrayList<Passenger> retval = new ArrayList<Passenger>();
		Iterator<Passenger> elevatorPeople = people.iterator();
		Passenger currentPassenger = null;

		if (!elevatorPeople.hasNext())
		{
			System.out.println("Nobody to unload.");
		}
		while (elevatorPeople.hasNext())
		{
			currentPassenger = elevatorPeople.next();
			/*
			 * If the passenger wants to get out, transfer them to the outgoing
			 * list.
			 */
			if (currentPassenger.shouldGetOut(this.floor))
			{
				retval.add(currentPassenger);
				elevatorPeople.remove();
			}
		}
		return retval;

	}

	/**
	 * Tests if the elevator needs to stop at the given floor because someone on
	 * the inside needs to get out.
	 * 
	 * @return True if the elevator should stop, false otherwise.
	 */
	private boolean needToStop()
	{

		boolean retval = false;
		Iterator<Passenger> elevatorPeople = people.iterator();
		Passenger currentPassenger = null;

		while (elevatorPeople.hasNext())
		{
			currentPassenger = elevatorPeople.next();
			/*
			 * If the passenger wants to get out, transfer them to the outgoing
			 * list.
			 */
			if (currentPassenger.shouldGetOut(this.floor))
			{
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * The main logic of the elevator. This method looks for people wait on
	 * floors in a specific search pattern: it scans from the elevator up for
	 * people above going up, from the elevator down for people below going
	 * down, from the bottom floor up for people below going up. from the top
	 * floor down for people above going down,
	 * 
	 * @param building
	 *            An array of all the floors in the building.
	 */
	public void scanForPeople(Floor[] building)
	{
		if (checkCurrentFloor(building))
		{
			return;
		}
		/*
		 * These four variables store the searching positions for passengers.
		 */
		int aboveGoingUp = this.floor, aboveGoingDown = (ElevatorSimulation.NUM_FLOORS + ElevatorSimulation.LOWEST_FLOOR - 1), belowGoingUp = ElevatorSimulation.LOWEST_FLOOR, belowGoingDown = this.floor;
		/*
		 * Check if there are people on the specified floors going the specified
		 * directions, otherwise change the search locations.
		 */
		do
		{
			if (testFloors(building, aboveGoingUp, aboveGoingDown, belowGoingUp, belowGoingDown))
			{
				return;
			}
			/*
			 * Change the search locations, unless they have reached their outer
			 * limits.
			 */
			aboveGoingUp = Math.min(ElevatorSimulation.NUM_FLOORS, aboveGoingUp + 1);
			aboveGoingDown = Math.max(this.floor, aboveGoingDown - 1);
			belowGoingUp = Math.min(this.floor, belowGoingUp + 1);
			belowGoingDown = Math.max(ElevatorSimulation.LOWEST_FLOOR, belowGoingDown - 1);
		} while (testLimits(aboveGoingUp, aboveGoingDown, belowGoingUp, belowGoingDown));
		System.out.println("No people found.");
	}

	/**
	 * Looks in the search positions for people.
	 * 
	 * @param building
	 *            An array of floors.
	 * @param aboveGoingUp
	 *            Above the elevator going upward
	 * @param aboveGoingDown
	 *            Above the elevator going downward
	 * @param belowGoingUp
	 *            Below the elevator going upward
	 * @param belowGoingDown
	 *            Below the elevator going downward
	 * @return true if person is found.
	 */
	private boolean testFloors(Floor[] building, int aboveGoingUp, int aboveGoingDown, int belowGoingUp, int belowGoingDown)
	{
		if (building[aboveGoingUp].goingUp && !building[aboveGoingUp].handledUp)
		{
			setPlan(ElevatorSimulation.Direction.UP, ElevatorSimulation.Direction.UP, aboveGoingUp, false, building);
			return true;
		}
		else if (building[aboveGoingDown].goingDown && !building[aboveGoingDown].handledDown)
		{
			setPlan(ElevatorSimulation.Direction.UP, ElevatorSimulation.Direction.DOWN, aboveGoingDown, false, building);
			return true;
		}
		else if (building[belowGoingUp].goingUp && !building[belowGoingUp].handledUp)
		{
			setPlan(ElevatorSimulation.Direction.DOWN, ElevatorSimulation.Direction.UP, belowGoingUp, false, building);
			return true;
		}
		else if (building[belowGoingDown].goingDown && !building[belowGoingDown].handledDown)
		{
			setPlan(ElevatorSimulation.Direction.DOWN, ElevatorSimulation.Direction.DOWN, belowGoingDown, false, building);
			return true;
		}
		return false;
	}

	/**
	 * Sets all the properties of the elevator necessary to tell it what to do
	 * to get to the next passenger.
	 * 
	 * @param direction
	 *            The direction to start moving in immediately.
	 * @param plannedDirection
	 *            The direction to start moving in once arrived at the
	 *            destination.
	 * @param destination
	 *            The floor where the next passenger is.
	 * @param open
	 *            Whether to open immediately (only true if the passenger is on
	 *            the current floor).
	 * @param building
	 *            An array of floors.
	 */
	private void setPlan(ElevatorSimulation.Direction direction, ElevatorSimulation.Direction plannedDirection, int destination, boolean open, Floor[] building)
	{
		System.out.println("Person found on floor " + destination + " going " + plannedDirection);
		if (direction == ElevatorSimulation.Direction.UP)
		{
			handleFloorsUpward(direction, plannedDirection, destination, building);
		}
		if (direction == ElevatorSimulation.Direction.DOWN)
		{
			handleFloorsDownward(direction, plannedDirection, destination, building);
		}
		this.plannedDirection = plannedDirection;
		this.destination = destination;
		this.direction = direction;
		this.open = open;
	}

	/**
	 * Handles all floors in the path of the elevator going downward.
	 * 
	 * @param direction
	 *            The direction to look in.
	 * @param plannedDirection
	 *            The direction the elevator will change to.
	 * @param destination
	 *            The last floor on the path.
	 * @param building
	 *            An array of floors.
	 */
	private void handleFloorsDownward(ElevatorSimulation.Direction direction, ElevatorSimulation.Direction plannedDirection, int destination, Floor[] building)
	{
		for (int i = this.floor; i > destination; i--)
		{
			handleFloor(direction, building, i);
		}

		handleFinalFloor(plannedDirection, destination, building);
	}

	/**
	 * Handles all floors in the path of the elevator going upward.
	 * 
	 * @param direction
	 *            The direction to look in.
	 * @param plannedDirection
	 *            The direction the elevator will change to.
	 * @param destination
	 *            The last floor on the path.
	 * @param building
	 *            An array of floors.
	 */
	private void handleFloorsUpward(ElevatorSimulation.Direction direction, ElevatorSimulation.Direction plannedDirection, int destination, Floor[] building)
	{
		for (int i = this.floor; i < destination; i++)
		{
			handleFloor(direction, building, i);
		}

		handleFinalFloor(plannedDirection, destination, building);
	}

	/**
	 * If the elevator is headed towards a floor, it sets that floor as handled.
	 * 
	 * @param direction
	 *            The direction the elevator is moving.
	 * @param building
	 *            An array of floors.
	 * @param floor
	 *            The floor to check.
	 */
	private void handleFloor(ElevatorSimulation.Direction direction, Floor[] building, int floor)
	{
		if (building[floor].goingUp && direction == ElevatorSimulation.Direction.UP)
		{
			building[floor].handledUp = true;
		}
		if (building[floor].goingDown && direction == ElevatorSimulation.Direction.DOWN)
		{
			building[floor].handledDown = true;
		}
	}

	/**
	 * Handles the final floor in the path of the elevator.
	 * 
	 * @param plannedDirection
	 *            The planned direction.
	 * @param destination
	 *            The floor number.
	 * @param building
	 *            An array of floors.
	 */
	private void handleFinalFloor(ElevatorSimulation.Direction plannedDirection, int destination, Floor[] building)
	{
		if (plannedDirection == ElevatorSimulation.Direction.UP)
		{
			building[destination].handledUp = true;
		}
		else
		{
			building[destination].handledDown = true;
		}
	}

	/**
	 * Tests if there are any people waiting on the current floor, and if so,
	 * sets the plan and returns true.
	 * 
	 * @param building
	 *            An array containing all the floors.
	 * @return True if people were found on the current floor, false otherwise.
	 */
	private boolean checkCurrentFloor(Floor[] building)
	{
		if (building[this.floor].goingUp && !building[this.floor].handledUp)
		{
			setPlan(ElevatorSimulation.Direction.UP, ElevatorSimulation.Direction.UP, this.floor, true, building);
			return true;
		}
		if (building[this.floor].goingDown && !building[this.floor].handledDown)
		{
			setPlan(ElevatorSimulation.Direction.DOWN, ElevatorSimulation.Direction.DOWN, this.floor, true, building);
			return true;
		}
		return false;
	}

	/**
	 * Tests whether all the search positions have reached their limits.
	 * 
	 * @param aboveGoingUp
	 *            The position searching above the elevator for people going up.
	 * @param aboveGoingDown
	 *            The position searching above the elevator for people going
	 *            down.
	 * @param belowGoingUp
	 *            The position searching below the elevator for people going up.
	 * @param belowGoingDown
	 *            The position searching below the elevator for people going
	 *            down.
	 * @return True if the search positions have reached their limits, False
	 *         otherwise.
	 */
	private boolean testLimits(int aboveGoingUp, int aboveGoingDown, int belowGoingUp, int belowGoingDown)
	{
		return (aboveGoingUp < ElevatorSimulation.NUM_FLOORS || aboveGoingDown < this.floor || belowGoingUp > this.floor || belowGoingDown > ElevatorSimulation.LOWEST_FLOOR);
	}

	/**
	 * Gets a list of all the passengers in the elevator.
	 * 
	 * @return The list of passengers.
	 */
	public ArrayList<Passenger> getPassengers()
	{
		return this.people;
	}

	/**
	 * Causes the elevator to draw itself.
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 */
	public void draw(Graphics g)
	{
		int xCoord = (this.id) * (WIDTH + 10) + 20;
		int yCoord = (Floor.HEIGHT * (ElevatorSimulation.NUM_FLOORS + 1)) - ((this.floor + 1) * Floor.HEIGHT) + ElevatorSimulation.FONTSIZE + 10;
		drawElevator(g, xCoord, yCoord);
		drawStatus(g, xCoord, yCoord);
		for (int i = 0; i < Math.min(((ElevatorSimulation.FONTSIZE + 10) * 4) / 3, people.size()); i++)
		{
			drawPerson(g, xCoord, yCoord, i);
		}
	}

	/**
	 * Draws the elevator's background.
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 * @param xCoord
	 *            x coordinate of elevator
	 * @param yCoord
	 *            y coordinate of elevator
	 */
	private void drawElevator(Graphics g, int xCoord, int yCoord)
	{
		if (this.people.size() == 0 && this.direction == ElevatorSimulation.Direction.NONE)
		{
			g.setColor(Color.green);

		}
		else
		{
			if (this.open)
			{
				g.setColor(Color.lightGray);
			}
			else
			{
				g.setColor(Color.gray);
			}
		}
		g.fillRect(xCoord, yCoord, WIDTH, HEIGHT);
		g.setColor(Color.black);
	}

	/**
	 * Draws some information about the elevator's internal state.
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 * @param xCoord
	 *            x coordinate of elevator
	 * @param yCoord
	 *            y coordinate of elevator
	 */
	private void drawStatus(Graphics g, int xCoord, int yCoord)
	{
		if (this.open)
		{
			g.drawString("Open", xCoord + 10, yCoord + HEIGHT - 10);
		}
		else
		{
			g.drawString("Closed", xCoord + 10, yCoord + HEIGHT - 10);
		}
		g.drawString(String.valueOf(this.destination), xCoord + (WIDTH * 2 / 3), yCoord + HEIGHT - 10);
	}

	/**
	 * Draws one person.
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 * @param xCoord
	 *            x coordinate of elevator
	 * @param yCoord
	 *            y coordinate of elevator
	 * @param i
	 *            The person's place in the elevator.
	 */
	private void drawPerson(Graphics g, int xCoord, int yCoord, int i)
	{
		if ((i % 3) == 0)
		{
			g.drawString(String.valueOf(people.get(i).id) + ":" + String.valueOf(people.get(i).destination), xCoord + 5, yCoord + ((i + 5) / 3)
					* ElevatorSimulation.FONTSIZE);
		}
		else if ((i % 3) == 1)
		{
			g.drawString(String.valueOf(people.get(i).id) + ":" + String.valueOf(people.get(i).destination), xCoord + WIDTH / 3, yCoord + ((i + 4) / 3)
					* ElevatorSimulation.FONTSIZE);
		}
		else if ((i % 3) == 2)
		{
			g.drawString(String.valueOf(people.get(i).id) + ":" + String.valueOf(people.get(i).destination), xCoord + WIDTH * 2 / 3, yCoord + ((i + 3) / 3)
					* ElevatorSimulation.FONTSIZE);
		}
	}

	/**
	 * Checks if there are people waiting on the elevator's path, and whether to
	 * stay open.
	 * 
	 * @param building
	 *            An array of floors.
	 * @return false if the elevator should stay open.
	 */
	public boolean reevaluatePath(Floor[] building)
	{
		if (!testThisFloor(building))
		{
			return false;
		}

		searchOtherFloors(building);
		return true;
	}

	/**
	 * Searches other floors to see if the elevator will handle them.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	private void searchOtherFloors(Floor[] building)
	{
		System.out.println(this.direction);
		if (this.direction == ElevatorSimulation.Direction.UP)
		{
			for (int i = this.floor; i < this.destination; i++)
			{
				System.out.println(i);
				if (building[i].goingUp)
				{
					System.out.println("*");
					building[i].handledUp = true;
				}
			}
		}
		else
		{
			for (int i = this.floor; i > this.destination; i--)
			{
				System.out.println(i);
				if (building[i].goingDown)
				{
					System.out.println("*");
					building[i].handledDown = true;
				}
			}
		}
	}

	/**
	 * Tests the current floor to see if the elevator should stay open.
	 * 
	 * @param building
	 *            An array of floors.
	 * @return false if the elevator should stay open.
	 */
	private boolean testThisFloor(Floor[] building)
	{
		if (this.direction == ElevatorSimulation.Direction.UP)
		{
			if (building[this.floor].goingUp && !building[this.floor].handledUp)
			{
				this.open = true;
				building[this.floor].handledUp = true;
				return false;
			}
		}
		if (this.direction == ElevatorSimulation.Direction.DOWN)
		{
			if (building[this.floor].goingDown && !building[this.floor].handledDown)
			{
				this.open = true;
				building[this.floor].handledDown = true;
				return false;
			}
		}
		return true;
	}

}