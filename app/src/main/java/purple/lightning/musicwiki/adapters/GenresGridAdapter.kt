package purple.lightning.musicwiki.adapters

import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import purple.lightning.musicwiki.databinding.GenreItemBinding

class GenresGridAdapter(private var genresList: MutableList<String>,
                        private val GridonClickListener: GridOnClickListener):
RecyclerView.Adapter<GenresGridAdapter.ViewHolder>(){

    interface GridOnClickListener{
        fun GridonClickListener(position: Int, genreName:String)
    }

    class ViewHolder(binding: GenreItemBinding):RecyclerView.ViewHolder(binding.root){
        var genreItemName = binding.genreItemName
        var cvItemGenre = binding.cvItemGenre
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            GenreItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = genresList[position]
        val temp = name.split("""""")
        var subS = ""
        for(c in 2 until temp.size-2){
            subS+=temp[c]
        }
        holder.genreItemName.text = subS
        holder.genreItemName.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD)
        holder.cvItemGenre.setOnClickListener {
            GridonClickListener.GridonClickListener(position, subS)
        }
    }

    override fun getItemCount(): Int {
        return genresList.size
    }
}