package com.licktopia.thechampionofsudoku;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by jeff on 1/21/17.
 */

public class BackgroundImages {



    private int pictureNumber;
    private static int [] backImages =
            {R.drawable.eiffeltower,R.drawable.horse,R.drawable.unpolar,R.drawable.shapes,R.drawable.lighthousegalaxies,R.drawable.haalu,R.drawable.candycane,R.drawable.flowers,
             R.drawable.ripple};

    public int getImage(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, backImages.length);
        pictureNumber = randomNum;
        return backImages[randomNum];
    }

    public int getImage(int pos){
        int randomNum = ThreadLocalRandom.current().nextInt(0, backImages.length);
        return backImages[pos];
    }
    public int getPictureNumber() {
        return pictureNumber;
    }
}

