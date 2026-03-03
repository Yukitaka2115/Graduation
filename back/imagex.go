package main

import (
	"fmt"
	"github.com/nfnt/resize"
	"image"
	"io"
	"math"
	"sort"
)

// Function used to convert RAW output from YOLOv8 to an array
// of detected objects. Each object contain the bounding box of
// this object, the type of object and the probability
// Returns array of detected objects in a format [[x1,y1,x2,y2,object_type,probability],..]
func processOutput(output []float32, imgWidth, imgHeight int64) [][]interface{} {
	var boxes [][]interface{}
	for index := 0; index < 8400; index++ {
		classId, prob := 0, float32(0.0)
		for col := 0; col < 3; col++ {
			if output[8400*(col+4)+index] > prob {
				prob = output[8400*(col+4)+index]
				classId = col
			}
		}
		if prob < 0.5 {
			continue
		}
		label := yoloClasses[classId]
		xc := output[index]
		yc := output[8400+index]
		w := output[2*8400+index]
		h := output[3*8400+index]
		x1 := (xc - w/2) / 640 * float32(imgWidth)
		y1 := (yc - h/2) / 640 * float32(imgHeight)
		x2 := (xc + w/2) / 640 * float32(imgWidth)
		y2 := (yc + h/2) / 640 * float32(imgHeight)
		boxes = append(boxes, []interface{}{float64(x1), float64(y1), float64(x2), float64(y2), label, prob})
	}

	sort.Slice(boxes, func(i, j int) bool {
		return boxes[i][5].(float32) < boxes[j][5].(float32)
	})
	var result [][]interface{}
	for len(boxes) > 0 {
		result = append(result, boxes[0])
		var tmp [][]interface{}
		for _, box := range boxes {
			if iou(boxes[0], box) < 0.7 {
				tmp = append(tmp, box)
			}
		}
		boxes = tmp
	}
	return result
}

// Function calculates "Intersection-over-union" coefficient for specified two boxes
// https://pyimagesearch.com/2016/11/07/intersection-over-union-iou-for-object-detection/.
// Returns Intersection over union ratio as a float number
func iou(box1, box2 []interface{}) float64 {
	return intersection(box1, box2) / union(box1, box2)
}

// Function calculates union area of two boxes
// Returns Area of the boxes union as a float number
func union(box1, box2 []interface{}) float64 {
	box1X1, box1Y1, box1X2, box1Y2 := box1[0].(float64), box1[1].(float64), box1[2].(float64), box1[3].(float64)
	box2X1, box2Y1, box2X2, box2Y2 := box2[0].(float64), box2[1].(float64), box2[2].(float64), box2[3].(float64)
	box1Area := (box1X2 - box1X1) * (box1Y2 - box1Y1)
	box2Area := (box2X2 - box2X1) * (box2Y2 - box2Y1)
	return box1Area + box2Area - intersection(box1, box2)
}

// Function calculates intersection area of two boxes
// Returns Area of intersection of the boxes as a float number
func intersection(box1, box2 []interface{}) float64 {
	box1X1, box1Y1, box1X2, box1Y2 := box1[0].(float64), box1[1].(float64), box1[2].(float64), box1[3].(float64)
	box2X1, box2Y1, box2X2, box2Y2 := box2[0].(float64), box2[1].(float64), box2[2].(float64), box2[3].(float64)
	x1 := math.Max(box1X1, box2X1)
	y1 := math.Max(box1Y1, box2Y1)
	x2 := math.Min(box1X2, box2X2)
	y2 := math.Min(box1Y2, box2Y2)
	return (x2 - x1) * (y2 - y1)
}

// Function used to convert input image to tensor,
// required as an input to YOLOv8 object detection
// network.
// Returns the input tensor, original image width and height
func prepareInput(buf io.Reader) ([]float32, int64, int64, error) {
	img, _, err := image.Decode(buf)
	if err != nil {
		fmt.Println(err)
		return nil, 0, 0, err
	}

	size := img.Bounds().Size()
	imgWidth, imgHeight := int64(size.X), int64(size.Y)
	img = resize.Resize(640, 640, img, resize.Lanczos3)
	var red []float32
	var green []float32
	var blue []float32
	for y := 0; y < 640; y++ {
		for x := 0; x < 640; x++ {
			r, g, b, _ := img.At(x, y).RGBA()
			red = append(red, float32(r/257)/255.0)
			green = append(green, float32(g/257)/255.0)
			blue = append(blue, float32(b/257)/255.0)
		}
	}
	input := append(red, green...)
	input = append(input, blue...)
	return input, imgWidth, imgHeight, nil
}
