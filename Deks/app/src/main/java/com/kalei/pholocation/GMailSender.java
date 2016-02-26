package com.kalei.pholocation;

import com.flurry.android.FlurryAgent;
import com.kalei.interfaces.IMailListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    public IMailListener mMailListener;

    static {
        Security.addProvider(new com.kalei.pholocation.JSSEProvider());
    }

    public GMailSender(String user, String password, IMailListener listener) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        this.user = user;
        this.password = password;
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
        File image = new File(filename);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bytes);

        File f = new File(filename);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();

        //end of new shit
        Log.i("Reid", "attaching file: " + filename);
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);
        _multipart.addBodyPart(messageBodyPart);

        BodyPart messageBodyPart2 = new MimeBodyPart();
        messageBodyPart2.setText(body);
//        messageBodyPart2.setText(subject);

        _multipart.addBodyPart(messageBodyPart2);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients, final String filename) throws Exception {
        try {

            addAttachment(filename, body);
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

            Transport.send(message);
            Log.i("Reid", "Sending mail");

            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    mMailListener.onMailSucceeded(filename);
                }
            }));
        } catch (final Exception e) {
            runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    mMailListener.onMailFailed(e, filename);
                }
            }));

            FlurryAgent.logEvent("failed to send: " + e.getMessage());
            Log.i("Reid", "FAILED: " + e.getMessage());
        }
    }

    public void setOnMailListener(final IMailListener mailListener) {
        mMailListener = mailListener;
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
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