package com.weiyu.jigsawpuzzle.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.weiyu.jigsawpuzzle.R;
import com.weiyu.jigsawpuzzle.adapter.GridItemsAdapter;
import com.weiyu.jigsawpuzzle.bean.ItemBean;
import com.weiyu.jigsawpuzzle.util.GameUtils;
import com.weiyu.jigsawpuzzle.util.ImageUtils;
import com.weiyu.jigsawpuzzle.util.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PuzzleMain extends AppCompatActivity implements View.OnClickListener {

    // 拼图完成时显示的最后一个图片
    public static Bitmap mLastBitmap;
    // 设置为N*N显示
    public static int TYPE = 2;
    // 步数显示
    public static int COUNT_INDEX = 0;
    // 计时显示
    public static int TIMER_INDEX = 0;

    /**
     * UI更新Handler
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 更新计时器
                    TIMER_INDEX++;
                    mTvTimer.setText("" + TIMER_INDEX);
                    break;
                default:
                    break;
            }
        }
    };
    // 选择的图片
    private Bitmap mPicSelected;
    // PuzzlePanel
    private GridView mGvPuzzleMainDetail;
    private int mResId;
    private String mPicPath;
    private ImageView mImageView;
    // Button
    private Button mBtnBack;
    private Button mBtnImage;
    private Button mBtnRestart;
    // 显示步数
    private TextView mTvPuzzleMainCounts;
    // 计时器
    private TextView mTvTimer;
    // 切图后的图片
    private List<Bitmap> mBitmapItemLists = new ArrayList<Bitmap>();
    // GridView适配器
    private GridItemsAdapter mAdapter;
    // Flag 是否已显示原图
    private boolean mIsShowImg;
    // 计时器类
    private Timer mTimer;
    /**
     * 计时器线程
     */
    private TimerTask mTimerTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_main);
        //获取选择的图片
        Bitmap picSelectedTemp;
        //选择默认图片还是自定义图片
        mResId = getIntent().getExtras().getInt("picSelectedID");
        mPicPath = getIntent().getExtras().getString("mPicPath");
        if(mResId != 0 ){
            picSelectedTemp = BitmapFactory.decodeResource(getResources(),mResId);
        }else{
            picSelectedTemp = BitmapFactory.decodeFile(mPicPath);
        }
        TYPE = getIntent().getExtras().getInt("mType",2);
        //对图片处理
        handlerImage(picSelectedTemp);
        //初始化views
        initViews();
        //生成游戏数据
        generateGame();
        //GridView点击事件
        mGvPuzzleMainDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //判断是否可以移动
                if(GameUtils.isMoveable(position)){
                    //交换点击Item与空格的位置
                    GameUtils.swapItems(GameUtils.mItemBeens.get(position),GameUtils.mBlankItemBean);
                    //重新获取图片
                    recreateData();
                    //通知GridView更改UI
                    mAdapter.notifyDataSetChanged();
                    //更新步数
                    COUNT_INDEX++;
                    mTvPuzzleMainCounts.setText(""+COUNT_INDEX);
                    //判断是否成功
                    if(GameUtils.isSuccess()){
                        //将最后一张图显示完整
                        recreateData();
                        mBitmapItemLists.remove(TYPE * TYPE -1);
                        mBitmapItemLists.add(mLastBitmap);
                        //通知GridView更改UI
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(PuzzleMain.this, "拼图成功", Toast.LENGTH_SHORT).show();
                        mGvPuzzleMainDetail.setEnabled(false);
                        mTimer.cancel();
                        mTimerTask.cancel();
                    }
                }
            }
        });
        //返回按钮点击事件
        mBtnBack.setOnClickListener(this);
        //显示原图按钮点击事件
        mBtnImage.setOnClickListener(this);
        //重置按钮点击事件
        mBtnRestart.setOnClickListener(this);
    }

    /**
     * 重新获取图片
     */
    private void recreateData() {
        mBitmapItemLists.clear();
        for (ItemBean temp : GameUtils.mItemBeens) {
            mBitmapItemLists.add(temp.getmBitmap());
        }
    }

    /**
     * 生成游戏数据
     */
    private void generateGame() {
        //切图   获取初始拼图数据  正常顺序
        new ImageUtils().createInitBitmaps(TYPE,mPicSelected,PuzzleMain.this);
        //生成随机数据
        GameUtils.getPuzzleGenerator();
        //获取Bitmap集合
        for (ItemBean temp: GameUtils.mItemBeens) {
            mBitmapItemLists.add(temp.getmBitmap());
        }
        //数据适配器
        mAdapter = new GridItemsAdapter(this,mBitmapItemLists);
        mGvPuzzleMainDetail.setAdapter(mAdapter);
        //启用计时器
        mTimer = new Timer(true);
        //计时器线程
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what  = 1;
                mHandler.sendMessage(message);
            }
        };
        //每1000ms执行，延迟0秒
        mTimer.schedule(mTimerTask,0,1000);
    }

    /**
     * 初始化views
     */
    private void initViews() {
        //Button
        mBtnBack = (Button) findViewById(R.id.btn_puzzlemain_back);
        mBtnImage = (Button) findViewById(R.id.btn_puzzlemain_img);
        mBtnRestart = (Button) findViewById(R.id.btn_puzzlemain_reset);
        //Flag 是否已显示原图
        mIsShowImg = false;
        //GridView
        mGvPuzzleMainDetail = (GridView) findViewById(R.id.gv_puzzlemain_detail);
        //设置为N*N显示
        mGvPuzzleMainDetail.setNumColumns(TYPE);
        RelativeLayout.LayoutParams gridParams = new RelativeLayout.LayoutParams(
                mPicSelected.getWidth(),mPicSelected.getHeight());
        //水平居中
        gridParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        //其他格式属性
        gridParams.addRule(RelativeLayout.BELOW,R.id.ll_puzzlemain_spinner);
        //Grid显示
        mGvPuzzleMainDetail.setLayoutParams(gridParams);
        mGvPuzzleMainDetail.setHorizontalSpacing(0);
        mGvPuzzleMainDetail.setVerticalSpacing(0);
        //textview步数
        mTvPuzzleMainCounts = (TextView) findViewById(R.id.tv_puzzlemain_counts);
        mTvPuzzleMainCounts.setText(""+COUNT_INDEX);
        //textview计时器
        mTvTimer = (TextView) findViewById(R.id.tv_puzzlemain_time);
        mTvTimer.setText("0秒");
        //添加新式原图的View
        addImgView();

    }

    /**
     * 添加显示原图的View
     */
    private void addImgView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_puzzlemain_mainlayout);
        mImageView = new ImageView(PuzzleMain.this);
        mImageView.setImageBitmap(mPicSelected);
        int x = (int) (mPicSelected.getWidth() * 0.9f);
        int y = (int) (mPicSelected.getHeight() *0.9f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(x,y);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mImageView.setLayoutParams(params);
        relativeLayout.addView(mImageView);
        mImageView.setVisibility(View.GONE);
    }

    /**
     * 对图片处理   自适应大小
     *
     * @param bitmap
     */
    private void handlerImage(Bitmap bitmap) {
        //将图片放大到固定尺寸
        int screenWidth = ScreenUtils.getScreenSize(this).widthPixels;
        int screenHeight = ScreenUtils.getScreenSize(this).heightPixels;
        mPicSelected = new ImageUtils().resizeBitmap(screenWidth*0.8f,screenHeight*0.6f,bitmap);
    }

    /**
     * Button 点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_puzzlemain_back:
                PuzzleMain.this.finish();
                break;
            case R.id.btn_puzzlemain_img:
                Animation animShow = AnimationUtils.loadAnimation(PuzzleMain.this,R.anim.image_show_anim);
                Animation animHide = AnimationUtils.loadAnimation(PuzzleMain.this,R.anim.image_hide_anim);
                if(mIsShowImg){
                    mImageView.startAnimation(animHide);
                    mImageView.setVisibility(View.GONE);
                    mIsShowImg = false;
                }else {
                    mImageView.startAnimation(animShow);
                    mImageView.setVisibility(View.VISIBLE);
                    mIsShowImg = true;
                }
                break;
            case R.id.btn_puzzlemain_reset:
                clearConfig();
                generateGame();
                recreateData();
                mTvPuzzleMainCounts.setText(""+COUNT_INDEX);
                mAdapter.notifyDataSetChanged();
                mGvPuzzleMainDetail.setEnabled(true);
                break;
            default:
                break;
        }
    }

    /**
     * 清空相关参数设置
     */
    private void clearConfig() {
        GameUtils.mItemBeens.clear();
        //停止计时器
        mTimer.cancel();
        mTimerTask.cancel();
        COUNT_INDEX = 0;
        TIMER_INDEX = 0;
        //清除拍摄的照片
        if(mPicPath != null){
            //删除照片
            File file = new File(MainActivity.TEMP_IMAGE_PATH);
            if(file.exists())
                file.delete();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //清空相关设置
        clearConfig();
        this.finish();
    }
}
