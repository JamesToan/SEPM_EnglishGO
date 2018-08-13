package sepm.englishgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ChooseTopic extends AppCompatActivity {

    public static int TOPIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_topic);

        Button btn1 = (Button)findViewById(R.id.topic1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TOPIC = 1;
                openChooseLevel(v);
            }
        });

        Button btn2 = (Button)findViewById(R.id.topic2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TOPIC = 2;
                openChooseLevel(v);
            }
        });

        Button btn3 = (Button)findViewById(R.id.topic3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TOPIC = 3;
                openChooseLevel(v);
            }
        });

    }

    public void openChooseLevel(View view){
        Intent changeView = new Intent( ChooseTopic.this, ChooseLevel.class);
        startActivity(changeView);
    }

}
