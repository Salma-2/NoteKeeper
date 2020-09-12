package com.example.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {
    public static final int EDIT_MODE_MODULE_COUNT = 7;
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;
    private float mOutlineWidth;
    private float mShapeSize;
    private float mSpacing;
    private float mRadius;
    private Rect[] mModuleRectangles;
    private int mFillColor;
    private Paint mPaintFill;
    private Paint mPaintOutline;
    private int mOutlineColor;
    private int mMaxHorizontalModules;

    public boolean[] getModuleStatus() {
        return mModuleStatus;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        mModuleStatus = moduleStatus;
    }

    private boolean[] mModuleStatus;


    public ModuleStatusView(Context context) {
        super( context );
        init( null, 0 );
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super( context, attrs );
        init( attrs, 0 );
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
        init( attrs, defStyle );
    }

    private void init(AttributeSet attrs, int defStyle) {

        if (isInEditMode()) {
            setUpEditModeValues();
        }

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0 );

        a.recycle();


        mOutlineWidth = 6f;
        mShapeSize = 144f;
        mSpacing = 30f;
        mRadius = (mShapeSize - mOutlineWidth) / 2;


        mOutlineColor = Color.BLACK;
        mPaintOutline = new Paint( Paint.ANTI_ALIAS_FLAG );
        mPaintOutline.setStyle( Paint.Style.STROKE );
        mPaintOutline.setStrokeWidth( mOutlineWidth );
        mPaintOutline.setColor( mOutlineColor );


        mFillColor = getContext().getResources().getColor( R.color.pluralsight_orange );
        mPaintFill = new Paint( Paint.ANTI_ALIAS_FLAG );
        mPaintFill.setStyle( Paint.Style.FILL );
        mPaintFill.setColor( mFillColor );


    }

    private void setUpEditModeValues() {

        boolean[] exampleModuleValues = new boolean[EDIT_MODE_MODULE_COUNT];
        int middle = EDIT_MODE_MODULE_COUNT / 2;
        for (int x = 0; x < middle; x++) {
            exampleModuleValues[x] = true;
        }
        setModuleStatus( exampleModuleValues );

    }

    private void setUpModuleRectangle(int width) {

        int availableWidth = width - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
        int maxHorizontalModules = Math.min( horizontalModulesThatCanFit, mModuleStatus.length );


        mModuleRectangles = new Rect[mModuleStatus.length];
        for (int moduleIndex = 0; moduleIndex < mModuleRectangles.length; moduleIndex++) {

            /*
            if mMaxHorizontalModules =4
            row = 0 for moduleIndex =0, 1, 2, 3
            column = 0, 1, 2, 3 for moduleIndex =0, 1, 2, 3
            */

            int row = moduleIndex / maxHorizontalModules;
            int column = moduleIndex % maxHorizontalModules;
            int x = getPaddingLeft() + (int) (column * (mShapeSize + mSpacing));
            int y = getPaddingTop() + (int) (row * (mShapeSize + mSpacing));
            mModuleRectangles[moduleIndex] = new Rect( x, y, x + (int) mShapeSize, y + (int) mShapeSize );
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 0;
        int desiredHeight = 0;


        //5leta msh encoded integer
        int specWidth = MeasureSpec.getSize( widthMeasureSpec );
        int availableWidth = specWidth - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (specWidth / (mShapeSize + mSpacing));
        mMaxHorizontalModules = Math.min( horizontalModulesThatCanFit, mModuleStatus.length );


        desiredWidth = (int) ((mMaxHorizontalModules * (mShapeSize + mSpacing)) - mSpacing);
        desiredHeight += getPaddingLeft() + getPaddingRight();


        int rows = (int) ((mModuleStatus.length - 1) / mMaxHorizontalModules) + 1;
        desiredHeight = (int) (rows * (mShapeSize + mSpacing));
        desiredHeight += getPaddingBottom() + getPaddingTop();

        int width = resolveSizeAndState( desiredWidth, widthMeasureSpec, 0 );
        int height = resolveSizeAndState( desiredHeight, heightMeasureSpec, 0 );

        setMeasuredDimension( width, height );
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setUpModuleRectangle(w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw( canvas );

        for (int moduleIndex = 0; moduleIndex < mModuleStatus.length; moduleIndex++) {
            int x = mModuleRectangles[moduleIndex].centerX();
            int y = mModuleRectangles[moduleIndex].centerY();

            if (mModuleStatus[moduleIndex]) {
                canvas.drawCircle( x, y, mRadius, mPaintFill );
            }

            canvas.drawCircle( x, y, mRadius, mPaintOutline );
        }


    }


}
