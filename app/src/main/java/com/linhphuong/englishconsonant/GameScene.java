package com.linhphuong.englishconsonant;

/**
 * Created by khidot on 1/12/2016.
 */


import org.andengine.engine.camera.hud.HUD;

import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;

import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;

import com.linhphuong.englishconsonant.SceneManager.SceneType;


/**************************************/

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;

import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;

import org.andengine.util.adt.io.in.IInputStreamOpener;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;

import android.widget.Toast;


public class GameScene extends BaseScene //implements IOnSceneTouchListener
{
    private int score = 0;
    private HUD gameHUD;
    private Text scoreText;
    private Text gameOverText;
    private boolean gameOverDisplayed = false;

    /*****************************************/

    private static String TAG = "POP";
    private static int CAMERA_WIDTH = 720;
    private static int CAMERA_HEIGHT = 1280;

    private static int alphaWidth = CAMERA_WIDTH/12, // 60
            startPositionX = CAMERA_WIDTH/2, // 360
            startPositionY = CAMERA_HEIGHT/2,// 640
            dimenX = CAMERA_WIDTH/120, // 6
            dimenY = CAMERA_HEIGHT/128;  // 10
    private int TIME_REMAINING = 60 * 2; // in second
    private int widthX;// table width
    private int widthY;// table height

    private Font mFont;
    private Line line;
    private Text timerText;

    private String[][] table;
    private WordSprite[][] spriteTable;
    private ArrayList<Word> wordList;
    private ArrayList<int[]> correctXYList;
    private ArrayList<Sprite> hoverList;
    private StringBuilder wordStack;
    private int[] lastestTouch;
    private boolean canSwipe;

    private HashMap<String, ITexture> alphabetBitmapList;
    private HashMap<String, ITextureRegion> textureRegionList;
    private ITextureRegion backgroundTextureRegion,
            tableBackgroundTextureRegion,
            hoverTextureRegion,

            timerTextureRegion,
            helpTimerTextureRegion;



    @Override
    public void createScene()
    {
        loadResources();
        create();


        createHUD();

    }

    @Override
    public void onBackKeyPressed()
    {


        SceneManager.getInstance().loadMenuScene(engine);
    }

