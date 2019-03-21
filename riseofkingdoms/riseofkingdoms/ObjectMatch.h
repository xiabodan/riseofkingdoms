#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

using namespace std;
using namespace cv;

#pragma warning(disable:4996)

class ObjectMatch {
	// 定义全局变量
	Mat srcImage;
	Mat templateImage;
	Mat dstImage;

	static const int trackbar_method_maxValue = 5;

	//CV_TM_SQDIFF = 0,
	//CV_TM_SQDIFF_NORMED = 1,
	//CV_TM_CCORR = 2,
	//CV_TM_CCORR_NORMED = 3,
	//CV_TM_CCOEFF = 4,
	//CV_TM_CCOEFF_NORMED = 5
	int trackbar_method;

public:

	ObjectMatch(const String& src, const String& tempate) {
		srcImage = imread(src);
		templateImage = imread(tempate);
		trackbar_method = CV_TM_CCOEFF_NORMED;
	}

	void setMethod(int method) {
		trackbar_method = method;
	}

	Mat match() {
		// 判断文件是否加载成功
		if (srcImage.empty() || templateImage.empty()) {
			cout << "图像加载失败!" << endl;
			return Mat();
		} else { 
			cout << "图像加载成功..." << endl;
		}

		pyrDown(templateImage, templateImage, Size(templateImage.cols * 0.5, templateImage.rows * 0.5));
		pyrDown(srcImage, srcImage, Size(srcImage.cols * 0.5, srcImage.rows * 0.5));
		// namedWindow("原图像", WINDOW_AUTOSIZE);
		// namedWindow("模板图像", WINDOW_AUTOSIZE);
		// imshow("原图像", srcImage);
		// imshow("模板图像", templateImage);

		// 定义轨迹条参数
		char mathodName[50];
		// namedWindow("匹配图像", WINDOW_AUTOSIZE);
		// sprintf(mathodName, "匹配方式%d\n 0:SQDIFF\n 1:SQDIFF_NORMED\n 2:TM_CCORR\n 3:TM_CCORR_NORMEND\n 4:TM_COEFF\n 5:TM_COEFF_NORMED", trackbar_method_maxValue);
		// createTrackbar(mathodName, "匹配图像", &trackbar_method, trackbar_method_maxValue, method);
		return match(trackbar_method, 0);
	}

private:

	Mat match(int, void*) {
		cout << "trackbar_method " << trackbar_method  << " trackbar_method_maxValue " << trackbar_method_maxValue  << endl;
		Mat display;
		srcImage.copyTo(display);
		// 创建输出矩阵
		int dstImage_rows = srcImage.rows - templateImage.rows + 1;
		int dstImage_cols = srcImage.cols - templateImage.cols + 1;
		dstImage.create(dstImage_rows, dstImage_cols, srcImage.type());

		matchTemplate(srcImage, templateImage, dstImage, trackbar_method);   // 模板匹配
		normalize(dstImage, dstImage, 0, 1, NORM_MINMAX);       // 归一化处理

		// 通过minMaxLoc定位最佳匹配位置
		double minValue, maxValue;
		Point minLocation, maxLocation;
		Point matchLocation;
		minMaxLoc(dstImage, &minValue, &maxValue, &minLocation, &maxLocation, Mat());

		// 对于方法SQDIFF和SQDIFF_NORMED两种方法来讲，越小的值就有着更高的匹配结果
		// 而其余的方法则是数值越大匹配效果越好
		if (trackbar_method == CV_TM_SQDIFF || trackbar_method == CV_TM_SQDIFF_NORMED) {
			matchLocation = minLocation;
		} else {
			matchLocation = maxLocation;
		}
		cout << "matchLocation (" << matchLocation.x  << ", " << matchLocation.y << ")" << endl;

		rectangle(display, matchLocation, Point(matchLocation.x + templateImage.cols, matchLocation.y + templateImage.rows), Scalar(0, 0, 255));
		// imshow("匹配图像", display);
		return display;
	}
};