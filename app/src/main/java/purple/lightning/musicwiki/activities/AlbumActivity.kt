package purple.lightning.musicwiki.activities

import android.content.Intent
import android.graphics.text.LineBreaker
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import purple.lightning.musicwiki.R
import purple.lightning.musicwiki.adapters.AAGenreAdapter
import purple.lightning.musicwiki.databinding.ActivityAlbumBinding
import purple.lightning.musicwiki.utils.ApiService
import purple.lightning.musicwiki.utils.ServiceGenerator

class AlbumActivity : AppCompatActivity(), AAGenreAdapter.AAGenreOnClickListener {
    private var binding:ActivityAlbumBinding? = null
    private val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)
    private var albums: MutableList<String> = mutableListOf()
    private var artists: MutableList<String> = mutableListOf()
    private var images:MutableList<String> = mutableListOf()
    private var topArtists:MutableList<String> = mutableListOf()
    private var topArtistsImages:MutableList<String> = mutableListOf()
    private var trackTitles:MutableList<String> = mutableListOf()
    private var trackArtists:MutableList<String> = mutableListOf()
    private var trackImages:MutableList<String> = mutableListOf()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // SETTING THE  CUSTOM TOOLBAR
        setSupportActionBar(binding?.toolbar)
        supportActionBar!!.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //TOOLBAR BACK BUTTON
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        val bundle = intent.extras
        Log.d("[CODE]:", "${bundle?.getString("img")}")

        try{
            binding?.ivAlbumImg?.let {
                Glide.with(it)
                    .load(bundle?.getString("img")!!.subSequence(1,bundle.getString("img")!!.length-1))
                    .into(binding?.ivAlbumImg!!)
            }

            binding?.tvAlbumTitle?.text = bundle?.getString("album")
            binding?.tvAlbumSummary?.text = bundle?.getString("summary")
            binding?.tvAlbumSummary?.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD)

            binding?.rvalbumAct?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
            val end = bundle?.getString("tagsB")?.length!!
            var adapterAA = AAGenreAdapter(bundle.getString("tagsB")?.subSequence(1, end-1)?.split(", ")!!.toMutableList(),this)
            binding?.rvalbumAct?.adapter = adapterAA
            binding?.albumPB?.visibility = View.GONE
            binding?.tvAboutArtist?.text = bundle.getString("artistInfo")!!.subSequence(1, bundle.getString("artistInfo")!!.length-1)
            binding?.tvAboutArtist?.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD)
        }catch (e:Exception){
            Log.d("[ERROR]:", "${e}")
        }


    }

    override fun AAGenreonClickListener(position: Int, list: MutableList<String>) {
        val intent = Intent(this@AlbumActivity, GenreInfoActivity::class.java)
        val bundle = Bundle()
        val mutex = Mutex()
        bundle.putString("GenreName", list[position].subSequence(1, list[position].length-1).toString())
        // TOP ALBUMS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getTopAlbums(list[position].subSequence(1, list[position].length-1).toString())
                albums.clear()
                artists.clear()
                images.clear()
                try{
                    call.execute().let {
                        Log.d("[TOP ALBUMS]:", "${it.body()!!.getAsJsonObject("albums").getAsJsonArray("album").size()}")
                        val point = it.body()!!.getAsJsonObject("albums").getAsJsonArray("album")
                        for(ele in 0 until point.size()){
                            val albumName = point.asJsonArray.get(ele).asJsonObject.get("name").toString()
                            val artistName = point.asJsonArray.get(ele).asJsonObject.get("artist").asJsonObject.get("name").toString()
                            val albumImg = point.asJsonArray.get(ele).asJsonObject.get("image").asJsonArray.get(1).asJsonObject.get("#text").toString()
                            albums.add(albumName)
                            artists.add(artistName)
                            images.add(albumImg)
                        }
                    }
                    if(albums.isNotEmpty() && artists.isNotEmpty()&&images.isNotEmpty()){
                        bundle.putString("Albums", albums.toString())
                        bundle.putString("Artists", artists.toString())
                        bundle.putString("Images", images.toString())
                        Log.d("[CODE]:", "Albums ${albums} Artist:${artists} Images:${images}")
                    }else{
                        recreate()
                    }
                }catch (e:Exception){
                    runOnUiThread{
                        Toast.makeText(this@AlbumActivity, "GO BACK->TRY AGAIN...${e}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // TOP ARTISTS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getTopArtists(list[position].subSequence(1, list[position].length-1).toString())
                topArtists.clear()
                topArtistsImages.clear()
                try{
                    call.execute().let {
                        val artistsLocal = it.body()!!.get("topartists").asJsonObject.get("artist").asJsonArray
                        for(i in 0 until artistsLocal.size()){
                            val artistName = artistsLocal.get(i).asJsonObject.get("name")
                            val artistImage = artistsLocal.get(i).asJsonObject.get("image").asJsonArray.get(1).asJsonObject.get("#text")
                            Log.d("[CODE]:", "ArtistName: ${artistName} ArtistImg: ${artistImage}")
                            topArtists.add(artistName.toString())
                            topArtistsImages.add(artistImage.toString())
                        }
                        Log.d("[CODE]:", "Artists: ${topArtists.size} Artists: ${topArtistsImages.size}")
                        if (topArtists.isNotEmpty() && topArtistsImages.isNotEmpty()){
                            bundle.putString("topArtist", topArtists.toString())
                            bundle.putString("topArtistsImages", topArtistsImages.toString())
                            Log.d("[CODE]:", "topArtists: ${topArtists} topArtistsImgs: ${topArtistsImages}")
                        }else{
                            recreate()
                        }
                    }
                }catch (e:Exception){
                    runOnUiThread{
                        Toast.makeText(this@AlbumActivity, "GO BACK->TRY AGAIN...${e}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // TOP TRACKS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getTopTracks(list[position].subSequence(1, list[position].length-1).toString())
                trackArtists.clear()
                trackImages.clear()
                trackTitles.clear()
                try{
                    call.execute().let {
                        val trackLocal = it.body()!!.get("tracks").asJsonObject.get("track").asJsonArray
                        for(i in 0 until trackLocal.size()){
                            val trackTitle = trackLocal.get(i).asJsonObject.get("name")
                            val trackArtist = trackLocal.get(i).asJsonObject.get("artist").asJsonObject.get("name")
                            val trackImg = trackLocal.get(i).asJsonObject.get("image").asJsonArray.get(1).asJsonObject.get("#text")
                            trackTitles.add(trackTitle.toString())
                            trackArtists.add(trackArtist.toString())
                            trackImages.add(trackImg.toString())
                        }
                        if(trackTitles.isNotEmpty() && trackArtists.isNotEmpty() && trackImages.isNotEmpty()){
                            bundle.putString("trackTitles", "${trackTitles}")
                            bundle.putString("trackArtists", "${trackArtists}")
                            bundle.putString("trackImages", "${trackImages}")
                            Log.d("[CODE]:", "trackTitles: $trackTitles} trackArtists: ${trackArtists} trackImages: ${trackImages}")
                        }else{
                            recreate()
                        }
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                }catch (e:Exception){
                    runOnUiThread{
                        Toast.makeText(this@AlbumActivity, "GO BACK->TRY AGAIN...${e}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}