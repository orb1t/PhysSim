package ps.simulation.frames.jfx;

import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ps.logic.beans.SimVariableBean;
import ps.logic.beans.TimeBean;
import ps.system.core.SimulatorInstanceJFX;
import ps.system.frames.JFXPanes;
import ps.system.main.PhysicsWindow;

public class TrackWindow extends SimulatorInstanceJFX implements TrackSimConstants {

	//Global arrays
	private static Object[][] marathonerAttributes = new Object[NUMBER_OF_MARATHONERS][NUMBER_OF_ATTRIBUTES];
	private static TrackPerson[] Marathoners = new TrackPerson[NUMBER_OF_MARATHONERS];
	private static Timeline runners = new Timeline();
	private static Text[] trackText = new Text[Marathoners.length];
	private static boolean[] marathonFinished = new boolean[Marathoners.length];
	private static KeyFrame[] keyFrames;
	private static KeyValue[] keyValues;
	
	//Global panes
	private static Pane marathonersPane;
	
	
	private static SimVariableBean runDistanceBean = new SimVariableBean();
	private static SimVariableBean speedModBean = new SimVariableBean();
	private static SimVariableBean baseTimeBean = new SimVariableBean();
	
	public TrackWindow() {
		//BEGIN JAVAFX
		GenerateMarathonerProperties();
		Handlers();
		
		runDistanceBean.setValue(500);
		baseTimeBean.setValue(5);
		speedModBean.setValue(1);

		for (int i = 0; i < Marathoners.length; i++) {
			System.out.println("TEST: " + Marathoners[i].toString());
		}
		
		BorderPane root = new BorderPane();
		scene = new Scene(root);
		
		root.setCenter(TrackPane());
	}
	
	private static BorderPane TrackPane() {
		BorderPane trackPane = new BorderPane();
		marathonersPane = new Pane();

		trackPane.setStyle(BGCOLOR);
		Track track = new Track();
		track.buildTrack(trackPane, TRACK_WIDTH, TRACK_HEIGHT, TRACK_COLOR, TRACK_COLOR_START, TRACK_COLOR_FINISH);

		for (int i = 0; i < Marathoners.length; i++) {
			Marathoners[i].runner(marathonersPane, 50, 20 + TRACK_LANE_SPACING * i);
		}

		trackPane.setBottom(StatusMenu());
		trackPane.getChildren().add(marathonersPane);

		return trackPane;
	}
	
