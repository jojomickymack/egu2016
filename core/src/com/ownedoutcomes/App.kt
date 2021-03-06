package com.ownedoutcomes

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.viewport.Viewport
import com.ownedoutcomes.asset.loadSkin
import com.ownedoutcomes.logic.GameController
import com.ownedoutcomes.logic.GameRenderer
import com.ownedoutcomes.music.loadMusic
import com.ownedoutcomes.music.playMusic
import com.ownedoutcomes.view.*
import ktx.app.KotlinApplication
import ktx.app.LetterboxingViewport
import ktx.assets.loadOnDemand
import ktx.inject.inject
import ktx.inject.register
import ktx.scene2d.Scene2DSkin

class App : KotlinApplication() {
    private var view: View = MockView()

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        val viewport: Viewport = LetterboxingViewport(targetPpiX = 96f, targetPpiY = 96f, aspectRatio = 4f / 3f)
        val batch = SpriteBatch()
        val stage = Stage(viewport, batch)
        val skin = loadSkin()
        Scene2DSkin.defaultSkin = skin
        val menuView = Menu(stage)
        val nextLevelView = NextLevel(stage)
        val gameController = GameController()
        val runner = this
        val renderer = GameRenderer(gameController, batch, skin)
        val gameView = Game(stage, gameController, renderer)

        view = menuView
        register {
            bindSingleton(runner)
            bindSingleton(skin)
            bindSingleton(batch)
            bindSingleton(stage)
            bindSingleton(menuView)
            bindSingleton(gameView)
            bindSingleton(gameController)
            bindSingleton(renderer)
            bindSingleton(GameOver(stage))
            bindSingleton(nextLevelView)
        }

        loadSounds()
        loadMusic()
        playMusic()

        Gdx.input.inputProcessor = stage
        stage.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.5f)))
        view.show()
    }

    override fun resize(width: Int, height: Int) {
        view.resize(width, height)
    }

    override fun render(delta: Float) {
        view.render(delta)
    }

    fun loadSounds() {
        arrayOf("kill.wav", "newPlayer.wav", "burp.wav").forEach {
            loadOnDemand<Sound>(it).asset
        }
    }

    fun setCurrentView(nextView: View) {
        inject<Stage>().addAction(Actions.sequence(
                Actions.fadeOut(0.5f),
                Actions.run {
                    view.hide()
                    view = nextView
                    nextView.show()
                    nextView.resize(Gdx.graphics.width, Gdx.graphics.height)
                },
                Actions.fadeIn(0.5f),
                Actions.alpha(1f)
        ))
    }
}