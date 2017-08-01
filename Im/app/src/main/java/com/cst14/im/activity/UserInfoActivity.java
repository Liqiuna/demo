package com.cst14.im.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cst14.im.Dialog.ModifyDialog;
import com.cst14.im.R;
import com.cst14.im.listener.SaveAttrPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.pictureUtils.httprequestPresenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInfoActivity extends Activity {
    Uri  uritempFile=null;
    private SaveAttrPresenter msaveAttrPresenter;
    private ModifyDialog dialog;
    ProgressDialog mdialog=null;
    /* 请求识别码 */
    private static final int CODE_GALLERY_REQUEST = 0xa0;//本地
    private static final int CODE_CAMERA_REQUEST = 0xa1;//拍照
    private static final int CODE_RESULT_REQUEST = 0xa2;//最终裁剪后的结果
    private static final int SELECT_PIC_KITKAT = 0xa3;
    /* 头像文件 */
    private static final String IMAGE_FILE_NAME = "temp_head_image.jpg";
    // 裁剪后图片的宽(X)和高(Y),480 X 480的正方形。
    private static int output_X = 600;
    private static int output_Y = 600;
    private static String requestURL = ImApplication.FILE_SERVER_HOST;
    private static String requestUploadURL = ImApplication.FILE_SERVER_HOST+"/Bitmap";

    private String  params=null,TAG="UserInfoActivity",address="";
    private static  Bitmap bitmap=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        msaveAttrPresenter=new SaveAttrPresenter();
        Initview();
        if(Model.userDetail==null){
            return;
        }
        if(Model.userDetail.hasAddress()==true){
            address=Model.userDetail.getAddress();
        }else{
            address="";
        }

    }

    private TextView tv_Role,tv_Nickname,tv_mail,tv_qq,tv_wechat,tv_phone,tv_name,tv_sex,tv_address,tv_age,tv_birth;
    private ImageView iv_head,iv_go,iv_nickIsneed,iv_headIsneed,iv_mailIsneed, iv_qqIsneed,iv_wechatIsneed,iv_phoneIsneed;
    private  ImageView iv_ageIsneed,iv_sexIsneed,iv_addressIsneed,iv_birthIsneeed;
    private EditText edit_content;

    public void Initview() {
        tv_Role=(TextView)findViewById(R.id.tv_Role);
        tv_Nickname=(TextView)findViewById(R.id.tv_Nickname);
        tv_mail=(TextView)findViewById(R.id.tv_mail);
        tv_qq=(TextView)findViewById(R.id.tv_qq);
        tv_wechat=(TextView)findViewById(R.id.tv_Wechat);
        tv_phone=(TextView)findViewById(R.id.tv_phone);
        tv_sex=(TextView)findViewById(R.id.tv_sex);
        tv_age=(TextView)findViewById(R.id.tv_age);

        iv_head=(ImageView)findViewById(R.id.iv_head);
        iv_go=(ImageView)findViewById(R.id.iv_go);

        iv_nickIsneed=(ImageView)findViewById(R.id.iv_nickIsneed);
        iv_headIsneed=(ImageView)findViewById(R.id.iv_headIsneed);
        iv_mailIsneed=(ImageView)findViewById(R.id.iv_mailIsneed);
        iv_qqIsneed=(ImageView)findViewById(R.id.iv_qqIsneed);
        iv_wechatIsneed=(ImageView)findViewById(R.id.iv_wechatIsneed);
        iv_phoneIsneed=(ImageView)findViewById(R.id.iv_phoneIsneed);
        iv_ageIsneed=(ImageView)findViewById(R.id.iv_ageIsneed);
        iv_sexIsneed=(ImageView)findViewById(R.id.iv_sexIsneed);
        iv_addressIsneed=(ImageView)findViewById(R.id.iv_addressIsneed);


        setUserinfo();

    }
    public  void  setUserinfo(){
        String Role;
        switch(Model.getRole()){
            case 3:Role="客服";break;
            case 2:Role="普通管理员";break;
            case 1:Role="超级管理员";break;
            case 4:Role="普通用户";break;
            default:Role="";
        }

        tv_Role.setText(Role);
        tv_mail.setText(Model.getUserDetail().getMail());
        tv_Nickname.setText(Model.getnickName());
        tv_qq.setText(Model.getUserDetail().getQQ());
        tv_wechat.setText(Model.getUserDetail().getWechat());
        tv_phone.setText(Model.getUserDetail().getPhone());
        System.out.println("性别："+Model.getUserDetail().getSex());
        tv_sex.setText(Model.getUserDetail().getSex());
        //tv_address.setText(Model.getUserDetail().getAddress());
        tv_age.setText(String.valueOf(Model.getUserDetail().getAge()));

        if(bitmap!=null){
            iv_head.setImageBitmap(bitmap);

        }else {
            if (Model.getHeadImage() != null && !Model.getHeadImage().equals("")) {
                params = Model.getHeadImage();
                Log.i(TAG,"params"+   params);
            }
            new Thread(runnable).start();
        }

        if(Model.getNickIsNeed()==true){
            iv_nickIsneed.setImageResource(R.drawable.red_start);
            iv_nickIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_nickIsneed.setVisibility(View.GONE);
        }
        if(Model.getPhotoIsNeed()==true){
            iv_headIsneed.setImageResource(R.drawable.red_start);
            iv_headIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_headIsneed.setVisibility(View.GONE);
        }
        if(Model.getmailIsNeed()==true){
            iv_mailIsneed.setImageResource(R.drawable.red_start);
            iv_mailIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_mailIsneed.setVisibility(View.GONE);
        }
        if(Model.getqqIsNeed()==true){
            iv_qqIsneed.setImageResource(R.drawable.red_start);
            iv_qqIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_qqIsneed.setVisibility(View.GONE);
        }
        if(Model.getwechatIsNeed()==true){
            iv_wechatIsneed.setImageResource(R.drawable.red_start);
            iv_wechatIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_wechatIsneed.setVisibility(View.GONE);
        }
        if(Model.getPhoneIsNeed()==true){
            iv_phoneIsneed.setImageResource(R.drawable.red_start);
            iv_phoneIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_phoneIsneed.setVisibility(View.GONE);
        }

        if(Model.getageIsNeed()==true){
            iv_ageIsneed.setImageResource(R.drawable.red_start);
            iv_ageIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_ageIsneed.setVisibility(View.GONE);
        }
        if(Model.getsexIsNeed()==true){
            iv_sexIsneed.setImageResource(R.drawable.red_start);
            iv_sexIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_sexIsneed.setVisibility(View.GONE);
        }
        if(Model.getAddressIsNeed()==true){
            iv_addressIsneed.setImageResource(R.drawable.red_start);
            iv_addressIsneed.setVisibility(View.VISIBLE);
        }else{
            iv_addressIsneed.setVisibility(View.GONE);
        }



    }

    Runnable runnable =  new Runnable() {

        @Override
        public void run() {
            try {
                if(params==null){
                    return;
                }
                if(params.equals("")){
                  Log.w(TAG,"该用户的头像出现错误，路径为"+params );
                    return;
                }
                URL url = new URL(params);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                // 设置参数
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.connect();
                // 获得响应码
                int code = conn.getResponseCode();
                 System.out.println(code);
                if (code == 200) {

                    // 相应成功,获得网络返回来的输入流
                    InputStream is = conn.getInputStream();
                    // 图片的输入流获取成功之后，设置图片的压缩参数,将图片进行压缩
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    // 将图片的宽高都压缩为原来的一半,在开发中此参数需要根据图片展示的大小来确定,否则可能展示的不正常
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    // 这个压缩的最小
                    bitmap = BitmapFactory.decodeStream(is);
                    // Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                    //经过压缩的图片
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }



            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    };

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.head:
                mulDialog(this);
                break;
            case R.id.nick:
                modifyNicknameDialog(this, tv_Nickname);
                break;
            case R.id.mail:
                modifyMailiDialog(this,tv_mail);
                break;
            case R.id.qq:
                modifyQQDialog(this,tv_qq);
                break;
            case R.id.wechat:
                modifyWechatDialog(this,tv_wechat);
                break;
            case R.id.phone:
                modifyPhoneDialog(this,tv_phone);
                break;
            case R.id.customAttr:
                Intent intent=new Intent(UserInfoActivity.this,CuttomAttrActivity.class);
                startActivity(
                        intent);
                break;
            case R.id.age:
                  modifyAgeDialog(this,tv_age);
                  break;
            case R.id.address:

                modifyAddressDialog(this,address);
                break;
            case R.id.sex:
                sexDialog(this,tv_sex);
                  break;
            case R.id.iv_address:
                new AlertDialog.Builder(this)
                        .setTitle("地址详情")
                        .setMessage(address)
                        .show();


                  break;
        }
    }
    public  void  sexDialog(Context context, final TextView tv_sex){
        final String[] sexArry = new String[] { "男", "女" };//性别选择
        int sex=0;
       switch (tv_sex.getText().toString()){
           case "男":sex=0;break;
           case "女":sex=1;break;

           default:break;

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);// 自定义对话框
        builder.setSingleChoiceItems(sexArry, sex, new DialogInterface.OnClickListener() {// 2默认的选中

            @Override
            public void onClick(DialogInterface dialog, int which) {// which是被选中的位置

                tv_sex.setText(sexArry[which]);

                dialog.dismiss();//随便点击一个item消失对话框，不用点击确认取消
            }
        });
        builder.show();// 让弹出框显示
    }
    private  String   uploadBitmapName=null;
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {

        switch (requestCode) {
            case CODE_GALLERY_REQUEST://如果是来自本地的
                if(resultCode!=RESULT_OK) break;
                uploadBitmapName= Model.getUsername();
                cropRawPhoto(intent.getData(), uploadBitmapName);//直接裁剪图片
                break;
            case SELECT_PIC_KITKAT:
                if(resultCode!=RESULT_OK) break;
                uploadBitmapName= Model.getUsername();
                cropRawPhoto(intent.getData(), uploadBitmapName);//直接裁剪图片
                                    break;

            case CODE_CAMERA_REQUEST:
                if(resultCode!=RESULT_OK) break;
                if (hasSdcard()) {
                    File tempFile = new File(
                            Environment.getExternalStorageDirectory(),
                            IMAGE_FILE_NAME);
                    uploadBitmapName= Model.getUsername();
                    cropRawPhoto(Uri.fromFile(tempFile), uploadBitmapName);
                } else {
                    Utils.showToast2(this, "未找到存储卡，无法存储照片！");
                }

                break;

            case CODE_RESULT_REQUEST:
                if(resultCode!=RESULT_OK) break;
                if (intent != null) {
                    try {
                        setImageToHeadView(intent);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        String imageUrl=UploadImage(intent);




                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    public String UploadImage(Intent intent) throws IOException {

        String imageUrl= httprequestPresenter.uploadBitmap(requestUploadURL, uritempFile.getPath());

        Model.setHeadimagePath(requestURL + "/" + Model.getUsername() + ".jpg");
        return imageUrl;
    }
    public  void  mulDialog(Context context){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);


        final String[] item = {"拍照","从相册选择"};
        //    设置一个下拉的列表选择项
        builder.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        choseHeadImageFromCameraCapture();
                        break;
                    case 1:
                        choseHeadImageFromGallery();
                        break;


                }
            }
        });
        builder.show();
    }
    // 从本地相册选取图片作为头像
    private void choseHeadImageFromGallery() {
        Intent intentFromGallery = new Intent();
        intentFromGallery.setType("image/*");//选择图片
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                            startActivityForResult(intentFromGallery,SELECT_PIC_KITKAT);
        } else {
                                             startActivityForResult(intentFromGallery,CODE_GALLERY_REQUEST);
        }

       // startActivityForResult(intentFromGallery, CODE_GALLERY_REQUEST);
    }
    // 启动手机相机拍摄照片作为头像
    private void choseHeadImageFromCameraCapture() {
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 判断存储卡是否可用，存储照片文件
        if (hasSdcard()) {
            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri
                    .fromFile(new File(Environment
                            .getExternalStorageDirectory(), IMAGE_FILE_NAME)));
        }

        startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);
    }





    /**
     * 裁剪原始的图片
     */
    public void cropRawPhoto(Uri uri,String photoName) {
        if (uri == null) {
                   Log.i("tag", "The uri is not exist.");
                      return;
                }

        Intent intent = new Intent("com.android.camera.action.CROP");
      //  intent.setDataAndType(uri, "image/*");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                          String url=getPath(this,uri);
            intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
            System.out.println(Uri.fromFile(new File(url)));
                      }else{

                        intent.setDataAndType(uri, "image/*");
                      }

        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", output_X);
        intent.putExtra("outputY", output_Y);
        uritempFile = Uri.parse("file://" + "/" + Environment.getExternalStorageDirectory().getPath() + "/" + photoName + ".jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uritempFile);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CODE_RESULT_REQUEST);
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

               final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

                 // DocumentProvider
                if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                          // ExternalStorageProvider
                         if (isExternalStorageDocument(uri)) {
                                    final String docId = DocumentsContract.getDocumentId(uri);
                                  final String[] split = docId.split(":");
                                   final String type = split[0];

                                   if ("primary".equalsIgnoreCase(type)) {
                                           return Environment.getExternalStorageDirectory() + "/" + split[1];
                                      }

                             }
                            // DownloadsProvider
                            else if (isDownloadsDocument(uri)) {
                               final String id = DocumentsContract.getDocumentId(uri);
                                final Uri contentUri = ContentUris.withAppendedId(
                                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                                  return getDataColumn(context, contentUri, null, null);
                              }
                           // MediaProvider
                          else if (isMediaDocument(uri)) {
                                  final String docId = DocumentsContract.getDocumentId(uri);
                                    final String[] split = docId.split(":");
                                   final String type = split[0];

                                  Uri contentUri = null;
                                 if ("image".equals(type)) {
                                         contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                                     } else if ("video".equals(type)) {
                                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                                        } else if ("audio".equals(type)) {
                                          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                      }

                                 final String selection = "_id=?";
                                final String[] selectionArgs = new String[] {
                                                split[1]
                                         };

                                  return getDataColumn(context, contentUri, selection, selectionArgs);
                              }
                      }
               // MediaStore (and general)
               else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        // Return the remote address
                        if (isGooglePhotosUri(uri))
                                  return uri.getLastPathSegment();

                        return getDataColumn(context, uri, null, null);
                     }
                   // File
                   else if ("file".equalsIgnoreCase(uri.getScheme())) {
                          return uri.getPath();
                  }

                    return null;
               }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                                  String[] selectionArgs) {

                  Cursor cursor = null;
                 final String column = "_data";
               final String[] projection = {
                                  column
                       };

                 try {
                         cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                                        null);
                         if (cursor != null && cursor.moveToFirst()) {
                              final int index = cursor.getColumnIndexOrThrow(column);
                               return cursor.getString(index);
                              }
                     } finally {
                         if (cursor != null)
                                 cursor.close();
                     }
                 return null;
             }


                /**
                 * @param uri The Uri to check.
                 * @return Whether the Uri authority is ExternalStorageProvider.
                 */
                   public static boolean isExternalStorageDocument(Uri uri) {
                  return "com.android.externalstorage.documents".equals(uri.getAuthority());
            }

                  /**
                   * @param uri The Uri to check.
                   * @return Whether the Uri authority is DownloadsProvider.
                    */
                  public static boolean isDownloadsDocument(Uri uri) {
                  return "com.android.providers.downloads.documents".equals(uri.getAuthority());
                }

                  /**
                    * @param uri The Uri to check.
                    * @return Whether the Uri authority is MediaProvider.
                   */
                  public static boolean isMediaDocument(Uri uri) {
                   return "com.android.providers.media.documents".equals(uri.getAuthority());
                }

                    /**
     277.         * @param uri The Uri to check.
     278.         * @return Whether the Uri authority is Google Photos.
     279.         */
                  public static boolean isGooglePhotosUri(Uri uri) {
                  return "com.google.android.apps.photos.content".equals(uri.getAuthority());
               }


    /**
     * 提取保存裁剪之后的图片数据，并设置头像部分的View
     */
    private void setImageToHeadView(Intent intent) throws FileNotFoundException {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uritempFile));
            iv_head.setImageBitmap(bitmap);
        }
    }
    /**
     * 检查设备是否存在SDCard的工具方法
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 有存储的SDCard
            return true;
        } else {
            return false;
        }
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    //关闭
                    ImageView view=(ImageView)findViewById(R.id.iv_head);
                    view.setImageBitmap(bitmap);

                    break;
            }
        }
    };
    private Button btn_ok;
    public void modifyAddressDialog(Context context,String myaddress) {

        dialog = new ModifyDialog(context, myaddress);
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();
        edit_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});  //其中15最大输入字数
        tv_name.setText("修改地址");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(""))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );

        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                address=edit_content.getText().toString();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    public void modifyAgeDialog(Context context, final TextView tv_age) {

        dialog = new ModifyDialog(context,tv_age.getText().toString());
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();
        edit_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});  //其中15最大输入字数
        tv_name.setText("修改年龄");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(tv_age.getText().toString()))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );

        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {


                tv_age.setText(edit_content.getText().toString());

                dialog.dismiss();
            }
        });

        dialog.show();
    }
    public void modifyNicknameDialog(Context context, final TextView tv_Nicknamen) {

        dialog = new ModifyDialog(context,tv_Nicknamen.getText().toString());
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();
        edit_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});  //其中15最大输入字数
        tv_name.setText("修改昵称");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(tv_Nicknamen.getText().toString()))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );

        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                System.out.println("昵称：" + edit_content.getText().toString());
                tv_Nicknamen.setText(edit_content.getText().toString());

                dialog.dismiss();
            }
        });

        dialog.show();

    }
    public void modifyMailiDialog(Context context, final TextView tv_mail) {

        dialog = new ModifyDialog(context,tv_mail.getText().toString());
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();

        tv_name.setText("修改邮箱");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(tv_mail.getText().toString()))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );



        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if(isEmail(edit_content.getText().toString())==true){
                    tv_mail.setText(edit_content.getText().toString());

                    dialog.dismiss();
                }else{
                    Utils.showToast2(UserInfoActivity.this, "格式错误");
                }

            }
        });

        dialog.show();

    }
    /**
     * 判断邮箱是否合法
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;

        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public void modifyQQDialog(Context context, final TextView tv_qq) {

        dialog = new ModifyDialog(context,tv_qq.getText().toString());
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();

        tv_name.setText("修改qq账号");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(tv_qq.getText().toString()))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );



        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if(isNumeric(edit_content.getText().toString())){
                    tv_qq.setText(edit_content.getText().toString());
                    dialog.dismiss();
                }else{
                    Utils.showToast2(UserInfoActivity.this, "格式错误");
                }

            }
        });

        dialog.show();

    }
    /**
     * 判断字符串是否为全数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
    public void modifyWechatDialog(Context context, final TextView tv_wechat) {

        dialog = new ModifyDialog(context,tv_wechat.getText().toString());
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();

        tv_name.setText("修改微信账号");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(tv_wechat.getText().toString()))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );


        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                tv_wechat.setText(edit_content.getText().toString());

                dialog.dismiss();


            }
        });

        dialog.show();

    }
    public void modifyPhoneDialog(Context context, final TextView tv_phone) {

        dialog = new ModifyDialog(context,tv_phone.getText().toString());
        tv_name  = dialog.getnameText();
        edit_content = dialog.getcontentText();
        tv_name.setText("修改手机号码");
        btn_ok=dialog.getBtn_ok();
        if(edit_content.getText().toString().equals(tv_phone.getText().toString()))   //初次进来为空的时候，就设置按钮为不可点击
            btn_ok.setEnabled(false);
        edit_content.addTextChangedListener(//设置编辑栏的文字输入监听
                new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable arg0) {
                        if (arg0.toString().equals("")) {  //当编辑栏为空的时候，将按钮设置为不可点击。
                            btn_ok.setEnabled(false);
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }
                }
        );

        dialog.setOnClickCommitListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if(checkPhone( edit_content.getText().toString())){
                    tv_phone.setText( edit_content.getText().toString());
                    dialog.dismiss();
                }else{
                    Utils.showToast2(UserInfoActivity.this, "格式错误");
                }



            }
        });

        dialog.show();

    }
    /**
     * 判断是否是手机号
     * @param phone
     * @return
     */
    public static boolean checkPhone(String phone) {
        Pattern pattern = Pattern
                .compile("^(13[0-9]|15[0-9]|153|15[6-9]|180|18[23]|18[5-9])\\d{8}$");
        Matcher matcher = pattern.matcher(phone);

        if (matcher.matches()) {
            return true;
        }
        return false;
    }
    public void setModel(){

        //Model.setUser(Model.getUsername(), Model.getPwb(), tv_Nickname.getText().toString(), Model.getHeadImage());
        Model.setUickName(tv_Nickname.getText().toString());
        Model.setHeadimagePath(Model.getHeadImage());


        ProtoClass.UserDetail.Builder mmbuilder = ProtoClass.UserDetail.newBuilder();
        Log.i("UserInforActivity,年龄",tv_age.getText().toString());
        int age=Integer.parseInt(tv_age.getText().toString());
        System.out.println(age);
        mmbuilder.setUID(Model.getID())
                .setMail(tv_mail.getText().toString())
                .setQQ(tv_qq.getText().toString())
                .setWechat(tv_wechat.getText().toString())
                .setPhone(tv_phone.getText().toString())
                .setAge(age)
                .setAddress(address)
                .setSex(tv_sex.getText().toString());
        System.out.println("年龄，地址");
        System.out.println(age);
        System.out.println(address);

        ProtoClass.UserDetail detail = mmbuilder.build();
        Model.setDetail(detail);

    }
    public void SaveData() {
        int age=Integer.parseInt(tv_age.getText().toString());


        ProtoClass.Msg.Builder builder = ProtoClass.Msg.newBuilder();
        ProtoClass.User.Builder mbuilder = ProtoClass.User.newBuilder();
        ProtoClass.UserDetail.Builder mmbuilder = ProtoClass.UserDetail.newBuilder();
        ProtoClass.UserCustomAttr.Builder attrbuilder = ProtoClass.UserCustomAttr.newBuilder();
        mbuilder.setNickName(tv_Nickname.getText().toString());
        mmbuilder.setMail(tv_mail.getText().toString());
        mmbuilder.setQQ(tv_qq.getText().toString());
        mmbuilder.setWechat(tv_wechat.getText().toString());
        mmbuilder.setPhone(tv_phone.getText().toString())
                .setAge(age)
                .setAddress(address)
                .setSex(tv_sex.getText().toString());
        if(!Model.getHeadImage().equals("")&&Model.getHeadImage()!=null) {
            mbuilder.setIconName(requestURL + "/" + Model.getUsername() + ".jpg");
        }
        //    Model.setUser(Model.getUsername(), Model.getPwb(), tv_Nickname.getText().toString(), Model.getHeadImage());
        Model.setUickName(tv_Nickname.getText().toString());
        Model.setHeadimagePath(Model.getHeadImage());


        if(Model.getCustomAttr()!=null){
            mmbuilder.setCustomAttr(Model.getCustomAttr())
                    .setUID(Model.getID());
        }else{
            attrbuilder.setUserID(Model.getID());
            ProtoClass.UserCustomAttr  attr= attrbuilder.build();
            mmbuilder.setCustomAttr(attr)
                    .setUID(Model.getID());
        }

        ProtoClass.UserDetail detail = mmbuilder.build();
        Model.setDetail(detail);

        mbuilder.setUserID(Model.getID())
                .setUserName(Model.getUsername())
                .setUserDetail(detail);
        ProtoClass.User muser = mbuilder.build();
        builder.setMsgType(ProtoClass.MsgType.SAVE_ATTR)
                .setUser(muser)
                .setToken(ImApplication.getLoginToken())
                .setAccount(Model.getUsername());
        Tools.startTcpRequest(builder, new Tools.TcpListener() {
            @Override
            public void onSendFail(Exception e) {
                Utils.showToast2(UserInfoActivity.this, "请求发送失败");
            }

            //第二个参数是服务器返回的响应消息
            @Override
            public boolean onResponse(String msgId, ProtoClass.Msg responseMsg) {
                msaveAttrPresenter.onProcess(responseMsg);
                return true;  //返回false代表响应消息在这里处理完之后会被传递到监听列表中轮询判断，true代表这里处理完之后就结束了
            }
        });
    }
    public boolean getAttrType(){

        int i;
      //  boolean[] attrType=new  boolean[10];
        boolean[] result=new  boolean[10];
        for(i = 0 ; i < 10 ; i++) {

            result[i]=true;
        }

        for(i = 0 ; i < 10 ; i++) {
           switch (i){
               case 0:if(Model.getNickIsNeed()==true&&tv_Nickname.getText().toString().equals("")){
                       alertOfTip("请填昵称");result[0]=false;}else{result[0]=true;}break;
               case 1:if(Model.getPhotoIsNeed()==true&&bitmap!=null){
                   alertOfTip("请上传头像");result[1]=false;}else{result[1]=true;}break;
               case 2:if(Model.getPhoneIsNeed()==true&&tv_phone.getText().toString().equals("")){
                   alertOfTip("请填手机号码");result[2]=false;}else{result[2]=true;}break;
             case 3:if(Model.getAddressIsNeed()==true&&address.equals("")){
               alertOfTip("请填地址");result[3]=false;}else{result[3]=true;}break;
             case 4:if(Model.getageIsNeed()==true&&tv_age.getText().toString().equals("")){
                 alertOfTip("请填年龄");result[4]=false;}else{result[4]=true;}break;
           case 5:if(Model.getsexIsNeed()==true&&tv_sex.getText().toString().equals("")){
                 alertOfTip("请填性别");result[5]=false;}else{result[5]=true;}break;
             //  case 6:if(Model.getbirthIsNeed()==true&&tv_brith.getText().toString().equals("")){
            //       alertOfTip("请填生日");result[6]=false;}else{result[6]=true;}break;
               case 7:if(Model.getmailIsNeed()==true&&tv_mail.getText().toString().equals("")){
                   alertOfTip("请填邮箱");result[7]=false;}else{result[7]=true;}break;
               case 8:if(Model.getqqIsNeed()==true&&tv_qq.getText().toString().equals("")){
                   alertOfTip("请填qq");result[8]=false;}else{result[8]=true;}break;
               case 9:if(Model.getwechatIsNeed()==true&&tv_wechat.getText().toString().equals("")){
                   alertOfTip("请填微信");result[9]=false;}else{result[9]=true;}break;
           }
        }
        for(i = 0 ; i < 10 ; i++) {
           if( result[i]==false){
               return false;
           }
        }
        return true;
    }
    public void alertOfTip(String tip){
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(tip)
                .show();

        }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(getAttrType()==false){
            return;
        }
        setModel();
        SaveData();
        finish();

    }
}
