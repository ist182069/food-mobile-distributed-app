package pt.ulisboa.tecnico.cmov.foodist;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FoodServiceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_service);

        TextView foodServiceName = findViewById(R.id.foodServiceName);
        TextView openingTimes = findViewById(R.id.openingTimes);
        TextView queueTime = findViewById(R.id.queueTime);

        Intent intent = getIntent();
        String serviceName = intent.getStringExtra("Service Name");
        foodServiceName.setText(serviceName);
        //TODO - Ask server for list of Menus
        ArrayList<String[]> menus = new ArrayList<>();
        menus.add(new String[]{"Tosta mista", "10€"});

        for(String[] menu : menus){

            //number of info
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.food_menu, null);

            TextView name = v.findViewById(R.id.menuFood);
            TextView cost = v.findViewById(R.id.menuCost);


            name.setText(menu[0]);
            cost.setText(menu[1]);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FoodServiceActivity.this, FoodMenuActivity.class);
                    startActivity(intent);
                }
            });

            ViewGroup foodServiceList = findViewById(R.id.menus);
            foodServiceList.addView(v);

        }
    }
}
