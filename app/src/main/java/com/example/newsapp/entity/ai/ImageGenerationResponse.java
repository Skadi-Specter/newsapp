package com.example.newsapp.entity.ai;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ImageGenerationResponse {
    @SerializedName("images")
    public List<ImageData> images;
    
    @SerializedName("timings")
    public Timings timings;
    
    @SerializedName("seed")
    public long seed;
    
    public static class Timings {
        @SerializedName("inference")
        public double inference;
    }
} 