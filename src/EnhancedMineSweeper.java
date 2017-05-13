import java.awt.EventQueue;

import javax.swing.UIManager;

/**
 * Project: Enhanced MineSweeper
 * @author Channy Uon 6189318, Cong Lu 3080021, Cedric Abou Farhat 7032633
 *
 * Program Description:
 * An enhance Mine Sweeper game as close a replication in the functionality
 * of the original Mine Sweeper game (mines, flags, reset and score). However,
 * it has the added features that allows a player to save/load the current
 * game state, the concept of multiples lives and the introduction of additional 
 * items like a probe, shield, bonus and immortality.
 */

public class EnhancedMineSweeper {

	public static void main(String[] args) {
		
		//create the GUI and run the game in the event dispatch thread
		EventQueue.invokeLater(new Runnable() {
			public void run(){	
				//for cross platform look and feel
				//so that Mac os can see the background colors
				try {
				    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
				 } catch (Exception e) {
				            e.printStackTrace();
				 }
				new MineSweeper();
			}
		});
	}
}
