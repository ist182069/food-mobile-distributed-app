package pt.ulisboa.tecnico.cmov.foodist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseCampusActivity extends AppCompatActivity {
    public static final String CAMPUS = "pt.ulisboa.tecnico.cmov.foodlist.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_campus);

        Button tagusparkButton = (Button) findViewById(R.id.taguspark);
        Button alamedaButton = (Button) findViewById(R.id.alameda);

        tagusparkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
                intent.putExtra(CAMPUS,"Taguspark");
                startActivity(intent);
            }
        });

        alamedaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseCampusActivity.this, MainActivity.class);
                intent.putExtra(CAMPUS,"Alameda");
                startActivity(intent);
            }
        });

    }
}