package org.jabref.gui.preferences;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import org.jabref.gui.icon.IconTheme;
import org.jabref.gui.util.BindingsHelper;
import org.jabref.gui.util.FieldsUtil;
import org.jabref.gui.util.IconValidationDecorator;
import org.jabref.gui.util.ValueTableCellFactory;
import org.jabref.gui.util.ViewModelListCellFactory;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.entry.field.Field;
import org.jabref.model.entry.field.FieldFactory;
import org.jabref.preferences.JabRefPreferences;

import com.airhacks.afterburner.views.ViewLoader;
import de.saxsys.mvvmfx.utils.validation.visualization.ControlsFxVisualizer;

public class XmpPrivacyTabView extends AbstractPreferenceTabView implements PreferencesTab {

    @FXML private CheckBox enableXmpFilter;
    @FXML private TableView<Field> filterList;
    @FXML private TableColumn<Field, Field> fieldColumn;
    @FXML private TableColumn<Field, Field> actionsColumn;
    @FXML private ComboBox<Field> addFieldName;
    @FXML private Button addField;

    private ControlsFxVisualizer validationVisualizer = new ControlsFxVisualizer();

    public XmpPrivacyTabView(JabRefPreferences preferences) {
        this.preferences = preferences;

        ViewLoader.view(this)
                .root(this)
                .load();
    }

    @Override
    public String getTabName() { return Localization.lang("XMP-metadata"); }

    public void initialize () {
        XmpPrivacyTabViewModel xmpPrivacyTabViewModel = new XmpPrivacyTabViewModel(dialogService, preferences);
        this.viewModel = xmpPrivacyTabViewModel;

        enableXmpFilter.selectedProperty().bindBidirectional(xmpPrivacyTabViewModel.xmpFilterEnabledProperty());
        filterList.disableProperty().bind(xmpPrivacyTabViewModel.xmpFilterEnabledProperty().not());
        addFieldName.disableProperty().bind(xmpPrivacyTabViewModel.xmpFilterEnabledProperty().not());
        addField.disableProperty().bind(xmpPrivacyTabViewModel.xmpFilterEnabledProperty().not());

        fieldColumn.setSortable(true);
        fieldColumn.setReorderable(false);
        fieldColumn.setCellValueFactory(cellData -> BindingsHelper.constantOf(cellData.getValue()));
        new ValueTableCellFactory<Field, Field>()
                .withText(FieldsUtil::getNameWithType)
                .install(fieldColumn);

        actionsColumn.setSortable(false);
        actionsColumn.setReorderable(false);
        actionsColumn.setCellValueFactory(cellData -> BindingsHelper.constantOf(cellData.getValue()));
        new ValueTableCellFactory<Field, Field>()
                .withGraphic(item -> IconTheme.JabRefIcons.DELETE_ENTRY.getGraphicNode())
                .withTooltip(item -> Localization.lang("Remove") + " " + item.getName())
                .withOnMouseClickedEvent(item -> evt -> {
                    xmpPrivacyTabViewModel.removeFilter(filterList.getFocusModel().getFocusedItem());
                })
                .install(actionsColumn);

        filterList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                xmpPrivacyTabViewModel.removeFilter(filterList.getSelectionModel().getSelectedItem());
            }
        });

        filterList.itemsProperty().bind(xmpPrivacyTabViewModel.filterListProperty());

        addFieldName.setEditable(true);
        new ViewModelListCellFactory<Field>()
                .withText(FieldsUtil::getNameWithType)
                .install(addFieldName);
        addFieldName.itemsProperty().bind(xmpPrivacyTabViewModel.availableFieldsProperty());
        addFieldName.valueProperty().bindBidirectional(xmpPrivacyTabViewModel.addFieldNameProperty());
        addFieldName.setConverter(new StringConverter<>() {
            @Override
            public String toString(Field object) {
                if (object != null) {
                    return object.getDisplayName();
                } else {
                    return "";
                }
            }

            @Override
            public Field fromString(String string) {
                return FieldFactory.parseField(string);
            }
        });

        validationVisualizer.setDecoration(new IconValidationDecorator());
        Platform.runLater(() -> validationVisualizer.initVisualization(xmpPrivacyTabViewModel.xmpFilterListValidationStatus(), filterList));
    }

    public void addField() { ((XmpPrivacyTabViewModel) viewModel).addField(); }
}