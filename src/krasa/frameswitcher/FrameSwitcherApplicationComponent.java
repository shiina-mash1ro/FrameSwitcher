package krasa.frameswitcher;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import krasa.frameswitcher.networking.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;

@State(name = "FrameSwitcherSettings", storages = {@Storage(id = "FrameSwitcherSettings", file = "$APP_CONFIG$/FrameSwitcherSettings.xml")})
public class FrameSwitcherApplicationComponent implements ApplicationComponent,
		PersistentStateComponent<FrameSwitcherSettings>, ExportableApplicationComponent {
	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());
	public static final String IDE_MAX_RECENT_PROJECTS = "ide.max.recent.projects";


	private FrameSwitcherSettings settings;
	private RemoteInstancesState remoteInstancesState = new RemoteInstancesState();
	private RemoteSender remoteSender;
	private ProjectFocusMonitor projectFocusMonitor;

	public static FrameSwitcherApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(FrameSwitcherApplicationComponent.class);
	}

	public FrameSwitcherApplicationComponent() {
	}

	public ProjectFocusMonitor getProjectFocusMonitor() {
		if (projectFocusMonitor == null) {
			projectFocusMonitor = new ProjectFocusMonitor();
		}
		return projectFocusMonitor;
	}

	public void initComponent() {
		if (getState().isRemoting()) {
			initRemoting();
		} else {
			remoteSender = new DummyRemoteSender();
		}
	}

	private void initRemoting() {
		UUID uuid = UUID.randomUUID();
		try {
			remoteSender = new RemoteSenderImpl(uuid, new Receiver(uuid, this));
		} catch (Throwable e) {
			LOG.warn(e);
		}
	}

	public void disposeComponent() {
		if (getRemoteSender() != null) {
			getRemoteSender().close();
		}
	}


	@NotNull
	public String getComponentName() {
		return "FrameSwitcherApplicationComponent";
	}

	@NotNull
	@Override
	public File[] getExportFiles() {
		return new File[]{PathManager.getOptionsFile("FrameSwitcherSettings")};
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return "Frame Switcher";
	}

	@NotNull
	@Override
	public FrameSwitcherSettings getState() {
		if (settings == null) {
			settings = new FrameSwitcherSettings();
		}
		return settings;
	}

	@Override
	public void loadState(FrameSwitcherSettings frameSwitcherSettings) {
		this.settings = frameSwitcherSettings;
		frameSwitcherSettings.applyMaxRecentProjectsToRegistry();
	}

	public void updateSettings(FrameSwitcherSettings settings) {
		this.settings = settings;
		this.settings.applyOrResetMaxRecentProjectsToRegistry();
		if (remoteSender instanceof DummyRemoteSender && this.settings.isRemoting()) {
			initRemoting();
		} else if (remoteSender instanceof RemoteSenderImpl && !this.settings.isRemoting()) {
			remoteSender.close();
			remoteInstancesState = new RemoteInstancesState();
			remoteSender = new DummyRemoteSender();
		}
	}

	public RemoteInstancesState getRemoteInstancesState() {
		return remoteInstancesState;
	}

	public RemoteSender getRemoteSender() {
		return remoteSender;
	}

}
