/**
 * This class represents a real life person.
 * 
 * @author Akshay
 * 
 */
public class Passenger
{
	public int id;
	public int destination;
	public int timeWaited = 0;
	public ElevatorSimulation.Direction direction;

	/**
	 * Sets up the passenger's id, destination, and direction.
	 * 
	 * @param id
	 *            The passenger's id.
	 * @param floor
	 *            The passenger's current floor.
	 * @param destination
	 *            The passenger's destination.
	 */
	Passenger(int id, int floor, int destination)
	{
		this.id = id;
		this.destination = destination;
		if ((destination - floor) > 0)
		{
			this.direction = ElevatorSimulation.Direction.UP;
		}
		else
		{
			this.direction = ElevatorSimulation.Direction.DOWN;
		}

	}

	/**
	 * Tests if the passenger needs to get out at this floor.
	 * 
	 * @param floor
	 *            The floor the elevator is on.
	 * @return True if the floor matches the destination.
	 */
	public boolean shouldGetOut(int floor)
	{
		return (floor == this.destination);
	}

	/**
	 * Tests if the passenger needs to get in to an elevator moving a specific
	 * way.
	 * 
	 * @param direction
	 *            The direction the elevator is going.
	 * @return True if the direction of the elevator matches the direction of
	 *         the passenger.
	 */
	public boolean shouldGetIn(ElevatorSimulation.Direction direction)
	{
		return (direction == this.direction);
	}

	/**
	 * Makes the passenger "wait" one click by progressing its internal counter.
	 */
	public void waitClick()
	{
		timeWaited++;
	}
}