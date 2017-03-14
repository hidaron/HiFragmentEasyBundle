package org.limlee.hifragmenteasybundle;


import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class FragmentHelper {

    private FragmentHelper() {

    }

    public static void addFragment(FragmentManager fragmentManager,
                                   @NonNull Fragment fragment,
                                   @IdRes int containerViewId,
                                   Bundle bundle,
                                   boolean isAddToBackStack) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (!fragment.isAdded()) {
            ft.add(containerViewId, fragment,
                    fragment.getClass().getName());
        }
        if (!ft.isEmpty()) {
            if (null != bundle) {
                if (null != fragment.getArguments()) {
                    fragment.getArguments().putAll(bundle);
                } else {
                    fragment.setArguments(bundle);
                }
            }
            ft.show(fragment);
            fragment.setUserVisibleHint(true);
            if (isAddToBackStack) {
                ft.addToBackStack(fragment.getClass().getName());
            }
            ft.commitAllowingStateLoss();
        }
    }
}
