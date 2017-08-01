package com.cst14.im.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cst14.im.utils.ImApplication;

/**
 * Created by MRLWJ on 2016/9/6.
 * 操作数据
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DB = ImApplication.User_id + "142857DB";//数据库名字  ID+142857  每个用户一个数据库

    public DBHelper(Context context) {
        super(context, DB, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table session_holders(\n" +
                "\tid int primary key,\n" +
                "\tmy_id varchar(10),\n" +
                "\tfriend_id varchar(10),\n" +
                "\tfix_top int,\n" +
                "\tno_tip int,\n" +
                "\tunread_msg_count int,\n" +
                "\tlast_brif_msg varchar(30) default \"\",\n" +
                "\tlast_brif_msg_time varchar(20) default \"\"\n" +
                ");");
        db.execSQL("CREATE INDEX idx ON session_holders(my_id,friend_id);");
        db.execSQL("create table msg_data(\n" +
                "\tid int primary key,\n" +
                "\tmy_id varchar(10),\n" +
                "\tfriend_id varchar(10),\n" +
                "\tis_mine_msg bool,\n" +
                "\ttext_content text,\n" +
                "\tis_readed int,\n" +
                "\tdata_type int not null,\n" +
                "\tmsg_id varchar(30) not null,\n" +
                "\tmsg_send_finished int,\n" +
                "\tsent_time varchar(20) default \"\",\n" +
                "\tread_time varchar(20) default \"\",\n" +
                "\tthumb_finger_print varchar(30),\n" +
                "\tfile_name varchar(30),\n" +
                "\tfile_size varchar(10),\n" +
                "\tforeign key(my_id,friend_id) references session_holders(my_id,friend_id) on delete cascade on update cascade\n" +
                ");");


//好友分组、好友列表
        db.execSQL("create table if not exists friendGroup(\n" +
                "id int primary key,UID varchar(10),\n" +
                "listNO int default 0,\n" +
                "listName varchar(20)\n" +
                ");\n");
        db.execSQL("create table if not exists friends(\n" +
                "\tid int primary key,\t\n" +
                "\townerID varchar(10),\t\t\n" +
                "\tfriendID varchar(10),\t\n" +
                "\taddTime timestamp,\n" +
                "\t\n" +
                "\tNick\tvarchar(20),\n" +
                "\n" +
                "\tremark varchar(20),\n" +
                "\t\t\n" +
                "\tlistNO int DEFAULT 0\t\t\n" +
                ");");
        db.execSQL("CREATE INDEX idx_friends ON friends(ownerID,friendID);");

        db.execSQL("create table if not exists friendRequest(\n" +
                "\tid int primary key,\n" +
                "\tfriendID varchar(10),\n" +
                "\townerID varchar(10),\n" +
                "\tNick varchar(10),\n" +
                "\tisAgree int DEFAULT 0\n" +
                ");");
        db.execSQL("CREATE INDEX idx_friend_request ON friendRequest(friendID,ownerID);");

        //群聊天记录
        db.execSQL("create table if not exists group_msg(" +
                "  msgid integer primary key autoincrement" +
                ", group_id int not null" +
                ", send_id  varchar(15) not null" +
                ", msg_type varchar(10) not null" +
                ", str_content text" +
                ", file_path varchar(60)" +
                ", file_name varchar(30)" +
                ", file_size varchar(10)" +
                ", thumb_finger_print varchar(30)" +
                ", voice_time int" +
                ", msg_unique_tag varchar(10) not null" +
                ", send_time varchar(15)" +
                ", read_time varchar(15)" +
                ", is_send_ok int default 0" +
                ", is_read int not null default 0" +
                ");");
        db.execSQL("CREATE INDEX idx_group_msg ON group_msg(group_id);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE session_holders ADD COLUMN other VARCHAR");//......
        db.execSQL("ALTER TABLE msg_data ADD COLUMN other VARCHAR");//......
        db.execSQL("ALTER TABLE friendGroup ADD COLUMN other VARCHAR");//......
        db.execSQL("ALTER TABLE friends ADD COLUMN other VARCHAR");//......
        db.execSQL("ALTER TABLE friendRequest ADD COLUMN other VARCHAR");//......
    }
}
