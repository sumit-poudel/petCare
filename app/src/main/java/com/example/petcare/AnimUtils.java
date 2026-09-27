package com.example.petcare;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

public class AnimUtils {

    public static void bounceClick(View view) {

        view.setOnTouchListener((v, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    v.animate()
                            .scaleX(0.94f)
                            .scaleY(0.94f)
                            .setDuration(100)
                            .start();

                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start();

                    break;

                default:
                    break;
            }

            return false;
        });
    }

    public static void popIn(View view) {

        view.startAnimation(
                AnimationUtils.loadAnimation(
                        view.getContext(),
                        R.anim.fab_pop_in
                )
        );
    }

    public static void slideForward(Activity activity) {

        activity.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );
    }

    public static void slideBack(Activity activity) {

        activity.overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
    }

    public static void fadeIn(View view) {

        view.setAlpha(0f);

        view.animate()
                .alpha(1f)
                .setDuration(400)
                .start();
    }
}