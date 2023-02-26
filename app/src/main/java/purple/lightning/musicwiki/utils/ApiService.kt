package purple.lightning.musicwiki.utils

import android.app.DownloadManager
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.json.JSONArray
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface ApiService {
    @GET("/2.0/?method=tag.getTopTags&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getTopGenres():Call<JsonObject>

    @GET("/2.0/?method=tag.getinfo&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getGenreInfo(@Query("tag") genre:String):Call<JsonObject>

    @GET("/2.0/?method=tag.gettopalbums&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getTopAlbums(@Query("tag") genre:String):Call<JsonObject>

    @GET("/2.0/?method=tag.gettoptracks&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getTopTracks(@Query("tag") genre:String):Call<JsonObject>

    @GET("/2.0/?method=tag.gettopartists&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getTopArtists(@Query("tag") genre:String):Call<JsonObject>

    @GET("/2.0/?method=album.getinfo&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getAlbumInfo(@Query("artist") artist:String, @Query("album") album:String):Call<JsonObject>

    @GET("/2.0/?method=artist.getinfo&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getArtistInfo(@Query("artist") artist:String):Call<JsonObject>

    @GET("/2.0/?method=artist.gettoptracks&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getArtistTopTracks(@Query("artist") artist:String):Call<JsonObject>

    @GET("/2.0/?method=artist.gettopalbums&api_key=9df8dd9448980998112adcb12d11c818&format=json")
    fun getArtistTopAlbums(@Query("artist") artist:String):Call<JsonObject>
}

