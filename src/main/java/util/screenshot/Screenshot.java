package util.screenshot;

import com.assertthat.selenium_shutterbug.utils.file.FileUtil;
import util.log.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Screenshot {
    private final int RED_RGB = (new Color(255, 0, 0)).getRGB();
    private final int width;
    private final int height;
    private final BufferedImage image;

    static final String FILE_FORMAT = "png";


    Screenshot(File imgPath) throws IOException {
        this(ImageIO.read(imgPath));
    }

    Screenshot(BufferedImage imgPath) {
        this.image = imgPath;
        this.width = image.getWidth(null);
        this.height = image.getHeight(null);
    }

    private BufferedImage getImage() {
        return image;
    }

    Diff getDiffFrom(File img) throws IOException {
        BufferedImage img1 = this.image;
        BufferedImage img2 = ImageIO.read(img);
        int w1 = img1.getWidth();
        int w2 = img2.getWidth();
        int h1 = img1.getHeight();
        int h2 = img2.getHeight();
//        Assert.assertEquals(w1, w2, "Images width are different.");
//        Assert.assertEquals(h1, h2, "Images height are different.");

        long diffPixelsCount = 0;
        long diff = 0;
        for (int j = 0; j < h1; j++) {
            for (int i = 0; i < w1; i++) {
                //Getting the RGB values of a pixel
                int pixel1 = img1.getRGB(i, j);
                Color color1 = new Color(pixel1, true);
                int r1 = color1.getRed();
                int g1 = color1.getGreen();
                int b1 = color1.getBlue();
                int pixel2;
                try {
                    pixel2 = img2.getRGB(i, j);
                }catch (ArrayIndexOutOfBoundsException e){
                    pixel2 = 0;
                }
                Color color2 = new Color(pixel2, true);
                int r2 = color2.getRed();
                int g2 = color2.getGreen();
                int b2 = color2.getBlue();
                //sum of differences of RGB values of the two images
                long data = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                if(data > 0) diffPixelsCount++;
                diff = diff + data;
            }
        }
        double avg = (double) diff / (w1 * h1 * 3);
        double percentage = (avg / 255) * 100;
        Log.debug("Difference: " + percentage + " %, pixels =" + diffPixelsCount + " of " + (w1 * h1));
        return new Diff(percentage, diffPixelsCount);

    }

    void createDiffImageFrom(BufferedImage image, String pathDiffImageFileName) {
        BufferedImage output = new BufferedImage(this.width, this.height, 1);

        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                int rgb1 = this.getImage().getRGB(x, y);
                int rgb2;
                try {
                    rgb2 = image.getRGB(x, y);
                }catch (ArrayIndexOutOfBoundsException e){
                    rgb2 = rgb1-1;
                }
                if (rgb1 != rgb2) {
                    output.setRGB(x, y, this.RED_RGB & rgb1);
                } else {
                    output.setRGB(x, y, rgb1);
                }
            }
        }

        FileUtil.writeImage(output, FILE_FORMAT, new File(pathDiffImageFileName));
    }

    static class Diff{

        private final Double percentage;
        private final long pixels;

        private Diff(double percentage, long pixels){
            this.percentage = percentage;
            this.pixels = pixels;
        }

        Double getPercentage() {
            return percentage;
        }

        long getPixels() {
            return pixels;
        }
    }
}
