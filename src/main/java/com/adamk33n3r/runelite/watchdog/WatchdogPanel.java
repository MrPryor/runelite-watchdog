package com.adamk33n3r.runelite.watchdog;

import com.adamk33n3r.runelite.watchdog.alerts.*;
import com.adamk33n3r.runelite.watchdog.ui.AlertListItem;
import com.adamk33n3r.runelite.watchdog.ui.Icons;
import com.adamk33n3r.runelite.watchdog.ui.ImportExportDialog;
import com.adamk33n3r.runelite.watchdog.ui.SearchBar;
import com.adamk33n3r.runelite.watchdog.ui.dropdownbutton.DropDownButtonFactory;
import com.adamk33n3r.runelite.watchdog.ui.panels.AlertPanel;
import com.adamk33n3r.runelite.watchdog.ui.panels.HistoryPanel;
import com.adamk33n3r.runelite.watchdog.ui.panels.PanelUtils;

import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.MultiplexingPluginPanel;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.DragAndDropReorderPane;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.Text;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.text.WordUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class WatchdogPanel extends PluginPanel {
    @Inject
    @Named("watchdog.helpURL")
    private String HELP_URL;

    @Inject
    @Named("watchdog.discordURL")
    private String DISCORD_URL;

    @Inject
    @Named("watchdog.kofiURL")
    private String KOFI_URL;

    @Inject
    @Named("watchdog.pluginVersion")
    private String PLUGIN_VERSION;

    @Getter
    private final MultiplexingPluginPanel muxer = new MultiplexingPluginPanel(this);

    @Getter
    @Inject
    private Provider<HistoryPanel> historyPanelProvider;

    @Inject
    private AlertManager alertManager;

    private String filterText = "";
    private static final Splitter SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();
    DragAndDropReorderPane dragAndDropReorderPane = new DragAndDropReorderPane();
    private final List<AlertListItem> alertListItems = new ArrayList<>();
    @Inject
    private OkHttpClient httpClient;

    public WatchdogPanel() {
        super(false);
        this.dragAndDropReorderPane.addDragListener((c) -> {
            int pos = this.dragAndDropReorderPane.getPosition(c);
            AlertListItem alertListItem = (AlertListItem) c;
            alertManager.moveAlertTo(alertListItem.getAlert(), pos);
        });
    }

    public void rebuild() {
        this.removeAll();
        this.setLayout(new BorderLayout(0, 3));
        this.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        JLabel title = new JLabel(WatchdogPlugin.getInstance().getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setHorizontalAlignment(JLabel.LEFT);
        title.setForeground(Color.WHITE);
        titlePanel.add(title);
        JLabel version = new JLabel("v"+PLUGIN_VERSION);
        title.setToolTipText(version.getText());
        version.setFont(version.getFont().deriveFont(10f));
        version.setBorder(new EmptyBorder(5, 0, 0, 0));
        titlePanel.add(version);
        topPanel.add(titlePanel);

        JPanel actionButtons = new JPanel();

        JButton discordButton = PanelUtils.createActionButton(Icons.DISCORD, Icons.DISCORD_HOVER, "Discord", (btn, modifiers) -> {
            LinkBrowser.browse(DISCORD_URL);
        });
        actionButtons.add(discordButton);

        JButton kofiButton = PanelUtils.createActionButton(Icons.KOFI, Icons.KOFI_HOVER, "Buy me a coffee :)", (btn, modifiers) -> {
            LinkBrowser.browse(KOFI_URL);
        });
        kofiButton.setPreferredSize(new Dimension(17, 17));
        actionButtons.add(kofiButton);

        JButton helpButton = PanelUtils.createActionButton(Icons.HELP, Icons.HELP_HOVER, "Wiki", (btn, modifiers) -> {
            LinkBrowser.browse(HELP_URL);
        });
        actionButtons.add(helpButton);

        JButton configButton = PanelUtils.createActionButton(Icons.CONFIG, Icons.CONFIG_HOVER, "Config", (btn, modifiers) -> {
            WatchdogPlugin.getInstance().openConfiguration();
        });
        actionButtons.add(configButton);

        JButton historyButton = PanelUtils.createActionButton(Icons.HISTORY, Icons.HISTORY_HOVER, "History", (btn, modifiers) -> {
            this.muxer.pushState(this.historyPanelProvider.get());
        });
        actionButtons.add(historyButton);

        JPopupMenu popupMenu = new JPopupMenu();
        ActionListener actionListener = e -> {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            TriggerType tType = (TriggerType) menuItem.getClientProperty(TriggerType.class);
            this.createAlert(tType);
        };

        Arrays.stream(TriggerType.values())
            .forEach(tType -> {
                JMenuItem c = new JMenuItem(WordUtils.capitalizeFully(tType.name().replaceAll("_", " ")));
                c.setToolTipText(tType.getTooltip());
                c.putClientProperty(TriggerType.class, tType);
                c.addActionListener(actionListener);
                popupMenu.add(c);
            });
        JButton addDropDownButton = DropDownButtonFactory.createDropDownButton(Icons.ADD, popupMenu);
        addDropDownButton.setToolTipText("Create New Alert");
        actionButtons.add(addDropDownButton);

        topPanel.add(actionButtons, BorderLayout.EAST);

        SearchBar searchBar = new SearchBar(this::filter);
        Arrays.stream(TriggerType.values()).map(TriggerType::getName).forEach(searchBar.getSuggestionListModel()::addElement);
        JPanel searchWrapper = new JPanel(new BorderLayout(0, 6));
        searchWrapper.add(searchBar);
        searchWrapper.setBorder(new EmptyBorder(0, 5, 0, 5));
        topPanel.add(searchWrapper, BorderLayout.SOUTH);

        this.add(topPanel, BorderLayout.NORTH);

        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        alertPanel.add(dragAndDropReorderPane, BorderLayout.NORTH);

        this.alertListItems.clear();
        this.dragAndDropReorderPane.removeAll();
        this.alertManager.getAlerts().stream()
            .map(alert -> new AlertListItem(this, this.alertManager, alert, this.dragAndDropReorderPane))
            .forEach(alertListItem -> {
                this.alertListItems.add(alertListItem);
                this.dragAndDropReorderPane.add(alertListItem);
            });
        if (!this.filterText.isEmpty())
            this.filter(this.filterText);
        this.add(new JScrollPane(alertPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel importExportGroup = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton importButton = new JButton("Import", Icons.IMPORT);
        importButton.setHorizontalTextPosition(SwingConstants.LEFT);
        importButton.addActionListener(ev -> {
            ImportExportDialog importExportDialog = new ImportExportDialog(SwingUtilities.getWindowAncestor(this));
            importExportDialog.setVisible(true);
        });
        importExportGroup.add(importButton);
        JButton exportButton = new JButton("Export", Icons.EXPORT);
        exportButton.setHorizontalTextPosition(SwingConstants.LEFT);
        exportButton.addActionListener(ev -> {
            ImportExportDialog importExportDialog = new ImportExportDialog(SwingUtilities.getWindowAncestor(this), WatchdogPlugin.getInstance().getConfig().alerts());
            importExportDialog.setVisible(true);
        });
        importExportGroup.add(exportButton);
        this.add(importExportGroup, BorderLayout.SOUTH);

        // Need this for rebuild for some reason
        this.revalidate();
    }

    public void openAlert(Alert alert) {
        PluginPanel panel = this.createPluginPanel(alert);
        if (panel != null) {
            this.muxer.pushState(panel);
        } else {
            log.error(String.format("Tried to open an alert of type %s that doesn't have a panel.", alert.getClass()));
        }
    }

    private void createAlert(TriggerType triggerType) {
        Alert createdAlert = WatchdogPlugin.getInstance().getInjector().getInstance(triggerType.getImplClass());
        this.alertManager.addAlert(createdAlert);
        this.openAlert(createdAlert);
    }

    private PluginPanel createPluginPanel(Alert alert) {
        if (alert instanceof ChatAlert) {
            ChatAlert gameMessageAlert = (ChatAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                .addAlertDefaults(alert)
                .addRegexMatcher(gameMessageAlert, "Enter the message to trigger on...", "The message to trigger on. Supports glob (*)")
                .build();
        } else if (alert instanceof PlayerChatAlert) {
            PlayerChatAlert playerChatAlert = (PlayerChatAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                    .addAlertDefaults(alert)
                    .addRegexMatcher(playerChatAlert, "Enter the message to trigger on...", "The message to trigger on. Supports glob (*)")
                    .build();
        } else if (alert instanceof NotificationFiredAlert) {
            NotificationFiredAlert notificationFiredAlert = (NotificationFiredAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                .addAlertDefaults(alert)
                .addRegexMatcher(notificationFiredAlert, "Enter the message to trigger on...", "The message to trigger on. Supports glob (*)")
                .build();
        } else if (alert instanceof StatChangedAlert) {
            StatChangedAlert statChangedAlert = (StatChangedAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                .addAlertDefaults(alert)
                .addSelect("Skill", "The skill to track", Skill.class, statChangedAlert.getSkill(), statChangedAlert::setSkill)
                .addSpinner("Changed Amount", "The difference in level to trigger the alert. Can be positive for boost and negative for drain", statChangedAlert.getChangedAmount(), statChangedAlert::setChangedAmount)
                .build();
        } else if (alert instanceof XPDropAlert) {
            XPDropAlert xpDropAlert = (XPDropAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                .addAlertDefaults(alert)
                .addSelect("Skill", "The skill to track", Skill.class, xpDropAlert.getSkill(), xpDropAlert::setSkill)
                .addSpinner("Gained Amount", "How much xp needed to trigger this alert", xpDropAlert.getGainedAmount(), xpDropAlert::setGainedAmount)
                .build();
        } else if (alert instanceof SpawnedAlert) {
            SpawnedAlert spawnedAlert = (SpawnedAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                .addAlertDefaults(alert)
                .addSelect("Spawned/Despawned", "Spawned or Despawned", SpawnedAlert.SpawnedDespawned.class, spawnedAlert.getSpawnedDespawned(), spawnedAlert::setSpawnedDespawned)
                .addSelect("Type", "The type of object to trigger on", SpawnedAlert.SpawnedType.class, spawnedAlert.getSpawnedType(), spawnedAlert::setSpawnedType)
                .addRegexMatcher(spawnedAlert, "Enter the object to trigger on...", "The name to trigger on. Supports glob (*)")
                .build();
        } else if (alert instanceof InventoryAlert) {
            InventoryAlert inventoryAlert = (InventoryAlert) alert;
            return AlertPanel.create(this.muxer, alert)
                .addAlertDefaults(alert)
                .addSelect("Type", "Type", InventoryAlert.InventoryAlertType.class, inventoryAlert.getInventoryAlertType(), (val) -> {
                    inventoryAlert.setInventoryAlertType(val);
                    this.muxer.popState();
                    this.openAlert(alert);
                })
                .addIf(
                    panel -> panel.addRegexMatcher(inventoryAlert, "Enter the name of the item to trigger on...", "The name to trigger on. Supports glob (*)")
                        .addSpinner("Quantity", "The quantity of item to trigger on, use 0 for every time", inventoryAlert.getItemQuantity(), inventoryAlert::setItemQuantity),
                    () -> inventoryAlert.getInventoryAlertType() == InventoryAlert.InventoryAlertType.ITEM
                )
                .build();
        }

        return null;
    }

    @Override
    public void onActivate() {
        this.rebuild();
    }

    private void filter(String text) {
        this.filterText = text;
        this.dragAndDropReorderPane.removeAll();
        this.alertListItems.stream()
            .filter(alertListItem -> Text.matchesSearchTerms(SPLITTER.split(this.filterText.toUpperCase()), alertListItem.getAlert().getKeywords()))
            .forEach(this.dragAndDropReorderPane::add);
        this.revalidate();
    }
}
