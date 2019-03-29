package com.junipersys.a3_chamber_test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;

public class GraphicsTest extends View {
    private Drawable mDrawable;
    private ShapeDrawable drawable;

    public GraphicsTest(Context context) {
        super(context);

        int x = 10;
        int y = 10;
        int width = 300;
        int height = 50;

        drawable = new ShapeDrawable(new OvalShape());
        // If the color isn't set, the shape uses black as the default.
        drawable.getPaint().setColor(0xff74AC23);
        // If the bounds aren't set, the shape can't be drawn.
        drawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas){
        drawable.draw(canvas);
    }
}
