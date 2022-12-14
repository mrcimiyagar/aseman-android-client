package kasper.android.pulse.extras;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;

public class CreateAnimationView {

private static int contador;
Integer colorFrom = Color.BLUE;
Integer colorTo = Color.RED;

public static AnimatorSet createAnimation(View view) {
    ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha",
            0f);
    fadeOut.setDuration(300);
    ObjectAnimator mover = ObjectAnimator.ofFloat(view,
            "translationX", -500f, 0f);
    mover.setDuration(400);
    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha",
            0f, 1f);
    fadeIn.setDuration(300);
    AnimatorSet animatorSet = new AnimatorSet();

    animatorSet.play(mover);
    animatorSet.start();
    return animatorSet;

 }
}