import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;

public class Main extends Thread {

    private String serverName;
    private String userName;
    private String pwd;
    private Socket socket;
    private JTextArea chat;
    private JTextArea userList;
    private JTextArea roomList;
    private String room;
    private ObjectOutputStream out;
    private LinkedList<BufferedImage> images;
    private LinkedList<byte[]> pdfs;

    public Main(String serverName, 
                LinkedList<BufferedImage> imgs,
                LinkedList<byte[]> pdfs) {
        this.serverName = serverName;
        this.images = imgs;
        this.pdfs = pdfs;
        this.room = "";
    }

    public void run() {
        
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
                System.out.println(requestPwd);
            }
           
           while(!socket.isClosed()) {
            
            
            msg = input.readObject();

            if (msg instanceof Message) {
                System.out.print(((Message)msg).type);
            } else {
                System.out.print("Not a Message");
            }
            
            if (msg instanceof String) {
                String response = msg.toString();
                chat.setText(chat.getText() + response + "\n");

                if (response.equals("exit")) {
                out.writeObject(response);
                socket.close();
                }
            }
            if (msg instanceof Message && ((Message)msg).type.equals("String")) {
                String response = msg.toString();
                userList.setText(response);
            }

            if (msg instanceof Message && ((Message)msg).type.equals("Rooms")) {
                String response = msg.toString();
                roomList.setText(response);
            }
            
            boolean isPng = false;
            Pattern formatPng = Pattern.compile(".*.png");
            boolean isJpeg = false;
            Pattern formatJpeg = Pattern.compile(".*.jpg");
            boolean isBmp = false;
            Pattern formatBmp = Pattern.compile(".*.bmp");
            boolean isGif = false;
            Pattern formatGif = Pattern.compile(".*.gif");
            boolean isPdf = false;
            Pattern formatPdf = Pattern.compile(".*.pdf");
            
            if (msg instanceof Message) {
                Matcher matcher = formatPng.matcher(((Message)msg).type);
                isPng = matcher.matches();
                matcher = formatJpeg.matcher(((Message)msg).type);
                isJpeg = matcher.matches();
                matcher = formatBmp.matcher(((Message)msg).type);
                isBmp = matcher.matches();
                matcher = formatGif.matcher(((Message)msg).type);
                isGif = matcher.matches();
                matcher = formatPdf.matcher(((Message)msg).type);
                isPdf = matcher.matches();
            }
            boolean isImage = isPng || isJpeg || isBmp || isGif;

            if (msg instanceof Message && isImage) {
                Message img = (Message)msg;
                String fileName = "";
                if (isPng) {
                    fileName = "temp.png";
                } else if (isJpeg) {
                    fileName = "temp.jpg";
                } else if (isBmp) {
                    fileName = "temp.bmp";
                } else if (isGif) {
                    fileName = "temp.gif";
                }

                System.out.println("received a BufferedImage");
                try {
                    File out = new File(fileName);
                    
                    BufferedImage newImg = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode((String)img.msg)));
                    ImageIO.write(newImg, "png", out);
                    images.add(newImg);
                } catch (IOException e) {
                    System.out.println("IOExeption occured sending file");
                }
            }

            if (msg instanceof Message && isPdf) {
                Message img = (Message)msg;
                
                System.out.println("received a BufferedImage");
                byte[] pdf = Base64.getDecoder().decode((String)img.msg);
                this.pdfs.add(pdf);
            } else {
                System.out.println("Not a Pdf");
            }
        }
            
        } catch (IOException ie) {
            System.out.println("IOException occured in client main: " + ie + ie.getStackTrace());
        } catch (ClassNotFoundException ce) {
            System.out.println("ClassNotFoundException occured in client main: " + ce.getStackTrace());
        }catch (Exception e) {
            System.out.println("Exception occured in client main: " + e.getStackTrace() + e);
        } 
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
        String format = "";
        
        if (f.isFile()) {
            System.out.println("Send: " + msg);
            Pattern formatPng = Pattern.compile(".*.png");
            Matcher matcherPng = formatPng.matcher(msg);

            Pattern formatJpeg = Pattern.compile(".*.jpg");
            Matcher matcherJpeg = formatJpeg.matcher(msg);

            Pattern formatBmp = Pattern.compile(".*.bmp");
            Matcher matcherBmp = formatBmp.matcher(msg);

            Pattern formatGif = Pattern.compile(".*.gif");
            Matcher matcherGif = formatGif.matcher(msg);

            if (matcherPng.matches()) {
                format = "png";
            } else if (matcherJpeg.matches()) {
                format = "jpg";
            } else if (matcherBmp.matches()) {
                format = "bmp";
            } else if (matcherGif.matches()) {
                format = "gif";
            }

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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setChat(JTextArea chat) {
        this.chat = chat;
    }

    public void setUserList(JTextArea userList) {
        this.userList = userList;
    }

    public void setRoomList(JTextArea roomList) {
        this.roomList = roomList;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}