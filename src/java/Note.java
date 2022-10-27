package com.example.gui_piano_1;

public class Note{
    /*
    the Note class is a helper class for the Song class
    every note has a string representation of the note, and a duration

     */
    private String note;
    private double duration;
    public Note(String note, double duration){
        this.note = note;
        this.duration = duration;
    }
    public Note(){}

    //the below methods are getters and setters to access the class attributes
    public void setNote(String note){ this.note = note;}
    public String getNote(){ return note;}
    public void setDuration(double duration){ this.duration = duration;}
    public double getDuration(){ return duration;}
    @Override
    public String toString(){ return note + ", " + duration;}
}