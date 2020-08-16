package com.example.cropdetector;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Classifier {
    private static String TAG="Classifier";

    class pair
    {
        float val;
        String label;
        pair(float val,String label)
        {
            this.val = val;
            this.label = label;
        }
    };
    private static final int IMAGE_MEAN = 0;
    private static final int IMAGE_STD = 255;
    private static int MAX_SIZE = 2;
    static int INPUT_SIZE = 64;
    static  int PIXEL_SIZE = 3;
    String label_path

     AssetManager assetManager;
     Interpreter interpreter;
     ArrayList<String>labels;
     Context context;
    public String predict(Bitmap image) {
        ByteBuffer inpBuffer = preProcessImage(image);
        Tensor outTensor = interpreter.getOutputTensor(0);
        int[] outShape = outTensor.shape();
        DataType outType = outTensor.dataType();
        Log.d("datatype is", "predict: "+ outType);
        float[][] out = new float[1][10];
        interpreter.run(inpBuffer, out);
        return sortedResult(out);
    }

    Classifier(Context context, String modelPath,String labelPath){
        this.context =  context;
        this.model_path = modelPath;
        this.label_path = labelPath;
    }
    private Interpreter createInterpreter(String model_path){
        Interpreter.Options options= new Interpreter.Options();
        options.setNumThreads(5);
        options.setUseNNAPI(true);
        return new Interpreter(loadModelFile(model_path