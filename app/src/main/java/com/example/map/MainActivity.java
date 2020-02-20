package com.example.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
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
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.map.overlayutil.WalkingRouteOverlay;

import javax.xml.transform.Result;

public class MainActivity extends Activity  {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private Button btn_change;
    private Button btn_btn_traffic;
    private Button btn_opt;
    private Button btn_loc;

    //定位模式
    private TextView mTextView = null;
    private boolean isFirstLoc = true;  //是否首次定位
    private LocationClient mLocationClient = null;
//    -----------------------------------------------------------------------------------
    //搜索模块
private EditText start_edit, end_edit;
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    RoutePlanSearch mSearch = null;
    RouteLine route = null;
    private String loaclcity = null;
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    private TextView popupText = null, driver_city; // 泡泡view
    boolean useDefaultIcon = false;
    //    -----------------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        //地图对象
        mBaiduMap = mMapView.getMap();

        //定位初始化
        mLocationClient = new LocationClient(getApplicationContext());
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(200);//快速回到定位间隔200ms
        option.setIsNeedAddress( true );
        option.setWifiCacheTimeOut( 5*60*1000 );
        option.setLocationMode( LocationClientOption.LocationMode.Hight_Accuracy );
////可选，设置是否需要设备方向结果
//        option.setNeedDeviceDirect(true);
//        //可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
//        option.setIsNeedAltitude(true);
        option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到


        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);


        initView();

        //按钮的监听器
        btn_change.setOnClickListener( clickListener );
        btn_btn_traffic.setOnClickListener( clickListener );
        btn_opt.setOnClickListener( clickListener );
        btn_loc.setOnClickListener( clickListener );


    }


    private void initView() {
        btn_change = findViewById( R.id.btn_change );
        btn_btn_traffic = findViewById( R.id.btn_traffic );
        btn_opt = findViewById( R.id.more );
        btn_loc = findViewById( R.id.btn_loc );
        mTextView = findViewById( R.id.locmsg );

    }

    public View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_change){
                if(mBaiduMap.getMapType() == BaiduMap.MAP_TYPE_NORMAL) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    btn_change.setText("打开普通地图");
                }
                else {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    btn_change.setText("打开卫星地图");
                }
            }else if(v.getId() == R.id.btn_traffic){
                if(!mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(true);
                    btn_btn_traffic.setText("关闭路况");
                }
                else {
                    mBaiduMap.setTrafficEnabled(false);
                    btn_btn_traffic.setText("打开路况");
                }
            }else if(v.getId() == R.id.more){
//添加滑动窗口函数，在activity_main.xml中由于添加了NavigationView组件，看书到底是要添加什么依赖。
                //其中添加了相应的图片  和nav_menu.xml和nav_header.xml
                //而且之前的标注北京图片因此被取消
                //点击more  出现弹出框，将另外两个页面都加载到此项目上。
                Intent intent = new Intent(MainActivity.this,CustomerMenu.class);
                startActivity(intent);
            }
            else if(v.getId() == R.id.btn_loc){
                //开启地图定位图层
                mLocationClient.start();
                mTextView.setVisibility( View.VISIBLE );
            }
        }
    };
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }

            mBaiduMap.setMyLocationEnabled(true);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            //判断是否是第一次定位
            if(isFirstLoc){
                //isFirstLoc = false;
                //将地图移动到定位的位置
                float f = mBaiduMap.getMaxZoomLevel();
                LatLng ll = new LatLng( location.getLatitude(),location.getLongitude() );
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom( ll,f-2 );
                mBaiduMap.animateMapStatus( u );
                //一次定位结束，当再次点击定位时候即可回到自己所在位置
                mLocationClient.stop();
            }
            StringBuffer sb = new StringBuffer(256);
            sb.append( "time:" );
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                //GPS定位
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append( "\nheight:" );
                sb.append( location.getAltitude() );
                sb.append( "\ndirection:" );
                sb.append( location.getDirection() );
                sb.append( "\naddr:" );
                sb.append( location.getAddrStr() );
                sb.append( "\ndescribe:" );
                sb.append( "GPS定位成功！" );
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append( "\noperationer:" );
                sb.append( location.getOperators() );
                sb.append( "\ndescribe:" );
                sb.append( "网络定位成功" );
            }else if(location.getLocType() == BDLocation.TypeOffLineLocation){
                //离线定位
                sb.append( "\ndescribe:" );
                sb.append( "离线定位成功" );
            }else if(location.getLocType() == BDLocation.TypeServerError){
                sb.append( "\ndescribe:" );
                sb.append( "server定位失败，没有对应的位置信息" );
            }else if(location.getLocType() == BDLocation.TypeNetWorkException){
                sb.append( "\ndescribe:" );
                sb.append( "网络连接失败" );
            }
            mTextView.setText( sb );
        }
    }


    private void addOpt() {
        LatLng point = new LatLng( 39.9155260000,116.4038470000 );
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource( R.drawable.region );
        OverlayOptions option = new MarkerOptions().position( point ).icon( bitmap );
        mBaiduMap.addOverlay( option );
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
