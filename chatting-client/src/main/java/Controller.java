
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class Controller implements Initializable {
  FileInputStream fileInputStream;
  DataInputStream ds;
  Map<String, ListView<Message>> savedListView = new HashMap<>();
  Map<String, List<String>> savedListFile = new HashMap<>();
  
  Map<String, String> getSavedListFile = new HashMap<>();
  ArrayList<String> chats = new ArrayList<>();
  public Socket socket;
  public HBox title;
  private double xOffset = 0;
  private double yOffset = 0;

  public Button close;
  public Button minimize;
  public Button maximize;
  boolean maximized;
    
  String toUsername;
  @FXML
  TextField containListTitle;
  @FXML
  Button privateChat;
  @FXML
  Button groupChat;
  @FXML
  Button groupList;
  @FXML
  Button showGroupList;
  List<String> toUsernames;
    
  List<List<String>> groups = new ArrayList<>();
    
  List<String> files = new ArrayList<>();
  public ListView<String> chatList;
  public TextArea inputArea;
  public Label currentUsername;
  public Label currentOnlineCnt;
  @FXML
  TextField userCnt;
  Stage stage;
  @FXML
  Button fileList;
  @FXML
  TextField left;
  BufferedReader bf;
    
  OutputStream os;
  @FXML
  ListView<Message> chatContentList;

  String username;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Stage stage = new Stage();
    stage.initStyle(StageStyle.TRANSPARENT);
    containListTitle.setEditable(false);
    inputArea.setVisible(false);
    chatContentList.setVisible(false);
    left.setEditable(false);
    userCnt.setEditable(false);
    groupList.setOnAction(event -> Platform.runLater(() -> setDialog("当前无群组")));
    close.setOnAction(actionEvent -> {
      try {
        socket.getOutputStream().write("close\n".getBytes());
      } catch (IOException e) {
        System.out.println("关闭失败");
      }
      System.out.println("客户端退出");
      try {
        save();
      } catch (IOException e) {
        System.out.println("保存信息失败");
      }
      Platform.exit();
      System.exit(0);
    });
    maximize.setOnAction(actionEvent -> ((Stage) maximize.getScene().getWindow())
        .setMaximized(maximized = !maximized));
    minimize.setOnAction(actionEvent -> ((Stage) minimize.getScene().getWindow())
        .setIconified(true));
    title.setOnMousePressed(event -> {
      xOffset = event.getSceneX();
      yOffset = event.getSceneY();
    });
    title.setOnMouseDragged(event -> {
      maximize.getScene().getWindow().setX(event.getScreenX() - xOffset);
      maximize.getScene().getWindow().setY(event.getScreenY() - yOffset);
    });
    TextField textField1 = new TextField();
    TextField textField2 = new TextField();
    Label l1 = new Label("用户名");
    Label l2 = new Label("密码");
    Button login = new Button("登录");
    Button signUp = new Button("注册");
    Button exit = new Button("退出");
    exit.setOnAction(event -> {
      stage.close();
      System.exit(0);
    });
    login.setOnAction(event -> {
      File file = new File("D:/COURSE/assignment/CS209/java2Assignment2/用户信息/"
          + textField1.getText() + ".txt");
      if (!file.exists()) {
        setDialog("用户不存在");
        return;
      }
      try {
        StringBuilder stringBuilder = new StringBuilder();
        int a;
        FileReader fr = new FileReader(file);
        while ((a = fr.read()) != -1) {
          stringBuilder.append((char) a);
        }
        if (!textField2.getText().isEmpty()
            && stringBuilder.toString().equals(textField2.getText())) {
          try {
            stage.close();
            while (true) {
              socket = new Socket("localhost", 1234);
              os = socket.getOutputStream();
              os.write((textField1.getText() + "\n").getBytes());
              bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
              if (Objects.equals(bf.readLine(), "")) {
                username = textField1.getText();
                left.setText(username);
                break;
              }
              setDialog("不能重复登录");
              stage.showAndWait();
            }
          } catch (IOException e) {
            System.out.println("无法连接到服务器");
          }
          StringBuilder stringBuilder1 = new StringBuilder();
          int aa;
          file = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/"
              + username + "/savedListFile.txt");
          if (file.exists() && file.length() != 0) {
            fr = new FileReader(file);
            while ((aa = fr.read()) != -1) {
              stringBuilder1.append((char) aa);
            }
            int start = 0;
            int initial = 0;
            int finial = 0;
            String substring = stringBuilder1.substring(1, stringBuilder1.length() - 1);
            for (int i = 0; i < substring.length(); i++) {
              if (substring.charAt(i) == '=') {
                savedListFile.put(substring.substring(initial, i), new ArrayList<>());
                finial = i;
                start = i + 1;
              } else if (substring.charAt(i) == '[') {
                start = i + 1;
              } else if (substring.charAt(i) == ',') {
                if (initial == i) {
                  initial = i + 1;
                } else {
                  savedListFile.get(substring.substring(initial, finial))
                      .add(substring.substring(start, i));
                }
                start = i + 1;
              } else if (substring.charAt(i) == ']') {
                savedListFile.get(substring.substring(initial, finial))
                    .add(substring.substring(start, i));
                start = i + 1;
                initial = start;
              }
            }
            stringBuilder1.delete(0, stringBuilder1.length());
          }
          file = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/"
              + username + "/getSavedListFile.txt");
          if (file.exists() && file.length() != 0) {
            fr = new FileReader(file);
            while ((aa = fr.read()) != -1) {
              stringBuilder1.append((char) aa);
            }
            String substring = stringBuilder1.substring(1, stringBuilder1.length() - 1);
            String[] strings = substring.split(",");
            for (String string : strings) {
              String[] split = string.split("=");
              if (split.length < 2) {
                continue;
              }
              getSavedListFile.put(split[0], split[1]);
            }
            stringBuilder1.delete(0, stringBuilder1.length());
          }
          file = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/"
              + username + "/savedListView.txt");
          if (file.exists() && file.length() != 0) {
            fr = new FileReader(file);
            while ((aa = fr.read()) != -1) {
              if (stringBuilder1.length() > 0 && stringBuilder1.substring(
                  stringBuilder1.length() - 1, stringBuilder1.length()).equals(",")
                  && (char) aa == ' ') {
                continue;
              }
              stringBuilder1.append((char) aa);
            }
            String[] strings = stringBuilder1.toString().split("\t");
            for (String string : strings) {
              String[] split = string.split("=");
              if (split.length < 2) {
                continue;
              }
              savedListView.put(split[0], new ListView<>());
              StringBuilder stringBuilder2 = new StringBuilder(split[1]);
              String substring = stringBuilder2.substring(1, stringBuilder2.length() - 1);
              String[] messages = substring.split(",");
              for (String message : messages) {
                String[] aaa = new String[4];
                Pattern pattern = Pattern.compile(" ");
                Matcher matcher = pattern.matcher(message);
                int j = 0;
                int index = 0;
                while (matcher.find()) {
                  aaa[j++] = message.substring(index, matcher.start());
                  index = matcher.start() + 1;
                  if (j == 3) {
                    aaa[j] = message.substring(index);
                    break;
                  }
                }
                savedListView.get(split[0]).getItems()
                    .add(new Message(Long.parseLong(aaa[0]), aaa[1], aaa[2], aaa[3]));
              }
            }
            stringBuilder1.delete(0, stringBuilder1.length());
          }
          chatContentList.setCellFactory(new MessageCellFactory());
          Task task = new Task() {
            @Override
            protected Object call() throws IOException, InterruptedException {
              while (true) {
                try {
                  String a = bf.readLine();
                  switch (a) {
                    case "userList":
                      updateChatList();
                      break;
                    case "message":
                      updateMessageView();
                      break;
                    case "exit":
                      if (toUsername != null && toUsername.equals(bf.readLine())) {
                        Platform.runLater(() -> setDialog("对方已下线"));
                        inputArea.setVisible(false);
                        chatContentList.setVisible(false);
                        toUsername = null;
                      }
                      save();
                      break;
                    case "group":
                      updateGroupList();
                      break;
                    case "file":
                      updateFileList();
                      break;
                  }
                } catch (Exception e) {
                  save();
                  Platform.runLater(() -> {
                    setDialog("服务器已关闭");
                  });
                  Thread.sleep(100000);
                }
              }
            }
          };
          new Thread(task).start();
        } else {
          setDialog("密码错误");
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    signUp.setOnAction(event -> {
      Stage stage1 = new Stage();
      TextField t1 = new TextField();
      TextField t2 = new TextField();
      Button okBt = new Button("确定");
      okBt.setOnAction(event1 -> {
        stage1.close();
        if (t2.getText().isEmpty() || t2.getText() == null || t1.getText().isEmpty()
            || t1.getText() == null || t1.getText().contains(" ") || t1.getText().contains(",")
            || t1.getText().contains("\n") || t1.getText().contains("=")) {
          setDialog("非法用户名");
          Platform.exit();
          return;
        }
        File file = new File("D:/COURSE/assignment/CS209/java2Assignment2/用户信息/"
            + t1.getText() + ".txt");
        if (file.exists()) {
          setDialog("用户已存在");
        } else {
          try {
            file.createNewFile();
            DataOutputStream ds = new DataOutputStream(Files.newOutputStream(file.toPath()));
            ds.write(t2.getText().getBytes());
            ds.flush();
          } catch (IOException e) {
            System.out.println("创建新用户失败");
          }
        }
      });
      Label ll1 = new Label("用户名");
      Label ll2 = new Label("密码");
      HBox box = new HBox(50);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(100, 100, 100, 100));
      box.getChildren().addAll(ll1, t1, ll2, t2, okBt);
      stage1.setScene(new Scene(box));
      stage1.showAndWait();
    });
    stage.setTitle("Login");
    HBox box = new HBox(50);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(100, 100, 100, 100));
    box.getChildren().addAll(l1, textField1, l2, textField2, login, signUp, exit);
    stage.setScene(new Scene(box));
    stage.showAndWait();
  }
  
  public void save() throws IOException {
    File file = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/" + username);
    if (!file.exists()) {
      file.mkdir();
    }
    File file1 = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/"
        + username + "/savedListView.txt");
    if (!file1.exists()) {
      file1.createNewFile();
    }
    DataOutputStream ds = new DataOutputStream(Files.newOutputStream(file1.toPath()));
    StringBuilder stringBuilder = new StringBuilder();
    List<String> c = new ArrayList<>(chatList.getItems());
    for (String s : c) {
      if (!s.equals(username) && savedListView.get(s) != null) {
        stringBuilder.append(s).append("=")
          .append(savedListView.get(s).getItems().toString()).append("\t");
      }
    }
    ds.write(stringBuilder.toString().getBytes());
    ds.flush();
    File file2 = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/"
        + username + "/getSavedListFile.txt");
    if (!file1.exists()) {
      file1.createNewFile();
    }
    ds = new DataOutputStream(Files.newOutputStream(file2.toPath()));
    ds.write(getSavedListFile.toString().getBytes());
    ds.flush();
    File file3 = new File("D:/COURSE/assignment/CS209/java2Assignment2/聊天记录/"
        + username + "/savedListFile.txt");
    if (!file1.exists()) {
      file1.createNewFile();
    }
    ds = new DataOutputStream(Files.newOutputStream(file3.toPath()));
    Map<String, List<String>> map = new HashMap<>(savedListFile);
    for (List<String> ls : groups) {
      map.remove(ls.toString());
    }
    ds.write(map.toString().getBytes());
    ds.flush();
  }
    
  private void updateFileList() throws IOException {
    String fileName = bf.readLine();
    String fileSendBy = bf.readLine();
    String fileLength = bf.readLine();
    Platform.runLater(() -> setDialog(fileSendBy + "给你传输了文件"));
    long length = 0;
    long total = 0;
    StringBuilder fileData = new StringBuilder();
    if (fileName.contains(".docx")) {
      fileData.append(bf.readLine());
    } else {
      byte[] b = new byte[1024];
      ds = new DataInputStream(socket.getInputStream());
      while ((length = ds.read(b)) != -1) {
        String str = new String(b, StandardCharsets.UTF_8);
        fileData.append(str);
        total += length;
        if (total >= Long.parseLong(fileLength)) {
          break;
        }
      }
    }
    savedListFile.computeIfAbsent(fileSendBy, k -> new ArrayList<>());
    savedListFile.get(fileSendBy).add(fileName);
    getSavedListFile.computeIfAbsent(fileName, k -> fileData.toString());
    setButtonFileList();
  }
    
  private void setDialog(String a) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("提示");
    alert.setContentText(a);
    alert.showAndWait();
    if (a.equals("服务器已关闭")) {
      System.exit(0);
    }
  }
  
  private void setButtonGroupList() {
    groupList.setOnAction(event -> {
      stage = new Stage();
      ComboBox<String> userSel = new ComboBox<>();
      userSel.setLayoutX(50);
      userSel.setLayoutY(50);
      for (List<String> ls : groups) {
        userSel.getItems().add(toStringUsernames(ls.toString()));
      }
      Button okBtn = new Button("OK");
      okBtn.setOnAction(e -> {
        stage.close();
        if (userSel.getSelectionModel().getSelectedItem() == null
            || toUsernames != null && Objects.equals(toStringUsernames(toUsernames.toString()),
            userSel.getSelectionModel().getSelectedItem())) {
          return;
        }
        toUsernames = Arrays.asList(userSel.getSelectionModel().getSelectedItem().split(","));
        chatContentList.getItems().clear();
        if (savedListView.get(toUsernames.toString()) == null) {
          savedListView.put(toUsernames.toString(), new ListView<>());
        }
        savedListFile.computeIfAbsent(toUsernames.toString(), k -> new ArrayList<>());
        files = savedListFile.get(toUsernames.toString());
        setButtonFileList();
        chatContentList.getItems().addAll(savedListView.get(toUsernames.toString()).getItems());
        containListTitle.setText(toUsernames.toString());
        inputArea.setVisible(true);
        chatContentList.setVisible(true);
        toUsername = null;
      });
      HBox box = new HBox(50);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(100, 100, 100, 100));
      box.getChildren().addAll(userSel, okBtn);
      stage.setScene(new Scene(box));
      stage.showAndWait();
    });
  }
  
  private void updateGroupList() throws IOException {
    if (stage != null) {
      Platform.runLater(() -> {
        stage.close();
        stage = null;
      });
    }
    String a = bf.readLine();
    groups.add(new ArrayList<>(Arrays.asList(a.split(","))));
    setButtonGroupList();
  }
  
  private void updateMessageView() throws IOException {
    int times = Integer.parseInt(bf.readLine());
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < times + 1; i++) {
      str.append(bf.readLine()).append("\n");
    }
    str.delete(str.length() - 1, str.length());
    String[] a = new String[4];
    Pattern pattern = Pattern.compile(" ");
    Matcher matcher = pattern.matcher(str);
    int i = 0;
    int index = 0;
    while (matcher.find()) {
      a[i++] = str.substring(index, matcher.start());
      index = matcher.start() + 1;
      if (i == 3) {
        a[i] = str.substring(index);
        break;
      }
    }
    Message message = new Message(Long.parseLong(a[0]), a[1], a[2], a[3]);
    if (a[2].equals(username)) {
      if (savedListView.get(a[1]) == null) {
        savedListView.put(a[1], new ListView<>());
      }
      savedListView.get(a[1]).getItems().add(message);
      chats.remove(a[1]);
      ArrayList<String> al = new ArrayList<>(chats);
      chats.clear();
      chats.add(a[1]);
      chats.addAll(al);
      if (!chats.contains(username)) {
        chats.add(username);
      }
      Platform.runLater(() -> {
        if (a[1].equals(toUsername)) {
          chatContentList.getItems().add(message);
        }
        setDialog("用户" + a[1] + "给你发了消息");
        chatList.getItems().clear();
        chatList.getItems().addAll(chats);
      });
    } else {
      List<String> list = Arrays.asList(a[2].split(","));
      if (savedListView.get(list.toString()) == null) {
        savedListView.put(list.toString(), new ListView<>());
      }
      savedListView.get(list.toString()).getItems().add(message);
      Platform.runLater(() -> {
        if (toUsernames != null && list.toString().equals(toUsernames.toString())) {
          chatContentList.getItems().add(message);
        }
      });
    }
  }
  
  public void setButtonFileList() {
    fileList.setOnAction(event -> {
      stage = new Stage();
      ComboBox<String> userSel = new ComboBox<>();
      userSel.setLayoutX(10);
      userSel.setLayoutY(30);
      userSel.getItems().addAll(files);
      Button okBtn = new Button("OK");
      okBtn.setOnAction(e -> {
        stage.close();
        if (userSel.getSelectionModel().getSelectedItem() == null) {
          return;
        }
        String filename = userSel.getSelectionModel().getSelectedItem();
        String fileData = getSavedListFile.get(filename);
        File file = new File("D:/COURSE/assignment/CS209/java2Assignment2/用户接收的文件/"
            + username);
        if (!file.exists()) {
          file.mkdir();
        }
        File file1 = new File("D:/COURSE/assignment/CS209/java2Assignment2/用户接收的文件/"
            + username + "/" + filename);
        try {
          DataOutputStream ds = new DataOutputStream(Files.newOutputStream(file1.toPath()));
          ds.write(fileData.getBytes());
          ds.flush();
        } catch (IOException ex) {
          System.out.println("文件下载异常");
        }
      });
      HBox box = new HBox(50);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(100, 100, 100, 100));
      box.getChildren().addAll(userSel, okBtn);
      stage.setScene(new Scene(box));
      stage.showAndWait();
    });
  }
  
  public void updateChatList() throws IOException {
    if (stage != null) {
      Platform.runLater(() -> {
        stage.close();
        stage = null;
      });
    }
    String[] as = bf.readLine().split(" ");
    chats.clear();
    for (String a : as) {
      if (!a.equals(username) && !a.equals("")) {
        chats.add(a);
      }
    }
    Platform.runLater(() -> {
      chatList.getItems().remove(0, chatList.getItems().size());
      chatList.getItems().addAll(chats);
      chatList.getItems().add(username);
      userCnt.setText(String.valueOf(chatList.getItems().size()));
    });
    privateChat.setOnAction(event -> {
      stage = new Stage();
      ComboBox<String> userSel = new ComboBox<>();
      userSel.setLayoutX(10);
      userSel.setLayoutY(30);
      userSel.getItems().addAll(chatList.getItems());
      Button okBtn = new Button("OK");
      okBtn.setOnAction(e -> {
        stage.close();
        if (userSel.getSelectionModel().getSelectedItem() == null
            || Objects.equals(toUsername, userSel.getSelectionModel().getSelectedItem())
            || username.equals(userSel.getSelectionModel().getSelectedItem())) {
          return;
        }
        toUsername = userSel.getSelectionModel().getSelectedItem();
        chatContentList.getItems().clear();
        if (!toUsername.equals(username)) {
          if (savedListView.get(toUsername) == null) {
            savedListView.put(toUsername, new ListView<>());
          }
          savedListFile.computeIfAbsent(toUsername, k -> new ArrayList<>());
          files = savedListFile.get(toUsername);
          setButtonFileList();
          chatContentList.getItems().addAll(savedListView.get(toUsername).getItems());
          containListTitle.setText(toUsername);
          inputArea.setVisible(true);
          chatContentList.setVisible(true);
          toUsernames = null;
        }
      });
      
      HBox box = new HBox(50);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(100, 100, 100, 100));
      box.getChildren().addAll(userSel, okBtn);
      stage.setScene(new Scene(box));
      stage.showAndWait();
    });
    groupChat.setOnAction(event -> {
      stage = new Stage();
      List<CheckBox> checkBoxList = new ArrayList<>();
      for (String chat : chats) {
        if (!chat.equals(username)) {
          checkBoxList.add(new CheckBox(chat));
        }
      }
      if (checkBoxList.size() == 0) {
        setDialog("当前无法创建群组！");
        return;
      }
      Button okBtn = new Button("OK");
      List<String> t = new ArrayList<>();
      okBtn.setOnAction(e -> {
        stage.close();
        for (CheckBox cb : checkBoxList) {
          if (cb.isSelected()) {
            t.add(cb.getText());
          }
        }
        if (t.size() == 0) {
          return;
        }
        t.add(username);
        groups.add(new ArrayList<>(t));
        try {
          setButtonGroupList();
          os.write(("group\n" + toStringUsernames(t.toString()) + "\n").getBytes());
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      });
      HBox box = new HBox(50);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(100, 100, 100, 100));
      box.getChildren().addAll(checkBoxList);
      box.getChildren().addAll(okBtn);
      stage.setScene(new Scene(box));
      stage.showAndWait();
    });
    showGroupList.setOnAction(event -> {
      if (toUsernames == null) {
        Platform.runLater(() -> setDialog("当前窗口不是群组窗口"));
      } else {
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(toStringUsernames(toUsernames.toString()).split(","));
        Stage stage = new Stage();
        HBox box = new HBox(50);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(100, 100, 100, 100));
        box.getChildren().addAll(listView);
        stage.setScene(new Scene(box));
        stage.showAndWait();
      }
    });
  }
  
  public String toStringUsernames(String a) {
    StringBuilder stringBuilder = new StringBuilder(a);
    stringBuilder.delete(0, 1);
    stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
    return stringBuilder.toString().replace(" ", "");
  }
    
  private void sendMessage(Message m) throws IOException {
    chatContentList.getItems().add(m);
    Pattern pattern = Pattern.compile("\n");
    Matcher matcher = pattern.matcher(inputArea.getText());
    int i = 0;
    while (matcher.find()) {
      i++;
    }
    os.write(("message\n" + i + "\n" + m + "\n").getBytes());
    inputArea.clear();
  }
  
  @FXML
  public void doSendMessage() throws IOException {
    if (toUsername != null) {
      Message m = new Message(System.currentTimeMillis(), username,
          toUsername, inputArea.getText());
      if (!m.getData().equals("")) {
        sendMessage(m);
        savedListView.get(toUsername).getItems().add(m);
      }
      chats.remove(toUsername);
      ArrayList<String> al = new ArrayList<>(chats);
      chats.clear();
      chats.add(toUsername);
      chats.addAll(al);
      if (!chats.contains(username)) {
        chats.add(username);
      }
      Platform.runLater(() -> {
        chatList.getItems().clear();
        chatList.getItems().addAll(chats);
      });
    }
    if (toUsernames != null) {
      Message m = new Message(System.currentTimeMillis(), username,
          toStringUsernames(toUsernames.toString()), inputArea.getText());
      if (!m.getData().equals("")) {
        sendMessage(m);
        savedListView.get(toUsernames.toString()).getItems().add(m);
      }
    }
  }
    
  @FXML
  public void sendFile() throws IOException {
    if (toUsername == null && toUsernames == null) {
      return;
    }
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("请选择要传输的文件");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("ALL", "*.*"),
      new FileChooser.ExtensionFilter("PNG", "*.png"),
      new FileChooser.ExtensionFilter("JPG", "*.jpg"),
      new FileChooser.ExtensionFilter("BMP", "*.bmp"),
      new FileChooser.ExtensionFilter("GIF", "*.gif"),
      new FileChooser.ExtensionFilter("ICO", "*.ico"),
      new FileChooser.ExtensionFilter("APK", "*.apk"),
      new FileChooser.ExtensionFilter("EXE", "*.exe"),
      new FileChooser.ExtensionFilter("TXT", "*.txt"),
      new FileChooser.ExtensionFilter("DOC", "*.doc"),
      new FileChooser.ExtensionFilter("DOCX", "*.docx"),
      new FileChooser.ExtensionFilter("PPTX", "*.pptx"),
      new FileChooser.ExtensionFilter("PPT", "*.ppt"),
      new FileChooser.ExtensionFilter("PDF", "*.pdf"),
      new FileChooser.ExtensionFilter("XLS", "*.xls"),
      new FileChooser.ExtensionFilter("XLSX", "*.xlsx"),
      new FileChooser.ExtensionFilter("AVI", "*.avi"),
      new FileChooser.ExtensionFilter("MP4", "*.mp4"),
      new FileChooser.ExtensionFilter("OGG", "*.ogg"),
      new FileChooser.ExtensionFilter("MP3", "*.mp3"),
      new FileChooser.ExtensionFilter("PSD", "*.psd"),
      new FileChooser.ExtensionFilter("RAR", "*.rar"),
      new FileChooser.ExtensionFilter("ZIP", "*.zip"),
      new FileChooser.ExtensionFilter("PSD", "*.psd"),
      new FileChooser.ExtensionFilter("JAR", "*.jar"),
      new FileChooser.ExtensionFilter("JAVA", "*.java"),
      new FileChooser.ExtensionFilter("CLASS", "*.class")
    );
    File file = fileChooser.showOpenDialog(new Stage());
    if (file == null) {
      return;
    }
    fileInputStream = new FileInputStream(file);
    String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
    StringBuilder stringBuilder = new StringBuilder();
    if (ext.equals("docx") || ext.equals("doc")) {
      XWPFDocument xwpfDocument = new XWPFDocument(fileInputStream);
      List<XWPFParagraph> xwpfParagraphs = xwpfDocument.getParagraphs();
      for (XWPFParagraph xwpfParagraph : xwpfParagraphs) {
        stringBuilder.append(xwpfParagraph.getText());
      }
      stringBuilder.append("\n");
      if (toUsername != null) {
        os.write(("file\n" + toUsername + "\n" + file.getName() + "\n"
            + stringBuilder.toString().getBytes().length + "\n").getBytes());
        os.write(stringBuilder.toString().getBytes());
      } else if (toUsernames != null) {
        os.write(("file\n" + toStringUsernames(toUsernames.toString()) + "\n" + file.getName()
            + "\n" + stringBuilder.toString().getBytes().length + "\n").getBytes());
        os.write(stringBuilder.toString().getBytes());
      }
    } else {
      if (toUsername != null) {
        os.write(("file\n" + toUsername + "\n" + file.getName()
            + "\n" + file.length() + "\n").getBytes());
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = fileInputStream.read(b)) != -1) {
          os.write(b);
          os.flush();
        }
      } else if (toUsernames != null) {
        os.write(("file\n" + toStringUsernames(toUsernames.toString()) + "\n"
            + file.getName() + "\n" + file.length() + "\n").getBytes());
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = fileInputStream.read(b)) != -1) {
          os.write(b);
          os.flush();
        }
      }
    }
  }
  
  private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {
        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }
          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          Label msgLabel = new Label(msg.getData());
          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
          
          if (username.equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }
          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}
