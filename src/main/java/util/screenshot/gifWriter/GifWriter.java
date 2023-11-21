package util.screenshot.gifWriter;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GifWriter {

    public static void createGIF(File[] fileArrays, String gifPath) throws IOException {
        BufferedImage first = ImageIO.read(fileArrays[0]);
        try (ImageOutputStream output = new FileImageOutputStream(new File(gifPath))) {
            GifSequenceWriter writer = new GifSequenceWriter(output, first.getType(), 400, true);
            try {
                for (File image : fileArrays) {
                    BufferedImage next = ImageIO.read(image);
                    writer.writeToSequence(next);
                }
            } finally {
                writer.close();
            }
        }

    }
}
