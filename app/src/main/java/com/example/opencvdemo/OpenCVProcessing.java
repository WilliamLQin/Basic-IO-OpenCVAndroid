package com.example.opencvdemo;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Size;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by William on 10/14/2017.
 */


public class OpenCVProcessing {

    private static Bitmap outputBitmap;
    public static Bitmap getOutputBitmap() {return outputBitmap;}
    private static void setOutputBitmap(Bitmap bitmap) {

        outputBitmap = bitmap;

    }

    public static void inputImage(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();

        // Convert image into usable HSV Mat
        Mat yuvMat = imageToMat(image);

        Mat bgrMat = new Mat(height, width, CvType.CV_8UC4);
        Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);

        setOutputBitmap(bgrMatToRgbBitmap(bgrMat));

//        Mat hsvMat = new Mat();
//        Imgproc.cvtColor(bgrMat, hsvMat, Imgproc.COLOR_RGB2HSV);

//        // Split HSV into respective channels
//        List<Mat> hsvChannels = new ArrayList<>();
//
//        Core.split(hsvMat, hsvChannels);
//
//        // Convert H channel into rgba
//        Mat hMat = new Mat();
//
//        Imgproc.cvtColor(hsvChannels.get(0), hMat, Imgproc.COLOR_GRAY2RGBA);
//
//        // Assign H mat to display
//        setOutputBitmap(matToBitmap(hMat, width, height));

//        // Canny edge detection algorithm
//        int threshold1 = 100;
//        int threshold2 = 300;
//
//        Mat cannyMat = new Mat();
//        Imgproc.Canny(hsvMat, cannyMat, threshold1, threshold2);
//
//        // Convert canny mat into rgba mat
//        Mat rgbaMat = new Mat();
//        Imgproc.cvtColor(cannyMat, rgbaMat, Imgproc.COLOR_GRAY2RGBA);

        // Assign edges mat to display
//        setOutputBitmap(matToBitmap(rgbaMat, width, height));

    }

    private static Bitmap bgrMatToRgbBitmap (Mat bgrMat) {
        Mat rgbaMatOut = new Mat();
        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA);
        final Bitmap bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgbaMatOut, bitmap);

        return bitmap;
    }

    private static Bitmap matToBitmap (Mat mat, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }


    private static Mat imageToMat(Image image) {
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int width = image.getWidth();
        int height = image.getHeight();
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
                if (pixelStride == bytesPerPixel) {
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);

                    // Advance buffer the remainder of the row stride, unless on the last row.
                    // Otherwise, this will throw an IllegalArgumentException because the buffer
                    // doesn't include the last padding.
                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {

                    // On the last row only read the width of the image minus the pixel stride
                    // plus one. Otherwise, this will throw a BufferUnderflowException because the
                    // buffer doesn't include the last padding.
                    if (h - row == 1) {
                        buffer.get(rowData, 0, width - pixelStride + 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];
                    }
                }
            }
        }

        // Finally, create the Mat.
        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);
        mat.put(0, 0, data);

        return mat;
    }

}
