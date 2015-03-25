/******************************************************************************
 * 
 * Name:		Akshay Srivatsan
 * Block:		D
 * Date:		10/1/13
 * 
 *  Program #A:	Elevator Simulation
 *  Description: This program runs a simulation of elevators in a building, given
 *  	passengers coming in from a file named "requests.txt". This program
 *  	simulates one time click, prints the world state to the console, and then
 *  	waits for a console.nextLine() before continuing.
 * 
 ******************************************************************************/
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * This program reads in elevator requests from a file (FILE_NAME), and then
 * adds those people into a simulated universe, where they are serviced by
 * NUM_ELEVATORS elevators. The main class sets up the simulation, reads in the
 * file, adds people to floors, and tells elevators to move. The special
 * requirements for the input file are: there must be a final time click (2)
 * before the end (4). This code may take longer than other code using the same
 * strategy, since my elevators cannot load and move in the same time click (to
 * make it more realistic).
 * 
 * @author Akshay
 * 
 */
@SuppressWarnings("serial")
public class ElevatorSimulation extends JFrame implements ActionListener, KeyListener
{
	static final String FONT = "Frutiger";
	static final int FONTSIZE = 30;

	/**
	 * This type represents one of the three possible directions: up, down, or
	 * not moving.
	 * 
	 * @author Akshay
	 * 
	 */
	public enum Direction
	{
		UP, DOWN, NONE
	};

	static final String FILE_NAME = "requests.txt";

	/*
	 * These variables are styled as if they were static because they are
	 * initialized based on the user's input in the constructor.
	 */
	static int NUM_FLOORS;
	static int NUM_ELEVATORS;
	/*
	 * The reason for having a lowest floor value is in case you have, for
	 * example, two sets of elevators (1É10 and 10É20). It has no functional
	 * difference, just an appearance difference.
	 */
	static int LOWEST_FLOOR;

	private final int WIDTH;
	static final int HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

	static boolean enterKeyFlag = false;
	static boolean readingValue = false;
	static int enteredValue = 0;

	/*
	 * These class scope variables are flags, which can tell paint to override the default output.
	 */
	static boolean askForTimeClicks = false;
	static boolean askForElevators = false;
	static boolean askForFloors0 = false;
	static boolean askForFloors1 = false;
	static boolean errorMessage = false;
	static boolean errorFile = false;
	static final String MESSAGE0 = "How many time clicks do you want to simulate (0É°)?";
	static final String MESSAGE1 = "Enter zero to simulate until completed.";
	static final String MESSAGE2 = "How many elevators do you want to simulate (1É°)?";
	static final String MESSAGE3 = "How many floors do you want to simulate (1É°)?";
	static final String MESSAGE4 = "What floor should the simulation start from (1É°)?";
	static final String MESSAGE_ERROR = "There were not enough floors in the building.";
	static final String MESSAGE_FILE_ERROR = "There was an error opening the file " + System.getProperty("user.dir") + "/" + FILE_NAME + ".";

	static Scanner requestsFile = null;
	static Scanner console = null;

	static boolean doneWithFile = false;
	static boolean done = false;

	static int countClicks = 0;

	/*
	 * The following class scope variables are only used in main() and paint(),
	 * since they are in different threads. The rest of the program uses
	 * parameters to access these arrays.
	 */
	Floor buildingClassScope[];
	Elevator elevatorsClassScope[];
	
	/*
	 * The values are: 0 = timeClicks; 1 = maxClicks. This array is used to
	 * create pointers to the two variables, allowing both threads to access it.
	 */
	static int valuesClassScope[] = { 0, 0 };

