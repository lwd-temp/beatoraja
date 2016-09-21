package bms.player.beatoraja.decide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import bms.player.beatoraja.Config.SkinConfig;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.PlayerResource;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;
import bms.player.beatoraja.play.BMSPlayer;
import bms.player.beatoraja.play.audio.SoundProcessor;
import bms.player.beatoraja.skin.LR2DecideSkinLoader;
import bms.player.beatoraja.skin.LR2PlaySkinLoader;
import bms.player.beatoraja.skin.LR2SkinHeader;
import bms.player.beatoraja.skin.LR2SkinHeaderLoader;
import bms.player.beatoraja.skin.SkinImage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * 曲決定部分。
 * 
 * @author exch
 */
public class MusicDecide extends MainState {

	private Sound bgm;

	private boolean cancel;

	public MusicDecide(MainController main) {
		super(main);
	}

	public void create() {
		cancel = false;
		final PlayerResource resource = getMainController().getPlayerResource();
		if (resource.getConfig().getBgmpath().length() > 0) {
			final File bgmfolder = new File(resource.getConfig().getBgmpath());
			if (bgmfolder.exists() && bgmfolder.isDirectory()) {
				for (File f : bgmfolder.listFiles()) {
					if (bgm == null && f.getName().startsWith("decide.")) {
						bgm = SoundProcessor.getSound(f.getPath());
						break;
					}
				}
			}
		}
		if (bgm != null) {
			bgm.play();
		}

		if (getSkin() == null) {
			if (resource.getConfig().getSkin()[6] != null) {
				try {
					SkinConfig sc = resource.getConfig().getSkin()[6];
					LR2SkinHeaderLoader loader = new LR2SkinHeaderLoader();
					LR2SkinHeader header = loader.loadSkin(Paths.get(sc.getPath()), this, sc.getProperty());
					Rectangle srcr = MainController.RESOLUTION[header.getResolution()];
					Rectangle dstr = MainController.RESOLUTION[resource.getConfig().getResolution()];
					LR2DecideSkinLoader dloader = new LR2DecideSkinLoader(srcr.width, srcr.height, dstr.width, dstr.height);
					setSkin(dloader.loadMusicDecideSkin(new File(header.getInclude()), this, header,
							loader.getOption(), sc.getProperty()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					setSkin(new MusicDecideSkin(MainController.RESOLUTION[resource.getConfig().getResolution()]));
				}
			} else {
				setSkin(new MusicDecideSkin(MainController.RESOLUTION[resource.getConfig().getResolution()]));
			}
		}
	}

	public void render() {
		long nowtime = getNowTime();

		if (getTimer()[BMSPlayer.TIMER_FADEOUT] != Long.MIN_VALUE) {
			if (nowtime > getTimer()[BMSPlayer.TIMER_FADEOUT] + getSkin().getFadeout()) {
				getMainController().changeState(
						cancel ? MainController.STATE_SELECTMUSIC : MainController.STATE_PLAYBMS);
			}
		} else {
			if (nowtime > getSkin().getInput()) {
				BMSPlayerInputProcessor input = getMainController().getInputProcessor();
				if (input.getKeystate()[0] || input.getKeystate()[2] || input.getKeystate()[4]
						|| input.getKeystate()[6]) {
					getTimer()[BMSPlayer.TIMER_FADEOUT] = nowtime;
				}
				if (input.isExitPressed()) {
					cancel = true;
					getTimer()[BMSPlayer.TIMER_FADEOUT] = nowtime;
				}
			}
			if (nowtime > getSkin().getScene()) {
				getTimer()[BMSPlayer.TIMER_FADEOUT] = nowtime;
			}
		}
	}

	@Override
	public void dispose() {
		if (bgm != null) {
			bgm.dispose();
			bgm = null;
		}
	}

	public String getTextValue(int id) {
		final PlayerResource resource = getMainController().getPlayerResource();
		if (resource.getCourseBMSModels() != null) {
			switch (id) {
			case STRING_TITLE:
			case STRING_FULLTITLE:
				return resource.getCoursetitle();
			}
			return "";
		}
		return super.getTextValue(id);
	}
}
