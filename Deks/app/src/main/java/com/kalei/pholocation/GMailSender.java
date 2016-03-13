package com.kalei.pholocation;

import com.kalei.PhotoLocationApplication;
import com.kalei.interfaces.IMailListener;

import android.os.StrictMode;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import static com.google.android.gms.internal.zzir.runOnUiThread;

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.mailgun.org";
//    private String mailhost = "smtp.gmail.com";

    private String user;
    private String password;
    private Session session;
    private String caption;
    public IMailListener mMailListener;

    static {
        Security.addProvider(new com.kalei.pholocation.JSSEProvider());
    }

    public GMailSender(String user, String password, String caption, IMailListener listener) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        this.user = user;
        this.password = password;
        this.caption = caption;
        mMailListener = listener;
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.EnableSSL.enable", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getInstance(props, this);
        _multipart = new MimeMultipart("mixed");
    }

    private Multipart _multipart;

    public void addAttachment(String filename, String body) throws Exception {
        Log.i("pl", "attaching file: " + filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        _multipart.addBodyPart(messageBodyPart);

        BodyPart messageBodyPart2 = new MimeBodyPart();
        body = (caption != null && caption.length() > 0 ? caption + "\n\n" + body : body);
        messageBodyPart2.setText(body);
//        messageBodyPart2.setText(subject);

        _multipart.addBodyPart(messageBodyPart2);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients, final String filename, final String scaledImage)
            throws Exception {
        try {

            addAttachment(scaledImage, body);
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress("picture@photolocation.com"));
            message.setSubject(subject);
            message.setDataHandler(handler);
            message.setContent(_multipart);
            if (recipients.indexOf(',') > 0) {
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            } else {
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            }

            if (!PhotoLocationApplication.debug) {
                Transport.send(message);
            }
            Log.i("pl", "Sending mail");

            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    File f = new File(scaledImage);
                    Log.i("pl", "scaled image size: " + f.length());
                    f.delete(); //delete old smaller file
                    if (mMailListener != null) {
                        mMailListener.onMailSucceeded(scaledImage);
                    }
                }
            }));
        } catch (final Exception e) {
            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    if (mMailListener != null) {
                        mMailListener.onMailFailed(e, scaledImage);
                    }
                }
            }));

            Log.i("pl", "FAILED: " + e.getMessage());
        }
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public String getContentType() {
            if (type == null) {
                return "application/octet-stream";
            } else {
                return type;
            }
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}