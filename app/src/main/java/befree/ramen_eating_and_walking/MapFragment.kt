package befree.ramen_eating_and_walking

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import befree.ramen_eating_and_walking.databinding.FragmentMapBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment: Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // Google Map APIを使うためのプロパティ
    private lateinit var mMap: GoogleMap
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
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 代表地点をセット
        val tokyo = LatLng(35.39291572, 139.44288869)
        // ズームレベルをセット
        val zoomLevel = 13.0f

        mMap.apply {
            // マーカーをセット
            addMarker(MarkerOptions().position(tokyo).title("Marker in tokyo"))
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
                // 現在地ボタン表示
                isMyLocationButtonEnabled = true
            }
        }
    }
}