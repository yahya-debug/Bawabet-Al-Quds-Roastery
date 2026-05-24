package app.db_proj;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.*;

public class ItemTable_UI {

    private TableView<ItemRow> table;
    private ObservableList<ItemRow> items;

    private TextField idField, nameField, priceField, wholesaleField;
    private ComboBox<String> typeBox;

    private TextField beanField, lightField, darkField;
    private TextField packageNameField, descriptionField;

    private HBox coffeeBox, packageBox;

    public VBox getPage() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.hsb(35, 0.12, 0.18, 1),
                CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = new Label("Items Table");
        title.setFont(Font.font("Adwaita Mono", FontWeight.BOLD, 26));
        title.setTextFill(Color.hsb(48, 1, 0.92, 1));

        table = new TableView<>();
        items = FXCollections.observableArrayList();
        table.setPrefHeight(400);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupColumns();

        VBox form = makeForm();
        HBox buttons = makeButtons();

        root.getChildren().addAll(title, table, form, buttons);

        loadItems();
        return root;
    }

    private void setupColumns() {
        TableColumn<ItemRow, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("itemId"));

        TableColumn<ItemRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ItemRow, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<ItemRow, Double> wholesaleCol = new TableColumn<>("Wholesale");
        wholesaleCol.setCellValueFactory(new PropertyValueFactory<>("wholesalePrice"));

        TableColumn<ItemRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("itemType"));

        TableColumn<ItemRow, String> beanCol = new TableColumn<>("Bean Type");
        beanCol.setCellValueFactory(new PropertyValueFactory<>("beanType"));

        TableColumn<ItemRow, Double> lightCol = new TableColumn<>("Light Ratio");
        lightCol.setCellValueFactory(new PropertyValueFactory<>("lightRatio"));

        TableColumn<ItemRow, Double> darkCol = new TableColumn<>("Dark Ratio");
        darkCol.setCellValueFactory(new PropertyValueFactory<>("darkRatio"));

        TableColumn<ItemRow, String> packageCol = new TableColumn<>("Package Name");
        packageCol.setCellValueFactory(new PropertyValueFactory<>("packageName"));

        TableColumn<ItemRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        table.getColumns().addAll(idCol, nameCol, priceCol, wholesaleCol, typeCol,
                beanCol, lightCol, darkCol, packageCol, descCol);
    }

    private VBox makeForm() {
        VBox mainBox = new VBox(10);
        mainBox.setAlignment(Pos.CENTER);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setAlignment(Pos.CENTER);

        idField = new TextField();
        idField.setPromptText("ID");
        idField.setDisable(true);

        nameField = new TextField();
        nameField.setPromptText("Name");

        priceField = new TextField();
        priceField.setPromptText("Price");

        wholesaleField = new TextField();
        wholesaleField.setPromptText("Wholesale Price");

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("coffee", "roasts", "spice", "package");
        typeBox.setPromptText("Type");
        typeBox.setOnAction(e -> updateVisibleFields());

        gp.add(idField, 0, 0);
        gp.add(nameField, 1, 0);
        gp.add(priceField, 2, 0);
        gp.add(wholesaleField, 3, 0);
        gp.add(typeBox, 4, 0);

        beanField = new TextField();
        beanField.setPromptText("Bean Type");

        lightField = new TextField();
        lightField.setPromptText("Light Ratio");

        darkField = new TextField();
        darkField.setPromptText("Dark Ratio");

        coffeeBox = new HBox(5, beanField, lightField, darkField);
        coffeeBox.setAlignment(Pos.CENTER);
        coffeeBox.setVisible(false);
        coffeeBox.setManaged(false);

        packageNameField = new TextField();
        packageNameField.setPromptText("Package Name");

        descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        packageBox = new HBox(5, packageNameField, descriptionField);
        packageBox.setAlignment(Pos.CENTER);
        packageBox.setVisible(false);
        packageBox.setManaged(false);

        gp.add(coffeeBox, 0, 1, 5, 1);
        gp.add(packageBox, 0, 1, 5, 1);

        mainBox.getChildren().add(gp);

        return mainBox;
    }
    private void updateVisibleFields() {
        coffeeBox.setVisible(false);
        coffeeBox.setManaged(false);

        packageBox.setVisible(false);
        packageBox.setManaged(false);

        if ("coffee".equals(typeBox.getValue())) {
            coffeeBox.setVisible(true);
            coffeeBox.setManaged(true);

            packageNameField.clear();
            descriptionField.clear();

        } else if ("package".equals(typeBox.getValue())) {
            packageBox.setVisible(true);
            packageBox.setManaged(true);

            beanField.clear();
            lightField.clear();
            darkField.clear();

        } else {
            beanField.clear();
            lightField.clear();
            darkField.clear();

            packageNameField.clear();
            descriptionField.clear();
        }
    }

    private HBox makeButtons() {
        Button add = new Button("Add");

        add.setOnAction(e -> addItem());

        HBox box = new HBox(10, add);
        box.setAlignment(Pos.CENTER);

        return box;
    }

    private void addItem() {
        if (!validateMainFields()) {
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            int newId = insertItem(con);
            insertSubtype(con, newId);

            loadItems();
            clearFields();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private boolean validateMainFields() {
        if (nameField.getText().isEmpty()
                || priceField.getText().isEmpty()
                || wholesaleField.getText().isEmpty()
                || typeBox.getValue() == null) {

            showError("Fill name, price, wholesale price and type");
            return false;
        }

        try {
            Double.parseDouble(priceField.getText());
            Double.parseDouble(wholesaleField.getText());

            if ("coffee".equals(typeBox.getValue())) {
                if (!lightField.getText().isEmpty()) {
                    Double.parseDouble(lightField.getText());
                }

                if (!darkField.getText().isEmpty()) {
                    Double.parseDouble(darkField.getText());
                }
            }

        } catch (NumberFormatException e) {
            showError("Numeric fields must contain valid numbers");
            return false;
        }

        return true;
    }

    private int insertItem(Connection con) throws SQLException {
        String sql = "INSERT INTO Item(name, price, wholesale_price, item_type) VALUES(?, ?, ?, ?)";

        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, nameField.getText());
        ps.setDouble(2, Double.parseDouble(priceField.getText()));
        ps.setDouble(3, Double.parseDouble(wholesaleField.getText()));
        ps.setString(4, typeBox.getValue());

        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();

        int newId = 0;

        if (keys.next()) {
            newId = keys.getInt(1);
        }

        keys.close();
        ps.close();

        return newId;
    }

    private void insertSubtype(Connection con, int id) throws SQLException {
        String type = typeBox.getValue();

        if ("coffee".equals(type)) {
            PreparedStatement c = con.prepareStatement(
                    "INSERT INTO Coffee(item_id, beanType, lightRatio, darkRatio) VALUES(?, ?, ?, ?)"
            );

            c.setInt(1, id);
            c.setString(2, beanField.getText());
            c.setDouble(3, lightField.getText().isEmpty() ? 0 : Double.parseDouble(lightField.getText()));
            c.setDouble(4, darkField.getText().isEmpty() ? 0 : Double.parseDouble(darkField.getText()));

            c.executeUpdate();
            c.close();

        } else if ("package".equals(type)) {
            PreparedStatement p = con.prepareStatement(
                    "INSERT INTO `Package`(item_id, package_name, description) VALUES(?, ?, ?)"
            );

            p.setInt(1, id);
            p.setString(2, packageNameField.getText());
            p.setString(3, descriptionField.getText());

            p.executeUpdate();
            p.close();

        } else {
            String table = type.substring(0, 1).toUpperCase() + type.substring(1);

            PreparedStatement s = con.prepareStatement(
                    "INSERT INTO " + table + "(item_id) VALUES(?)"
            );

            s.setInt(1, id);

            s.executeUpdate();
            s.close();
        }
    }

    private void clearFields() {
        idField.clear();
        nameField.clear();
        priceField.clear();
        wholesaleField.clear();

        typeBox.setValue(null);

        beanField.clear();
        lightField.clear();
        darkField.clear();

        packageNameField.clear();
        descriptionField.clear();

        updateVisibleFields();
        table.getSelectionModel().clearSelection();
    }

    private void loadItems() {
        items.clear();

        String sql = "SELECT i.item_id, i.name, i.price, i.wholesale_price, i.item_type, " +
                "c.beanType, c.lightRatio, c.darkRatio, " +
                "p.package_name, p.description " +
                "FROM Item i " +
                "LEFT JOIN Coffee c ON i.item_id = c.item_id " +
                "LEFT JOIN `Package` p ON i.item_id = p.item_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new ItemRow(
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("wholesale_price"),
                        rs.getString("item_type"),
                        rs.getString("beanType"),
                        rs.getDouble("lightRatio"),
                        rs.getDouble("darkRatio"),
                        rs.getString("package_name"),
                        rs.getString("description")
                ));
            }

            table.setItems(items);

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static class ItemRow {
        private final int itemId;
        private final String name;
        private final double price;
        private final double wholesalePrice;

        private final String itemType;
        private final String beanType;
        private final String packageName;
        private final String description;

        private final double lightRatio;
        private final double darkRatio;

        public ItemRow(int itemId, String name, double price, double wholesalePrice,
                       String itemType, String beanType, double lightRatio, double darkRatio,
                       String packageName, String description) {

            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.wholesalePrice = wholesalePrice;
            this.itemType = itemType;
            this.beanType = beanType;
            this.lightRatio = lightRatio;
            this.darkRatio = darkRatio;
            this.packageName = packageName;
            this.description = description;
        }

        public int getItemId() {
            return itemId;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public double getWholesalePrice() {
            return wholesalePrice;
        }

        public String getItemType() {
            return itemType;
        }

        public String getBeanType() {
            return beanType;
        }

        public double getLightRatio() {
            return lightRatio;
        }

        public double getDarkRatio() {
            return darkRatio;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getDescription() {
            return description;
        }
    }
}