package com.example.gui_piano_1;

import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
public class Song{
    private String name;
    private int bpm;
    private ArrayList<Note> sheetMusic = new ArrayList<Note>();

    public Song(String name, int bpm){
        this.name = name;
        this.bpm = bpm;
    }

    //below are setters and getters for class attributes
    public void setName(String name){this.name = name;}
    public String getName(){return this.name;}
    public void setBpm(int bpm){this.bpm = bpm;}
    public int getBpm(){return bpm;}
    public ArrayList<Note> getSong(){return sheetMusic;}
    public String toString(){return name;}


    //Precondition: notes properly formatted, in uppercase, followed by double for tempo, and seperated by space character
    //tempo is assumed to be in 4/4
    //double following note is percentage of a measure
    //quarter note is .25, eigth is .125, etc
    //rests are written as R with number indicating how long rest should be
    // ex: "6C.25 6D.25 6E.125 R.25 6F1.0" etc
    public void parseNotes(String text){
        String [] notes = text.split("\\s+");
        for(int i = 0; i<notes.length; i++){
            notes[i] = notes[i].trim();
            int index = notes[i].indexOf(".");
            if(Character.isDigit(notes[i].charAt(index-1)))
                sheetMusic.add(new Note(notes[i].substring(0,index-1), Double.parseDouble(notes[i].substring(index-1))));
            else
                sheetMusic.add(new Note(notes[i].substring(0,index), Double.parseDouble(notes[i].substring(index))));
        }
    }
    public void parseNotes(File filename){
        //iterates through a file to parse all the notes in a file into notes
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String str = "";
            while ((str = br.readLine()) != null) {
                parseNotes(str);
            }
        }catch (Exception e){System.out.println("Error reading file");}

    }
    public void clearNotes(){
        //empties all the notes from sheetMusic
        sheetMusic.clear();
    }

}