package com.example.newsapp.entity.ai;

import com.google.gson.annotations.SerializedName;

public class ImageGenerationRequest {
    public String model;
    public String prompt;
    @SerializedName("negative_prompt")
    public String negativePrompt;
    @SerializedName("image_size")
    public String imageSize;
    @SerializedName("batch_size")
    public int batchSize;
    public long seed;
    @SerializedName("num_inference_steps")
    public int numInferenceSteps;
    @SerializedName("guidance_scale")
    public double guidanceScale;

    public ImageGenerationRequest(String prompt) {
        this.model = "Kwai-Kolors/Kolors";
        this.prompt = prompt;
        this.negativePrompt = ""; // Default to empty
        this.imageSize = "1024x1024";
        this.batchSize = 1;
        this.seed = 4999999999L; // Use L for long
        this.numInferenceSteps = 20;
        this.guidanceScale = 7.5;
    }
} 