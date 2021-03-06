package org.opencv.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;


import org.opencv.android.Utils;
import org.opencv.core.Algorithm;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.opencv.features2d.AKAZE;

import org.opencv.features2d.DescriptorMatcher;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.highgui.Highgui.CV_LOAD_IMAGE_GRAYSCALE;


/**
 * Created by kqw on 2016/9/9.
 * FaceUtil
 */
public final class FaceUtil {

    private static final String TAG = "FaceUtil";

    private FaceUtil() {
    }

    /**
     * 特征保存
     *
     * @param context  Context
     * @param image    Mat
     * @param rect     人脸信息
     * @param fileName 文件名字
     * @return 保存是否成功
     */
    public static boolean saveImage(Context context, Mat image, Rect rect, String fileName) {
        // 原图置灰
        Mat grayMat = new Mat();
        Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY);
        // 把检测到的人脸重新定义大小后保存成文件
        Mat sub = grayMat.submat(rect);

        Mat mat = new Mat();
        Size size = new Size(300, 300);
        Imgproc.resize(sub, mat, size);
        String filePath = getFilePath(context, fileName);
        return Imgcodecs.imwrite(filePath, mat);
    }

    public static boolean saveImagev2(Context context, Mat image, String fileName) {
       /* Mat grayMat = new Mat();
        Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY);*/
        String filePath = getFilePath(context, fileName);
        return Imgcodecs.imwrite(filePath, image);

    }

    /**
     * 删除特征
     *
     * @param context  Context
     * @param fileName 特征文件
     * @return 是否删除成功
     */
    public static boolean deleteImage(Context context, String fileName) {
        // 文件名不能为空
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        // 文件路径不能为空
        String path = getFilePath(context, fileName);
        if (path != null) {
            File file = new File(path);
            return file.exists() && file.delete();
        } else {
            return false;
        }
    }

    /**
     * 提取特征
     *
     * @param context  Context
     * @param fileName 文件名
     * @return 特征图片
     */
    public static Bitmap getImage(Context context, String fileName) {
        String filePath = getFilePath(context, fileName);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {
            return BitmapFactory.decodeFile(filePath);
        }
    }

    public static byte[] readStream(Context context, String fileName) {
        try {
            String filePath = getFilePath(context, fileName);
            if (TextUtils.isEmpty(filePath)) {
                return null;
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bytes = baos.toByteArray();
                return bytes;
            }
        }catch (Exception e){

        }
        return null;
    }

   /* *//**
     * 特征对比 直方图 javacv 引用注释
     *
     * @param context   Context
     * @param fileName1 人脸特征
     * @param fileName2 人脸特征
     * @return 相似度
     *//*
    public static double compare(Context context, String fileName1, String fileName2) {
        try {
            String pathFile1 = getFilePath(context, fileName1);
            String pathFile2 = getFilePath(context, fileName2);
            IplImage image1 = cvLoadImage(pathFile1, CV_LOAD_IMAGE_GRAYSCALE);
            IplImage image2 = cvLoadImage(pathFile2, CV_LOAD_IMAGE_GRAYSCALE);
            if (null == image1 || null == image2) {
                return -1;
            }

            int l_bins = 256;
            int hist_size[] = {l_bins};
            float v_ranges[] = {0, 255};
            float ranges[][] = {v_ranges};

            IplImage imageArr1[] = {image1};
            IplImage imageArr2[] = {image2};
            CvHistogram Histogram1 = CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
            CvHistogram Histogram2 = CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
            cvCalcHist(imageArr1, Histogram1, 0, null);
            cvCalcHist(imageArr2, Histogram2, 0, null);
            cvNormalizeHist(Histogram1, 100.0);
            cvNormalizeHist(Histogram2, 100.0);
            double c1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL) * 100;
            double c2 = cvCompareHist(Histogram1, Histogram2, CV_COMP_INTERSECT);
            return (c1 + c2) / 2;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }*/


    /**
     * 获取人脸特征路径
     *
     * @param fileName 人脸特征的图片的名字
     * @return 路径
     */
    public static String getFilePath(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        // 内存路径
        return context.getApplicationContext().getFilesDir().getPath() + fileName + ".jpg";
        // 内存卡路径 需要SD卡读取权限
        // return Environment.getExternalStorageDirectory() + "/FaceDetect/" + fileName + ".jpg";
    }


    /**
     * BRISK  慢
     * ORB
     *
     * @return
     */
    public static float match(Mat srcA, Mat srcB) {
        //寻找关键点
        AKAZE fd = AKAZE.create();
        MatOfKeyPoint keyPointA = new MatOfKeyPoint();
        MatOfKeyPoint keyPointB = new MatOfKeyPoint();
        Mat descriptionA = new Mat();
        Mat descriptionB = new Mat();
        fd.detectAndCompute(srcA, descriptionA, keyPointA, descriptionA);
        fd.detectAndCompute(srcB, descriptionB, keyPointB, descriptionB);
        //匹配两张图片关键点的特征
        List<MatOfDMatch> matches = new ArrayList<>();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        matcher.knnMatch(descriptionA, descriptionB, matches, 2);
        int total = Math.min(keyPointA.rows(), keyPointB.rows());
        int matchedNum = 0;
        for (MatOfDMatch match : matches) {
            if (match.rows() != 0) {
                List<DMatch> dMatches = match.toList();
                if (dMatches != null && dMatches.size() > 0) {
                    float dist1 = dMatches.get(0).distance;
                    float dist2 = dMatches.get(1).distance;
                    if (dist1 < 0.8 * dist2) {
                        matchedNum++;
                    }
                }
            }
        }
        //Log.d("mine", "matchedNum" + keyPointA.rows() + "_" + keyPointB.rows() + "_" + matchedNum);
        float ratio = matchedNum * 1.0f / total;
        return ratio;
    }

}
