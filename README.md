# csot-ohcrapi
Ocr wrapper to perform OCR both locally (using [Tesseract](https://github.com/rmtheis/tess-two)) or remotely (using [OCR Space API](https://ocr.space))

# Performing Ocr locally 
## 1. Setup
To setup the ocr task, we need 3 steps:

1. Get the tessdata of the language we want. Download the desired language from [__HERE__](https://github.com/tesseract-ocr/tessdata "GitHub TessData").

2. Place it under your assets Folder
The .traineddata file should be placed under your assets folder 
```
/assets
  /tessdata
    eng.traineddata
```
3. Call 
```java
OhCRapiLocal.init(Context ctx, String tessDirectoryPath, String trainedDataLanguage, TessBaseAPI.ProgressNotifier progressNotifier); 
```
## 2. Calling the AsyncTask
## 3. Handling Results

# Performing Ocr as a service
## 1. Setup
## 2. Calling the Service
## 3. Handling Results

Service response details can be found [Here](https://ocr.space/OCRAPI)

[![](https://jitpack.io/v/carlossotelo/ohcrapi.svg)](https://jitpack.io/#carlossotelo/ohcrapi)
