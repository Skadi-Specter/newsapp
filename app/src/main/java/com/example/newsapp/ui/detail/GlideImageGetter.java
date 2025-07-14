package com.example.newsapp.ui.detail;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class GlideImageGetter implements Html.ImageGetter {

    private final Context context;
    private final TextView textView;

    public GlideImageGetter(Context context, TextView textView) {
        this.context = context;
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(String source) {
        BitmapDrawablePlaceHolder drawable = new BitmapDrawablePlaceHolder();

        Glide.with(context)
                .load(source)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        int width = getDeviceWidth();
                        if (width == 0) {
                            width = resource.getIntrinsicWidth();
                        }
                        int newHeight = (int) Math.floor((float) resource.getIntrinsicHeight() * (float) width / (float) resource.getIntrinsicWidth());

                        Rect rect = new Rect(0, 0, width, newHeight);
                        resource.setBounds(rect);

                        drawable.setBounds(rect);
                        drawable.setDrawable(resource);

                        textView.setText(textView.getText());
                        textView.invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // handle cleanup
                    }
                });

        return drawable;
    }

    private int getDeviceWidth() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels - textView.getPaddingLeft() - textView.getPaddingRight();
        }
        return 0;
    }

    private static class BitmapDrawablePlaceHolder extends BitmapDrawable {
        protected Drawable drawable;

        @Override
        public void draw(Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }
} 