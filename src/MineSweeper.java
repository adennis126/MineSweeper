import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
/**
 * MineSweeper
 * @author Channy Uon 6189318, Cong Lu 3080021, Cedric Abou Farhat 7032633
 * An enhance Mine Sweeper game with added features like
 * save/load state and extra items
 */
class MineSweeper implements Serializable, ActionListener, MouseListener {
	private static final long serialVersionUID = -6246198092905731927L;
	
	private final int GRID_SIZE = 15;
	//array of button
	private JButton[][] field = new JButton[GRID_SIZE][GRID_SIZE];
	//to compare current and saved state of buttons
	private JButton[][] saved = new JButton[GRID_SIZE][GRID_SIZE];
	
	//Determine if an item is a mine, probe, shield, bonus, or an immortal
	private final int MINE = 31;
	private final int PROBE = 1001;
	private final int SHIELD = 1501;
	private int BONUS = 1701;
	private final int IMMORTAL = 1601;
	
	//Number of each items
	private int numOfMines = 30;
	private int	numOfProbes = 2;
	private int	numOfShields = 2;
	private int numOfBonus = 2;
	private int numOfImmortal = 1;
	
	//lives and items
	private int lives = 3;
	private int probeUse;
	private int shieldStrength;
	private int bonusTime = 5;
	
	//field items, coordinates and the frame
	private int[][] fieldItems = new int[GRID_SIZE][GRID_SIZE];
	private int x;
	private int y;
	private JFrame mineFrame;
	
	//top menu section
	private Container menu = new Container();
	private JButton reset = new JButton("Reset Game");
	private JButton load = new JButton("Load State");
	private JButton save = new JButton("Save State");
	private JTextField displayLives = new JTextField("Number of lives remaining: " + Integer.toString(lives));
	private JTextField currentItem = new JTextField("Current Item in Possession: ");
	private JTextField scoreInfo = new JTextField("");
	private Container body = new Container();
	private Container centerMenu = new Container();
	
	//timer attributes
	private int everySecond = 1000;
	private int score = 0; 
	private JTextArea top10; 
	private Timer timer;
	private int[] top10List = new int[10];
	
	/**
	 * MineSweeper()
	 * Constructor for the game
	 */
	public MineSweeper(){
		
		//creating the frame
		JFrame mineFrame = new JFrame("Enhanced Mine Sweeper");
		mineFrame.setSize(1000,700);
		
		//terminate when pressing the x on the frame
		mineFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		//for grid layout
		mineFrame.setLayout(new BorderLayout());
		
		frameComponents(mineFrame);
		
		//allows the frame to be visible
		mineFrame.setVisible(true);
		
	}
	
	/**
	 * scoreTiming()
	 * creates a timer that increases every second and displays it as the score
	 */
	public void scoreTiming(){
		//timer has it own listener
		timer = new Timer(everySecond, new ActionListener(){
			public void actionPerformed(ActionEvent event) {	
				score++;
				scoreInfo.setText("Score " + score);
			}
		});
		//to show 0
		scoreInfo.setText("Score " +score);
		timer.start();
	}
	
	/**
	 * scoreStartTiming()
	 * starts the timer
	 */
	public void scoreStartTiming(){
		timer.start();
	}
	
	/**
	 * scoreStopTiming()
	 * stops the timer
	 */
	public void scoreStopTiming(){
		timer.stop();
	}
	
	/**
	 * updateTop10List()
	 * checks if the current score is better than the previous
	 * If it is, the score is updated and the old score is shifted to adjust to the change
	 */
	public void updateTop10List(){
		int temp = 0;
		int temp2 = 0;
		
		//replaces old score with the new one
		for(int i=0; i< top10List.length; i++){
			if(score <= top10List[i]){
				temp = top10List[i];
				top10List[i] = score;
				break;
			}	
		}
		//orders the score
		for(int i=0; i< top10List.length; i++){
			if(temp <= top10List[i]){
				temp2 = top10List[i];
				top10List[i] = temp;
				temp = temp2;
			
			}
				
		}
	}
	
	/**
	 * frameComponents(JFrame mineFrame)
	 * Takes JFrame as a parameter and adds in all the components and 
	 * runs any functions needed for the game to function
	 * @param mineFrame
	 */
	public void frameComponents(JFrame mineFrame){		
		//retrieve the scores
		retrieveScoreData();
		//change text alignment
		displayLives.setHorizontalAlignment(JTextField.CENTER);
		currentItem.setHorizontalAlignment(JTextField.CENTER);
		scoreInfo.setHorizontalAlignment(JTextField.CENTER);
		
		centerMenu.setLayout(new BorderLayout());
		centerMenu.add(displayLives, BorderLayout.NORTH);
		centerMenu.add(currentItem, BorderLayout.CENTER);
		centerMenu.add(scoreInfo, BorderLayout.SOUTH);
		//display top 10 scores
		top10 = new JTextArea("Top 10 Scores \n"
				+ "1. "+ top10List[0] +"	"
				+ "2. "+ top10List[1] +" 	"
				+ "3. "+ top10List[2] +"	"
				+ "4. "+ top10List[3] +" 	"
				+ "5. "+ top10List[4] +" 	"
				+ "6. "+ top10List[5] +" 	"
				+ "7. "+ top10List[6] +" 	"
				+ "8. "+ top10List[7] +" 	"
				+ "9. "+ top10List[8] +" 	"
				+ "10. "+ top10List[9] +" 	"
				);
		
		//make text area uneditable
		top10.setEditable(false);
	    
		//add to menu container
		menu.setLayout(new BorderLayout());
		menu.add(reset, BorderLayout.NORTH);
		menu.add(centerMenu, BorderLayout.CENTER);
		menu.add(load, BorderLayout.EAST);
		menu.add(save, BorderLayout.WEST);
		menu.add(top10, BorderLayout.SOUTH);
		mineFrame.add(menu, BorderLayout.NORTH);
				
		//listener for the button
		reset.addActionListener(this);
		load.addActionListener(this);
		save.addActionListener(this);
				
		//set the layout of the container to be a 10 by 10 grid
		body.setLayout(new GridLayout(GRID_SIZE,GRID_SIZE));
				
		//adding all buttons and action listener
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {
				field[i][j] = new JButton("");
				field[i][j].addActionListener(this);
				field[i][j].addMouseListener(this);
				body.add(field[i][j]);
			} 
		}
		//set the disable button's text color
		UIManager.put("Button.disabledText", Color.WHITE);
		
