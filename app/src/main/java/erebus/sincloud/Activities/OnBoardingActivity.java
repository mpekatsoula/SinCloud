package erebus.sincloud.Activities;

import androidx.appcompat.app.AppCompatActivity;
import erebus.sincloud.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ramotion.paperonboarding.PaperOnboardingEngine;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnChangeListener;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

public class OnBoardingActivity extends AppCompatActivity
{
    public static String COMPLETED_ONBOARDING = "COMPLETED_ONBOARDING";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_main_layout);

        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener(new PaperOnboardingOnChangeListener()
        {
            @Override
            public void onPageChanged(int oldElementIndex, int newElementIndex)
            {

            }
        });

        engine.setOnRightOutListener(new PaperOnboardingOnRightOutListener()
        {
            @Override
            public void onRightOut()
            {
                openMainActivity();
            }
        });
    }

    private ArrayList<PaperOnboardingPage> getDataForOnboarding()
    {
        PaperOnboardingPage scr1 = new PaperOnboardingPage(getString(R.string.welcome_onboarding), getString(R.string.welcome_onboarding_desc),
                Color.parseColor("#CE93D8"), R.drawable.sincloud_logo, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr2 = new PaperOnboardingPage(getString(R.string.onboarding_title1), getString(R.string.onboarding_sin),
                Color.parseColor("#9FA8DA"), R.drawable.angel, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr3 = new PaperOnboardingPage(getString(R.string.oboarding_title2), getString(R.string.oboarding_discover),
                Color.parseColor("#EF9A9A"), R.drawable.devil, R.drawable.onboarding_pager_circle_icon);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }


    private void openMainActivity()
    {
        // User has seen OnBoardingFragment, so mark our SharedPreferences
        // flag as completed.
        SharedPreferences.Editor sharedPreferencesEditor;
        sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit();

        sharedPreferencesEditor.putBoolean(COMPLETED_ONBOARDING, true);
        sharedPreferencesEditor.apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
