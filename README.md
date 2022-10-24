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

### First, to **create the canvas**:

Piano keys obviously have a very specific and recognisable look, and I wanted to maintain that look for the best user experience. To do this, I used a digital image of piano keys, similar to [**this**](https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/PianoKeyboard.svg/161px-PianoKeyboard.svg.png?20061008130835), and measured the relative sizes of the height and width of the white and black keys. 

**The units used are irrelevant**, whether its pixels, inches, centimeters, or anything else, so long as the same unit is consistently used. With these measurements, I recorded **the ratios of the keys in relation to each other**, as well as some other useful numbers, as seen below. 

```java
    /*
        white key ratio width : height -- 1 : 5
        black key ratio width : height -- 1.5 : 7 ( for white height of 10 )
        if height = 10, black key starts 4 from bottom
        one octave is 13 keys, 5 black, 8 white ( from low C to high C )
        ratio of canvas size width : height -- 8 : 5
    */

```


