package club.chisc.fantasticfilterandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private static final int OPEN_IMAGE_CODE = 0;
    private static final int SAVE_IMAGE_CODE = 1;
    ImageView previewImageView;
    Context context;
    Bitmap originImage;
    Enhancer enhancer = new Enhancer();
    private Bitmap enhancedImage;
    private String DONATE_URL = "https://ray1422.github.io/Fantastic-Filter-Professional-Plus";
    private Bitmap fullSizeOrigin;
    private ImageView adsImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        previewImageView = findViewById(R.id.preview);
        adsImageView = findViewById(R.id.ads);
        final Button loadImageButton = findViewById(R.id.loadImage);
        final Button resizeButton = findViewById(R.id.resize);
        final Button enhanceButton = findViewById(R.id.enhance);
        final Button shareButton = findViewById(R.id.share);
        final Button saveButton = findViewById(R.id.save);
        final Button donateButton = findViewById(R.id.donate);
        try {
            InputStream inputStream = getAssets().open("banner.png");
            Bitmap banner = BitmapFactory.decodeStream(inputStream);
            previewImageView.setImageBitmap(banner);
            inputStream = getAssets().open("ads.png");
            Bitmap ads = BitmapFactory.decodeStream(inputStream);
            adsImageView.setImageBitmap(ads);
        } catch (IOException e) {
            // handle exception
        }
        enhancer.init(getAssets());
        previewImageView.post(new Runnable() {
            @Override
            public void run() {
                //int width = previewImageView.getWidth();
                //int height = (int) ((float) width / 16 * 9);
                //previewImageView.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
            }
        });

        loadImageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                enhanceButton.setEnabled(true);
                //load image
                Intent intent = new Intent()
                        .setType("image/jpeg")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a photo"), OPEN_IMAGE_CODE);
            }
        });
        resizeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (originImage == null) {
                    Toast.makeText(context, "請先載入圖片！", Toast.LENGTH_LONG).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                final View mView = inflater.inflate(R.layout.resize_dialog, null);
                builder.setView(mView)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                TextView longSideSizeTV = mView.findViewById(R.id.long_side_size);
                                int longSideSize = Integer.parseInt(longSideSizeTV.getText().toString());
                                longSideSize = longSideSize < 100 ? 100 : longSideSize;
                                longSideSize = longSideSize - longSideSize % 4;
                                int h = fullSizeOrigin.getHeight();
                                int w = fullSizeOrigin.getWidth();
                                if (h > w) {
                                    w = (int) (w * ((float) longSideSize / h));
                                    w = w - w % 4;
                                    h = longSideSize;
                                } else {
                                    h = (int) (h * ((float) longSideSize / w));
                                    h = h - h % 4;
                                    w = longSideSize;
                                }
                                originImage = Bitmap.createScaledBitmap(fullSizeOrigin, w, h, false);//ThumbnailUtils.extractThumbnail(bitmap, w, h);
                                Toast.makeText(context, "高度:" + h + " 寬度:" + w, Toast.LENGTH_LONG).show();

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.create();
                builder.show();

            }
        });
        enhanceButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originImage == null) {
                    Toast.makeText(context, "請先載入圖片 ╮(╯_╰)╭", Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("即將開始處理")
                        .setMessage("您好，由於AI過於強大，請於開始後不要亂點，以免出現不可逆的錯誤")
                        .setPositiveButton("我準備好了，快開車！", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                enhanceButton.setEnabled(false);
                                loadImageButton.setEnabled(false);
                                Toast.makeText(context, "正在增強畫質...", Toast.LENGTH_LONG).show();
                                final ProgressDialog loadingDialog = new ProgressDialog(MainActivity.this).show();
                                new Thread(new Runnable() {
                                    public void run() {
                                        enhancedImage = enhancer.enhance(originImage);
                                        previewImageView.post(new Runnable() {
                                            public void run() {
                                                previewImageView.setImageBitmap(enhancedImage);
                                                Toast.makeText(context, "處理完成AWA", Toast.LENGTH_LONG).show();
                                                loadImageButton.setEnabled(true);
                                                loadingDialog.dismiss();
                                            }
                                        });
                                    }
                                }).start();
                                //
                            }

                        }).show();
            }
        });
        shareButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = enhancedImage;
                if (enhancedImage == null) {
                    Toast.makeText(context, "請先增強圖片(´ﾟдﾟ`)", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.putExtra(Intent.EXTRA_TEXT, "#FantasticFilterPrefessionalPlus");
                    Uri uri = bitmap2Uri(context, bitmap);
                    if (uri == null) {
                        return;
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setType("image/*");
                    startActivity(Intent.createChooser(intent, "Share image via"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enhancedImage == null) {
                    Toast.makeText(context, "請先增強圖片(´･_･`)", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_TITLE, "enhanced.jpg");
                startActivityForResult(intent, SAVE_IMAGE_CODE);
            }
        });
        donateButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(DONATE_URL));
                startActivity(i);
            }
        });
    }

    public Uri bitmap2Uri(Context inContext, Bitmap inImage) {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_read = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PERMISSION_GRANTED || permission_read != PERMISSION_GRANTED) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("需要權限")
                    .setMessage("您好，我們需要讀寫的權限才能緩存照片，請於下一步點選允許，並且重新執行操作。")
                    .setPositiveButton("好！", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 48763);
                        }
                    }).show();
            return null;
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "FantasticFilter", null);
        return Uri.parse(path);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_IMAGE_CODE && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData(); //The uri with the location of the file
            assert selectedFile != null;
            int rotation = getRotation(context, selectedFile);
            try {
                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.setRotate(rotation);
                fullSizeOrigin = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedFile);
                fullSizeOrigin = Bitmap.createBitmap(fullSizeOrigin, 0, 0, fullSizeOrigin.getWidth(), fullSizeOrigin.getHeight(), matrix, true);
                //TODO: 自定義長寬
                int h = fullSizeOrigin.getHeight();
                int w = fullSizeOrigin.getWidth();
                if (h > w) {
                    if ((h / w) > 1.7) {
                        w = (int) (w * ((float) 1280 / h));
                        w = w - w % 4;
                        h = 1280;
                    } else {
                        w = (int) (w * ((float) 1024 / h));
                        w = w - w % 4;
                        h = 1024;
                    }
                } else {
                    if ((h / w) > 1.7) {
                        h = (int) (h * ((float) 1280 / w));
                        h = h - h % 4;
                        w = 1280;
                    } else {
                        h = (int) (h * ((float) 1024 / w));
                        h = h - h % 4;
                        w = 1024;
                    }
                }
                originImage = Bitmap.createScaledBitmap(fullSizeOrigin, w, h, false);//ThumbnailUtils.extractThumbnail(bitmap, w, h);
                //originImage.setConfig(Bitmap.Config.RGB_565);
                previewImageView.setImageBitmap(originImage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == SAVE_IMAGE_CODE && resultCode == RESULT_OK) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 48763);
            }
            Uri uri = data.getData();


            try {
                assert uri != null;
                OutputStream out = context.getContentResolver().openOutputStream(uri, "rw");
                enhancedImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Toast.makeText(context, "圖片已經儲存好囉(*´∀`)~♥", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static int getRotation(Context context, Uri uri) {
        int rotation = 0;
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            ExifInterface exifInterface = null;
            exifInterface = new ExifInterface(in);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }

}

