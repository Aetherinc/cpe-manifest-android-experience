package com.wb.nextgen.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;

import com.wb.nextgen.NextGenExperience;
import com.wb.nextgen.NextGenExperience;
import com.wb.nextgen.R;
import com.wb.nextgen.data.NextGenStyle;
import com.wb.nextgen.fragment.NextGenActorListFragment;
import com.wb.nextgen.fragment.NextGenExtraMainTableFragment;
import com.wb.nextgen.interfaces.NextGenFragmentTransactionInterface;
import com.wb.nextgen.interfaces.SensitiveFragmentInterface;
import com.wb.nextgen.util.utils.NextGenFragmentTransactionEngine;

/**
 * Created by gzcheng on 1/7/16.
 */
public class NextGenExtraActivity extends AbstractNextGenActivity implements NextGenFragmentTransactionInterface {

    //protected ImageView extraTitleView;
    //protected ImageView extraLogoImageView;
    //protected Button extraBackButton;

    NextGenFragmentTransactionEngine nextGenFragmentTransactionEngine;

    protected LinearLayout leftPanelFrame;
    protected LinearLayout rightMainFrame;

    private int startupStackCount = 2;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.next_gen_extra);
        float density = NextGenExperience.getScreenDensity(this);
        int spacing = (int)(10 *density);

        leftPanelFrame = (LinearLayout)findViewById(R.id.next_gen_extra_left_view);
        rightMainFrame = (LinearLayout)findViewById(R.id.next_gen_extra_right_view);

        nextGenFragmentTransactionEngine = new NextGenFragmentTransactionEngine(this);
        initFragments();
    }

    protected void initFragments(){
        transitLeftFragment(new NextGenActorListFragment());
        transitRightFragment(new NextGenExtraMainTableFragment());
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //*************** NextGenFragmentTransactionInterface ***************
    @Override
    public void transitRightFragment(Fragment nextFragment){
        nextGenFragmentTransactionEngine.transitFragment(getSupportFragmentManager(), R.id.next_gen_extra_right_view, nextFragment);

    }

    @Override
    public void transitLeftFragment(Fragment nextFragment){
        nextGenFragmentTransactionEngine.transitFragment(getSupportFragmentManager(), R.id.next_gen_extra_left_view, nextFragment);

    }

    @Override
    public void transitMainFragment(Fragment nextFragment){
        //nextGenFragmentTransactionEngine.transitFragment(getSupportFragmentManager(), R.id.next_gen_extra_main_frame, nextFragment);
    }

    @Override
    public void resetUI(boolean bIsRoot){
        if (bIsRoot){
            setBackButtonLogo(R.drawable.home_logo);
            setBackButtonText(getResources().getString(R.string.home_button_text) );
        }else{
            setBackButtonLogo(R.drawable.back_logo);
            setBackButtonText(getResources().getString(R.string.back_button_text) );
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Fragment leftFragment = getSupportFragmentManager().findFragmentById(R.id.next_gen_extra_left_view);
        Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.next_gen_extra_right_view);

        if (leftFragment != null && leftFragment instanceof SensitiveFragmentInterface){
            ((SensitiveFragmentInterface)leftFragment).notifyCurrentSensitiveFragment(rightFragment);
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 1 )
            finish();


    }

    @Override
    public int getLeftButtonLogoId(){
        return R.drawable.home_logo;
    }

    @Override
    public String getBackgroundImgUri(){
        return NextGenExperience.getMovieMetaData().getStyle().getBackgroundImageURL(NextGenStyle.NextGenAppearanceType.OutOfMovie);
    }

    @Override
    public String getLeftButtonText(){
        return getResources().getString(R.string.home_button_text);
    }

    @Override
    public String getRightTitleImageUri(){
        return NextGenExperience.getMovieMetaData().getStyle().getTitleImageURL(NextGenStyle.NextGenAppearanceType.OutOfMovie);
    }

    @Override
    public String getRightTitleText(){
        return "";
    }

}
