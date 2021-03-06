package net.minecraft.launcher.ui.popups.profile;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.Document;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.Profile.Resolution;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

public class ProfileInfoPanel extends JPanel
  implements RefreshedVersionsListener
{
  private final ProfileEditorPopup editor;
  private final JCheckBox gameDirCustom = new JCheckBox("Game Directory:");
  private final JTextField profileName = new JTextField();
  private final JTextField gameDirField = new JTextField();
  private final JComboBox versionList = new JComboBox();
  private final JCheckBox resolutionCustom = new JCheckBox("Resolution:");
  private final JTextField resolutionWidth = new JTextField();
  private final JTextField resolutionHeight = new JTextField();

  public ProfileInfoPanel(ProfileEditorPopup editor) {
    this.editor = editor;

    setLayout(new GridBagLayout());
    setBorder(BorderFactory.createTitledBorder("Profile Info"));

    createInterface();
    fillDefaultValues();
    addEventHandlers();

    List versions = editor.getLauncher().getVersionManager().getVersions();

    if (versions.isEmpty())
      editor.getLauncher().getVersionManager().addRefreshedVersionsListener(this);
    else
      populateVersions(versions);
  }

  protected void createInterface()
  {
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

    this.versionList.setRenderer(new VersionListRenderer(null));
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
  }

  protected void addEventHandlers() {
    this.profileName.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileInfoPanel.this.updateProfileName();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateProfileName();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateProfileName();
      }
    });
    this.gameDirCustom.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateGameDirState();
      }
    });
    this.gameDirField.getDocument().addDocumentListener(new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileInfoPanel.this.updateGameDir();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateGameDir();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateGameDir();
      }
    });
    this.versionList.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateVersionSelection();
      }
    });
    this.resolutionCustom.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e) {
        ProfileInfoPanel.this.updateResolutionState();
      }
    });
    DocumentListener resolutionListener = new DocumentListener()
    {
      public void insertUpdate(DocumentEvent e) {
        ProfileInfoPanel.this.updateResolution();
      }

      public void removeUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateResolution();
      }

      public void changedUpdate(DocumentEvent e)
      {
        ProfileInfoPanel.this.updateResolution();
      }
    };
    this.resolutionWidth.getDocument().addDocumentListener(resolutionListener);
    this.resolutionHeight.getDocument().addDocumentListener(resolutionListener);
  }

  private void updateProfileName() {
    if (this.profileName.getText().length() > 0)
      this.editor.getProfile().setName(this.profileName.getText());
  }

  private void updateGameDirState()
  {
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
      Version version = ((VersionSyncInfo)selection).getLatestVersion();
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

  public void onVersionsRefreshed(VersionManager manager)
  {
    List versions = manager.getVersions();
    populateVersions(versions);
    this.editor.getLauncher().getVersionManager().removeRefreshedVersionsListener(this);
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  private void updateGameDir() {
    File file = new File(this.gameDirField.getText());
    this.editor.getProfile().setGameDir(file);
  }

  private static class VersionListRenderer extends BasicComboBoxRenderer
  {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      if ((value instanceof VersionSyncInfo)) {
        VersionSyncInfo syncInfo = (VersionSyncInfo)value;
        Version version = syncInfo.getLatestVersion();

        value = String.format("%s %s", new Object[] { version.getType().getName(), version.getId() });
      }

      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      return this;
    }
  }
}