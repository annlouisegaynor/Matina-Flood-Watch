package com.example.chynnasevilleno.mfw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;

public class NavigationDrawerActivity extends AppCompatActivity {

    // Class variables
    protected DrawerLayout mDrawer;
    protected NavigationView navigationView;

    private MaterialMenuDrawable materialMenu;
    private boolean isDrawerOpened;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        initDrawer();
        initToolbar();
    }


    private void initDrawer(){
        // Initialize the navigation drawer, assign certain activities to the menus options

        mDrawer = findViewById(R.id.drawer_layout);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        String selected = menuItem.toString();
                        Intent intent = null;

                        switch (selected){
                            case "Home":
                                intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                break;

                            case "About MFW":
                                intent = new Intent(getApplicationContext(), AboutActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                break;

                            case "Developers":
                                intent = new Intent(getApplicationContext(), DevelopersActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                break;
                        }

                        mDrawer.closeDrawers(); // Close drawer when item is tapped
                        return true;
                    }
                });

        mDrawer.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        materialMenu.setTransformationOffset(
                                MaterialMenuDrawable.AnimationState.BURGER_ARROW,
                                isDrawerOpened ? 2 - slideOffset : slideOffset
                        );
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                        isDrawerOpened = true;
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                        isDrawerOpened = false;
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                        if(newState == DrawerLayout.STATE_IDLE) {
                            if(isDrawerOpened) {
                                materialMenu.setIconState(MaterialMenuDrawable.IconState.ARROW);
                            } else {
                                materialMenu.setIconState(MaterialMenuDrawable.IconState.BURGER);
                            }
                        }
                    }
                }
        );
    }


    private void initToolbar(){
        // Initialize the Toolbar and give the burger animations

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        materialMenu = new MaterialMenuDrawable(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        toolbar.setNavigationIcon(materialMenu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // Handle your drawable state here
                materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);

                if (isDrawerOpened) {
                    // Close the drawer when the arrow button is clicked and drawer is opened
                    mDrawer.closeDrawer(GravityCompat.START);
                }
                else{
                    // Open the drawer when the burger button is clicked and drawer is closed
                    mDrawer.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
