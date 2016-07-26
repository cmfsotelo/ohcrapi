# csot-ohcrapi
Ocr wrapper to perform OCR both locally (using [Tesseract](https://github.com/rmtheis/tess-two)) or remotely (using [OCR Space API](https://ocr.space))

This library is available as a __jitpack__ dependency!   [![](https://jitpack.io/v/carlossotelo/ohcrapi.svg)](https://jitpack.io/#carlossotelo/ohcrapi)

# Performing Ocr locally 
## 1. Setup
To setup the ocr task, we need 3 steps:

1. Get the tessdata of the language we want. Download the desired language from [__HERE__](https://github.com/tesseract-ocr/tessdata "GitHub TessData").

2. Place it under your assets Folder
The .traineddata file should be placed under your assets folder.
```
/assets
  /tessdata
    eng.traineddata
```
3. Use the _OhCRapiLocal.init_ method to start up the OCR engine and copies the traineddata file from your assets folder to the android device. Starting up the engine and copying the assets can be heavy, and that is why thee method first checks if the engine is not instantiated.
```java
 /** Initializes the Ocr engine (TessBaseAPI). If the engine already exists, nothing is done.
     * @param ctx - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     */
    public static void init(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable TessBaseAPI.ProgressNotifier progressNotifier) {
        if (mLocalOcrEngine == null) {
            mLocalOcrEngine = touchTesseract(ctx, tessDirectoryPath, trainedDataLanguage, progressNotifier);
        }
    }
```
To force the initialization of a new engine (to use another language for instance) use the method _OhCRapiLocal.restart_ with the same signature as the _OhCRapiLocal.init_ method.

## 2. Calling the AsyncTask

## 3. Handling Results

***

# Performing Ocr as a service
## 1. Setup

## 2. Calling the Service

## 3. Handling Results

Service response details can be found [Here](https://ocr.space/OCRAPI)
