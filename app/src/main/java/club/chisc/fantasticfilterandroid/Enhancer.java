package club.chisc.fantasticfilterandroid;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.ByteArrayOutputStream;

public class Enhancer {
    private static final String INPUT_NODE = "input_image:0";
    private static final String OUTPUT_NODE = "output_image:0";
    private static final String MODEL_FILE = "file:///android_asset/filmlabs_m.pb";
    private AssetManager am;
    private TensorFlowInferenceInterface inferenceInterface;

    void init(AssetManager assetManager) {
        am = assetManager;
        inferenceInterface = new TensorFlowInferenceInterface(am, MODEL_FILE);
    }

    Bitmap enhance(Bitmap bitmap) {
        System.out.println("start enhancing");
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float[] floatValues = new float[w * h * 3];
        int[] intValues = new int[w * h];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF);
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF);
            floatValues[i * 3 + 2] = (val & 0xFF);
        }
        inferenceInterface.feed(INPUT_NODE, floatValues, 1, bitmap.getHeight(), bitmap.getWidth(), 3);
        inferenceInterface.run(new String[]{OUTPUT_NODE}, true);
        inferenceInterface.fetch(OUTPUT_NODE, floatValues);
        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3])) << 16)
                            | (((int) (floatValues[i * 3 + 1])) << 8)
                            | ((int) (floatValues[i * 3 + 2]));
        }
        Bitmap new_bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        new_bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        System.out.println("done!");
        return new_bitmap;

    }

}
