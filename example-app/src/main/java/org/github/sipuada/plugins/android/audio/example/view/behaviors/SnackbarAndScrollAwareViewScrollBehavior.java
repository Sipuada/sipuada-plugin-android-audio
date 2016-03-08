package org.github.sipuada.plugins.android.audio.example.view.behaviors;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

@SuppressWarnings("unused")
public class SnackbarAndScrollAwareViewScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final boolean SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;

    private static final ThreadLocal<Matrix> sMatrix = new ThreadLocal<>();
    private static final ThreadLocal<RectF> sRectF = new ThreadLocal<>();
    private static final Matrix IDENTITY = new Matrix();
    private float fabTranslationY;
    private Rect tmpRect;
    boolean fabShouldScroll = true;

    public SnackbarAndScrollAwareViewScrollBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return SNACKBAR_BEHAVIOR_ENABLED && dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            fabShouldScroll = false;
        }
        return child.getVisibility() == View.VISIBLE && FabOnDependentViewChanged(parent, child, dependency);
    }

    private boolean FabOnDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            updateFabTranslationForSnackbar(parent, child, dependency);
        } else if (dependency instanceof AppBarLayout) {
            updateFabVisibility(parent, (AppBarLayout) dependency, child);
        }
        return false;
    }

    public void onDependentViewRemoved(CoordinatorLayout parent, V child, View dependency) {
        if(dependency instanceof Snackbar.SnackbarLayout) {
            ViewCompat.animate(child).translationY(0.0F)
                    .setInterpolator(new FastOutSlowInInterpolator()).setListener(null);
        }
    }

    private boolean updateFabVisibility(CoordinatorLayout parent, AppBarLayout appBarLayout, V child) {
        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if (lp.getAnchorId() != appBarLayout.getId()) {
            return false;
        }
        if (tmpRect == null) {
            tmpRect = new Rect();
        }
        final Rect rect = tmpRect;
        getDescendantRect(parent, appBarLayout, rect);
        changeVisibilityBasedOnMinimumHeightForVisibleOverlappingContent(appBarLayout, child, rect.bottom);
        return true;
    }

    private void getDescendantRect(ViewGroup parent, View view, Rect rect) {
        rect.set(0, 0, view.getWidth(), view.getHeight());
        if (Build.VERSION.SDK_INT >= 11) {
            offsetDescendantRect(parent, view, rect);
        }
        else {
            parent.offsetDescendantRectToMyCoords(view, rect);
        }
    }

    private static void offsetDescendantRect(ViewGroup parent, View view, Rect rect) {
        Matrix m = sMatrix.get();
        if (m == null) {
            m = new Matrix();
            sMatrix.set(m);
        } else {
            m.set(IDENTITY);
        }
        offsetDescendantMatrix(parent, view, m);
        RectF rectF = sRectF.get();
        if (rectF == null) {
            rectF = new RectF();
        }
        rectF.set(rect);
        m.mapRect(rectF);
        rect.set((int) (rectF.left + 0.5f), (int) (rectF.top + 0.5f),
                (int) (rectF.right + 0.5f), (int) (rectF.bottom + 0.5f));
    }

    private static void offsetDescendantMatrix(ViewParent target, View view, Matrix m) {
        final ViewParent parent = view.getParent();
        if (parent instanceof View && parent != target) {
            final View vp = (View) parent;
            offsetDescendantMatrix(target, vp, m);
            m.preTranslate(-vp.getScrollX(), -vp.getScrollY());
        }

        m.preTranslate(view.getLeft(), view.getTop());

        if (!view.getMatrix().isIdentity()) {
            m.preConcat(view.getMatrix());
        }
    }

    private void changeVisibilityBasedOnMinimumHeightForVisibleOverlappingContent(
            final AppBarLayout appBarLayout, final V child, final int threshold) {
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                WindowInsetsCompat lastInsets = ViewCompat.onApplyWindowInsets(view, insets);
                final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
                int minHeight = ViewCompat.getMinimumHeight(appBarLayout);
                if (minHeight != 0) {
                    minHeight = (minHeight * 2) + topInset;
                } else {
                    final int childCount = appBarLayout.getChildCount();
                    minHeight = childCount >= 1 ? (ViewCompat.getMinimumHeight(appBarLayout
                            .getChildAt(childCount - 1)) * 2) + topInset : 0;
                }
                if (threshold <= minHeight) {
                    child.setVisibility(View.GONE);
                } else {
                    child.setVisibility(View.VISIBLE);
                }
                return lastInsets;
            }
        });
        ViewCompat.requestApplyInsets(appBarLayout);
    }

    private void updateFabTranslationForSnackbar(CoordinatorLayout parent, V child, View snackbar) {
        if (child.getVisibility() != View.VISIBLE) {
            return;
        }
        final float targetTransY = getFabTranslationYForSnackbar(parent, child);
        if (fabTranslationY == targetTransY) {
            return;
        }
        fabTranslationY = targetTransY;
        final float currentTransY = ViewCompat.getTranslationY(child);
        final float dy = currentTransY - targetTransY;

        if (Math.abs(dy) > (child.getHeight() * 0.667f)) {
            ViewCompat.animate(child)
                    .translationY(targetTransY)
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setListener(null);
        } else {
            ViewCompat.animate(child).cancel();
            ViewCompat.setTranslationY(child, targetTransY);
        }
    }

    private float getFabTranslationYForSnackbar(CoordinatorLayout parent, V child) {
        float minOffset = 0;
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            final View view = dependencies.get(i);
            if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(child, view)) {
                minOffset = Math.min(minOffset,
                        ViewCompat.getTranslationY(view) - view.getHeight());
            }
        }
        return minOffset;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, count = dependencies.size(); i < count; i++) {
            final View dependency = dependencies.get(i);
            if (dependency instanceof AppBarLayout
                    && updateFabVisibility(parent, (AppBarLayout) dependency, child)) {
                break;
            }
        }
        parent.onLayoutChild(child, layoutDirection);
        //offsetIfNeeded(parent, child);
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout parent, V child, View directTargetChild,
                                       View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout parent, V child,
                        View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(parent, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        int lowerThreshold = 0;
        int upperThreshold = child.getHeight() * 2;
        if (!fabShouldScroll && child.getTranslationY() == lowerThreshold) {
            if (child.getVisibility() != View.VISIBLE) {
                child.setTranslationY(upperThreshold);
            }
            fabShouldScroll = true;
        }
        if (fabShouldScroll) {
            int translationY = Math.max(lowerThreshold, (int) child.getTranslationY() + dyConsumed);
            if (translationY > upperThreshold) {
                translationY = upperThreshold;
                if (child.getVisibility() == View.VISIBLE) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
            child.setTranslationY(translationY);
            if (dyConsumed < 0 && child.getVisibility() == View.INVISIBLE) {
                child.setVisibility(View.VISIBLE);
            }
        }
    }

}
