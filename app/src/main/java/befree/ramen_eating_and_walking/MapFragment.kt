package befree.ramen_eating_and_walking

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import befree.ramen_eating_and_walking.databinding.FragmentMapBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment: Fragment(),
    OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // Google Map APIを使うためのプロパティ
    private lateinit var mMap: GoogleMap
    private var permissionMyLocation = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Google Mapを描画
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync (this)
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

            // 位置情報取得が許可されている場合の処理
            if (permissionMyLocation) {
                // 現在地ボタンの表示
                isMyLocationEnabled
                setOnMyLocationClickListener {
                    onMyLocationClick(it)
                }
                setOnMyLocationButtonClickListener {
                    onMyLocationButtonClick()
                }

                // 現在地にマップを移動
            }
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

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireContext(), "${getString(R.string.my_location)}\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), getString(R.string.move_my_location), Toast.LENGTH_SHORT)
            .show()
        // falseを返すことで、イベントを消費せず、デフォルトの動作を維持する。
        // (カメラはユーザーの現在位置に合わせてアニメーションする）。
        return false
    }
}