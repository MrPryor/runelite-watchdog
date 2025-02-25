package com.adamk33n3r.runelite.watchdog.ui;

import com.adamk33n3r.runelite.watchdog.AlertManager;
import com.adamk33n3r.runelite.watchdog.WatchdogPanel;
import com.adamk33n3r.runelite.watchdog.alerts.Alert;
import com.adamk33n3r.runelite.watchdog.ui.panels.PanelUtils;

import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.MouseDragEventForwarder;
import net.runelite.client.util.SwingUtil;

import lombok.Getter;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class AlertListItem extends JPanel {

    private static final int ROW_HEIGHT = 30;
    private static final int PADDING = 2;

    @Getter
    private final Alert alert;

    public AlertListItem(WatchdogPanel panel, AlertManager alertManager, Alert alert, JComponent parent) {
        this.alert = alert;

        this.setLayout(new BorderLayout(5, 0));
        this.setBorder(new EmptyBorder(PADDING, 0, PADDING, 0));
        this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, ROW_HEIGHT + PADDING * 2));


        JPanel frontGroup = new JPanel(new DynamicGridLayout(1, 0, 3, 0));

        JButton dragHandle = new JButton(Icons.DRAG_VERT);
        SwingUtil.removeButtonDecorations(dragHandle);
        dragHandle.setPreferredSize(new Dimension(8, 16));
        MouseDragEventForwarder mouseDragEventForwarder = new MouseDragEventForwarder(parent);
        dragHandle.addMouseListener(mouseDragEventForwarder);
        dragHandle.addMouseMotionListener(mouseDragEventForwarder);
        frontGroup.add(dragHandle);

        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setSelected(alert.isEnabled());
        toggleButton.addItemListener(i -> {
            alert.setEnabled(toggleButton.isSelected());
            alertManager.saveAlerts();
        });
        toggleButton.setOpaque(false);
        frontGroup.add(toggleButton);

        this.add(frontGroup, BorderLayout.LINE_START);

        final JButton alertButton = new JButton(alert.getName());
        alertButton.setToolTipText(alert.getName());
        alertButton.addActionListener(ev -> {
            panel.openAlert(alert);
        });
        this.add(alertButton, BorderLayout.CENTER);

        final JPanel actionButtons = new JPanel(new DynamicGridLayout(1, 0, 0, 0));
        this.add(actionButtons, BorderLayout.LINE_END);

        actionButtons.add(PanelUtils.createActionButton(Icons.CLONE, Icons.CLONE, "Clone Alert", (btn, modifiers) -> {
            alertManager.cloneAlert(alert);
        }));

        final JButton deleteButton = PanelUtils.createActionButton(Icons.DELETE, Icons.DELETE, "Delete Alert", (btn, modifiers) -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the " + alert.getName() + " alert?", "Delete?", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                alertManager.removeAlert(alert);
            }
        });
        actionButtons.add(deleteButton);
    }
}
