package roguelike_emotions.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import roguelike_emotions.fusionCodex.FusionVisualHelpers;

/**
 * Builder para construcción de diálogos modales con animaciones.
 * Patrón: Builder + Fluent Interface
 * 
 * @version 1.0
 */
public class DialogBuilder {
    
    // Defaults
    private static final float DEFAULT_WIDTH = 480f;
    private static final float DEFAULT_HEIGHT = 280f;
    private static final Color DEFAULT_TITLE_COLOR = new Color(1f, 1f, 1f, 1f);
    
    // Required
    private final Skin skin;
    private final Stage stage;
    
    // Optional
    private String title = "";
    private String icon = "";
    private String message = "";
    private float width = DEFAULT_WIDTH;
    private float height = DEFAULT_HEIGHT;
    private Color titleColor = DEFAULT_TITLE_COLOR;
    private Runnable onClose;
    private Runnable onConfirm;
    private String confirmText = "Aceptar";
    private String cancelText = "Cancelar";
    private boolean showCancel = false;
    private Actor customContent;
    
    public DialogBuilder(Skin skin, Stage stage) {
        this.skin = skin;
        this.stage = stage;
    }
    
    // Fluent setters
    public DialogBuilder title(String title) {
        this.title = title;
        return this;
    }
    
    public DialogBuilder icon(String icon) {
        this.icon = icon;
        return this;
    }
    
    public DialogBuilder message(String message) {
        this.message = message;
        return this;
    }
    
    public DialogBuilder size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public DialogBuilder titleColor(Color color) {
        this.titleColor = color;
        return this;
    }
    
    public DialogBuilder onClose(Runnable callback) {
        this.onClose = callback;
        return this;
    }
    
    public DialogBuilder onConfirm(Runnable callback) {
        this.onConfirm = callback;
        return this;
    }
    
    public DialogBuilder confirmText(String text) {
        this.confirmText = text;
        return this;
    }
    
    public DialogBuilder cancelText(String text) {
        this.cancelText = text;
        this.showCancel = true;
        return this;
    }
    
    public DialogBuilder customContent(Actor content) {
        this.customContent = content;
        return this;
    }
    
    /**
     * Construye y muestra el diálogo
     */
    public Table build() {
        Table dialog = createDialogContainer();
        
        // Icono (opcional)
        if (!icon.isEmpty()) {
            addIcon(dialog, icon);
        }
        
        // Título
        if (!title.isEmpty()) {
            addTitle(dialog, title, titleColor);
        }
        
        // Mensaje o contenido custom
        if (customContent != null) {
            dialog.add(customContent).expand().fill().padBottom(20f);
            dialog.row();
        } else if (!message.isEmpty()) {
            addMessage(dialog, message);
        }
        
        // Botones
        addButtons(dialog);
        
        // Animación de entrada
        animateIn(dialog);
        
        stage.addActor(dialog);
        return dialog;
    }
    
    private Table createDialogContainer() {
        Table dialog = new Table();
        dialog.setBackground(FusionVisualHelpers.makeDialogBg());
        dialog.pad(32f);
        dialog.setSize(width, height);
        dialog.setPosition((stage.getWidth() - width) / 2, (stage.getHeight() - height) / 2);
        return dialog;
    }
    
    private void addIcon(Table dialog, String iconText) {
        Label iconLabel = new Label(iconText, skin, "h2");
        iconLabel.setFontScale(1.8f);
        iconLabel.setAlignment(Align.center);
        dialog.add(iconLabel).center().padBottom(12f);
        dialog.row();
    }
    
    private void addTitle(Table dialog, String titleText, Color color) {
        Label titleLabel = new Label(titleText, skin, "h2");
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(color);
        dialog.add(titleLabel).center().padBottom(16f);
        dialog.row();
    }
    
    private void addMessage(Table dialog, String msgText) {
        Label msgLabel = new Label(msgText, skin, "body");
        msgLabel.setAlignment(Align.center);
        msgLabel.setWrap(true);
        msgLabel.setFontScale(0.92f);
        dialog.add(msgLabel).width(width - 64f).center().padBottom(20f);
        dialog.row();
    }
    
    private void addButtons(Table dialog) {
        Table btnRow = new Table();
        
        if (showCancel) {
            TextButton btnCancel = new TextButton(cancelText, skin, "secondary");
            btnCancel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    closeDialog(dialog);
                    if (onClose != null) onClose.run();
                }
            });
            btnRow.add(btnCancel).width(160f).height(50f).padRight(12f);
        }
        
        TextButton btnConfirm = new TextButton(confirmText, skin, "primary");
        btnConfirm.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeDialog(dialog);
                if (onConfirm != null) onConfirm.run();
            }
        });
        btnRow.add(btnConfirm).width(showCancel ? 160f : 200f).height(50f);
        
        dialog.add(btnRow).center();
    }
    
    private void closeDialog(Table dialog) {
        dialog.addAction(Actions.sequence(
            Actions.fadeOut(0.15f),
            Actions.removeActor()
        ));
    }
    
    private void animateIn(Table dialog) {
        dialog.getColor().a = 0f;
        dialog.setScale(0.85f);
        dialog.setOrigin(Align.center);
        dialog.setTransform(true);
        dialog.addAction(Actions.parallel(
            Actions.fadeIn(0.25f, Interpolation.smooth),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.elasticOut)
        ));
    }
}
