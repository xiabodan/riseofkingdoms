#include <iostream>
#include <map>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>
#include <opencv2/features2d/features2d.hpp>

#include "ObjectMatch.h"

using namespace std;
using namespace cv;

#pragma warning(disable:4996)
typedef std::pair<std::string, std::string> TStrStrPair;

void templateMatch();
void featureMatch(String srcImg, String templateImg);

int main() {
	// templateMatch();
	featureMatch("img/1.png", "img/_1_1.png");

	waitKey(0);
	return 0;
}

void templateMatch() {
	map<string, string> matchs;
	matchs.insert(TStrStrPair("img/_1_1.png", "img/1.png"));
	matchs.insert(TStrStrPair("img/_2_1.png", "img/2.png"));
	matchs.insert(TStrStrPair("img/_3_1.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_2.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_3.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_4.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_5.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_6.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_7.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_3_8.png", "img/3.png"));
	matchs.insert(TStrStrPair("img/_4_1.png", "img/4.png"));
	matchs.insert(TStrStrPair("img/_4_2.png", "img/4.png"));
	matchs.insert(TStrStrPair("img/_5_1.png", "img/5.png"));
	matchs.insert(TStrStrPair("img/_5_2.png", "img/5.png"));
	matchs.insert(TStrStrPair("img/_6_1.png", "img/6.png"));
	matchs.insert(TStrStrPair("img/_7_1.png", "img/7.png"));
	matchs.insert(TStrStrPair("img/_8_1.png", "img/8.png"));
	matchs.insert(TStrStrPair("img/_9_1.png", "img/9.png"));

	std::map<string, string>::iterator it = matchs.begin();
	int i = 1;
	for (it = matchs.begin(); it != matchs.end(); ++it) {
		ObjectMatch* objectMatch = new ObjectMatch(it->second, it->first);
		time_t start = clock();
		Mat match = objectMatch->match();
		time_t end = clock();
		double cost = double(end - start) * 1000 / CLOCKS_PER_SEC;
		cout << "matching " << it->first << " " + it->second << " cost " << cost << endl << endl;

		//char namewind[128];
		//sprintf(namewind, "match %d", i++);
		//namedWindow(namewind, WINDOW_AUTOSIZE);
		//imshow(namewind, match);
	}
}

// https://blog.csdn.net/yang_xian521/article/details/6901762
void featureMatch(String srcImg, String templateImg) {
	Mat img_1 = imread(srcImg, IMREAD_GRAYSCALE);
	Mat img_2 = imread(templateImg, IMREAD_GRAYSCALE);

	if (!img_1.data || !img_2.data) {
		return;
	}

	//-- Step 1: Detect the keypoints using SURF Detector
	int minHessian = 400;

	Ptr<BRISK> detector = BRISK::create();

	std::vector<KeyPoint> keypoints_1, keypoints_2;

	detector->detect(img_1, keypoints_1);
	detector->detect(img_2, keypoints_2);

	//-- Step 2: Calculate descriptors (feature vectors)
	Ptr<BRISK> extractor = BRISK::create();

	Mat descriptors_1, descriptors_2;

	extractor->compute(img_1, keypoints_1, descriptors_1);
	extractor->compute(img_2, keypoints_2, descriptors_2);

	//-- Step 3: Matching descriptor vectors with a brute force matcher
	BFMatcher matcher(NORM_HAMMING);
	std::vector< DMatch > matches;
	matcher.match(descriptors_1, descriptors_2, matches);

	double max_dist = 0; double min_dist = 100;

	//-- Quick calculation of max and min distances between keypoints
	for (int i = 0; i < descriptors_1.rows; i++)
	{
		double dist = matches[i].distance;
		if (dist < min_dist) min_dist = dist;
		if (dist > max_dist) max_dist = dist;
	}

	printf("-- Max dist : %f \n", max_dist);
	printf("-- Min dist : %f \n", min_dist);

	//-- Draw only "good" matches (i.e. whose distance is less than 2*min_dist )
	//-- PS.- radiusMatch can also be used here.
	std::vector< DMatch > good_matches;

	for (int i = 0; i < descriptors_1.rows; i++)
	{
		if (matches[i].distance < 2 * min_dist)
		{
			good_matches.push_back(matches[i]);
		}
	}

	//-- Draw matches
	Mat img_matches;
	drawMatches(img_1, keypoints_1, img_2, keypoints_2, matches, img_matches);

	//-- Show detected matches
	imshow("Matches", img_matches);

	//-- Localize the object from img_1 in img_2 
	std::vector<Point2f> obj;
	std::vector<Point2f> scene;

	for (int i = 0; i < good_matches.size(); i++)
	{
		//-- Get the keypoints from the good matches
		obj.push_back(keypoints_1[good_matches[i].queryIdx].pt);
		scene.push_back(keypoints_2[good_matches[i].trainIdx].pt);
	}

	Mat H = findHomography(obj, scene, RANSAC);

	//-- Get the corners from the image_1 ( the object to be "detected" )
	Point2f obj_corners[4] = { cvPoint(0,0), cvPoint(img_1.cols, 0), cvPoint(img_1.cols, img_1.rows), cvPoint(0, img_1.rows) };
	Point scene_corners[4];

	//-- Map these corners in the scene ( image_2)
	for (int i = 0; i < 4; i++)
	{
		double x = obj_corners[i].x;
		double y = obj_corners[i].y;

		double Z = 1. / (H.at<double>(2, 0)*x + H.at<double>(2, 1)*y + H.at<double>(2, 2));
		double X = (H.at<double>(0, 0)*x + H.at<double>(0, 1)*y + H.at<double>(0, 2))*Z;
		double Y = (H.at<double>(1, 0)*x + H.at<double>(1, 1)*y + H.at<double>(1, 2))*Z;
		scene_corners[i] = cvPoint(cvRound(X) + img_1.cols, cvRound(Y));
	}

	//-- Draw lines between the corners (the mapped object in the scene - image_2 )
	line(img_matches, scene_corners[0], scene_corners[1], Scalar(0, 255, 0), 2);
	line(img_matches, scene_corners[1], scene_corners[2], Scalar(0, 255, 0), 2);
	line(img_matches, scene_corners[2], scene_corners[3], Scalar(0, 255, 0), 2);
	line(img_matches, scene_corners[3], scene_corners[0], Scalar(0, 255, 0), 2);

	//-- Show detected matches
	imshow("Good Matches & Object detection", img_matches);
}