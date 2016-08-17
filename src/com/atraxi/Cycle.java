package com.atraxi;

import java.awt.Color;

/**
 * Created by Atraxi on 17/08/2016.
 */
public class Cycle
{
    private static final int CYCLE_SPEED = 280;
    private static final int PRECISION = 100;
    private static final boolean USE_BLEND_SHIFT = true;
    private double reverse;
    private double rate;
    private double low;
    private double high;
    private double current;

    public Cycle(int reverse, int rate, int low, int high)
    {
        this.reverse = reverse;
        this.rate = rate;
        this.low = low;
        this.high = high;
        current = low;
    }

    public boolean rotate(Color[] colors, long time)
    {
        if(rate != 0 && time % rate < 50)
        {
            Color colorBackup = colors[(int) current];
            current++;
            if(current > high)
            {
                current -= high - low;//wrap around
            }
            for(int i = 0; i < high - low - 1; i++)
            {
                int offset = (int) ((i + current) % (high - low));
                colors[(int) (low + offset)] = colors[(int) (low + ((offset + 1) % (high - low)))];
            }
            colors[(int) current] = colorBackup;
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean cycle(Color[] colors, long timeInMS)
    {
        if(rate != 0)
        {
            double cycleSize = (high - low) + 1;
            double cycleRate = rate / CYCLE_SPEED;
            double cycleAmount = 0;

            if(reverse < 3)
            {
                // standard cycle
                cycleAmount = DFLOAT_MOD((timeInMS / (1000.0 / cycleRate)), cycleSize);
            }
            else if(reverse == 3)
            {
                // ping-pong
                cycleAmount = DFLOAT_MOD((timeInMS / (1000 / cycleRate)), cycleSize * 2);
                if(cycleAmount >= cycleSize) cycleAmount = (cycleSize * 2) - cycleAmount;
            }
            else if(reverse < 6)
            {
                // sine wave
                cycleAmount = DFLOAT_MOD((timeInMS / (1000 / cycleRate)), cycleSize);
                cycleAmount = Math.sin((cycleAmount * 3.1415926 * 2) / cycleSize) + 1;
                if(reverse == 4) cycleAmount *= (cycleSize / 4);
                else if(reverse == 5) cycleAmount *= (cycleSize / 2);
            }

            if(reverse == 2) reverseColors(colors, low, high);

            if(USE_BLEND_SHIFT) blendShiftColors(colors, low, high, cycleAmount);
            else shiftColors(colors, low, high, cycleAmount);

            if(reverse == 2) reverseColors(colors, low, high);

            return true;
        }
        else
        {
            return false;
        }
    }

    private static void reverseColors(Color[] colors, double low, double high)
    {
        // reverse order of colors
        int i;
        double cycleSize = (high - low) + 1;

        for(i = 0; i < cycleSize / 2; i++)
        {
            swapColors(colors, low + i, high - i);
        }
    }

    private static void swapColors(Color[] colors, double indexA, double indexB)
    {
        Color temp = colors[(int) indexA];
        colors[(int) indexA] = colors[(int) indexB];
        colors[(int) indexB] = temp;
    }

    private static void shiftColors(Color[] colors, double low, double high, double amount)
    {
        // shift (hard cycle) colors by amount
        Color temp;
        amount = Math.floor(amount);

        for(int i = 0; i < amount; i++)
        {
            temp = colors[(int) high];
            for(double j = high - 1; j >= low; j--)
            {
                colors[(int) (j + 1)] = colors[(int) j];
            }
            colors[(int) low] = temp;
        } // i loop
    }

    private static void blendShiftColors(Color[] colors, double low, double high, double amount)
    {
        // shift colors using BlendShift (fade colors creating a smooth transition)
        // BlendShift Technology conceived, designed and coded by Joseph Huckaby
        int j;
        Color temp;

        shiftColors(colors, low, high, amount);

        double frame = Math.floor((amount - Math.floor(amount)) * PRECISION);

        temp = colors[(int) high];
        for(j = (int) (high - 1); j >= low; j--)
        {
            colors[j + 1] = fadeColor(colors[j + 1], colors[j], frame, PRECISION);
        }
        colors[(int) low] = fadeColor(colors[(int) low], temp, frame, PRECISION);
    }

    private static Color fadeColor(Color sourceColor, Color destColor, double frame, int max)
    {
        // fade one color into another by a partial amount, return new color in between
        Color tempColor;

        if(max == 0) return sourceColor; // avoid divide by zero
        if(frame < 0) frame = 0;
        if(frame > max) frame = max;

        tempColor = new Color((int)Math.floor(sourceColor.getRed() + (((destColor.getRed() - sourceColor.getRed()) * frame) / max)),
                              (int)Math.floor(sourceColor.getGreen() + (((destColor.getGreen() - sourceColor.getGreen()) * frame) / max)),
                              (int)Math.floor(sourceColor.getBlue() + (((destColor.getBlue() - sourceColor.getBlue()) * frame) / max)));

        return (tempColor);
    }

    private static double DFLOAT_MOD(double a, double b)
    {
        return (Math.floor(a*PRECISION) % Math.floor(b*PRECISION))/PRECISION;
    }

    public double getRate()
    {
        return rate;
    }
}
