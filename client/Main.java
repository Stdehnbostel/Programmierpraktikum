import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Main extends Thread {

    private String serverName;
    private String userName;
    private String pwd;
    private Socket socket;
    private JTextArea userList;
    private JList<String> roomList;
    private ObjectOutputStream out;
    ObjectInputStream in;
    private LinkedList<BufferedImage> images;
    private LinkedList<byte[]> pdfs;
    private String[] privateMessages;

    public Main(String serverName, 
                LinkedList<BufferedImage> imgs,
                LinkedList<byte[]> pdfs,
                String[] pM) {
        this.serverName = serverName;
        this.images = imgs;
        this.pdfs = pdfs;
        this.privateMessages = pM;
    }

    public ObjectInputStream login() {
        
        try {
            this.socket = new Socket(serverName, 1234);

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            this.out = new ObjectOutputStream(socket.getOutputStream());

            Object msg;

            msg = input.readObject();
            
            String requestUsername = "";

            if (msg instanceof String) {
                requestUsername = msg.toString();
            }
                
            if(requestUsername.equals("Nutzernamen eingeben:")) {
                out.writeObject(this.userName);
                out.flush();
            }

            msg = input.readObject();
            String requestPwd = "";
            
            if (msg instanceof String) {
                requestPwd = msg.toString();
            }

            if(requestPwd.equals("Log dich mit deinem Passwort ein:") ||
                requestPwd.equals("Registriere dich mit deinem Passwort:")) {
                out.writeObject(this.pwd);
                out.flush();
            }

            return input;
        } catch (IOException ie) {
            System.out.println("IOException occured in client main: " + ie + ie.getStackTrace());
        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundException occured in client main: " + ce.getStackTrace());
        }catch (Exception e) {
            System.out.println("Exception occured in client main: " + e.getStackTrace() + e);
        } 

        return null;
    }

    public void send(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("IOException occurd in Main" + e);
        }
    }

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("IOException occurd in Main" + e);
        }
    }

    public void sendPic(String msg) {
        File f = new File(msg);
        BufferedImage bimg;
        String format = getFormat(msg);
        
        if (f.isFile()) {
            try {
                bimg = ImageIO.read(f);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bimg, format, baos);
                baos.close();
                Message pic = new Message(msg, Base64.getEncoder().encodeToString(baos.toByteArray()));
                out.writeObject(pic);
                out.flush();
            
            } catch (IOException e) {
                System.out.println("IOException occurd in Main" + e);
            }
        }
        
    }

    public void sendPdf(String msg) {
        File f = new File(msg);
        
        if (f.isFile()) {
            System.out.println("Send: " + msg);
            try {
                byte[] pdf = Files.readAllBytes(Paths.get(msg));
                Message pic = new Message(msg, Base64.getEncoder().encodeToString(pdf));
                out.writeObject(pic);
                out.flush();
            
            } catch (IOException e) {
                System.out.println("IOException occurd in Main" + e);
            }
        }
        
    }

    public ObjectInputStream getObjectInputStream() {
        return in;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setUserList(JTextArea userList) {
        this.userList = userList;
    }

    public void setRoomList(JList<String> roomList) {
        this.roomList = roomList;
    }

    public String getUserName() {
        return this.userName;
    }

    private String getFormat(String filePath) {
        Pattern formatPng = Pattern.compile(".*.png");
        Pattern formatJpeg = Pattern.compile(".*.jpg");
        Pattern formatBmp = Pattern.compile(".*.bmp");
        Pattern formatGif = Pattern.compile(".*.gif");
        Pattern formatPdf = Pattern.compile(".*.pdf");

        Matcher matcher = formatPng.matcher(filePath);
        if (matcher.matches()) {
            return "png";
        }
        matcher = formatJpeg.matcher(filePath);
        if (matcher.matches()) {
            return "jpg";
        }
        matcher = formatBmp.matcher(filePath);
        if (matcher.matches()) {
            return "bmp";
        }
        matcher = formatGif.matcher(filePath);
        if (matcher.matches()) {
            return "gif";
        }
        matcher = formatPdf.matcher(filePath);
        if (matcher.matches()) {
            return "pdf";
        }
        return "";
    }

    public void decodeMessage(Message msg, ArrayList<String> currentRoom) {

        if (msg.type.equals("Rooms")) {
            String[] response = msg.toStringArray();
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    roomList.setListData(response);
                }
            });
        }

        if (msg.type.equals("Room")) {
            if (((String)msg.msg).equals("")) {

                currentRoom.clear();
            } else {
                currentRoom.add((String)msg.msg);
            }
            sendMessage(msg);
        }
            
        if (((Message)msg).type.equals("Users")) {
            String response = msg.toString();
            userList.setText(response);
        }

        if (msg.type.equals("Private") && msg.msg instanceof String[]) {
            String[] pM = (String[])msg.msg;
            privateMessages[0] = pM[0];
            privateMessages[1] = pM[1];
        }
            
        String fileEnding = getFormat(((Message)msg).type);
        boolean isImage = !fileEnding.equals("") && !fileEnding.equals("pdf");
        boolean isPdf = fileEnding.equals("pdf");

        if (isImage) {
            showImage(msg, fileEnding);
        }

        if (isPdf) {
            showPdf(msg);
        }
    }

    public void exit(String msg) {
        send(msg);
        try {
        socket.close();
        } catch (IOException e) {
            System.out.println("IOException occurred while trying to close socket" + e + e.getStackTrace());
        }
    }

    private void showImage(Message img, String fileEnding) {
        String encodedImg = (String)img.msg;
        String fileName = "temp." + fileEnding;

        System.out.println("received a BufferedImage");
        try {
            File out = new File(fileName);
                    
            BufferedImage newImg = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encodedImg)));
            ImageIO.write(newImg, fileEnding, out);
            images.add(newImg);
        } catch (IOException e) {
            System.out.println("IOExeption occured sending file");
        }
    }

    private void showPdf(Message msg) {
        String encodedPdf = (String)msg.msg;
        System.out.println("received a BufferedImage");
        byte[] pdf = Base64.getDecoder().decode(encodedPdf);
        this.pdfs.add(pdf);
    }
}