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
    }
    private static final int IMAGE_MEAN = 0;
    private static final int IMAGE_STD = 255;
    private static int MAX_SIZE = 2;
    static int INPUT_SIZE = 64;
    static  int PIXEL_SIZE = 3;
    String label_path;
    String model_path;

     AssetManager assetManager;
     Interpreter interpreter;
     ArrayList<String>labels;
     Context context;
    Classifier(Context context,AssetManager assetManager, String modelPath,String labelPath){
        this.context =  context;
        this.model_path = modelPath;
        this.assetManager=assetManager;
        this.label_path = labelPath;
        labels=new ArrayList<>();
    }
    public String predict(Bitmap image) {
        ByteBuffer inpBuffer = preProcessImage(image);
        interpreter = createInterpreter(model_path);
        loadLabel(assetManager,label_path);
        Tensor outTensor = interpreter.getOutputTensor(0);
        int[] outShape = outTensor.shape();
        DataType outType = outTensor.dataType();
        Log.d("datatype is", "predict: " + outType);
        float[][] out = new float[1][10];
        interpreter.run(inpBuffer, out);
        return sortedResult(out);
    }
    private Interpreter createInterpreter (String model_path){
//        Interpreter.Options options= new Interpreter.Options();
//        options.setNumThreads(5);
//        options.setUseNNAPI(true);
            return new Interpreter(loadModelFile(assetManager,model_path));
    }
    private static ByteBuffer preProcessImage(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues =new int[INPUT_SIZE * INPUT_SIZE];
        byteBuffer.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i=0; i< INPUT_SIZE; i++) {
            for (int j=0; j<INPUT_SIZE; j++) {
                int input = intValues[pixel++];

                byteBuffer.putFloat((((input>>16  & 0xFF) - IMAGE_MEAN) / IMAGE_STD));
                byteBuffer.putFloat((((input>>8 & 0xFF) - IMAGE_MEAN) / IMAGE_STD));
                byteBuffer.putFloat((((input & 0xFF) - IMAGE_MEAN) / IMAGE_STD));
            }
        }
        return byteBuffer;
    }

    public void loadLabel(AssetManager assetManager,String labelPath)
    {
        BufferedReader bf = null;
        try
        {
            bf = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
            String lb;
            while((lb=bf.readLine())!=null)
            {
                labels.add(lb);
                Log.d(TAG, "loadLabel: "+lb);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(context,"labels cannot be loaded",Toast.LENGTH_LONG).show();
        }
    }

     public String sortedResult(float out[][])
     {
         Comparator<pair> comparator = new floatComparator();
         PriorityQueue<pair> pq = new PriorityQueue<pair>(labels.size(),comparator);

         for(int i=0;i<labels.size();i++)
         {
             Log.d(TAG, "sortedResult: "+out[0][i]+" "+labels.get(i));
             pq.add(new pair(out[0][i],labels.get(i)));
         }

         String ans="";
         ans+=pq.poll().label;
         ans+="\n";
         ans+=pq.poll().label;
         return ans;
     }

    static public class floatComparator implements  Comparator<pair>
     {
         @Override
         public int compare(pair t2, pair t1) {
            return java.lang.Float.compare(t2.val,t1.val)*-1;
         }
     }
    public ByteBuffer loadModelFile(AssetManager assetManager,String modelPath){
            AssetFileDescriptor fileDescriptor = null;
            Log.d("this  ", "loadModelFile: loading the model "+modelPath);
            try
            {
                fileDescriptor = assetManager.openFd(modelPath);
                Log.d("this2", "loadModelFile: model loaded ");
                FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
                FileChannel fileChannel = inputStream.getChannel();
                long startOffset = fileDescriptor.getStartOffset();
                long declaredLength = fileDescriptor.getDeclaredLength();
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;

    }


}
