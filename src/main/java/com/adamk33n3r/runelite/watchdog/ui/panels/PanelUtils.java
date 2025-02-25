package com.adamk33n3r.runelite.watchdog.ui.panels;

import com.adamk33n3r.runelite.watchdog.Displayable;
import com.adamk33n3r.runelite.watchdog.Util;
import com.adamk33n3r.runelite.watchdog.ui.Icons;
import com.adamk33n3r.runelite.watchdog.ui.PlaceholderTextArea;

import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

import org.apache.commons.text.WordUtils;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class PanelUtils {
    private static final ImageIcon FOLDER;
    static {
        final BufferedImage folderImg = ImageUtil.loadImageResource(Icons.class, "mdi_folder-open.png");

        FOLDER = new ImageIcon(folderImg);
    }

    private PanelUtils () {}

    public static JPanel createLabeledComponent(String label, String tooltip, Component component) {
        return createLabeledComponent(label, tooltip, component, false);
    }

    public static JPanel createLabeledComponent(String label, String tooltip, Component component, boolean twoLines) {
        JPanel panel = new JPanel();
        if (twoLines) {
            panel.setLayout(new DynamicGridLayout(2, 0, 5, 5));
        } else {
            panel.setLayout(new BorderLayout(5, 0));
        }
        JLabel jLabel = new JLabel(label);
        jLabel.setToolTipText(tooltip);
        panel.add(jLabel, BorderLayout.WEST);
        panel.add(component);
        return panel;
    }

    public static JPanel createIconComponent(ImageIcon icon, String tooltip, Component component) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(null);
        JLabel jLabel = new JLabel(icon);
        jLabel.setToolTipText(tooltip);
        panel.add(jLabel, BorderLayout.WEST);
        panel.add(component);
        return panel;
    }

    public static JPanel createFileChooser(String label, String tooltip, ActionListener actionListener, String path, String filterLabel, String... filters) {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.setBackground(null);
        if (label != null) {
            panel.setLayout(new GridLayout(2, 1));
            JLabel jLabel = new JLabel(label);
            jLabel.setToolTipText(tooltip);
            panel.add(jLabel);
        }
        JPanel chooserPanel = new JPanel(new BorderLayout(5, 0));
        chooserPanel.setBackground(null);
        panel.add(chooserPanel);
        JTextField pathField = new JTextField(path);
        pathField.setToolTipText(path);
        pathField.setEditable(false);
        chooserPanel.add(pathField);
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                String fileName = file.getName();
                int i = fileName.lastIndexOf('.');
                String extension = null;
                if (i > 0 &&  i < fileName.length() - 1) {
                    extension = fileName.substring(i+1).toLowerCase();
                }
                if (extension != null) {
                    return Arrays.asList(filters).contains(extension);
                }
                return false;
            }

            @Override
            public String getDescription() {
                return filterLabel + Arrays.stream(filters).map(ft -> "*." + ft).collect(Collectors.joining(", ", " (", ")"));
            }
        });
        JButton fileChooserButton = new JButton(null, FOLDER);
        fileChooserButton.setToolTipText(tooltip);
        fileChooserButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                String absPath = fileChooser.getSelectedFile().getAbsolutePath();
                pathField.setText(absPath);
                pathField.setToolTipText(absPath);
                actionListener.actionPerformed(new ActionEvent(fileChooser, result, "selected"));
            }
        });
        chooserPanel.add(fileChooserButton, BorderLayout.EAST);
        return panel;
    }

    public static JButton createActionButton(ImageIcon icon, ImageIcon rolloverIcon, String tooltip, ButtonClickListener listener) {
        JButton actionButton = new JButton();
        SwingUtil.removeButtonDecorations(actionButton);
        actionButton.setPreferredSize(new Dimension(16, 16));
        actionButton.setIcon(icon);
        actionButton.setRolloverIcon(rolloverIcon);
        actionButton.setToolTipText(tooltip);
        actionButton.addActionListener(ev -> listener.clickPerformed(actionButton, ev.getModifiers()));
        return actionButton;
    }

    public interface ButtonClickListener {
        void clickPerformed(JButton button, int modifiers);
    }

    public interface OnRemove {
        void elementRemoved(JComponent removed);
    }

    public static JButton createToggleActionButton(ImageIcon onIcon, ImageIcon onRolloverIcon, ImageIcon offIcon, ImageIcon offRolloverIcon, String onTooltip, String offTooltip, boolean initialValue, ButtonClickListener listener) {
        JButton actionButton = createActionButton(offIcon, offRolloverIcon, offTooltip, (btn, modifiers) -> {
            btn.setSelected(!btn.isSelected());
            listener.clickPerformed(btn, modifiers);
        });
        SwingUtil.addModalTooltip(actionButton, onTooltip, offTooltip);
        actionButton.setSelectedIcon(onIcon);
        actionButton.setRolloverSelectedIcon(onRolloverIcon);
        actionButton.setSelected(initialValue);
        return actionButton;
    }

    public static JCheckBox createCheckbox(String name, String tooltip, boolean initialValue, Consumer<Boolean> onChange) {
        JCheckBox checkbox = new JCheckBox(name, initialValue);
        checkbox.setToolTipText(tooltip);
        checkbox.addItemListener(ev -> {
            onChange.accept(checkbox.isSelected());
        });
        return checkbox;
    }


    public static JTextArea createTextArea(String placeholder, String tooltip, String initialValue, Consumer<String> onChange) {
        PlaceholderTextArea textArea = new PlaceholderTextArea(initialValue);
        textArea.setPlaceholder(placeholder);
        textArea.setToolTipText(tooltip);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(4, 6, 5, 6));
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                textArea.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                onChange.accept(textArea.getText());
            }
        });

        return textArea;
    }

    public static JSpinner createSpinner(int initialValue, int min, int max, int step, Consumer<Integer> onChange) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, min, max, step));
        spinner.addChangeListener(e -> {
            onChange.accept((Integer) spinner.getValue());
        });

        return spinner;
    }

    public static ColorJButton createColorPicker(String placeholder, String tooltip, String windowTitle, Component parentComponent, Color initialValue, ColorPickerManager colorPickerManager, boolean showAlpha, Consumer<Color> onChange) {
        ColorJButton colorPickerBtn = new ColorJButton(placeholder, Color.BLACK);
        if (initialValue != null) {
            String colorHex = "#" + ColorUtil.colorToAlphaHexCode(initialValue).toUpperCase();
            colorPickerBtn.setText(colorHex);
            colorPickerBtn.setColor(initialValue);
        }
        colorPickerBtn.setToolTipText(tooltip);
        colorPickerBtn.setFocusable(false);
        colorPickerBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RuneliteColorPicker colorPicker = colorPickerManager.create(
                    SwingUtilities.getWindowAncestor(colorPickerBtn),
                    colorPickerBtn.getColor(),
                    windowTitle,
                    !showAlpha);
                colorPicker.setLocation(parentComponent.getLocationOnScreen());
                colorPicker.setOnColorChange(c -> {
                    colorPickerBtn.setColor(c);
                    colorPickerBtn.setText("#" + ColorUtil.colorToAlphaHexCode(c).toUpperCase());
                });
                colorPicker.setOnClose(onChange);
                colorPicker.setVisible(true);
            }
        });

        return colorPickerBtn;
    }

    public static <T extends Enum<T>> JComboBox<T> createSelect(T[] items, T initialValue, Consumer<T> onChange) {
        JComboBox<T> select = new JComboBox<>(items);
        select.setSelectedItem(initialValue);
        select.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (value instanceof Displayable) {
                list.setToolTipText(((Displayable) value).getTooltip());
                return new DefaultListCellRenderer().getListCellRendererComponent(list, ((Displayable) value).getName(), index, isSelected, cellHasFocus);
            }
            String titleized = value == null ? "null" : WordUtils.capitalizeFully(value.name());
            list.setToolTipText(titleized);
            return new DefaultListCellRenderer().getListCellRendererComponent(list, titleized, index, isSelected, cellHasFocus);
        });
        select.addActionListener(e -> {
            onChange.accept(select.getItemAt(select.getSelectedIndex()));
        });

        return select;
    }

    public static <T> JComboBox<T> createSelect(T[] items, T initialValue, @Nullable Function<T, String> onRender, Consumer<T> onChange) {
        JComboBox<T> select = new JComboBox<>(items);
        select.setSelectedItem(initialValue);
        select.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (onRender != null) {
                String title = value == null ? "Loading..." : onRender.apply(value);
                return new DefaultListCellRenderer().getListCellRendererComponent(list, title, index, isSelected, cellHasFocus);
            }

            if (value instanceof Displayable) {
                list.setToolTipText(((Displayable) value).getTooltip());
                return new DefaultListCellRenderer().getListCellRendererComponent(list, ((Displayable) value).getName(), index, isSelected, cellHasFocus);
            }
            String titleized = value == null ? "null" : WordUtils.capitalizeFully(value.toString());
            list.setToolTipText(titleized);
            return new DefaultListCellRenderer().getListCellRendererComponent(list, titleized, index, isSelected, cellHasFocus);
        });
        select.addActionListener(e -> {
            onChange.accept(select.getItemAt(select.getSelectedIndex()));
        });

        return select;
    }

    public static boolean isPatternValid(Component parent, String pattern, boolean isRegex) {
        try {
            Pattern.compile(isRegex ? pattern : Util.createRegexFromGlob(pattern));
            return true;
        } catch (PatternSyntaxException ex) {
            JLabel errorLabel = new JLabel("<html>" + ex.getMessage().replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;") + "</html>");
            errorLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(parent, errorLabel, "Error in regex/pattern", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
