package purple.lightning.musicwiki.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import purple.lightning.musicwiki.databinding.ArtisitActivtiyRvItemBinding

class ArtistAlbumAdapter(
    private var artistName:String,
    private var albums:MutableList<String>,
    private var images:MutableList<String>,
    private var artists:MutableList<String>,
    private var ArtistAlbumonClickListener: ArtistAlbumOnClickListner): RecyclerView.Adapter<ArtistAlbumAdapter.ViewHolder>(){
    class ViewHolder(binding:ArtisitActivtiyRvItemBinding):RecyclerView.ViewHolder(binding.root){
        var tvObj1 = binding.tvObj1
        var tvObj2 = binding.tvObj2
        var ivRVvImg = binding.ivRVvImg
        var cvRvItem = binding.cvRvItem
    }

    interface ArtistAlbumOnClickListner{
        fun ArtistAlbumonClickListner(position: Int,
                                      albums:MutableList<String>,
                                      images:MutableList<String>,
                                      artists:MutableList<String>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ArtisitActivtiyRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try{
            holder.tvObj1.text = albums[position].subSequence(1, albums[position].length-1)
            holder.tvObj2.text = artistName
            holder.cvRvItem.setOnClickListener {
                try{
                    ArtistAlbumonClickListener.ArtistAlbumonClickListner(position, albums, images, artists)
                    Log.d("[CLICK LISTENER]:", "Albums: ${albums} Images${images} Artists${artists}")
                }catch (e:Exception){
                    Log.d("[CLICK LISTENER]:", "${e}")
                }
            }
            Glide.with(holder.ivRVvImg)
                .load(images[position].subSequence(1,images[position].length-1))
                .into(holder.ivRVvImg)
        }catch (e:Exception){
            Log.d("[ARTIST ALBUM ADAPTER]:", "${e}")
        }
    }

    override fun getItemCount(): Int {
        return albums.size
    }
}