package net.afterday.compas.logging;

/**
 * Created by Justas Spakauskas on 3/20/2018.
 */

public class LogLine {
    private String text,
            date;
    private int resId = -1;
    private long id;
    private int color;

    public LogLine(String text, String date, int color) {
        this(text, date, color, -1);
    }

    public LogLine(String text, String date, int color, int resId) {
        this.text = text;
        this.date = date;
        this.color = color;
        this.resId = resId;
    }

    public LogLine() {

    }

    public String getText() {
        return text;
    }

    public LogLine setText(String text) {
        this.text = text;
        return this;
    }

    public String getDate() {
        return date;
    }

    public LogLine setDate(String date) {
        this.date = date;
        return this;
    }

    public int getColor() {
        return color;
    }

    public LogLine setColor(int color) {
        this.color = color;
        return this;
    }

    public int getResId() {
        return resId;
    }

    public LogLine setId(long id) {
        this.id = id;
        return this;
    }

    public boolean hasResId() {
        return resId > -1;
    }

}
