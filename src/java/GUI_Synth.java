import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import java.util.*;
import java.io.File;

public class GUI_Synth extends Application {
    private static List<String> notes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private Thread[] noteThreads = new Thread[14];
    private boolean[] notesOn = new boolean[14];
    private static MidiChannel[] channels = null;
    private static int INSTRUMENT = 0;
    private static int VOLUME = 100;
    private static int CHANNEL = 0;
    private static Synthesizer synth = null;
    @Override
    public void start(Stage primaryStage)
            throws Exception {

        synth = MidiSystem.getSynthesizer();
        synth.open();
        channels = synth.getChannels();
        primaryStage.setTitle("Piano Keys");
        for(int i = 0; i< notesOn.length; i++){
            notesOn[i] = true;
        }

        /*

        some math to keep track of how big each key is:

        white key ratio width : height -- 1 : 5
        black key ratio width : height -- 1.5 : 7 ( for white height of 10 )
        if height = 10, black key starts 4 from bottom
        one octave is 13 keys, 5 black, 8 white ( from low C to high C )
        ratio of canvas size width : height -- 8 : 5

        */


        int height = 750;
        int width = 1200;
        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc, height, width);

        ComboBox<Instrument> instrumentSelector = new ComboBox<Instrument>(FXCollections.unmodifiableObservableList(getInstrumentList()));
        instrumentSelector.getSelectionModel().select(instrumentSelector.getItems().get(0));
        instrumentSelector.setOnAction(new EventHandler<ActionEvent>() {
            //instrument slider allows user to change what instrument sound to use
            @Override
            public void handle(ActionEvent event) {
                Instrument instrument = instrumentSelector.getValue();
                if (instrument != null) {
                    if (instrument.toString().startsWith("Drumkit")) // drums play on their own MIDI channel
                        CHANNEL = 9;
                    else
                        CHANNEL = 0;
                    channels[CHANNEL].programChange(instrument.getPatch().getBank(), instrument.getPatch().getProgram());
                }
            }
        });

        Slider octaveSlider = new Slider(0, 8, 1);
        octaveSlider.setMajorTickUnit(1);
        octaveSlider.setMinorTickCount(0);
        octaveSlider.setShowTickMarks(true);
        octaveSlider.setSnapToTicks(true);
        octaveSlider.setValue(3);
        canvas.setFocusTraversable(true);
        Button button = new Button("Play song");
        button.setWrapText(true);
        button.setDefaultButton(true);

        Song s1 = new Song("Twinkle Twinkle Little Star", 60);
        s1.parseNotes(new File("D:\\AP_compsci\\GUi_piano_1\\src\\main\\java\\com\\example\\gui_piano_1\\twinkleSong.txt"));
        Song s2 = new Song("Hush Little Baby", 60);
        s2.parseNotes(new File("D:\\AP_compsci\\GUi_piano_1\\src\\main\\java\\com\\example\\gui_piano_1\\hushLittleBaby.txt"));
        Song s3 = new Song("Happy Birthday", 40);
        s3.parseNotes(new File("D:\\\\AP_compsci\\\\GUi_piano_1\\\\src\\\\main\\\\java\\\\com\\\\example\\\\gui_piano_1\\\\happyBirthday.txt"));
        //initializes song variables to be put in the song choice

