package com.licktopia.thechampionofsudoku;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Handles state changes for the display of an individual Sudoku square
 */

public class SquareView {

    private static final float COLOR_SQUARE_TRANSPARENCY = .8f;
    private static final float COLOR_SQUARE_SCALE_FACTOR = .9f;
    private static final float COLOR_SQUARE_SAME_NUMBER_TRANSPARENCY = .2f;

    private Context context;
    private RelativeLayout squareHolder;
    private RelativeLayout highlight;
    private RelativeLayout selectedSquare;
    private ImageView number;
    private RelativeLayout color;
    private LinearLayout notepad;
    private ImageView[] notepadNums;
    private boolean colorMode;
    private ImageView squareTexture;
    private ImageView whiteX;


    public RelativeLayout getSquareHolder() {
        return squareHolder;
    }

    /* Constructor */
    public SquareView(Context context, RelativeLayout squareHolder, boolean colorState) {

        this.context = context;
        this.squareHolder = squareHolder;
        this.notepadNums = new ImageView[9];
        this.colorMode = colorState;

        inflateSquareHolder();
    }


    /* Inflate view into square layout */
    private void inflateSquareHolder() {
        LayoutInflater li = LayoutInflater.from(context);

        View v = li.inflate(R.layout.square_view, null);
        highlight = (RelativeLayout) v.findViewById(R.id.highlight);
        selectedSquare = (RelativeLayout) v.findViewById(R.id.selected);
        number = (ImageView) v.findViewById(R.id.squareNumber);
        color = (RelativeLayout) v.findViewById(R.id.squareColor);
        notepad = (LinearLayout) v.findViewById(R.id.squareNotepad);
        whiteX = (ImageView) v.findViewById(R.id.white_x);
        squareTexture = (ImageView)v.findViewById(R.id.square_texture);
        notepadNums[0] = (ImageView) v.findViewById(R.id.squareNotepad1);
        notepadNums[1] = (ImageView) v.findViewById(R.id.squareNotepad2);
        notepadNums[2] = (ImageView) v.findViewById(R.id.squareNotepad3);
        notepadNums[3] = (ImageView) v.findViewById(R.id.squareNotepad4);
        notepadNums[4] = (ImageView) v.findViewById(R.id.squareNotepad5);
        notepadNums[5] = (ImageView) v.findViewById(R.id.squareNotepad6);
        notepadNums[6] = (ImageView) v.findViewById(R.id.squareNotepad7);
        notepadNums[7] = (ImageView) v.findViewById(R.id.squareNotepad8);
        notepadNums[8] = (ImageView) v.findViewById(R.id.squareNotepad9);
        for (int i = 0; i < notepadNums.length; i++) {
            if(!colorMode) {
                notepadNums[i].setImageDrawable(ContextCompat.getDrawable(context, Tags.numberImageTags[i]));
            }
            else{
                notepadNums[i].setImageDrawable(ContextCompat.getDrawable(context, Tags.colorButtonTags[i]));
            }
        }

        squareHolder.addView(v);
    }


    /* Set number displayed in square */
    public void setBigNumber(int n, boolean isStartingSquare) {
        if (n == 0) {
            number.setImageDrawable(null);   // Remove display object
            number.setVisibility(View.INVISIBLE);
            notepad.setVisibility(View.VISIBLE);
        }
        else {
            if(isStartingSquare) {
                number.setImageDrawable(ContextCompat.getDrawable(context, Tags.numberImageTags[n - 1])); // Set background image
            }
            else{
                number.setImageDrawable(ContextCompat.getDrawable(context, Tags.greyNumberImageTags[n - 1])); // Set background image
            }
            number.setVisibility(View.VISIBLE);
            notepad.setVisibility(View.INVISIBLE);
        }
    }

    /* Set color displayed in square */
    public void setSquareColor(int n, boolean startSquare) {
        if (n == 0) {
            color.setBackground(null);   // Remove display object
            squareHolder.setAlpha(1);
            notepad.setVisibility(View.VISIBLE);
        }
        else {
            color.setBackground(ContextCompat.getDrawable(context, Tags.colors[n - 1])); // Set background image
            squareHolder.setAlpha(COLOR_SQUARE_TRANSPARENCY);
            if(!startSquare){
                color.setScaleX(COLOR_SQUARE_SCALE_FACTOR);
                color.setScaleY(COLOR_SQUARE_SCALE_FACTOR);
            }
            notepad.setVisibility(View.INVISIBLE);
        }
    }

    /* Set number displayed in square */
    public void setBigNumberAnimated(int n) {
        if (n == 0)
            number.setImageDrawable(null);   // Remove display object
        else {
            number.setAlpha(0f);
            number.setImageDrawable(ContextCompat.getDrawable(context, Tags.numberImageTags[n - 1])); // Set background image
            ObjectAnimator mainFade = ObjectAnimator.ofFloat(number, "alpha", 0, 1);
            mainFade.setDuration(700);
            mainFade.start();

        }
    }

