package com.cst14.im.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.listener.SaveAttrPresenter;
import com.cst14.im.protobuf.ProtoClass;
import com.cst14.im.tools.Tools;
import com.cst14.im.utils.ImApplication;
import com.cst14.im.utils.Model;
import com.cst14.im.utils.Utils;
import com.cst14.im.utils.fileUtils.FileUtils;
import com.cst14.im.utils.fileUtils.UploadFile;
import com.cst14.im.utils.pictureUtils.LocalCacheUtils;
import com.cst14.im.utils.pictureUtils.httprequestPresenter;
import com.cst14.im.utils.sessionUtils.ChatMsgBean;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModHeadImageActivity extends AppCompatActivity  {

}
