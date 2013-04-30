package ps.system.frames;

import java.awt.BorderLayout;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import ps.logic.beans.SimulationIDBean;
import ps.system.api.ChartMaker;
import ps.system.api.InfoPane;
import ps.system.api.SimulatorInstanceJFX;
import ps.system.api.SimulatorInstanceSwing;
import ps.system.main.PhysicsWindow;
import ps.system.main.SimulationList;
import ps.system.main.SystemConstants;
import ps.system.menu.Menu;

public class JFXPanes extends JPanel implements SystemConstants {

	// Pane separators
	private static JSplitPane seperatedPanes;
	private static JPanel bottomPane;
	private static JPanel menuPane;

	//Generic components
	public static Object genericSimulation = null;
	
	// Swing simulation object components
	public static SimulatorInstanceSwing SwingSimulation;
	
	// JavaFX simulation object components
	public static SimulatorInstanceJFX JFXSimulation;
	private Menu JFXMenu;
	
	//Simulation analysis components
	private static ChartMaker Graph;
	public static InfoPane Input;

	/*
	 * Containers for Swing and JavaFX integration
	 */
	
	//Swing
	private JPanel window_Menu;
	private JPanel window_Simulation;
	private JPanel window_Graph;
	
	//JavaFX
	private static JFXPanel JFXPanel_Simulation = new JFXPanel();
	private static JFXPanel JFXPanel_Graph = new JFXPanel();
	private static JFXPanel JFXPanel_Input = new JFXPanel();
	private static JFXPanel JFXPanel_Menu = new JFXPanel();

	// Which simulation to display
	public SimulationIDBean simulationID = new SimulationIDBean();

	public JFXPanes() {

		// Create menu panel
		window_Menu = new JPanel(new BorderLayout());
		window_Menu.add(JFXPanel_Menu, BorderLayout.CENTER);

		// Create panel dedicated to showing the simulation
		window_Simulation = new JPanel(new BorderLayout());

		// Create panel dedicated to showing graphs
		window_Graph = new JPanel(new BorderLayout());
		window_Graph.add(JFXPanel_Graph, BorderLayout.CENTER);

		// Create panel dedicated to showing input and buttons
		bottomPane = new JPanel(new BorderLayout());
		bottomPane.add(JFXPanel_Input, BorderLayout.CENTER);

		// Add panels to horizontally separated panes
		seperatedPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, window_Simulation, window_Graph);
		seperatedPanes.setResizeWeight(JFXPanes_RESIZEWEIGHT);
		seperatedPanes.setContinuousLayout(JFXPanes_CONTINUOUSLAYOUT);

		// Menu
		menuPane = new JPanel(new BorderLayout());
		menuPane.add(JFXPanel_Menu, BorderLayout.CENTER);

		// Initialize JavaFX Event Thread
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				InitializeSimulationWindows();
			}
		});

	}

	// Initialize JavaFX Objects
	private void InitializeSimulationWindows() {
		SimulationList simList = new SimulationList();

		if (simulationID.getSimulationID() == null) {
			JFXMenu = new Menu();
			initJFX_Module(JFXPanel_Menu, JFXMenu.getScene());
		}

		simulationID.SimulationIDProperty().addListener(new InvalidationListener() {

					@Override
					public void invalidated(Observable arg0) {
						System.out.println(simulationID.getSimulationID());

						if (!(simulationID.getSimulationID().equals(" "))) {
							
							// Initialize Simulation Content
							genericSimulation = SimulationList.simulationList.get(simulationID.getSimulationID());
							String instanceName = null;

							if (!((simulationID.getSimulationID()).split("_")[2].equals(null))) {
								instanceName = (simulationID.getSimulationID()).split("_")[2];

								try {
									genericSimulation = Class.forName( SIMFRAMEPATH + instanceName).newInstance();
								} catch (InstantiationException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
							if (genericSimulation instanceof SimulatorInstanceJFX) {
								
								JFXSimulation = (SimulatorInstanceJFX) genericSimulation;
								window_Simulation.add(JFXPanel_Simulation, BorderLayout.CENTER);
								
								JFXSimulation.LoadData();
								
								initJFX_Module(JFXPanel_Simulation, ((SimulatorInstanceJFX) JFXSimulation).getScene());
								
								// Initialize Chart for content
								JFXPanes.Graph = new ChartMaker(ChartMaker.JFXDATASET, simulationID.getSimulationID().split("_")[1]);

								// Initialize InfoPane for content
								JFXPanes.Input = new InfoPane();
								
							} else if (genericSimulation instanceof SimulatorInstanceSwing){
								SwingSimulation = (SimulatorInstanceSwing) genericSimulation;
								window_Simulation.add(SwingSimulation, BorderLayout.CENTER);

								SwingSimulation.LoadData();
								
								// Initialize Chart for content
								JFXPanes.Graph = new ChartMaker(ChartMaker.SWINGDATASET, simulationID.getSimulationID().split("_")[1]);

								// Initialize InfoPane for content
								JFXPanes.Input = new InfoPane(SwingSimulation);
							}


							initJFX_Module(JFXPanel_Graph, Graph.getScene());
							initJFX_Module(JFXPanel_Input, Input.getScene());
						} else {
							/*
							 * TO DO:
							 * 	-> Fix data unloading to properly load according chart 
							 */
							if (genericSimulation instanceof SimulatorInstanceJFX) {
								
								JFXPanes.Graph = null;
								JFXPanes.Input =  null;
								
								JFXSimulation.UnLoadData();
								JFXSimulation = null;
								
								window_Simulation.removeAll();
								
							} else if (genericSimulation instanceof SimulatorInstanceSwing) {
								
								JFXPanes.Graph = null;
								JFXPanes.Input = null;
								
								SwingSimulation.UnLoadData();
								SwingSimulation = null;
								
								window_Simulation.removeAll();
							}
							
							PhysicsWindow.changeWindow("Menu");
						}
					}

				});

	}

	public static ChartMaker getGraphComponent() {
		return Graph;
	}

	private static void initJFX_Module(JFXPanel panel, Scene scene) {
		panel.setScene(scene);
	}

	public JSplitPane getSeperatedPanes() {
		return seperatedPanes;
	}

	public JPanel getBottomPane() {
		return bottomPane;
	}

	public JPanel getMenuPane() {
		return menuPane;
	}

}
