package edu.harvard.cs50.pokedexdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import edu.harvard.cs50.pokedexdemo.databinding.ActivityPokemonBinding;

public class PokemonActivity extends AppCompatActivity {

    private ActivityPokemonBinding binding;
    private RequestQueue requestQueue;
    private String url;

    // Shared preferences object
    private SharedPreferences caughtPokemon;
    private String name;
    private int id;
    private boolean isCaught;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up binding
        binding = ActivityPokemonBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        url = getIntent().getStringExtra("url");
        name = getIntent().getStringExtra("name");
        id = getIntent().getIntExtra("id", 0);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        binding.pokemonName.setText(name);
        binding.pokemonNumber.setText(String.format("#%03d", id));

        // This code to get the data from a disc
        caughtPokemon = getPreferences(Context.MODE_PRIVATE);
        isCaught = caughtPokemon.getBoolean(name, false);

        if (!isCaught) {
            binding.pokemonButtonCatch.setText("Catch");
        } else {
            binding.pokemonButtonCatch.setText("Release");
        }

        // Load from API
        load();
        loadDescription();
    }

    private void load() {
        binding.pokemonType1.setText("");
        binding.pokemonType2.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Get the url for downloading an image and download it
                    String spriteUrl = response.getJSONObject("sprites").getString("front_shiny");
                    new DownloadSpriteTask().execute(spriteUrl);

                    JSONArray types = response.getJSONArray("types");
                    for (int i = 0, n = types.length(); i < n; i++) {
                        JSONObject type = types.getJSONObject(i);
                        int slot = type.getInt("slot");
                        String typeName = type.getJSONObject("type").getString("name");
                        if (slot == 1) {
                            binding.pokemonType1.setText(typeName);
                        } else if (slot == 2) {
                            binding.pokemonType2.setText(typeName);
                        }
                    }
                }
                catch(JSONException e) {
                    Log.e("CS50", "Json exception such as compatibility in Pokemon details");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CS50","General internet error in Pokemon details");
            }
        });
        requestQueue.add(request);
    } // Load method

    private void loadDescription() {
        String urlDescription = String.format("https://pokeapi.co/api/v2/pokemon-species/%d/", id);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlDescription, null, response -> {
            try {
                String description = "";
                JSONArray flavorTextEntries = response.getJSONArray("flavor_text_entries");
                for (int i = 0, n = flavorTextEntries.length(); i < n; i++) {
                    JSONObject flavorText = flavorTextEntries.getJSONObject(i);
                    String language = flavorText.getJSONObject("language").getString("name");
                    if (language.equals("en")) {
                        description = flavorText.getString("flavor_text");
                        break;
                    }
                }
                binding.pokemonDescription.setText(description);
            }
            catch(JSONException e) {
                Log.e("CS50", "Json exception such as compatibility in Pokemon details");
            }
        }, error -> Log.e("CS50","General internet error in Pokemon details"));
        requestQueue.add(request);
    } // Load Description method

    public void toggleCatch(View view) {

        isCaught = !isCaught;

        if (!isCaught) {
            binding.pokemonButtonCatch.setText("Catch");
        } else {
            binding.pokemonButtonCatch.setText("Release");
        }
    }

    // This is to save the data into a disc
    @Override
    protected void onPause() {
        super.onPause();
        caughtPokemon.edit().putBoolean(name, isCaught).apply();
    }


    // A class to asyncronously download an image input String url output bitmap
    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            binding.pokemonAvatar.setImageBitmap(bitmap);
        }
    }
}