		//generates the items	
		randomMines();
		randomProbes();
		randomShields();
		randomBonus();
		randomImmortal();
		//start timer
		scoreTiming();
		//checks the surrounding cells for mines and count them.
		//The count is then stored in fieldItems
		checkMines();
		mineFrame.add(body, BorderLayout.CENTER);
	}
	
	/**
	 * randomMines()
	 * creates the mines and randomly place them on the field
	 */
	public void randomMines(){
		//location of mines and can have multiple mines
		int temp= 0;
		while(temp != numOfMines){
			x = (int)(Math.random()* GRID_SIZE);
			y = (int)(Math.random()* GRID_SIZE);
			fieldItems[x][y] +=MINE;
			temp++;
		}
	}
	
	/**
	 * randomProbe()
	 * creates the probes and randomly place them on the field
	 */
	public void randomProbes(){
		//location of each probe
		int temp =0;
		while(temp != numOfProbes) {
			x = (int)(Math.random()* GRID_SIZE);
			y = (int)(Math.random()* GRID_SIZE);
			if(!(fieldItems[x][y] >=MINE) && fieldItems[x][y] !=PROBE 
					&& fieldItems[x][y] !=SHIELD && fieldItems[x][y] !=BONUS
					&& fieldItems[x][y] !=IMMORTAL)
			{
				fieldItems[x][y] = PROBE;
				temp++;
			}
		}	
	}
	
	/**
	 * randomShields()
	 * creates the shields and randomly placed them on the field
	 */
	public void randomShields(){
		//location of each shield
		int temp =0;
		while(temp != numOfShields) {
			x = (int)(Math.random()* GRID_SIZE);
			y = (int)(Math.random()* GRID_SIZE);
			if(!(fieldItems[x][y] >=MINE) && fieldItems[x][y] !=PROBE 
					&& fieldItems[x][y] !=SHIELD && fieldItems[x][y] !=BONUS
					&& fieldItems[x][y] !=IMMORTAL)
			{
				fieldItems[x][y] = SHIELD;
				temp++;
			}
		}	
	}
	
	/**
	 * randomBonus()
	 * creates the bonus and randomly placed them on the field
	 */
	public void randomBonus(){
		//location of each shield
		int temp =0;
		while(temp != numOfBonus) {
			x = (int)(Math.random()* GRID_SIZE);
			y = (int)(Math.random()* GRID_SIZE);
			if(!(fieldItems[x][y] >=MINE) && fieldItems[x][y] !=PROBE 
					&& fieldItems[x][y] !=SHIELD && fieldItems[x][y] !=BONUS
					&& fieldItems[x][y] !=IMMORTAL)
			{
				fieldItems[x][y] = BONUS;
				temp++;
			}
		}	
	}
	
	/**
	 * randomImmortal()
	 * creates the immortal and randomly placed it on the field
	 */
	public void randomImmortal(){
		//location of the immortal
		int temp =0;
		while(temp != numOfImmortal) {
			x = (int)(Math.random()* GRID_SIZE);
			y = (int)(Math.random()* GRID_SIZE);
			if(!(fieldItems[x][y] >=MINE) && fieldItems[x][y] !=PROBE 
					&& fieldItems[x][y] !=SHIELD && fieldItems[x][y] !=BONUS
					&& fieldItems[x][y] !=IMMORTAL)
			{
				fieldItems[x][y] = IMMORTAL;
				temp++;
			}
		}		
	}
	
	/**
	 * checkMines()
	 * Checks the surrounding cells for mines and count them.
	 * The count is then stored in fieldItems and used to set the text for empty cells
	 */
	public void checkMines(){
		
		for (int i = 0; i < fieldItems.length; i++) {
			for (int j = 0; j < fieldItems.length; j++) {
				
				if(fieldItems[i][j]== 0){
					int nearbyMines = 0;
					//top left
					if(i>0 && j> 0 && fieldItems[i-1][j-1] >=MINE
							&& fieldItems[i-1][j-1] <PROBE)
						nearbyMines += fieldItems[i-1][j-1]%numOfMines;
					
					//top center
					if(j>0 && fieldItems[i][j-1] >=MINE
							&& fieldItems[i][j-1] <PROBE)
						nearbyMines+= fieldItems[i][j-1]%numOfMines;
					
					//top right
					if(i<fieldItems.length-1 && j> 0 && fieldItems[i+1][j-1] >=MINE
							&& fieldItems[i+1][j-1] <PROBE)
						nearbyMines+= fieldItems[i+1][j-1]%numOfMines;
					
					//left
					if(i>0 && fieldItems[i-1][j] >=MINE
							&& fieldItems[i-1][j] <PROBE)
						nearbyMines+= fieldItems[i-1][j]%numOfMines;
					
					//right
					if(i<fieldItems.length-1 && fieldItems[i+1][j] >=MINE
							&& fieldItems[i+1][j] <PROBE)
						nearbyMines+=fieldItems[i+1][j]%numOfMines;
					
					//bottom left
					if(i>0 && j<fieldItems.length-1 && fieldItems[i-1][j+1] >=MINE
							&& fieldItems[i-1][j+1] <PROBE)
						nearbyMines+=fieldItems[i-1][j+1]%numOfMines;
					
					//bottom center
					if(j<fieldItems.length-1 && fieldItems[i][j+1] >=MINE
							&& fieldItems[i][j+1] <PROBE)
						nearbyMines+=fieldItems[i][j+1]%numOfMines;
					
					//bottom right
					if(i< fieldItems.length-1 && j<fieldItems.length -1 
							&& fieldItems[i+1][j+1] >=MINE
							&& fieldItems[i+1][j+1] <PROBE)
						nearbyMines+=fieldItems[i+1][j+1]%numOfMines;
					
					fieldItems[i][j] = nearbyMines;
				}
				
			}
		}

	}
	
	/**
	 * clearEmptyCells()
	 * Takes in the coordinates for the first empty cell that has 0 mines around it.
	 * Recursively checks each nearby cell. If cell has mines around it, set the text to the
	 * number of mines around it. If the cell has no mines around it, add it to the ArrayList,
	 * disabled the button and use that point as the next starting point for the recursion.
	 * If there is any flags on the field, the recursion will ignore them.
	 * 
	 * @param x
	 * @param y
	 */
	public void clearEmptyCells(ArrayList<Integer> x, ArrayList<Integer> y){	
		//if size is 0, stop the recursion
		//in order to prevent stack overflow, isEnabled checks the cell has been checked already or not
		if(x.size() == 0 && y.size() == 0)
		{
			return;
		}
		else{
				
				//have the recursion ignore flags by disabling them
				for (int l = 0; l < field.length; l++) {
					for (int m = 0; m < field[0].length; m++){
						if(field[l][m].isEnabled() && field[l][m].getText().equals("F"))
							{
								field[l][m].setBackground(null);
								field[l][m].setEnabled(false);
							}
					}
				}	
				int i = x.get(0);
				int j = y.get(0);
				x.remove(0);
				y.remove(0);

				//top left
				if(i>0 && j> 0 )
				{
					if(fieldItems[i-1][j-1] != 0 && field[i-1][j-1].isEnabled()
							&& fieldItems[i-1][j-1] <PROBE )
					{
						field[i-1][j-1].setText(fieldItems[i-1][j-1]+"");
						field[i-1][j-1].setBackground(Color.DARK_GRAY);
						field[i-1][j-1].setEnabled(false);
					}
					else if(fieldItems[i-1][j-1] == 0 && field[i-1][j-1].isEnabled())
					{
						field[i-1][j-1].setText("");
						field[i-1][j-1].setBackground(Color.DARK_GRAY);
						field[i-1][j-1].setEnabled(false);
						x.add(i-1);
						y.add(j-1);
					}	
					
					
				}
				
				//top center
				if(j>0)
				{
					if(fieldItems[i][j-1] != 0 && field[i][j-1].isEnabled()
							&& fieldItems[i][j-1] <PROBE)
					{
						field[i][j-1].setText(fieldItems[i][j-1]+"");
						field[i][j-1].setBackground(Color.DARK_GRAY);
						field[i][j-1].setEnabled(false);
					}else if(fieldItems[i][j-1] == 0 && field[i][j-1].isEnabled())
					{
						field[i][j-1].setText("");
						field[i][j-1].setBackground(Color.DARK_GRAY);
						field[i][j-1].setEnabled(false);
						x.add(i);
						y.add(j-1);
					}
				}
				
				//top right
				if(i<fieldItems.length-1 && j> 0 )
				{
					if(fieldItems[i+1][j-1] != 0 && field[i+1][j-1].isEnabled()
							&& fieldItems[i+1][j-1] <PROBE)
					{
						field[i+1][j-1].setText(fieldItems[i+1][j-1]+"");
						field[i+1][j-1].setBackground(Color.DARK_GRAY);
						field[i+1][j-1].setEnabled(false);
					}
					else if(fieldItems[i+1][j-1] == 0 && field[i+1][j-1].isEnabled())
					{
						field[i+1][j-1].setText("");
						field[i+1][j-1].setBackground(Color.DARK_GRAY);
						field[i+1][j-1].setEnabled(false);
						x.add(i+1);
						y.add(j-1);
					}
				}
				
				//left
				if(i>0)
				{
					if(fieldItems[i-1][j] != 0 && field[i-1][j].isEnabled()
							&& fieldItems[i-1][j] <PROBE)
					{
						field[i-1][j].setText(fieldItems[i-1][j]+"");
						field[i-1][j].setBackground(Color.DARK_GRAY);
						field[i-1][j].setEnabled(false);
					}
					else if(fieldItems[i-1][j] == 0 && field[i-1][j].isEnabled())
					{
						field[i-1][j].setText("");
						field[i-1][j].setBackground(Color.DARK_GRAY);
						field[i-1][j].setEnabled(false);
						x.add(i-1);
						y.add(j);
						
					}
					
				}
				
				//right
				if(i<fieldItems.length-1)
				{
					if(fieldItems[i+1][j] != 0 && field[i+1][j].isEnabled()
							&& fieldItems[i+1][j] <PROBE)
					{
						field[i+1][j].setText(fieldItems[i+1][j]+"");
						field[i+1][j].setBackground(Color.DARK_GRAY);
						field[i+1][j].setEnabled(false);
					}
					else if(fieldItems[i+1][j] == 0 && field[i+1][j].isEnabled())
					{
						field[i+1][j].setText("");
						field[i+1][j].setBackground(Color.DARK_GRAY);
						field[i+1][j].setEnabled(false);
						x.add(i+1);
						y.add(j);
					
					}
					
				}
				
				//bottom left
				if(i>0 && j<fieldItems.length-1)
				{
					if(fieldItems[i-1][j+1] != 0 && field[i-1][j+1].isEnabled()
							&& fieldItems[i-1][j+1] <PROBE)
					{
						field[i-1][j+1].setText(fieldItems[i-1][j+1]+"");
						field[i-1][j+1].setBackground(Color.DARK_GRAY);
						field[i-1][j+1].setEnabled(false);
					}
					else if(fieldItems[i-1][j+1] == 0 && field[i-1][j+1].isEnabled())
					{
						field[i-1][j+1].setText("");
						field[i-1][j+1].setBackground(Color.DARK_GRAY);
						field[i-1][j+1].setEnabled(false);
						x.add(i-1);
						y.add(j+1);
					
					}
					
				}
				
				//bottom center
				if(j<fieldItems.length-1)
				{
					if(fieldItems[i][j+1] != 0 && field[i][j+1].isEnabled()
							&& fieldItems[i][j+1] <PROBE)
					{
						field[i][j+1].setText(fieldItems[i][j+1]+"");
						field[i][j+1].setBackground(Color.DARK_GRAY);
						field[i][j+1].setEnabled(false);
					}
					else if(fieldItems[i][j+1] == 0 && field[i][j+1].isEnabled())
					{
						field[i][j+1].setText("");
						field[i][j+1].setBackground(Color.DARK_GRAY);
						field[i][j+1].setEnabled(false);
						x.add(i);
						y.add(j+1);
					}
					
				}
				
				//bottom right
				if(i< fieldItems.length-1 && j<fieldItems.length -1)
				{
					if(fieldItems[i+1][j+1] != 0 && field[i+1][j+1].isEnabled()
							&& fieldItems[i+1][j+1] <PROBE)
					{
						field[i+1][j+1].setText(fieldItems[i+1][j+1]+"");
						field[i+1][j+1].setBackground(Color.DARK_GRAY);
						field[i+1][j+1].setEnabled(false);	
					}
					else if(fieldItems[i+1][j+1] == 0 && field[i+1][j+1].isEnabled())
					{
						field[i+1][j+1].setText("");
						field[i+1][j+1].setBackground(Color.DARK_GRAY);
						field[i+1][j+1].setEnabled(false);	
						x.add(i+1);
						y.add(j+1);
						
					}
					
				}
				
			//have the recursion restore the disabled flags
			for (int l = 0; l < field.length; l++) {
				for (int m = 0; m < field[0].length; m++){
					if(!field[l][m].isEnabled() && field[l][m].getText().equals("F"))
						{
							field[l][m].setBackground(null);
							field[l][m].setEnabled(true);
						}
				}
			}	
			//ensure that all the required cells have been disabled
			//Example, a cell may not be disabled but will have a text set after the recursion
			//It will also not clear a cell that has a probe on it
			for (int l = 0; l < field.length; l++) {
				for (int m = 0; m < field[0].length; m++){
					if(field[l][m].isEnabled() && !field[l][m].getText().equals("") 
							&& !field[l][m].getText().equals("M")&& !field[l][m].getText().equals("F"))
						{
							field[l][m].setBackground(Color.DARK_GRAY);
							field[l][m].setEnabled(false);
						}
				}
			}
			//recursively call clearEmptyCells
			clearEmptyCells(x,y);
		}
		
	}
	
	/**
	 * showAll()
	 * Display all the cells in the game
	 */
	public void showAll(){
		//clear all the flags off the buttons
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {	
				if(field[i][j].getText().equals("F"))
					field[i][j].setText("");
			}
		}
		//show the field
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {	
				//for mines
				//checks enable button and button with probe on it
				if((field[i][j].getText().equals("")||field[i][j].getText().equals("M"))
						&& fieldItems[i][j]>=MINE && fieldItems[i][j]<PROBE)
				{
					//set the text
					field[i][j].setText((fieldItems[i][j]%numOfMines)+"M");
					field[i][j].setBackground(Color.RED);
					//removes the button
					field[i][j].setEnabled(false);		
				}
				//for probes
				else if(field[i][j].getText().equals("") && fieldItems[i][j]==PROBE)
				{
					//set the text
					field[i][j].setText("P");
					field[i][j].setBackground(Color.MAGENTA);
					//removes the button
					field[i][j].setEnabled(false);
				}
				//for shields
				else if(field[i][j].getText().equals("") && fieldItems[i][j]==SHIELD)
				{
					//set the text
					field[i][j].setText("S");
					field[i][j].setBackground(Color.GREEN);
					//removes the button
					field[i][j].setEnabled(false);
				}
				//for bonus
				else if(field[i][j].getText().equals("") && fieldItems[i][j]==BONUS)
				{
					//set the text
					field[i][j].setText("B");
					field[i][j].setBackground(Color.BLUE);
					//removes the button
					field[i][j].setEnabled(false);
				}
				//for immortality
				else if(field[i][j].getText().equals("") && fieldItems[i][j]==IMMORTAL)
				{
					//set the text
					field[i][j].setText("IMM");
					field[i][j].setBackground(Color.ORANGE);
					//removes the button
					field[i][j].setEnabled(false);
				}
				//for the rest of the cells
				else if(field[i][j].getText().equals("") && fieldItems[i][j]<MINE)
				{
					//set the text
					if(fieldItems[i][j] != 0)
					{	//for mines nearby
						field[i][j].setText(fieldItems[i][j]+"");
						field[i][j].setBackground(Color.DARK_GRAY);
						field[i][j].setEnabled(false);
					}
					else if(fieldItems[i][j] == 0)
					{
						//set the text
						field[i][j].setText("");
						field[i][j].setBackground(Color.DARK_GRAY);
						
						//removes the button
						field[i][j].setEnabled(false);
					}
					else 
					{	
						//set the text
						field[i][j].setText("");
						//removes the button
						field[i][j].setEnabled(false);
					}
				}	
			}
		}
	}
	
	/**
	 * clearBoard()
	 * sets all cells to its default text, background color, set all values to 0 and enable all buttons
	 */
	public void clearBoard(){
		//reset all disabled buttons
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {	
				if(!field[i][j].isEnabled())
				{
					field[i][j].setEnabled(true);
					field[i][j].setText("");
				}	
			}
		}
		//if they reset after probe was used
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {	
				if(field[i][j].isEnabled() && !field[i][j].getText().equals(""))
				{
					field[i][j].setText("");
				}	
			}
		}
		//clear out all field items
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {	
					fieldItems[i][j] = 0;
					field[i][j].setBackground(null);
			}
		}
	}
	
	/**
	 * winner()
	 * checks if the player has completed the game by clearing all the cells that aren't mines
	 * Does not include items in the criteria for winning
	 */
	public void winner(){
		boolean winner = true;
	
		//if all the non mines button are disabled (not including items), you win
		//you can have mines button disabled and still win as long as your lives is above 0
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++){
				if((fieldItems[i][j] < MINE) &&
						field[i][j].isEnabled())
					winner = false;
			}
		}
		
		if(winner == true)
		{
			//stop the timer once you have completed the game
			scoreStopTiming();
			//update and save the score only if you have completed the game
			updateTop10List();
			saveScoreData();
			//displays the new score
			top10.setText("Top 10 Scores \n"
					+ "1. "+ top10List[0] +"	"
					+ "2. "+ top10List[1] +" 	"
					+ "3. "+ top10List[2] +"	"
					+ "4. "+ top10List[3] +" 	"
					+ "5. "+ top10List[4] +" 	"
					+ "6. "+ top10List[5] +" 	"
					+ "7. "+ top10List[6] +" 	"
					+ "8. "+ top10List[7] +" 	"
					+ "9. "+ top10List[8] +" 	"
					+ "10. "+ top10List[9] +" 	"
					);
			
			JOptionPane.showMessageDialog(mineFrame,"You are a Winner");
			//display all the cells after you have completed the game
			showAll();
		}					
}	
	/**
	 * saveScoreData()
	 * saves the top 10 scores
	 */
	public void saveScoreData(){
		try{
			
			 FileOutputStream fos= new FileOutputStream("top10List");
	         ObjectOutputStream oos= new ObjectOutputStream(fos);
	         try {
	        	 oos.writeObject(top10List);
	         }
	         finally{
		         oos.close();
		         fos.close();
	         }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException ioe){
			ioe.printStackTrace();
	        }
	}
	
	/**
	 * retrievecoreData()
	 * loads the top 10 scores
	 */
	public void retrieveScoreData(){ 
		 try{
			 FileInputStream fis = new FileInputStream("top10List");
			 ObjectInputStream ois = new ObjectInputStream(fis);
			 try{
				 int[] temp = (int[]) ois.readObject();
				 for(int i = 0; i< temp.length; i++){
					 top10List[i] = temp[i];
				 }   
			 }
			 finally{
				 ois.close();
				 fis.close(); 
		       	}
		 }catch (FileNotFoundException e) {
			 e.printStackTrace();
		 }catch(IOException e){
			 e.printStackTrace();
		 }catch(ClassNotFoundException e){
			 e.printStackTrace();
		 }
	}
	
	/**
	 * saveFieldData()
	 * save the array of JButtons field
	 */
	public void saveFieldData(){
		try{
			
			 FileOutputStream fos= new FileOutputStream("field");
	         ObjectOutputStream oos= new ObjectOutputStream(fos);
	         try {
	        	 oos.writeObject(field);
	         }
	         finally{
		         oos.close();
		         fos.close();
	         }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException ioe){
			ioe.printStackTrace();
	        }
	}
	
	/**
	 * retrieveFieldData()
	 * loads the array of JButtons field and copy it to the array of JButtons "saved" 
	 */
	public void retrieveFieldData(){ 
		 try{
			 FileInputStream fis = new FileInputStream("field");
			 ObjectInputStream ois = new ObjectInputStream(fis);
			 try{
				 JButton[][] temp = (JButton[][]) ois.readObject();
				 for(int i = 0; i< temp.length; i++){
					 for (int j = 0; j < temp[0].length; j++){
						 saved[i][j] = temp[i][j];
					 }
				 }   
			 }
			 finally{
				 ois.close();
				 fis.close(); 
		       	}
		 }catch (FileNotFoundException e) {
			 e.printStackTrace();
		 }catch(IOException e){
			 e.printStackTrace();
		 }catch(ClassNotFoundException e){
			 e.printStackTrace();
		 }
	}
	
	/**
	 * saveItemsLocationData()
	 * saves all the item locations found in fieldItems array
	 */
	public void saveItemsLocationData(){
		try{
			
			 FileOutputStream fos= new FileOutputStream("itemsLocation");
	         ObjectOutputStream oos= new ObjectOutputStream(fos);
	         try {
		        	 oos.writeObject(fieldItems);
	         }
	         finally{
		         oos.close();
		         fos.close();
	         }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * retrieveItemsLocationData()
	 * loads all the item locations and copies them to the fieldItems array
	 */
	public void retrieveItemsLocationData(){ 
		try{
			FileInputStream fis = new FileInputStream("itemsLocation");
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        try{
	        	int[][] temp = (int[][]) ois.readObject();
	        	for(int i = 0; i< temp.length; i++){
	        		for (int j = 0; j < temp[0].length; j++){
	        			fieldItems[i][j] = temp[i][j];
	        		}
	        	}
	        }
	        finally{
	        	ois.close();
	        	fis.close(); 
	        }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * saveItemsData()
	 * saves the information for lives and items
	 */
	public void saveItemsData(){
		int[] temp = new int[4];
		temp[0] = lives;
		temp[1] = probeUse;
		temp[2] = shieldStrength;
		temp[3] = score;
		try{
			
			 FileOutputStream fos= new FileOutputStream("items");
	         ObjectOutputStream oos= new ObjectOutputStream(fos);
	         try {
		        	 oos.writeObject(temp);
	         }
	         finally{
		         oos.close();
		         fos.close();
	         }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * retrueveItemsData()
	 * loads the information for lives and items and places them into their respective variables
	 */
	public void retrieveItemsData(){ 
		try{
			FileInputStream fis = new FileInputStream("items");
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        try{
	        	int[] temp = (int[]) ois.readObject();
	        	lives = temp[0];
	        	probeUse = temp[1];
	        	shieldStrength = temp[2];
	        	//minus one to adjust for a slight delay
	        	score = temp[3]-1;
	        	
	        }
	        finally{
	        	ois.close();
	        	fis.close(); 
	        }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * actionPerformed(ActionEvent event)
	 * The action listener that performs specific action based on the
	 * the source of the event.
	 * @param event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {	
		//reset the game
		if(event.getSource().equals(reset))
		{	
			//stops the timer
			scoreStopTiming();
			//clears everything
			clearBoard();
			
			//reset all values
			lives = 3;
			probeUse = 0;
			shieldStrength = 0;
			
			//reset the time and starts a fresh timer
			score = 0;
			scoreTiming();
			
			displayLives.setText("Number of lives remaining: " + Integer.toString(lives));
			currentItem.setText("Current Item in Possession:");
			
			randomMines();
			randomProbes();
			randomShields();
			randomBonus();
			randomImmortal();
			
			checkMines();
		}
		//save the state of the game
		else if(event.getSource().equals(save))
		{
			saveItemsLocationData(); 
			saveItemsData();
			saveFieldData();
			 
		}
		//loading previously saved state
		else if(event.getSource().equals(load))
		{
			//if you the lost the game and reload, timer will start from save point
			if(!timer.isRunning())
				scoreStartTiming();
			
			retrieveItemsLocationData();
			retrieveItemsData();
			retrieveFieldData();
			
			//if you have a shield on when you saved the game
			if(shieldStrength !=0 ){
				displayLives.setText("Number of lives remaining with Shield on: " + Integer.toString(lives));
				currentItem.setText("Current Item in Possession: Shield Strength at " 
						+ Integer.toString(shieldStrength));
			}
			else{
				displayLives.setText("Number of lives remaining: " + Integer.toString(lives));
			}
			//if you have a probe available when you saved the game
			if(probeUse != 0){
				
				shieldStrength = 0;
				currentItem.setText("Current Item in Possession: Probe Available ");
			}
			//if you have immortality when you saved the game
			if((lives == 100002 || lives == 100001 || lives == 100000)
					&& probeUse == 0){
				currentItem.setText("Current Item in Possession: Immortality was previously activated (permanent)");
	
			}
			//if you saved the game while having no items
			if(shieldStrength == 0 && probeUse == 0 && lives <= 3){		
				currentItem.setText("Current Item in Possession: ");
			}
			//if any of the current button are disabled, enabled them
			//before reverting back to the previous state
			//in order to avoid overwriting previous saved cell information
			for (int i = 0; i < field.length; i++) {
				for (int j = 0; j < field[0].length; j++) {
					if(!field[i][j].isEnabled()){
						field[i][j].setEnabled(true);
						field[i][j].setBackground(null);
						field[i][j].setText("");	
					}
					
				}
			}
			
			//recreate the saved status on the field
			for (int i = 0; i < field.length; i++) {
				for (int j = 0; j < field[0].length; j++) {
					//checks if the current field buttons and saved field button are different in terms of button enabled
					if(field[i][j].isEnabled() != saved[i][j].isEnabled()){
						//if the saved field button is disabled
						if(!saved[i][j].isEnabled())
						{	//for mines
							if(fieldItems[i][j]>=MINE && fieldItems[i][j]<PROBE)
							{
						
								//set the text and disable button
								field[i][j].setText((fieldItems[i][j]%numOfMines)+"M");
								field[i][j].setBackground(Color.RED);
								field[i][j].setEnabled(false);	
							}
							//for probes
							if(fieldItems[i][j]==PROBE)
							{
								//set the text and disable button
								field[i][j].setText("P");
								field[i][j].setBackground(Color.MAGENTA);
								field[i][j].setEnabled(false);
			
							}
							//for shields
							if(fieldItems[i][j]==SHIELD)
							{
								//set the text and disable button
								field[i][j].setText("S");
								field[i][j].setBackground(Color.GREEN);
								field[i][j].setEnabled(false);
							}
							//for bonus
							if(fieldItems[i][j]==BONUS)
							{
								//set the text and disable button
								field[i][j].setText("B");
								field[i][j].setBackground(Color.BLUE);
								field[i][j].setEnabled(false);
							}
							//for immortality
							if(fieldItems[i][j]==IMMORTAL)
							{
								//set the text and disable button
								field[i][j].setText("IMM");
								field[i][j].setBackground(Color.ORANGE);
								field[i][j].setEnabled(false);
								
							}
							//for empty cells
							if(fieldItems[i][j]<MINE)
							{
								//set the text and disable button
								if(fieldItems[i][j] != 0)
								{	//for mines nearby
									field[i][j].setText(fieldItems[i][j]+"");
									field[i][j].setBackground(Color.DARK_GRAY);
									field[i][j].setEnabled(false);
									
								
								}
								else if(fieldItems[i][j] == 0)
								{
									//set the text and disable button
									field[i][j].setText("");
									field[i][j].setBackground(Color.DARK_GRAY);
									field[i][j].setEnabled(false);
									
								}
								else 
								{	
									//set the text and disable button
									field[i][j].setText("");
									field[i][j].setBackground(Color.DARK_GRAY);
									field[i][j].setEnabled(false);
								
								}
							}
						}
						else
						{
							field[i][j].setEnabled(true);
							field[i][j].setBackground(null);
							field[i][j].setText("");
							}
						}
					//if the current button and saved button are both enabled but have different
					//text, then change the text
					if(field[i][j].isEnabled() == saved[i][j].isEnabled())
						{
							//if saved button text is M (a probe was used on this cell), set current button text to M
							if(saved[i][j].getText().equals("M")){
								
								field[i][j].setEnabled(true);
								field[i][j].setBackground(null);
								field[i][j].setForeground(Color.red);
								field[i][j].setText("M");
								
							}
							//if saved button text is F (the cell has a flag), set current button text to F
							if(saved[i][j].getText().equals("F")){
								
								field[i][j].setEnabled(true);
								field[i][j].setBackground(null);
								field[i][j].setForeground(Color.red);
								field[i][j].setText("F");
							}
							//if saved button text is "" and current button has text, set current button text to ""
							if(saved[i][j].getText().equals("") && !field[i][j].getText().equals("")){
			
								field[i][j].setEnabled(true);
								field[i][j].setBackground(null);
								field[i][j].setText("");
							}
							
						}
				}
			}
		}
		//perform a action based on the event source
		else{
			for (int i = 0; i < field.length; i++) {
				for (int j = 0; j < field[0].length; j++) {	
					//for mines
					//checks for an enable button or a button with probe on it
					if(event.getSource().equals(field[i][j]) 
							&& (field[i][j].getText().equals("")||field[i][j].getText().equals("M"))
							&& fieldItems[i][j]>=MINE && fieldItems[i][j]<PROBE)
					{
						//if you have a probe available for use
						if(probeUse == 1)
						{
							//place a M on top of the mine
							field[i][j].setText("M");
							field[i][j].setForeground(Color.red);
							probeUse = 0;
							currentItem.setText("Current Item in Possession: ");
							break;
						}
						//set the text
						field[i][j].setText((fieldItems[i][j]%numOfMines)+"M");
						field[i][j].setBackground(Color.RED);
						
						//adjust the number of lives when you hit a mine and have a shield on
						if(shieldStrength !=0)
						{
							shieldStrength -= fieldItems[i][j]%numOfMines;
							lives -= fieldItems[i][j]%numOfMines;
							displayLives.setText("Number of lives remaining with Shield on: " + Integer.toString(lives));
							if(shieldStrength <= 0)
								{
								currentItem.setText("Current Item in Possession:");
								displayLives.setText("Number of lives remaining: " + Integer.toString(lives));
								}
							else
							currentItem.setText("Current Item in Possession: Shield Strength at " 
									+ Integer.toString(shieldStrength));
						}
						else
						{	//adjust the number of lives when no shield is on
							lives -= fieldItems[i][j]%numOfMines;
							displayLives.setText("Number of lives remaining: " + Integer.toString(lives));
						}
						//removes the button
						field[i][j].setEnabled(false);
						
						//see if you have completed the game
						winner();
						
						//when the number of lives reaches 0, game over
						if(lives  <= 0)
						{	
							//stop timing when you lost
							scoreStopTiming();
							//displays game over message
							JOptionPane.showMessageDialog(mineFrame,"Game Over");
							//shows everything after you lost
							showAll();
							
						}
						
							
					}
					//for probes
					if(event.getSource().equals(field[i][j]) && field[i][j].getText().equals("")
							&& fieldItems[i][j]==PROBE)
					{
						//set the text
						field[i][j].setText("P");
						field[i][j].setBackground(Color.MAGENTA);
						probeUse = 1;
						//adjust the number of lives base on remaining shield
						lives -= shieldStrength;
						displayLives.setText("Number of lives remaining: " + Integer.toString(lives));
						//replace your shield with a probe
						shieldStrength = 0;
						currentItem.setText("Current Item in Possession: Probe Available ");
						//removes the button
						field[i][j].setEnabled(false);
						
						//see if you have completed the game
						winner();
					}
					//for shields
					if(event.getSource().equals(field[i][j]) && field[i][j].getText().equals("")
							&& fieldItems[i][j]==SHIELD)
					{
						//set the text
						field[i][j].setText("S");
						field[i][j].setBackground(Color.GREEN);
						//replace the probe with a shield
						probeUse = 0;
						//remove any old shield if any
						lives -= shieldStrength;
						
						shieldStrength = 3;
						//add the new shield
						lives += shieldStrength;
						currentItem.setText("Current Item in Possession: Shield Strength at " 
								+ Integer.toString(shieldStrength));
						displayLives.setText("Number of lives remaining with Shield on: " + Integer.toString(lives));
						//removes the button
						field[i][j].setEnabled(false);
						
						//see if you have completed the game
						winner();
					}
					//for Bonus, getting a bonus will not remove any other items
					if(event.getSource().equals(field[i][j]) && field[i][j].getText().equals("")
							&& fieldItems[i][j]==BONUS)
					{
						//set the text
						field[i][j].setText("B");
						field[i][j].setBackground(Color.BLUE);
						field[i][j].setEnabled(false);
						score -= bonusTime;
						currentItem.setText("Current Item in Possession: Bonus has been activated (permanent, will not replace previous item)");
						//see if you have completed the game
						winner();
					}
					//for immortality
					if(event.getSource().equals(field[i][j]) && field[i][j].getText().equals("")
							&& fieldItems[i][j]==IMMORTAL)
					{
						//set the text
						field[i][j].setText("IMM");
						field[i][j].setBackground(Color.ORANGE);
						//replaces any shield or probe with immortality
						lives -= shieldStrength;
						shieldStrength = 0;
						probeUse = 0;
						//adjust the number of lives for immortality
						lives += 99999;
						//displays the change
						displayLives.setText("Number of lives remaining: " + Integer.toString(lives));
						currentItem.setText("Current Item in Possession: Immortality has been activated (permanent, replaces previous item)");
						//removes the button
						field[i][j].setEnabled(false);
						
						//see if you are won the game
						winner();
					}
					//for empty cells
					if(event.getSource().equals(field[i][j]) && field[i][j].getText().equals("")
							&& fieldItems[i][j]<MINE)
					{
						//set the text
						if(fieldItems[i][j] != 0)
						{	//for mines nearby
							field[i][j].setText(fieldItems[i][j]+"");
							field[i][j].setBackground(Color.DARK_GRAY);
							field[i][j].setEnabled(false);
							
							//see if you are won the game
							winner();
						}
						else if(fieldItems[i][j] == 0)
						{
							//set the text
							field[i][j].setText("");
							field[i][j].setBackground(Color.DARK_GRAY);
							//removes the button
							field[i][j].setEnabled(false);
							ArrayList<Integer> listOfX = new ArrayList<Integer>();
							ArrayList<Integer> listOfY = new ArrayList<Integer>();
							listOfX.add(i);
							listOfY.add(j);
							clearEmptyCells(listOfX,listOfY);
							
							//see if you are won the game
							winner();
						}
						else 
						{	
							//set the text
							field[i][j].setText("");
							field[i][j].setBackground(Color.DARK_GRAY);
							//removes the button
							field[i][j].setEnabled(false);
							
							//see if you are won the game
							winner();
						}
						
					}	
				}
			}
		}
	}
	
	/**
	 * mouseClicked(MouseEvent event)
	 * The mouse listener used to add/remove any flags
	 * @param event
	 */
	@Override
	public void mouseClicked(MouseEvent event) {		
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[0].length; j++) {
				if(SwingUtilities.isRightMouseButton(event))
				{
					if(event.getSource().equals(field[i][j]))
					{
						if(field[i][j].isEnabled() && field[i][j].getText().equals(""))//set the text
						{
							field[i][j].setText("F");
							field[i][j].setForeground(Color.red);
						}
						else if(field[i][j].isEnabled() && field[i][j].getText().equals("F"))
						{
							field[i][j].setText("");
						}
						
					}
				}
			}
		}
	}


	//abstract methods that is required to used the Mouse listeners
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
