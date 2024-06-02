package befree.ramen_eating_and_walking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "results")
    val results: Results
)

@JsonClass(generateAdapter = true)
data class Results(
    @Json(name = "shop")
    val shop: List<Shop>
)

@JsonClass(generateAdapter = true)
data class Shop(
    @Json(name = "address")
    val address: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lng")
    val lng: Double,
    @Json(name = "logo_image")
    val logoImage: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "photo")
    val photo: Photo,
    @Json(name = "shop_detail_memo")
    val shopDetailMemo: String,
    @Json(name = "urls")
    val urls: Urls
)


@JsonClass(generateAdapter = true)
data class Photo(
    @Json(name = "mobile")
    val mobile: Mobile,
    @Json(name = "pc")
    val pc: Pc
)


@JsonClass(generateAdapter = true)
data class Urls(
    @Json(name = "pc")
    val pc: String
)

@JsonClass(generateAdapter = true)
data class Mobile(
    @Json(name = "l")
    val l: String,
    @Json(name = "s")
    val s: String
)

@JsonClass(generateAdapter = true)
data class Pc(
    @Json(name = "l")
    val l: String,
    @Json(name = "m")
    val m: String,
    @Json(name = "s")
    val s: String
)