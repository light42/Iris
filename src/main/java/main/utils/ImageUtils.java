package main.utils;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * Created by Magda on 29/05/2017.
 */
public class ImageUtils {
    //static class

    private static final Scalar GREEN = new Scalar(0, 255, 0);

    public static void showImage(String name, Mat src) {
        BufferedImage image = matToBufferedImage(src);
        showBufferedImage(image, name);
    }

    public static void showImage(String name, Mat src, int resize) {
        if (resize > 1)
            src = resizeImage(src, resize);
        showImage(name, src);
    }


    public static BufferedImage matToBufferedImage(Mat m) {
        if (m.type() == CvType.CV_32F)
            m.convertTo(m, MatConstants.TYPE);

        //https://stackoverflow.com/questions/15670933/opencv-java-load-image-to-gui
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }


        //https://stackoverflow.com/questions/26441072/finding-the-size-in-bytes-of-cvmat
        //int bufferSize = m.channels()*m.cols()*m.rows()*Double.BYTES;
        long bufferSize = m.step1(0) * m.rows();
        //TODO I really don't like this conversion
        byte[] b = new byte[(int) bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;

    }

    public static void showBufferedImage(Mat img, String name) {
        showBufferedImage(matToBufferedImage(img), name);
    }

    public static void showBufferedImage(BufferedImage img, String name) {
        JFrame frame = new JFrame(name);
        Dimension dimension = new Dimension(max(200 + 7 * name.length(), img.getWidth()), max(200, img.getHeight()));
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.getContentPane().setPreferredSize(dimension);
        frame.pack();
        frame.setVisible(true);
    }

    public static Mat bufferedImageToMat(BufferedImage image) {
        //http://enfanote.blogspot.com/2013/06/converting-java-bufferedimage-to-opencv.html

        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), MatConstants.TYPE);
        mat.put(0, 0, data);

        return mat;
    }

    public static Mat resizeImage(Mat src, int times) {
        Size size = new Size(src.width() * times, src.height() * times);
        Mat dst = new Mat(size, src.type());
        Imgproc.resize(src, dst, size);
        return dst;
    }

    //for all drawCircles functions: src is not modified!
    public static Mat drawCircles(Mat src, Mat circles) {
        return drawCircles(src, circles, GREEN);
    }

    public static Mat drawCircles(Mat src, Circle[] circles) {
        return drawCircles(src, circles, GREEN);
    }

    private static Mat convertMatToC3(Mat src) {
        Mat color_image = src;
        if (src.type() != CV_8UC3) {
            color_image = new Mat(src.size(), CV_8UC3);
            cvtColor(src, color_image, COLOR_GRAY2BGR);
        }
        return color_image;
    }

    public static Mat drawCircles(Mat src, Mat circles, Scalar color) {
        Mat color_image = convertMatToC3(src);   // first convert to CV_8UC3
        for (int i = 0; i < circles.cols(); i++) {
            Circle circle = new Circle(circles.get(0, i));
            color_image = drawCircle(color_image, circle, color);
        }
        return color_image;
    }

    public static Mat drawCircles(Mat src, Circle[] circles, Scalar color) {
        Mat color_image = convertMatToC3(src);   // first convert to CV_8UC3
        for (Circle circle : circles) {
            color_image = drawCircle(color_image, circle, color);
        }
        return color_image;
    }

    // Mat src must be CV_8UC3 type
    public static Mat drawCircle(Mat color_image, Circle circle, Scalar color) {
        Point center = circle.getCenter();
        double radius = circle.getRadius();
        Imgproc.circle(color_image, center, (int) Math.round(radius), color, 6, Imgproc.LINE_AA, 0);
        return color_image;
    }

    public static Mat drawPointsOnImage(Mat src, Point[] points) {
        Scalar red = new Scalar(0, 0, 255);
        int size = points.length;
        Circle[] circles = new Circle[size];
        Point point;
        for (int i = 0; i < size; i++) {
            point = points[i];
            circles[i] = new Circle(new double[]{point.x, point.y, 1.});
        }
        return drawCircles(src, circles, red);
    }

    public static double distance(Point a, Point b) {
        return sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public static void writeToFile(Mat image, Path directory, String filename) {
        assert image != null;
        Path path = Paths.get(directory.toString(), filename);
        imwrite(path.toString(), image);

    }

}
