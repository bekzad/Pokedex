package edu.harvard.cs50.pokedexdemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.cs50.pokedexdemo.databinding.PokedexRowBinding;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {

    private PokedexRowBinding binding;
    private RequestQueue requestQueue;

    public class PokedexViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView textView;

        public PokedexViewHolder(View view) {
            super(view);
            containerView = binding.pokedexRow;
            textView = binding.pokedexRowTextView;

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());
                    intent.putExtra("name", current.getName());
                    intent.putExtra("id", current.getId());

                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    List<Pokemon> pokemons = new ArrayList<>();
    // The reason we could change reference of filtered in the method is
    // that these are instance variables, they are not passed into methods
    // our methods can use them
    List<Pokemon> filtered = pokemons;

    public PokedexAdapter(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        loadPokemon();
    }

    private void loadPokemon() {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    String name;
                    for (int i = 0, n = results.length(); i < n; i++) {
                        JSONObject result = results.getJSONObject(i);
                        name = result.getString("name");

                        pokemons.add(new Pokemon(
                                name.substring(0, 1).toUpperCase() + name.substring(1),
                                result.getString("url"),
                                i + 1
                                ));
                    }
                    notifyDataSetChanged();
                }
                catch(JSONException e) {
                    Log.e("CS50", "Json exception such as compatibility in Recycler View controller");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("CS50","General internet error in Recycler view Controller");
            }
        });

        requestQueue.add(request);
    }

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = PokedexRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        View view = binding.getRoot();
        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon current = filtered.get(position);
        holder.textView.setText(current.getName());
        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    private class PokemonFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Pokemon> filteredPokemon = new ArrayList<>();
            String searchedString = constraint.toString().toLowerCase();
            for (Pokemon pokemon : pokemons) {
                if (pokemon.getName().toLowerCase().contains(searchedString)) {
                    filteredPokemon.add(pokemon);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredPokemon;
            results.count = filteredPokemon.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (List<Pokemon>) results.values;
            notifyDataSetChanged();
        }
    }
}
