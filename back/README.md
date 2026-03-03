## 鸣谢与参考 (Acknowledgements)

本项目后端推理逻辑参考并集成了以下开源项目：

1.  **推理框架集成**: [yolov8_onnx_go](https://github.com/AndreyGermanov/yolov8_onnx_go)
    * **作者**: Andrey Germanov
    * **贡献**: 提供了在 Go 语言环境中使用 `onnxruntime` 进行 YOLOv8 目标检测的工程实践参考。

2.  **核心模型算法**: [Ultralytics YOLOv8](https://github.com/ultralytics/ultralytics)
    * **协议**: AGPL-3.0 License
    * **说明**: 本项目所使用的权重文件及其原始算法逻辑均源自 Ultralytics 官方。

---
*注：本项目仅将上述技术应用于 [你的毕业设计题目] 场景中的 [具体功能，如：零件检测/人脸识别]，并完成了相应的接口封装与业务逻辑实现。*