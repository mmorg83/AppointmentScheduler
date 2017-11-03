/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mmorg29.appointmentscheduler;

import com.mmorg29.agenda.DailyAgenda;
import com.mmorg29.agenda.MonthlyAgenda;
import com.mmorg29.agenda.InvalidAgendaException;
import com.mmorg29.agenda.WeeklyAgenda;
import com.mmorg29.dbtools.Address;
import com.mmorg29.dbtools.AddressBuilder;
import com.mmorg29.dbtools.City;
import com.mmorg29.dbtools.CityBuilder;
import com.mmorg29.dbtools.Country;
import com.mmorg29.dbtools.CountryBuilder;
import com.mmorg29.dbtools.Customer;
import com.mmorg29.dbtools.CustomerBuilder;
import com.mmorg29.dbtools.DBManager;
import com.mmorg29.dbtools.InvalidDBOperationException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller class
 *
 * @author mmorg
 */
public class MainFormController implements Initializable {

    @FXML
    private Label week_identifier_lbl;
    @FXML
    private ComboBox<String> month_selector_combo_box;
    @FXML
    private Spinner<Integer> year_selector_spinner;
    @FXML
    private Button add_appointment_btn;
    @FXML
    private GridPane month_view_grid;
    @FXML
    private GridPane week_view_grid;
    @FXML
    private GridPane week_expander_grid;
    @FXML
    private ImageView week_collapse_btn;
    @FXML
    private ImageView add_new_customer_btn;
    @FXML
    private ListView<String> customers_list_view;
    @FXML
    private TextField customer_name_input;
    @FXML
    private TextField customer_street_address_input_1;
    @FXML
    private TextField customer_street_address_input_2;
    @FXML
    private ComboBox<String> customer_city_selector;
    @FXML
    private TextField customer_zip_code_display;
    @FXML
    private ComboBox<String> customer_country_selector;
    @FXML
    private ComboBox<String> customer_status_selector;
    @FXML
    private TextField customer_phone_number_input;
    @FXML
    private Button customer_save_btn;
    @FXML
    private Button customer_cancel_btn;
    @FXML
    private Group customer_info_group;
    @FXML
    private TabPane main_form_tab_pane;

    private final ObservableList<String> months
            = FXCollections.observableArrayList(
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
            );

    private MonthlyAgenda monthlyAgenda;
    private WeeklyAgenda selectedWeekAgenda;
    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, City> cities = new HashMap<>();
    private final Map<String, Country> countries = new HashMap<>();
    private Customer selectedCustomer;

    //mam Validates that a required form field is completed
    private final Predicate<String> NOT_VALID = (input) -> input == null || input.isEmpty();
    
    //mam Converts the status string to a boolean
    private final Predicate<String> STATUS = (input) -> input.equals("Active");

    //mam Validates that all required customer information fields are complete.  Capitalizes the info in the fields.
    private final BooleanSupplier CUSTOMER_FORM_IS_VALID = () -> {
        boolean isValid = true;
        if (NOT_VALID.test(this.customer_name_input.getText())) {
            isValid = false;
        } else {
            this.customer_name_input.setText(capitalize(this.customer_name_input.getText()));
        }

        if (NOT_VALID.test(this.customer_street_address_input_1.getText())) {
            isValid = false;
        } else {
            this.customer_street_address_input_1.setText(capitalize(this.customer_street_address_input_1.getText()));
        }

        if (!NOT_VALID.test(this.customer_street_address_input_2.getText())) {
            this.customer_street_address_input_2.setText(capitalize(this.customer_street_address_input_2.getText()));
        }

        if (NOT_VALID.test(this.customer_city_selector.getValue())) {
            isValid = false;
        } else {
            this.customer_city_selector.getEditor().setText(capitalize(this.customer_city_selector.getValue()));
        }

        if (NOT_VALID.test(this.customer_country_selector.getValue())) {
            isValid = false;
        } else {
            this.customer_country_selector.getEditor().setText(capitalize(this.customer_country_selector.getValue()));
        }

        if (NOT_VALID.test(this.customer_status_selector.getValue())) {
            isValid = false;
        }

        if (NOT_VALID.test(this.customer_phone_number_input.getText())) {
            isValid = false;
        } else {
            if (!this.customer_phone_number_input.getText().matches("^[+]?([0-9]*[\\.\\s\\-\\(\\)]|[0-9]+){3,24}$")) {
                isValid = false;
            }
        }

        if (NOT_VALID.test(this.customer_zip_code_display.getText())) {
            isValid = false;
        }
        return isValid;
    };

