package purple.lightning.musicwiki.activities

import android.content.Intent
import android.graphics.text.LineBreaker
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import purple.lightning.musicwiki.R
import purple.lightning.musicwiki.adapters.AlbumsAdapter
import kotlinx.serialization.json.*
import purple.lightning.musicwiki.adapters.ArtistsAdapter
import purple.lightning.musicwiki.adapters.TracksAdapter
import purple.lightning.musicwiki.databinding.ActivityGenreInfoBinding

import purple.lightning.musicwiki.utils.ApiService
import purple.lightning.musicwiki.utils.ServiceGenerator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GenreInfoActivity : AppCompatActivity(), AlbumsAdapter.AlbumOnClickListener, ArtistsAdapter.ArtistOnClickListener,TracksAdapter.TrackOnClickListener {
    private var binding:ActivityGenreInfoBinding?=null
    private var summary:String = ""
    private val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)
    private var albmL = ""
    private var artLa = ""
    private var artLar = ""
    private var imgL = ""
    private var artImgL = ""
    private val mutex = Mutex()
//    private var albums: MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenreInfoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val bundle  = intent.extras
        binding?.tvGenreName?.text = bundle?.getString("GenreName").toString()

        // SETTING THE  CUSTOM TOOLBAR
        setSupportActionBar(binding?.toolbar)
        supportActionBar!!.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //TOOLBAR BACK BUTTON
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.rvWiki?.visibility = View.VISIBLE
        binding?.rvArtistsWiki?.visibility = View.GONE
        binding?.rvTracksWiki?.visibility = View.GONE
        albmL = bundle!!.getString("Albums")!!
        artLa = bundle.getString("Artists")!!
        imgL = bundle.getString("Images")!!
        Log.d("[CODE]:", "${albmL.substring(1, albmL.length - 1).split(", ").toMutableList()}")
        binding?.rvWiki?.layoutManager = LinearLayoutManager(this@GenreInfoActivity)
        val adapter = AlbumsAdapter(albmL.substring(1, albmL.length - 1).split(", ").toMutableList(),
            artLa.substring(1, artLa.length - 1).split(", ").toMutableList(),
            imgL.substring(1, imgL.length - 1).split(", ").toMutableList(),
            this)
        binding?.rvWiki?.adapter = adapter

        // FETCHING DATA FOR GENRE SUMMARY
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = serviceGenerator.getGenreInfo(bundle.getString("GenreName").toString())
                call.enqueue(object : Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.Q)
                    override fun onResponse(
                        call: Call<JsonObject>,
                        response: Response<JsonObject>
                    ) {
                        if(response.isSuccessful) {
                            Log.d("[CODE]:", response.body().toString())
                            response.body().let {
                                if (it != null) {
                                    Log.d("[summary]:", "${it.getAsJsonObject("tag").getAsJsonObject("wiki").get("summary")}")
                                    summary = it.getAsJsonObject("tag").getAsJsonObject("wiki").get("summary").toString()
                                    binding?.tvGenreSummary?.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD)
                                    binding?.tvGenreSummary?.text = summary.subSequence(1,summary.length-1)
                                    binding?.pbGenreInfoActivity?.visibility = View.GONE

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
                        Toast.makeText(this@GenreInfoActivity, "TIMEOUT. PLEASE TRY AGAIN", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                Log.d("[EXCEPTION]|:", "WHILE FETCHING DATA $e")
            }
        }


        binding?.cvAlbums?.setOnClickListener {
            try{
                binding?.rvWiki?.visibility = View.VISIBLE
                binding?.rvArtistsWiki?.visibility = View.GONE
                binding?.rvTracksWiki?.visibility = View.GONE
                binding?.rvWiki?.layoutManager = LinearLayoutManager(this@GenreInfoActivity)
                val adapter = AlbumsAdapter(albmL.substring(1, albmL.length - 1).split(", ").toMutableList(),
                    artLa.substring(1, artLa.length - 1).split(", ").toMutableList(),
                    imgL.substring(1, imgL.length - 1).split(", ").toMutableList(),
                    this)
                binding?.rvWiki?.adapter = adapter
            }catch (e:Exception){
                Toast.makeText(this@GenreInfoActivity, "TRY AGAIN...", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        binding?.cvArtists?.setOnClickListener {
            try{
                binding?.rvWiki?.visibility = View.GONE
                binding?.rvArtistsWiki?.visibility = View.VISIBLE
                binding?.rvTracksWiki?.visibility = View.GONE
                artLar = bundle.getString("topArtist")!!
                artImgL = bundle.getString("topArtistsImages")!!
                binding?.rvArtistsWiki?.layoutManager = LinearLayoutManager(this@GenreInfoActivity)
                val adapter = ArtistsAdapter(artLar.substring(1, artLar.length - 1).split(", ").toMutableList(),
                    artImgL.substring(1, artImgL.length - 1).split(", ").toMutableList(),
                    this)
                binding?.rvArtistsWiki?.adapter = adapter
            }catch(e:Exception){
                Toast.makeText(this@GenreInfoActivity, "TRY AGAIN...", Toast.LENGTH_SHORT).show()
                Log.d("[ERROR]:", e.toString())
                finish()
            }
        }
        binding?.cvTracks?.setOnClickListener {
            try{
                binding?.rvWiki?.visibility = View.GONE
                binding?.rvArtistsWiki?.visibility = View.GONE
                binding?.rvTracksWiki?.visibility = View.VISIBLE
                val trackTitles = bundle.getString("trackTitles")
                val trackArtists = bundle.getString("trackArtists")
                val trackImages = bundle.getString("trackImages")
                binding?.rvTracksWiki?.layoutManager = LinearLayoutManager(this@GenreInfoActivity)
                val adapter = TracksAdapter(trackTitles?.substring(1, trackTitles.length - 1)?.split(", ")!!.toMutableList(),
                    trackArtists?.substring(1, trackArtists.length - 1)?.split(", ")!!.toMutableList(),
                    trackImages?.substring(1, trackImages.length - 1)?.split(", ")!!.toMutableList(),
                    this)
                binding?.rvTracksWiki?.adapter = adapter
            }catch (e:Exception){
                Toast.makeText(this@GenreInfoActivity, "TRY AGAIN...", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun AlbumonClickListener(position: Int) {
        val album = albmL.substring(1, albmL.length - 1).split(", ").toMutableList()
        val artist = artLa.substring(1, artLa.length - 1).split(", ").toMutableList()
        val tagsB = mutableListOf<String>()
        var img = ""
        var summary = ""
        val handler = Handler(Looper.getMainLooper())
        val bundle = Bundle()
        // FETCHING ARTIST INFO
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getArtistInfo(
                    artist[position].subSequence(
                        1,
                        artist[position].length - 1
                    ).toString()
                )
                Log.d(
                    "[TAGS]:",
                    artist[position].subSequence(1, artist[position].length - 1).toString()
                )
                call.execute().let {
                    try {
                        val artistInfo = it.body()!!
                            .get("artist").asJsonObject.get("bio").asJsonObject.get("summary")
                        bundle.putString("artistInfo", artistInfo.toString())
                    } catch (e: Exception) {
                        handler.post {
                            Toast.makeText(
                                this@GenreInfoActivity,
                                "GO BACK->TRY AGAIN...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }


        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getAlbumInfo(
                    artist[position].subSequence(
                        1,
                        artist[position].length - 1
                    ).toString(),
                    album[position].subSequence(1, album[position].length - 1).toString()
                )
                Log.d("[TAGS]:", "${artist[position]} ${album[position]}")
                call.execute().let {
                    try {
                        val tags =
                            it.body()!!.asJsonObject.get("album").asJsonObject.get("tags").asJsonObject.get(
                                "tag"
                            ).asJsonArray
                        img =
                            it.body()!!.asJsonObject.get("album").asJsonObject.get("image").asJsonArray.get(
                                2
                            ).asJsonObject.get("#text").toString()
                        summary =
                            it.body()!!.asJsonObject.get("album").asJsonObject.get("wiki").asJsonObject.get(
                                "summary"
                            ).toString()
                        for (ele in tags) {
                            tagsB.add(ele.asJsonObject.get("name").toString())
                        }
                        Log.d("[TAGS]:", "${tagsB}")
//                        val bundle = Bundle()
                        bundle.putString(
                            "album",
                            album[position].subSequence(1, album[position].length - 1)
                                .toString()
                        )
                        bundle.putString(
                            "artist",
                            artist[position].subSequence(1, artist[position].length - 1)
                                .toString()
                        )
                        bundle.putString("tagsB", "${tagsB}")
                        bundle.putString("img", img)
                        bundle.putString(
                            "summary",
                            summary.subSequence(1, summary.length - 1).toString()
                        )
                        val intent = Intent(this@GenreInfoActivity, AlbumActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    } catch (e: Exception) {
                        handler.post {
                            Toast.makeText(
                                this@GenreInfoActivity,
                                "GO BACK->TRY AGAIN...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        }
    }

    override fun ArtistonClickListener(position: Int, artists:MutableList<String>, images:MutableList<String>){
        val bundle = Bundle()
        bundle.putString("artistName", "${artists[position].subSequence(1,artists[position].length-1)}")
        bundle.putString("artistImg", "${images[position].subSequence(1,images[position].length-1)}")
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getArtistInfo("${artists[position].subSequence(1,artists[position].length-1)}")
                call.execute().let {
                    var followers = it.body()!!.get("artist").asJsonObject.get("stats").asJsonObject.get("listeners").toString()
                    var playcount = it.body()!!.get("artist").asJsonObject.get("stats").asJsonObject.get("playcount").toString()
                    val tagsP = it.body()!!.get("artist").asJsonObject.get("tags").asJsonObject.get("tag").asJsonArray
                    var tags = mutableListOf<String>()
                    for(ele in 0 until tagsP.size()){
                        tags.add(tagsP.get(ele).asJsonObject.get("name").toString())
                    }
                    Log.d("[FOLLOWERS]:", followers)
                    followers = adjustString(followers.subSequence(1, followers.length-1).toString())
                    playcount = adjustString(playcount.subSequence(1, playcount.length-1).toString())
                    bundle.putString("followers", followers)
                    bundle.putString("playcount", playcount)
                    bundle.putString("tags", tags.toString())
                }
            }
        }
        // FETCHING TRACKS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                Log.d("[ARTIST NAME]: ", "${artists[position].subSequence(1,artists[position].length-1)}")
                val call = serviceGenerator.getArtistTopTracks("${artists[position].subSequence(1,artists[position].length-1)}")
                call.execute().let {
                    val trackP = it.body()!!.get("toptracks").asJsonObject.get("track").asJsonArray
                    Log.d("[]: ", "${trackP.size()}")
                    val trackNames = mutableListOf<String>()
                    val trackImgs = mutableListOf<String>()
                    for(ele in 0 until trackP.size()){
                        val trackName = trackP.get(ele).asJsonObject.get("name")
                        val trackImg = trackP.get(ele).asJsonObject.get("image").asJsonArray.get(1).asJsonObject.get("#text")
                        trackImgs.add(trackImg.toString())
                        trackNames.add(trackName.toString())
                    }
                    bundle.putString("trackNames", "${trackNames}")
                    bundle.putString("trackImgs", "${trackImgs}")
                    Log.d("trackNames: ", "${trackNames}")
                }
            }
        }

        // FETCHING ALBUMS
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getArtistTopAlbums("${artists[position].subSequence(1,artists[position].length-1)}")
                call.execute().let {
                    val albumP = it.body()!!.get("topalbums").asJsonObject.get("album").asJsonArray
                    val artistTopAlbums = mutableListOf<String>()
                    val topAlbumsImgs = mutableListOf<String>()
                    val albumArtists = mutableListOf<String>()
                    for(ele in 0 until albumP.size()){
                        val albumName = albumP.get(ele).asJsonObject.get("name")
                        val albumImg = albumP.get(ele).asJsonObject.get("image").asJsonArray.get(1).asJsonObject.get("#text")
                        val albumArtist = albumP.get(ele).asJsonObject.get("artist").asJsonObject.get("name")
                        artistTopAlbums.add(albumName.toString())
                        topAlbumsImgs.add(albumImg.toString())
                    }
                    bundle.putString("artistTopAlbums", "${artistTopAlbums}")
                    bundle.putString("topAlbumsImgs", "${topAlbumsImgs}")
                    bundle.putString("albumArtists", "${albumArtists}")
                    val intent = Intent(this@GenreInfoActivity, ArtistActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
        }
    }

    override fun TrackonClickListener(position: Int) {
        TODO("Not yet implemented")
    }

    private fun adjustString(str:String):String{
        var res = ""
        if(str.length==4||str.length==5||str.length==6){
            res = "%.1fK".format(str.toInt() / 1000.0)
        }
        else if (str.length>6){
            res = "%.1fM".format(str.toInt()/ 1000000.0)
        }
        else{
            res = str
        }
        return res
    }
}

