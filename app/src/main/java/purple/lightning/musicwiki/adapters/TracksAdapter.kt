package purple.lightning.musicwiki.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import purple.lightning.musicwiki.databinding.RvItemBinding

class TracksAdapter(private var titles:MutableList<String>,
                    private var artists:MutableList<String>,
                    private var images:MutableList<String>,
                    private var TrackonClickListener:TrackOnClickListener):
                    RecyclerView.Adapter<TracksAdapter.ViewHolder>(){
    class ViewHolder(binding:RvItemBinding):RecyclerView.ViewHolder(binding.root){
        val tvTrackTitle = binding.tvObj1
        val ivTrackImg = binding.ivObj
        val tvPosition = binding.tvPosition
        val tvTrackArtist = binding.tvObj2
    }

    interface TrackOnClickListener {
        fun TrackonClickListener(position: Int)
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
            holder.tvTrackTitle.text = "Title: ${titles[position].subSequence(1, titles[position].length-1)}"
            holder.tvTrackArtist.text = "Artist: ${artists[position].subSequence(1, artists[position].length-1)}"
            Glide.with(holder.ivTrackImg)
                .load(images[position].subSequence(1,images[position].length-1))
                .into(holder.ivTrackImg)
        }catch (e:Exception){
            Log.d("[ERROR]:", e.toString())
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}