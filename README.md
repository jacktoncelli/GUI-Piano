# GUI-Piano
A GUI project using Javafx to create a playable piano
----------

This project was my first big GUI project, and I spent most of my time with it experimenting with various features of **javafx**. The final product is a playable piano with some extra features, including:
- **"clickable" keys**, i.e; mouse inputs
- a **keyboard interface**, i.e; keyboard input for the notes
- a choice of **several octaves** totaling to 88 keys
- **different instrument** selection
- **pre-written** and playable songs

For further information on the logistics of the project, please read below: 
-------------

### To **create the canvas**:

Piano keys obviously have a very specific and recognisable look, and I wanted to maintain that look for the best user experience. To do this, I used a digital image of piano keys, one similar to [**this**](https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/PianoKeyboard.svg/161px-PianoKeyboard.svg.png?20061008130835), and measured the relative sizes of the height and width of the white and black keys. 

**The units used are irrelevant**, whether its pixels, inches, centimeters, or anything else, so long as the same unit is consistently used. With these measurements, I recorded **the ratios of the keys in relation to each other**, as well as some other useful numbers, as shown here. 

```java
    /*
        white key ratio width : height -- 1 : 5
        black key ratio width : height -- 1.5 : 7 ( for white height of 10 )
        if height = 10, black key starts 4 from bottom
        one octave is 13 keys, 5 black, 8 white ( from low C to high C )
        ratio of canvas size width : height -- 8 : 5
    */

```

To actually draw the keys onto the canvas, I made a helper function, ***drawShapes()***. Instead of creating each key as a button, this method accepted the height and width of the canvas and used some light geometry to split it up according to the defined ratios of the keys. For white keys, I utilized the ***strokeLine*** method to draw their seperating lines. For black keys, I used the ***fillRect()*** method built into javafx canvas. 


-------------

### To play the notes:

As part of initialization, two arrays are created, **noteThreads** and **notesOn**. 

```java
    private Thread[] noteThreads = new Thread[14];
    private boolean[] notesOn = new boolean[14];
```

noteThreads is used to allocate a individual thread to play for each note. Doing so allows for multiple notes to be played simultaneously. notesOn is an array of booleans that represents whether a note is currently playing or not. notesOn is used as a layer of protection against trying to play a note on an already active thread.

    

By making a visual display instead of a series of buttons, I had to create my own way of collecting mouse inputs. Luckily, the MouseEvent class has the built in classes of ***getX()*** and ***getY()***, which return the x and y coordinates, respectively, of where the cursor has been clicked. Based on those values, the helper method ***getNote()*** uses a series of **if** statements to determine which key the click is on. 

When the mouse is clicked, an EventHandler calls getNote(), and passes the string into a seperate helper method, ***playThreadedNote()***, and this method will activate the correct thread within noteThreads. When the thread is activated, the computer on which the program is being run should play the note(if it has functioning speakers). 

In order to stop playing the note, the thread must be turned off. A seperate EventHandler from the one used to activate the thread is called when the mouse is released. 


