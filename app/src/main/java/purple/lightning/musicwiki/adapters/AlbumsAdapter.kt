package purple.lightning.musicwiki.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import purple.lightning.musicwiki.databinding.RvItemBinding

class AlbumsAdapter(private var albums:MutableList<String>,
                    private var artists:MutableList<String>,
                    private var images:MutableList<String>,
                    private var AlbumonClickListener:AlbumOnClickListener):
                    RecyclerView.Adapter<AlbumsAdapter.ViewHolder>(){
    class ViewHolder(binding:RvItemBinding):RecyclerView.ViewHolder(binding.root){
        val tvAlbumName = binding.tvObj1
        val ivAlbumImg = binding.ivObj
        val tvPosition = binding.tvPosition
        val tvArtistName = binding.tvObj2
        val llRvItem = binding.llRvItem
    }

    interface AlbumOnClickListener {
        fun AlbumonClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                RvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try{
            holder.tvPosition.text = "${position+1}"
            holder.tvAlbumName.text = "Album: ${albums[position].subSequence(1, albums[position].length-1)}"
            holder.tvArtistName.text = "Artist: ${artists[position].subSequence(1, artists[position].length-1)}"
            Glide.with(holder.ivAlbumImg)
                .load(images[position].subSequence(1,images[position].length-1))
                .into(holder.ivAlbumImg)
            holder.llRvItem.setOnClickListener {
                AlbumonClickListener.AlbumonClickListener(position)
            }
        }catch (e:Exception){
            Log.d("[ERROR]:", e.toString())
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }
}