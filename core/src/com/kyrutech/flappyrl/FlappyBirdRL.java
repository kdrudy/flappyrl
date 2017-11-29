package com.kyrutech.flappyrl;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

public class FlappyBirdRL extends ApplicationAdapter {
    SpriteBatch batch;
    Sprite flappyFall, flappyFlap, flappyDead;
    NinePatch topPipe, bottomPipe;

    BitmapFont font;

    public static final int FLAPPY_SIZE = 64;

    private Engine engine;

    private boolean flipped = false;

    private FlappyInput input;

    private boolean firstRender = true;

    private byte[] pixels = new byte[160 * 120 * 3];

    double time = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        flappyFall = new Sprite(new Texture("frame-1.png"));
        flappyFlap = new Sprite(new Texture("frame-2.png"));

        flappyDead = new Sprite(flappyFall);
        flappyDead.flip(false, true);

        bottomPipe = new NinePatch(new Texture("bottomPipe.png"), 9, 9, 20, 9);
        topPipe = new NinePatch(new Texture("topPipe.png"), 9, 9, 9, 20);

        font = new BitmapFont(Gdx.files.internal("menlobold.fnt"));

        engine = new Engine(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        input = new FlappyInput(this);

        Gdx.input.setInputProcessor(input);


    }

    public void startGame() {
        engine.setState(Engine.GameState.RUNNING);
    }

    private void restartGame() {
        engine.restart();
        flipped = false;
        time = 0;
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        drawFlappy();
        drawPipes();
        drawScore();

        batch.end();

        if (engine.getState() == Engine.GameState.RUNNING) {
            engine.act(Gdx.graphics.getDeltaTime());
        }

        time = Gdx.graphics.getDeltaTime();

        try {
            pixels = getScreenPixels();
        } catch (Exception ex) {
            //System.out.println(ex.getMessage());
        }
    }

    private void drawScore() {
        font.draw(batch, "Score: " + engine.getScore(), 10, Gdx.graphics.getHeight() - 20);
    }

    private void drawFlappy() {

        Rectangle flappyRect = engine.getFlappyRect();
        if(engine.getFlappy().isAlive()) {
            if (engine.getFlappy().getFallingSpeed() > 0) {
                batch.draw(flappyFall, flappyRect.x, flappyRect.y, flappyRect.width, flappyRect.height);
            } else {
                batch.draw(flappyFlap, flappyRect.x, flappyRect.y, flappyRect.width, flappyRect.height);
            }
        } else {
            batch.draw(flappyDead, flappyRect.x, flappyRect.y, flappyRect.width, flappyRect.height);
        }
    }

    private void drawPipes() {
        topPipe.draw(batch, engine.getTopPipe().x, engine.getTopPipe().y, engine.getTopPipe().width, engine.getTopPipe().height);
        bottomPipe.draw(batch, engine.getBottomPipe().x, engine.getBottomPipe().y, engine.getBottomPipe().width, engine.getBottomPipe().height);
    }

    @Override
    public void dispose() {
        batch.dispose();

    }

    public byte[] getScreenPixels() {
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        Pixmap pixmap2 = new Pixmap(160, 120, Pixmap.Format.RGBA8888);
        pixmap2.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap2.getWidth(), pixmap2.getHeight());

        return pixmap2.getPixels().array();
    }


    public enum Inputs {
        NOTHING, FLAP, RESTART
    }

    public double getScore() {
        return engine.getScore() + time;
    }

    public void handleInput(Inputs input) {
        switch (input) {
            case FLAP:
                this.input.keyDown(Input.Keys.SPACE);
                this.input.keyUp(Input.Keys.SPACE);
                break;
            case RESTART:
                this.input.keyDown(Input.Keys.S);
                break;
            case NOTHING:
                break;
        }
    }

    public byte[] getPixels() {
        return pixels;
    }

    public boolean isDone() {
        return engine.getState() == Engine.GameState.GAMEOVER;
    }

    private static class FlappyInput extends InputAdapter {

        FlappyBirdRL game;

        public FlappyInput(FlappyBirdRL game) {
            this.game = game;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.SPACE) {
                game.engine.flapFlappy();
            }
            if (game.engine.getState() == Engine.GameState.GAMEOVER && keycode == Input.Keys.S) {
                game.restartGame();
            }
            if (keycode == Input.Keys.P) {
                System.out.println("Taking screenshot");
                byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
                Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
                BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
                Pixmap pixmap2 = new Pixmap(160, 120, Pixmap.Format.RGBA8888);
                pixmap2.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap2.getWidth(), pixmap2.getHeight());
                PixmapIO.writePNG(Gdx.files.external("mypixmap.png"), pixmap2);
                pixmap.dispose();
            }
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (keycode == Input.Keys.SPACE) {
                game.engine.unFlapFlappy();
            }
            return true;
        }
    }
}
