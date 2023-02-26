package purple.lightning.musicwiki.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import purple.lightning.musicwiki.databinding.ArtisitActivtiyRvItemBinding

class ArtistTracksAdapter(private var artistName:String,
private var trackNames:MutableList<String>,
private var trackImgs:MutableList<String>):RecyclerView.Adapter<ArtistTracksAdapter.ViewHolder>(){
    class ViewHolder(binding:ArtisitActivtiyRvItemBinding):RecyclerView.ViewHolder(binding.root){
        var tvObj1 = binding.tvObj1
        var tvObj2 = binding.tvObj2
        var ivRvvImg = binding.ivRVvImg
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
            holder.tvObj1.text = trackNames[position].subSequence(1, trackNames[position].length-1)
            holder.tvObj2.text = artistName
            Glide.with(holder.ivRvvImg)
                .load(trackImgs[position].subSequence(1,trackImgs[position].length-1))
                .into(holder.ivRvvImg)
        }catch (e:Exception){
            Log.d("[ARTIST ALBUM ADAPTER]:", "${e}")
        }
    }

    override fun getItemCount(): Int {
        return trackNames.size
    }
}