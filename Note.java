package com.example.johnsaunders.mydiary;

public class Note implements Comparable<Note> {

    private String contents, title,  date;
    private int id;

    public Note(String c, String t, String d, int id){
        contents = c;
        title = t;
        date = d;
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public String getContents(){
        return contents;
    }


    public String getDate(){
        return date;
    }


    public int getId(){return id;}

    @Override
    public int compareTo(Note n){
        //first compare year, then month, then day format: Sun Aug 12 17:00:30 EDT 2018
        int thisYear = Integer.parseInt(date.substring(24,28));
        int compareYear = Integer.parseInt(n.getDate().substring(24,28));

        if(thisYear != compareYear){
            return thisYear - compareYear;
        }

        int thisMonth = getNumMonth(date.substring(4,8));
        int compareMonth = getNumMonth(n.getDate().substring(4,8));

        if(thisMonth != compareMonth){
            return thisMonth - compareMonth;
        }


        int thisDay = Integer.parseInt(date.substring(8,10));
        int compareDay = Integer.parseInt(n.getDate().substring(8,10));

        if(thisDay != compareDay)
            return thisDay - compareDay;

        int thishour = Integer.parseInt(date.substring(11,13));
        int comparehour = Integer.parseInt(n.getDate().substring(11,13));

        if(thishour != comparehour)
            return thishour - comparehour;

        int thisMin = Integer.parseInt(date.substring(14,16));
        int compareMin = Integer.parseInt(n.getDate().substring(14,16));

        if(thisMin != compareMin){
            return thisMin - compareMin;
        }

        int thisSec = Integer.parseInt(date.substring(17,19));
        int compareSec = Integer.parseInt(n.getDate().substring(17,19));

        return thisSec - compareSec;

    }

    public static int getNumMonth(String month){
        if(month.equals("Jan"))
            return 1;
        else if(month.equals("Feb"))
            return 2;
        else if(month.equals("Mar"))
            return 3;
        else if(month.equals("Apr"))
            return 4;
        else if(month.equals("May"))
            return 5;
        else if(month.equals("Jun"))
            return 6;
        else if(month.equals("Jul"))
            return 7;
        else if(month.equals("Aug"))
            return 8;
        else if(month.equals("Sep"))
            return 9;
        else if(month.equals("Oct"))
            return 10;
        else if(month.equals("Nov"))
            return 11;
        else
            return 12;
    }
}
