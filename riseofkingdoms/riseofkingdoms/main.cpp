#include <iostream>
#include <map>
#include <opencv2/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

#include "ObjectMatch.h"

using namespace std;
using namespace cv;

#pragma warning(disable:4996)
typedef std::pair<std::string, std::string> TStrStrPair;

int main() {
	map<string, string> matchs;
	matchs.insert(TStrStrPair("img/_4_2.png", "img/1.png"));
	//matchs.insert(TStrStrPair("img/_1_1.png", "img/1.png"));
	//matchs.insert(TStrStrPair("img/_2_1.png", "img/2.png"));
	//matchs.insert(TStrStrPair("img/_3_1.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_2.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_3.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_4.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_5.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_6.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_7.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_3_8.png", "img/3.png"));
	//matchs.insert(TStrStrPair("img/_4_1.png", "img/4.png"));
	//matchs.insert(TStrStrPair("img/_4_2.png", "img/4.png"));
	//matchs.insert(TStrStrPair("img/_5_1.png", "img/5.png"));
	//matchs.insert(TStrStrPair("img/_5_2.png", "img/5.png"));
	//matchs.insert(TStrStrPair("img/_6_1.png", "img/6.png"));
	//matchs.insert(TStrStrPair("img/_7_1.png", "img/7.png"));
	//matchs.insert(TStrStrPair("img/_8_1.png", "img/8.png"));
	//matchs.insert(TStrStrPair("img/_9_1.png", "img/9.png"));

	std::map<string, string>::iterator it = matchs.begin();
	int i = 1;
	for (it = matchs.begin(); it != matchs.end(); ++it) {
		cout << "match " << it->first << " " + it->second << endl;
		ObjectMatch* objectMatch = new ObjectMatch(it->second, it->first);
		Mat match = objectMatch->match();

		char namewind[128];
		sprintf(namewind, "match %d", i++);
		namedWindow(namewind, WINDOW_AUTOSIZE);
		imshow(namewind, match);
	}
	waitKey(0);

	return 0;
}