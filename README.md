# 区域热力地图 ChinaMapView

[![](https://jitpack.io/v/NoEndToLF/ChinaMapView.svg)](https://jitpack.io/#NoEndToLF/ChinaMapView)

**ChinaMapView**：这是一个可拖拽，可点击，可手势放大的自定义中国地图组件，使用简单，具备一个自定义View应有的基本素质

* **原理** ：画地图用path，缩放拖拽用Matrix，判断点击位置用Region，判断滑动和缩放过程中的Map边界用Matrix.mapRect
* **功能** ：
   * 1、可随手势拖动，到边界抵消相应方向的滑动，且只有down的点在map内才可以拖动。
   * 2、可随缩放手势放大缩小，放大缩小的手势不做任何限制，只要在View内即可。
   * 3、点击某个省份回调该Select事件，传递出省份名字
   * 4、提供了刷新入口(Data改变刷新)
* **基本素质** ：
   * 1、旋转屏幕状态不丢失，转之前啥样，回来还是啥样（处理好onSaveInstanceState和onRestoreInstanceState）
   * 2、暴露事件冲突接口，允许外界操作父控件的事件及该view自己的事件（因为这只是个View，没办法直接处理所有的滑动冲突场景）
   * 3、内存抖动要小，防止内存溢出。
-------------------
# 示例（真机流畅丝滑，压缩的GIF丢帧严重）
## Demo演示了普通用法和涉及到SwipeRefreshLayout+AppBarLayout等类似的滑动冲突的用法（细节请看代码）。
* **1、普通使用** ：只有Down的点在map的范围内才可以响应拖拽事件，否则通知父控件拦截；缩放事件不做限制。
* **2、下拉刷新及其他滑动冲突** ：
   * 1、拖拽事件：Down的点在map内，通过onPromiseParentTouchListener方法中使用SwipeRefreshLayout.setEnabled(promise)通知外界设置SwipeRefreshLayout不可以滑动。
   * 2、缩放事件：在MyScaleGestureDetector的onScaleBegin方法中通过onPromiseParentTouchListener方法中使用SwipeRefreshLayout.setEnabled(promise)通知外界设置SwipeRefreshLayout不可以滑动。
   * 3、在SwipeRefreshLayout的OnRefreshListener中设置ChinaMapView的setEnableTouch(false)方法通知刷新期间，ChinaMapView不响应任何事件。
   * 4、同理，监听AppBarLayout的滚动高度来控制只有完全展开才允许SwipeRefreshLayout下拉刷新和ChinaMapView的事件响应,否则都禁止

| 常规使用      |下拉刷新及其他滑动冲突  |
| :--------:| :--------:|  
|![normal](https://github.com/NoEndToLF/ChinaMapView/blob/master/DemoImg/demo1.gif)| ![fix](https://github.com/NoEndToLF/ChinaMapView/blob/master/DemoImg/demo2.gif)| 
 <br />

# 开始使用  
* [基本API](#基本API)
* [使用](#使用)
    * [引入](#引入)
    * [布局XML中添加](#布局XML中添加与系统View使用方式一样宽高如果只确定其一另一个根据parent的宽高和map的比例取最小值确定最终map的宽度和高度由padding决定)
    * [代码中修改Data和View属性](#代码中通过ChinaMapView的getChinaMapModel方法拿到ChinaMapModel通过修改ChinaMapModel的属性来刷新ChinaMapView的显示效果其他的缩放倍数和接口通过ChinaMapView直接设置Demo中的SwipRefreshAppbarActivity和NormalActivity中有详细使用代码)
* [反馈与建议](#反馈与建议)    
# 基本API
### 所有的省份、自治区、直辖市
#### 安徽省,北京市,重庆市,福建省,广东省,甘肃省,广西省,贵州省,海南省,河北省,河南省,香港,黑龙江,湖南省,湖北省,吉林省,江苏省,江西省,辽宁省,澳门,内蒙古,宁夏区,青海省,陕西省,四川省,山东省,上海市,山西省,天津市,台湾,新疆区,西藏区,云南省,浙江省

### Data实例类 ChinaMapModel，通过ChinaMapView.getChinaMapModel()获得，以下为使用期间会接触到的属性，别的属性都是为绘制准备的，不用关心，也不用去设置。    

|属性  | 类型  |作用  |
| :--------| :--------|:--: |
| provinceslist| List<ProvinceModel>|包含所有的省份model|
  
### Data实例类 ProvinceModel，通过chinaMapModel.getProvinceslist()获得，以下为使用期间会接触到的属性，别的属性都是为绘制准备的，不用关心，也不用去设置。 

|属性  | 类型  |作用  |
| :--------| :--------|:--: |
| color| int|省份填充的颜色|
| normalBordercolor| int|省份未选中状态下的边框颜色|
| selectBordercolor| int|省份未选中状态下的边框颜色|

### ChinaMapView
|方法  |参数  | 作用  |
| :--------| :--------| :--: |
|setEnableTouch  |boolean  | 设置是否可以消费事件（默认为true）  |
|setScaleMin  |int  | 设置缩放的最小倍数，最终结果>=0  |
|setScaleMax  |int  | 设置缩放的最大倍数，最终结果>=1  |
|getChinaMapModel  |void  | 返回ChinaMapModel对象，用于之后的修改刷新view的展示  |
|setOnProvinceClickLisener  |ChinaMapView.onProvinceClickLisener  | 省份点击选中接口  |
|setOnPromiseParentTouchListener  |ChinaMapView.onPromiseParentTouchListener  | 通知外界是否允许chinamapview之上的view拦截事件 |
|notifyDataChanged  |void  | 修改ChinaMapModel对象后，刷新View  |

# 使用
### 引入
Step 1. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.NoEndToLF:ChinaMapView:1.0.1'
	}
### 布局XML中添加	
#### 布局XML中添加与系统View使用方式一样，宽高如果只确定其一，另一个根据parent的宽高和map的比例取最小值确定。最终map的宽度和高度由padding决定
 ``` java
<com.wxy.chinamapview.view.ChinaMapView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:id="@+id/map"></com.wxy.chinamapview.view.ChinaMapView>
 ``` 
 
### 代码中修改Data和View属性
#### 代码中通过ChinaMapView的getChinaMapModel()方法拿到ChinaMapModel，通过修改ChinaMapModel的属性来刷新ChinaMapView的显示效果，其他的缩放倍数和接口通过ChinaMapView直接设置，Demo中的SwipRefreshAppbarActivity和NormalActivity中有详细使用代码
##### 拿到ChinaMapModel
 ``` java
chinaMapModel = map.getChinaMapModel();
 ```
 ##### 设置缩放的最大最小值
  ``` java
 map.setScaleMin(1);
        map.setScaleMax(3);
```
##### 修改省份颜色，这里所有省份都处理成一样了，实际场景可给省份设置不同的颜色，修改完后map.notifyDataChanged()刷新View
  ``` java
for (ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
                                    provinceModel.setColor(color);
                                }
                                map.notifyDataChanged();
```
##### 修改省份未选中状态下边框颜色，这里所有省份都处理成一样了，实际场景可给省份设置不同的颜色，修改完后map.notifyDataChanged()刷新View
  ``` java
for (ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
                                    provinceModel.setNormalBordercolor(color);
                                }
                                map.notifyDataChanged();
```
##### 修改省份选中状态下边框颜色，这里所有省份都处理成一样了，实际场景可给省份设置不同的颜色，修改完后map.notifyDataChanged()刷新View
``` java
for (ProvinceModel provinceModel:chinaMapModel.getProvinceslist()){
                                    provinceModel.setSelectBordercolor(color);
                                }
                                map.notifyDataChanged();
```
##### 设置省份点击事件，这里传递的是省份或者直辖市或者自治区的名字（上方有全部的name）
 ``` java
map.setOnProvinceClickLisener(new ChinaMapView.onProvinceClickLisener() {
            @Override
            public void onSelectProvince(String provinceName) {
                tvName.setText(provinceName);
            }
        });
```
##### 添加事件处理回调，即通知外界是否要拦截事件;
``` java
chinamapView.setOnPromiseParentTouchListener(new ChinaMapView.onPromiseParentTouchListener() {
            @Override
            public void onPromiseTouch(boolean promise) {
                swipe.setEnabled(promise);
            }

        });
```
##### 修改完chinamapmodel的数据后，刷新数据用chinamapView.notifyDataChanged()，且刷新期间禁止chinamapView响应事件chinamapView.setEnableTouch(true);;
``` java
swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chinamapView.setEnableTouch(false);
                //模拟耗时
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String namestring = ColorChangeUtil.nameStrings[++currentColor %               ColorChangeUtil.nameStrings.length];
                        btnChange.setText(namestring);
                        colorView.setList(colorView_hashmap.get(namestring));
                        //重置map各省份颜色
                        ColorChangeUtil.changeMapColors(chinaMapModel, namestring);
                        chinamapView.notifyDataChanged();
                        swipe.setRefreshing(false);
                        chinamapView.setEnableTouch(true);
                    }
                },2000);
            }
        });
```
# 反馈与建议
- 邮箱：<wxy314309@foxmail.com>

# License
```
Copyright (c) [2018] [static]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
---------
