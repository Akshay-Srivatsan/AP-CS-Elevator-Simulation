import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The floor class represents a real-life floor.
 * 
 * @author Akshay
 * 
 */
public class Floor
{
	static final int XCOORDINATE = (Elevator.WIDTH + 10) * ElevatorSimulation.NUM_ELEVATORS + 20;
	static final int WIDTH = 500;
	static final int HEIGHT = (ElevatorSimulation.HEIGHT - 50) / (ElevatorSimulation.NUM_FLOORS + 1);

	public int id;
	public boolean goingUp = false;
	public boolean goingDown = false;
	public boolean handledUp = false;
	public boolean handledDown = false;
	public int peopleArrived = 0;
	public int waitSum = 0;
	public int waitMax = 0;

	public ArrayList<Passenger> people = new ArrayList<Passenger>();
	public ArrayList<Passenger> arrivedPeople = new ArrayList<Passenger>();

	/**
	 * The initializer for a floor.
	 * 
	 * @param id
	 *            The floor number.
	 */
	Floor(int id)
	{
		this.id = id;
	}

	/**
	 * A getter for the going up "button".
	 * 
	 * @return The status of the button.
	 */
	public boolean requestedUp()
	{
		return goingUp;
	}

	/**
	 * A getter for the going down "button".
	 * 
	 * @return The status of the button.
	 */
	public boolean requestedDown()
	{
		return goingDown;
	}

	/**
	 * This method looks for people who need to get on to an elevator moving in
	 * the given direction, and extracts them from the floor.
	 * 
	 * @param direction
	 *            The direction the elevator is moving.
	 * @return The passengers to get in.
	 */
	public ArrayList<Passenger> getPeopleToLoad(ElevatorSimulation.Direction direction)
	{
		if (direction == ElevatorSimulation.Direction.UP)
		{
			this.handledUp = false;
		}
		else if (direction == ElevatorSimulation.Direction.DOWN)
		{
			this.handledDown = false;
		}
		ArrayList<Passenger> retval = new ArrayList<Passenger>();
		Iterator<Passenger> floorPeople = people.iterator();
		System.out.println("Looking for people going " + direction + ".");
		while (floorPeople.hasNext())
		{
			addPersonToElevator(direction, retval, floorPeople);
		}
		if (direction == ElevatorSimulation.Direction.DOWN)
		{
			this.goingDown = false;
		}
		if (direction == ElevatorSimulation.Direction.UP)
		{
			this.goingUp = false;
		}
		System.out.println();
		return retval;

	}

	/**
	 * Adds a person to the return array.
	 * 
	 * @param direction
	 *            The direction to look for.
	 * @param retval
	 *            The return array.
	 * @param floorPeople
	 *            The floor's people.
	 */
	private void addPersonToElevator(ElevatorSimulation.Direction direction, ArrayList<Passenger> retval, Iterator<Passenger> floorPeople)
	{
		Passenger currentPassenger = null;
		currentPassenger = floorPeople.next();
		System.out.print("Passenger " + currentPassenger.id + " is going " + currentPassenger.direction + ".");
		if (currentPassenger.shouldGetIn(direction))
		{
			System.out.print("*");
			retval.add(currentPassenger);
			floorPeople.remove();
		}
		System.out.println();
	}

	/**
	 * This method adds one Passenger object to the floor, and "presses" a
	 * direction "button", if necessary.
	 * 
	 * @param person
	 *            The passenger to add.
	 */
	public void addPerson(Passenger person)
	{
		people.add(person);
		if (person.direction == ElevatorSimulation.Direction.UP)
		{
			this.goingUp = true;
		}
		else
		{
			this.goingDown = true;
		}
	}

	/**
	 * This method receives passengers from the elevator, and counts them.
	 * 
	 * @param peopleToLoad
	 *            The list of passengers.
	 */
	public void unloadPassengers(ArrayList<Passenger> peopleToLoad)
	{
		Iterator<Passenger> loader = peopleToLoad.iterator();
		Passenger next = null;
		while (loader.hasNext())
		{
			next = loader.next();
			unloadPerson(next);
		}
	}

	/**
	 * This method unloads a single person onto the floor, incrementing the
	 * counter.
	 * 
	 * @param person
	 *            The passenger to unload.
	 */
	public void unloadPerson(Passenger person)
	{
		System.out.println("Person " + person.id + " exited on floor " + this.id + ".");
		this.peopleArrived++;
		this.waitSum += person.timeWaited;
		if (person.timeWaited > this.waitMax)
		{
			this.waitMax = person.timeWaited;
		}
		arrivedPeople.add(person);
	}

	/**
	 * This methods gets a list of passengers waiting on this floor (for
	 * displaying purposes).
	 * 
	 * @return A list of passengers on this floor.
	 */
	public ArrayList<Passenger> getPassengers()
	{
		return this.people;
	}

	/**
	 * The code to draw a floor's boundary, number, and passengers.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	public void draw(Graphics g)
	{
		int yCoord = (HEIGHT * (ElevatorSimulation.NUM_FLOORS + 1)) - (HEIGHT * (this.id)) + ElevatorSimulation.FONTSIZE + 10;
		g.setColor(Color.black);
		g.drawLine(XCOORDINATE, yCoord, XCOORDINATE + WIDTH, yCoord);
		g.drawString(String.valueOf(this.id), XCOORDINATE + WIDTH, yCoord);
		int maxWidth = g.getFontMetrics(new Font(ElevatorSimulation.FONT, Font.PLAIN, ElevatorSimulation.FONTSIZE)).stringWidth("00-00 ");
		for (int i = 0; i < Math.min(WIDTH / maxWidth + 20, people.size()); i++)
		{
			g.drawString(String.valueOf(people.get(i).id) + ":" + String.valueOf(people.get(i).destination), XCOORDINATE + i * (maxWidth) + 20, yCoord - HEIGHT
					/ 2);
		}

		g.setColor(Color.blue);
		maxWidth = g.getFontMetrics(new Font(ElevatorSimulation.FONT, Font.PLAIN, ElevatorSimulation.FONTSIZE)).stringWidth("00:00 ");
		for (int i = 0; i < Math.min(WIDTH / maxWidth + 20, arrivedPeople.size()); i++)
		{
			g.drawString(String.valueOf(arrivedPeople.get(i).id) + "-" + String.valueOf(arrivedPeople.get(i).timeWaited), XCOORDINATE + i * (maxWidth) + 20,
					yCoord - 10);
		}

		drawButtons(g, yCoord);
	}

	/**
	 * Draws elevator buttons: gray = not pressed; yellow = pressed; green =
	 * being handled.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 * @param yCoord
	 *            The y coordinate to draw at.
	 */
	private void drawButtons(Graphics g, int yCoord)
	{
		g.setColor(Color.darkGray);
		if (this.goingUp)
		{
			g.setColor(Color.yellow);
		}
		if (this.handledUp)
		{
			g.setColor(Color.green);
		}

		g.fillOval(XCOORDINATE, yCoord - (HEIGHT * 2 / 3), 10, 10);

		g.setColor(Color.darkGray);
		if (this.goingDown)
		{
			g.setColor(Color.yellow);
		}
		if (this.handledDown)
		{
			g.setColor(Color.green);
		}

		g.fillOval(XCOORDINATE, yCoord - (HEIGHT * 1 / 3), 10, 10);
	}
}