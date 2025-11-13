#include <opencv2/opencv.hpp>
#include "opencv_processor.h"

using namespace cv;

Mat processEdgeDetection(const Mat& input) {
    Mat gray, edges;
    cvtColor(input, gray, COLOR_RGBA2GRAY);
    Canny(gray, edges, 50, 150);

    Mat output;
    cvtColor(edges, output, COLOR_GRAY2RGBA);
    return output;
}