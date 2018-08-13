package sepm.englishgo;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class Result extends AppCompatActivity {

    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        TextView word = findViewById(R.id.resultWord);
        TextView explanation = findViewById(R.id.explanation);
        TextView sampleSen = findViewById(R.id.sampleSen);

        word.setText((Challenge.VOCABULARY.getContent()));
        explanation.setText("Explanation:\n"+Challenge.VOCABULARY.getExplanation());
        sampleSen.setText("Sample Sentence:\n"+Challenge.VOCABULARY.getSampleSen());


    }

    public void newChallenge(View v){
        Intent changeView = new Intent( Result.this, Challenge.class);
        startActivity(changeView);
    }

    public void textToSpeech(View view){
        try {
            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(Locale.UK);
                        t1.speak(Challenge.VOCABULARY.getContent(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });
        }
        catch (NullPointerException e){
            System.out.println("No word error");
        }
    }

    public void getBack(View view){
        Intent changeView = new Intent( Result.this, ChooseTopic.class);
        startActivity(changeView);
    }
}
