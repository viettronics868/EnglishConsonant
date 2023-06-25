package com.linhphuong.englishconsonant;

/**
 * Created by khidot on 1/12/2016.
 */

import android.content.Intent;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

import com.linhphuong.englishconsonant.SceneManager.SceneType;

public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener
{
    //---------------------------------------------
    // VARIABLES
    //---------------------------------------------

    private MenuScene menuChildScene;

    private final int MENU_PLAY = 0;
    private final int MENU_README = 1;
    private final int MENU_GAME = 2;
    private static int CAMERA_WIDTH=720;
    private static int CAMERA_HEIGHT=1280;

    //---------------------------------------------
    // METHODS FROM SUPERCLASS
    //---------------------------------------------

    @Override
    public void createScene()
    {
        createBackground();
        createMenuChildScene();
    }

    @Override
    public void onBackKeyPressed()
    {
        System.exit(0);
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_MENU;
    }


    @Override
    public void disposeScene()
    {
        // TODO Auto-generated method stub
    }

    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
    {
        switch(pMenuItem.getID())
        {
            case MENU_PLAY:
                //Load Game Scene!
                final Intent controlsIntent = new Intent(this.activity, VideoListActivity.class);
                this.activity.startActivity(controlsIntent);

                return true;
            case MENU_README:
                //Load Game Scene!
                final Intent readmeIntent = new Intent(this.activity, ReadmeActivity.class);
                this.activity.startActivity(readmeIntent);
                return true;
            case MENU_GAME:

                //final Intent controlsIntent = new Intent(ResourcesManager.getInstance().activity, VideoListActivity.class);
                //ResourcesManager.getInstance().activity.startActivity(controlsIntent);
                SceneManager.getInstance().loadGameScene(engine);


                return true;
            default:
                return false;
        }
    }

    //---------------------------------------------
    // CLASS LOGIC
    //---------------------------------------------

    private void createBackground()
    {
        attachChild(new Sprite(360, 640, resourcesManager.menu_background_region, vbom)
        {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera)
            {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        });
    }

    private void createMenuChildScene()
    {
        menuChildScene = new MenuScene(camera);
        menuChildScene.setPosition(0, 0);

        final IMenuItem playMenuItem =
                new ScaleMenuItemDecorator(
                        new SpriteMenuItem(
                                MENU_PLAY,
                                resourcesManager.play_region,
                                vbom),
                        1.2f, 1);
        final IMenuItem readmeMenuItem =
                new ScaleMenuItemDecorator(
                        new SpriteMenuItem(
                                MENU_README,
                                resourcesManager.readme_region,
                                vbom),
                        1.2f, 1);
        final IMenuItem gameMenuItem =
                new ScaleMenuItemDecorator(
                        new SpriteMenuItem(
                                MENU_GAME,
                                resourcesManager.gamebtn_region,
                                vbom),
                        1.2f, 1);

        menuChildScene.addMenuItem(playMenuItem);
        menuChildScene.addMenuItem(readmeMenuItem);
        menuChildScene.addMenuItem(gameMenuItem);

        menuChildScene.buildAnimations();
        menuChildScene.setBackgroundEnabled(false);

        playMenuItem.setPosition(CAMERA_WIDTH / 2, (CAMERA_HEIGHT/2) - 160);
        readmeMenuItem.setPosition(playMenuItem.getX()-150, playMenuItem.getY() - 70);
        gameMenuItem.setPosition(playMenuItem.getX()+150, playMenuItem.getY() - 70);

        menuChildScene.setOnMenuItemClickListener(this);

        setChildScene(menuChildScene);
    }
}