	private static VBox StatusMenu() {
		final TimeBean timer = new TimeBean();
		timer.setTime(0);
		
		VBox statusPane = new VBox();
		statusPane.setStyle(BGCOLOR);
		statusPane.setPadding(new Insets(PADDING,PADDING,PADDING,PADDING));
		statusPane.setSpacing(20);
		statusPane.setAlignment(Pos.BASELINE_CENTER);
		
		for (int i = 0; i < trackText.length; i++) {
			String text = "Track " + (i + 1) + ": 0";
			trackText[i] = new Text(text);
			trackText[i].setScaleX(2);
			trackText[i].setScaleY(2);
			statusPane.getChildren().addAll(trackText[i]);
		}
		
		runners.currentTimeProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable arg0) {
				
				for (int i = 0; i < trackText.length; i++) {
					String text = "Track " + (i + 1) + ": ";

					if ((Marathoners[i].runnerNode().getTranslateX() == runDistanceBean.getValue()) && !marathonFinished[i]) {
						int places = 0;
						int currentTime = (int) runners.getCurrentTime().toMillis();

						Marathoners[i].StopRunning();
						marathonFinished[i] = true;

						for (int j = 0; j < marathonFinished.length; j++) {
							if (marathonFinished[j] == true) {
								places++;
							} 
						}

						switch (places) {
						case 1:
							trackText[i].setText(text + currentTime + "ms " +" - 1st Place!");
							break;
						case 2:
							trackText[i].setText(text + currentTime + "ms " +" - 2nd Place");
							break;
						case 3:
							trackText[i].setText(text + currentTime + "ms " + " - 3rd Place");
							break;
						}

					} else if (!marathonFinished[i]) {
						timer.setTime((int) runners.getCurrentTime().toMillis());
						trackText[i].setText(text + timer.getTime() + "ms");
					}
				}
			}

		});
			
		return statusPane;
	}
	
	private static void Handlers() {
		
		startButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0) {		
				Random randomNumber = new Random();
				keyValues = new KeyValue[Marathoners.length];
				keyFrames = new KeyFrame[Marathoners.length];
				
				runners.setCycleCount(1);
				
				for (int i = 0; i < Marathoners.length; i++) {
					double lapTime = (baseTimeBean.getValue() + (double)((randomNumber.nextInt(10)/2) + randomNumber.nextInt(20)/3)/1.5) * speedModBean.getValue();
					keyValues[i]= new KeyValue(Marathoners[i].runnerNode().translateXProperty(), runDistanceBean.getValue(), interpolators[randomNumber.nextInt(interpolators.length - 1)]);
					keyFrames[i] = new KeyFrame(Duration.seconds(lapTime), keyValues[i]);
					runners.getKeyFrames().addAll(keyFrames[i]);
				}
				
				
				for (int i = 0; i < Marathoners.length; i++) {
					Marathoners[i].StartRunning();
				}
				
				runners.play();
				
			}
			
		});
		
		resetButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				ResetTrack();
				JFXPanes.getGraphComponent().clearData();
			}
			
		});
		
		backButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				ResetTrack();
				PhysicsWindow.JFXPanes.simulationID.setSimulationID(" ");
			}
			
		});
	}
	
	private static void GenerateMarathonerProperties() {
		Random randomNumber = new Random();
		
		//NUMBER, COLOR, COLOR, COLOR
		for (int i = 0; i < marathonerAttributes.length; i++) {
			for (int j = 0; j < marathonerAttributes[i].length; j++) {
				if (j == 0) {
					marathonerAttributes[i][j] = Integer.toString(randomNumber.nextInt(SHIRTNUMBER_RANGE));
				} else {
					marathonerAttributes[i][j] = COLOR_LIST[randomNumber.nextInt(COLOR_LIST.length)];
				}

			}
		}
		
		for (int i = 0; i < Marathoners.length; i++) {
			Marathoners[i] = new TrackPerson(
					(String) marathonerAttributes[i][0],
					(Color) marathonerAttributes[i][1],
					(Color) marathonerAttributes[i][2],
					(Color) marathonerAttributes[i][3]);
		}
		
}
	
	private static void ResetTrack() {
		runners.stop();

		for (int i = 0; i < Marathoners.length; i++) {
			Marathoners[i].StopRunning();
			Marathoners[i].runnerNode().setTranslateX(0);
			trackText[i].setText("Track " + (i + 1) + ": 0");
			marathonFinished[i] = false;
		}
	}
	
	
	public void LoadData() {
		
		data_shared_write_independant = new Object[][] { {"Time", runners} };
		
		data_shared_write_dependant = new Object[][] { {"m1-1", Marathoners[0].runnerNode()},
													   {"m2-1", Marathoners[1].runnerNode()},
													   {"m3-1", Marathoners[2].runnerNode()},
													   {"m4-1", Marathoners[3].runnerNode()},
													   {"m5-1", Marathoners[4].runnerNode()}};
		
		data_shared_read = new Object[][]  { {"Distance", runDistanceBean.getSimVariableBeanProperty()}, 
											 {"Base Time", baseTimeBean.getSimVariableBeanProperty()},
											 {"Speed Modifier", speedModBean.getSimVariableBeanProperty()}}; 
		// Bind buttons to infopane
		Handlers();

		// Data Read by sim
		PhysicsWindow.sharedData.addReadData(data_shared_read);

		// Data Written by sim
		PhysicsWindow.sharedData.addWriteDataJFX(data_shared_write_independant, data_shared_write_dependant);
	}
	
}