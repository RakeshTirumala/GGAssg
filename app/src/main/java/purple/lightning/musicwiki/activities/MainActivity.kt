package purple.lightning.musicwiki.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import purple.lightning.musicwiki.R
import purple.lightning.musicwiki.adapters.GenresGridAdapter
import purple.lightning.musicwiki.databinding.ActivityMainBinding
import purple.lightning.musicwiki.utils.ApiService
import purple.lightning.musicwiki.utils.Constants
import purple.lightning.musicwiki.utils.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), GenresGridAdapter.GridOnClickListener {
    private var binding:ActivityMainBinding?=null
    private var genres:MutableList<String> = mutableListOf()
    private var value: Int = 10
    private val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)
    private var albums: MutableList<String> = mutableListOf()
    private var artists: MutableList<String> = mutableListOf()
    private var images:MutableList<String> = mutableListOf()
    private var topArtists:MutableList<String> = mutableListOf()
    private var topArtistsImages:MutableList<String> = mutableListOf()
    private var trackTitles:MutableList<String> = mutableListOf()
    private var trackArtists:MutableList<String> = mutableListOf()
    private var trackImages:MutableList<String> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = serviceGenerator.getTopGenres()
                call.enqueue(object : Callback<JsonObject> {
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        if(response.isSuccessful) {
                            Log.d("[CODE]:", response.body().toString())
                            response.body().let {

                                if (it != null) {
                                    Log.d("[CODE]:", "${it.getAsJsonObject("toptags").getAsJsonArray("tag").size()}")
                                    for (ele in 0 until it.getAsJsonObject("toptags").getAsJsonArray("tag").size()){
                                        genres.add(it.getAsJsonObject("toptags").getAsJsonArray("tag")[ele].toString().split(":")[1].split(",")[0])
                                    }
                                    Log.d("[CODE]:", genres.toString())
                                    setupGenreGridLayout(10)
                                    binding?.civArrow!!.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24)
                                    binding?.tvGenres?.visibility = View.VISIBLE
                                    binding?.cvGenres?.visibility = View.VISIBLE
                                    binding?.pbMainActivity?.visibility = View.GONE
                                    binding?.appName?.visibility = View.GONE
                                }
                            }
                        }
                        else {
                            Log.d("[CODE]:", "Failed to get ${response.errorBody()}")
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        t.printStackTrace()
                        Log.d("[CODE]: ","FAILED TO GET THE DATA ${t.message.toString()}")
                        Toast.makeText(this@MainActivity, "TIMEOUT. PLEASE CLOSE AND OPEN THE APP AGAIN", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                Log.d("[EXCEPTION]|:", "WHILE FETCHING DATA $e")
            }
        }


        binding?.civArrow?.setOnClickListener {
            when(value){
                10->{
                    setupGenreGridLayout(genres.size)
                    value = genres.size
                    binding?.civArrow!!.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24)
                }
                genres.size->{
                    setupGenreGridLayout(10)
                    value = 10
                    binding?.civArrow!!.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24)
                }
            }
        }


    }

    private fun setupGenreGridLayout(endV:Int){
        binding?.rvGenres?.layoutManager = GridLayoutManager(applicationContext, 2, LinearLayoutManager.VERTICAL, false)
        val rvGenresGridAdapter = GenresGridAdapter(genres.subList(0,endV), this)
        binding?.rvGenres?.adapter = rvGenresGridAdapter
    }

    override fun GridonClickListener(position: Int, genreName: String){
        val bundle = Bundle()
        val mutex = Mutex()
        bundle.putString("GenreName", genreName)
        // TOP ALBUMS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getTopAlbums(bundle.getString("GenreName").toString())
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
                    }else{
                        recreate()
                    }
                }catch (e:Exception){
                    Toast.makeText(this@MainActivity, "GO BACK->TRY AGAIN...", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // TOP ARTISTS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getTopArtists(bundle.getString("GenreName").toString())
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
                        }else{
                            recreate()
                        }
                    }
                }catch (e:Exception){
                    Toast.makeText(this@MainActivity, "GO BACK->TRY AGAIN...", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // TOP TRACKS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getTopTracks(bundle.getString("GenreName").toString())
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
                        }else{
                            recreate()
                        }
                        val intent = Intent(this@MainActivity, GenreInfoActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                }
            }catch (e:Exception){
                    Toast.makeText(this@MainActivity, "GO BACK->TRY AGAIN...", Toast.LENGTH_SHORT).show()
            }
            }
        }
    }
}
