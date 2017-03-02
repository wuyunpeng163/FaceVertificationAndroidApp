package com.example.wuyunpeng.facevertificationultimate;

/**
 * Created by 12282 on 2016/10/31.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/************************************************************************
 *                        人脸认证系统App登录主界面                     *
 ************************************************************************/
public class AppMainActivity extends Activity {
    /**
     * 成员定义部分
     */
    private Button loginButton = null;//登陆按键
    private ImageView backgroundImageView = null;//背景图片显示控件
    private Bitmap backgroundBitmap = null;//背景图片
    private Bitmap nameBitmap = null;//背景图片
    private Bitmap idBitmap = null;//背景图片
    private Bitmap ipBitmap = null;//
    private EditText idText = null;//用户Id号
    private EditText nameText = null;//用户姓名
    private EditText ipText = null;//设定服务器ip地址
    private ImageView nameImageView = null;
    private ImageView idImageView = null;
    private ImageView ipImageView = null;
    private CircleImageButton nameCancleImageButton = null;//姓名取消圆形按钮
    private CircleImageButton idCancleImageButton = null;//id取消圆形按钮
    private CircleImageButton ipCancleImageButton = null;//ip取消圆形按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main_layout);
        this.nameCancleImageButton = (CircleImageButton)findViewById(R.id.NameCircleImageButton);
        this.idCancleImageButton = (CircleImageButton)findViewById(R.id.IdCircleImageButton);
        this.ipCancleImageButton = (CircleImageButton)findViewById(R.id.IpCircleImageButton);
        this.loginButton = (Button)findViewById(R.id.Login_Button);
        this.backgroundImageView = (ImageView)findViewById(R.id.Main_ImageView);
        this.nameImageView = (ImageView)findViewById(R.id.NameImageView);
        this.idImageView = (ImageView)findViewById(R.id.IdImageView);
        this.ipImageView = (ImageView)findViewById(R.id.IPImageView);
       //this.backgroundBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.face);
        this.nameBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.user);
        this.idBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.password);
        this.ipBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ip);
       // backgroundImageView.setImageBitmap(backgroundBitmap);
        nameImageView.setImageBitmap(nameBitmap);
        idImageView.setImageBitmap(idBitmap);
        ipImageView.setImageBitmap(ipBitmap);
        this.idText = (EditText) findViewById(R.id.ClientID_EditText);
        this.nameText = (EditText) findViewById(R.id.ClientName_EditText);
        this.ipText = (EditText) findViewById(R.id.IP_EditText);
        nameCancleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameText.setText("");
            }
        });
        idCancleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idText.setText("");
            }
        });
        ipCancleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipText.setText("");
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = "4202221993081300";//暂定的身份证前缀
                idStr = idStr + idText.getText().toString();//输入后的完整身份证号
                String ipStr = ipText.getText().toString();//获取输入的ip地址
                //这里写登入时启动的活动,以及进行边界条件的判定
                if (idText.getText().toString().isEmpty() || nameText.getText().toString().isEmpty())
                    Toast.makeText(AppMainActivity.this,"还没有输入id或者姓名",Toast.LENGTH_SHORT).show();
                    //身份证正则表达式的判定
                else if(idStr.matches("([\\d]{17}(\\d|x|X))") == false)
                    Toast.makeText(AppMainActivity.this,"输入id格式不正确",Toast.LENGTH_SHORT).show();
                else if(ipStr.isEmpty())
                    Toast.makeText(AppMainActivity.this,"未输入ip地址",Toast.LENGTH_SHORT).show();
                else{
                    Intent intent = new Intent(AppMainActivity.this, FaceVertifactionProcessActivity.class);//登录后进入第二个认证界面
                    intent.putExtra("id", idStr);
                    intent.putExtra("name", nameText.getText().toString());
                    intent.putExtra("ip",ipStr);
                    startActivity(intent);
                }
            }
        });
        idText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        /*showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    idText.setTransformationMethod(null);
                }else{
                    idText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });*/
    }
}
