package com.letv.shared.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.letv.shared.util.MedianCutQuantizer.ColorNode;

import java.util.Arrays;
import java.util.Comparator;

public class DominantColorFinder {
    private static final int NUM_COLORS = 16; 
    
    private final ColorNode[] mPalette;
    private final ColorNode[] mWeightedPalette;
    
    public DominantColorFinder(Bitmap bitmap) {
        this(bitmap, NUM_COLORS);
    }
    
    public DominantColorFinder(Bitmap bitmap, int colorNumber) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
 
        final int[] rgbPixels = new int[width * height];
        bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height);
        
        if (colorNumber < 4) {
            colorNumber = 4;
        }
 
        final MedianCutQuantizer mcq = new MedianCutQuantizer(rgbPixels, colorNumber);
 
        mPalette = mcq.getQuantizedColors();
        mWeightedPalette = weight(mPalette);
    }
    
    public ColorNode[] getDominantColorList() {
        ColorNode[] copy = Arrays.copyOf(mWeightedPalette, mWeightedPalette.length);
        return copy;
    }
    
    public int getDominantColor() {
        return mWeightedPalette[0].getRgb();
    }
    
    public int getDominantColorExcludeWhite() {
        for (ColorNode colorNode : mWeightedPalette) {
            
            if (!isWhite(colorNode.getRgb()))
                return colorNode.getRgb();
        }
        
        return mWeightedPalette[0].getRgb();
    }
    
    public int getDominantColorExcludeBlack() {
        for (ColorNode colorNode : mWeightedPalette) {
            
            if (!isBlack(colorNode.getRgb()))
                return colorNode.getRgb();
        }
        
        return mWeightedPalette[0].getRgb();
    }
    
    boolean isWhite(int rgb) {
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        
        return (r>=0xF4 && g>=0xF4 && b>=0xF4) ? true : false;
    }
    
    boolean isBlack(int rgb) {
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);
        
        return (r<=10 && g<=10 && b<=10) ? true : false;
    }
    
    
    private static ColorNode[] weight(ColorNode[] palette) {
        final ColorNode[] copy = Arrays.copyOf(palette, palette.length);
        float tmaxCount = palette[0].getCount();
        
        for (ColorNode node : copy) {
            if (node.getCount() > tmaxCount)
                tmaxCount = (float)node.getCount();
        }
        
        final float maxCount = tmaxCount;
 
        Arrays.sort(copy, new Comparator<ColorNode>() {
            @Override
            public int compare(ColorNode lhs, ColorNode rhs) {
                final float lhsWeight = calculateWeight(lhs, maxCount);
                final float rhsWeight = calculateWeight(rhs, maxCount);
 
                if (lhsWeight < rhsWeight) {
                    return 1;
                } else if (lhsWeight > rhsWeight) {
                    return -1;
                }
                return 0;
            }
        });
 
        return copy;
    }
 
    private static float calculateWeight(ColorNode node, float maxCount) {
        return weightedAverage(
                calculateColorfulness(node), 2f,
                (node.getCount() / maxCount), 1f
        );
    }
    
    public static final float calculateColorfulness(ColorNode node) {
        float[] hsv = node.getHsv();
        return hsv[1] * hsv[2];
    }
    
    public static float weightedAverage(float... values) {
        assert values.length % 2 == 0;
        
        float sum = 0;
        float sumWeight = 0;
        
        for (int i = 0; i < values.length; i += 2) {
            float value = values[i];
            float weight = values[i + 1];
            
            sum += (value * weight);
            sumWeight += weight;
        }
        
        return sum / sumWeight;
    }
}
