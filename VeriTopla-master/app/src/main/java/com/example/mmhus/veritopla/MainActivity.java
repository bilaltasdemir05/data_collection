package com.example.mmhus.veritopla;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback ,SensorEventListener {

    Button baslat, kaydet;
    TextView bilgitv,konumtv,vidtv;
    boolean zaman;
    private SurfaceView svYuzey;
    private SurfaceHolder sh;
    private Camera cmr;
    MediaRecorder mr;
    private boolean isRecording = false;
    public static final int MEDIA_TYPE_VIDEO =2;

    final int GPS_SENSORNO = 0;
    final int YAZIM_SENSORNO = GPS_SENSORNO + 1;
    int sensorNo = 0;

    float[] accDeger = new float[]{0, 0, 0};
    float[] graDeger = new float[]{0, 0, 0};
    float[] laDeger = new float[]{0, 0, 0};
    float[] gyDeger = new float[]{0, 0, 0};

    //### Managerler ####
    SensorManager sm;
    LocationManager lm;
    //###################

    //### Diğer Değişkenler ###
    int durum = 0;
    File dosya, dosya2;
    FileWriter yaz, yazL;
    BufferedWriter yazici,yaziciL;
    String satir = "", satirloc="",test;
    String sensor="";
    int counter = 1;
    int countt = 0;
    int sayac = 1;
    int kont = 1;
    boolean isBasladi = false;
    String timeStampp, timeStamppp;
    static String testAdi;
    girisEkrani giris;
    static String pathh;
    PowerManager.WakeLock wakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baslat = findViewById(R.id.baslat);
        kaydet = findViewById(R.id.kaydet);
        kaydet.setVisibility(View.INVISIBLE);
        konumtv = findViewById(R.id.tvKonum);
        vidtv = findViewById(R.id.tvVid);
        konumtv.setTextColor(Color.RED);
        vidtv.setTextColor(Color.RED);
        svYuzey =findViewById(R.id.svYuzey);
        sh = svYuzey.getHolder();
        sh.addCallback(this);
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cmr = getCameraInstance();
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");

        //sh.setFixedSize(300,300);
        /*Toast.makeText(getApplicationContext(),"BASLADI",Toast.LENGTH_SHORT).show();
        kont =1;
        zaman = new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"BASIIIIILDI",Toast.LENGTH_SHORT).show();
                baslat.performClick();
            }
        },10000);*/
    }

    @SuppressLint("WakelockTimeout")
    public void setBaslat(View V) {
        try {
            if (durum == 0) {
                //FTPText.setText("");
                wakeLock.acquire();
                Toast.makeText(getApplicationContext(), "GPS bağlantısı sağlanır sağlanmaz işlemler başlayacak!", Toast.LENGTH_LONG).show();
                durum = 1;
                baslat.setText("Durdur");

                sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
                sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
                sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
                girisEkrani giris = new girisEkrani();
                test = giris.testAdi;
                try {
                    Criteria kriterler = new Criteria();
                    kriterler.setAccuracy(Criteria.ACCURACY_FINE);
                    kriterler.setAltitudeRequired(false);
                    kriterler.setSpeedRequired(true);
                    kriterler.setPowerRequirement(Criteria.POWER_HIGH);
                    //kriterler.setCostAllowed(false);

                    String bilgiSaglayici = lm.getBestProvider(kriterler, true);
                    if (bilgiSaglayici == null) {
                        List<String> bilgiSaglayicilar = lm.getAllProviders();

                        for (String tempSaglayici : bilgiSaglayicilar) {
                            if (lm.isProviderEnabled(tempSaglayici))
                                bilgiSaglayici = tempSaglayici;
                        }
                    }

                    if (lm.isProviderEnabled(bilgiSaglayici)) {
                        LocationListener locationListener = new LocationListener() {

                            @Override
                            public void onLocationChanged(Location location) {
                                timeStamppp = SimpleDateFormat.getTimeInstance().format(new Date());
                                //timeStampp = new SimpleDateFormat("HHmmss").format(new Date());

                                    String hiz = String.valueOf(location.getSpeed() * 3.6);
                                    satirloc = (location.getLatitude() + ";" + location.getLongitude() + ";" + hiz+";"+timeStamppp+"\n");
                                    try {
                                    yaziciL.write(satirloc);
                                    } catch (IOException e) {
                                    e.printStackTrace();
                                    }
                                    if(kont==1){
                                        Toast.makeText(getApplicationContext(),"GPS VERİLERİ ALINMAYA BAŞLANDI",Toast.LENGTH_LONG).show();
                                        konumtv.setTextColor(Color.GREEN);
                                        isBasladi = true;
                                        kont++;
                                       kaydet.performClick();
                                    }
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                                Toast.makeText(getApplicationContext(), "GPS Verisi Alınıyor!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                Toast.makeText(getApplicationContext(), "GPS Bağlantı Kapandı!", Toast.LENGTH_LONG).show();
                            }
                        };
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }

                        lm.requestLocationUpdates(bilgiSaglayici, 0, 0, locationListener);
                    } else {
                        sm.unregisterListener(this);
                        Toast.makeText(getApplicationContext(), "Konum sağlayıcısı kapalı!!!", Toast.LENGTH_LONG).show();
                        durum = 0;
                        baslat.setText("Başlat");
                        konumtv.setTextColor(Color.RED);
                    }

                } catch (Exception e) {
                    Log.e("Hata!", e.getMessage());
                    e.printStackTrace();
                }
                DosyaOlustur();
            } else {
                wakeLock.release();
                isBasladi = false;
                durum = 0;
                counter = 1;
                countt = 0;
                konumtv.setTextColor(Color.RED);
                baslat.setText("Başlat");
                if(isRecording)
                    kaydet.performClick();
                sm.unregisterListener(this);
                yazici.close();
                yaziciL.close();
            }
        } catch (Exception e) {
            Log.e("Hata!", e.getMessage());
            e.printStackTrace();
        }

    }

    private void DosyaOlustur() {
        try {

            //SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
            //String dosyaAdi = ft.format(new Date()) + ".csv";
            timeStampp = SimpleDateFormat.getTimeInstance().format(new Date());
            String dosyaAdi = "Veri"+timeStampp+".txt";
            String dosyaAdiL = "Konum"+timeStampp+".txt";


            File klasor = new File(Environment.getExternalStoragePublicDirectory("Veri Topla/"+testAdi), "Veriler");
            if (!klasor.exists()) {
                if (!klasor.mkdirs()) {
                    Log.e("dosya", "Dosya oluluşturulamadı");
                }
                else
                {
                    Log.e("dosya","mkdir var");
                }
            }
            else{
                Log.e("dosya","exists var");
                Toast.makeText(getApplicationContext(),"Bu isimde bir test kalsörü bulunmakta",Toast.LENGTH_LONG).show();
            }
            pathh = "Veri Topla/"+testAdi;
            dosya = new File(Environment.getExternalStoragePublicDirectory("Veri Topla/"+testAdi+"/Veriler"),dosyaAdi);
            yaz = new FileWriter(dosya, true);
            yazici = new BufferedWriter(yaz);

            if (!dosya.exists()) {
                //dosya.mkdirs();
                dosya.createNewFile();

            }

            dosya2 = new File(Environment.getExternalStoragePublicDirectory("Veri Topla/"+testAdi+"/Veriler"),dosyaAdiL);
            yazL = new FileWriter(dosya2,true);
            yaziciL = new BufferedWriter(yazL);

            if(!dosya2.exists()){
                dosya2.createNewFile();

            }

            if (countt == 0){
                yazici.write("AccX;AccY;AccZ;GraX;GraY;GraZ;LAX;LAY;LAZ;GyroX;GyroY;GyroZ;Time2\n");
                yaziciL.write("Lat;Long;Hız;Time\n");
                countt++;
            }
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(klasor)));

        } catch (Exception e) {
            Log.e("Hata!", e.getMessage());
            e.printStackTrace();
        }
        sayac ++;

    }

    @Override
    protected void onResume() {
        super.onResume();
        cmr = getCameraInstance();
//        prepareVideoRecorder();
    }

    public void setKaydet(View v){
        if(isRecording){
            mr.stop();
            releaseMediaRecorder();
            cmr.lock();
            Toast.makeText(getApplicationContext(),"Video Kayıt Durduruldu",Toast.LENGTH_LONG).show();
            vidtv.setTextColor(Color.RED);
            kaydet.setText(R.string.kaydetbtn);
            isRecording = false;
        }
        else{
            if(prepareVideoRecorder()){
                mr.start();
                Toast.makeText(getApplicationContext(),"Video Kayıt Başladı",Toast.LENGTH_LONG).show();
                vidtv.setTextColor(Color.GREEN);
                kaydet.setText("DURDUR");
                isRecording= true;
            }
            else{
                releaseMediaRecorder();
            }
        }
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(pathh),"Video Kayıt");
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.e("dosyaa","Dosya oluşturulamadı");
                return null;
            }
        }
        String timeStamp = SimpleDateFormat.getTimeInstance().format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath()+File.separator+"VID"+timeStamp+".mp4");
        return mediaFile;
    }

    private void releaseMediaRecorder() {
        if(mr != null){
            mr.reset();
            mr.release();
            mr = null;
            cmr.lock();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }

    private void releaseCamera(){
        if(cmr!=null){
            cmr.release();
            cmr = null;
        }
    }

    public static  Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }catch (Exception e){

        }
        return c;
    }

    private boolean prepareVideoRecorder(){
        mr = new MediaRecorder();
        cmr.unlock();
        mr.setCamera(cmr);
        mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mr.setOrientationHint(90);
//        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mr.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
        mr.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mr.setPreviewDisplay(svYuzey.getHolder().getSurface());
        try {
            mr.prepare();
        }catch (IllegalStateException e){
            Log.e("asd",e.getMessage());
            releaseMediaRecorder();
            return false;

        }catch (IOException e){
            Log.e("IOOO",e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {

            cmr.setPreviewDisplay(sh);
            cmr.setDisplayOrientation(90);
            cmr.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        try{
            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accDeger[0] = event.values[0];
                accDeger[1] = event.values[1];
                accDeger[2] = event.values[2];
            }

            if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                laDeger[0] = event.values[0];
                laDeger[1] = event.values[1];
                laDeger[2] = event.values[2];
            }

            if(sensor.getType() == Sensor.TYPE_GRAVITY){
                graDeger[0] = event.values[0];
                graDeger[1] = event.values[1];
                graDeger[2] = event.values[2];
            }

            if(sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyDeger[0] = event.values[0];
                gyDeger[1] = event.values[1];
                gyDeger[2] = event.values[2];
            }
            if(isBasladi) {
                timeStampp = SimpleDateFormat.getTimeInstance().format(new Date());
                satir = String.format("%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;%f;", accDeger[0], accDeger[1], accDeger[2], graDeger[0], graDeger[1], graDeger[2], laDeger[0], laDeger[1], laDeger[2], gyDeger[0], gyDeger[1], gyDeger[2]);
                satir += timeStampp;
                Log.d("Eşleşti", satir);
                yazici.write(satir + "\n");
                satir = "";
            }


        } catch (Exception e) {
            Log.e("Hata!", e.getMessage());
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
