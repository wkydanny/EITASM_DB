package gui;

import dao.EmployeeDAO;
import dao.SecurityDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DashboardApplication extends Application {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final SecurityDAO securityDAO = new SecurityDAO();

    private final ObservableList<String[]> masterEmpSummary = FXCollections.observableArrayList();
    private final ObservableList<String[]> masterAssetSummary = FXCollections.observableArrayList();

    private final String ICON_PATH = "C:\\EITASM\\AppInJava\\icon\\EITASM-icon.png";
    private Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;

        // Set application icon from absolute path
        try {
            FileInputStream input = new FileInputStream(ICON_PATH);
            primaryStage.getIcons().add(new Image(input));
        } catch (FileNotFoundException e) {
            System.err.println("Icon file not found: " + ICON_PATH);
        }

        showLoginGateway();
    }

    // =========================================================================
    // SYSTEM GATEWAY: STARTUP AUTHENTICATION PAGE
    // =========================================================================
    private void showLoginGateway() {
        mainStage.setTitle("EITASM - System Authentication Control Gateway");

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f4f6f9;");

        Label lblTitle = new Label("EITASM ADMINISTRATIVE GATEWAY");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(12);
        loginGrid.setAlignment(Pos.CENTER);

        TextField txtGatewayUser = new TextField();
        txtGatewayUser.setPromptText("Enter admin username...");
        PasswordField txtGatewayPass = new PasswordField();
        txtGatewayPass.setPromptText("Enter admin password...");

        loginGrid.add(new Label("Username:"), 0, 0);
        loginGrid.add(txtGatewayUser, 1, 0);
        loginGrid.add(new Label("Password:"), 0, 1);
        loginGrid.add(txtGatewayPass, 1, 1);

        Button btnLogin = new Button("Authorize Launch");
        btnLogin.setPrefWidth(150);
        btnLogin.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold;");

        // CHANGE 1: Enable default button option so pressing "Enter" key submits authentication automatically
        btnLogin.setDefaultButton(true);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(lblTitle, loginGrid, btnLogin, lblError);

        Scene loginScene = new Scene(root, 450, 280);
        mainStage.setScene(loginScene);
        mainStage.show();

        // Execution path sequence processing event
        btnLogin.setOnAction(e -> {
            String user = txtGatewayUser.getText().trim();
            String pass = txtGatewayPass.getText();

            if ("dbadmin".equals(user) && "Vanier1234".equals(pass)) {
                buildAndShowMainApplication();
            } else {
                lblError.setText("Invalid credentials. System entry denied.");
                txtGatewayPass.clear();
            }
        });
    }

    // Builds the primary environment structure post-authentication
    private void buildAndShowMainApplication() {
        mainStage.setTitle("EITASM - Enterprise Control Console Platform");

        TabPane mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // CHANGE 2 & 3 & 4 & 5: Setup explicit multi-tab panel alignment mapping
        Tab listTab = createListAllEmployeesTab();
        Tab employeeDetailTab = createEmployeeDetailTab();
        Tab assetTab = createAssetTab();
        Tab securityAuditTab = createSecurityAuditTab(); // Added merged tab

        mainTabPane.getTabs().addAll(listTab, employeeDetailTab, assetTab, securityAuditTab, createAboutTab());

        Scene scene = new Scene(mainTabPane, 1150, 750);
        mainStage.setScene(scene);
        mainStage.show();

        // Hydrate data streams
        refreshDataCache();
    }

    private void refreshDataCache() {
        Thread.startVirtualThread(() -> {
            try {
                List<String[]> emps = employeeDAO.getAllEmployeesSummary();
                List<String[]> assets = securityDAO.getAllAssets();
                Platform.runLater(() -> {
                    masterEmpSummary.setAll(emps);
                    masterAssetSummary.setAll(assets);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showErrorAlert("Cache Refresh Failure", "Database offline or session expired.", e.getMessage()));
            }
        });
    }

    // =========================================================================
    // TAB 1: LIST OF ALL EMPLOYEES (Live Simultaneous Typing Filters)
    // =========================================================================
    private Tab createListAllEmployeesTab() {
        Tab tab = new Tab("List of all Employees");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        GridPane filters = new GridPane();
        filters.setHgap(10); filters.setVgap(10);

        TextField txtSearchId = new TextField(); txtSearchId.setPromptText("Search ID...");
        TextField txtSearchFirst = new TextField(); txtSearchFirst.setPromptText("Search First Name...");
        TextField txtSearchLast = new TextField(); txtSearchLast.setPromptText("Search Last Name...");
        TextField txtSearchPhone = new TextField(); txtSearchPhone.setPromptText("Search Phone...");

        ComboBox<String> cbSearchDept = new ComboBox<>(FXCollections.observableArrayList("All", "Human Resources", "Information Technology", "Cyber Security", "Finance"));
        cbSearchDept.getSelectionModel().selectFirst();

        filters.add(new Label("ID:"), 0, 0); filters.add(txtSearchId, 1, 0);
        filters.add(new Label("First Name:"), 2, 0); filters.add(txtSearchFirst, 3, 0);
        filters.add(new Label("Last Name:"), 4, 0); filters.add(txtSearchLast, 5, 0);
        filters.add(new Label("Phone:"), 0, 1); filters.add(txtSearchPhone, 1, 1);
        filters.add(new Label("Department:"), 2, 1); filters.add(cbSearchDept, 3, 1);

        TableView<String[]> table = new TableView<>();
        TableColumn<String[], String> colId = new TableColumn<>("Employee ID"); colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        TableColumn<String[], String> colFirst = new TableColumn<>("First Name"); colFirst.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        TableColumn<String[], String> colLast = new TableColumn<>("Last Name"); colLast.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        TableColumn<String[], String> colPhone = new TableColumn<>("Phone Number"); colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        TableColumn<String[], String> colEmail = new TableColumn<>("Email Address"); colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        TableColumn<String[], String> colDept = new TableColumn<>("Department"); colDept.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5]));

        table.getColumns().addAll(colId, colFirst, colLast, colPhone, colEmail, colDept);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        FilteredList<String[]> filteredList = new FilteredList<>(masterEmpSummary, p -> true);
        Runnable runFiltering = () -> {
            filteredList.setPredicate(emp -> {
                String idKey = txtSearchId.getText().trim().toLowerCase();
                String firstKey = txtSearchFirst.getText().trim().toLowerCase();
                String lastKey = txtSearchLast.getText().trim().toLowerCase();
                String phoneKey = txtSearchPhone.getText().trim().toLowerCase();
                String deptKey = cbSearchDept.getValue();

                if (!idKey.isEmpty() && !emp[0].toLowerCase().contains(idKey)) return false;
                if (!firstKey.isEmpty() && !emp[1].toLowerCase().startsWith(firstKey)) return false;
                if (!lastKey.isEmpty() && !emp[2].toLowerCase().contains(lastKey)) return false;
                if (!phoneKey.isEmpty() && !emp[3].toLowerCase().contains(phoneKey)) return false;
                if (deptKey != null && !deptKey.equals("All") && !emp[5].equalsIgnoreCase(deptKey)) return false;

                return true;
            });
        };

        txtSearchId.textProperty().addListener((o, old, n) -> runFiltering.run());
        txtSearchFirst.textProperty().addListener((o, old, n) -> runFiltering.run());
        txtSearchLast.textProperty().addListener((o, old, n) -> runFiltering.run());
        txtSearchPhone.textProperty().addListener((o, old, n) -> runFiltering.run());
        cbSearchDept.valueProperty().addListener((o, old, n) -> runFiltering.run());

        VBox.setVgrow(table, Priority.ALWAYS);

        table.setItems(filteredList);
        layout.getChildren().addAll(filters, new Separator(), table);
        tab.setContent(layout);
        return tab;
    }

    // =========================================================================
    // TAB 2: EMPLOYEE (Full Individual CRUD Profiler Matrix)
    // =========================================================================
    private Tab createEmployeeDetailTab() {
        Tab tab = new Tab("Employee System Profile");
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        HBox header = new HBox(12); header.setAlignment(Pos.CENTER_LEFT);
        TextField txtIdFinder = new TextField(); txtIdFinder.setPromptText("Type ID + Press Enter...");
        header.getChildren().addAll(new Label("Search Profile Employee ID:"), txtIdFinder);

        GridPane grid = new GridPane(); grid.setHgap(15); grid.setVgap(10);
        TextField txtFirst = new TextField(); TextField txtLast = new TextField();
        TextField txtPhone = new TextField(); TextField txtEmail = new TextField();
        TextField txtCivil = new TextField(); TextField txtStreet = new TextField();
        TextField txtCity = new TextField(); TextField txtProvince = new TextField();
        TextField txtCountry = new TextField(); TextField txtPostal = new TextField();
        DatePicker dpDob = new DatePicker();

        ComboBox<String> cbDept = new ComboBox<>(FXCollections.observableArrayList("Human Resources", "Information Technology", "Cyber Security", "Finance"));
        ComboBox<String> cbSec = new ComboBox<>(FXCollections.observableArrayList("1", "2", "3", "4"));
        TextField txtUser = new TextField(); PasswordField txtPass = new PasswordField();

        grid.add(new Label("First Name:"), 0, 0); grid.add(txtFirst, 1, 0);
        grid.add(new Label("Last Name:"), 2, 0); grid.add(txtLast, 3, 0);
        grid.add(new Label("Phone:"), 0, 1); grid.add(txtPhone, 1, 1);
        grid.add(new Label("Email:"), 2, 1); grid.add(txtEmail, 3, 1);
        grid.add(new Label("Civil ID:"), 0, 2); grid.add(txtCivil, 1, 2);
        grid.add(new Label("Street:"), 2, 2); grid.add(txtStreet, 3, 2);
        grid.add(new Label("City:"), 0, 3); grid.add(txtCity, 1, 3);
        grid.add(new Label("Province/State:"), 2, 3); grid.add(txtProvince, 3, 3);
        grid.add(new Label("Country:"), 0, 4); grid.add(txtCountry, 1, 4);
        grid.add(new Label("Postal/Zip:"), 2, 4); grid.add(txtPostal, 3, 4);
        grid.add(new Label("Date of Birth:"), 0, 5); grid.add(dpDob, 1, 5);
        grid.add(new Label("Department:"), 2, 5); grid.add(cbDept, 3, 5);
        grid.add(new Label("Security Level:"), 0, 6); grid.add(cbSec, 1, 6);
        grid.add(new Label("Username:"), 2, 6); grid.add(txtUser, 3, 6);
        grid.add(new Label("Password (*) :"), 0, 7); grid.add(txtPass, 1, 7);

        HBox actions = new HBox(15);
        Button btnSave = new Button("Save Modifications");
        Button btnCreate = new Button("Register New Profile");
        Button btnDelete = new Button("Purge Profile");
        btnDelete.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        actions.getChildren().addAll(btnSave, btnCreate, btnDelete);

        txtIdFinder.setOnAction(e -> {
            String idStr = txtIdFinder.getText().trim();
            if (idStr.isEmpty()) return;
            try {
                String[] profile = employeeDAO.getEmployeeCompleteDetails(Integer.parseInt(idStr));
                if (profile != null) {
                    txtFirst.setText(profile[1]); txtLast.setText(profile[2]); txtPhone.setText(profile[3]);
                    txtEmail.setText(profile[4]); txtCivil.setText(profile[5]); txtStreet.setText(profile[6]);
                    txtCity.setText(profile[7]); txtProvince.setText(profile[8]); txtCountry.setText(profile[9]);
                    txtPostal.setText(profile[10]);
                    dpDob.setValue(profile[11].isEmpty() ? null : LocalDate.parse(profile[11]));
                    cbDept.setValue(profile[12]); cbSec.setValue(profile[13]);
                    txtUser.setText(profile[14]); txtPass.setText(profile[15]);
                } else {
                    showErrorAlert("Record Absolute Fault", "No verified user matching ID target.", "Key identifier unmatched.");
                }
            } catch (Exception ex) {
                showErrorAlert("Query Process Halt", "Failed processing key lookup parameters.", ex.getMessage());
            }
        });

        btnSave.setOnAction(e -> {
            try {
                int id = Integer.parseInt(txtIdFinder.getText().trim());
                int deptId = employeeDAO.getDeptIdByName(cbDept.getValue());
                employeeDAO.updateEmployee(id, txtFirst.getText(), txtLast.getText(), txtPhone.getText(), txtEmail.getText(),
                        txtCivil.getText(), txtStreet.getText(), txtCity.getText(), txtProvince.getText(), txtCountry.getText(),
                        txtPostal.getText(), dpDob.getValue() != null ? dpDob.getValue().toString() : "", deptId,
                        Integer.parseInt(cbSec.getValue()), txtUser.getText(), txtPass.getText());
                refreshDataCache();
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Database profiles successfully modified."); a.show();
            } catch (Exception ex) {
                showErrorAlert("Transactional Interruption", "Failed to commit record updates.", ex.getMessage());
            }
        });

        btnCreate.setOnAction(e -> {
            try {
                int deptId = employeeDAO.getDeptIdByName(cbDept.getValue());
                employeeDAO.createEmployee(txtFirst.getText(), txtLast.getText(), txtPhone.getText(), txtEmail.getText(),
                        txtCivil.getText(), txtStreet.getText(), txtCity.getText(), txtProvince.getText(), txtCountry.getText(),
                        txtPostal.getText(), dpDob.getValue() != null ? dpDob.getValue().toString() : "", deptId,
                        Integer.parseInt(cbSec.getValue()), txtUser.getText(), txtPass.getText());
                refreshDataCache();
                Alert a = new Alert(Alert.AlertType.INFORMATION, "New identity catalog record registered successfully."); a.show();
            } catch (Exception ex) {
                showErrorAlert("Insertion Integrity Error", "Failed to compile secondary record parameters.", ex.getMessage());
            }
        });

        btnDelete.setOnAction(e -> {
            String targetIdText = txtIdFinder.getText().trim();
            if (targetIdText.isEmpty()) {
                showErrorAlert("Purge Aborted", "Please search and specify a profile ID first.", "");
                return;
            }

            // Challenge Dialogue Prompt
            if (promptDeleteVerificationChallenge()) {
                try {
                    int id = Integer.parseInt(targetIdText);
                    employeeDAO.removeEmployee(id);
                    refreshDataCache();
                    txtFirst.clear(); txtLast.clear(); txtPhone.clear(); txtEmail.clear(); txtCivil.clear();
                    txtStreet.clear(); txtCity.clear(); txtProvince.clear(); txtCountry.clear(); txtPostal.clear();
                    dpDob.setValue(null); txtUser.clear(); txtPass.clear(); txtIdFinder.clear();
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Identity file safely removed from data schemas."); a.show();
                } catch (Exception ex) {
                    showErrorAlert("Cascading Table Constraint Lockout", "Cannot delete. Active relational references to this record exist.", ex.getMessage());
                }
            } else {
                Alert cancellationAlert = new Alert(Alert.AlertType.WARNING, "Purge workflow cancelled or verification credentials invalid.");
                cancellationAlert.show();
            }
        });

        layout.getChildren().addAll(header, new Separator(), grid, actions);
        tab.setContent(layout);
        return tab;
    }

    private boolean promptDeleteVerificationChallenge() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("CRITICAL SECURITY CHALLENGE");
        dialog.setHeaderText("Confirm Deletion Profile Procedure.\nThis action is irreversible. Provide root authority credentials:");

        ButtonType confirmButtonType = new ButtonType("Authorize Purge", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtVerifyUser = new TextField();
        txtVerifyUser.setPromptText("dbadmin");
        PasswordField txtVerifyPass = new PasswordField();
        txtVerifyPass.setPromptText("Password");

        grid.add(new Label("Admin Username:"), 0, 0);
        grid.add(txtVerifyUser, 1, 0);
        grid.add(new Label("Admin Password:"), 0, 1);
        grid.add(txtVerifyPass, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmButtonType) {
            String inputUser = txtVerifyUser.getText().trim();
            String inputPass = txtVerifyPass.getText();
            return "dbadmin".equals(inputUser) && "Vanier1234".equals(inputPass);
        }
        return false;
    }

    // =========================================================================
    // TAB 3: ASSET MANAGEMENT (Assets & Hardware Operational Control Layer)
    // =========================================================================
    private Tab createAssetTab() {
        Tab tab = new Tab("Infrastructure Assets Ledger");
        VBox layout = new VBox(12);
        layout.setPadding(new Insets(15));

        HBox searchRow = new HBox(10); searchRow.setAlignment(Pos.CENTER_LEFT);
        TextField txtAssetGlobalSearch = new TextField(); txtAssetGlobalSearch.setPromptText("Type asset name/status to filter instantly...");
        txtAssetGlobalSearch.setPrefWidth(350);
        searchRow.getChildren().addAll(new Label("Global Table Filter:"), txtAssetGlobalSearch);

        TableView<String[]> assetTable = new TableView<>();
        TableColumn<String[], String> colId = new TableColumn<>("Asset ID"); colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        TableColumn<String[], String> colName = new TableColumn<>("Asset HostName"); colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        TableColumn<String[], String> colDate = new TableColumn<>("Purchase Date"); colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        TableColumn<String[], String> colStatus = new TableColumn<>("Tracking Status"); colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        TableColumn<String[], String> colEmp = new TableColumn<>("Assigned Owner ID"); colEmp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        TableColumn<String[], String> colSec = new TableColumn<>("Security Clearance Required"); colSec.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5]));

        assetTable.getColumns().addAll(colId, colName, colDate, colStatus, colEmp, colSec);
        assetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        FilteredList<String[]> filteredAssets = new FilteredList<>(masterAssetSummary, p -> true);
        txtAssetGlobalSearch.textProperty().addListener((o, old, n) -> {
            filteredAssets.setPredicate(asset -> {
                if (n == null || n.trim().isEmpty()) return true;
                String low = n.toLowerCase().trim();
                return asset[0].toLowerCase().contains(low) || asset[1].toLowerCase().contains(low) || asset[3].toLowerCase().contains(low);
            });
        });
        assetTable.setItems(filteredAssets);

        GridPane editor = new GridPane(); editor.setHgap(12); editor.setVgap(10);
        TextField txtAsName = new TextField(); DatePicker dpAsDate = new DatePicker();
        ComboBox<String> cbAsStatus = new ComboBox<>(FXCollections.observableArrayList("Available", "Assigned", "Maintenance", "Decommissioned"));
        TextField txtAsEmp = new TextField(); ComboBox<String> cbAsSec = new ComboBox<>(FXCollections.observableArrayList("1", "2", "3", "4"));

        editor.add(new Label("HostName:"), 0, 0); editor.add(txtAsName, 1, 0);
        editor.add(new Label("Purchase Date:"), 2, 0); editor.add(dpAsDate, 3, 0);
        editor.add(new Label("Status:"), 0, 1); editor.add(cbAsStatus, 1, 1);
        editor.add(new Label("Assigned Emp ID:"), 2, 1); editor.add(txtAsEmp, 3, 1);
        editor.add(new Label("Security Level Req:"), 0, 2); editor.add(cbAsSec, 1, 2);

        assetTable.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) {
                txtAsName.setText(n[1]);
                dpAsDate.setValue(n[2].isEmpty() ? null : LocalDate.parse(n[2]));
                cbAsStatus.setValue(n[3]); txtAsEmp.setText(n[4]); cbAsSec.setValue(n[5]);
            }
        });

        HBox controls = new HBox(12);
        Button btnAdd = new Button("Add Asset Entry");
        Button btnMod = new Button("Commit Modifications");
        Button btnDel = new Button("Scrap Asset Record");
        btnDel.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white;");
        controls.getChildren().addAll(btnAdd, btnMod, btnDel);

        btnAdd.setOnAction(e -> {
            try {
                String empVal = txtAsEmp.getText().trim();
                Integer empId = empVal.isEmpty() ? null : Integer.parseInt(empVal);
                securityDAO.addAsset(txtAsName.getText(), dpAsDate.getValue() != null ? dpAsDate.getValue().toString() : null,
                        cbAsStatus.getValue(), empId, Integer.parseInt(cbAsSec.getValue()));
                refreshDataCache();
            } catch (Exception ex) {
                showErrorAlert("Inventory Update Halt", "Failed processing parameters into inventory system blocks.", ex.getMessage());
            }
        });

        btnMod.setOnAction(e -> {
            String[] sel = assetTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                String empVal = txtAsEmp.getText().trim();
                Integer empId = empVal.isEmpty() ? null : Integer.parseInt(empVal);
                securityDAO.updateAsset(Integer.parseInt(sel[0]), txtAsName.getText(), dpAsDate.getValue() != null ? dpAsDate.getValue().toString() : null,
                        cbAsStatus.getValue(), empId, Integer.parseInt(cbAsSec.getValue()));
                refreshDataCache();
            } catch (Exception ex) {
                showErrorAlert("Transaction Validation Abort", "Failed modifying specific operational indexes.", ex.getMessage());
            }
        });

        btnDel.setOnAction(e -> {
            String[] sel = assetTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            try {
                securityDAO.deleteAsset(Integer.parseInt(sel[0]));
                refreshDataCache();
            } catch (Exception ex) {
                showErrorAlert("Purge Restriction Fault", "Foreign key constraints block database removal activity.", ex.getMessage());
            }
        });

        layout.getChildren().addAll(searchRow, assetTable, new Separator(), editor, controls);
        tab.setContent(layout);
        return tab;
    }

    // =========================================================================
    // TAB 4: SECURITY INCIDENT RESPONSE
    // =========================================================================
    private Tab createSecurityAuditTab() {
        Tab tab = new Tab("System Security & Audit");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label lblHeader = new Label("Consolidated Security & Audit Log Details");
        lblHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Unified TableView for both Incidents and Audits
        TableView<String[]> unifiedTable = new TableView<>();

        TableColumn<String[], String> colType = new TableColumn<>("Category");
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0])); // Incident or Audit

        TableColumn<String[], String> colId = new TableColumn<>("Ref ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));

        TableColumn<String[], String> colDesc = new TableColumn<>("Details");
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));

        TableColumn<String[], String> colTime = new TableColumn<>("Timestamp");
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));

        unifiedTable.getColumns().addAll(colType, colId, colDesc, colTime);
        unifiedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Mock data integration
        ObservableList<String[]> logs = FXCollections.observableArrayList(
                new String[]{"Incident", "INC-4001", "Brute-force attempt on terminal", "2026-06-18 14:22:01"},
                new String[]{"Audit", "AUD-9021", "User dbadmin updated HR records", "2026-06-18 19:01:10"}
        );
        VBox.setVgrow(unifiedTable, Priority.ALWAYS);

        unifiedTable.setItems(logs);
        layout.getChildren().addAll(lblHeader, unifiedTable);
        tab.setContent(layout);
        return tab;
    }

    // =========================================================================
    // TAB 5: ABOUT EITASM
    // =========================================================================
    private Tab createAboutTab() {
        Tab tab = new Tab("About");
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        try {
            FileInputStream input = new FileInputStream(ICON_PATH);
            ImageView imageView = new ImageView(new Image(input));
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);
            layout.getChildren().add(imageView);
        } catch (FileNotFoundException e) {
            layout.getChildren().add(new Label("Logo missing."));
        }

        Label lblTitle = new Label("EITASM console, version: 1.0.20");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox authors = new VBox(5);
        authors.setAlignment(Pos.CENTER);
        authors.getChildren().addAll(
                new Label("Author:"),
                new Label("Kwok Yiu Wong (ID: 9820001)"),
                new Label("Danny Gilberto (ID: 2339533)"),
                new Label("Nafiseh Khosravi Rad (ID: 6411955)")
        );

        layout.getChildren().addAll(lblTitle, authors);
        tab.setContent(layout);
        return tab;
    }

    private void showErrorAlert(String header, String context, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Operational Fault Alert");
        alert.setHeaderText(header);
        alert.setContentText(context + "\n\nDetails: " + details);
        alert.showAndWait();
    }
}