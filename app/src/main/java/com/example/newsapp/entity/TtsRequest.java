package com.example.newsapp.entity;

import com.google.gson.annotations.SerializedName;

public class TtsRequest {
    public String model;
    public String input;
    public String voice;
    @SerializedName("response_format")
    public String responseFormat;
    @SerializedName("sample_rate")
    public int sampleRate;
    public boolean stream;
    public double speed;
    public double gain;

    public TtsRequest(String input) {
        this.model = "FunAudioLLM/CosyVoice2-0.5B";
        this.input = input;
        this.voice = "FunAudioLLM/CosyVoice2-0.5B:alex";
        this.responseFormat = "mp3";
        this.sampleRate = 32000;
        this.stream = false; // Streaming playback is more complex, so we'll get the full file first
        this.speed = 1.0;
        this.gain = 0;
    }
} 