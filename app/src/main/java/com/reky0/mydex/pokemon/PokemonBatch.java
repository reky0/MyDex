package com.reky0.mydex.pokemon;

import java.util.ArrayList;

public class PokemonBatch {
    private ArrayList<PokemonBatchSlot> batch;

    public PokemonBatch() {
        this.batch = new ArrayList<>();
    }

    public ArrayList<PokemonBatchSlot> getBatch() {
        return batch;
    }

    public void setBatch(ArrayList<PokemonBatchSlot> batch) {
        this.batch = batch;
    }

    @Override
    public String toString() {
        return "PokemonBatch{" +
                "batch=" + batch +
                '}';
    }

    public class PokemonBatchSlot {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "PokemonBatchSlot{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }
}
