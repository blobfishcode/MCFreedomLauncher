package net.minecraft.launcher.ui.popups.profile;

import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.locale.LocaleHelper;
import net.minecraft.launcher.profile.LauncherVisibilityRule;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class ProfileInfoPanel extends JPanel
        implements RefreshedVersionsListener {
    private final ProfileEditorPopup editor;
    private final JCheckBox gameDirCustom = new JCheckBox("Game Directory:");
    private final JTextField profileName = new JTextField();
    private final JTextField gameDirField = new JTextField();
    private final JComboBox versionList = new JComboBox();
    private final JCheckBox resolutionCustom = new JCheckBox("Resolution:");
    private final JTextField resolutionWidth = new JTextField();
    private final JTextField resolutionHeight = new JTextField();
    private final JCheckBox allowSnapshots = new JCheckBox("Enable experimental development versions (\"snapshots\")");
    private final JComboBox<Locale> langList = new JComboBox<Locale>(LocaleHelper.getLocales());
    private final JCheckBox useHopper = new JCheckBox("Automatically ask Mojang for assistance with fixing crashes");
    private final JCheckBox launcherVisibilityCustom = new JCheckBox("Launcher Visibility:");
    private final JComboBox launcherVisibilityOption = new JComboBox();

    public ProfileInfoPanel(ProfileEditorPopup editor) {
        this.editor = editor;

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Profile Info"));

        createInterface();
        fillDefaultValues();
        addEventHandlers();

        //List versions = editor.getLauncher().getVersionManager().getVersions();
        List versions = editor.getLauncher().getVersionManager().getVersions(editor.getProfile().getVersionFilter());

        if (versions.isEmpty())
            editor.getLauncher().getVersionManager().addRefreshedVersionsListener(this);
        else
            populateVersions(versions);
    }

    protected void createInterface() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.anchor = 17;

        constraints.gridy = 0;

        add(new JLabel("Profile Name:"), constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(this.profileName, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        add(this.gameDirCustom, constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(this.gameDirField, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        constraints.fill = 2;
        constraints.weightx = 1.0D;
        constraints.gridwidth = 0;
        add(this.allowSnapshots, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        add(new JLabel("Use version:"), constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(this.versionList, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        JPanel resolutionPanel = new JPanel();
        resolutionPanel.setLayout(new BoxLayout(resolutionPanel, 0));
        resolutionPanel.add(this.resolutionWidth);
        resolutionPanel.add(Box.createHorizontalStrut(5));
        resolutionPanel.add(new JLabel("x"));
        resolutionPanel.add(Box.createHorizontalStrut(5));
        resolutionPanel.add(this.resolutionHeight);

        add(this.resolutionCustom, constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(resolutionPanel, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        constraints.fill = 2;
        constraints.weightx = 1.0D;
        constraints.gridwidth = 0;
        add(this.useHopper, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        add(this.launcherVisibilityCustom, constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(this.launcherVisibilityOption, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        add(new JLabel("Language:"), constraints);
        constraints.fill = 2;
        constraints.weightx = 1.0D;
        add(this.langList, constraints);
        constraints.weightx = 0.0D;
        constraints.fill = 0;

        constraints.gridy += 1;

        this.versionList.setRenderer(new VersionListRenderer());

        for (LauncherVisibilityRule value : LauncherVisibilityRule.values())
            this.launcherVisibilityOption.addItem(value);
    }

    protected void fillDefaultValues() {
        this.profileName.setText(this.editor.getProfile().getName());

        File gameDir = this.editor.getProfile().getGameDir();
        if (gameDir != null) {
            this.gameDirCustom.setSelected(true);
            this.gameDirField.setText(gameDir.getAbsolutePath());
        } else {
            this.gameDirCustom.setSelected(false);
            this.gameDirField.setText(this.editor.getLauncher().getWorkingDirectory().getAbsolutePath());
        }
        updateGameDirState();

        Profile.Resolution resolution = this.editor.getProfile().getResolution();
        this.resolutionCustom.setSelected(resolution != null);
        if (resolution == null) resolution = Profile.DEFAULT_RESOLUTION;
        this.resolutionWidth.setText(String.valueOf(resolution.getWidth()));
        this.resolutionHeight.setText(String.valueOf(resolution.getHeight()));
        updateResolutionState();

        this.allowSnapshots.setSelected(this.editor.getProfile().getVersionFilter().getTypes().contains(ReleaseType.SNAPSHOT));

        this.useHopper.setSelected(this.editor.getProfile().getUseHopperCrashService());

        LauncherVisibilityRule visibility = this.editor.getProfile().getLauncherVisibilityOnGameClose();

        if (visibility != null) {
            this.launcherVisibilityCustom.setSelected(true);
            this.launcherVisibilityOption.setSelectedItem(visibility);
        } else {
            this.launcherVisibilityCustom.setSelected(false);
            this.launcherVisibilityOption.setSelectedItem(Profile.DEFAULT_LAUNCHER_VISIBILITY);
        }
        updateLauncherVisibilityState();

        this.langList.setSelectedIndex(0);

    }

    protected void addEventHandlers() {
        this.profileName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateProfileName();
            }

            public void removeUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateProfileName();
            }

            public void changedUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateProfileName();
            }
        });
        this.gameDirCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateGameDirState();
            }
        });
        this.gameDirField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateGameDir();
            }

            public void removeUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateGameDir();
            }

            public void changedUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateGameDir();
            }
        });
        this.versionList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateVersionSelection();
            }
        });
        this.resolutionCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateResolutionState();
            }
        });
        DocumentListener resolutionListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateResolution();
            }

            public void removeUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateResolution();
            }

            public void changedUpdate(DocumentEvent e) {
                ProfileInfoPanel.this.updateResolution();
            }
        };
        this.resolutionWidth.getDocument().addDocumentListener(resolutionListener);
        this.resolutionHeight.getDocument().addDocumentListener(resolutionListener);

        this.allowSnapshots.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateCustomVersionFilter();
            }
        });
        this.useHopper.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateHopper();
            }
        });
        this.launcherVisibilityCustom.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateLauncherVisibilityState();
            }
        });
        this.launcherVisibilityOption.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ProfileInfoPanel.this.updateLauncherVisibilitySelection();
            }
        });
        this.langList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ProfileInfoPanel.this.editor.getProfile().setLocale((Locale) langList.getSelectedItem());
            }
        });
    }

    private void updateLauncherVisibilityState() {
        Profile profile = this.editor.getProfile();

        if ((this.launcherVisibilityCustom.isSelected()) && ((this.launcherVisibilityOption.getSelectedItem() instanceof LauncherVisibilityRule))) {
            profile.setLauncherVisibilityOnGameClose((LauncherVisibilityRule) this.launcherVisibilityOption.getSelectedItem());
            this.launcherVisibilityOption.setEnabled(true);
        } else {
            profile.setLauncherVisibilityOnGameClose(null);
            this.launcherVisibilityOption.setEnabled(false);
        }
    }

    private void updateLauncherVisibilitySelection() {
        Profile profile = this.editor.getProfile();

        if ((this.launcherVisibilityOption.getSelectedItem() instanceof LauncherVisibilityRule))
            profile.setLauncherVisibilityOnGameClose((LauncherVisibilityRule) this.launcherVisibilityOption.getSelectedItem());
    }

    private void updateHopper() {
        Profile profile = this.editor.getProfile();

        if (this.useHopper.isSelected())
            profile.setUseHopperCrashService(true);
        else
            profile.setUseHopperCrashService(false);
    }

    private void updateCustomVersionFilter() {
        Profile profile = this.editor.getProfile();

        if (this.allowSnapshots.isSelected()) {
            if (profile.getAllowedReleaseTypes() == null) {
                profile.setAllowedReleaseTypes(new HashSet(Profile.DEFAULT_RELEASE_TYPES));
            }

            profile.getAllowedReleaseTypes().add(ReleaseType.SNAPSHOT);
        } else if (profile.getAllowedReleaseTypes() != null) {
            profile.getAllowedReleaseTypes().remove(ReleaseType.SNAPSHOT);

            if (profile.getAllowedReleaseTypes().equals(Profile.DEFAULT_RELEASE_TYPES)) {
                profile.setAllowedReleaseTypes(null);
            }
        }

        populateVersions(this.editor.getLauncher().getVersionManager().getVersions(this.editor.getProfile().getVersionFilter()));
        this.editor.getLauncher().getVersionManager().removeRefreshedVersionsListener(this);
    }

    private void updateProfileName() {
        if (this.profileName.getText().length() > 0)
            this.editor.getProfile().setName(this.profileName.getText());
    }

    private void updateGameDirState() {
        if (this.gameDirCustom.isSelected()) {
            this.gameDirField.setEnabled(true);
            this.editor.getProfile().setGameDir(new File(this.gameDirField.getText()));
        } else {
            this.gameDirField.setEnabled(false);
            this.editor.getProfile().setGameDir(null);
        }
    }

    private void updateResolutionState() {
        if (this.resolutionCustom.isSelected()) {
            this.resolutionWidth.setEnabled(true);
            this.resolutionHeight.setEnabled(true);
            updateResolution();
        } else {
            this.resolutionWidth.setEnabled(false);
            this.resolutionHeight.setEnabled(false);
            this.editor.getProfile().setResolution(null);
        }
    }

    private void updateResolution() {
        try {
            int width = Integer.parseInt(this.resolutionWidth.getText());
            int height = Integer.parseInt(this.resolutionHeight.getText());

            this.editor.getProfile().setResolution(new Profile.Resolution(width, height));
        } catch (NumberFormatException ignored) {
            this.editor.getProfile().setResolution(null);
        }
    }

    private void updateVersionSelection() {
        Object selection = this.versionList.getSelectedItem();

        if ((selection instanceof VersionSyncInfo)) {
            Version version = ((VersionSyncInfo) selection).getLatestVersion();
            this.editor.getProfile().setLastVersionId(version.getId());
        } else {
            this.editor.getProfile().setLastVersionId(null);
        }
    }

    private void populateVersions(List<VersionSyncInfo> versions) {
        String previous = this.editor.getProfile().getLastVersionId();
        VersionSyncInfo selected = null;

        this.versionList.removeAllItems();
        this.versionList.addItem("Use Latest Version");

        for (VersionSyncInfo version : versions) {
            if (version.getLatestVersion().getId().equals(previous)) {
                selected = version;
            }

            this.versionList.addItem(version);
        }

        if ((selected == null) && (!versions.isEmpty()))
            this.versionList.setSelectedIndex(0);
        else
            this.versionList.setSelectedItem(selected);
    }

    public void onVersionsRefreshed(VersionManager manager) {
        List versions = manager.getVersions(this.editor.getProfile().getVersionFilter());
        //List versions = manager.getVersions();
        populateVersions(versions);
        this.editor.getLauncher().getVersionManager().removeRefreshedVersionsListener(this);
    }

    public boolean shouldReceiveEventsInUIThread() {
        return true;
    }

    private void updateGameDir() {
        File file = new File(this.gameDirField.getText());
        this.editor.getProfile().setGameDir(file);
    }

    private static class VersionListRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if ((value instanceof VersionSyncInfo)) {
                VersionSyncInfo syncInfo = (VersionSyncInfo) value;
                Version version = syncInfo.getLatestVersion();

                value = String.format("%s %s", new Object[]{version.getType().getName(), version.getId()});
            }

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            return this;
        }
    }
}