	/**
	 * The main drawing method of the simulation.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	public void paint(Graphics g)
	{
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(Color.black);
		g.setFont(new Font(FONT, Font.PLAIN, 30));
		figureOutWhatToDraw(g);

	}

	/**
	 * Based on flag variables, figures out what to draw.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void figureOutWhatToDraw(Graphics g)
	{
		if (askForTimeClicks)
		{
			drawTimeClicksQ(g);
		}
		else if (askForElevators)
		{
			drawElevatorsQ(g);
		}
		else if (askForFloors0)
		{
			drawFloorsNumberQ(g);
		}
		else if (askForFloors1)
		{
			drawFloorStartingQ(g);
		}
		else if (errorMessage)
		{
			drawError(g);
		}
		else if (done)
		{
			drawStatistics(g);
		}
		else
		{
			drawUniverse(g);
		}
	}

	/**
	 * Draws an error message.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawError(Graphics g)
	{
		if (errorFile)
		{
			drawErrorFile(g);
			return;
		}
		g.setColor(Color.red);
		g.drawString(MESSAGE_ERROR, 10, FONTSIZE * 2);
	}
	
	/**
	 * Draws an error message about the file.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawErrorFile(Graphics g)
	{
		g.setColor(Color.red);
		g.drawString(MESSAGE_FILE_ERROR, 10, FONTSIZE * 2);
	}

	/**
	 * Draws a prompt for the starting floor.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawFloorStartingQ(Graphics g)
	{
		/* Ask for the starting floor. */
		g.drawString(MESSAGE4, 10, FONTSIZE * 2);
		g.drawString(String.valueOf(enteredValue), 10, FONTSIZE * 4);
	}

	/**
	 * Draws a prompt for the number of floors.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawFloorsNumberQ(Graphics g)
	{
		/* Ask for the number of floors. */
		g.drawString(MESSAGE3, 10, FONTSIZE * 2);
		g.drawString(String.valueOf(enteredValue), 10, FONTSIZE * 4);
	}

	/**
	 * Draws a prompt for the number of elevators.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawElevatorsQ(Graphics g)
	{
		/* Ask for the number of elevators */
		g.drawString(MESSAGE2, 10, FONTSIZE * 2);
		g.drawString(String.valueOf(enteredValue), 10, FONTSIZE * 4);
	}

	/**
	 * Draws a prompt for the number of time clicks.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawTimeClicksQ(Graphics g)
	{
		/* Ask for the number of time clicks. */
		g.drawString(MESSAGE0, 10, FONTSIZE * 2);
		g.drawString(MESSAGE1, 10, FONTSIZE * 4);
		g.drawString(String.valueOf(enteredValue), 10, FONTSIZE * 6);
	}

	/**
	 * The most important drawing method Ð tells the floors and elevators to
	 * draw themselves.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawUniverse(Graphics g)
	{
		for (int i = LOWEST_FLOOR; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			buildingClassScope[i].draw(g);
		}
		for (int i = 0; i < NUM_ELEVATORS; i++)
		{
			elevatorsClassScope[i].draw(g);
		}
		if (!(doneWithFile))
		{
			g.setColor(Color.red);
		}
		else
		{
			g.setColor(Color.blue);
		}
		String completedClicks = "";
		if (valuesClassScope[1] == 0)
		{
			completedClicks = "°";
		}
		else
		{
			completedClicks = String.valueOf(valuesClassScope[1]);
		}
		g.drawString("Time Click " + valuesClassScope[0] + "/" + completedClicks, 10, 50);
	}

	/**
	 * A method that runs at the end of the simulation, drawing statistics onto
	 * the graphics window.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawStatistics(Graphics g)
	{
		drawCalculatedStatistics(g);
		drawUniverseFacts(g);
	}

	/**
	 * A method that draws facts about how many people are: waiting, traveling,
	 * and done.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawUniverseFacts(Graphics g)
	{
		g.drawString(valuesClassScope[0] + " time clicks have been completed.", 100, FONTSIZE * 2);

		int count;
		count = 0;
		for (int i = LOWEST_FLOOR; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			count += buildingClassScope[i].getPassengers().size();
		}
		g.drawString(count + " passengers are still on their starting floor.", 100, FONTSIZE * 8);

		count = 0;
		for (int i = 0; i < NUM_ELEVATORS; i++)
		{
			count += elevatorsClassScope[i].getPassengers().size();
		}
		g.drawString(count + " passengers are still inside an elevator.", 100, FONTSIZE * 10);

		count = 0;
		for (int i = LOWEST_FLOOR; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			count += buildingClassScope[i].peopleArrived;
		}
		g.drawString(count + " passengers have been serviced.", 100, FONTSIZE * 12);
		drawCompletionState(g);
	}

	/**
	 * Draws information about whether the elevators have completed their
	 * movement.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawCompletionState(Graphics g)
	{
		if (!doneWithFile)
		{
			g.setColor(Color.red);
			g.drawString("Requests are still coming in.", 100, FONTSIZE * 14);
		}
		else if (doneWithFile && countClicks != 0)
		{
			g.setColor(Color.blue);
			g.drawString("The elevators finished after " + countClicks + " time clicks.", 100, FONTSIZE * 14);
		}
		else
		{
			g.setColor(Color.orange);
			g.drawString("The elevators are still moving.", 100, FONTSIZE * 14);
		}
	}

	/**
	 * A method that draws statistics that involve calculations based on data
	 * from the floors.
	 * 
	 * @param g
	 *            The graphics element to draw on.
	 */
	private void drawCalculatedStatistics(Graphics g)
	{
		double mean;
		int sum = 0, maxWait = 0, count = 0;
		for (int i = LOWEST_FLOOR; i <= NUM_FLOORS; i++)
		{
			sum += buildingClassScope[i].waitSum;
			count += buildingClassScope[i].peopleArrived;
			if (maxWait < buildingClassScope[i].waitMax)
			{
				maxWait = buildingClassScope[i].waitMax;
			}
		}
		mean = sum / (double) count;

		g.drawString("The average wait time is " + String.format("%.2f", mean) + " time clicks.", 100, FONTSIZE * 4);
		g.drawString("The maximum wait time is " + maxWait + " time clicks.\n", 100, FONTSIZE * 6);
	}

	/**
	 * Reads a digit from the keyboard and adds it to the number
	 * <i>enteredValue</i>.
	 * 
	 * @param e
	 *            The keypress event.
	 */
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		if ((keyCode == KeyEvent.VK_ENTER))
		{
			enterKeyFlag = true;
		}
		else if ((keyCode >= KeyEvent.VK_0) && (keyCode <= KeyEvent.VK_9) && readingValue)
		{
			enteredValue = (enteredValue * 10) + (keyCode - KeyEvent.VK_0);
		}
		else if (keyCode == KeyEvent.VK_BACK_SPACE)
		{
			enteredValue = enteredValue / 10;
		}
		repaint();
	}

	/**
	 * Necessary method - does nothing.
	 * 
	 * @param e
	 *            The keypress event.
	 */
	public void keyTyped(KeyEvent e)
	{
	}

	/**
	 * Necessary method - does nothing.
	 * 
	 * @param e
	 *            The keypress event.
	 */
	public void keyReleased(KeyEvent e)
	{
	}

	/**
	 * Causes the program to hang until [ENTER] is pressed.
	 */
	public static void waitForKey()
	{
		while (!enterKeyFlag)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
			}
		}
		enterKeyFlag = false;
	}

	/**
	 * Waits for a number to be entered, followed by a [ENTER] press.
	 * 
	 * @return The entered number.
	 */
	public static int nextInt()
	{
		readingValue = true;
		while (!enterKeyFlag)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
			}
		}
		enterKeyFlag = false;
		readingValue = false;
		int retval = enteredValue;
		enteredValue = 0;
		return retval;
	}

	/**
	 * The main method in the simulation. Creates an ElevatorSimulation object.
	 * 
	 * @param args
	 *            The command-line parameters passed to the program.
	 */
	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		ElevatorSimulation gp = new ElevatorSimulation();

	}

	/**
	 * The constructor for the class. It sets up the environment for the
	 * simulation, then uses other methods to manage the simulation.
	 */
	ElevatorSimulation()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Elevator Simulation");
		FontMetrics fontm = this.getFontMetrics(new Font(FONT, Font.PLAIN, FONTSIZE));
		setSize(Math.max(fontm.stringWidth(MESSAGE0) + 20, fontm.stringWidth(MESSAGE_FILE_ERROR) + 20), HEIGHT);
		setVisible(true);
		addKeyListener(this);

		Timer clock = new Timer(100, this);
		clock.start();
		
		if (!openFile())
		{
			while (true)
			{
			}
		}
		int timeClicks = 0, maxClicks = readInValues();

		WIDTH = Math.max((NUM_ELEVATORS * (Elevator.WIDTH + 10)) + Floor.WIDTH + 50, fontm.stringWidth(MESSAGE0) + 20);
		setSize(WIDTH, HEIGHT);

		initArrays();
		/*
		 * These lines are for console operation. System.out.print(
		 * "How many time clicks do you want to simulate?\nEnter 0 to simulate until all passengers are serviced."
		 * ); maxClicks = console.nextInt();
		 */
		runMainProgram(timeClicks, maxClicks);
	}

	/**
	 * This method sets certain flag variables, which are then read by the
	 * graphics routine to display a message and read in numbers, which are then
	 * passed back here.
	 * 
	 * @return The maximum number of clicks the user specified.
	 */
	private int readInValues()
	{
		askForTimeClicks = true;
		int maxClicks = nextInt();
		valuesClassScope[1] = maxClicks;
		askForTimeClicks = false;
		do
		{
			askForElevators = true;
			NUM_ELEVATORS = nextInt();
			askForElevators = false;
		} while (NUM_ELEVATORS < 1);

		do
		{
			askForFloors0 = true;
			NUM_FLOORS = nextInt();
			askForFloors0 = false;
		} while (NUM_FLOORS < 1);

		do
		{
			askForFloors1 = true;
			LOWEST_FLOOR = nextInt();
			askForFloors1 = false;
		} while (LOWEST_FLOOR < 1);
		return maxClicks;
	}

	/**
	 * This method is responsible for simulating all the time clicks.
	 * 
	 * @param timeClicks
	 *            The number of time clicks that have passed.
	 * @param maxClicks
	 *            The number of time clicks to simulate up to.
	 */
	private void runMainProgram(int timeClicks, int maxClicks)
	{
		try
		{
			if (maxClicks == 0)
			{
				timeClicks = handleRequests(timeClicks, Integer.MAX_VALUE, buildingClassScope, elevatorsClassScope);
			}
			else
			{
				timeClicks = handleRequests(timeClicks, maxClicks, buildingClassScope, elevatorsClassScope);
			}
			if (timeClicks != maxClicks)
			{
				doneWithFile = true;
			}
			timeClicks = finishRequests(timeClicks, maxClicks, buildingClassScope, elevatorsClassScope);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			errorMessage = true;
			return;
		}

		done = true;
		printStatistics(buildingClassScope, elevatorsClassScope, timeClicks);
	}

	/**
	 * Sets up the arrays of floors and elevators.
	 */
	private void initArrays()
	{
		/*
		 * The number of spots in the array is the number of floors greater than
		 * the lowest floor. Any floors below the lowest floor exist, but cannot
		 * be accessed by the elevators.
		 */
		buildingClassScope = new Floor[NUM_FLOORS + LOWEST_FLOOR];
		elevatorsClassScope = new Elevator[NUM_ELEVATORS];

		for (int i = 0; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			buildingClassScope[i] = new Floor(i);
		}
		for (int i = 0; i < NUM_ELEVATORS; i++)
		{
			elevatorsClassScope[i] = new Elevator(i, ((NUM_FLOORS - LOWEST_FLOOR) / 2) + LOWEST_FLOOR);
		}
	}

	/**
	 * This method is called whenever the timer in the constructor goes off. It
	 * repaints the screen every tenth of a second.
	 * 
	 * @param e
	 *            The keypress event.
	 */
	public void actionPerformed(ActionEvent e)
	{
		repaint();
	}

	/* The next methods all print out data about the simulation. */

	/**
	 * Prints out statistics about the elevator.
	 * 
	 * @param building
	 *            An array of floors.
	 * @param elevators
	 *            An array of elevators.
	 * @param timeClicks
	 *            The number of time clicks the passed.
	 */
	private static void printStatistics(Floor[] building, Elevator[] elevators, int timeClicks)
	{

		System.out.println((timeClicks) + " time clicks have passed.");

		printMaxAndMean(building);
		printServiced(building);
		printInsideElevator(elevators);
		printWaiting(building);
		printCompletion();
		System.out.println("\nDone.");
	}

	/**
	 * Prints information about whether the elevators have completed.
	 */
	private static void printCompletion()
	{
		if (!doneWithFile)
		{
			System.out.println("Requests are still coming in.");
		}
		else if (doneWithFile && countClicks != 0)
		{
			System.out.println("The elevators finished after " + countClicks + " time clicks.");
		}
		else
		{
			System.out.println("The elevators are still moving.");
		}
	}

	/**
	 * Prints the number of people still waiting for the elevator.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	private static void printWaiting(Floor[] building)
	{
		int count = 0;
		for (int i = LOWEST_FLOOR; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			count += building[i].getPassengers().size();
		}
		System.out.println(count + " passengers are still on their starting floor.");
	}

	/**
	 * Prints the number of people still inside an elevator.
	 * 
	 * @param elevators
	 *            An array of elevators.
	 */
	private static void printInsideElevator(Elevator[] elevators)
	{
		int count = 0;
		for (int i = 0; i < NUM_ELEVATORS; i++)
		{
			count += elevators[i].getPassengers().size();
		}
		System.out.println(count + " passengers are still inside an elevator.");
	}

	/**
	 * Prints the number of people who have reached their destination.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	private static void printServiced(Floor[] building)
	{
		int count = 0;
		for (int i = LOWEST_FLOOR; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			count += building[i].peopleArrived;
		}
		System.out.println(count + " passengers have been serviced.");
	}

	/**
	 * Prints the maximum and average wait times among people who have reached
	 * their destination.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	private static void printMaxAndMean(Floor[] building)
	{
		double mean;
		int sum = 0, count = 0, maxWait = 0;
		for (int i = LOWEST_FLOOR; i <= NUM_FLOORS; i++)
		{
			sum += building[i].waitSum;
			count += building[i].peopleArrived;
			if (maxWait < building[i].waitMax)
			{
				maxWait = building[i].waitMax;
			}
		}
		mean = sum / (double) count;

		System.out.format("The average wait time is %.2f time clicks.%n", mean);
		System.out.println("The maximum wait time is " + maxWait + " time clicks.\n");
	}

	/* Done with statistics. */

	/**
	 * Gets requests.txt and the console ready to use.
	 * 
	 * @return Success - true means the scanners were opened successfully, false
	 *         means there was an error.
	 */
	public static boolean openFile()
	{
		try
		{
			requestsFile = new Scanner(new BufferedReader(new FileReader(FILE_NAME)));
			console = new Scanner(System.in);
		}
		catch (IOException fileException)
		{
			errorMessage = true;
			errorFile = true;
			System.out.println("Error opening file.");
			return false;
		}
		return true;
	}

	/**
	 * The main logic of the request handling. The method reads the next
	 * instruction from the file, and based on that adds passengers or
	 * progresses time.
	 * 
	 * @param timeClicks
	 *            The number of time clicks that have passed so far. In most
	 *            cases, this is 0 at the beginning.
	 * @param maxClicks
	 *            The number of clicks to run until.
	 * @param building
	 *            An array that contains every floor.
	 * @param elevators
	 *            An array that contains every elevator.
	 * @return The number of time clicks that progressed during this method.
	 */
	public static int handleRequests(int timeClicks, int maxClicks, Floor[] building, Elevator[] elevators)
	{
		int nextInstruction = 0;
		for (int i = 0; i < maxClicks; i++)
		{
			nextInstruction = requestsFile.nextInt();
			if (nextInstruction == 4)
			{
				return timeClicks;
			}
			while (nextInstruction != 2)
			{
				switch (nextInstruction)
				{
				case 3:
					addPassenger(building);
					break;
				}
				requestsFile.nextLine();
				nextInstruction = requestsFile.nextInt();
			}
			timeClicks = timeClick(timeClicks, building, elevators);
			waitForKey();
			/*
			 * This is for console operation. console.nextLine();
			 */
			requestsFile.nextLine();
		}
		return timeClicks;
	}

	/**
	 * The method that finishes the time clicks that the user specified, if
	 * there are more than in the file.
	 * 
	 * @param timeClicks
	 *            The number of clicks that have already passed.
	 * @param maxClicks
	 *            The number of clicks to stop at.
	 * @param building
	 *            An array of all the floors.
	 * @param elevators
	 *            An array of all the elevators.
	 * @return The number of time clicks after the method is finished.
	 */
	public static int finishRequests(int timeClicks, int maxClicks, Floor[] building, Elevator[] elevators)
	{
		int i = timeClicks;
		while ((i < maxClicks && maxClicks > 0) || (maxClicks == 0 && countClicks == 0))
		{
			timeClicks = timeClick(timeClicks, building, elevators);
			int count = 0;

			for (int j = LOWEST_FLOOR; j < (LOWEST_FLOOR + NUM_FLOORS); j++)
			{
				count += building[j].getPassengers().size();
			}
			for (int j = 0; j < NUM_ELEVATORS; j++)
			{
				count += elevators[j].getPassengers().size();
			}
			if (count == 0 && countClicks == 0)
			{
				countClicks = timeClicks;
			}

			waitForKey();
			/*
			 * This is for console operation. console.nextLine();
			 */
			i++;
		}
		return timeClicks;
	}

	/**
	 * Read a passenger's data from the file, and add the passenger to the
	 * building.
	 * 
	 * @param building
	 *            An array of all the floors.
	 */
	public static void addPassenger(Floor[] building)
	{
		int id = requestsFile.nextInt();
		int floor = requestsFile.nextInt();
		int destination = requestsFile.nextInt();
		System.out.println("Adding person " + id + " on floor " + floor + " going to floor " + destination + ".");

		building[floor].addPerson(new Passenger(id, floor, destination));
	}

	/*
	 * The next several methods are all used to print out elevator statistics,
	 * load/unload passengers, scan for passengers, or move elevators during one
	 * time click.
	 */
	/**
	 * This method prints out a header, and then loops through the elevators,
	 * printing out their data.
	 * 
	 * @param timeClicks
	 *            The number of time clicks that have passed.
	 * @param building
	 *            An array of the floors.
	 * @param elevators
	 *            An array of elevators.
	 * @return The number of clicks that passed at the end (should always be 1
	 *         more than the starting value).
	 */
	public static int timeClick(int timeClicks, Floor[] building, Elevator[] elevators)
	{
		timeClicks++;
		valuesClassScope[0] = timeClicks;
		printMainHeader(timeClicks, building);
		for (int i = 0; i < NUM_ELEVATORS; i++)
		{
			printElevator(building, elevators, i);
		}
		makePassengersWaitClick(building);
		System.out.println("####################\n");
		return timeClicks;
	}

	/**
	 * Makes every passenger not on an elevator "wait" one time click.
	 * 
	 * @param building
	 *            An array of floors.
	 */
	private static void makePassengersWaitClick(Floor[] building)
	{
		for (int i = LOWEST_FLOOR; i <= NUM_FLOORS; i++)
		{
			Iterator<Passenger> iterator = building[i].people.iterator();
			while (iterator.hasNext())
			{
				iterator.next().waitClick();
			}
		}
	}

	/**
	 * Print out the data for one elevator.
	 * 
	 * @param building
	 *            An array of floors.
	 * @param elevators
	 *            An array of elevators.
	 * @param i
	 *            The elevator number.
	 */
	private static void printElevator(Floor[] building, Elevator[] elevators, int i)
	{
		printElevatorHeader(elevators, i);
		if (elevators[i].open)
		{
			loadUnloadElevatorPassengers(building, elevators, i);
		}
		else
		{
			moveOrScanElevator(building, elevators, i);
		}
		System.out.println("********************");
	}

	/**
	 * Depending on whether the elevator is open or closed, this method scans
	 * for people to load or moves one floor.
	 * 
	 * @param building
	 *            An array of floors.
	 * @param elevators
	 *            An array of elevators.
	 * @param i
	 *            The elevator number.
	 */
	private static void moveOrScanElevator(Floor[] building, Elevator[] elevators, int i)
	{
		System.out.println("Closed");
		if (elevators[i].getDirection() == Direction.NONE)
		{
			/* The elevator is idle.. */
			System.out.println("Idle...");
			System.out.println("Scanning...");
			elevators[i].scanForPeople(building);
		}
		else
		{
			/* The elevator is moving... */
			elevators[i].move(building);
		}
	}

	/**
	 * This method prints a header, and then depending on whether the elevator
	 * is stopped (direction == NONE), it chooses how to load passengers. The
	 * options for loading are: load in the direction of the elevator; load in
	 * whichever direction you find people in first, UP or DOWN;
	 * 
	 * @param building
	 *            An array of floors.
	 * @param elevators
	 *            An array of elevators.
	 * @param i
	 *            The elevator number.
	 */
	private static void loadUnloadElevatorPassengers(Floor[] building, Elevator[] elevators, int i)
	{
		printLoadingHeader(building, elevators, i);
		if (elevators[i].getDirection() == Direction.NONE)
		{
			loadPassengersWhileStopped(building, elevators, i);
		}
		else
		{
			elevators[i].loadPassengers(building[elevators[i].floor].getPeopleToLoad(elevators[i].getDirection()));
		}
		printElevatorFooter();
		elevators[i].open = false;
	}

	/**
	 * This method prints a footer for the loading sections.
	 */
	private static void printElevatorFooter()
	{
		System.out.println("@@ Done Loading. @@\n");
		System.out.println("Closing.");
	}

	/**
	 * This method looks both ways for passengers waiting, and loads passengers
	 * in the first direction it finds them in: UP or DOWN.
	 * 
	 * @param building
	 *            An array of floors.
	 * @param elevators
	 *            An array of elevators.
	 * @param i
	 *            The elevator number.
	 */
	private static void loadPassengersWhileStopped(Floor[] building, Elevator[] elevators, int i)
	{
		elevators[i].setDirection(Direction.UP);
		ArrayList<Passenger> p = building[elevators[i].floor].getPeopleToLoad(Direction.UP);
		if (p.isEmpty())
		{
			elevators[i].setDirection(Direction.DOWN);
			p = building[elevators[i].floor].getPeopleToLoad(Direction.DOWN);
		}
		if (!p.isEmpty())
		{
			elevators[i].loadPassengers(p);
			System.out.println(elevators[i].getDirection());
		}
		else
		{
			elevators[i].setDirection(Direction.NONE);
		}
	}

	/**
	 * This method prints out the current time click, then loops through the
	 * floors and prints them out.
	 * 
	 * @param timeClicks
	 *            The number of time clicks that have passed.
	 * @param building
	 *            An array of floors. An array of floors.
	 */
	private static void printMainHeader(int timeClicks, Floor[] building)
	{
		System.out.println("\n## Time Click: " + timeClicks + " ###");
		for (int i = LOWEST_FLOOR; i < (LOWEST_FLOOR + NUM_FLOORS); i++)
		{
			System.out.println("$$$$ Floor " + i + " $$$$");
			printPassengers(building[i].getPassengers());
			System.out.println("$$$$$$$$$$$$$$$$$\n");
		}
	}

	/**
	 * This method prints out the information about loading/unloading
	 * passengers.
	 * 
	 * @param building
	 *            An array of floors.
	 * @param elevators
	 *            An array of elevators.
	 * @param i
	 *            The elevator number.
	 */
	private static void printLoadingHeader(Floor[] building, Elevator[] elevators, int i)
	{
		System.out.println("Open:");
		System.out.println("@@@@@ Unloading @@@@@");
		building[elevators[i].floor].unloadPassengers(elevators[i].unloadPassengers());
		System.out.println("@@ Done Unloading. @@\n");
		System.out.println("@@@@@ Loading @@@@@");
	}

	/**
	 * This method prints a header for an elevator containing the number, floor,
	 * destination, plan, and passengers.
	 * 
	 * @param elevators
	 *            An array of elevators.
	 * @param i
	 *            The elevator number.
	 */
	private static void printElevatorHeader(Elevator[] elevators, int i)
	{
		System.out.println("**** Elevator " + i + " ****");
		System.out.println("Floor: " + elevators[i].floor);
		System.out.println("Direction: " + elevators[i].getDirection());
		System.out.println("Planned Direction: " + elevators[i].getPlannedDirection());
		System.out.println("---- Passengers ----");
		ElevatorSimulation.printPassengers(elevators[i].getPassengers());
		System.out.println("--------------------");
	}

	/* This is the end of all the methods that occur within one time click. */

	/**
	 * A method to print out all the people in a given list of passengers.
	 * 
	 * @param passengers
	 *            The list to print out.
	 */
	public static void printPassengers(ArrayList<Passenger> passengers)
	{
		Iterator<Passenger> i = passengers.iterator();
		Passenger next = null;
		if (!i.hasNext())
		{
			System.out.println("No people here.");
		}
		while (i.hasNext())
		{
			next = (Passenger) i.next();
			System.out.println("Passenger " + next.id + " going to floor " + next.destination + ".");
		}
	}
}