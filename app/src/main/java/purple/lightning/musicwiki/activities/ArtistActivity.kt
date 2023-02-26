package purple.lightning.musicwiki.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import purple.lightning.musicwiki.adapters.AAGenreAdapter
import purple.lightning.musicwiki.adapters.ArtistAlbumAdapter
import purple.lightning.musicwiki.adapters.ArtistTracksAdapter
import purple.lightning.musicwiki.databinding.ActivityArtistBinding
import purple.lightning.musicwiki.utils.ApiService
import purple.lightning.musicwiki.utils.ServiceGenerator

class ArtistActivity : AppCompatActivity(), AAGenreAdapter.AAGenreOnClickListener, ArtistAlbumAdapter.ArtistAlbumOnClickListner {
    private var binding:ActivityArtistBinding? = null
    private val serviceGenerator = ServiceGenerator.buildService(ApiService::class.java)
    private var albums: MutableList<String> = mutableListOf()
    private var artists: MutableList<String> = mutableListOf()
    private var images:MutableList<String> = mutableListOf()
    private var topArtists:MutableList<String> = mutableListOf()
    private var topArtistsImages:MutableList<String> = mutableListOf()
    private var trackTitles:MutableList<String> = mutableListOf()
    private var trackArtists:MutableList<String> = mutableListOf()
    private var trackImages:MutableList<String> = mutableListOf()
    val mutex = Mutex()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtistBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val bundle = intent.extras

        // SETTING THE  CUSTOM TOOLBAR
        setSupportActionBar(binding?.toolbar)
        supportActionBar!!.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //TOOLBAR BACK BUTTON
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.ivArtistImg?.let {
            Glide.with(it)
                .load(bundle?.getString("artistImg"))
                .into(binding?.ivArtistImg!!)
        }
        binding?.tvArtistName?.text = bundle?.getString("artistName")
        binding?.tvFollowers?.text = "${bundle?.getString("followers")} Followers"
        binding?.tvPlayCount?.text = "${bundle?.getString("playcount")} Playcount"

        // SETTING UP RV GENRES
        binding?.rvGenres?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val tags = bundle?.getString("tags")
        var adapterAA = AAGenreAdapter(tags?.subSequence(1,tags.length-1)?.split(", ")!!.toMutableList(), this)
        binding?.rvGenres?.adapter = adapterAA

        //SETTING UP RV TRACKS
        val artistName = bundle.getString("artistName")
        try{
            val trackNames = bundle.getString("trackNames")
            val trackImgs = bundle.getString("trackImgs")
            Log.d("[ARTIST ACTIVITY]: ", "${trackNames}")
            Log.d("[ARTIST ACTIVITY]: ", "${trackImgs}")
            binding?.rvArtistTopTracks?.layoutManager = GridLayoutManager(this, 50)
            var adapterATracks = ArtistTracksAdapter(artistName.toString(),
                trackNames?.subSequence(1, trackNames.length-1)?.split(", ")!!.toMutableList(),
                trackImgs?.subSequence(1, trackImgs.length-1)?.split(", ")!!.toMutableList()
            )
            binding?.rvArtistTopTracks?.adapter = adapterATracks
        }catch (e:Exception){
            Toast.makeText(this, "${e}", Toast.LENGTH_SHORT).show()
        }

        // SETTING UP RV ALBUMS
        try{
            val albums = bundle.getString("artistTopAlbums")
            val images = bundle.getString("topAlbumsImgs")
            val artists = bundle.getString("albumArtists")
            binding?.rvArtistTopAlbums?.layoutManager = GridLayoutManager(this, 50)
            var adapterAAlbums = ArtistAlbumAdapter(artistName.toString(),
                albums?.subSequence(1, albums.length-1)?.split(", ")!!.toMutableList(),
                images?.subSequence(1, images.length-1)?.split(", ")!!.toMutableList(),
                artists?.subSequence(1, artists.length-1)?.split(", ")!!.toMutableList(),
                this)
            binding?.rvArtistTopAlbums?.adapter = adapterAAlbums
        }catch (e:Exception){
            Toast.makeText(this, "${e}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun AAGenreonClickListener(position: Int, list: MutableList<String>) {
        val intent = Intent(this@ArtistActivity, GenreInfoActivity::class.java)
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
                        Toast.makeText(this@ArtistActivity, "GO BACK->TRY AGAIN...${e}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@ArtistActivity, "GO BACK->TRY AGAIN...${e}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@ArtistActivity, "GO BACK->TRY AGAIN...${e}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun ArtistAlbumonClickListner(position: Int, albums:MutableList<String>, images:MutableList<String>, artists:MutableList<String>) {
        var album = albums[position]
        album = album.subSequence(1, album.length-1).toString()
        var image = images[position]
        image = image.subSequence(1, image.length-1).toString()
        var artist = artists[position]
        artist = artist.subSequence(1, artist.length-1).toString()
        var summary = ""
        var tagsB = mutableListOf<String>()
        var bundle = Bundle()

        // FETCHING SUMMARY OF ALBUM
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getAlbumInfo(artist,album)
                call.execute().let {
                    summary = it.body()!!.get("album").asJsonObject.get("wiki").asJsonObject.get("summary").toString()
                    val tagsL = it.body()!!.get("album").asJsonObject.get("tags").asJsonObject.get("tag").asJsonArray
                    for(ele in 0 until tagsL.size()){
                        tagsB.add(tagsL.get(ele).asJsonObject.get("name").toString())
                    }
                    bundle.putString("summary", "${summary}")
                    bundle.putString("img", "${image}")
                    bundle.putString("album", "${album}")
                    bundle.putString("tagsB", tagsB.toString())
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                val call = serviceGenerator.getArtistInfo(artist)
                call.execute().let {
                    val artistInfo = it.body()!!.get(artist).asJsonObject.get("bio").asJsonObject.get("summary")
                    bundle.putString("artistInfo", artistInfo.toString())

                    val intent = Intent(this@ArtistActivity, AlbumActivity::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
        }
    }
}