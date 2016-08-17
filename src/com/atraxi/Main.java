package com.atraxi;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        for(String arg : args)
        {
            System.out.println(System.lineSeparator() + "File: " + arg);
            StringBuilder builder = new StringBuilder();
            try(BufferedReader reader = new BufferedReader(new FileReader(new File(arg))))
            {
                String data = reader.readLine();
                while(data != null)
                {
                    builder.append(data);
                    data = reader.readLine();
                }
            }
            JSONObject json = new JSONObject(builder.toString());
            int width = json.getInt("width");
            int height = json.getInt("height");
            System.out.println("Width: " + width + ", Height: " + height);

//            json.keys().forEachRemaining(key -> System.out.println("key:" + key));

            JSONArray coloursJSON = json.getJSONArray("colors");
            System.out.println("Colours length: " + coloursJSON.length());

            Color[] colors = new Color[coloursJSON.length()];
            for(int i = 0; i < colors.length; i++)
            {
                JSONArray colourRGB_JSON = coloursJSON.getJSONArray(i);
                colors[i] = new Color(colourRGB_JSON.getInt(0), colourRGB_JSON.getInt(1), colourRGB_JSON.getInt(2));
            }

            JSONArray cyclesJSON = json.getJSONArray("cycles");
            System.out.println("Cycles length: " + cyclesJSON.length());
            Cycle[] cycles = new Cycle[cyclesJSON.length()];
            long lcm = 1;
            for(int i = 0; i < cycles.length; i++)
            {
                JSONObject cycleJSON = cyclesJSON.getJSONObject(i);
                cycles[i] = new Cycle(cycleJSON.getInt("reverse"), cycleJSON.getInt("rate"), cycleJSON.getInt("low"), cycleJSON.getInt("high"));
                if(cycles[i].getRate() > 0.000001)//rate!=0, rate is known to be positive
                {
                    lcm = MathUtil.lcm(lcm, (long) cycles[i].getRate());
                }
            }
            System.out.println("LCM: " + lcm);

            // create a new BufferedOutputStream with the last argument
            ImageOutputStream output = new FileImageOutputStream(new File(arg + "3.gif"));
            // create a gif sequence with the type of the first image, 1 second
            // between frames, which loops continuously
            GifSequenceWriter writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, 50, false);

            JSONArray pixelsJSON = json.getJSONArray("pixels");
            System.out.println("Pixels length: " + pixelsJSON.length());

            Color[] colorsDirty = colors.clone();
            long dirtyCount = 0;
            long endPoint = 30000;//lcm;
            for(long i = 0; i < endPoint; i+=1)
            {
                if(i % 100 == 0) { System.out.println("progress:"+((i*100)/endPoint)+"%"); }
                boolean dirty = false;
                for(Cycle cycle : cycles)
                {
                    dirty = dirty || cycle.cycle(colorsDirty, i);
                }
//                writer.writeToSequence(render(pixelsJSON, width, height, colorsDirty));
                if(dirty)
                {
                    writer.writeToSequence(render(pixelsJSON, width, height, colorsDirty));
                    dirtyCount++;
                }
            }
            System.out.println("dirtyCount: " + dirtyCount);
            writer.close();
            output.close();

//            BufferedImage imageClean = render(json.getJSONArray("pixels"), width, height, colors);
//            BufferedImage imageDirty = render(json.getJSONArray("pixels"), width, height, colorsDirty);
//            System.out.println("Are images equal?: " + compareImages(imageClean, imageDirty));
//            ImageIO.write(imageClean, "png", new File(arg + "CLEAN.png"));
//            ImageIO.write(imageDirty, "png", new File(arg + "DIRTY.png"));
        }
    }

    private static BufferedImage render(JSONArray pixelsJSON, int width, int height, Color[] colors)
    {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < height; y += 1)
        {
            for(int x = 0; x < width; x += 1)
            {
                bufferedImage.setRGB(x, y, colors[pixelsJSON.getInt(((width * y) + x))].getRGB());
            }
        }
        return bufferedImage;
    }

    /**
     * Compares two images pixel by pixel.
     *
     * @param imgA the first image.
     * @param imgB the second image.
     * @return whether the images are both the same or not.
     */
    public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() == imgB.getWidth() && imgA.getHeight() == imgB.getHeight())
        {
            int width = imgA.getWidth();
            int height = imgA.getHeight();

            // Loop over every pixel.
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Compare the pixels for equality.
                    if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                        return false;
                    }
                }
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    /**
     public GifSequenceWriter(
     BufferedOutputStream outputStream,
     int imageType,
     int timeBetweenFramesMS,
     boolean loopContinuously)
     */
    //    public static void main(String[] args) throws Exception {
//        if (args.length > 1) {
//            // grab the output image type from the first image in the sequence
//            BufferedImage firstImage = ImageIO.read(new File(args[0]));
//
//            // create a new BufferedOutputStream with the last argument
//            ImageOutputStream output =
//                    new FileImageOutputStream(new File(args[args.length - 1]));
//
//            // create a gif sequence with the type of the first image, 1 second
//            // between frames, which loops continuously
//            GifSequenceWriter writer =
//                    new GifSequenceWriter(output, firstImage.getType(), 1, false);
//
//            // write out the first image to our sequence...
//            writer.writeToSequence(firstImage);
//            for(int i=1; i<args.length-1; i++) {
//                BufferedImage nextImage = ImageIO.read(new File(args[i]));
//                writer.writeToSequence(nextImage);
//            }
//
//            writer.close();
//            output.close();
//        } else {
//            System.out.println(
//                    "Usage: java GifSequenceWriter [list of gif files] [output file]");
//        }
//    }
}
