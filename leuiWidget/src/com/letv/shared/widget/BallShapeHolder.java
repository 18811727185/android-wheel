package com.letv.shared.widget;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;

public class BallShapeHolder {

    private ShapeDrawable shapeDrawable;
    /** postion */
    private float x, y;
    /** the radius of circle */
    private float radius;
    private Paint paint;
    private int color;
    private float arc;

    public ShapeDrawable getShapeDrawable() {
        return shapeDrawable;
    }

    public void setShapeDrawable(ShapeDrawable s) {
        shapeDrawable = s;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        shapeDrawable.getShape().resize(radius * 2, radius * 2);
        this.radius = radius;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        shapeDrawable.getPaint().setColor(color);
        this.color = color;
    }

    public float getArc() {
        return arc;
    }

    public void setArc(float arc) {
        this.arc = arc;
    }

    public BallShapeHolder(ShapeDrawable s) {
        shapeDrawable = s;
    }
}
