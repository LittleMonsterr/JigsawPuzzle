package com.weiyu.jigsawpuzzle.util;

import android.content.Intent;

import com.weiyu.jigsawpuzzle.activity.PuzzleMain;
import com.weiyu.jigsawpuzzle.bean.ItemBean;

import java.net.PortUnreachableException;
import java.util.ArrayList;
import java.util.List;

/**
 * 拼图工具类：实现拼图的交换与生成算法
 *
 * Created by Administrator on 2016/11/28.
 */
public class GameUtils {

    //游戏信息单元格Bean
    public static List<ItemBean> mItemBeens = new ArrayList<ItemBean>();
    //空格单元格
    public static ItemBean mBlankItemBean = new ItemBean();

    /**
     * 生成随机的Item
     */
    public static void getPuzzleGenerator(){
        int index = 0;
        //随机打乱顺序
        for (int i =  0; i<mItemBeens.size(); i++){
            index = (int) (Math.random()* PuzzleMain.TYPE * PuzzleMain.TYPE);
            swapItems(mItemBeens.get(index),GameUtils.mBlankItemBean);
        }

        List<Integer> data = new ArrayList<Integer>();
        for (int i = 0; i < mItemBeens.size(); i++) {
            data.add(mItemBeens.get(i).getmBitmapId());
        }

        //判断生成是否有解
        if(canSolve(data)){
            return;
        }else{
            getPuzzleGenerator();
        }

    }

    /**
     * 该数据是否有解
     *
     * @param data  拼图数组数据
     * @return      该数据是否有解
     */
    private static boolean canSolve(List<Integer> data) {
        //获取空格ID
        int blankId = GameUtils.mBlankItemBean.getmItemId();
        //可行性原则
        if(data.size()%2 ==1){
            return getInversions(data)%2 ==0;
        }else{
            //从下往上数，空格位于奇数行
            if(((blankId-1)/PuzzleMain.TYPE)%2 ==1){
                return getInversions(data)%2 ==0;
            }else{
                //从下往上数，空格位于偶数行
                return getInversions(data)%2 ==1;
            }
        }
    }

    /**
     * 计算倒置和算法
     *
     * @param data   拼图数组数据
     * @return  该序列的倒置和
     */
    private static int getInversions(List<Integer> data) {
        int inversions = 0;
        int inversionCount = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.size(); j++) {
                int index = data.get(i);
                if(data.get(j) != 0 && data.get(j)<index){
                    inversionCount++;
                }
            }
            inversions += inversionCount;
            inversionCount = 0;
        }
        return inversions;
    }

    /**
     * 交换空额与点击Item的位置
     *
     * @param from   交换图
     * @param blankItemBean  空白图
     */
    public static void swapItems(ItemBean from, ItemBean blankItemBean) {
        ItemBean tempItemBean = new ItemBean();
        //交换BitmapId
        tempItemBean.setmBitmapId(from.getmBitmapId());
        from.setmBitmapId(blankItemBean.getmBitmapId());
        blankItemBean.setmBitmapId(tempItemBean.getmBitmapId());
        //交换Bitmap
        tempItemBean.setmBitmap(from.getmBitmap());
        from.setmBitmap(blankItemBean.getmBitmap());
        blankItemBean.setmBitmap(tempItemBean.getmBitmap());
        //设置新的blank
        GameUtils.mBlankItemBean = from;
    }

    /**
     * 判断电机的Item是都可以移动
     *
     * @param position
     * @return
     */
    public static boolean isMoveable(int position){
        int type = PuzzleMain.TYPE;
        //获取空格Item
        int blankId = GameUtils.mBlankItemBean.getmItemId();
        //不同行相差为type
        if(Math.abs(blankId-position) ==type){
            return true;
        }
        //相同行相差为1
        if((blankId/type == position/type) && Math.abs(blankId-position)==1){
            return true;
        }
        return false;
    }

    /**
     * 是否拼图成功
     *
     * @return
     */
    public static boolean isSuccess(){
        for(ItemBean tempBeam : GameUtils.mItemBeens){
            if(tempBeam.getmBitmapId() != 0 && (tempBeam.getmItemId())==tempBeam.getmBitmapId()){
                continue;
            }else if(tempBeam.getmBitmapId()==0 && tempBeam.getmItemId()==PuzzleMain.TYPE*PuzzleMain.TYPE){
                continue;
            }else {
                return false;
            }
        }
        return true;
    }
}
