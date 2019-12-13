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
   * 4、同理，监听AppBarLayout的滚动高度来控制只有展开才允许SwipeRefreshLayout下拉刷新和ChinaMapView的事件响应,否则都禁止

| 常规使用      |下拉刷新及其他滑动冲突  |
| :--------:| :--------:|  
|![normal](https://github.com/NoEndToLF/ChinaMapView/blob/master/DemoImg/demo1.gif)| ![fix](https://github.com/NoEndToLF/ChinaMapView/blob/master/DemoImg/demo2.gif)| 
 <br />

# 使用  
* [基本API](#基本API)
* [使用](#使用)
    * [引入](#引入)
    * [布局XML中添加](#布局XML中添加与系统View使用方式一样，宽高如果只确定其一，另一个根据parent的宽高和map的比例取最小值确定。最终map的宽度和高度由padding决定。
    * [代码中修改显示Data](#代码中通过ChinaMapView.getChinaMapModel()拿到ChinaMapModel，通过修改ChinaMapModel的属性来刷新ChinaMapView的显示效果，Demo中的SwipRefreshAppbarActivity和NormalActivity中有详细使用代码)
    
# 基本API
## Data实例类 ChinaMapModel，通过ChinaMapView.getChinaMapModel()获得，以下为使用期间会接触到的属性，别的属性都是为绘制准备的，不用关心，也不用去设置。    

|属性  | 类型  |作用  |
| :--------| :--------|:--: |
| provinceslist| List<ProvinceModel>|包含所有的省份model|
  
## Data实例类 ProvinceModel，通过chinaMapModel.getProvinceslist()获得，以下为使用期间会接触到的属性，别的属性都是为绘制准备的，不用关心，也不用去设置。 

|属性  | 类型  |作用  |
| :--------| :--------|:--: |
| color| int|省份填充的颜色|
| normalBordercolor| int|省份未选中状态下的边框颜色|
| selectBordercolor| int|省份未选中状态下的边框颜色|

# 使用
## 引入
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
