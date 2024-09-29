package com.example.clarivate_employee_privilege.navbar_menu.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CameraOverlayViewPortrait extends View {

    private Paint paint;
    private Paint bracketPaint;
    private RectF rect;

    public CameraOverlayViewPortrait(Context context) {
        super(context);
        init();
    }

    public CameraOverlayViewPortrait(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraOverlayViewPortrait(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xCC000000); // Semi-transparent black

        bracketPaint = new Paint();
        bracketPaint.setColor(0xFFFFFFFF); // White color
        bracketPaint.setStrokeWidth(8); // Thickness of the brackets
        bracketPaint.setStyle(Paint.Style.STROKE); // Stroke style for the brackets

        // Apply CornerPathEffect to round the corners of the brackets
        PathEffect cornerEffect = new CornerPathEffect(10); // Adjust the radius as needed
        bracketPaint.setPathEffect(cornerEffect);

        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Define the rectangle in the middle of the screen
        int rectWidth = width * 3 / 4;
        int rectHeight = height * 3 / 5;
        int left = (width - rectWidth) / 2;
        int top = (height - rectHeight) / 2;
        int right = left + rectWidth;
        int bottom = top + rectHeight;

        rect.set(left, top, right, bottom);

        // Draw the darkened area outside the rectangle
        canvas.drawRect(0, 0, width, top, paint);
        canvas.drawRect(0, top, left, bottom, paint);
        canvas.drawRect(right, top, width, bottom, paint);
        canvas.drawRect(0, bottom, width, height, paint);

        // Draw white crosshair brackets around the rectangle
        int bracketLength = 90; // Length of the brackets
        int bracketOffset = 4; // Offset to make the lines closer together

        // Top-left corner
        Path topLeftPath = new Path();
        topLeftPath.moveTo(left, top);
        topLeftPath.lineTo(left + bracketLength, top);
        topLeftPath.moveTo(left + bracketOffset, top);
        topLeftPath.lineTo(left + bracketOffset, top + bracketLength);
        canvas.drawPath(topLeftPath, bracketPaint);

        // Top-right corner
        Path topRightPath = new Path();
        topRightPath.moveTo(right, top);
        topRightPath.lineTo(right - bracketLength, top);
        topRightPath.moveTo(right - bracketOffset, top);
        topRightPath.lineTo(right - bracketOffset, top + bracketLength);
        canvas.drawPath(topRightPath, bracketPaint);

        // Bottom-left corner
        Path bottomLeftPath = new Path();
        bottomLeftPath.moveTo(left, bottom);
        bottomLeftPath.lineTo(left + bracketLength, bottom);
        bottomLeftPath.moveTo(left + bracketOffset, bottom);
        bottomLeftPath.lineTo(left + bracketOffset, bottom - bracketLength);
        canvas.drawPath(bottomLeftPath, bracketPaint);

        // Bottom-right corner
        Path bottomRightPath = new Path();
        bottomRightPath.moveTo(right, bottom);
        bottomRightPath.lineTo(right - bracketLength, bottom);
        bottomRightPath.moveTo(right - bracketOffset, bottom);
        bottomRightPath.lineTo(right - bracketOffset, bottom - bracketLength);
        canvas.drawPath(bottomRightPath, bracketPaint);
    }
}