    //mam Dislplays an alert if an exception occurs that the application can not or should not continue after
    //logs the user out.
    private final Consumer<String> FATAL_ERROR = (message) -> {
        String addOnMessage = " User will be logged out.";
        Alert alert = new Alert(AlertType.ERROR, message + addOnMessage, ButtonType.OK);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    alert.close();
                    Stage stage = (Stage) this.month_view_grid.getScene().getWindow();
                    stage.close();
                    User.getInstance().logout();
                });
    };

    /**
     * Initializes the controller class.
     */
    @Override

    public void initialize(URL url, ResourceBundle rb) {
        //mam start notifications executor service
        AppointmentScheduler.SERVICE.scheduleAtFixedRate(new Notifications(), 0, 1, TimeUnit.MINUTES);
        loadMonthYearSelectors();
        loadMonthView();
        loadCustomerData();
        loadCustomerCitySelector();
        loadCustomerCountrySelector();
        loadCustomerStatusSelector();
    }

    //mam starts the loading process for the newley selected month to view
    @FXML
    private void handleMonthSelectorOnSelection(ActionEvent event) {
        loadMonthView();
    }

    //mam launches a dialog to input new appointment info
    @FXML
    private void handleAddAppointmentAction(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddNewAppointmentDialog.fxml"));
        try {
            Parent root = loader.load();
            AddNewAppointmentDialogController addNewAppointmentController = loader.<AddNewAppointmentDialogController>getController();
            addNewAppointmentController.setMonthlyAgenda(this.monthlyAgenda);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("New Appointment");
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();
        } catch (IOException ioEx) {
            this.FATAL_ERROR.accept("Unable to find required resources.");
        }
    }

    //mam starts the process of showing a detailed schedule view
    @FXML
    private void handleDayCellClick(MouseEvent event) {
        VBox dayCell = (VBox) event.getSource();
        HBox hbox = (HBox) dayCell.getChildren().get(0);
        Label dateLabel = (Label) hbox.getChildren().get(0);
        ListView dailyAgendaView = (ListView) dayCell.getChildren().get(1);
        int dayNum = Integer.parseInt(dateLabel.getText());
        LocalDate day = LocalDate.of(this.year_selector_spinner.getValue(), Month.valueOf(this.month_selector_combo_box.getValue().toUpperCase()), dayNum);
        DailyAgenda dailyAgenda = this.monthlyAgenda.getDailyAgendaByDate(day);
        showDetailedAgendaView(dailyAgenda, dailyAgendaView.getSelectionModel().getSelectedIndex());
    }

    //mam starts the process of showing a detailed schedule view
    @FXML
    private void handleListViewClick(MouseEvent event) {
        ListView dailyAgendaView = (ListView) event.getSource();
        VBox dayCell = (VBox) dailyAgendaView.getParent();
        HBox hbox = (HBox) dayCell.getChildren().get(0);
        Label dateLabel = (Label) hbox.getChildren().get(0);
        int dayNum = Integer.parseInt(dateLabel.getText());
        LocalDate day = LocalDate.of(this.year_selector_spinner.getValue(), Month.valueOf(this.month_selector_combo_box.getValue().toUpperCase()), dayNum);
        DailyAgenda dailyAgenda = this.monthlyAgenda.getDailyAgendaByDate(day);
        showDetailedAgendaView(dailyAgenda, dailyAgendaView.getSelectionModel().getSelectedIndex());
    }

    //mam shows a dialog containing the detailed schedule info for a selected date
    private void showDetailedAgendaView(DailyAgenda dailyAgenda, int selectedIndex) {
        if (dailyAgenda != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DailyAgendaViewDialog.fxml"));
            try {
                Parent root = loader.load();
                DailyAgendaViewDialogController dailyAgendaViewController = loader.<DailyAgendaViewDialogController>getController();
                dailyAgendaViewController.setDateLabel(dailyAgenda.getDate());
                dailyAgendaViewController.setDailyAgenda(dailyAgenda);
                dailyAgendaViewController.setSelectedAppointment(selectedIndex);
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UTILITY);
                stage.setTitle("Daily Agenda");
                stage.setScene(scene);
                stage.sizeToScene();
                stage.show();
            } catch (IOException ioEx) {
                this.FATAL_ERROR.accept("Unable to find required resources.");
            }
        }
    }

    //mam shows the weekly schedule for the selected week and hides the monthyl schedul view
    @FXML
    private void handleWeekExpandClick(MouseEvent event) {
        this.month_view_grid.setVisible(false);
        this.week_view_grid.setVisible(true);
        this.month_selector_combo_box.setVisible(false);
        this.year_selector_spinner.setVisible(false);
        Node clickedExpander = (Node) event.getSource();
        ObservableList<Node> weekExpanderBtns = this.week_expander_grid.getChildren();
        weekExpanderBtns.forEach((weekExpanderBtn) -> {
            if (weekExpanderBtn.isVisible()) {
                weekExpanderBtn.setVisible(false);
            } else {
                weekExpanderBtn.setVisible(true);
            }
        });
        ObservableList<Node> weekViewCells = this.week_view_grid.getChildren();
        int weekNum = GridPane.getRowIndex(clickedExpander) - 1;
        WeeklyAgenda weeklyAgenda = this.monthlyAgenda.getWeeklyAgenda(weekNum);
        this.selectedWeekAgenda = weeklyAgenda;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        this.week_identifier_lbl.setText(weeklyAgenda.getWeekStart().format(dateFormatter) + " - " + weeklyAgenda.getWeekEnd().format(dateFormatter));
        this.week_identifier_lbl.setVisible(true);
        for (int i = 7; i < weekViewCells.size(); i++) {
            ListView dayAgendaView = (ListView) weekViewCells.get(i);
            try {
                DailyAgenda dailyAgenda = weeklyAgenda.getDailyAgendaByDate(weeklyAgenda.getWeekStart().plusDays(i - 7));
                dailyAgenda.setWeekDisplay(dayAgendaView);
                dayAgendaView.setItems(dailyAgenda.getDisplayableAgenda(false));
            } catch (InvalidAgendaException iaEx) {
                this.FATAL_ERROR.accept("There was an error retrieving the schedule.");
            }
        }
    }

    //mam launches the dialog to show detailed schedule info for the selectd date
    @FXML
    private void handleWeekListViewClick(MouseEvent event) {
        ListView<String> weekDayListView = (ListView) event.getSource();
        int dayNum = GridPane.getColumnIndex(weekDayListView);
        try {
            DailyAgenda dailyAgenda = this.selectedWeekAgenda.getDailyAgendaByDate(this.selectedWeekAgenda.getWeekStart().plusDays(dayNum));
            showDetailedAgendaView(dailyAgenda, weekDayListView.getSelectionModel().getSelectedIndex());
        } catch (InvalidAgendaException iaEx) {
            this.FATAL_ERROR.accept("There was an error retrieving the schedule.");
        }
    }

    //mam closes the weekly schedule view and displays the monthly view
    @FXML
    private void handleWeekCollapseClick(MouseEvent event) {
        this.month_view_grid.setVisible(true);
        this.week_view_grid.setVisible(false);
        this.month_selector_combo_box.setVisible(true);
        this.year_selector_spinner.setVisible(true);
        this.week_identifier_lbl.setVisible(false);
        ObservableList<Node> weekExpanderBtns = this.week_expander_grid.getChildren();
        weekExpanderBtns.forEach((weekExpanderBtn) -> {
            if (!weekExpanderBtn.isVisible()) {
                weekExpanderBtn.setVisible(true);
            } else {
                weekExpanderBtn.setVisible(false);
            }
        });
    }

    //mam starts the process of adding a new customer to the database
    @FXML
    private void handleAddNewCustomerClicked(MouseEvent event) {
        this.customer_cancel_btn.setDisable(false);
        this.customer_save_btn.setDisable(false);
        this.customers_list_view.setDisable(true);
        this.add_new_customer_btn.setDisable(true);
        this.customer_info_group.setDisable(false);
        ObservableList<Node> customer_info_inputs = this.customer_info_group.getChildren();
        customer_info_inputs.stream().map((node) -> {
            if (node instanceof TextField) {
                TextField textField = (TextField) node;
                textField.setEditable(true);
                textField.clear();
            }
            return node;
        }).filter((node) -> (node instanceof ComboBox)).map((node) -> (ComboBox) node).map((comboBox) -> {
            comboBox.setEditable(true);
            return comboBox;
        }).forEachOrdered((comboBox) -> {
            comboBox.getSelectionModel().clearSelection();
        });
        this.customer_status_selector.getEditor().setEditable(false);
        this.customer_save_btn.setText("Add");
        this.customer_name_input.requestFocus();
    }

    //mam starts the process of loading the customer info form
    @FXML
    private void handleCustomerClicked(MouseEvent event) {
        loadSelectedCustomerInfo();
        this.customer_save_btn.setDisable(false);
    }

    //mam starts the customer edit process or starts the customer save process for edited customer info or starts the add new customer process
    @FXML
    private void handleCustomerSaveBtnAction(ActionEvent event) {
        this.customer_cancel_btn.setDisable(false);
        try {
            String mode = this.customer_save_btn.getText();
            switch (mode) {
                case "Edit":
                    startCustomerEdit();
                    break;
                case "Save":
                    saveCustomerUpdates();
                    break;
                case "Add":
                    addNewCustomer();
                    break;
            }
        } catch (InvalidCustomerInfoException iciEx) {
            Alert alert = new Alert(AlertType.ERROR, iciEx.getMessage(), ButtonType.OK);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> alert.close());
        }
    }

    //mam cancels the edit or add customer operation
    @FXML
    private void handleCustomerCancleBtnAction(ActionEvent event) {
        resetCustomerForm();
        loadSelectedCustomerInfo();
    }

    //mam displays the monthly appointment types report in a new tab
    @FXML
    private void handleMenuReportAppointmentTypesOnAction(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AppointmentTypesByMonthReport.fxml"));
        try {
            VBox root = loader.load();
            Tab appointmentTypesReportTab = new Tab("Report - Appointment Types", root);
            AppointmentTypesByMonthReportController controller = loader.<AppointmentTypesByMonthReportController>getController();
            controller.setParentTab(appointmentTypesReportTab);
            this.main_form_tab_pane.getTabs().add(appointmentTypesReportTab);
            root.autosize();
            this.main_form_tab_pane.getSelectionModel().select(appointmentTypesReportTab);
        } catch (IOException ioEx) {
            this.FATAL_ERROR.accept("Unable to find required resources.");
        }
    }

    //mam displays the consultant schedules report in a new tab
    @FXML
    private void handleMenuReportsUserSchedulesOnAction(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ConsultantScheduleReport.fxml"));
        try {
            VBox root = loader.load();
            Tab consultantSchedulesReportTab = new Tab("Report - Consultant Schedules", root);
            ConsultantScheduleReportController controller = loader.<ConsultantScheduleReportController>getController();
            controller.setParentTab(consultantSchedulesReportTab);
            this.main_form_tab_pane.getTabs().add(consultantSchedulesReportTab);
            root.autosize();
            this.main_form_tab_pane.getSelectionModel().select(consultantSchedulesReportTab);
        } catch (IOException ioEx) {
            this.FATAL_ERROR.accept("Unable to find required resources.");
        }
    }

    //mam displays the customer history report in a new tab
    @FXML
    private void handleMenuReportCustomerActivityOnAction(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerHistoryReport.fxml"));
        try {
            VBox root = loader.load();
            Tab customerHistoryReportTab = new Tab("Report - Customer History", root);
            CustomerHistoryReportController controller = loader.<CustomerHistoryReportController>getController();
            controller.setParentTab(customerHistoryReportTab);
            this.main_form_tab_pane.getTabs().add(customerHistoryReportTab);
            root.autosize();
            this.main_form_tab_pane.getSelectionModel().select(customerHistoryReportTab);
        } catch (IOException ioEx) {
            this.FATAL_ERROR.accept("Unable to find required resources.");
        }
    }

    //mam logs the user out
    @FXML
    private void handleMenuLogoutOnAction(ActionEvent event) {
        Stage stage = (Stage) this.month_view_grid.getScene().getWindow();
        stage.close();
        User.getInstance().logout();
    }

    //mam closes the application
    @FXML
    private void handleMenuCloseOnAction(ActionEvent event) {
        Platform.exit();
    }

    //mam Populates the customer info form with the info for the selected customer
    private void loadSelectedCustomerInfo() {
        Customer customer = this.customers.get(this.customers_list_view.getSelectionModel().getSelectedItem());
        if (customer != null) {
            this.selectedCustomer = customer;
            Address address = customer.ADDRESS.get();
            City city = address.CITY.get();
            Country country = city.COUNTRY.get();
            this.customer_name_input.setText(customer.getCustomerName());
            this.customer_street_address_input_1.setText(address.getAddress());
            this.customer_street_address_input_2.setText(address.getAddress2());
            this.customer_phone_number_input.setText(address.getPhoneNumber());
            this.customer_zip_code_display.setText(address.getPostalCode());
            this.customer_city_selector.getSelectionModel().select(city.getCityName());
            this.customer_country_selector.getSelectionModel().select(country.getCountryName());
            if (customer.getStatus()) {
                this.customer_status_selector.getSelectionModel().select("Active");
            } else {
                this.customer_status_selector.getSelectionModel().select("Not Active");
            }
        }
    }
    
    //mam loads the month and year selctors
    private void loadMonthYearSelectors() {
        month_selector_combo_box.setItems(months);
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue() - 1;
        int year = now.getYear();
        month_selector_combo_box.getSelectionModel().select(month);
        IntegerSpinnerValueFactory spinnerIntegerFactory = new IntegerSpinnerValueFactory(2010, 2050, year);
        spinnerIntegerFactory.valueProperty().addListener((obs, oldValue, newValue) -> {
            loadMonthView();
        });
        this.year_selector_spinner.valueFactoryProperty().set(spinnerIntegerFactory);
    }

    //mam populates the schedule data in the monthly view
    private void loadMonthView() {
        this.monthlyAgenda = new MonthlyAgenda(this.month_selector_combo_box.getValue(), this.year_selector_spinner.getValue());
        try {
            this.monthlyAgenda.loadMonthlySchedule(User.getInstance().getUserId() + "");
        } catch (InvalidAgendaException iaEx) {
            this.FATAL_ERROR.accept("There is an issue loading the schedule");
        }
        ObservableList<Node> monthViewCells = this.month_view_grid.getChildren();
        for (int i = 7; i < monthViewCells.size(); i++) {
            VBox monthViewCell = (VBox) monthViewCells.get(i);
            HBox monthViewCellLabelArea = (HBox) monthViewCell.getChildren().get(0);
            Label monthViewCellDateLabel = (Label) monthViewCellLabelArea.getChildren().get(0);
            LocalDate day = this.monthlyAgenda.getMonthStart().plusDays(i - 7);
            int dayNumber = day.getDayOfMonth();
            monthViewCellDateLabel.setText(dayNumber + "");
            ListView monthViewCellAgenda = (ListView) monthViewCell.getChildren().get(1);
            LocalDate firstOfMonth = LocalDate.of(this.year_selector_spinner.getValue(), Month.valueOf(this.month_selector_combo_box.getValue().toUpperCase()), 1);
            LocalDate endOfMonth = firstOfMonth.with(TemporalAdjusters.lastDayOfMonth());
            if (day.isBefore(firstOfMonth) || day.isAfter(endOfMonth)) {
                monthViewCell.setDisable(true);
            } else {
                monthViewCell.setDisable(false);
            }
            DailyAgenda dailyAgenda = this.monthlyAgenda.getDailyAgendaByDate(day);
            if (dailyAgenda != null) {
                dailyAgenda.setMonthDislplay(monthViewCellAgenda);
                if (!monthViewCellAgenda.isDisabled()) {
                    monthViewCellAgenda.setItems(dailyAgenda.getDisplayableAgenda(false));
                }
            }
        }
    }

    //mam loads the customers HashMap with Customer objects created from the customers available in the database.
    private void loadCustomerData() {
        this.customers.clear();
        this.customers_list_view.getItems().clear();
        String sql = "SELECT * FROM " + DBManager.CUSTOMER_TABLE + " ORDER BY customerName ASC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                CustomerBuilder customerBuilder = new CustomerBuilder();
                customerBuilder.setCustomerId(resultSet.getInt(1));
                customerBuilder.setCustomerName(resultSet.getString(2));
                customerBuilder.setAddress(resultSet.getInt(3));
                customerBuilder.setStatus(resultSet.getBoolean(4));
                customerBuilder.setCreateDate(resultSet.getTimestamp(5));
                customerBuilder.setCreatedBy(resultSet.getString(6));
                customerBuilder.setLastUpdate(resultSet.getTimestamp(7));
                customerBuilder.setLastUpdatedBy(resultSet.getString(8));
                Customer customer = customerBuilder.build();
                this.customers.put(customer.getCustomerName(), customer);
            }
            ObservableList sortedCustomers = FXCollections.observableArrayList(this.customers.keySet()).sorted();
            this.customers_list_view.setItems(sortedCustomers);
        } catch (SQLException sqlEx) {
            this.FATAL_ERROR.accept("There is an issue communicating with the database.");
        }
    }

    //mam loads the the customer city selector with the available cities.
    private void loadCustomerCitySelector() {
        this.cities.clear();
        this.customer_city_selector.getItems().clear();
        String sql = "SELECT * FROM " + DBManager.CITY_TABLE + " ORDER BY city ASC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                CityBuilder cityBuilder = new CityBuilder();
                cityBuilder.setCityId(resultSet.getInt(1));
                cityBuilder.setCityName(resultSet.getString(2));
                cityBuilder.setCountry(resultSet.getInt(3));
                cityBuilder.setCreateDate(resultSet.getTimestamp(4));
                cityBuilder.setCreatedBy(resultSet.getString(5));
                cityBuilder.setLastUpdate(resultSet.getTimestamp(6));
                cityBuilder.setLastUpdatedBy(resultSet.getString(7));
                this.cities.put(resultSet.getString(2), cityBuilder.build());
            }
            ObservableList sortedCities = FXCollections.observableArrayList(this.cities.keySet()).sorted();
            this.customer_city_selector.setItems(sortedCities);
        } catch (SQLException sqlEx) {
            this.FATAL_ERROR.accept("There is an issue communicating with the database.");
        }
    }

    //mam loads the customer country selectory with the available countries.
    private void loadCustomerCountrySelector() {
        this.countries.clear();
        this.customer_country_selector.getItems().clear();
        String sql = "SELECT * FROM " + DBManager.COUNTRY_TABLE + " ORDER BY country ASC";
        try (DBManager dbManager = new DBManager(); Statement statement = dbManager.getStatement(); ResultSet resultSet = statement.executeQuery(sql);) {
            while (resultSet.next()) {
                CountryBuilder countryBuilder = new CountryBuilder();
                countryBuilder.setCountryId(resultSet.getInt(1));
                countryBuilder.setCountryName(resultSet.getString(2));
                countryBuilder.setCreateDate(resultSet.getTimestamp(3));
                countryBuilder.setCreatedBy(resultSet.getString(4));
                countryBuilder.setLastUpdate(resultSet.getTimestamp(5));
                countryBuilder.setLastUpdatedBy(resultSet.getString(6));
                this.countries.put(resultSet.getString(2), countryBuilder.build());
            }
            ObservableList sortedCountries = FXCollections.observableArrayList(this.countries.keySet()).sorted();
            this.customer_country_selector.setItems(sortedCountries);
        } catch (SQLException sqlEx) {
            this.FATAL_ERROR.accept("There is an issue communicating with the database.");
        }
    }

    //mam loads the customer status selector.
    private void loadCustomerStatusSelector() {
        this.customer_status_selector.getItems().add("Active");
        this.customer_status_selector.getItems().add("Not Active");
    }

    //mam starts the editing process of the selected customer's info.
    private void startCustomerEdit() {
        this.customers_list_view.setDisable(true);
        this.add_new_customer_btn.setDisable(true);
        this.customer_info_group.setDisable(false);
        ObservableList<Node> customerInfoInputs = this.customer_info_group.getChildren();
        customerInfoInputs.stream().filter((node) -> (node instanceof TextField)).forEachOrdered((node) -> {
            ((TextField) node).setEditable(true);
        });
        this.customer_status_selector.getEditor().setEditable(false);
        this.customer_save_btn.setText("Save");
        this.customer_name_input.requestFocus();
    }

    //mam resets the customer info form.
    private void resetCustomerForm() {
        this.customers_list_view.setDisable(false);
        this.add_new_customer_btn.setDisable(false);
        this.customer_info_group.setDisable(true);
        this.customer_cancel_btn.setDisable(true);
        this.customer_save_btn.setText("Edit");
        if (this.selectedCustomer == null) {
            this.customer_save_btn.setDisable(true);
        }
    }

    /**
     * mam
     * Adds a new Country to the database or retrieves the the selected Country.
     * @return A Country object containing the selected or created country info.
     * @throws InvalidCustomerInfoException when a required operation fails.
     */
    private Country getCustomerCountry() throws InvalidCustomerInfoException {
        Country country = this.countries.get(this.customer_country_selector.getValue());
        if (country == null) {
            CountryBuilder countryBuilder = new CountryBuilder();
            int nextId = Country.NEXT_ID.apply(DBManager.COUNTRY_TABLE);
            if(nextId == -1) {
                throw new InvalidCustomerInfoException("Unable to create new Country.");
            }
            countryBuilder.setCountryId(nextId);
            countryBuilder.setCountryName(this.customer_country_selector.getValue());
            country = countryBuilder.build();
            try {
                if (country.writeToDB()) {
                    this.countries.put(country.getCountryName(), country);
                    ObservableList sortedCountries = FXCollections.observableArrayList(this.countries.keySet()).sorted();
                    this.customer_country_selector.setItems(sortedCountries);
                }
            } catch (InvalidDBOperationException idboEx) {
                throw new InvalidCustomerInfoException(idboEx.getMessage());
            }
        }
        return country;
    }

    /**
     * mam
     * Adds a new city to the database or retrieves the selected city.
     * @param countryId the id of the country this city is in
     * @return A City object containing the selected or created city info.
     * @throws InvalidCustomerInfoException when a required operation fails.
     */
    private City getCustomerCity(int countryId) throws InvalidCustomerInfoException {
        City city = this.cities.get(this.customer_city_selector.getValue());
        if (city == null) {
            CityBuilder cityBuilder = new CityBuilder();
            int nextId = City.NEXT_ID.apply(DBManager.CITY_TABLE);
            if(nextId == -1) {
                throw new InvalidCustomerInfoException("Unable to create new City.");
            }
            cityBuilder.setCityId(nextId);
            cityBuilder.setCityName(this.customer_city_selector.getValue());
            cityBuilder.setCountry(countryId);
            city = cityBuilder.build();
            try {
                if (city.writeToDB()) {
                    this.cities.put(city.getCityName(), city);
                    ObservableList sortedCities = FXCollections.observableArrayList(this.cities.keySet()).sorted();
                    this.customer_city_selector.setItems(sortedCities);
                }
            } catch (InvalidDBOperationException idboEx) {
                throw new InvalidCustomerInfoException(idboEx.getMessage());
            }
        }
        return city;
    }
    
    /**
     * mam
     * Updates the info for the selected customer.
     * @throws InvalidCustomerInfoException 
     */
    private void saveCustomerUpdates() throws InvalidCustomerInfoException {
        if (this.CUSTOMER_FORM_IS_VALID.getAsBoolean()) {
            Address address = this.selectedCustomer.ADDRESS.get();
            Country country = getCustomerCountry();
            City city = getCustomerCity(country.getId());
            address.setCityId(city.getId());
            address.setAddress(this.customer_street_address_input_1.getText());
            address.setAddress2(this.customer_street_address_input_2.getText());
            address.setPhoneNumber(this.customer_phone_number_input.getText());
            address.setPostalCode(this.customer_zip_code_display.getText());
            try {
                if (address.updateInDB()) {
                    this.selectedCustomer.setCustomerName(this.customer_name_input.getText());
                    this.selectedCustomer.setStatus(this.STATUS.test(this.customer_status_selector.getValue()));
                    if (this.selectedCustomer.updateInDB()) {
                        this.loadMonthView();
                        ObservableList sortedCustomers = FXCollections.observableArrayList(this.customers.keySet()).sorted();
                        this.customers_list_view.setItems(sortedCustomers);
                        this.customers_list_view.getSelectionModel().select(this.selectedCustomer.getCustomerName());
                    }
                }
            } catch (InvalidDBOperationException idboEx) {
                throw new InvalidCustomerInfoException(idboEx.getMessage());
            }
            resetCustomerForm();
        } else {
            throw new InvalidCustomerInfoException("Required customer data is missing or incorrect.");
        }
    }

    /**
     * mam
     * Adds a new customer and address to the database.
     * @throws InvalidCustomerInfoException when there is invalid customer information or one of the required operations fails.
     */
    private void addNewCustomer() throws InvalidCustomerInfoException {
        if (this.CUSTOMER_FORM_IS_VALID.getAsBoolean()) {
            Country country = getCustomerCountry();
            City city = getCustomerCity(country.getId());
            AddressBuilder addressBuilder = new AddressBuilder();
            int nextId = Address.NEXT_ID.apply(DBManager.ADDRESS_TABLE);
            if(nextId == -1) {
                throw new InvalidCustomerInfoException("Unable to add new Address.");
            }
            addressBuilder.setAddressId(nextId);
            addressBuilder.setCityId(city.getId());
            addressBuilder.setAddress(this.customer_street_address_input_1.getText());
            addressBuilder.setAddress2(this.customer_street_address_input_2.getText());
            addressBuilder.setPhoneNumber(this.customer_phone_number_input.getText());
            addressBuilder.setPostalCode(this.customer_zip_code_display.getText());
            Address address = addressBuilder.build();
            try {
                address.writeToDB();
            } catch (InvalidDBOperationException idboEx) {
                throw new InvalidCustomerInfoException(idboEx.getMessage());
            }
            CustomerBuilder customerBuilder = new CustomerBuilder();
            nextId = Customer.NEXT_ID.apply(DBManager.CUSTOMER_TABLE);
            if(nextId == -1) {
                throw new InvalidCustomerInfoException("Unable to add new Customer.");
            }
            customerBuilder.setCustomerId(nextId);
            customerBuilder.setCustomerName(this.customer_name_input.getText());
            customerBuilder.setAddress(address.getId());
            customerBuilder.setStatus(this.STATUS.test(this.customer_status_selector.getValue()));
            Customer customer = customerBuilder.build();
            try {
                if (customer.writeToDB()) {
                    this.loadMonthView();
                    this.customers.put(customer.getCustomerName(), customer);
                    ObservableList sortedCustomers = FXCollections.observableArrayList(this.customers.keySet()).sorted();
                    this.customers_list_view.setItems(sortedCustomers);
                    this.customers_list_view.getSelectionModel().select(customer.getCustomerName());
                } else {
                    address.deleteFromDB();
                }
            } catch (InvalidDBOperationException idboEx) {
                throw new InvalidCustomerInfoException(idboEx.getMessage());
            }
            resetCustomerForm();
        } else {
            throw new InvalidCustomerInfoException("Required customer data is missing or incorrect.");
        }

    }

    /**
     * mam 
     * Capitalizes the first letter of the provided string
     * @param value the string to capitalize
     * @return the Capitalized string
     */
    public static String capitalize(String value) {
        String[] stringParts = value.split("\\s+");
        StringBuilder capitalizedStringBuilder = new StringBuilder();
        for (String part : stringParts) {
            capitalizedStringBuilder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1)).append(' ');
        }
        return capitalizedStringBuilder.toString().trim();
    }

}
