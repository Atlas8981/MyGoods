package com.example.mygoods.Activity.AboutMe;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygoods.R;

public class TermAndConditionActivity extends AppCompatActivity {

    private TextView termText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_and_condition);

        termText = findViewById(R.id.termText);

        termText.setText(term);
    }

    private static final String term =
            "Mygoods is intended to be a friendly, enjoyable and useful experience, but as with anything concerning any kind of transactions there are risks involved and we strongly advise you to understand all of these terms before submitting the listing.\n" +
            "\nRights to Suspend or Terminate.\n" +
            "You agree that this Application, in its own discretion, may terminate any listing or use of the service immediately and without notice if\n" +
            "(a) We believe that you are not abiding by the general rules of this Application\n" +
            "(b) You have repeatedly broken a certain term\n" +
            "(c) You have listed a suspicious item\n" +
            "Your Conduct.\n" +
            "You are solely responsible for the contents of your listings through this web-site. You must ensure that your participation in the listing of items does not violate any applicable laws or regulations. By this we mean that you must check that you entitled to sell or buy the relevant item and that you are not prohibited from doing so by any law or regulation.\n" +
            "You must not transmit through the service any unlawful, harassing, libelous, abusive, threatening, harmful, vulgar, obscene or otherwise objectionable material. You must also not transmit any material that encourages conduct that could constitute a criminal offense, give rise to civil liability or otherwise violate any applicable law or regulation.\n" +
            "\n" +
            "\n" +
            "\n" +
            "General Terms.\n" +
            "This Application acts as the window for sellers to post listings and for buyers to purchase on sellers' listings. We are not involved in the actual transaction between buyers and sellers. As a result, we have no control over the quality, safety or legality of the items listed the truth or accuracy of the listings, the ability of sellers to sell items or the ability of buyers to buy items. We cannot and do not control whether or not sellers will complete the sale of items they offer or buyers will complete the purchases of items they have purchased. In addition, note that there are risks of dealing with foreign nationals, underage persons or people acting under false pretend.\n" +
            "This Application shall not be responsible for any items sold through it, for any damage to items during transit or during the inspection period, nor for misrepresentations or breaches of contract by either buyer or seller. This application shall not be responsible for the cost of procurement of substitute goods or any losses resulting from any goods purchased or obtained.\n" +
            "\n";
}