        ComboBox<Song> songChoice= new ComboBox<Song>(FXCollections.observableArrayList(s1, s2, s3));
        songChoice.getSelectionModel().select(songChoice.getItems().get(0));
        //combo box is used to decide what song to be played
        songChoice.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            }
        });

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Song s = songChoice.getValue();
                playSheetMusic(s, synth);
                //when button is pressed, method to play given song is called
            }
        });


        canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            //when user clicks the cursor, calls method to figure out what note it was, and plays it
            @Override
            public void handle(MouseEvent event) {
                try {
                    playThreadedNote(getNote(event, width, height), (int) octaveSlider.getValue(), noteThreads);
                }catch(Exception e){}
            }
        });
        root.setOnKeyPressed(new EventHandler<KeyEvent>() {
            //when a key on the keyboard is pressed, if it is a key mapped to a note, then it plays the note
            @Override
            public void handle(KeyEvent keyEvent) {
                try {
                    int x = getKeyNum(keyEvent.getText());
                    if(x >= 0 && notesOn[x]){
                        playThreadedNote(getNoteStr(x), (int) octaveSlider.getValue(), noteThreads);
                        notesOn[x] = false;
                    }
                }catch(Exception e){}
            }
        });
        root.setOnKeyReleased(new EventHandler<KeyEvent>() {
            //stops the note playing when a key is released
            @Override
            public void handle(KeyEvent keyEvent) {
                try {
                    int x = getKeyNum(keyEvent.getText());
                    if(x >= 0){
                        noteThreads[x].interrupt();
                        notesOn[x] = true;
                    }
                }catch(Exception e){}
            }
        });
        canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
            //stops playing the note when the user releases the cursor
            @Override
            public void handle(MouseEvent event) {
                noteThreads[getNoteNum(getNote(event, width, height))].interrupt();
            }});

        root.setCenter(canvas);

        HBox controlsBox = new HBox();
        controlsBox.getChildren().addAll(instrumentSelector, octaveSlider, songChoice, button);
        //hbox sets up a table across the top of the canvas

        root.setTop(controlsBox);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }
    private void drawShapes(GraphicsContext gc, int height, int width) {
        //draws one octave of keys based on geometry
        //splits up the screen equally for each key, and uses the ratios defined above
        //should adjust based on varying height and width, accepts those as inputs
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);
        gc.strokeLine(width/8, height, width/8, height*.7);
        gc.strokeLine(width*(2.0/8), height, width*(2.0/8), height*.7);
        gc.strokeLine(width*(3.0/8), height, width*(3.0/8), 0);
        gc.strokeLine(width*(4.0/8), height, width*(4.0/8), height*.7);
        gc.strokeLine(width*(5.0/8), height, width*(5.0/8), height*.7);
        gc.strokeLine(width*(6.0/8), height, width*(6.0/8), height*.7);
        gc.strokeLine(width*(7.0/8), height, width*(7.0/8), 0);
        gc.strokeLine(width, height, width, height*.6);
        //gc.fillRect(x(top left), y(top left), width, height)
        gc.fillRect((double)(width/8)-((width/8)*(.35)),0.0, width*(5.0/64), height*(.7));
        gc.fillRect((double)(width*(2.0/8))-((width*(1.0/8))*(.25)),0.0, width*(5.0/64), height*(.7));
        gc.fillRect((double)(width*(4.0/8))-((width*(1.0/8))*(.375)),0.0, width*(5.0/64), height*(.7));
        gc.fillRect((double)(width*(5.0/8))-((width*(1.0/8))*(.3)),0.0, width*(5.0/64), height*(.7));
        gc.fillRect((double)(width*(6.0/8))-((width*(1.0/8))*(.25)),0.0, width*(5.0/64), height*(.7));
        gc.fillRect((double)(width)-((width*(1.0/8))*(.35)),0.0, width*(5.0/64), height*(.7));

    }
    private static Thread playThreadedNote(String note, int octave, Thread[] noteThreads) {
        //given a note in an octave, it will play the note until either the cursor the key is released
        //plays each note on a different thread so that multiple notes can be played simultaneously
        int noteId = id(note, octave);
        int noteNum = getNoteNum(note);
        int noteChannel = CHANNEL;
//        final int DURATION = 1000;
        noteThreads[noteNum] = new Thread(new Runnable() {
            public void run() {
                channels[noteChannel].noteOn(noteId, VOLUME);
                try {
                    Thread.sleep(Long.MAX_VALUE);
                    //will not turn off until it is interrupted -- done in the release action handlers

                } catch (InterruptedException ex) {
                    channels[noteChannel].noteOff(noteId);
                }

            }
        });
        noteThreads[noteNum].start();
        return noteThreads[noteNum];
    }

    private static ObservableList<Instrument> getInstrumentList() {
        //returns list of available instruments
        ObservableList<Instrument> instruments = FXCollections.observableArrayList();
        for (Instrument instrument : synth.getAvailableInstruments()) {
            instruments.add(instrument);
        }
        return instruments;
    }
    private static void playSheetMusic(Song song, Synthesizer synth){
        //plays all the notes of a song in the current instrument
        ArrayList<Note> sheetMusic = song.getSong();
        int bpm = song.getBpm();
        for(int i = 0; i < sheetMusic.size(); i++){
            if(sheetMusic.get(i).getNote().equals("R")) {
                try {
                    rest((int) ((60000.0 / bpm) * sheetMusic.get(i).getDuration()));
                    //when it is at a rest(break), it will pause for the duration
                    //duration is converted to milliseconds based on the bpm(beats per minute)
                } catch (InterruptedException IE) {
                }
            }
            else
                playNote(sheetMusic.get(i).getNote(), synth, (int)((60000.0/bpm) * sheetMusic.get(i).getDuration()));
                //if it is not a rest, it will play a specified note for the duration
                //duration is converted from bpm to milliseconds
        }
    }
    private static void playNote(String note, Synthesizer synth, int duration){
        //sets the channel to the current instrument, and makes a method call to play()
        try {
            channels = synth.getChannels();
            play(note,  duration);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void play(String note, int duration) throws InterruptedException
    {
        //plays a note for a specified duration
        channels[INSTRUMENT].noteOn(id(note, 6), VOLUME );
        Thread.sleep( duration );
        channels[INSTRUMENT].noteOff(id(note, 6));

    }
    private static void rest(int duration) throws InterruptedException
    {
        //will stop playing notes for specified duration
        Thread.sleep(duration);
    }
    private static int id(String note, int octaveBase)
    {
        //
        int octave = Integer.parseInt(note.substring(0, 1)); // 6 or 7 -- remap to 0, 1 and add to octave slider value
        return notes.indexOf(note.substring(1)) + 12 * (octave + octaveBase - 6) + 12;
    }
    public static void main(String[] args) {
        launch(args);
    }
    public static int getKeyNum(String text){
        //will convert a keyboard input into a number
        //the number can be used for knowing what note to play in thread
        //if the key is not mapped to a note, it returns -1 as the base case
        text = text.toUpperCase();
        if(text.equals("S")) return 0;
        if(text.equals("E")) return 1;
        if(text.equals("D")) return 2;
        if(text.equals("R")) return 3;
        if(text.equals("F")) return 4;
        if(text.equals("G")) return 5;
        if(text.equals("Y")) return 6;
        if(text.equals("H")) return 7;
        if(text.equals("U")) return 8;
        if(text.equals("J")) return 9;
        if(text.equals("I")) return 10;
        if(text.equals("K")) return 11;
        if(text.equals("L")) return 12;
        if(text.equals("P")) return 13;
        return -1;
    }
    public static String getNoteStr(int num){
        //converts a number to the string representation of the note
        if(num == 0) return "6C";
        if(num == 1) return "6C#";
        if(num == 2) return "6D";
        if(num == 3) return "6D#";
        if(num == 4) return "6E";
        if(num == 5) return "6F";
        if(num == 6) return "6F#";
        if(num == 7) return "6G";
        if(num == 8) return "6G#";
        if(num == 9) return "6A";
        if(num == 10) return "6A#";
        if(num == 11) return "6B";
        if(num == 12) return "7C";
        if(num == 13) return "7C#";
        return "6C";
    }
    public static int getNoteNum(String text){
        //converts the string representation of a note to the corresponding number
        if(text.equals("6C")) return 0;
        if(text.equals("6C#")) return 1;
        if(text.equals("6D")) return 2;
        if(text.equals("6D#")) return 3;
        if(text.equals("6E")) return 4;
        if(text.equals("6F")) return 5;
        if(text.equals("6F#")) return 6;
        if(text.equals("6G")) return 7;
        if(text.equals("6G#")) return 8;
        if(text.equals("6A")) return 9;
        if(text.equals("6A#")) return 10;
        if(text.equals("6B")) return 11;
        if(text.equals("7C")) return 12;
        if(text.equals("7C#")) return 13;
        return 0;
    }
    public static String getNote(MouseEvent event, int width, int height){
        /*
        based on where the cursor is clicked, determines which key it is on
        uses the X and Y values of the MouseEvent to determine where on the canvas it is
        returns a string representation of a note
         */
        if (event.getX() <= width / 8) {
            if (event.getX() >= (double) (width / 8) - ((width / 8) * (.35)) && event.getY() <= (height * .7)) {
                return "6C#";
            } else {
                return "6C";
            }
        } else if (event.getX() <= (width * (2.0 / 8))) {
            if (event.getX() <= (double) (width / 8) - ((width / 8) * (.35)) + width * (5.0 / 64) && event.getY() <= (height * .7)) {
                return "6C#";
            } else if (event.getX() >= (double) (width * (2.0 / 8)) - ((width * (1.0 / 8)) * (.25)) && event.getY() <= (height * .7)) {
                return "6D#";
            } else {
                return "6D";
            }
        } else if (event.getX() <= width * (3.0 / 8)) {
            if (event.getX() <= (double) (width * (2.0 / 8)) - ((width * (1.0 / 8)) * (.25)) + width * (5.0 / 64) && event.getY() <= (height * .7)) {
                return "6D#";
            } else {
                return "6E";
            }
        } else if (event.getX() <= width * (4.0 / 8)) {
            if (event.getX() >= (double) (width * (4.0 / 8)) - ((width * (1.0 / 8)) * (.375)) && event.getY() <= (height * .7)) {
                return "6F#";
            } else {
                return "6F";
            }
        } else if (event.getX() <= width * (5.0 / 8)) {
            if (event.getX() <= (double) (width * (4.0 / 8)) - ((width * (1.0 / 8)) * (.375)) + width * (5.0 / 64) && event.getY() <= (height * .7)) {
                return "6F#";
            } else if (event.getX() >= (double) (width * (5.0 / 8)) - ((width * (1.0 / 8)) * (.3)) && event.getY() <= (height * .7)) {
                return "6G#";
            } else {
                return "6G";
            }
        } else if (event.getX() <= width * (6.0 / 8)) {
            if (event.getX() <= (double) (width * (5.0 / 8)) - ((width * (1.0 / 8)) * (.3)) + width * (5.0 / 64) && event.getY() <= (height * .7)) {
                return "6G#";
            } else if (event.getX() >= (double) (width * (6.0 / 8)) - ((width * (1.0 / 8)) * (.25)) && event.getY() <= (height * .7)) {
                return "6A#";
            } else {
                return "6A";
            }
        } else if (event.getX() <= width * (7.0 / 8)) {
            if (event.getX() <= (double) (width * (6.0 / 8)) - ((width * (1.0 / 8)) * (.25)) + width * (5.0 / 64) && event.getY() <= (height * .7)) {
                return "6A#";
            } else {
                return "6B";
            }
        } else {
            if (event.getX() >= (double) (width) - ((width * (1.0 / 8)) * (.35)) && event.getY() <= (height * .7)) {
                return "7C#";
            } else {
                return "7C";
            }
        }
    }


}