    @Override
    public SceneType getSceneType()
    {
        return SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene()
    {
        camera.setHUD(null);
        camera.setChaseEntity(null); //TODO
        camera.setCenter(CAMERA_WIDTH/2, CAMERA_HEIGHT/2);

        // TODO code responsible for disposing scene
        // removing all game scene objects.
    }

/**********************************************/



    public void loadResources() {
    widthX = dimenX * alphaWidth; // 6*60 limit table width
    widthY = dimenY * alphaWidth; // 10*60 limit table height

    lastestTouch = new int[2];

    correctXYList = new ArrayList<int[]>();
    table = new String[dimenX][dimenY]; //[6][10]
    spriteTable = new WordSprite[dimenX][dimenY]; //[6][10]
    alphabetBitmapList = new HashMap<String, ITexture>();
    textureRegionList = new HashMap<String, ITextureRegion>();

    // 1 - Set up bitmap textures
    ITexture backgroundTexture = loadTexture("gfx/gamebackground.png");
    ITexture tableBackgroundTexture = loadTexture("gfx/table1.png");
    ITexture hoverBackgroundTexture = loadTexture("gfx/hover2.png");
    //ITexture hoverVBackgroundTexture = loadTexture("gfx/hoverV.png");
    ITexture timerTexture = loadTexture("gfx/timer.png");
    ITexture helpTimerTexture = loadTexture("gfx/helpTime.png");
    initTable();
    initWordList();

    // 2 - Load bitmap textures into VRAM
    backgroundTexture.load();
    tableBackgroundTexture.load();
    hoverBackgroundTexture.load();
    //hoverVBackgroundTexture.load();
    timerTexture.load();
    helpTimerTexture.load();
    loadAllAlphabet();

    // 3 - Set up texture regions
    backgroundTextureRegion = TextureRegionFactory.extractFromTexture(backgroundTexture);
    tableBackgroundTextureRegion = TextureRegionFactory.extractFromTexture(tableBackgroundTexture);
    hoverTextureRegion = TextureRegionFactory.extractFromTexture(hoverBackgroundTexture);
    //hoverVTextureRegion = TextureRegionFactory.extractFromTexture(hoverVBackgroundTexture);
    timerTextureRegion = TextureRegionFactory.extractFromTexture(timerTexture);
    helpTimerTextureRegion = TextureRegionFactory.extractFromTexture(helpTimerTexture);

    // 4 - Set up Font
    mFont = FontFactory.create(resourcesManager.activity.getFontManager(), resourcesManager.activity.getTextureManager(), 256, 256,
            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 24, Color.BLUE.hashCode());
    mFont.load();
}


    public void create() {
        // 1 - Create new scene & add background
        final Scene scene = new Scene();
        Sprite sprite = new Sprite(

                startPositionX,

                startPositionY,
                backgroundTextureRegion,
                resourcesManager.activity.getVertexBufferObjectManager());
        scene.attachChild(sprite);

        // 2 - Add Table Background
        Sprite tableBackground = new Sprite(
                startPositionX,
                startPositionY,
                widthX,
                widthY,
                tableBackgroundTextureRegion,
                resourcesManager.activity.getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                try {
                    int posX, posY;
                    Sprite hover;
                    if (pSceneTouchEvent.isActionDown()) {
                        hoverList = new ArrayList<Sprite>();
                        wordStack = new StringBuilder();
                        lastestTouch = touchAt(pTouchAreaLocalX, pTouchAreaLocalY);
                        canSwipe = true;
                        for (int[] xy : correctXYList) {
                            if (xy[0] == lastestTouch[0] && xy[1] == lastestTouch[1]) {
                                canSwipe = false;
                                return false;
                            }
                        }

                        if (canSwipe) {
                            //posX = (lastestTouch[0] * alphaWidth) + startPositionX;
                            posX = (lastestTouch[0] * alphaWidth) + startPositionX/2 + alphaWidth/2;
                            //posY = (lastestTouch[1] * alphaWidth) + startPositionY;
                            posY = (lastestTouch[1] * alphaWidth) + startPositionY/2 + alphaWidth/2+alphaWidth/4;

                            hover = new Sprite(posX, posY, alphaWidth, alphaWidth,
                                    hoverTextureRegion,
                                    getVertexBufferObjectManager());
                            scene.attachChild(hover);

                            hoverList.add(hover);
                            wordStack.append(table[lastestTouch[0]][lastestTouch[1]]);
                        }
                    }

                    if (pSceneTouchEvent.isActionUp()) {
                        if (canSwipe) {
                            Word w = isCorrectWord(wordStack.toString());
                            if (w == null) {
                                //Log.i(TAG, "Not found!! : " + wordStack.toString());
                                showToast("Not found !!! : ");
                                for (Sprite h : hoverList) {
                                    scene.detachChild(h);
                                }
                            } else {  //strike through word found
                                //Log.i(TAG, "Found!! : " + wordStack.toString());
                                showToast("GOOD, found: ");
                                float x = w.text.getX();

                                float y = w.text.getY() ;

                                line = new Line(x- w.text.getWidth()/2, y, x+ w.text.getWidth()/2, y, 3.0f, getVertexBufferObjectManager());
                                line.setColor(Color.RED);
                                scene.attachChild(line);

                                int[] xy = new int[2];
                                xy[0] = w.x;
                                xy[1] = w.y;
                                correctXYList.add(xy);

                                wordList.remove(w);
                                if (wordList.size() == 0) {
                                    showDialog("YOU WIN", "Your score : " + TIME_REMAINING);
                                }
                            }
                        }
                        canSwipe = false;
                        //Log.i(TAG, correctXYList.toString());
                    }

                    if (canSwipe && pTouchAreaLocalX >= 0
                            && pTouchAreaLocalX <= widthX
                            && pTouchAreaLocalY >= 0
                            && pTouchAreaLocalY <= widthY) {
                        int[] touch = touchAt(pTouchAreaLocalX, pTouchAreaLocalY);
                        if (isSwipeToNextAlphabet(touch)) {
                            wordStack.append(table[touch[0]][touch[1]]);
                            lastestTouch = touch;


                            posX = (touch[0] * alphaWidth) + startPositionX/2+ alphaWidth/2;

                            posY = (touch[1] * alphaWidth) + startPositionY/2+ alphaWidth/2+alphaWidth/4;

                            hover = new Sprite(posX, posY, alphaWidth, alphaWidth,
                                    hoverTextureRegion,
                                    getVertexBufferObjectManager());
                            scene.attachChild(hover);

                            hoverList.add(hover);
                        }
                    }
                } catch (Exception e)
                {
                    for (Sprite h : hoverList) {
                        scene.detachChild(h);
                    }
                   // e.printStackTrace();
                }
                return true;
            }
        };
        scene.attachChild(tableBackground);

        // 3 - Add Alphabets
        int x, y, posX, posY;
        for (y = 0; y < dimenY; y++) {

            posY = (y * alphaWidth)+  startPositionY/2 ;
            for (x = 0; x < dimenX; x++) {

                posX = (x * alphaWidth)+ startPositionX/2 ;
                spriteTable[x][y] = new WordSprite(x, y,
                        posX+alphaWidth/2,
                        posY+alphaWidth*3/4,
                        textureRegionList.get(table[x][y]),
                        resourcesManager.activity.getVertexBufferObjectManager());
                scene.attachChild(spriteTable[x][y]);
            }
        }

        // 4 - Add Font
        posX = startPositionX-CAMERA_WIDTH*2/5;

        posY = startPositionY +130 ;
        for (Word w : wordList) {
            w.text = new Text(posX, posY, this.mFont, w.value.toUpperCase(), new TextOptions(HorizontalAlign.LEFT),
                    resourcesManager.activity.getVertexBufferObjectManager());
            scene.attachChild(w.text);
            posY += w.text.getHeight();

        }

        // 6 - Timer & Helper
        posX = CAMERA_WIDTH - 80;
        posY = CAMERA_HEIGHT - 55;
        timerText = new Text(posX, posY,
                            this.mFont,
                            getTime(),
                            new TextOptions(HorizontalAlign.LEFT),
                            resourcesManager.activity.getVertexBufferObjectManager());
        scene.attachChild(timerText);

        posX = CAMERA_WIDTH - 145;
        posY = CAMERA_HEIGHT - 70;
        sprite = new Sprite(posX, posY, timerTextureRegion, resourcesManager.activity.getVertexBufferObjectManager());
        scene.attachChild(sprite);

        posX = CAMERA_WIDTH - 250;
        posY = CAMERA_HEIGHT - 70;
        ButtonSprite helper = new ButtonSprite(posX, posY,
                                    helpTimerTextureRegion,
                                    resourcesManager.activity.getVertexBufferObjectManager(),
                new OnClickListener() {

                    @Override
                    public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                        pButtonSprite.setEnabled(false);
                        pButtonSprite.setColor(Color.BLACK);
                        TIME_REMAINING += 60;
                    }
                });
        scene.attachChild(helper);

