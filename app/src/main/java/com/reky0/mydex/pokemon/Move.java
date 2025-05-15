package com.reky0.mydex.pokemon;

import java.util.ArrayList;

public class Move {
    private int accuracy;
    private String name;
    private int power;
    private int pp;
    private Type type;
    private DamageClass damage_class;
    private ArrayList<EffectEntry> effect_entries;

    public int getAccuracy() {
        return accuracy;
    }

    public String getName() {
        return name;
    }

    public int getPower() {
        return power;
    }

    public int getPp() {
        return pp;
    }

    public Type getType() {
        return type;
    }

    public DamageClass getDamage_class() {
        return damage_class;
    }

    public ArrayList<EffectEntry> getEffect_entries() {
        return effect_entries;
    }

    public static class Type {
        private String name;

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Type{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static class DamageClass {
        String name;

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "DamageClass{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static class EffectEntry {
        private String short_effect;
        private Language language;

        public String getShort_effect() {
            return short_effect;
        }

        public Language getLanguage() {
            return language;
        }

        @Override
        public String toString() {
            return "EffectEntry{" +
                    "short_effect='" + short_effect + '\'' +
                    ", language=" + language +
                    '}';
        }

        public static class Language {
            private String name;

            public String getName() {
                return name;
            }

            @Override
            public String toString() {
                return "Language{" +
                        "name='" + name + '\'' +
                        '}';
            }
        }
    }
}
