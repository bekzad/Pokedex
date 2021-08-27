package edu.harvard.cs50.pokedexdemo;

public class Pokemon {
    private String name;
    private String url;
    private int id;

    public Pokemon(String name, String url, int id) {
        this.name = name;
        this.url = url;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getId() { return id; }

}
