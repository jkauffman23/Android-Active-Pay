package com.miriasystems.jkauffman.active_pay_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.apache.commons.net.ftp.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ActivePayImageViewActivity extends AppCompatActivity {

    private String strAuthToken;
    private String strSeqId;
    private String strGuid;
    private String strJsonLocation;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //create the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_pay_image_view);

        //gets the auth token from login
        Bundle extras = getIntent().getExtras();
        strAuthToken = extras.getString("globalAuthToken");
        strGuid = extras.getString("detailGuid");
        strSeqId = extras.getString("detailSeqId");
        progressDialog = new ProgressDialog(ActivePayImageViewActivity.this);

        //button creations

        //logout
        Button mLogoutButton = (Button) findViewById(R.id.btn_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppDelegate.logout(ActivePayImageViewActivity.this);
            }
        });


        //3 bottom navigation buttons
        ImageButton mImageNav = (ImageButton) findViewById(R.id.image_nav_button);
        mImageNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToImage();
            }
        });

        ImageButton mLinesNav = (ImageButton) findViewById(R.id.line_details_nav_button);
        mLinesNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLines();
            }
        });

        ImageButton mHeaderNav = (ImageButton) findViewById(R.id.header_nav_button);
        mHeaderNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToHeader();
            }
        });

        //begin the task of displaying the image
        ImageJsonTask mImageJsonTask = new ImageJsonTask();
        mImageJsonTask.execute(AppDelegate.createRestUrl(ActivePayImageViewActivity.this, "image/getimage"));

    }

    //handles physical back button pressing
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ActivePayInvoiceHeaderActivity.class);
        intent.putExtra("globalAuthToken", strAuthToken);
        intent.putExtra("detailSeqId", strSeqId);
        intent.putExtra("detailGuid", strGuid);
        startActivity(intent);
    }

    public void navigateToImage() {
        Intent intent = new Intent(this, ActivePayImageViewActivity.class);
        intent.putExtra("globalAuthToken", strAuthToken);
        intent.putExtra("detailSeqId", strSeqId);
        intent.putExtra("detailGuid", strGuid);
        startActivity(intent);
    }

    public void navigateToHeader() {
        Intent intent = new Intent(this, ActivePayInvoiceHeaderActivity.class);
        intent.putExtra("globalAuthToken", strAuthToken);
        intent.putExtra("detailSeqId", strSeqId);
        intent.putExtra("detailGuid", strGuid);
        startActivity(intent);
    }

    public void navigateToLines() {
        Intent intent = new Intent(this, ActivePayInvoiceDetailLinesActivity.class);
        intent.putExtra("globalAuthToken", strAuthToken);
        intent.putExtra("detailSeqId", strSeqId);
        intent.putExtra("detailGuid", strGuid);
        startActivity(intent);
    }

    //async task to download image file from site
    public class ImageJsonTask extends AsyncTask<String, Void, Boolean> {

        private static final int BUFFER_SIZE = 4096;
        String content;
        String error;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Image loading ...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader br = null;
            URL url;

            try {
                url = new URL(params[0]);
                //parameters to pass
                String urlParameters = "authToken=" + strAuthToken + "&guid=" + strGuid;
                urlParameters = urlParameters.replace("%", "%25");
                urlParameters = urlParameters.replace("%2525", "%25");
                urlParameters = urlParameters.replace("+", "%2B");
                //postable data
                byte[] postData = urlParameters.getBytes("UTF-8");
                String postDataString = URLEncoder.encode(urlParameters, "utf-8");
                Log.w("Array", Arrays.toString(postData));
                Log.w("URL Encoder", postDataString);
                String remadeString = new String(postData);
                Log.w("Byte[]", remadeString);

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "utf-8");

                Log.w("CONN", "Opened");
                try {
                    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

                    for (int i = 0; i < postData.length; i++) {
                        wr.write(postData[i]);
                        Log.w(Integer.toString(i), Byte.toString(postData[i]));
                    }
                    //wr.writeBytes(urlParameters);
                    //Log.w("SIZE", Integer.toString(wr.size()));
                    wr.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append(System.getProperty("line.separator"));
                }

                content = sb.toString();
                Log.w("CONTENT", content);

            } catch (MalformedURLException e) {
                error = e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            if (content.contains("location"))
                return true;
            else
                return false;

        }


        @Override
        protected void onPostExecute(Boolean success) {

            JSONObject jsonResponse;

            if (success) {
                //if recieved json file contains an AuthNum display ActivePayInvoiceListActivity
                String strAuthToken = "";
                try {
                    jsonResponse = new JSONObject(content);

                    strJsonLocation = jsonResponse.get("location").toString();
                    Log.w("JSONARRAY", jsonResponse.toString());

                    FtpDownloadTask ftpTask = new FtpDownloadTask();
                    ftpTask.execute(strJsonLocation);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {

                progressDialog.dismiss();

                String message = "Timeout Error";
                String code = "";
                try {
                    jsonResponse = new JSONObject(content);
                    message = (String) jsonResponse.get("message");
                    code = (String) jsonResponse.get("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ActivePayErrorMessageDialog dialog = new ActivePayErrorMessageDialog(ActivePayImageViewActivity.this, "Image View", message, code);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();


            }


        }
    }

    Drawable DownloadDrawable(String url, String src_name) throws java.io.IOException {
        return Drawable.createFromStream(((java.io.InputStream) new java.net.URL(url).getContent()), src_name);
    }

    public class FtpDownloadTask extends AsyncTask<String, Void, URL> {

        private static final int BUFFER_SIZE = 4096;
        String strUrlPathFileName;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected URL doInBackground(String... params) {
            BufferedReader br = null;
            URL url = null;
            Bitmap bm = null;
            String strUrlPath = "";
            String strUrlProtocol = "";
            String strUrlUserInfo = "";
            String strUrlHost = "";
            String strUserName = "";
            String strUserPass = "";
            int iUrlDefaultPort = 0;
            String strUrl = params[0];
            //substring used because json value passed surrounded by [" "]
            strUrl = strUrl.substring(2, strUrl.length() - 2);
            strUrl = strUrl.replace("\\", "");
            strUrl = strUrl.replace("%7B", "{");
            strUrl = strUrl.replace("%7D", "}");

            //create url
            try {
                url = new URL(strUrl);

                System.out.println("url : " + url.toString());

                strUrlPath = url.getPath();
                System.out.println("strUrlPath : " + strUrlPath);

                strUrlProtocol = url.getProtocol();
                System.out.println("strUrlProtocol : " + strUrlProtocol);

                strUrlHost = url.getHost();
                System.out.println("strUrlHost : " + strUrlHost);

                iUrlDefaultPort = url.getDefaultPort();
                System.out.println("iUrlDefaultPort : " + iUrlDefaultPort);

                strUrlUserInfo = url.getUserInfo();
                System.out.println("sUrlUserInfo : " + url.getUserInfo());
                String namepass[] = strUrlUserInfo.split(":");
                strUserName = namepass[0];
                strUserPass = namepass[1];


            } catch (Exception e) {
                e.printStackTrace();
            }

            if (strUrlProtocol.equals("ftp")) {
                //connect to site depending on protocol
                try {

                    FTPClient ftp = null;

                    try {
                        ftp = new FTPClient();
                        //connection information
                        ftp.connect(strUrlHost, iUrlDefaultPort);
                        ftp.login(strUserName, strUserPass);
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);
                        ftp.enterLocalPassiveMode();

                        strUrlPathFileName = strUrl.replaceFirst(".*/([^/?]+).*", "$1");
                        System.out.println("strUrlPathFileName : " + strUrl.replaceFirst(".*/([^/?]+).*", "$1"));


                        //create the file (same for every use) that stores the user information
                        File fname = new File(getFilesDir().getAbsolutePath(), strUrlPathFileName);
                        if (!fname.exists()) {
                            fname.createNewFile();
                        }

                        System.out.println("fname initial : " + fname.toString());
                        //outputstream information
                        OutputStream outputStream = null;
                        boolean success = false;
                        FileOutputStream fos = new FileOutputStream(fname);


                        try {
                            outputStream = new BufferedOutputStream(fos);
                            //download file with outputStream
                            success = ftp.retrieveFile(strUrlPath, outputStream);
                            System.out.println("File was retrieved : " + success);

                        } finally {
                            //close outputstream
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }

                        //and then ftp disconnection
                    } finally {
                        if (ftp != null) {
                            ftp.logout();
                            ftp.disconnect();
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (strUrlProtocol.equals("https")) {


//                try {
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                    connection.setRequestMethod("GET");
//                    connection.setDoOutput(true);
//
//                    //create the file (same for every use) that stores the user information
//                    File fname = new File(getFilesDir().getAbsolutePath(), "imagefile.tif");
//                    if (!fname.exists()) {
//                        fname.createNewFile();
//                    }
//
//                    //this will be used to write the downloaded data into the file we created
//                    FileOutputStream fileOutput = new FileOutputStream(fname);
//                    //this will be used in reading the data from the internet
//                    InputStream inputStream = connection.getInputStream();
//                    //this is the total size of the file
//                    int totalSize = connection.getContentLength();
//                    //variable to store total downloaded bytes
//                    int downloadedSize = 0;
//                    //create a buffer...
//                    byte[] buffer = new byte[1024];
//                    int bufferLength = 0; //used to store a temporary size of the buffer
//
//                    //now, read through the input buffer and write the contents to the file
//                    while ( (bufferLength = inputStream.read(buffer)) > 0 )
//                    {
//                        //add the data in the buffer to the file in the file output stream (the file on the sd card
//                        fileOutput.write(buffer, 0, bufferLength);
//
//                        //add up the size so we know how much is downloaded
//                        downloadedSize += bufferLength;
//
//                        int progress=(int)(downloadedSize*100/totalSize);
//
//
//                    }
//
//                    System.out.println("Downloaded");
//                    //close the output stream when done
//                    fileOutput.close();
//
//                } catch(Exception e){
//                    e.printStackTrace();
//                }
            }

            return url;

        }


        @Override
        protected void onPostExecute(URL url) {

            String strUrlProtocol = url.getProtocol();
            System.out.println("strUrlProtocol : " + strUrlProtocol);

            if (strUrlProtocol.equals("https")) {

                WebView webview = (WebView) findViewById(R.id.web_detail_image);
                webview.loadUrl(url.toString());
                progressDialog.dismiss();

            } else if (strUrlProtocol.equals("ftp")) {

                PhotoViewAttacher mAttacher;

                try {
                    WebView webview = (WebView) findViewById(R.id.web_detail_image);
                    webview.setVisibility(View.INVISIBLE);
                    ImageView imageView = (ImageView) findViewById(R.id.detail_image);
                    File inFile = new File(getFilesDir().getAbsolutePath(), strUrlPathFileName);

                    String strExt = "";

                    int i = strUrlPathFileName.lastIndexOf('.');
                    if (i > 0) {
                        strExt = strUrlPathFileName.substring(i + 1);
                    }
                    System.out.println("strExt : " + strExt);

                    if (strExt.equals("tif") || strExt.equals("tiff")) {
                        System.out.println("inFile : " + inFile);
                        Bitmap ob = AppDelegate.decodeSampledTiffBitmapFromFile(inFile, 500, 500);
                        mAttacher = new PhotoViewAttacher(imageView);

                        //TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
                        //options.inAvailableMemory = 1024 * 1024 * 10; //10 mb
                        //Bitmap bmp = TiffBitmapFactory.decodeFile(inFile, options);
                        imageView.setImageBitmap(ob);
                        mAttacher.update();
                        inFile.delete();

                    } else if (strExt.equals("pdf")) {


                        Bitmap ob = AppDelegate.renderToBitmap(getApplicationContext(), inFile.toString());
                        mAttacher = new PhotoViewAttacher(imageView);



                        imageView.setImageBitmap(ob);
                        mAttacher.update();



                    } else {

                        Bitmap ob = AppDelegate.decodeSampledBitmapFromFile(inFile, 500, 500);
                        mAttacher = new PhotoViewAttacher(imageView);
                        imageView.setImageBitmap(ob);
                        mAttacher.update();
                        inFile.delete();
                    }

                    progressDialog.dismiss();
                } catch (Exception e) {
                    File inFile = new File(getFilesDir().getAbsolutePath(), strUrlPathFileName);
                    inFile.delete();
                    progressDialog.dismiss();

                    ActivePayErrorMessageDialog dialog = new ActivePayErrorMessageDialog(ActivePayImageViewActivity.this, "Active Finance Image View", "Image could not be displayed", "IMG001");
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();

                }


            }

        }


    }


}
