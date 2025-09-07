package com.example.todo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.scene.control.Alert.AlertType.*;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

public class ToDoController implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TableView<TodoItem> todoTable;

    @FXML
    private TableColumn<TodoItem, Long> idColumn;

    @FXML
    private TableColumn<TodoItem, String> titleColumn;

    @FXML
    private TableColumn<TodoItem, String> descriptionColumn;

    @FXML
    private TableColumn<TodoItem, String> completedColumn;

    @FXML
    private TableColumn<TodoItem, String> createdAtColumn;

    @FXML
    private TableColumn<TodoItem, String> updatedAtColumn;

    @FXML
    private TableColumn<TodoItem, Void> actionsColumn;

    @FXML
    private Label totalTasksLabel;

    @FXML
    private Label completedTasksLabel;

    @FXML
    private Label footerTotalLabel;

    @FXML
    private Label filterLabel;

    private final TodoService todoService = new TodoService();
    private final ObservableList<TodoItem> todoItems = FXCollections.observableArrayList();
    private final FilteredList<TodoItem> filteredTodoItems = new FilteredList<>(todoItems);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadTodoItems();
        updateTaskCounters();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        completedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().isCompleted() ? "Выполнено" : "Активно"
                )
        );

        createdAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(formatter)
                ));

        updatedAtColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getUpdatedAt().format(formatter)
                ));

        setupActionsColumn();

        SortedList<TodoItem> sortedData = new SortedList<>(filteredTodoItems);
        sortedData.comparatorProperty().bind(todoTable.comparatorProperty());
        todoTable.setItems(sortedData);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Редактировать");
            private final Button deleteButton = new Button("Удалить");
            private final HBox pane = new HBox(editButton, deleteButton);

            {
                editButton.setMaxWidth(Double.MAX_VALUE);
                deleteButton.setMaxWidth(Double.MAX_VALUE);

                HBox.setHgrow(editButton, Priority.ALWAYS);
                HBox.setHgrow(deleteButton, Priority.ALWAYS);

                editButton.setOnAction(event -> {
                    TodoItem item = getTableView().getItems().get(getIndex());
                    handleEditTodo(item);
                });
                deleteButton.setOnAction(event -> {
                    TodoItem item = getTableView().getItems().get(getIndex());
                    handleDeleteTodo(item.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                    pane.setPrefWidth(actionsColumn.getWidth() - 10);
                    pane.setSpacing(5);
                }
            }
        });
    }

    @FXML
    private void handleAddTodo() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();

        if (title.isEmpty()) {
            showAlert();
            return;
        }

        todoService.addTodoItem(title, description);
        clearFields();
        loadTodoItems();
        updateTaskCounters();
    }

    @FXML
    private void handleShowCompleted() {
        filteredTodoItems.setPredicate(item -> item.isCompleted());
        filterLabel.setText("(только выполненные)");
        updateFooterCounter();
    }

    @FXML
    private void handleShowActive() {
        filteredTodoItems.setPredicate(item -> !item.isCompleted());
        filterLabel.setText("(только активные)");
        updateFooterCounter();
    }

    @FXML
    private void handleShowAll() {
        filteredTodoItems.setPredicate(null);
        filterLabel.setText("(все задачи)");
        updateFooterCounter();
    }

    private void handleEditTodo(TodoItem item) {
        Dialog<TodoItem> dialog = new Dialog<>();
        dialog.setTitle("Редактирование задачи");
        dialog.setHeaderText("Редактирование задачи #" + item.getId());

        ButtonType saveButtonType = new ButtonType("Сохранить", OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField editTitleField = new TextField(item.getTitle());
        TextField editDescriptionField = new TextField(item.getDescription());
        CheckBox completedCheckBox = new CheckBox("Выполнено");
        completedCheckBox.setSelected(item.isCompleted());

        grid.add(new Label("Название:"), 0, 0);
        grid.add(editTitleField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(editDescriptionField, 1, 1);
        grid.add(completedCheckBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new TodoItem(
                        item.getId(),
                        editTitleField.getText(),
                        editDescriptionField.getText(),
                        completedCheckBox.isSelected(),
                        item.getCreatedAt(),
                        null
                );
            }
            return null;
        });

        Optional<TodoItem> result = dialog.showAndWait();
        result.ifPresent(editedItem -> {
            todoService.updateTodoItem(
                    editedItem.getId(),
                    editedItem.getTitle(),
                    editedItem.getDescription(),
                    editedItem.isCompleted()
            );
            loadTodoItems();
            updateTaskCounters();
        });
    }

    private void handleDeleteTodo(Long id) {
        Alert alert = new Alert(CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Вы уверены, что хотите удалить эту задачу?");
        alert.setContentText("Это действие нельзя отменить.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == OK) {
            todoService.deleteTodoItem(id);
            loadTodoItems();
            updateTaskCounters();
        }
    }

    private void loadTodoItems() {
        todoItems.setAll(todoService.getAllTodoItems());
        updateTaskCounters();
    }

    private void updateTaskCounters() {
        int total = todoItems.size();
        long completed = todoItems.stream().filter(TodoItem::isCompleted).count();

        totalTasksLabel.setText(String.valueOf(total));
        completedTasksLabel.setText(String.valueOf(completed));
        updateFooterCounter();
    }

    private void updateFooterCounter() {
        int visibleCount = filteredTodoItems.size();
        footerTotalLabel.setText(String.valueOf(visibleCount));
    }

    private void clearFields() {
        titleField.clear();
        descriptionField.clear();
    }

    private void showAlert() {
        Alert alert = new Alert(WARNING);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText("Название задачи не может быть пустым");
        alert.showAndWait();
    }
}