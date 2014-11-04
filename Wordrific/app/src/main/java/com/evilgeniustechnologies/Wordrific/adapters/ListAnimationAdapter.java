package com.evilgeniustechnologies.Wordrific.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by benjamin on 5/22/14.
 */
public class ListAnimationAdapter extends AnimationAdapter {

    public ListAnimationAdapter(BaseAdapter baseAdapter) {
        super(baseAdapter);
    }

    @Override
    public Animator[] getAnimators(ViewGroup viewGroup, View view) {
        Animator rightInAnimator = ObjectAnimator.ofFloat(view, "translationX", viewGroup.getWidth() / 2, 0);
        return new Animator[]{rightInAnimator};
    }
}
