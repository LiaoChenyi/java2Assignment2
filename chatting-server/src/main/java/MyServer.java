

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;


public class MyServer extends Observable implements Runnable {
  static List<ClientThread> threadList = new ArrayList<>();
  static List<String> userList = new ArrayList<>();
  static ServerSocket serverSocket;
  
  public MyServer() throws IOException {
    serverSocket = new ServerSocket(1234);
    new Thread(this).start();
  }
  
  @Override
  public void run() {
    try {
      while (true) {
        System.out.println("等待连接");
        Socket socket = serverSocket.accept();
        System.out.println("连接中");
        ClientThread clientThread = new ClientThread(socket, this);
        threadList.add(clientThread);
        clientThread.start();
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      System.err.println("连接异常");
      System.exit(0);
    }
  }
  
  
  public static class ClientThread extends Thread {
    
    Socket socket;
    String username;
    MyServer server;
    private BufferedReader bf;
    DataInputStream ds;
    private OutputStream os;
    
    public ClientThread(Socket socket, MyServer server) {
      this.socket = socket;
      this.server = server;
    }
  
    @Override
    public void run() {
      try {
        bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        username = bf.readLine();
        os = socket.getOutputStream();
        //判断是否具有相同的用户名
        if (userList.contains(username)) {
          System.out.println("连接失败");
          os.write("!\n".getBytes());
          threadList.remove(currentThread());
          return;
        } else {
          System.out.println("用户" + username + "已连接到服务器");
          os.write("\n".getBytes());
          userList.add(username);
          sendUserList();
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      try {
        String a;
        do {
          a = bf.readLine();
          switch (a) {
            case "userList":
              sendUserList();
              break;
            case "message":
              sendMessage(Integer.parseInt(bf.readLine()));
              break;
            case "group":
              sendGroup(bf.readLine());
              break;
            default:
              sendFile(bf.readLine());
          }
        } while (!a.equals("close"));
        System.out.println("用户" + username + "断开连接");
      } catch (IOException | InterruptedException e) {
        System.out.println("用户" + username + "非正常断开连接");
      } finally {
        try {
          userList.remove(username);
          threadList.remove(currentThread());
          sendUserList();
          sendExit(username);
          os.close();
          bf.close();
        } catch (IOException e) {
          System.out.println("断开连接时出现了一些异常");
        }
      }
    }
  
    private void sendFile(String a) throws IOException, InterruptedException {
      String fileName = bf.readLine();
      String fileLength = bf.readLine();
      List<String> aa = Arrays.asList(a.split(","));
      for (ClientThread t : threadList) {
        if (aa.contains(t.username) && !t.equals(currentThread())) {
          if (aa.size() == 1) {
            t.os.write(("file\n" + fileName + "\n" + username + "\n" + fileLength + "\n")
                .getBytes());
          } else {
            t.os.write(("file\n" + fileName + "\n" + aa + "\n" + fileLength + "\n").getBytes());
          }
          if (fileName.contains(".docx")) {
            t.os.write((bf.readLine() + "\n").getBytes());
          } else {
            byte[] b = new byte[1024];
            long length = 0;
            long total = 0;
            ds = new DataInputStream(socket.getInputStream());
            while ((length = ds.read(b)) != -1) {
              t.os.write(b);
              t.os.flush();
              total += length;
              if (total >= Long.parseLong(fileLength)) {
                break;
              }
            }
          }
        }
      }
    }
  
    private void sendMessage(int time) throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < time + 1; i++) {
        stringBuilder.append(bf.readLine()).append("\n");
      }
      String[] a = stringBuilder.toString().split(" ");
      List<String> aa = Arrays.asList(a[2].split(","));
      for (ClientThread t : threadList) {
        if (aa.contains(t.username) && !t.equals(currentThread())) {
          t.os.write(("message\n" + time + "\n" + stringBuilder).getBytes());
        }
      }
    }
  
    private void sendGroup(String a) throws IOException {
      List<String> list = Arrays.asList(a.split(","));
      for (ClientThread t : threadList) {
        if (list.contains(t.username) && !t.equals(currentThread())) {
          t.os.write(("group\n" + a + "\n").getBytes());
        }
      }
    }
    
  
    public void sendExit(String a) throws IOException {
      for (ClientThread t : threadList) {
        t.os.write(("exit\n" + a + "\n").getBytes());
      }
    }
  
    public void sendUserList() throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("userList\n");
      stringBuilder.append(" ");
      for (String a : userList) {
        stringBuilder.append(a).append(" ");
      }
      stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "\n");
      for (ClientThread t : threadList) {
        t.os.write(stringBuilder.toString().getBytes());
      }
    }
  }
}
