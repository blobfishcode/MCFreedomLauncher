package net.minecraft.launcher.ui.popups.login;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.locale.LocaleHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

public class LogInPopup extends JPanel
        implements ActionListener {
    private ResourceBundle resourceBundle = LocaleHelper.getMessages();
    private final Launcher launcher;
    private final Callback callback;
    private final AuthErrorForm errorForm;
    private final ExistingUserListForm existingUserListForm;
    private final LogInForm logInForm;
    private final JButton loginButton = new JButton(resourceBundle.getString("log.in"));
    private final JButton registerButton = new JButton(resourceBundle.getString("register"));
    private final JProgressBar progressBar = new JProgressBar();

    public LogInPopup(Launcher launcher, Callback callback) {
        super(true);
        this.launcher = launcher;
        this.callback = callback;
        this.errorForm = new AuthErrorForm(this);
        this.existingUserListForm = new ExistingUserListForm(this);
        this.logInForm = new LogInForm(this);

        createInterface();

        this.loginButton.addActionListener(this);
        this.registerButton.addActionListener(this);
    }

    protected void createInterface() {
        setLayout(new BoxLayout(this, 1));
        setBorder(new EmptyBorder(5, 15, 5, 15));
        try {
            InputStream stream = LogInPopup.class.getResourceAsStream("/minecraft_logo.png");
            if (stream != null) {
                BufferedImage image = ImageIO.read(stream);
                JLabel label = new JLabel(new ImageIcon(image));
                JPanel imagePanel = new JPanel();
                imagePanel.add(label);
                add(imagePanel);
                add(Box.createVerticalStrut(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!this.launcher.getProfileManager().getAuthDatabase().getKnownNames().isEmpty())
            add(this.existingUserListForm);
        add(this.errorForm);
        add(this.logInForm);

        add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(this.registerButton);
        buttonPanel.add(this.loginButton);

        add(buttonPanel);

        this.progressBar.setIndeterminate(true);
        this.progressBar.setVisible(false);
        add(this.progressBar);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.loginButton)
            this.logInForm.tryLogIn();
        else if (e.getSource() == this.registerButton)
            OperatingSystem.openLink(LauncherConstants.URL_REGISTER);
    }

    public static void showLoginPrompt(final Launcher launcher, final Callback callback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LogInPopup popup = new LogInPopup(launcher, callback);
                launcher.getLauncherPanel().setCard("login", popup);
            }
        });
    }

    public Launcher getLauncher() {
        return this.launcher;
    }

    public void setCanLogIn(final boolean enabled) {
        if (SwingUtilities.isEventDispatchThread()) {
            this.loginButton.setEnabled(enabled);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setIndeterminate(true);
            this.progressBar.setVisible(!enabled);
            repack();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    LogInPopup.this.setCanLogIn(enabled);
                }
            });
        }
    }

    public LogInForm getLogInForm() {
        return this.logInForm;
    }

    public AuthErrorForm getErrorForm() {
        return this.errorForm;
    }

    public ExistingUserListForm getExistingUserListForm() {
        return this.existingUserListForm;
    }

    public void setLoggedIn(String uuid) {
        this.callback.onLogIn(uuid);
    }

    public void repack() {
        Window window = SwingUtilities.windowForComponent(this);
        if (window != null) window.pack();
    }

    public static abstract interface Callback {
        public abstract void onLogIn(String paramString);
    }
}