        // 7 - Register Event
        scene.registerTouchArea(helper);
        scene.registerTouchArea(tableBackground);
        scene.setTouchAreaBindingOnActionDownEnabled(true);
        scene.registerUpdateHandler(

                new TimerHandler(1f, true, new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler mTimerHandler) {

                        TIME_REMAINING--;
                        timerText.setText(getTime());
                        if (TIME_REMAINING == 0) {
                            scene.unregisterUpdateHandler(mTimerHandler);
                            showDialog("TIME'S UP", "You lose, Please try again!!");
                        }
                    }
                })
        );

        setChildScene(scene);
    }


    /*
     * Initial Function
     */
    private void initTable() {
        int x = 0;
        int y = 0;
        Random r = new Random();
        String w;
        for (y = 0; y < dimenY; y++) {
            for (x = 0; x < dimenX; x++) {
                w = String.valueOf((char) (r.nextInt(26) + 'a'));
                table[x][y] = w;
                loadAlphabet(w);
            }
        }
    }

    private void initWordList() {
        Random r = new Random();
        int id = r.nextInt(4) + 0;
        wordList = WordPrepare.getWords(id);
        for (Word word : wordList) {
            fillWord2Table(word);
        }
    }

    /*
     * Helper Function
     */
    private void loadAllAlphabet() {
        for (String key : alphabetBitmapList.keySet()) {
            ITexture texture = alphabetBitmapList.get(key);
            texture.load();
            textureRegionList.put(key, TextureRegionFactory.extractFromTexture(texture));
        }
    }

    private ITexture loadTexture(final String path) {
        try {
            ITexture texture = new BitmapTexture(resourcesManager.activity.getTextureManager(), new IInputStreamOpener() {
                @Override
                public InputStream open() throws IOException {
                    return resourcesManager.activity.getAssets().open(path);
                }
            });
            return texture;
        } catch (IOException e) {
            //Debug.e(e);
        }
        return null;
    }

    private void loadAlphabet(String key) {
        if (!alphabetBitmapList.containsKey(key))
            alphabetBitmapList.put(key, loadTexture("alphabets/" + key + ".png"));
    }

    private void fillWord2Table(Word word) {
        int len = word.value.length();
        int start = 0;
        String w;
        if (word.orientation == ENUM.HORIZONTAL) {
            for (start = 0; start < len; start++) {
                w = String.valueOf(word.value.charAt(start));
                table[word.x + start][word.y] = w;
                loadAlphabet(w);
            }
        } else {
            for (start = 0; start < len; start++) {
                w = String.valueOf(word.value.charAt(start));
                table[word.x][word.y + start] = w;
                loadAlphabet(w);
            }
        }
    }

    private int[] touchAt(float posX, float posY) { // return touch area's coodination
        int[] touch = new int[2];
        int x = (int) Math.floor((posX * dimenX) / (alphaWidth * dimenX));//the largest integer not greater than it
        int y = (int) Math.floor((posY * dimenY) / (alphaWidth * dimenY));
        touch[0] = (x >= dimenX ? dimenX - 1 : x); // if x>=dimenx then x=dimenx-1 else x=x
        touch[1] = (y >= dimenY ? dimenY - 1 : y);// if then else statement or ternary operator
        return touch;
    }

    private boolean isSwipeToNextAlphabet(int[] touch) {
        if (touch[0] == lastestTouch[0] && touch[1] == lastestTouch[1])
            return false;
        return true;
    }

    private Word isCorrectWord(String word) {
        for (Word w : wordList) {
            if (w.value.equalsIgnoreCase(word))
                return w;
        }
        return null;
    }

    private String getTime() {
        int minutes = TIME_REMAINING / 60;
        int seconds = TIME_REMAINING % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void showDialog(final String title, final String msg) {
        resourcesManager.activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(resourcesManager.activity);
                alert.setTitle(title);
                alert.setMessage(msg);
                alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SceneManager.getInstance().loadGameScene(engine);
                    }
                });
                alert.setNegativeButton("Exit", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SceneManager.getInstance().loadMenuScene(engine);
                    }
                });

                alert.show();
            }
        });
    }

    private void showToast( final String msg) {
        ResourcesManager.getInstance().activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ResourcesManager.getInstance().activity, msg + wordStack.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void createHUD()
    {
        gameHUD = new HUD();

        scoreText = new Text(10, CAMERA_HEIGHT*3/4,
                            resourcesManager.font,
                            "Score: 0123456789",
                            new TextOptions(HorizontalAlign.LEFT),
                            vbom);
        scoreText.setAnchorCenter(0, 0);
        scoreText.setText("Keyword:");

        gameHUD.attachChild(scoreText);

        camera.setHUD(gameHUD);
    }




}