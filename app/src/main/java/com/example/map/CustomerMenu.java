package com.example.map;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.map.overlayutil.BikingRouteOverlay;
import com.example.map.overlayutil.DrivingRouteOverlay;
import com.example.map.overlayutil.OverlayManager;
import com.example.map.overlayutil.PoiOverlay;
import com.example.map.overlayutil.TransitRouteOverlay;
import com.example.map.overlayutil.WalkingRouteOverlay;


import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 此demo用来展示如何进行驾车、步行、公交路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class CustomerMenu extends Activity implements OnMapClickListener, OnGetRoutePlanResultListener,
		OnClickListener, OnGetSuggestionResultListener, OnGetPoiSearchResultListener, OnItemClickListener {
	// 浏览路线节点相关
	private String localcity;// 记录当前城市
	Button mBtnPre = null; // 上一个节点
	Button mBtnNext = null; // 下一个节点
	int nodeIndex = -1; // 节点索引,供浏览节点时使用
	private Button findroute;// 路线规划
	private Button findroute2;// 路线规划
	RouteLine route = null;
	OverlayManager routeOverlay = null;
	private Button requestLocButton;
	private LocationMode mCurrentMode;
	private ImageButton my_back;// 返回按钮
	private LinearLayout edit_layout;// 底部目的地栏
	private LinearLayout choosemode;// 选择导航方式
	private ListView search_end;// 推荐目的地
	private LinearLayout guide_layout;
	private LinearLayout locationLayout;// 定位框
	BitmapDescriptor mCurrentMarker;
	boolean useDefaultIcon = false;
	private TextView popupText = null, customer_city; // 泡泡view
	private TextView mylocation;
	private EditText start_edit, end_edit;
	boolean isFirstLoc = true; // 是否首次定位

	// 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	// 地图控件
	private TextureMapView mMapView = null;
	private BaiduMap mBaidumap;

	// 搜索相关
	RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	// 搜索周边
	private LinearLayout poilayout;
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	private ImageButton customer_find_btn;

	/**
	 * 搜索关键字输入窗口
	 */
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index = 0;

	// 定位相关
	LocationClient mLocClient;
	LatLng currentPt;
	public MyLocationListenner myListener = new MyLocationListenner();

	// 点击地图事件
	private LinearLayout click_layout;
	private TextView endlocation;
	private Button go_end;
	private LatLng endPt;
	private GeoCoder geoCoder;

	// 动画效果
	Animation slide_in_above;
	Animation slide_in_bottom;
	Animation slide_out_above;
	Animation slide_out_bottom;
	// 交通图
	private ImageButton officient;
	private boolean flag = false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置标题栏不可用
		setContentView(R.layout.customer_menu);
		// 初始化控件
		initview();
		// 初始化地图
		inintmap();

		mCurrentMode = LocationMode.COMPASS;
		requestLocButton.setText("罗");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaidumap.setMyLocationConfigeration(
							new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
					hideclickLayout(false);
					break;
				case COMPASS:
					requestLocButton.setText("普");
					mCurrentMode = LocationMode.NORMAL;
					mBaidumap.setMyLocationConfigeration(
							new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));

					locationLayout.startAnimation(slide_in_bottom);
					locationLayout.setVisibility(View.VISIBLE);
					findroute.setVisibility(View.GONE);
					hideclickLayout(false);
					break;
				case FOLLOWING:
					requestLocButton.setText("罗");
					mCurrentMode = LocationMode.COMPASS;
					mBaidumap.setMyLocationConfigeration(
							new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));

					locationLayout.startAnimation(slide_in_bottom);
					locationLayout.setVisibility(View.VISIBLE);
					findroute.setVisibility(View.GONE);
					hideclickLayout(false);
					break;
				default:
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);
		CharSequence titleLable = "路线规划功能";
		setTitle(titleLable);

		// 地图点击事件处理
		mBaidumap.setOnMapClickListener(this);
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);
		// 点击地图获取点的坐标
		mBaidumap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapPoiClick(MapPoi arg0) {
				if (locationLayout.getVisibility() == View.VISIBLE) {
					locationLayout.setVisibility(View.GONE);
					locationLayout.startAnimation(slide_out_bottom);
				}
				hideclickLayout(true);
				findroute.setVisibility(View.GONE);
				end_edit.setText(arg0.getName());
				endlocation.setText(arg0.getName());
				endPt = arg0.getPosition();
				mBaidumap.clear();
				mydraw(arg0.getPosition(), R.drawable.icon_en);

			}

			@Override
			public void onMapClick(LatLng Ll) {
				if (locationLayout.getVisibility() == View.VISIBLE) {
					locationLayout.setVisibility(View.GONE);
					locationLayout.startAnimation(slide_out_bottom);
				}
				findroute.setVisibility(View.GONE);
				hideclickLayout(true);
				endPt = Ll;
				mBaidumap.clear();
				mydraw(endPt, R.drawable.icon_en);
				// 创建地理编码检索实例
				geoCoder = GeoCoder.newInstance();
				// 设置反地理经纬度坐标,请求位置时,需要一个经纬度
				geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(endPt));
				// 设置地址或经纬度反编译后的监听,这里有两个回调方法,
				geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
					@Override
					public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

						// addre = "地址："+reverseGeoCodeResult.getAddress();
						// Log.i(TAG, "onGetReverseGeoCodeResult: "+reverseGeoCodeResult.getAddress());
					}

					/**
					 *
					 * @param reverseGeoCodeResult
					 */
					@Override
					public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

						if (reverseGeoCodeResult == null
								|| reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {

						} else {
							end_edit.setText(reverseGeoCodeResult.getAddress());
							endlocation.setText(reverseGeoCodeResult.getAddress());
						}
					}
				});
			}
		});
	}

	// 地图初始化
	public void inintmap() {
		// 地图初始化
		mMapView = (TextureMapView) findViewById(R.id.mTexturemap);
		mBaidumap = mMapView.getMap();

		// 不显示缩放比例尺
		mMapView.showZoomControls(false);
		// 不显示百度地图Logo
		mMapView.removeViewAt(1);
		// 开启定位图层
		mBaidumap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1500);
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
		option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		mLocClient.setLocOption(option);
		mLocClient.start();// 启动sdk
	}

	public void initview() {

		start_edit = (EditText) findViewById(R.id.start);
		end_edit = (EditText) findViewById(R.id.end);
		customer_city = (TextView) findViewById(R.id.customer_city);
		my_back = (ImageButton) findViewById(R.id.my_back);
		mylocation = (TextView) findViewById(R.id.mylocation);
		requestLocButton = (Button) findViewById(R.id.change);
		findroute = (Button) findViewById(R.id.findroute);
		findroute2 = (Button) findViewById(R.id.findroute2);
		guide_layout = (LinearLayout) findViewById(R.id.guide_layout);
		edit_layout = (LinearLayout) findViewById(R.id.edit_layout);
		search_end = (ListView) findViewById(R.id.search_end);
		locationLayout = (LinearLayout) findViewById(R.id.locationLayout);
		poilayout = (LinearLayout) findViewById(R.id.poilayout);
		choosemode = (LinearLayout) findViewById(R.id.choosemode);
		// 交通图
		officient = (ImageButton) findViewById(R.id.officient);
		officient.setOnClickListener(this);
		// 地图点击事件
		click_layout = (LinearLayout) findViewById(R.id.click_layout);
		endlocation = (TextView) findViewById(R.id.endlocation);

		my_back.setOnClickListener(this);
		findroute.setOnClickListener(this);
		findroute2.setOnClickListener(this);

		/****************** 动画 ***************/
		slide_in_above = AnimationUtils.loadAnimation(this, R.anim.slide_in_above);// 显示
		slide_out_above = AnimationUtils.loadAnimation(this, R.anim.slide_out_above);// 消失
		slide_in_bottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);// 显示
		slide_out_bottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);// 消失

		// ListView中推荐地址选择
		search_end.setOnItemClickListener(this);// 推荐地址的监听

		// *************搜索周边******************
		customer_find_btn = (ImageButton) findViewById(R.id.customer_find_btn);
		customer_find_btn.setOnClickListener(this);
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchpoi);
		sugAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		search_end.setAdapter(sugAdapter);
		keyWorldsView.setAdapter(sugAdapter);

		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
		keyWorldsView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString()).city(localcity));
			}
		});

		/**
		 * 目的地关键字变化时
		 * 
		 */
		end_edit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() <= 0) {
					return;
				}
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption()).keyword(s.toString()).city(localcity));

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 发起路线规划搜索示例
	 *
	 * @param v
	 */
	public void searchButtonProcess(View v) {
		// 选择导航方式
		TextView driver = (TextView) findViewById(R.id.go_driver);
		TextView bus = (TextView) findViewById(R.id.go_bus);
		TextView bike = (TextView) findViewById(R.id.go_bike);
		TextView walk = (TextView) findViewById(R.id.go_walk);
		// 重置浏览节点的路线数据
		route = null;
		mBaidumap.clear();
		// 设置起终点信息，对于tranist search 来说，城市名无意义
		PlanNode stNode = PlanNode.withCityNameAndPlaceName(localcity, start_edit.getText().toString());
		PlanNode enNode = PlanNode.withCityNameAndPlaceName(localcity, end_edit.getText().toString());

		// 实际使用中请对起点终点城市进行正确的设定
		switch (v.getId()) {
		case R.id.go_driver:
			bus.setSelected(false);
			bike.setSelected(false);
			walk.setSelected(false);
			driver.setSelected(true);
			mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
			hideguide();
			break;
		case R.id.go_bus:
			bus.setSelected(true);
			bike.setSelected(false);
			walk.setSelected(false);
			driver.setSelected(false);
			mSearch.transitSearch((new TransitRoutePlanOption()).from(stNode).city("郑州").to(enNode));
			hideguide();
			break;
		case R.id.go_bike:
			bus.setSelected(false);
			bike.setSelected(true);
			walk.setSelected(false);
			driver.setSelected(false);
			mSearch.bikingSearch(new BikingRoutePlanOption().from(stNode).to(enNode));
			hideguide();
			break;
		case R.id.go_walk:
			bus.setSelected(false);
			bike.setSelected(false);
			walk.setSelected(true);
			driver.setSelected(false);
			mSearch.walkingSearch(new WalkingRoutePlanOption().from(stNode).to(enNode));
			hideguide();
			break;
		case R.id.go_end:
			click_layout.setVisibility(View.GONE);
			bus.setSelected(false);
			bike.setSelected(false);
			walk.setSelected(true);
			driver.setSelected(false);
			PlanNode startPlanNode = PlanNode.withLocation(currentPt); // lat long
			PlanNode endPlanNode = PlanNode.withLocation(endPt);
			mSearch.walkingSearch(new WalkingRoutePlanOption().from(startPlanNode).to(endPlanNode));
			hideall();
			showguide();
			search_end.setVisibility(View.GONE);
			edit_layout.setVisibility(View.GONE);
			choosemode.setVisibility(View.VISIBLE);
			break;
		}

	}

	/**
	 * 节点浏览示例
	 *
	 //* @param v
	 */

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	// 步行
	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			MyToast("抱歉，未找到结果");
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
			
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			route = result.getRouteLines().get(0);
			WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaidumap);
			routeOverlay = overlay;
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	// 公交
	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			route = result.getRouteLines().get(0);
			TransitRouteOverlay overlay = new TransitRouteOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			// 设置路线数据
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap(); // 将所有overlay添加到地图中
			overlay.zoomToSpan();// 缩放地图
		}
	}

	@Override
	public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

	}

	// 驾车
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			MyToast("抱歉，未找到结果");
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			route = result.getRouteLines().get(0);
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
			routeOverlay = overlay;
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	@Override
	public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

	}

	// 骑行
	@Override
	public void onGetBikingRouteResult(BikingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			MyToast("抱歉，未找到结果");
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			hideall();
			showguide();
			edit_layout.setVisibility(View.VISIBLE);
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			nodeIndex = -1;
			route = result.getRouteLines().get(0);
			BikingRouteOverlay overlay = new BikingRouteOverlay(mBaidumap);
			routeOverlay = overlay;
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	@Override
	public void onMapClick(LatLng point) {
		mBaidumap.hideInfoWindow();
	}

	@Override
	public void onMapPoiClick(MapPoi poi) {

	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mSearch.destroy();
		mMapView.onDestroy();
		super.onDestroy();
	}

	public void MyToast(String s) {
		Toast.makeText(CustomerMenu.this, s, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null) {
				return;
			}
			String locationDescribe = location.getLocationDescribe(); // 获取位置描述信息
			String startLocation = locationDescribe.substring(1);
			MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
			mBaidumap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				currentPt = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatus.Builder builder = new MapStatus.Builder();
				builder.target(currentPt).zoom(17.5f);
				mBaidumap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
				start_edit.setText(startLocation);
				MyToast("当前所在位置：" + locationDescribe);
				mylocation.setText(locationDescribe);
				localcity = location.getCity();
				customer_city.setText(location.getCity());
				String mm = "customer " + "location " + location.getLatitude() + " " + location.getLongitude() + "\n";

			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	/**
	 * 影响搜索按钮点击事件 public void searchPoiProcess(View v) {
	 * 
	 * }
	 * 
	// * @param v
	 */

	public void onGetPoiResult(PoiResult result) {
		if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			MyToast("未找到结果");
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			mBaidumap.clear();
			PoiOverlay overlay = new MyPoiOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到结果";
			MyToast(strInfo);
		}
	}

	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			MyToast("抱歉，未找到结果");
		} else {
			endPt = result.getLocation();
			endlocation.setText(result.getName() + ": " + result.getAddress());
			
		}
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

	}

	@Override
	public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			// if (poi.hasCaterDetails) {
			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
			if (locationLayout.getVisibility() == View.VISIBLE) {
				locationLayout.setVisibility(View.GONE);
				locationLayout.startAnimation(slide_out_bottom);
			}
			findroute.setVisibility(View.GONE);
			hideclickLayout(true);
			// }
			return true;
		}
	}

	// ----------------------------------------------------------
	public void mydraw(LatLng location, int a) {
		// 定义Maker坐标点 LatLng location

		// 构建Marker图标

		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(a);

		// 构建MarkerOption，用于在地图上添加Marker

		OverlayOptions option = new MarkerOptions().position(location).icon(bitmap);

		// 在地图上添加Marker，并显示
		mBaidumap.addOverlay(option);
	}

	// 点击事件
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.findroute:
			showguide();
			findroute.setVisibility(View.GONE);
			requestLocButton.setVisibility(View.GONE);
			officient.setVisibility(View.GONE);
			poilayout.setVisibility(View.GONE);
			break;
		case R.id.findroute2:
			locationLayout.setVisibility(View.GONE);
			poilayout.setVisibility(View.GONE);
			findroute.setVisibility(View.GONE);
			requestLocButton.setVisibility(View.GONE);
			officient.setVisibility(View.GONE);
			showguide();
			break;
		case R.id.customer_find_btn:
			EditText editSearchKey = (EditText) findViewById(R.id.searchpoi);
			mPoiSearch.searchNearby(new PoiNearbySearchOption().location(currentPt)
					.keyword(editSearchKey.getText().toString()).radius(3000).pageNum(15)
					// 以currentPt为搜索中心1000米半径范围内的自行车点
					.pageNum(load_Index));
			break;
		case R.id.my_back:
			hideguide();
			requestLocButton.setVisibility(View.VISIBLE);
			officient.setVisibility(View.VISIBLE);
			findroute.setVisibility(View.VISIBLE);
			poilayout.setVisibility(View.VISIBLE);
			edit_layout.setVisibility(View.VISIBLE);
			guide_layout.setVisibility(View.GONE);
			locationLayout.setVisibility(View.GONE);
			click_layout.setVisibility(View.GONE);
			break;
		case R.id.officient:
			// 交通图
			if (flag) {
				mBaidumap.setTrafficEnabled(true);
				MyToast("打开交通图");
				flag = false;
			} else {
				mBaidumap.setTrafficEnabled(false);
				flag = true;
				MyToast("关闭交通图");
			}
		}
	}

	// ListView中点击事件
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 通过view获取其内部的组件，进而进行操作
		String text = search_end.getItemAtPosition(position) + "";
		end_edit.setText(text);
		if (search_end.getVisibility() == View.VISIBLE) {
			search_end.setVisibility(View.GONE);
			search_end.startAnimation(slide_out_bottom);
		}
	}

	private void showguide() {
		if (guide_layout.getVisibility() == View.GONE) {
			guide_layout.setVisibility(View.VISIBLE);
			guide_layout.startAnimation(slide_in_above);
		}
		if (search_end.getVisibility() == View.GONE) {
			search_end.setVisibility(View.VISIBLE);
			search_end.startAnimation(slide_in_bottom);
		}

	}

	private void hideguide() {
		mBaidumap.clear();
		if (edit_layout.getVisibility() == View.VISIBLE) {
			edit_layout.setVisibility(View.GONE);
			edit_layout.startAnimation(slide_out_above);
		}
		if (search_end.getVisibility() == View.VISIBLE) {
			search_end.setVisibility(View.GONE);
			search_end.startAnimation(slide_out_bottom);
		}

	}

	private void hideclickLayout(boolean flag) {
		if (flag) {
			if (click_layout.getVisibility() == View.GONE) {
				click_layout.setVisibility(View.VISIBLE);
				click_layout.startAnimation(slide_in_bottom);
			}

		} else {
			if (click_layout.getVisibility() == View.VISIBLE) {
				click_layout.setVisibility(View.GONE);
				click_layout.startAnimation(slide_out_bottom);
			}
		}
	}

	private void hideall() {
		edit_layout.setVisibility(View.VISIBLE);
		requestLocButton.setVisibility(View.GONE);
		officient.setVisibility(View.GONE);
		poilayout.setVisibility(View.GONE);
	}

}
