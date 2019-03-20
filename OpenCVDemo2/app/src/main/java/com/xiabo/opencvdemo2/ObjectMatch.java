package com.xiabo.opencvdemo2;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

class ObjectMatch {
    private static final String TAG = ObjectMatch.class.getSimpleName();

    Mat srcImage = null;
    Mat templateImage = null;
    Mat dstImage = null;

    // TM_SQDIFF = 0,
    // TM_SQDIFF_NORMED = 1,
    // TM_CCORR = 2,
    // TM_CCORR_NORMED = 3,
    // TM_CCOEFF = 4,
    // TM_CCOEFF_NORMED = 5
    int match_method = 0;

    ObjectMatch(final String src, final String tempate) {
        File srcFile = new File(src);
        File tempateFile = new File(tempate);
        if (srcFile.exists() && tempateFile.exists()) {
            srcImage = Imgcodecs.imread(src);
            templateImage = Imgcodecs.imread(tempate);
        }
        match_method = Imgproc.TM_CCORR_NORMED;
    }

    void setMethod(int method) {
        match_method = method;
    }

    public Point run(String outFile) {
        Log.i(TAG, "Running Template Matching with method " + match_method);

        // Create the result matrix
        int result_cols = srcImage.cols() - templateImage.cols() + 1;
        int result_rows = srcImage.rows() - templateImage.rows() + 1;
        dstImage = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // Do the Matching and Normalize
        Imgproc.matchTemplate(srcImage, templateImage, dstImage, match_method);
        Core.normalize(dstImage, dstImage, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(dstImage);

        Point matchLocation;
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLocation = mmr.minLoc;
        } else {
            matchLocation = mmr.maxLoc;
        }
        // Show me what you got
        Imgproc.rectangle(srcImage, matchLocation, new Point(matchLocation.x + templateImage.cols(),
                matchLocation.y + templateImage.rows()), new Scalar(0, 255, 0));
        // Save the visualized detection.
        if (outFile != null) {
            Log.i(TAG, "Writing "+ outFile);
            Imgcodecs.imwrite(outFile, srcImage);
        }

        Log.i(TAG, "matchLocation " + matchLocation);
        return matchLocation;
    }

    Point match(String outFile) {
        if (srcImage == null || templateImage == null || srcImage.empty() || templateImage.empty()) {
            Log.e(TAG, "image load fail");
            return null;
        }
        Log.i(TAG, "image load success...");
        return run(outFile);
    }
}