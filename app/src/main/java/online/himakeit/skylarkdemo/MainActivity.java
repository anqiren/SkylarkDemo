package online.himakeit.skylarkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import online.himakeit.skylarkdemo.about.AboutActivity;
import online.himakeit.skylarkdemo.about.WebActivity;
import online.himakeit.skylarkdemo.immersion_status_bar.MainImmersionActivity;
import online.himakeit.skylarkdemo.searchdemo.SearchActivity;
import online.himakeit.skylarkdemo.shapedemo.ShapeActivity;
import online.himakeit.skylarkdemo.sqlitedemo.SQLiteActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_search_demo).setOnClickListener(this);
        findViewById(R.id.btn_sqlite_demo).setOnClickListener(this);
        findViewById(R.id.btn_shape_demo).setOnClickListener(this);
        findViewById(R.id.btn_immersion_demo).setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = WebActivity.newTntent(MainActivity.this,"http://himakeit.online","LiXueLong'Blog");
                startActivity(mIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_about:
                mIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(mIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search_demo:
                mIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(mIntent);
                break;
            case R.id.btn_sqlite_demo:
                mIntent = new Intent(MainActivity.this, SQLiteActivity.class);
                startActivity(mIntent);
                break;
            case R.id.btn_shape_demo:
                mIntent = new Intent(MainActivity.this, ShapeActivity.class);
                startActivity(mIntent);
                break;
            case R.id.btn_immersion_demo:
                mIntent = new Intent(MainActivity.this, MainImmersionActivity.class);
                startActivity(mIntent);
                break;
        }
    }
}
