package erebus.sincloud.Activities;

import androidx.appcompat.app.AppCompatActivity;
import erebus.sincloud.R;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.ramotion.paperonboarding.PaperOnboardingEngine;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnChangeListener;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

public class OnBoardingActivity extends AppCompatActivity
{
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
                // Probably here will be your exit action
                openMainActivity();
            }
        });
    }

    private ArrayList<PaperOnboardingPage> getDataForOnboarding()
    {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage(getString(R.string.welcome_onboarding), getString(R.string.welcome_onboarding_desc),
                Color.parseColor("#CE93D8"), R.drawable.sincloud_logo, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("Sins", "Confess your sin and share it with other users anonymously!",
                Color.parseColor("#9FA8DA"), R.drawable.angel, R.drawable.onboarding_pager_circle_icon);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("Sins", "Discover other sins and be the voice of good or evil! Let's get stated!",
                Color.parseColor("#EF9A9A"), R.drawable.devil, R.drawable.onboarding_pager_circle_icon);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }


    private void openMainActivity()
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
