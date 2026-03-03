package main

import (
	"encoding/json"
	"fmt"
	"github.com/gin-gonic/gin"
	"io"
	"net/http"
	//ws "github.com/gorilla/websocket"
	_ "image/gif"
	_ "image/jpeg"
	_ "image/png"
)

//var upGrader = ws.Upgrader{
//	ReadBufferSize:  65536,
//	WriteBufferSize: 65536,
//}

// Main function that defines
// a web service endpoints a starts
// the web service

func main() {
	router := gin.Default()
	//router.GET("", index)
	router.POST("/detect", detector)
	err := router.Run(":8088")
	if err != nil {
		return
	}
} //mobile

//func main() {
//	server := http.Server{
//		Addr: "0.0.0.0:8088",
//	}
//	http.HandleFunc("/", index)
//	http.HandleFunc("/detect", detect)
//	err := server.ListenAndServe()
//	if err != nil {
//		return
//	}
//}//web

// Site main page handler function.
// Returns Content of index.html file

//func index(w http.ResponseWriter, _ *http.Request) {
//	file, _ := os.Open("index.html")
//	buf, _ := io.ReadAll(file)
//	_, err := w.Write(buf)
//	if err != nil {
//		return
//	}
//}

// Handler of /detect POST endpoint
// Receives uploaded file with a name "image_file", passes it
// through YOLOv8 object detection network and returns and array
// of bounding boxes.
// Returns a JSON array of objects bounding boxes in format [[x1,y1,x2,y2,object_type,probability],..]

//func detect(w http.ResponseWriter, r *http.Request) {
//	err := r.ParseMultipartForm(0)
//	if err != nil {
//		return
//	}
//	file, _, err := r.FormFile("image_file")
//	if err != nil {
//		fmt.Println("upload failed")
//	}
//	boxes, err := detectObjectsOnImage(file)
//	fmt.Println(boxes)
//	if err != nil {
//		fmt.Println(err.Error())
//	}
//	buf, _ := json.Marshal(&boxes)
//	_, err = w.Write(buf)
//	fmt.Printf(string(buf))
//	if err != nil {
//		return
//	}
//}//web

func detector(ctx *gin.Context) {
	file, _, err := ctx.Request.FormFile("image")
	if err != nil {
		fmt.Println("No image uploaded")
		ctx.JSON(http.StatusBadRequest, gin.H{"error": "No image uploaded"})
		return
	}
	boxes, err2 := detectObjectsOnImage(file)
	if err2 != nil {
		fmt.Println("detect error")
		ctx.JSON(http.StatusInternalServerError, gin.H{"error": "Detection error"})
		return
	}

	// Modify the structure of boxes to the desired format
	var formattedBoxes []map[string]interface{}
	for _, box := range boxes {
		formattedBox := map[string]interface{}{
			"x1":    box[0],
			"y1":    box[1],
			"x2":    box[2],
			"y2":    box[3],
			"class": box[4],
			"prob":  box[5],
		}
		formattedBoxes = append(formattedBoxes, formattedBox)
	}

	buf, _ := json.Marshal(&formattedBoxes)
	_, err3 := ctx.Writer.Write(buf)
	fmt.Println(formattedBoxes)
	if err3 != nil {
		fmt.Println("Write error")
		ctx.JSON(http.StatusInternalServerError, gin.H{"error": "Write error"})
		return
	}
}

// Function receives an image,
// passes it through YOLOv8 neural network
// and returns an array of detected objects
// and their bounding boxes
// Returns Array of bounding boxes in format [[x1,y1,x2,y2,object_type,probability],..]

//func detectObjectsOnImage(data []byte) ([][]interface{}, error) {
//	buf := bytes.NewReader(data)
//	input, imgWidth, imgHeight := prepareInput(buf)
//	output, err := runModel(input)
//	if err != nil {
//		return nil, err
//	}
//	res := processOutput(output, imgWidth, imgHeight)
//	fmt.Println("Detection results:")
//	for _, obj := range res {
//		fmt.Println(obj)
//	}
//	return res, nil
//}

func detectObjectsOnImage(buf io.Reader) ([][]interface{}, error) {
	input, imgWidth, imgHeight, err := prepareInput(buf)
	if err != nil {
		fmt.Println("input failed")
		return nil, err
	}
	output, err := runModel(input)
	if err != nil {
		return nil, err
	}

	data := processOutput(output, imgWidth, imgHeight)
	//fmt.Println("Detection results:")
	//for _, obj := range data {
	//	fmt.Println(obj)
	//}
	return data, nil
}

// Function used to pass provided input tensor to
// YOLOv8 neural network and return result
// Returns raw output of YOLOv8 network as a single dimension
// array
func runModel(input []float32) ([]float32, error) {

	var err error

	if Yolo8Model.Session == nil {
		Yolo8Model, err = InitYolo8Session(input)
		if err != nil {
			return nil, err
		}
	}

	return runInference(Yolo8Model, input)

}