    /* Set color displayed in square */
    public void setSquareColorAnimated(int n) {
        if (n == 0) {
            color.setBackground(null);   // Remove display object
            squareHolder.setAlpha(1);
        }
        else {
            color.setAlpha(0f);
            color.setBackground(ContextCompat.getDrawable(context, Tags.colors[n - 1])); // Set background image
            ObjectAnimator mainFade = ObjectAnimator.ofFloat(color, "alpha", 0, 1);
            ObjectAnimator mainFade2 = ObjectAnimator.ofFloat(squareHolder, "alpha", 1, COLOR_SQUARE_TRANSPARENCY);
            mainFade.setDuration(700);
            mainFade2.setDuration(700);
            mainFade.start();
            mainFade2.start();

        }
    }


    /* Set visibility of notepad */
    public void setNotepadDisplay(boolean visible) {
        if (visible) notepad.setVisibility(View.VISIBLE);
        else notepad.setVisibility(View.INVISIBLE);
    }


    /* Set visibility of notepad number (1 - 9)*/
    public void showNotepadNumber(int num, boolean show) {
        notepad.setVisibility(View.VISIBLE);
        number.setVisibility(View.INVISIBLE);
        if (show)
            notepadNums[num - 1].setVisibility(View.VISIBLE);
        else
            notepadNums[num - 1].setVisibility(View.INVISIBLE);
    }

    /* Toggle visibility of notepad number (1 - 9) */
    public void toggleNotepadNumber(int num) {
        if (notepadNums[num - 1].getVisibility() == View.VISIBLE)
            showNotepadNumber(num, false);
        else
            showNotepadNumber(num, true);
    }


    /* Set art displayed in square*/
    public void setArt() {
        //TODO
    }


    /* Sets highlight state of current square */
    public void setHighlighted(boolean highlighted, boolean numberMode) {
        if(numberMode) {
            if (highlighted)
                setHighlightColor(ContextCompat.getColor(context, R.color.colorHighLight)); // Set to generic highlighted
            else {
                setHighlightColor(ContextCompat.getColor(context, R.color.white)); // Remove highlight
                selectedSquare.setBackground(null);
            }
        }
        else{
            if (highlighted) {
                if (color.getBackground() == null) {
                    squareTexture.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.grey_highlight));
                }
                else{
                    squareTexture.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.white_highlight));

                }
                squareTexture.setVisibility(View.VISIBLE); // Set to generic highlighted
            }
            else {
                squareTexture.setVisibility(View.GONE); // Remove highlight
                setHighlightColor(ContextCompat.getColor(context, R.color.white)); // Remove highlight
                selectedSquare.setBackground(null);
            }
            whiteX.setVisibility(View.GONE);
        }
    }

    public void setNotepadHighlightRed(int num) {
        notepadNums[num - 1].setBackgroundColor(ContextCompat.getColor(context, R.color.red));
    }

    public void setNotepadHighlightGreen(int num) {
        notepadNums[num - 1].setBackgroundColor(ContextCompat.getColor(context, R.color.color1));
    }

    public void clearNotepadHighlights() {
        for (ImageView np : notepadNums) {
            np.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        }
    }

    /* Sets highlight state to error color */
    public void setErrorHighlight(boolean colorGame) {
        if(!colorGame) {
            setHighlightColor(ContextCompat.getColor(context, R.color.highlightred));
        }
        else{
            whiteX.setVisibility(View.VISIBLE);
        }
    }

    public void setDarkHighlight() {
        setHighlightColor(ContextCompat.getColor(context, R.color.silver));
    }

    public void setSelectedHighlight() {
        setHighlightMarker();
    }

    public void setSameNumberHighlight() {
        setHighlightColor(ContextCompat.getColor(context, R.color.samecolorhiglight));
    }

    public void setSameColorHighlight() {
        color.setAlpha(COLOR_SQUARE_SAME_NUMBER_TRANSPARENCY);
    }

    public void setSameColorHighlightNotepad() {
        for (ImageView i : notepadNums) {
            i.setAlpha(COLOR_SQUARE_SAME_NUMBER_TRANSPARENCY);
        }
    }

    /* Sets highlight layout to given color */
    private void setHighlightColor(int color) {
        highlight.setBackgroundColor(color);
    }

    /* Sets highlight layout to given selected marker */
    private void setHighlightMarker() {
       selectedSquare.setBackground(ContextCompat.getDrawable(context, R.drawable.selected_square_marker));

    }
    /* Sets Alpha back to full */
    public void setFullAlpha() {
        color.setAlpha(1f);
        for (ImageView i : notepadNums) {
            i.setAlpha(1f);
        }
    }


}
