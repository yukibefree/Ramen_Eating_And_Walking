package befree.ramen_eating_and_walking

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import befree.ramen_eating_and_walking.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MapFragment: Fragment(),
    OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // Google Map APIを使うためのプロパティ
    private lateinit var mMap: GoogleMap
    private var permissionMyLocation = false
    // PlacesClientの変数を設定
    private lateinit var placesClient: PlacesClient
    // FusedLocationProviderClientの変数を設定
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(35.681236, 139.767125) // 東京駅をセット
    private var lastKnownLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Google Mapを描画
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync (this)

        // Construct a PlacesClient
        Places.initialize(requireContext(), getString(R.string.google_api_key))
        placesClient = Places.createClient(requireContext())

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    // Mapの設定
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkPermissionMyLocation()
        // 代表地点をセット
        val tokyo = LatLng(35.681236, 139.767125)
        // ズームレベルをセット
        val zoomLevel = 13.0f

        mMap.apply {
            // マーカーをセット
            addMarker(
                MarkerOptions()
                    .position(tokyo)
                    .title("店舗名")
                    .snippet("住所")
            )
            // .icon()) アイコンを設定
            // カメラを代表地点を中心にしてzoomする
            moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, zoomLevel))

            // UIを設定
            uiSettings.apply {
                // 拡大・縮小ボタン表示
                isZoomControlsEnabled = true
                // スワイプで地図を並行移動可能にする
                isScrollGesturesEnabled = true
                // ピンチイン・アウトで縮尺を変更可能にする
                isZoomGesturesEnabled = true
            }
        }
        // 位置情報取得が許可されている場合の処理
        if (permissionMyLocation) {
            // 現在地ボタンの表示
            mMap.apply {
                isMyLocationEnabled
                setOnMyLocationClickListener {
                    onMyLocationClick(it)
                }
                setOnMyLocationButtonClickListener {
                    onMyLocationButtonClick()
                }
            }
            // 現在地にカメラを移動
            getDeviceLocation()
        }
    }

    private fun checkPermissionMyLocation () {
        // 権限が付与されているか確認し、付与されている場合はマイロケーションレイヤーを有効にする。
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            permissionMyLocation = true
            return
        }
    }

    /**
     * 現在地のボタンを押した時の処理
     * 緯度・経度を表示
     */
    override fun onMyLocationClick(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        Toast.makeText(requireContext(), "${getString(R.string.my_location)}( $lat , $lng )", Toast.LENGTH_LONG)
            .show()
    }

    /**
     * 現在地ボタンを押した時の処理
     * トーストを表示
     */
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), getString(R.string.move_my_location), Toast.LENGTH_SHORT)
            .show()
        // falseを返すことで、イベントを消費せず、デフォルトの動作を維持する。
        // (カメラはユーザーの現在位置に合わせてアニメーションする）。
        return false
    }

    /**
     * デバイスに残ったログから現在地の取得をする
     * (位置情報が取れなかった場合は、デフォルトで表示)
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        // ズームレベルをセット
        val zoomLevel = 13.0f
        try {
            if (permissionMyLocation) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), zoomLevel))
                        }
                    } else {
                        Log.d("null error", "Current location is null. Using defaults.")
                        Log.e("Exception", "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, zoomLevel))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
}