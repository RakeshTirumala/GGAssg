package purple.lightning.musicwiki.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import purple.lightning.musicwiki.databinding.ArtistItemBinding

class ArtistsAdapter(
                     private var artists:MutableList<String>,
                     private var images:MutableList<String>,
                     private var ArtistonClickListener:ArtistOnClickListener):
                    RecyclerView.Adapter<ArtistsAdapter.ViewHolder>(){
    class ViewHolder(binding: ArtistItemBinding):RecyclerView.ViewHolder(binding.root){
        val ivArtistImg = binding.ivArtistImg
        val tvPosition = binding.tvPosition
        val tvArtistName = binding.tvArtistName
        val llArtist = binding.llArtist
    }

    interface ArtistOnClickListener {
        fun ArtistonClickListener(position: Int, artists: MutableList<String>, images: MutableList<String>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                ArtistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try{
            holder.tvPosition.text = "${position+1}"
            holder.tvArtistName.text = "Artist: ${artists[position].subSequence(1,artists[position].length-1)}"
            Glide.with(holder.ivArtistImg)
                .load(images[position].subSequence(1,images[position].length-1))
                .into(holder.ivArtistImg)
        }catch (e:Exception){
            Log.d("[ERROR]:", e.toString())
        }
        holder.llArtist.setOnClickListener {
            ArtistonClickListener.ArtistonClickListener(position, artists, images)
        }
    }

    override fun getItemCount(): Int {
        return artists.size
    }
}