package com.android.zzw.delicacy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.zzw.delicacy.Adapter.TransitionAdapter;
import com.android.zzw.delicacy.JavaBean.picture;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import es.dmoral.toasty.Toasty;


/**
 * Created by zzw on 17/3/7.
 */

public class DetailActivity extends AppCompatActivity {
    private TextView title;
    private TextView description;
    private boolean favourite;
    private boolean savefavourite;
    private int result;
    ImageView imageView;
    Toolbar toolbar;
    String id;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setView();
        choosefavourite();
        Bitmap photo = setupPhoto(getIntent().getIntExtra("photo",R.drawable.xiaren));
        colorize(photo);
        setupText();
        //透明状态栏
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        CollapsingToolbarLayout.LayoutParams lp= (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
        lp.topMargin=statusBarHeight-10;
        toolbar.setLayoutParams(lp);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);//calculateStatusColor(Color.WHITE, (int) alphaValue)
        }

        applySystemWindowsBottomInset(R.id.container);
        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                ImageView hero = (ImageView) findViewById(R.id.photo);
                ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                        getResources().getColor(R.color.photo_tint), 0);
                color.start();
                getWindow().getEnterTransition().removeListener(this);
            }
        });
    }
    private void setupText(){
        title= (TextView) findViewById(R.id.title);
        description= (TextView) findViewById(R.id.description);
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/songti.ttf");
        title.setTypeface(typeFace);
        description.setTypeface(typeFace);
        title.setText(getIntent().getStringExtra("name"));
        description.setText(getIntent().getStringExtra("detail"));
    }
    @Override
    public void onBackPressed() {
        if(favourite==savefavourite){
            result=1;
        }else {
            result=0;
        }
        ImageView hero = (ImageView) findViewById(R.id.photo);
        ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(),"tint",
                0, getResources().getColor(R.color.photo_tint));
        color.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setResult(result,getIntent());
                finishAfterTransition();
            }
        });
        color.start();
        MainActivity.sPhotoCache.clear();
        if(savefavourite!=favourite) {
            picture food = new picture();
            if (favourite) {
                food.setFavourite(true);
            } else {
                food.setFavourite(false);
            }
            food.update(id, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                    } else {
                        Toasty.error(DetailActivity.this, "更新失败：" + e.getMessage() + "," + e.getErrorCode(), Toast.LENGTH_SHORT, true).show();
                    }
                }
            });
        }
    }
    private void applySystemWindowsBottomInset(int container) {
        View containerView = findViewById(container);
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
                    view.setPadding(0, 0, 0, windowInsets.getSystemWindowInsetBottom());
                } else {
                    view.setPadding(0, 0, windowInsets.getSystemWindowInsetRight(), 0);
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }
    private Bitmap setupPhoto(int resource) {
        Bitmap bitmap = MainActivity.sPhotoCache.get(resource);
        ImageView view= (ImageView) findViewById(R.id.photo);
        view.setImageBitmap(bitmap);
        return bitmap;
    }
    private void colorize(Bitmap bitmap) {
        Palette.generateAsync(bitmap, 24, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                Palette.Swatch lightvibrant=palette.getLightVibrantSwatch();
                Palette.Swatch darkmuted=palette.getDarkMutedSwatch();
                if (vibrant != null) {
                    title.setTextColor(vibrant.getRgb());
                }else {
                    title.setTextColor(Color.parseColor("#e16531"));
                }
                if(lightvibrant !=null) {
                    description.setTextColor(lightvibrant.getRgb());
                }else {
                    description.setTextColor(Color.parseColor("#ea986c"));
                }
                if(darkmuted!=null) {
                    getWindow().setBackgroundDrawable(new ColorDrawable(darkmuted.getRgb()));
                }
            }
        });
    }
    public void setView(){
        imageView= (ImageView) findViewById(R.id.photo);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        favourite=getIntent().getBooleanExtra("favourite",false);

        savefavourite=favourite;
        id=getIntent().getStringExtra("id");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_18dp);
        TextView tbtitle= (TextView) findViewById(R.id.tbtitle);
        Typeface typeFace = Typeface.createFromAsset(getAssets(),"fonts/songti.ttf");
        tbtitle.setTypeface(typeFace);
        tbtitle.setText("Delicacy");
        fab= (FloatingActionButton) findViewById(R.id.fab);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    public void choosefavourite(){
        if(favourite){
            fab.setImageResource(R.drawable.ic_star_favourite);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favourite=!favourite;
                if(favourite){
                    fab.setImageResource(R.drawable.ic_star_favourite);
                    Toasty.success(DetailActivity.this, "收藏成功", Toast.LENGTH_SHORT, true).show();
                }else {
                    fab.setImageResource(R.drawable.ic_star);
                    Toasty.success(DetailActivity.this, "取消收藏", Toast.LENGTH_SHORT, true).show();
                }

            }
        });
    }

}
