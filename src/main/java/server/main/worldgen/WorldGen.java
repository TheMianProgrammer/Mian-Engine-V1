package server.main.worldgen;

import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;

public class WorldGen {
    /// OVEERWORLD
    PerlinNoiseGenerator overworldNoise_base;
    PerlinNoiseGenerator overworldNoise_detail;
    PerlinNoiseGenerator overworldNoise_detailAmplitude;
    PerlinNoiseGenerator overworldNoise_mountains;
    PerlinNoiseGenerator overworldNoise_mountainsAmplitude;

    public WorldGen(int seed)
    {
        overworldNoise_base = PerlinNoiseGenerator.newBuilder()
            .setSeed((long)seed)
            .build();
        overworldNoise_detail = PerlinNoiseGenerator.newBuilder()
            .setSeed((long)seed+1)
            .build();
        overworldNoise_detailAmplitude = PerlinNoiseGenerator.newBuilder()
            .setSeed((long)seed+2)
            .build();
        overworldNoise_mountains = PerlinNoiseGenerator.newBuilder()
            .setSeed((long)seed+3)
            .build();
        overworldNoise_mountainsAmplitude = PerlinNoiseGenerator.newBuilder()
            .setSeed((long)seed+4)
            .build();
    }
    
    public boolean isInCave(int x, int y, int z)
    {
        return false;
    }

    public int GetOverworldY(int x, int z)
    {
        double base = overworldNoise_base.evaluateNoise(x*0.009, 0, z*0.009) * 60f;
        double detail = overworldNoise_detail.evaluateNoise(x*0.006, 0, z*0.006)*10f;
        double detailAmp = overworldNoise_detailAmplitude.evaluateNoise(x*0.0005, 0, z*0.0005)*100f;
        double montain = overworldNoise_mountains.evaluateNoise(x*0.001, 0, z*0.001) * 40f;
        double montainAmp = overworldNoise_mountainsAmplitude.evaluateNoise(x*0.00009, 0, z*0.00009) *100;
        if(montain < 0) montain = -montain;

        detail *= 1.0 - Math.max(-1, Math.max(-1, detailAmp));
        detail *= 0.2f;

        montain *= Math.max(-10, montainAmp);

        base = Math.max(0, base);

        //int height = (int)((base+detail)*(montain*0.5f));
        int height = (int)((base+detail)*2f+montain*0.5f);
        return (int)height;
    }

    float smoothstep(float edge0, float edge1, float x) {
        x = (x - edge0) / (edge1 - edge0);
        x = Math.max(0f, Math.min(1f, x));
        return x * x * (3 - 2 * x);
    }
}
