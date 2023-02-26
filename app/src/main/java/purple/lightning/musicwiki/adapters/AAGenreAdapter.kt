package purple.lightning.musicwiki.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import purple.lightning.musicwiki.databinding.RvAlbumItemBinding
import java.text.FieldPosition

class AAGenreAdapter(private var list:MutableList<String>, private var AAGenreonClickListener:AAGenreOnClickListener):
    RecyclerView.Adapter<AAGenreAdapter.ViewHolder>(){
        interface AAGenreOnClickListener{
            fun AAGenreonClickListener(position: Int, list:MutableList<String>)
        }

    class ViewHolder(binding:RvAlbumItemBinding):RecyclerView.ViewHolder(binding.root){
        var cvGenre = binding.cvGenre
        var tvGenreNameAA = binding.tvGenreNameAA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RvAlbumItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvGenreNameAA.text = list[position].subSequence(1,list[position].length-1)
        holder.cvGenre.setOnClickListener {
            AAGenreonClickListener.AAGenreonClickListener(position, list)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}