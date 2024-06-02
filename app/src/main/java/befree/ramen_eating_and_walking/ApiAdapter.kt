package befree.ramen_eating_and_walking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import befree.ramen_eating_and_walking.databinding.RecylerRamenShopBinding

/**
 * RecyclerView用Adapter
 * 第一引数: データを保持するクラス。今回はShop
 * 第二引数: リスト内の1行の内容を保持するViewHolder。今回はApiItemViewHolder
 */
class ApiAdapter : ListAdapter<Shop, ApiItemViewHolder>(ApiItemCallback()) {
    /**
     * ViewHolderを生成して返す
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApiItemViewHolder {
        // ViewBindingを引数にApiItemViewHolderを生成
        val view =
            RecylerRamenShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApiItemViewHolder(view)
    }

    /**
     * 指定された位置（position）のViewにShopの情報をセットする
     */
    override fun onBindViewHolder(holder: ApiItemViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}

/**
 * リスト内の1行の内容を保持する
 */
class ApiItemViewHolder(private val binding: RecylerRamenShopBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(shop: Shop, position: Int) {
        // 偶数番目と奇数番目で背景色を変更させる
        binding.rootView.setBackgroundColor(
            ContextCompat.getColor(
                binding.rootView.context,
                if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray
            )
        )
        // nameTextViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
        binding.nameTextView.text = shop.name

        // Picassoライブラリを使い、imageViewにdata.logoImageのurlの画像を読み込ませる
        Picasso.get().load(shop.logoImage).into(binding.imageView)
    }
}

/**
 * データの差分を確認するクラス
 */
internal class ApiItemCallback : DiffUtil.ItemCallback<Shop>() {

    override fun areItemsTheSame(oldItem: Shop, newItem: Shop): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Shop, newItem: Shop): Boolean {
        return oldItem == newItem
    }
}