#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

using namespace std;
using namespace cv;

#pragma warning(disable:4996)

// ����ȫ�ֱ���
Mat srcImage, templateImage, dstImage;
const int trackbar_method_maxValue = 5;
int trackbar_method;

// ����ص�����
void method(int, void*);

int main()
{
	srcImage = imread("1.png");
	templateImage = imread("_1_1.png");

	// �ж��ļ��Ƿ���سɹ�
	if (srcImage.empty() || templateImage.empty()) 
	{
		cout << "ͼ�����ʧ��!" << endl;
		return -1;
	}
	else
		cout << "ͼ����سɹ�..." << endl << endl;

	namedWindow("ԭͼ��", WINDOW_AUTOSIZE);
	namedWindow("ģ��ͼ��", WINDOW_AUTOSIZE);
	imshow("ԭͼ��", srcImage);
	imshow("ģ��ͼ��", templateImage);

	// ����켣������
	trackbar_method = 1;
	char mathodName[50];
	namedWindow("ƥ��ͼ��", WINDOW_AUTOSIZE);
	sprintf(mathodName, "ƥ�䷽ʽ%d\n 0:SQDIFF\n 1:SQDIFF_NORMED\n 2:TM_CCORR\n 3:TM_CCORR_NORMEND\n 4:TM_COEFF\n 5:TM_COEFF_NORMED", trackbar_method_maxValue);
	createTrackbar(mathodName, "ƥ��ͼ��", &trackbar_method, trackbar_method_maxValue, method);
	method(trackbar_method, 0);

	waitKey(0);

	return 0;
}

void method(int, void*)
{
	Mat display;
	srcImage.copyTo(display);
	// �����������
	int dstImage_rows = srcImage.rows - templateImage.rows + 1;
	int dstImage_cols = srcImage.cols - templateImage.cols + 1;
	dstImage.create(dstImage_rows, dstImage_cols, srcImage.type());

	matchTemplate(srcImage, templateImage, dstImage, trackbar_method);   //ģ��ƥ��
	normalize(dstImage, dstImage, 0, 1, NORM_MINMAX);       //��һ������

	// ͨ��minMaxLoc��λ���ƥ��λ��
	double minValue, maxValue;
	Point minLocation, maxLocation;
	Point matchLocation;
	minMaxLoc(dstImage, &minValue, &maxValue, &minLocation, &maxLocation, Mat());

	// ���ڷ���SQDIFF��SQDIFF_NORMED���ַ���������ԽС��ֵ�����Ÿ��ߵ�ƥ����
	// ������ķ���������ֵԽ��ƥ��Ч��Խ��
	if (trackbar_method == CV_TM_SQDIFF || trackbar_method == CV_TM_SQDIFF_NORMED)
	{
		matchLocation = minLocation;
	}
	else
	{
		matchLocation = maxLocation;
	}
	rectangle(display, matchLocation, Point(matchLocation.x + templateImage.cols, matchLocation.y + templateImage.rows), Scalar(0, 0, 255));

	imshow("ƥ��ͼ��", display);
}